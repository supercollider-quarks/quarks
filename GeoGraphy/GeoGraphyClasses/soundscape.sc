// Soundscape is an abstract class to be subclassed
// In order to have several Soundscape models that implement a SynthDef. The audio synthesis module
// You could have several Soundscape instances, and each of them is a Zone of your Soundscape.
// A Zone allow you to manage set of vertex (sample), as they were an unique channel/layer. 


Soundscape {

// CORE
////////////////////////////////////////////////////////////////////a model to generate interactives soundscapes
	var <>server, <> runner, <>geoListener, <>ns, <>sampleplaying;
	
	// needs a geoListener
	*new { arg runner, geoListener, aServer ; 
		^super.new.initSoundscape(runner, geoListener, aServer) ; 
	}

	initSoundscape { arg aRunner, aGeoListener, theServer ;
		runner = aRunner;
		runner.addDependant(this);
		geoListener = aGeoListener ;
		geoListener.addDependant(this) ;
		
		//the user could ask to work with internal server
		if (theServer == nil, {
			server = Server.local ;
			},{server = theServer}
		);
		server.boot;
		server.doWhenBooted({
			this.sendDef ;
			});
		sampleplaying = IdentityDictionary.new; //to trace active synth and sample property
		ns = 0;
	}

//more here is : //[\actant, vID,[x, y, vDur, vLabel, [offsetVertexListenedArea]],
// eID, [end, eDur, eID, eOpts], aID, weight, offsetWeight, count]
	update { arg theChanged, theChanger, more;
		if (more[0] == \actant,

			 { 	
				this.play( more[1..] );
				}	
				) ; 
	
//more here is :[\tobefilteredbyposition,la,lb,lorient]		
		if (more[0] == \tobefilteredbyposition && (ns > 0), 
			{ this.modifplay(more)}	
				) ;			
	}

//////////////////////////////////////////////////////////////////

// dummy methods to be overwritten
	initAudio { "init done".postln }


	sendDef { var aDef ; aDef.send(server) ;
	}


	play { arg more ;
		more.postln ;
	}
	
	modifplay{arg more;
		more.postln;
	}
}

// NOTE: -> .mp3 Format is not supported

