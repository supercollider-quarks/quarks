////////////////////////////////////////////////////////////////////////////
//
// Copyright (C) Fundació Barcelona Media, October 2014 [www.barcelonamedia.org]
// Author: Andrés Pérez López [contact@andresperezlopez.com]
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; withot even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <http://www.gnu.org/licenses/>
//
////////////////////////////////////////////////////////////////////////////
//
// SpatDifLogger.sc
//
// Log all incoming OSC-SpatDIF messages to a textFile
//
//////////////////////////////////////////////////////////////////////////////
//
// SpatDifPlayer.sc
//
// Utility for playing back a given OSC-SpatDIF log file
//
////////////////////////////////////////////////////////////////////////////

SpatDifLogger{

	var <timelogfile;
	var <offset;
	var <oscRecFunc;
	var <>log = true;
	var <filename;
	var isEmpty = true;
	var lastTime, <>deltaTime = 0.01;// default to 10 ms



	*new{ |fn,path,extensions,author,host,date,session,location,annotation|
		^super.new.init( fn,path,extensions,author,host,date,session,location,annotation);
	}

	init{ |fn,path,extensions,author,host,date,session,location,annotation|

		// create receiver function
		oscRecFunc = { |msg, time|
			if (log) {
				var route=msg[0].asString.split($/)[1]; //first word in osc string
				// filter spatdif messages
				if (route.isNil.not and:{ route == "spatdif"}) {
					this.writeLine( msg,time )
				};
			};
		};

		// instanciate osc receiver
		this.resetTime;
		thisProcess.addOSCRecvFunc( oscRecFunc );


		// create logger file
		if (fn.isNil) {fn = "TimeFileLog" ++ "_" ++ Date.localtime.stamp ++ ".txt";};
		filename = path +/+ fn;
		timelogfile = File(filename,"w");

		// WRITE SPATIDIF META SECTION

		this.writeMetaVersion;
		timelogfile.write("\n");
		timelogfile.write("\n");

		this.writeMetaExtensions(extensions);
		timelogfile.write("\n");

		this.writeMetaInfo(author,host,date,session,location,annotation);
		timelogfile.write("\n");

		this.writeMetaEnd;
		timelogfile.write("\n");
		timelogfile.write("\n");
	}

	resetTime{
		offset = Process.elapsedTime;
		lastTime = offset;
	}

	writeMetaVersion { |version = 0.3|
		timelogfile.write( "/spatdif/version " ++ version.asString );
	}

	writeMetaExtensions { |extensions|
		if (extensions.notEmpty) {
			timelogfile.write( "/spatdif/meta/extensions ");
			extensions.do{ |ext|
				timelogfile.write( ext.asString ++ " ");
			}
		}
	}

	writeMetaInfo { |...info|
		var elements = #["author","host","date","session","location","annotation"];
		info.do{ |field,i|
			if (field.isNil.not) {
				timelogfile.write( "/spatdif/meta/info" +/+ elements[i] ++ " " ++ field ++ "\n");
			}
		}
	}

	writeMetaEnd {
		timelogfile.write( "################################" );
	}


	writeLine{ |msg,time|
		isEmpty = false;

		// allow pseudo-bundles
		if ((time - lastTime) > deltaTime) {
			timelogfile.write("/spatdif/time " ++ (time-offset).asString ++ "\n");
		};

		msg.do{ |value| timelogfile.write(value.asString ++ " ")};
		timelogfile.write("\n");

		lastTime = time;
	}

	save {
		// close the file and then open it with "a"
		timelogfile.close;
		timelogfile = File(filename,"a");

	}

	close {
		timelogfile.close;
		thisProcess.removeOSCRecvFunc( oscRecFunc );
		// delete the file if nothing was written
		if (isEmpty) {File.delete(filename)};
	}
}

// reads a data network log and plays it

SpatDifPlayer {

	var netAddr; // direction where to send data
	var file;
	var fileStrings, metaSection;
	var timeStamps, spatDifCommands;
	var deltas; // difference times between timestamps
	var task;
	var taskController;
	var taskPlaying = false;
	var taskLoop = false;
	var <>verbose = true;

	*new { |netAddr|
		^super.new.init(netAddr);
	}

	init { |myNetAddr|
		netAddr = myNetAddr ? NetAddr.localAddr;
		metaSection = List.new;
		timeStamps = List.new;
		spatDifCommands = List.new;
	}

	loadFile { |pathToFile|
		var provList;

		file = File.new(pathToFile,"r");

		// fill the array with file lines, discarding the empty ones and the comments
		fileStrings = file.readAllString.split($\n).reject(_=="").reject({|e|e[0]==$#});

		// close the file
		file.close;


		// classify strings
		fileStrings.do{ |string|

			var line = string.split($/);
			var word = line[2].split($ );

			// word[0] is the first word in the second slash
			if (word[0] == "meta" or:{word[0] == "version"}) {
				metaSection.add(string);
			} {
				if (word[0] == "time") {
					timeStamps.add(word[1].asFloat);

					if (provList.isNil.not) { // avoid first empty array
						spatDifCommands.add(provList.asArray);
					};
					provList = List.new;
				} {
					var commentIndex;
					// adequate string
					string = string.split($ ).reject(_=="");
					// take out comments
					commentIndex = string.indicesOfEqual("#");

					if (commentIndex.isNil) {
						provList.add(string);
					} {
						provList.add(string[0..commentIndex[0]-1])
					}
				}
			};
		};
		spatDifCommands.add(provList.asArray); // add last list

		// convert timestamps into deltas
		deltas=Array.new(timeStamps.size);
		deltas.add(timeStamps[0]);
		timeStamps[..timeStamps.size-2].do{|time,i|
			deltas.add(timeStamps[i+1]-time)
		};

	}

	createTask { |loop=1,play=true|

		"createTask".postln;
		loop.postln;
		play.postln;

		task = Task({
			this.loadMetaSection;
			loop.do {
				deltas.do{ |delta,i|
					delta.wait;
					spatDifCommands[i].do{|cmd|
						netAddr.sendBundle(0,cmd);
						if (verbose) {cmd.postln};
					}
				};
				// give some extra time before finishing:
				// if not, SpatialRender.sendToWorld will check spatDifPlayer_isPlaying=false
				// and will not send the last message
			};
			0.1.wait;
		});

		// task controller
		taskController = SimpleController(task);
		taskController.put(\userPlayed, {taskPlaying = true});
		taskController.put(\userStopped, {taskPlaying = false});
		taskController.put(\stopped, {taskPlaying = false});

		if (play) {
			task.play;
		}
	}


	loadMetaSection {
		metaSection.do{ |string|
			netAddr.sendMsg(string)
		}
	}

	start{ |loop=1|
		this.createTask(loop,play:true);
	}

	play{
		task.play;
	}

	pause {
		task.pause;
	}

	reset {
		task.reset;
	}

	isPlaying {
		^taskPlaying;
	}


}
