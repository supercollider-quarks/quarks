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
// SSWorld.sc
// Based on RedUniverse quark (by redFrik)
//
// This class implements a Sound Scene World, which is a special case of RedWorld type 1 (without walls)
//
// Extended features:
// -> auto drawing function with nice cenital perspective and object names and shapes display
// -> dictionary for storing objects by name
// -> internal timing functions for the simulation
// -> OSC messages with adjustable JND variations for sending to the spatial render
//
//
////////////////////////////////////////////////////////////////////////////

SSWorld : RedWorld1 { //default with walls

	var <>friction;

	var time, <>stepFreq;

	var window;

	var <>rDiff, <>aziDiff, <>eleDiff;

	var <>viewDiff;

	var <renderAddresses;

	var <numObjects=0;

	var objectsID;

	var worldView;

	var <sweetSpotSize=2;

	var <maxDistance; //maximum distance on the room from the center

	var <>receivingFromServer = false;

	*new {|dim, gravity, maxVel, damping, friction=0.01, timeStep=60, netAddr|

		^super.newCopyArgs(
			dim !? {if (dim.isArray) {Cartesian.fromArray(dim)} {dim} } ?? {Cartesian(10,10,5)},
			gravity !? {if (gravity.isArray) {Cartesian.fromArray(gravity)} {gravity} } ?? {Cartesian(0,0,0.98)},
			maxVel ? 100,
			damping ? 0.25
		).initSSWorld(friction,timeStep,netAddr);
	}

	initSSWorld{ |myFriction,mystepFreq,myNetAddr|
		friction=myFriction;
		stepFreq=mystepFreq;

		renderAddresses = List.new;
		renderAddresses.add(myNetAddr ? NetAddr.localAddr);

		//defaults
		rDiff=0.05; ////////// <---- check bibliography!!
		aziDiff=1.degree2rad;
		eleDiff=5.degree2rad; ////////// <---- check bibliography!!
		viewDiff=false;



		maxDistance=this.getMaxDistance;


		//view
		window= SSWindow("SS-WORLD",bounds:Rect(100,100,500,500),dimVector:dim);
		window.background_(Color.white);
		window.alwaysOnTop_(true);
		// window= RedQWindow("SS-WORLD", Rect(0,0,dim[0],dim[1])).background_(Color.white);
		worldView=SSWorldView.new(this);
		window.draw(worldView.draw);


		// task managing objects update and time passing
		time=Task({
			inf.do{
				// avoid sending position information in playback
				if (receivingFromServer.not) {
					this.update;
				};
				stepFreq.reciprocal.wait;
			}
		}).start;

		// from initRedWorld
		// objects=[];
		objects=Dictionary.new;

		objectsID=Dictionary.new;


		// objects= Dictionary.new; // inRedWorld
		RedUniverse.add(this);	//add world to universe
		this.prInitSurroundings;


		//////////////////// RECEIVERS FROM SPATIALRENDER ///////////
		OSCdef(\fromSpatialRender,{ |msg|
			var cmd = msg[1];
			var sourceName = msg[2];
			var args = msg[3..];

			if (this.getObject(sourceName).isNil) { //create object
				this.add(SSObject.new(this,name:sourceName,registerInWorld:false),internal:false);
			};


			switch (cmd)
			{\mediaType}  {}
			{\mediaChannel} {
				this.getObject(sourceName).setChannel(msg[3],internal:false)
			}
			{\setPosition} {
				var azi = args[0].degree2rad;
				var ele = args[1].degree2rad;
				var r = args[2];
				this.getObject(sourceName).locSph_(Spherical(r,azi,ele));
			}
			{\sourcePresent} {
				this.getObject(sourceName).present_(msg[3].asFloat.asBoolean,internal:false)
			}
			{\setSourceWidth} {
				this.getObject(sourceName).dAzimuth_(args[0],internal:false);
				this.getObject(sourceName).dElevation_(args[1],internal:false);
			}
			{\preserveArea} {
				this.getObject(sourceName).preserveArea_(msg[3],internal:false)
			};

			receivingFromServer = true;
			this.updateView;


		},"/ssworld",nil);

	}

	getMaxDistance {
		var extreme=dim*[0.5,0.5,1];
		^extreme.rho;
	}

	setSweetSpotSize { |newSize|
		sweetSpotSize = newSize;
		//refresh view
		this.updateView;
	}

	dim_{ |value|

		if (value.isArray) {
			value=Cartesian.fromArray(value)
		};
		dim=value;
	}


	add { |obj,internal=true|

		// super.add(obj); // save object in objects array and set object's world to this
		objects.add(numObjects -> obj);
		// obj.world= this; //maybe this is redundant???
		//////
		obj.channel = obj.channel ? numObjects;
		if (this.getAllObjectNames.indexOf((numObjects).asSymbol).isNil.not) {
			obj.name = (numObjects.asSymbol++"*".asSymbol).asSymbol;
		} {

			obj.name = obj.name ?? {(numObjects).asSymbol};
		};

		//internal dictionaries;
		objectsID.add(numObjects -> obj.name);
		numObjects=numObjects+1;

		// send message to render
		if (internal) {
			this.sendMsg(\new,obj);
			this.sendMsg(\position,obj);

		};

		//refresh view
		this.updateView;

	}

	remove { |obj,internal=true|
		var key = objects.findKeyForValue(obj);
		if (key.isNil.not) {
			// remove object from objects list
			objects.removeAt(key);
			// remove object name from objectsID list
			objectsID.removeAt(key);
			//refresh view
			this.updateView;

			if (internal) {
				// send message to render
				this.sendMsg(\end,obj);
			}
		}
	}

	removeByName { |name|
		var key = objectsID.findKeyForValue(name);
		if (key.isNil.not) {
			this.remove(objects.at(key));
		} {
			("Object " ++ name ++ " not found").warn;
		}
	}

	// TODO: avoid the clip when crossing by [0,0,0]
	sendMsg { |type,obj|
		var name = obj.name;

		switch(type)

		// instance management
		{\new} {
			renderAddresses.do { |address|
				// create instance
				address.postln;
				address.sendMsg("/spatdifcmd/addEntity",name);
				// set media source
				address.sendMsg("/spatdif/source/"++name++"/media/type",\jack); // redundant because it's the only type supported by SpatialRender
				address.sendMsg("/spatdif/source/"++name++"/media/channel",obj.channel);
			}
		}

		{\end} {
			renderAddresses.do { |address|
				address.sendMsg("/spatdifcmd/removeEntity",name);
			}
		}


		// source characteristics

		{\channel} {
			renderAddresses.do { |address|
				address.sendMsg("/spatdif/source/"++name++"/media/channel",obj.channel);
			}
		}

		{\position} {
			var azi = obj.locSph.azimuth.rad2degree;
			var ele = obj.locSph.elevation.rad2degree;
			// no value changes inside sweetspot
			var rho = max(obj.locSph.rho,sweetSpotSize);

			// [azimuth, elevation, distance] in degrees
			renderAddresses.do { |address|
				address.sendMsg("/spatdif/source/"++name++"/position",azi,ele,rho,\aed);
			}
		}

		{\type} {
			renderAddresses.do { |address|
				address.sendMsg("/spatdif/source/"++name++"/type",obj.shape);
			}
		}

		{\present} {
			renderAddresses.do { |address|
				address.sendMsg("/spatdif/source/"++name++"/present",obj.present);
			}
		}


		// source characteristics -- extended

		{\width} {
			var da = obj.dAzimuth.rad2degree;
			var de = obj.dElevation.rad2degree;
			renderAddresses.do { |address|
				address.sendMsg("/spatdif/source/"++name++"/width",da,de);
			}
		}

		{\preserveArea} {
			renderAddresses.do { |address|
				address.sendMsg("/spatdif/source/"++name++"/preserveArea",obj.preserveArea);
			}
		}

	}

	contains {|ssObj|	//returns boolean if object inside world dim
		var arrayDim=dim.asArray;
		^ssObj.loc.any{ |l,i| l.abs > (arrayDim[i]/2) }.not
	}

	// wrap: toroidal world
	/* contain {|ssObj|
	var arrayDim=dim.asArray;
	if(ssObj.loc.any{ |l,i| l.abs > (arrayDim[i]/2) }) {
	ssObj.loc= ssObj.loc%dim;
	};
	}*/

	contain {|ssObj| // hold object inside world
		var arrayDim=dim.asArray;
		var loc, vel;

		loc=ssObj.loc.asArray;
		vel=ssObj.vel.asArray;

		////////////////////////////////////////// <--------------------------------
		loc.do{|l, i|
			if (i != 2) { //z must be treated different
				if( l.abs > (arrayDim[i]/2) ) { //if location exceeds world dimension

					//invert vel
					vel.put(i,vel[i]*(1-damping).neg);
					ssObj.vel_(vel);
					//fold loc

					loc.put(i,l.fold(arrayDim[i]/2.neg,arrayDim[i]/2));
					ssObj.loc_(loc);

				}
			} { //z coordinate only defined in range (0..depth)
				if( l > (arrayDim[i]) or:{l<0} ) { //if location exceeds world dimension

					//invert vel
					vel.put(i,vel[i]*(1-damping).neg);
					ssObj.vel_(vel);
					//fold loc
					loc.put(i,l.fold(0,arrayDim[i]));
					ssObj.loc_(loc);
				}
			}

		}
	}


	center {
		// ^dim/2;
		^Cartesian();
	}

	pause {
		time.pause;
	}

	resume {
		time.resume;
	}

	update {
		if (this.objects.size > 0 ) {
			this.objects.do { |obj,i|
				// TODO: addForce??
				var lastPos,newPos;
				var diffVector;
				var updateReg;

				// get last recorded position
				lastPos=obj.regLoc;
				//update
				obj.update;
				this.contain(obj); //wrap it into world limits
				// compare new and old positions
				newPos=obj.loc;
				diffVector=(lastPos-newPos).abs.asSpherical;
				updateReg=false;

				// check if the difference is somehow exceding JND
				if (diffVector.rho > rDiff or:{diffVector.azimuth > aziDiff or:{diffVector.elevation > eleDiff}}) {
					updateReg=true;
				};

				//update new location inside object and send message to render
				if (updateReg) {
					obj.regLoc_(newPos);
					this.sendMsg(\position,obj);
					/////////////////////////////////////////
					// see what is reported
					if (viewDiff) {
						this.updateView;
					}
				};

				// see in a continuous way
				if (not(viewDiff)) {
					this.updateView;
				}

			}
		}
	}

	updateView {
		{window.refresh}.defer;
	}

	///////////////// view
	// view: only Qt
	showView {
		window.visible_(true);
	}

	hideView {
		window.visible_(false)
	}

	alwaysOnTop { |bool|
		window.alwaysOnTop_(bool);
	}

	showNames { |bool|
		if (bool) {
			worldView.writeText_(true);
		} {
			worldView.writeText_(false);
		}
	}

	////////////////////////////////////////
	////
	//// accessing objects

	// get internal ID for a given object name
	getObjectID { |objName|
		^objectsID.findKeyForValue(objName);
	}

	// get object instance for a given object name
	getObject { |objName|
		^objects.at(this.getObjectID(objName));
	}

	// get an array of all existing objects
	getAllObjects {
		^objects.values.asArray;
	}

	// get an array of all names of existing objects
	getAllObjectNames {
		^objectsID.values.asArray;
	}

	//// RENDER ADDRESS ///////

	addRenderAddress { |addr|
		^renderAddresses.add(addr);
	}

	removeRenderAddress { |addr|
		^renderAddresses.remove(addr);
	}

	//// CLOSE WINDOW //////

	closeWindow {
		window.close;
	}




}