CartoonModel : Soundscape  {
	var <>bufDict, <>samplesPath ;
	var <>offsetVertexStandardListenedArea;
	var <>gain;
	var <>m;	// ratio VirtualUnit/meter. 
	var aDef;
	var c; //condition to sync buffer loading process
	var <>name; //you could need it, if you create a soundscape with several zones.

	initAudio { arg aSamplesPath, nameList, ext = "wav" ;

			offsetVertexStandardListenedArea = 60; //in meter
			gain = 1;
			m = 1;
			
			samplesPath = aSamplesPath ;
			bufDict = IdentityDictionary.new ;
			c = Condition.new;
			Routine.run {
				server.doWhenBooted({
				
						if (nameList == nil, {
						
							this.loadDatabase(samplesPath);},
							
							{ //else
							//old implementation, provide a path and a list of file name
							nameList.do({ arg name ;
							bufDict.add(name -> Buffer.read(server, samplesPath++name++"."++ext));
							//[name, \loaded].postln;
							});
							server.sync;
							c.test = true; 
							c.signal;
						});
					});
						c.wait;
						"folder loaded".postln;
				};
			
			}
	
	sendDef {				
			 aDef = SynthDef(\CartoonModel, { arg bufnum, amp, out = 0, dur, pan = 0, cutFrequency=22000; //SC Bug: if cutfreq>22000 LPF do not correctly work 
							Out.ar(out, LPF.ar( 
									Pan2.ar(			
										amp * 
										EnvGen.kr(Env.new([0,1,1,0], [dur*0.1, dur*0.8, dur*0.1]), doneAction:2) 
											//the samples have fad in and fade out depending on their dur
											//doneaction will free the synth at the end of control line.kr = dur 
										*
										PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum), loop: 0)
										,pan)
								,cutFrequency)	
								)
							}
					) ;
					aDef.send(server) ;
	}
	
	
	setGain { arg value;
		gain = value;
	}


	readGain {
		^gain;
	}
	
	
	setVirtualUnitMeterRatio { arg ratio;
		m = ratio;
	}
	
	
	loadDatabase { arg aPath; //in order to add samples also after the server.init
							//need identifier per sample to be passed in segmentDict
		
		var path = PathName.new(aPath);
		var files = path.entries;
		var sample = SoundFile.new;
		var uid; //unique id
		var exist;
		var name;
	
		files.do({arg aPath;
			if ( sample.openRead(aPath.fullPath) == true, { //if true SoundFile.openRead is been succesfull
			
				uid = aPath.fileNameWithoutExtension;
				
				exist = false;
				this.bufDict.do ({arg value; var name; //just to call the findKeyForValue
				name = this.bufDict.findKeyForValue(value);
				});
				
				//("name"+name+"uid"+uid).postln;
				
				if (uid == name, {exist = true;});
				
				if (exist == true, {("You already provide a sound with the unique id"+name).postln;},
					{ 
					this.bufDict.add(uid.asSymbol -> Buffer.read(server, aPath.fullPath));
				});
				
			},{("The file:"+aPath.fullPath+"is not a SoundFile").postln;}
			); 
		});
		server.sync;
		c.test = true; 
		c.signal;	

	}
	
	
	//manage graph dict on graph.sc
	
	//set the listened area annotation in graph vertex options vector 
	setListenedArea { arg vID, val, aGraph ;
			aGraph.setvopts(vID, 0, val); //setvopts args are: vID, annotationOrder, val
		}
	
	// set automatic normalisation parameter to correct the distance of recordings if different from 5m
	setDy { arg vID, val, aGraph ;
			aGraph.setvopts(vID, 1, val); //setvopts args are: vID, annotationOrder, val
		}
	
	
	filter { arg aXv, aYv, aA, aB, aOffsetVertexListenedArea, aDy ;
		var xv = aXv;
		var yv = aYv;
		var a = aA;
		var b = aB;
		var amp = 0; 
		var cutFrequency = 22000;
		var offsetListenedArea = aOffsetVertexListenedArea; //also rol off (see OPENGL standard)
		var perceptionArea = geoListener.perceptionArea;
		
		var d = sqrt(squared(a-xv)+squared(b-yv));
		var dy = aDy ; //aDy is the distance of recording if different from 5m
		
		//dy = normalisation, adjust the sample amp if the recording distance if different from 5 meter. need to be passed by method call because dy is a vertex property
		
		if (offsetListenedArea == nil, {offsetListenedArea = offsetVertexStandardListenedArea});
		if (dy == nil, {dy = 1}, //dy = 1 means the vertex is already normalised or recorder at 5m. 			
			{ dy = this.fromRecDistanceToNormalisationAmp(dy); };
			
		);  
			
		//attenutaion formula for point sources
		
		//point sources has a lowpassband filter to simulate the perception of distance
		if(d > (m*perceptionArea*2),
				{
				cutFrequency = exp(10 - ( 7 *((d - (m*perceptionArea*2))/(m*offsetListenedArea + m*perceptionArea))));
				if(cutFrequency < 20, {cutFrequency = 20});			//the max filtered value with a LPB is 20hz
	
					} 
		);	
			
				
		//attenuation formula
		d = d*m;
		
		if (d < 1, {d = 1}); // d cannot be 0
		
		amp = ((dy)/d); 
			
		//"amp".post;
		//amp.postln;

		if(amp < 0, {amp = 0});
		^[amp,cutFrequency];
	}
	
	fromRecDistanceToNormalisationAmp { arg recordingDistance;
			var a2deg = [-0.00114526,  0.1509869 ,  0.17773213]; // coefficient of a 2 degree equation approximation. 
			var x, y;				
			
			x = recordingDistance;
			y = (a2deg[0]*x*x) + (a2deg[1]*x) + a2deg[2] ;	
			^y;
	}
	
	
	play { arg message ; //more here is : [vID,[x, y, vDur, vLabel, [offsetVertexListenedArea]],
						// eID, [end, eDur, eID, eOpts], aID, weight, offsetWeight, count]
			var label, weight, offsetWeight, offsetVertexListenedArea, dur, amp, ampatt;
			var al, bl;
			var result;
			var cutFrequency, pan, xv, yv, synthplaying, r, name;
			
			weight = message[5] ;
	 		offsetWeight = message[6] ;
			ampatt = 0; //(weight+offsetWeight).thresh(0) ;			//to calculate the amp send by attant not usefull in cartoon model
			
			
			label = message[1][3] ;
			xv = message[1][0]; //vertex position
			yv = message[1][1];	
			dur = message[1][2] ; 
			
			al = geoListener.la; //listener position
			bl = geoListener.lb;

			offsetVertexListenedArea = message[1][4][0];
	 			
	 		
	 		result = this.filter(xv, yv, al, bl, offsetVertexListenedArea);
	 		amp = result[0];
	 		cutFrequency = result[1];
	 		
	 		pan = geoListener.calculatePanning(xv, yv, al, bl, geoListener.lorient); 
	 		
	 		
	 		
			if (bufDict[label] != nil,
		  			{ 	dur = bufDict[label].numFrames/server.sampleRate ;
						synthplaying = Synth.new(\CartoonModel,		
		 				[\bufnum,bufDict[label].bufnum,\dur,dur,\amp,amp,\pan,pan,\cutFrequency,cutFrequency]);
						name = synthplaying.asString;
						sampleplaying = sampleplaying.add(name -> [synthplaying, xv, yv, offsetVertexListenedArea]);
						ns = ns + 1;
						r = Routine.new({ dur.wait; sampleplaying.removeAt(name); ns = ns-1}); 
						SystemClock.play(r); 
						//dur.postln;
						},{("bufDict["++label++"] is nil").postln})
						
			}

	modifplay {arg message ;	//[\tobefilteredbyposition,la,lb,lorient]
			var durvar, amp, cutFrequency, pan, xv, yv, offsetVertexListenedArea, a, b, orient ;
			var result;
			a = message[1];
			b = message[2];
			orient = message[3];

			if ( ns >0, 
			sampleplaying.do({	
					arg item;
					xv = item[1] ;
					yv = item[2] ;
					offsetVertexListenedArea = item[3] ;
					result = this.filter(xv,yv,a,b,offsetVertexListenedArea);
					amp = result[0];
					cutFrequency = result[1];
					pan = geoListener.calculatePanning(xv,yv,a,b,orient);
					//"[amp,cutFrequency,pan]".postln;
					//[amp,cutFrequency,pan].postln;
					item[0].set(\amp,amp,\pan,pan,\cutFrequency,cutFrequency);
					//synthplaying[item].set(\amp,amp,\pan,pan,\cutFrequency,cutFrequency);
													
					})
				,{"no synth playing".postln; }
			);
			}

}


// M. Schirosa Multimedia Engineering Master Thesis
// made @
// CIRMA, Turin
// revised @
// MTG, Barcelona
