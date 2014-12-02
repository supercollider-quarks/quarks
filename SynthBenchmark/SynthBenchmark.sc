/*
SynthBenchmark: A class for using NRT mode to benchmark synths by their time taken.
(c) Dan Stowell 2010, published under GPL3+

REQUIRES GNU TIME COMMAND - on macports it's the gtime package, "sudo port install gtime"

b = SynthBenchmark();
b.run;   "";

output should be something like

a SynthBenchmark started 2010-01-28 19:55
-------------------------------------
unaryops1:   26.1022s
binaryops1:   29.3034s
phasevocoding:   33.3309s
sinoscs1:   13.4443s
pulseoscs1:   13.4448s
-------------------------------------

*/
SynthBenchmark {

var <dirs, <paths, <startTime, <endTime, <results;

classvar <>cmd, <>defaultDuration=10;

*new { |dirs|
	^super.new.init(dirs)
}

run{ |post=true|
	results = Dictionary[];
	
	startTime = Date.getDate;
	endTime = nil;
	
	// First we run a "dummy" benchmark in the hope that this mitigates cacheing effects in loading scsynth etc
	this.runOne(this.class.filenameSymbol.asString.dirname +/+ "z_dummy_one.scd");
	
	paths.do{|apath|
		results.put(apath, this.runOne(apath))
	};
	
	endTime = Date.getDate;
	// when all is done, post
	if(post){this.postln};
}

init { |argDirs|
	dirs = argDirs ?? {[this.class.defaultDir]};
	if(dirs.isString){dirs = [dirs]};
	// Scan the dirs for benchmark files
	paths = [];
	dirs.do{|dir|
		paths = paths ++ (dir +/+ "*_bench.scd").pathMatch;
	};
}

*defaultDir {
	^this.filenameSymbol.asString.dirname +/+ "benchmarks"
}

*initClass {
	StartUp.add{
		cmd = Platform.case(
			\osx, {"/opt/local/bin/gtime"},
			{"/usr/bin/time"}
		)
	}
}

runOne{|path|
	var synthdef, scorepath, tmpscore=false, score, synthdefbinpath, fp, options, fullCmd, outputFilePath, res;
	//"runOne: %".format(path).postln;
	
	// Interpret the synthdef file to make our synthdef
	synthdef = path.loadPaths[0];

	// ensure the synthdef is written somewhere for the server to read it
	synthdefbinpath = PathName.tmp;// +/+ "benchSystemCmd_%_%.scsyndef".format(synthdef.name, this.hash.asString);
	synthdef.writeDefFile(synthdefbinpath, true);
	synthdefbinpath = synthdefbinpath +/+ synthdef.name ++ ".scsyndef";
		
	// Check for an existing OSC score, otherwise we create a default one
	scorepath = path.splitext[0] ++ "_score.scd";
	if(File.exists(scorepath).not){
		scorepath = PathName.tmp +/+ "benchSystemCmd_%_%.osc".format(synthdef.name, this.hash.asString);
		tmpscore = true;
		score = [
			[0, [\d_load, synthdefbinpath,  // load synthdef
					[\s_new, synthdef.name, 1001, 1, 0]]], // start it
			[defaultDuration, [\c_set, 0, 0]] // dummy command marks end
		];
		Score.write(score, scorepath);
	};
	
	
	// Invoke the NRT synthesis , remember to set scsynth verbosity low , don't read synthdefs dir, send result to dev null
	options = Server.default.options.copy
		.loadDefs_(false)
		.verbosity_(-1)
		.numOutputBusChannels_(2)
		.memSize_(262144);

	outputFilePath = PathName.tmp +/+ "runOne.aiff";
	fullCmd = Score.program + " -N" + scorepath + "_" + outputFilePath.quote + "44100 AIFF int16"
			+ options.asOptionsString;

	res = fullCmd.benchSystemCmd;
	[synthdefbinpath].do{|f| File.delete(f)};
	if(tmpscore){File.delete(scorepath)};
	^res
}

asString {
	var strs;
	^if(results.isNil){
		"a SynthBenchmark (not yet run)"
	}{
		strs = results.collect{|time, which|
			"%:   % s".format(which.basename.splitext[0].replace("_bench", ""), time)
		}.asArray;
		strs.sort;
		"a SynthBenchmark started %
%-------------------------------------
%
-------------------------------------
finished %
".format(startTime, "uname -mrsv".unixCmdGetStdOut,
	strs.join(Char.nl), 
	endTime ? "not yet")
	}
}

write { |path, mode="w"|
	var fp = File(path, mode);
	if(fp.isOpen){
		fp.write(this.asString).flush.close
	}{
		^"SynthBenchmark.write unable to open path %".format(path).error
	}
}


// Output statistics comparing two benchmarks against each other
compare { |that|
	var k1, k2, a1, a2, wsr, wsrN, avgdiff;
	k1 = this.results.keys.asArray;
	k2 = that.results.keys.asArray;
	k1.sort;
	k2.sort;
	if(k1 != k2){ ^"SynthBenchmark.compare: keys don't match, cannot compare".error };
	
	// get times as sorted lists
	a1 = k1.collect{|k| this.results[k]};
	a2 = k2.collect{|k| that.results[k]};
	
	// wilcoxonSR is in MathLib quark
	# wsr, wsrN = wilcoxonSR(a1, a2, false);
	avgdiff = median(a2 / a1);
	
	^"The latter benchmark's tests took on average % percent the time of the former (Wilcoxon signed-rank %, N=%)".format((avgdiff * 100).asStringPrec(4), wsr, wsrN);
}


} // end class

+ String {
	/*
	// benchSystemCmd: use the gnu "time" command to time a single process's CPU time
	"find /usr -name parp".benchSystemCmd
	*/
	benchSystemCmd {
		var tmppath, fullcmd, fp, output, res, time_u, time_s;
		tmppath = PathName.tmp +/+ "benchSystemCmd_%.txt".format(this.hash.asString);
		fullcmd = SynthBenchmark.cmd + "-f" + "%U,%S".quote + "--output" + tmppath.quote + this;
		fullcmd.systemCmd;
		fp = File(tmppath, "r");
		if(fp.isOpen.not){^"benchSystemCmd couldn't open tmppath %".format(tmppath).error};
		output = fp.readAllString;
		fp.close;
		// http://stackoverflow.com/questions/556405/what-do-real-user-and-sys-mean-in-the-output-of-time1
		res = output.split($,);
		if(res.size!=2){^("benchSystemCmd failed to parse times from:" ++ Char.nl ++ output).error};
		time_u = res[0].asFloat;
		time_s = res[1].asFloat;
		File.delete(tmppath);
		^ time_u + time_s
	}
}
