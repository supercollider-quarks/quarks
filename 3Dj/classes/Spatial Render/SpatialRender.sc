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
// SpatialRender.sc
//
// This class implements a spatial render
//
// Render instance receives OSC messages from an external spatialization software
// and applies spatial coefficients to the existing input channels
//
// Each different source/channel is tracked inside encoders dictionary
//
// Distance cues are supported by means of distance attenuation and air absorption simulation
//
// ATK Binaural SynthDefs are treated specially, since head models should be loaded into buffers before instanciating Synths...
//
// TODO:
//
// -> Automatic adding to worldAddresses the direction from received messages
//
////////////////////////////////////////////////////////////////////////////


SpatialRender {

	var <server;
	// Dictionary with references to the Synth groups (one for each track)
	var <encoders;
	//Dictionary with references to the internal distance buses between synths
	var <distanceBuses;
	//Dictionary with references to the internal panner buses between synths
	var <pannerBuses;
	//Dictionary with references to the internal distance groups
	var <distanceGroups;
	//Dictionary with references to the individual absorption synths
	var <absorptionSynths;
	//Dictionary with references to the individual attenuation synths
	var <attenuationSynths;
	//Dictionary with references to the individual panner synths
	var <pannerSynths;
	// Dictionary with references to the channel of each source
	var <channels;
	// List with names to all sources
	var <sourceNames;
	// Dictionary storing ambisonics parameters for each source: [order,shape]
	var <ambisonicsParameters;
	// Dictionary with references to the object positions
	var <objectPositions;

	var defaultAmbisonicsOrder = 3;
	var defaultAmbisonicsShape = \point;

	var <absorptionModel;
	var <attenuationModel;
	var defaultAbsorptionModel = 1;
	var defaultAttenuationModel = 2;

	var <spatialTechnique;
	var defaultSpatialTechnique = \binaural;

	var <vbapSpeakerArray,<vbapSpeakerBuffer,<vbapNumSpeakers;

	var defaultBinauralDecoder = \cipic;
	var defaultSubjectID = 0065;
	var <binauralDecoder;
	var <binauralSubjectID;

	var <>verbose=true;

	var <spatDifLogger, <logOSC = true;
	var <spatDifLoggerPath;
	var <autosaveOSC = true, <>autosavePeriod=10;
	var autosaveTask;

	var <receiverFunctions;

	var <spatDifPlayer;

	var <>resendToWorld = false; // resend all world messages, for remote visualization
	var <worldAddresses;

	var numAudioOutputs = 24;



	*new{ | server, spatDifLoggerPath |
		^super.new.init(server,spatDifLoggerPath);
	}

	init { |myServer, mySpatDifLoggerPath|

		// // // // INIT VALUES // // // //

		server = myServer;
		spatialTechnique = defaultSpatialTechnique;

		//init vbap synthdef
		// just give some values for the synthdef not to crash

		vbapSpeakerArray = VBAPSpeakerArray.new(2, [ -30, 30, 0, -110, 110 ]); // 5.1
		{vbapSpeakerBuffer = vbapSpeakerArray.loadToBuffer}.defer(0.9); // give some time...
		vbapNumSpeakers = vbapSpeakerArray.numSpeakers;

		channels = Dictionary.new;
		encoders=Dictionary.new;
		distanceBuses = Dictionary.new;
		pannerBuses = Dictionary.new;
		distanceGroups = Dictionary.new;
		sourceNames = List.new;

		absorptionModel = defaultAbsorptionModel;
		attenuationModel = defaultAttenuationModel;

		absorptionSynths = Dictionary.new;
		attenuationSynths = Dictionary.new;
		pannerSynths = Dictionary.new;

		ambisonicsParameters = Dictionary.new;

		receiverFunctions=Dictionary.new;

		objectPositions = Dictionary.new;



		// // // // RECEIVER FUNCTIONS // // // //

		OSCdef(\addEntity,this.newSource,"/spatdifcmd/addEntity",nil);
		OSCdef(\removeEntity,this.endSource,"/spatdifcmd/removeEntity",nil);

		// internal configuration: send internal=false flag for avoid infinite osc feedback

		// distance cues
		OSCdef(\referenceDistance,{ |msg| this.setReferenceDistance(msg[1],false);
		},"/spatdif/distance-cues/reference-distance",nil);
		OSCdef(\maximumDistance,{ |msg| this.setMaximumDistance(msg[1],false);
		},"/spatdif/distance-cues/maximum-distance",nil);
		OSCdef(\maximumAttenuation,{ |msg| this.setMaximumAttenuation(msg[1],msg[2],false);
		},"/spatdif/distance-cues/maximum-attenuation",nil);
		OSCdef(\attenuationModel,{ |msg| this.setAttenuationModel(msg[1],false);
		},"/spatdif/distance-cues/attenuation-model",nil);
		OSCdef(\absorptionModel,{ |msg| this.setAbsorptionModel(msg[1],false);
		},"/spatdif/distance-cues/absorption-model",nil);

		// spatialization techniques settings
		OSCdef(\spatializationTechnique,{ |msg|  this.setSpatializationTechnique(msg[1],false);
		},"/spatdif/scliss/spatialization-technique");
		OSCdef(\binauralParameters,{ |msg| this.setBinauralDecoder(msg[1],msg[2],false);
		},"/spatdif/scliss/binaural-parameters",nil);
		OSCdef(\ambisonicsParameters,{ |msg| this.setAmbisonicsParameters(nil,msg[1],msg[2],false);
		},"/spatdif/scliss/ambisonics-parameters",nil); //apply to all

		/* TODO */OSCdef(\vbapParameters,{ |msg| this.setBinauralDecoder(msg[1],msg[2],false);
		},"/spatdif/scliss/binaural-parameters",nil);


		// VALID IF THERE WAS NO spatdifcmd MESSAGE!!

		// first time an entity is sended, we must create its responders
		// but also run the message!!
		thisProcess.addOSCRecvFunc({|msg|
			if (msg[0].asString != "/status.reply") {
				var strings = msg[0].asString.split;
				if (strings[1]=="spatdif" and:{strings[2]=="source"}) {
					var name = strings[3].asSymbol;
					if (receiverFunctions.at(name).isNil) {
						//create responders
						this.addReceiverFunction(name);
						thisProcess.addOSCRecvFunc(receiverFunctions.at(name));
					};
					if (encoders.at(name).isNil) {
						// ("encorders at "++name++" still not exist").postln;
						// resend message
						NetAddr.localAddr.sendBundle(0,msg);
					}
				}
			}
		});



		// // // // SETUP JACK CONNECTIONS // // // //

		// HOA 3rd order requires 16 output channels, we give also 8 extra
		server.options.numOutputBusChannels_(numAudioOutputs);
		// just in case, set the num output channels
		server.reboot;

		server.doWhenBooted({
			// // // // INIT SYNTHDEFS // // // //

			// binaural init: synth initialized from this method
			this.setBinauralDecoder(defaultBinauralDecoder,defaultSubjectID);

			this.initSynthDefs;
			this.initVbapSynthDef;

			this.setAudioOutput;
		});


		// // // // INIT OSC LOGGER // // // //
		this.initSpatDifLogger(mySpatDifLoggerPath);

		// // // // INIT OSC PLAYER // // // //

		spatDifPlayer = SpatDifPlayer.new(NetAddr.localAddr); //send here

		// // // // WORLD RESPONDER // // // //

		worldAddresses = List.new;
		worldAddresses.add(NetAddr.localAddr);


	}

	autosaveOSC_ { |bool|
		if (bool) {
			autosaveTask.resume;
		} {
			autosaveTask.pause;
		};
		autosaveOSC = bool;
	}

	logOSC_ { |bool|
		logOSC = bool;
		spatDifLogger.log_(bool);
	}


	addReceiverFunction { |name|
		var entityString = "/spatdif/source/"++name;

		var newFunc = {	|msg, time, replyAddr, recvPort|
			switch(msg[0].asString)

			///// external functions

			{entityString++"/media/type"} {this.mediaType(name,msg[1]) }
			{entityString++"/media/channel"} { this.mediaChannel(name,msg[1])}
			{entityString++"/position"} {this.setPosition(name,msg[1],msg[2],msg[3],msg[4])}
			{entityString++"/type"} {this.sourceType(name,msg[1])}
			{entityString++"/present"} {this.sourcePresent(name,msg[1])}
			{entityString++"/width"} {this.setSourceWidth(name,msg[1],msg[2])}
			{entityString++"/preserveArea"} {this.preserveArea(name,msg[1])}

			///// internal functions

			{entityString++"/ambisonics"} {this.setAmbisonicsParameters(name,msg[1],msg[2],false)}

		};

		receiverFunctions.add(name -> newFunc)
	}


	// // // // OSC FUNCTIONS // // // // // // // // // // // // // // // // // // // //


	newSource {
		^{ |msg, time, addr, recvPort|
			var name = msg[1];

			// create responders for this new source
			if (receiverFunctions.at(name).isNil) {
				this.addReceiverFunction(name);
				thisProcess.addOSCRecvFunc(receiverFunctions.at(name));
			};

			// create entry for object positions
			if (objectPositions.at(name).isNil) {
				objectPositions.add(name -> [1,0,0]) // position message will follow..
			};

		}
	}


	mediaType { |name, type|
		if (type == \jack) {
			var newSourceGroup;
			// var newBus;
			var newDistanceBus, newPannerBus;

			var distanceGroup, absorptionSynth, attenuationSynth;
			/*		var distanceSynth;*/
			var pannerSynth;

			var channel = 0; //default



			//////////////////////////////////     SYNTH STRUCTURE   ///////////////////////////////////////
			//                                                                                            //
			//   for each new source, a new synth structure is created:                                   //
			//                                                                                            //
			//                                                                                            //
			//              |··········· distanceGroup ············|                                      //
			//              |                                      |                                      //
			// channel      |             distanceBus              |  pannerBus                           //
			//    |         V                  |                   V     |                                //
			// [SC:in] ---> [absorptionSynth] ---> [attenuationSynth] -----> [pannerSynth] ---> [SC:out]  //
			//              ^                                                            ^                //
			//              |                        (encoders)                          |                //
			//              |······················· sourceGroup ························|                //
			//                                                                                            //
			// each element is stored in a dictionary, taking as a key the object's name                  //
			// e.g. source \new: encoders[\new], distanceBuses[\new], pannerSynths[\new] ...              //
			//                                                                                            //
			////////////////////////////////////////////////////////////////////////////////////////////////



			// avoid second mediaType resend when there is still no encoders
			if (sourceNames.indexOf(name).isNil) {

				if (verbose) {
					"NEW SOURCE---".postln;
					("name: "++name.asString).postln;
					// ("channel: "++channel.asString).postln;
				};

				// 0. add name to sourceNames list
				sourceNames.add(name);
				channels.add(name -> channel);


				// 1. create sourceSroup
				newSourceGroup = Group.new;
				encoders.add(name -> newSourceGroup);

				// 2. create internal buses
				newDistanceBus = Bus.audio(server,1);
				distanceBuses.add(name -> newDistanceBus);

				newPannerBus = Bus.audio(server,1);
				pannerBuses.add(name -> newPannerBus);

				// 3. create distanceGroup
				distanceGroup = Group.new(target:newSourceGroup,addAction:\addToHead);
				distanceGroups.add(name -> distanceGroup);

				absorptionSynth = Synth((\airAbsorption ++ defaultAbsorptionModel).asSymbol,[\externalIn,channel,\r,1,\busOut,newDistanceBus],target:distanceGroup,addAction:\addToHead);
				absorptionSynths.add(name -> absorptionSynth);

				attenuationSynth = Synth((\distanceAttenuation ++ defaultAttenuationModel).asSymbol,[\externalIn,newDistanceBus,\r,1,\busOut,newPannerBus],target:distanceGroup,addAction:\addToTail);
				attenuationSynths.add(name -> attenuationSynth);


				// *. initialize default ambisonics parameters
				// TODO: move this to a more elegant place??
				ambisonicsParameters.add(name -> [defaultAmbisonicsOrder,defaultAmbisonicsShape]);

				// 4, create encoding synth, add to tail group
				switch(spatialTechnique)
				{\ambisonics} {
					var synthName = this.getAmbisonicsSynthName(name);
					pannerSynth = Synth(synthName,[\busIn,newPannerBus],target:newSourceGroup,addAction:\addToTail);
				}

				{\vbap} {
					var synthName = \vbapEncoder;
					pannerSynth = Synth(synthName,[\busIn,newPannerBus,\numChannels,vbapNumSpeakers,\speakerBuffer,vbapSpeakerBuffer],target:newSourceGroup,addAction:\addToTail)
				}

				{\binaural} {
					var synthName = \binauralEncoder;
					pannerSynth = Synth(synthName,[\busIn,newPannerBus],target:newSourceGroup,addAction:\addToTail);
				}
				;

				pannerSynths.add(name -> pannerSynth);


				//TODO: check if source already exists

				// world replicate
				this.sendToWorld(\mediaType,name,type);

			}

		}
	}

	////// distance-cues extension properties.
	//
	// all methods behave in the same way (in order to allow data log and avoid infinite loopback):
	// 1. user call them from outside (with the default flag internal=true)
	// 2. the message goes to the net and is automatically picked up for the OSCdef above
	// 3. the function inside OSCdef recall this function, but now with the flag internal=false
	// 4. the things are finally done

	setReferenceDistance { |newValue,internal=true|
		var referenceDistance;

		if (internal) {
			if (newValue == \default) {
				referenceDistance = 1;
			} {
				referenceDistance = newValue;
			};
			// self-message to the oscLogger
			NetAddr.localAddr.sendMsg("/spatdif/distance-cues/reference-distance",referenceDistance);
		} {
			referenceDistance = newValue;

			sourceNames.do { |source|
				attenuationSynths.at(source).set(\refDistance,referenceDistance);
			};
			if (verbose) {
				"*********".postln;
				("reference distance: "++referenceDistance).postln;
			};
		}
	}

	setMaximumDistance { |newValue,internal=true|
		var maximumDistance;

		if (internal) {
			if (newValue == \default) {
				maximumDistance = 62500;
			} {
				maximumDistance = newValue;
			};
			// self-message to the oscLogger
			NetAddr.localAddr.sendMsg("/spatdif/distance-cues/maximum-distance",maximumDistance);
		} {
			maximumDistance = newValue;

			sourceNames.do { |source|
				attenuationSynths.at(source).set(\maxDistance,maximumDistance);
			};
			if (verbose) {
				"*********".postln;
				("maximum distance: "++maximumDistance).postln;
			};
		}

	}

	setMaximumAttenuation { |newValue, unit=\linear, internal=true|
		var maximumAttenuation;

		if (unit==\dB) {
			newValue = newValue.dbamp;
		};

		if (internal) {
			if (newValue == \default) {
				maximumAttenuation = 0.000016;
			} {
				maximumAttenuation = newValue;
			};
			// self-message to the oscLogger
			NetAddr.localAddr.sendMsg("/spatdif/distance-cues/maximum-attenuation",maximumAttenuation,\linear);
		} {
			maximumAttenuation = newValue;
			if (unit==\dB) {
				maximumAttenuation = maximumAttenuation.dbamp;
			};

			sourceNames.do { |source|
				attenuationSynths.at(source).set(\maxAttenuation,maximumAttenuation);
			};
			if (verbose) {
				"*********".postln;
				("maximum attenuation: "++maximumAttenuation).postln;
			};

		}
	}

	setAttenuationModel { |newValue,internal=true|
		var model;

		if (internal) {
			if (newValue == \default) {
				model = 2;
			} {
				model = newValue;
			};
			// self-message to the oscLogger
			NetAddr.localAddr.sendMsg("/spatdif/distance-cues/attenuation-model",model);
		} {
			model = newValue;

			if (attenuationModel != model) {
				attenuationModel = model;

				sourceNames.do { |source|
					attenuationSynths.at(source).free; // free the synth

					attenuationSynths.put(source,Synth((\distanceAttenuation ++ model).asSymbol,[\externalIn,distanceBuses[source],\r,objectPositions[source][0],\busOut,pannerBuses[source]],target:distanceGroups[source],addAction:\addToTail));

				};

				if (verbose) {
					"*********".postln;
					("attenuation model: "++ model).postln;
				};
			};
		}

	}


	setAbsorptionModel { |newValue,internal=true|
		var model;

		if (internal) {
			if (newValue == \default) {
				model = 1;
			} {
				model = newValue;
			};

			// self-message to the oscLogger
			NetAddr.localAddr.sendMsg("/spatdif/distance-cues/absorption-model",model);
		} {
			model = newValue;

			if (absorptionModel != model) {
				absorptionModel = model;

				sourceNames.do { |source|
					absorptionSynths.at(source).free; // free the synth

					absorptionSynths.put(source,Synth((\airAbsorption ++ model).asSymbol,[\externalIn,channels[source],\r,objectPositions[source][0],\busOut,distanceBuses[source]],target:distanceGroups[source],addAction:\addToHead));

				};

				if (verbose) {
					"*********".postln;
					("absorption model: "++ model).postln;
				};
			};
		}
	}

	mediaChannel { |name, channel|

		if (absorptionSynths.at(name).isNil.not) {
			absorptionSynths.at(name).set(\externalIn,channel);

			channels.put(name,channel);

			if (verbose) {
				"*********".postln;
				("name: "++name).postln;
				("channel: "++channel.asString).postln;
			};

			// world replicate
			this.sendToWorld(\mediaChannel,name,channel);
		}
	}


	setPosition { |name,azimuth=0,elevation=0,r=0,convention=\aed|


		if (encoders.at(name).isNil.not) {

			// just-in-case casting
			azimuth = azimuth.asFloat;
			elevation = elevation.asFloat;
			r = r.asFloat;

			if (verbose) {
				"*********".postln;
				("name: "++name).postln;
				("r: "++r).postln;
				("azimuth: "++azimuth).postln;
				("elevation: "++elevation).postln;
			};

			switch(spatialTechnique)
			{\ambisonics} {
				encoders.at(name).set(\r,r);
				encoders.at(name).set(\azi,azimuth.degree2rad);
				encoders.at(name).set(\ele,elevation.degree2rad);
			}
			{\binaural} {
				encoders.at(name).set(\r,r);
				encoders.at(name).set(\azi,((azimuth.degree2rad.neg)));
				encoders.at(name).set(\ele,elevation.degree2rad);
			}
			{\vbap} {
				encoders.at(name).set(\r,r);
				encoders.at(name).set(\azi,azimuth);
				encoders.at(name).set(\ele,elevation);
			};

			// save internal parameters
			objectPositions.put(name,[r,azimuth,elevation]);


			// world replicate
			this.sendToWorld(\setPosition,name,azimuth,elevation,r,convention);
		}
	}


	sourcePresent { |name,present=true|

		if (encoders.at(name).isNil.not) {


			if (present.class == \Symbol) {
				present = booleanValue(present.asInt)
			};
			// play/stop synth

			absorptionSynths.at(name).set(\present,present.asInt);

			if (verbose) {
				"*********".postln;
				("name: "++name).postln;
				("present: "++present).postln;

			};

			// world replicate
			this.sendToWorld(\sourcePresent,name,present);
		}
	}


	setSourceWidth { |name, da=1, de=1|

		if (encoders.at(name).isNil.not) {


			if (verbose) {
				"*********".postln;
				("name: "++name).postln;
				("da: "++da).postln;
				("de: "++de).postln;
			};

			// send parameters to all nodes inside their group
			encoders.at(name).set(\dAzi,da.degree2rad);
			encoders.at(name).set(\dEle,de.degree2rad);

			// world replicate
			this.sendToWorld(\setSourceWidth,name,da,de);
		}
	}


	preserveArea { |name,preserveArea=true|

		if (encoders.at(name).isNil.not) {


			if (verbose) {
				"*********".postln;
				("name: "++name).postln;
				("preserve area: "++preserveArea).postln;
			};

			// send parameters to all nodes inside their group
			encoders.at(name).set(\preserveArea,preserveArea);

			// world replicate
			this.sendToWorld(\preserveArea,name,preserveArea);
		}
	}

	setSpatializationTechnique { |newSpatialTechnique,internal=true|
		var lastSpatialTechnique;

		if (internal) {
			// self-message to the oscLogger
			NetAddr.localAddr.sendMsg("/spatdif/scliss/spatialization-technique",newSpatialTechnique);
		} {
			lastSpatialTechnique = spatialTechnique;

			spatialTechnique = newSpatialTechnique;

			if (lastSpatialTechnique != newSpatialTechnique) {

				sourceNames.do { |name|
					var pannerSynth;
					var synthName;

					// free old synths
					pannerSynths[name].free;

					//instanciate new
					pannerSynth = switch(newSpatialTechnique)
					{\ambisonics} {
						synthName = this.getAmbisonicsSynthName(name);
						Synth(synthName,[\busIn,pannerBuses[name]],target:encoders[name],addAction:\addToTail);
					}

					{\vbap} {
						var synthName = \vbapEncoder;
						Synth(synthName,[\busIn,pannerBuses[name],\numChannels,vbapNumSpeakers,\speakerBuffer,vbapSpeakerBuffer],target:encoders[name],addAction:\addToTail)
					}

					{\binaural} {
						var synthName = \binauralEncoder;
						Synth(synthName,[\busIn,pannerBuses[name]],target:encoders[name],addAction:\addToTail);
					}
					;

					pannerSynths.put(name,pannerSynth);

				};

				// change jack connexions
				this.setAudioOutput(lastSpatialTechnique);

				if (verbose) {
					"*********".postln;
					("spatialization technique: "++spatialTechnique).postln;
				};

			}
		}
	}


	getAmbisonicsSynthName { |source|
		var order = ambisonicsParameters.at(source).at(0);
		var shape = ambisonicsParameters.at(source).at(1);

		var name = \ambiEncoder;
		var extName;

		extName = switch (shape)
		{\ring} {\Ring}
		{\extended} {\Ext}
		{\meridian} {\Mer}
		{\point} {""};

		^(name ++ order ++ extName).asSymbol;
	}


	sourceType { |name, type|
		var order = ambisonicsParameters.at(name)[0];
		this.setAmbisonicsParameters(name,order,type,internal:false);
		// resend; if it comes from SSWorld, it will be duplicated
		this.sendToWorld(\sourceTupe,name,type);
	}


	// TODO: maybe check if parameters are not changed?

	setAmbisonicsParameters { |source, order = 3, shape = \point, internal=true|

		// only apply if current technique is ambisonics
		if (spatialTechnique == \ambisonics) {

			if (internal) {
				if (source.isNil) {
					// apply to all sources
					NetAddr.localAddr.sendMsg("/spatdif/scliss/ambisonics-parameters",order,shape);

				} {
					// apply to one source
					NetAddr.localAddr.sendMsg("/spatdif/source" +/+ source +/+ "ambisonics",order,shape);
				}
			} {
				// apply to all sources
				if (source.isNil) {
					sourceNames.do { |key|
						var pannerSynth;
						var synthName;

						// set new ambisonics parameters
						ambisonicsParameters.put(key,[order,shape]);
						/*					ambisonicsParameters.at(key).put(0,order);
						ambisonicsParameters.at(key).put(1,shape);*/
						synthName = this.getAmbisonicsSynthName(key);

						// free current synth
						pannerSynths.at(key).free;

						// create new synth
						pannerSynths.put(key,Synth(synthName,[\busIn,pannerBuses.at(key)],target:encoders.at(key),addAction:\addToTail));

						if (verbose) {
							"*********".postln;
							("name: "++key).postln;
							("ambisonics order: "++order).postln;
							("source shape:"++shape).postln;
						};

					}
				} {
					// apply to a specific source
					var pannerSynth;
					var synthName;

					// set new ambisonics parameters
					ambisonicsParameters.put(source,[order,shape]);
					/*				ambisonicsParameters.at(source).put(0,order);
					ambisonicsParameters.at(source).put(1,shape);*/
					synthName = this.getAmbisonicsSynthName(source);

					// free current synth
					pannerSynths.at(source).free;

					// create new synth
					pannerSynths.put(source,Synth(synthName,[\busIn,pannerBuses.at(source)],target:encoders.at(source),addAction:\addToTail));

					if (verbose) {
						"*********".postln;
						("name: "++source).postln;
						("ambisonics order: "++order).postln;
						("source shape:"++shape).postln;
					};

				}

			}

		} {
			"Ambisonics is not the current spatialization technique".warn;
		}
	}


	setBinauralDecoder { |decoderType, subjectID,internal=true|

		// only apply if current spatial technique is binaural
		if (spatialTechnique == \binaural) {

			if (internal) {
				// self-message to the oscLogger
				NetAddr.localAddr.sendMsg("/spatdif/scliss/binaural-parameters",decoderType,subjectID);
			} {

				// load new data
				binauralSubjectID = subjectID;
				binauralDecoder  = switch(decoderType)
				{\spherical} {FoaDecoderKernel.newSpherical(binauralSubjectID,server)}
				{\listen} {FoaDecoderKernel.newListen(binauralSubjectID,server)}
				{\cipic} {FoaDecoderKernel.newCIPIC(binauralSubjectID,server)}
				;

				// update synthdef
				this.initBinauralSynthDef;

				// reload synths
				sourceNames.do { |key|
					// free current synth
					pannerSynths.at(key).free;

					// create new synth
					pannerSynths.put(key,Synth(\binauralEncoder,[\busIn,pannerBuses.at(key),\subjectID,binauralSubjectID],target:encoders.at(key),addAction:\addToTail));

				};

				if (verbose) {
					"*********".postln;
					("binaural decoder: "++binauralDecoder).postln;
					("binaural subject ID:"++binauralSubjectID).postln;
				};

			}
		} {
			"Binaural is not the current spatialization technique".warn;
		}
	}

	// this is not logged since it is supossed that the speaker configuration will remain the same, and it is nor a compositional parameter ()

	setVbapParameters { |speakerArray|

		vbapSpeakerArray = speakerArray;
		vbapSpeakerBuffer = vbapSpeakerArray.loadToBuffer;
		vbapNumSpeakers = vbapSpeakerArray.numSpeakers;

		if (verbose) {
			"*********".postln;
			("vbap speaker array: "++vbapSpeakerArray).postln;
		};

		// reload synthdef with new vbap parameters
		this.initVbapSynthDef;

		// reload synths
		sourceNames.do { |key|
			// free current synth
			pannerSynths.at(key).free;

			// create new synth
			pannerSynths.put(key,Synth(\vbapEncoder,[\busIn,pannerBuses.at(key),\numChannels,vbapNumSpeakers,\speakerBuffer,vbapSpeakerBuffer],target:encoders.at(key),addAction:\addToTail));

		};

	}




	endSource {
		^{ |msg, time, addr, recvPort|
			var name=msg[1];

			if (verbose) {
				"END SOURCE---".postln;
				("name: "++name.asString).postln;
			};

			//TODO!!! avoid click

			if (sourceNames.at(name).isNil.not) {

				// free all nodes inside the group
				encoders.at(name).free; //group.freeAll;
				//then remove the association
				encoders.removeAt(name);

				// remove from  object positions list
				objectPositions.removeAt(name);
			} {
				("source " ++ name ++ " does not exist").warn;
			};
		}
	}

	close {
		//remove OSC receivers
		receiverFunctions.do{|f| thisProcess.removeOSCRecvFunc(f)};
		OSCdef.freeAll;
		//free all synths (take care with default_group)
		server.defaultGroup.freeAll;
		// stop autosave task
		autosaveTask.stop;
		// close file
		spatDifLogger.save;
		spatDifLogger.close;
	}

	//// SpatDIF player

	spatDifPlayer_load { |aFile|
		spatDifPlayer.loadFile(aFile);
		if (verbose) {
			"*********".postln;
			("File " ++ PathName(aFile).fileName ++ " loaded").postln;
		}
	}

	spatDifPlayer_play {
		spatDifPlayer.play;
		// stop logging
		spatDifLogger.log_(false);
		if (verbose) {
			"*********".postln;
			("SpatDIF player: play").postln;
		}
	}

	spatDifPlayer_pause {
		spatDifPlayer.pause;
		// continue logging
		spatDifLogger.log_(true);
		if (verbose) {
			"*********".postln;
			("SpatDIF player: pause").postln;
		}
	}

	spatDifPlayer_start { |loop=1|
		spatDifPlayer.start(loop);
		// stop logging
		spatDifLogger.log_(false);
		if (verbose) {
			"*********".postln;
			("SpatDIF player: start").postln;
		}
	}

	spatDifPlayer_stop {
		spatDifPlayer.pause;
		spatDifPlayer.reset;
		// continue logging
		spatDifLogger.log_(true);
		if (verbose) {
			"*********".postln;
			("SpatDIF player: stop").postln;
		}
	}

	spatDifPlayer_reset {
		spatDifPlayer.reset;
		if (verbose) {
			"*********".postln;
			("SpatDIF player: reset").postln;
		}
	}

	spatDifPlayer_isPlaying {
		var ans = spatDifPlayer.isPlaying;

		// don't bother all the time...
		/*		if (verbose) {
		"*********".postln;
		("SpatDIF player is playing: " ++ ans).postln;
		};*/
		^ans;
	}
	spatDifPlayer_verbose { |bool|
		^spatDifPlayer.verbose_(bool);
	}


	////////////// RESEND TO WORLD //////////////

	sendToWorld { |cmd, sourceName ...args|
		var msg;

		// in localhost, only resend if messages come from SpatDifPlayer (not from the world)
		if (this.spatDifPlayer_isPlaying or:{resendToWorld}) {

			msg = switch (cmd)
			{\mediaType} {[cmd,sourceName,args[0]]}
			{\mediaChannel} {[cmd,sourceName,args[0]]}
			{\setPosition} {[cmd,sourceName,args[0],args[1],args[2],args[3]]}
			{\sourcePresent} {[cmd,sourceName,args[0]]}
			{\setSourceWidth} {[cmd,sourceName,args[0],args[1]]}
			{\preserveArea} {[cmd,sourceName,args[0]]}
			{\sourceType} {[cmd,sourceName,args[0]]}
			;

			msg = ['/ssworld',msg].flat;

			worldAddresses.do { |addr|
				addr.sendBundle(0,msg)
			}
		}
	}

	addWorldAddress { |addr|
		^worldAddresses.add(addr);
	}

	removeWorldAddress { |addr|
		^worldAddresses.remove(addr);
	}

	spatDifLoggerFilename {
		^spatDifLogger.filename;
	}

}
