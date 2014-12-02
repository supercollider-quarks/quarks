/*
	Filename: VirtualRoom.sc 
	created: 21.4.2005 

	Copyright (C) IEM 2005, Christopher Frauenberger [frauenberger@iem.at] 

	This program is free software; you can redistribute it and/or 
	modify it under the terms of the GNU General Public License 
	as published by the Free Software Foundation; either version 2 
	of the License, or (at your option) any later version. 

	This program is distributed in the hope that it will be useful, 
	but WITHOUT ANY WARRANTY; without even the implied warranty of 
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
	GNU General Public License for more details. 

	You should have received a copy of the GNU General Public License 
	along with this program; if not, write to the Free Software 
	Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA. 

	IEM - Institute of Electronic Music and Acoustics, Graz 
	Inffeldgasse 10/3, 8010 Graz, Austria 
	http://iem.at
*/


/* 	NOTE: 
	the coordinate system is given according to the listener's head:
	x-axis (nose), y-axis (left-ear) and z-axis (vertex)
	
	* left-deep-corner			  *
		       x				  |
		       |	       		  |
		      / \		  		  depth
		y<---| z |			  |
		     ----				  |
							  |
	*---------- width ------------* (origin) 

	Rooms are defined by the origin and width/depth/height
*/


/* 	Class: VirtualEnvironment
	Provides a virtual room with sources, listener and binaural rendering
*/
VirtualRoom {
	
	// the path to Kemar
	classvar <>kemarPath = "KemarHRTF/";
	
	// the node proxies
	var <>encoded, <>bin, <>out, <>revIn;

	// maximum of sources allowed (exclusive reflections) 
	classvar <>maxSources = 10;
	
	// roomProperties (reverberation, reflections)
	var <>roomProperties;
		
	// the room as [x, y, z, depth, width, height] where xyz is the origin
	var <>roomSize;
	
	// the listener as NodeProxy.kr [ x, y, z, orientation] 
	var <>listener;
	
	// the list of sources for each instance
	var <>sources;
		
	/*	Class method: *new
		create an instance and initialise the instance's list of sources
	*/
	*new {
		^super.new.sources_(IdentityDictionary.new)
			.roomProperties_(IdentityDictionary.new);
	}

	/*	Method: init
		init the binaural rendering engine
	*/
	init {

		// initialise the rendering
		BinAmbi3O.kemarPath = kemarPath;
		
		// Note: different schemes may be passed to the bin NodeProxy, see source / help
		BinAmbi3O.init('1_4_7_4', doneAction: {
		 	// initialise the rendering chain when buffers are ready
		 	revIn = NodeProxy.audio(numChannels: 2);
			encoded = NodeProxy.audio(numChannels: 16);
			bin = NodeProxy.audio(numChannels: 2);
			bin.prime({ BinAmbi3O.ar( encoded.ar ) }); // only prime them, dont start them yet.
			revIn.prime({ bin.ar }); 	// not ideal... 
									// - should be diffuse-field EQed, 
									// and decorrelated sum of 'encoded'. 
									// hmm. or maybe better: as is, but softened down (OnePole)
									
			out = NodeProxy.audio(numChannels: 2);
			out.prime({ arg room = 0.25, revGain = 0.1, hfDamping = 0.6;
					bin.ar + (FreeVerb2.ar( revIn.ar[0], revIn.ar[1], 
						mix: 1, room: room, damp: hfDamping) * revGain) 
			}); 
			listener = NodeProxy.control(numChannels: 4);
			listener.prime({ |x=0, y=0, z=0, o=0| [ x, y, z, o] });
			"Virtual Room initialised.".postln;
		});
	}

	// access methods	
	refGain_ { arg value;
		if(value.isNil) { roomProperties.removeAt(\refGain) } { roomProperties.put(\refGain, value) };
		sources.do({ |source| source.set(\refGain, value) });
	}
	refGain { ^roomProperties.at(\refGain).value ?? 0.6 }
	
	revGain_ { arg value;
		if(value.isNil) { roomProperties.removeAt(\revGain) } { roomProperties.put(\revGain, value) };
		out.set(\revGain, value);
	}
	revGain { ^roomProperties.at(\revGain).value ?? 0.1 }
	hfDamping_ { arg value;
		if(value.isNil) { roomProperties.removeAt(\hfDamping) } { roomProperties.put(\hfDamping, value) };
		out.set(\hfDamping, value);
	}
	hfDamping { ^roomProperties.at(\hfDamping).value ?? 0.6 }
	
	room_ { arg value;
		var diag;
		if(value.isNil || (value.size!=6)) { 
			"WARNING AmbIEM: using default room (0,0,0,5,8,5)".postln; 
			roomSize=[0, 0, 0, 5, 8, 5] } 
		{ roomSize = value };
		diag = hypot(roomSize[2]-roomSize[5], hypot(roomSize[0]-roomSize[3], roomSize[1]-roomSize[4]));
		out.set(\room, diag.linlin(0, 50, 0, 1)); // set the room size for FreeVerb
	}
	room { ^roomSize }
	

	/* 	Method: gui
		provide a GUI for the room properties
	*/
	gui {
		var w, f, s, v, t;
		var height = 15;
		s = Array.newClear(3);
		v = Array.newClear(3);
		roomProperties.put(\rTSpec, [0, 3].asSpec);
		roomProperties.put(\rMSpec, [0, 1].asSpec);			w = GUI.window.new("Virtual Room Properties", Rect(128, 64, 340, 100));
		w.view.decorator = f = FlowLayout(w.view.bounds,Point(4,4),Point(4,2));

		t = GUI.staticText.new(w, Rect(0, 0, 75, height+2));
		t.string = "RefGain: ";
		v[0] = GUI.staticText.new(w, Rect(0, 0, 30, height+2));
		s[0] = GUI.slider.new(w, Rect(0, 0, 182, height));
		s[0].value = this.refGain;
		s[0].action = { 
			this.refGain = s[0].value;
			v[0].string = s[0].value.round(0.01).asString;
		};
		f.nextLine;
		
		t = GUI.staticText.new(w, Rect(0, 0, 75, height+2));
		t.string = "RevGain: ";
		v[1] = GUI.staticText.new(w, Rect(0, 0, 30, height+2));
		s[1] = GUI.slider.new(w, Rect(0, 0, 182, height));
		s[1].value = this.revGain;
		s[1].action = { 
			this.revGain = s[1].value;
			v[1].string = s[1].value.round(0.01).asString;
		};
		f.nextLine;

		t = GUI.staticText.new(w, Rect(0, 0, 75, height+2));
		t.string = "hfDamping: ";
		v[2] = GUI.staticText.new(w, Rect(0, 0, 30, height+2));
		s[2] = GUI.slider.new(w, Rect(0, 0, 182, height));
		s[2].value = this.hfDamping;
		s[2].action = { 
			this.hfDamping = s[2].value;
			v[2].string = s[2].value.round(0.01).asString;
		};
		f.nextLine;
		s.do({|x|x.action.value });
		
		w.front;
	}
	
	/* 	method: play
		play the output node proxy
	*/
	play { out.play }

	/* 	method: stop
		stops the output node proxy
	*/	
	stop { out.stop }

	/* 	method: end
		ends the virtual room, removes all node proxies
		Parameter:
			fadeTime A time to fade out
	*/
	end { |fadeTime=0.1| 
		out.end(fadeTime);
		fork { 
			fadeTime.wait; 
			sources.do(_.end);
			bin.end;
			revIn.end;
			encoded.end;
			listener.end;
		};
	}

	/* 	method: addSource
		add a source to the virtual room 
		Parameter:
			source A mono sound source as NodeProxy.audio
			key A string to identify the source
			x, y, z the position of the source
	*/
	addSource { arg source, key, x, y, z;
		^this.prAddSource(false, source, key, x, y, z;) 
	}

	/* 	method: addSourceLight
		add a source to the virtual room, a lighter version
		Parameter:
			source A mono sound source as NodeProxy.audio
			key A string to identify the source
			x, y, z the position of the source
	*/
	addSourceLight { arg source, key, x, y, z;
		^this.prAddSource(true, source, key, x, y, z;) 
	}

	/* 	method: removeSource
		remove a source from the virtual room 
		Parameter:
			key A string to identify the source
	*/
	removeSource { |key| 
		this.prAddSource(false, nil, key); 
		{ sources.removeAt(key).postln.free; }.defer(0.1);
	}

	/* 	method: prAddSource
		private function that takes care of adding the source
		Parameter:
			light Boolean to denote if light version or not
			source A mono sound source as NodeProxy.audio
			key A string to identify the source
			x, y, z the position of the source
	*/
	prAddSource { |light=true, source, key, x, y, z| 
		var synthFunc, myProxy; 
		if (source.notNil) { 
			if (light) { 
				synthFunc = this.lightSourceFunc(source, x, y, z) 
			} { 
				synthFunc = this.fullSourceFunc(source, x, y, z) 
			};
		};
			// make the audio proxy if needed
		if (sources[key].isNil) { 
			sources.put(key, 
				NodeProxy.audio(numChannels: encoded.numChannels)
				.bus_(encoded.bus)	// now writes to encoded bus directly!
			);
		} { 
			// "VirtualRoom: reusing existing proxy.".postln;
		};
		
		sources[key].set(\refGain, this.refGain)
			.set(\xpos, x, \ypos, y, \zpos, z);
			 
		sources[key].source = synthFunc; 
	}

	/* Alberto:
		funcs could be optimised as SynthDefs, 
		with an arg for listenBusIndex, 
		room could be a KeyBus (synced bus and lang values)
		 ... later ... 
	*/
		
	/* 	method: lightSourceFunc
		create the encoded source function
		Parameter:
			source A mono sound source as NodeProxy.audio
	*/
	lightSourceFunc { arg source;
	
		^{ arg refGain = 0, xpos, ypos, zpos;
			var sourcePositions;
			var distances, gains, phis, thetas, delTimes, gainSource;
			var lx, ly, lz, lo;
			
			#lx, ly, lz, lo = listener.kr(4);

			// direct source + 4 reflections		
			sourcePositions = [
				xpos, ypos, zpos, 
				this.room[0] + (2 * this.room[3]) - xpos, ypos, zpos, 
				this.room[0] - xpos, ypos, zpos, 
				xpos, this.room[1] + (2 * this.room[4]) - ypos, zpos, 
				xpos, this.room[1] -ypos, zpos
			];
			
			#phis, thetas, distances = sourcePositions.clump(3).collect({ |pos|
				var planeDist;
				planeDist = hypot(pos[1]-ly, pos[0]-lx);
				[atan2(ly-pos[1], pos[0]-lx) + lo, atan2(pos[2]-lz, planeDist), hypot(planeDist, lz - pos[2])];
			}).flop;		

			delTimes = ( distances / 340 );

			//phis.first.poll(1, "phi0");
			//thetas.first.poll(1, "theta0");
			//distances.first.poll(1, "distance0");
			
			gains = (distances + 1).reciprocal.squared; 

			(1..4).do({ | i | gains[i] = gains[i] * refGain });
		
			// sum up the encoded channels of all sources (original + reflections) 
			// DelayL replacement with BufRead....
			DelayL.ar( source.ar, 2, delTimes, gains).collect( { |ch, i| 
				PanAmbi3O.ar(ch, phis[i], thetas[i]); }).sum;
		}
	}

	/* 	method: fullSourceFunc
		create the encoded source function
		Parameter:
			source A mono sound source as NodeProxy.audio
	*/	
	fullSourceFunc { arg source;
	
		^{ arg refGain = 0, xpos, ypos, zpos;
			var sourcePositions;
			var distances, gains, phis, thetas, delTimes, gainSource;
			var phi, theta, planeDist, roomDist, refGain2; 
			var roomModel, sourceAndRefs; 
			var lx, ly, lz, lo;
			refGain2 =  refGain.squared;

			#lx, ly, lz, lo = listener.kr(4);
										
			// calculate angles for source
			planeDist = hypot(ypos-ly, xpos-lx);
			phi = atan2(ly-ypos, xpos-lx) + lo;
			theta = atan2(zpos-lz, planeDist);
			roomDist = hypot(planeDist, lz - zpos);
			// calculate the room model
			roomModel = Room3D.new;
			roomModel.room = this.room;
			sourceAndRefs = [phi, theta, roomDist] ++ 
				roomModel.refs10polar(xpos, ypos, zpos, lx, ly, lz);
		
			#phis, thetas, distances = sourceAndRefs.clump(3).flop;
			
			delTimes = ( distances / 340 );

			//distances.poll(1, "dists");
			gains = (distances + 1).reciprocal.squared; //.poll(1, "refGain"); 
			
			(1..10).do({ | i | gains[i] = gains[i] * if (i.inclusivelyBetween(5,8), refGain, refGain2) });
		
			// sum up the encoded channels of all sources (original + reflections) 
			// DelayL replacement with BufRead....
			DelayL.ar( source.ar, 2, delTimes, gains).collect( { |ch, i| 
				PanAmbi3O.ar(ch, phis[i], thetas[i]); }).sum;
		}
	}
}