//redFrik

//--related:
//RedMatrixMixerGUI, RedEffectsRack, RedMixer

RedMatrixMixer {
	var <group, <cvs, <args,
		<isReady= false, groupPassedIn,
		<nIn, <nOut, <synth, <os, <defString;
	*new {|nIn= 8, nOut= 8, in= 0, out= 0, group, lag= 0.05|
		^super.new.initRedMatrixMixer(nIn, nOut, in, out, group, lag);
	}
	initRedMatrixMixer {|argNIn, argNOut, argIn, argOut, argGroup, argLag|
		var server;
		nIn= argNIn;
		nOut= argNOut;
		
		if(argGroup.notNil, {
			server= argGroup.server;
			groupPassedIn= true;
		}, {
			server= Server.default;
			groupPassedIn= false;
		});
		
		//--create cvs and argument array
		cvs= (
			\in: CV.new.spec_(\audiobus.asSpec),
			\out: CV.new.spec_(\audiobus.asSpec),
			\lag: CV.new.spec_(ControlSpec(0, 99, \lin, 0, argLag))
		);
		cvs.in.value= argIn;
		cvs.out.value= argOut;
		args= [
			\in, cvs.in,
			\out, cvs.out,
			\lag, cvs.lag
		];
		
		forkIfNeeded{
			if(groupPassedIn.not, {
				server.bootSync;
				group= Group.after(server.defaultGroup);
				server.sync;
				CmdPeriod.doOnce({group.free});
			}, {
				group= argGroup;
			});
			
			//--send definitions
			this.def.add;
			server.sync;
			
			//--create synth
			synth= Synth.controls(\redMatrixMixer, args, group, \addToTail);
			server.sync;
			
			//--create more cvs and map to synth
			os= List.new;
			nOut.do{|i|
				var name= ("o"++i).asSymbol;
				var cv, arr= 0.dup(nIn);
				if(i<nIn, {arr= arr.put(i, 1)});
				cv= CV.new.sp(arr, 0, 1, 0, \lin);
				cvs.put(name, cv);
				os.add(cv);
				synth.setControls([name, cv]);
			};
			server.sync;
			isReady= true;
		};
	}
	def {
		defString= "SynthDef('redMatrixMixer', {|in= 0, out= 0, lag= 0.05";
		nOut.do{|i|
			var arr= 0.dup(nIn);
			if(i<nIn, {arr= arr.put(i, 1)});
			defString= defString++", o"++i++"= #"++arr;
		};
		defString= defString++"|\n\tvar z= In.ar(in, "++nIn++");";
		nOut.do{|i| defString= defString++"\n\tvar m"++i++"= Mix(z*Ramp.kr(o"++i++", lag));"};
		nOut.do{|i| defString= defString++"\n\tReplaceOut.ar(out+"++i++", m"++i++");"};
		defString= defString++"\n});";
		^defString.interpret;
	}
	defaults {
		cvs.do{|cv| cv.value= cv.spec.default};
	}
	free {
		synth.free;
		if(groupPassedIn.not, {group.free});
	}
	gui {|position|
		^RedMatrixMixerGUI(this, position);
	}
}
