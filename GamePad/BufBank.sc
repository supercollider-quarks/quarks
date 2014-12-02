/*
unclean behaviour when .clearJams is called after loading samples: they index at the tail of the list, allowing multiple sets of Jams to be allocated (which is only trouble because they are named the same way..)
*/

BufBank {
	classvar <>server, <>numBufs = 64, <>numJams = 4, <>jamDur = 10, 
	<bufs, <jamBufs, <fileBufs, <>recDurs,
	<>root = "~/Music/echo_SBm", <subFolders, <loaded, <folderID=0;
	 

	*init { arg serv; 
		server = serv ? Server.default;			// make blank buffers to reserve numbers
		if (server.serverRunning.not, { "boot server first!".postln; ^this });

		bufs = { |i| Buffer(server, 0, 1) } ! numBufs; 
		jamBufs = bufs.keep(numJams);
		fileBufs = bufs.drop(numJams);

		loaded = List[];
		this.clearJams; 
	}
	
	*clearJams { 
		recDurs = 0 ! numJams;  // fix this ! maybe  not needed
		this.jamBufs.do { |jam, i| 
			jam.numFrames_(server.sampleRate * jamDur).alloc; 
			loaded.add(("JamBuf_" ++ jam.bufnum).postln);
		}
	}
	*loadJams { "please use clearJams!".postln; this.clearJams }
	
	*fileBufNums  { ^this.loaded.drop(numJams).collect({|it,i| BufBank.fileBufs[i].bufnum; }) }
	*usedBufNums  { ^this.usedBufs.collect({|it,i| it.bufnum; }) }
	
	*tellBufs { var str = "";
		{
			this.bufs.do({|b| 
				if(b.numFrames > 0) 
				{
					b.bufnum.post; "    ".post; BufBank.loaded[b.bufnum].postln;
					str = str.catArgs(
						b.bufnum.asString ++ "   " 
						++ BufBank.loaded[b.bufnum].asString ++ "\n"
					);
				};
				nil;
			}); 
			Document.new("bufs", str).bounds_( Rect(1169, 165, 273, 500) );
		}.defer;
		nil
	}
	
	*usedBufs { 
		^this.bufs.select({|b| 
			(b.numFrames > 0) or:
			(b.path.notNil)
		}); 
	}
	
/////////////////////////////////////////////////////////////////////////////////
	*arRec { |bufnum=0, in=1, limGain=5, loop=0| 
		("//	jam" + bufnum + "recs: ").post; 
		if (in.isNumber, { ("audio" + in).postln; in = AudioIn.ar(in) }, { in.postln; }); 
		
		in = Limiter.ar(HPF.ar(in, 75) * limGain, 1);
//		in = Normalizer.ar(in, 1);
// 		Amplitude.kr(in).poll(Impulse.kr(4));

		^[ 	{ RecordBuf.ar(in, bufnum) * EnvGen.kr(Env([1, 1,0], [jamDur, 0]), doneAction: 2);
				Silent.ar; }, 
			{ BufWr.ar(in, bufnum, 
				Phasor.ar(0, BufRateScale.kr(bufnum), 0, BufFrames.kr(bufnum)), 
				loop: 1
			);
			Silent.ar;
			}
		].clipAt(loop.round.asInteger)
	}
/////////////////////////////////////////////////////////////////////////////////

	*findFolders { ^(subFolders = (root ++ "/*").pathMatch.sort) }
	
	*folderID_ { |index| ^folderID = index.wrap(0, subFolders.size - 1) }
	
	*stepFolder { |step=1, post=true| 
		folderID = (folderID + step).wrap(0, subFolders.size - 1); 
		if (post, { ("folder" + subFolders[folderID].basename + "   ok?").postln; });
		^folderID 
	}

	*loadFiles { |add=false, post=true| 
		var bufIDoffset=0, count=0, fileNames2Load; 
		
		if (server.serverRunning.not, { "boot server first!".postln; ^this });

		if (add) { 
			bufIDoffset = bufIDoffset + loaded.size - jamBufs.size ;
			"loaded already:".postln; 	loaded.printAll; "".postln;
		} { loaded = loaded.keep(jamBufs.size) /**/ };
	
		fileNames2Load = (subFolders[folderID] ++ "*").pathMatch;
		("from folder " ++ subFolders[folderID] ++ ", I load : ").postln;
	
		fileNames2Load.do { |path, i| 
			var buf; buf = fileBufs[i + bufIDoffset];
			if (buf.notNil, 
				{ 	buf.allocRead(path, completionMessage: {|buf| {buf.updateInfo()}.defer(1) }); 
					loaded.add(("	" ++ path.basename).postln); 
					count = count + 1;
				}, 
				{ "SORRY, out of buffer IDs.".postln; ^this }
			);
		};
		("numFiles:" + count + "\n").postln; 
	}
}

/*
s.boot;
BufBank.root = "~/Music/echo_SBm";
BufBank.findFolders;
BufBank.stepFolder(-1);
BufBank.stepFolder;

		// clear old buffers when clear-loading a new folder? 
BufBank.loadFiles;
BufBank.stepFolder; 
*/