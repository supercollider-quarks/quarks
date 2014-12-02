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
// OrientationController.sc
//
// A convenience class for managing interaction between a SSWorld and a device with orientation sensing capabilities (for example a smartphone)
//
// Provides easy access to events, and looping capabilities
////////////////////////////////////////////////////////////////////////////


OrientationController {

	var <azimuth, <elevation, <roll;
	var dAzi;

	var <acceleration;
	var <gravity;

	var <logging = false;
	var logTime;
	var <isPlaying = false;

	var replayTask;
	var oscListener;
	var action;
	var logger;


	*new {
		^super.new.initOC;
	}

	initOC {

		acceleration = Array.newClear(3); //[x,y,z] components of linear acceleration
		gravity = Array.newClear(3); //[x,y,z] components of linear acceleration

		replayTask = Dictionary.new;
		oscListener = Dictionary.new;
		action = Dictionary.new;

		logger = Dictionary.new;

		//////////////////////////////////////////////////////////////////////////////
		// define listeners and start to perform
		//////////////////////////////////////////////////////////////////////////////

		///////////// TYPE_ORIENTATION /////////////
		oscListener.add(\orientation -> OSCdef(\orientationListener,{ |msg, time, addr, recvPort|

			if (azimuth.isNil) { //first message received
				dAzi=msg[1]; //get this azimuth as our reference 0
			};
			azimuth = msg[1];
			elevation = msg[2];
			roll = msg[3];


			//transform into SSWorld coordinate system
			azimuth = (azimuth - dAzi).neg.degree2rad.wrap(0,2pi);
			elevation = elevation.neg.degree2rad;
			roll = roll.neg.degree2rad;

			if (isPlaying.not) {
				//call user-defined function
				action[\orientation].value([azimuth,elevation,roll],time);
				//logger
				if (logging) {
					logger[\orientation].add([time-logTime,[azimuth,elevation,roll]]);
				};
			};
			},"/orientationController/orientation")
		);


		///////////// TYPE_LINEAR_ACCELERATION /////////////
		oscListener.add(\acceleration -> OSCdef(\accelerationListener,{ |msg, time, addr, recvPort|

			acceleration[0] = msg[1];
			acceleration[1] = msg[2];
			acceleration[2] = msg[3];

			if (isPlaying.not) {
				//call user-defined function
				action[\acceleration].value(msg[(1..3)],time);
				//logger
				if (logging) {
					logger[\acceleration].add([time-logTime,msg[(1..3)]]);
				};
			};

			},"/orientationController/acceleration");
		);

		///////////// TYPE GRAVITY /////////////
		oscListener.add(\gravity -> OSCdef(\gravityListener,{ |msg, time, addr, recvPort|

			gravity[0] = msg[1];
			gravity[1] = msg[2];
			gravity[2] = msg[3];

			if (isPlaying.not) {
				//call user-defined function
				action[\gravity].value(msg[(1..3)],time);
				//logger
				if (logging) {
					logger[\gravity].add([time-logTime,msg[(1..3)]]);
				};
			};

			},"/orientationController/gravity")
		);

	}


	enableOscListeners {
		oscListener.do(_.enable);
	}

	disableOscListeners {
		oscListener.do(_.disable);
	}

	setAction { |key, aFunc|
		action.add(key -> aFunc);
	}

	//////////////////////////////////////////////////////////////////////////////
	// logger
	//////////////////////////////////////////////////////////////////////////////


	log_ { |state|
		logging = state;
		if (state) {
			this.initializeBuffers;
		}
	}

	initializeBuffers {
		logger.add(\orientation -> List.new);
		logger.add(\acceleration -> List.new);
		logger.add(\gravity -> List.new);
		logTime = Main.elapsedTime;
	}


	//////////////////////////////////////////////////////////////////////////////
	// replay
	//////////////////////////////////////////////////////////////////////////////

	replay { |repeat=inf|

		if (isPlaying.not) {
			isPlaying = true;

			logger.keys.do { |key| // if never logger, will do nothing

				if (action[key].isNil.not) {

					var times= [0,logger[key].flop[0]].flat;
					var deltas = (1..times.size-1).collect{|i| times[i] - times[i-1] };


					var task = Task({
						repeat.do({
							deltas.do({ |delta,i|
								delta.wait;
								action[key].value(logger[key][i][1]);
							})
						})
					});

					replayTask.add(key -> task);
				}
			};

			replayTask.do(_.play);
		} {
			"already playing".postln;
		}
	}

	pause {
		isPlaying = false;
		replayTask.do(_.pause);
		logging = false; // in order to later recover the data
	}

	stop {
		isPlaying = false;
		replayTask.do(_.pause);
		// if logging was true, ie it was recording, initialize again buffers for new data acquisition
		if (logging) {
			this.log_(true); // to create new data
		}
	}

	resume {
		isPlaying = true;
		replayTask.do(_.resume);
	}


}
