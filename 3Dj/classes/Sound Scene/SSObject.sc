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
// SSObject.sc
// Based on RedUniverse quark (by redFrik)
//
// This class implements a Sound Scene Object
//
// Each SSObject instance extends RedObject with:
// -> shape
// -> name id
// -> audio channel associated
// -> motion: dynamic behavior associated
//
////////////////////////////////////////////////////////////////////////////
//
// TODO: motion composition
//
//////////////////////////////////////////////////////////////////////////////

SSObject : RedObject {

	//adds shape
	var <shape;
	var <motion;
	////
	var <motionList;
	////
	var <>regLoc; //initialize to start pos
	var <>name;
	var <>channel;
	//
	var <>gravity;
	var <>friction;
	//
	var <dAzimuth,<dElevation; // width for extended objects
	var <preserveArea; // for extended objects

	var <present;

	*new {|world, loc, vel, accel, mass, size, shape, gravity, friction, name, channel,registerInWorld=true|
		//almost same method as in RedObject

		^super.newCopyArgs(
			world ?? {SSWorld.new},
			loc !? {if (loc.isArray) {Cartesian.fromArray(loc)} {loc} } ?? {Cartesian(0,0,0)},
			vel !? {if (vel.isArray) {Cartesian.fromArray(vel)} {vel} } ?? {Cartesian(0,0,0)},
			accel !? {if (accel.isArray) {Cartesian.fromArray(accel)} {accel} } ?? {Cartesian(0,0,0)},
			mass ? 1,
			size ? 1
			//change init function
		).initSSObject(shape, gravity, friction, name, channel,registerInWorld)
	}


	initSSObject { |myshape, mygravity, myfriction, myname, mychannel,registerInWorld=true|

		// var numNames;
		regLoc=loc;

		// if shape is a point give it a infinitesimal small value
		shape = myshape ? \point;
		if (shape == \point) {
			size=0.05;
		};

		gravity = mygravity ? false;
		friction = myfriction ? false;

		dAzimuth=AmbXEnc.delta;
		dElevation=AmbXEnc.delta;
		preserveArea=0; //false

		present = true;

		//channel = mychannel ? 0;
		//name = myname ?? {(\track++channel).asSymbol};
		channel=mychannel; //if they are nil, they will be assigned by default SSWorld numObjects


		name=myname; //if they are nil, they will be assigned by default SSWorld numObjects

		//check if the name already exists in the world: change then if true
		if (world.getAllObjectNames.indicesOfEqual(myname).size>0) {
			name=nil;
			("Name "++myname++" already exists; default name applied").warn;
		};

		this.setMotion(\static); //static as default
		////
		motionList=List.new;
		// this.addMotion(\static);
		////

		if (registerInWorld) {
			this.initRedObject; // calls this.world.add(this);
		}
	}

	remove {
		world.remove(this);
	}

	/////////////////////////////////////////////////////////////////////////////////
	// getter / setter methods


	setChannel { |newChannel,internal=true|
		channel = newChannel;
		if (internal) {
			world.sendMsg(\channel,this);
		}
	}

	present_ { |bool,internal=true|
		present = bool;
		if (internal) {
			world.sendMsg(\present,this);
		}
	}

	// private:: auto-casting to Cartesian
	setValue { |value,type=\cartesian|

		if (value.isArray) {
			if (type==\cartesian) {
				value=Cartesian.fromArray(value)
			};
			if (type==\spherical) {
				value=Spherical.fromArray(value)
			};
		}

		^value.asCartesian;

	}

	///////////////// loc

	/*	loc_ { |newLoc|
	loc=this.setValue(newLoc);
	}*/

	loc_ { |newLoc,x=nil,y=nil,z=nil|
		newLoc ?? {
			newLoc = loc;
			x !? {newLoc.x = x};
			y !? {newLoc.y = y};
			z !? {newLoc.z = z};
		};
		loc=this.setValue(newLoc);
	}

	/*	locX_ { |newX|
	var newLoc=[newX,]
	}*/

	locSph {
		^loc.asSpherical;
	}

	/*	locSph_ {|newLocSph|
	loc=this.setValue(newLocSph,\spherical);
	}*/

	locSph_ { |newLocSph,rho=nil,azi=nil,ele=nil|
		newLocSph ?? {
			newLocSph = this.locSph;
			rho !? {newLocSph.rho = rho};
			azi !? {newLocSph.azimuth = azi};
			ele !? {newLocSph.elevation = ele};
		};
		loc=this.setValue(newLocSph,\spherical);
	}

	///////////////// vel

	vel_ { |newVel|
		vel=this.setValue(newVel);
	}

	addVel { |newVel|
		vel=vel+this.setValue(newVel);
	}

	velSph {
		^vel.asSpherical;
	}

	velSph_ {|newVelSph|
		vel=this.setValue(newVelSph,\spherical);
	}

	///////////////// accel

	accel_ { |newAccel|
		accel=this.setValue(newAccel);
	}

	accelSph {
		^accel.asSpherical;
	}

	accelSph_ {|newAccelSph|
		accel=this.setValue(newAccelSph,\spherical);
	}

	stop {
		this.accel_([0,0,0]);
		this.vel_([0,0,0]);
	}

	///// only for extended objects

	dAzimuth_ { |newD,internal=true| // in radians

		// do not allow values of 0
		dAzimuth = newD.clip(AmbXEnc.delta,2pi);

		if (internal) {
			world.sendMsg(\width,this);
		}
	}

	dElevation_ { |newD,internal=true| // in radians
		var e = this.locSph.elevation;

		// do not allow values of 0
		dElevation = newD.clip(AmbXEnc.delta,pi);

		// requieriment: e+de/2 <= pi/2 AND: e-de/2 >= -pi/2
		e = min(e,(pi/2)-(dElevation/2));
		e = max(e,(-pi/2)+(dElevation/2));
		this.locSph_(ele:e);

		if (internal) {
			world.sendMsg(\width,this);
		}
	}

	shape_ { |newShape,internal=true|
		shape = newShape;

		if (internal) {
			world.sendMsg(\type,this);
		}
	}

	preserveArea_ { |bool,internal=true|
		if (bool) {
			preserveArea = 1; //int values for the server
		} {
			preserveArea = 0;
		};

		if (internal) {
			world.sendMsg(\preserveArea,this);
		}
	}


	/////////////////////////////////////////////////////////////////////////////////
	//set
	setMotion { |type=\static ... args|
		motion = switch (type)
		{\static} {Static.new(this)}
		{\rect} {RectMov.new(this,args)}
		{\random} {RandomMov.new(this,args)}
		{\brown} {Brownian.new(this,args)}
		{\shm} {Shm.new(this,args)}
		{\orbit} {Orbit.new(this,args)};

	}

	/* addMotion { |type=\static ... args|
	var motion = switch (type)
	{\static} {Static.new(this)}
	{\rect} {RectMov.new(this,args)}
	{\orbit} {Orbit.new(this,args)};

	motionList.add(motion);
	}*/


	// update method is called from inside SSWorld update routine
	update {
		// gravity values are in m/s/s, but we apply it every stepFreq
		if (gravity)  { this.addForce(world.gravity/world.stepFreq) };
		if (friction) { this.vel_(this.vel*(1-world.friction)) };

		motion.next;
		// motionList.do(_.next);
	}


}
