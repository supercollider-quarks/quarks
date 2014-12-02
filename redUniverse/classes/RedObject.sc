// this file is part of redUniverse toolkit /redFrik

//todo:
//collision detection for redrock, groundplane etc
//spring: now euler method but use runge-kutta method instead - mention which in the helpfile
//pendulum and boid: find general multi dimensional methods

//--base for all objects
RedObject {
	var <>world,									//world
		<>loc, <>vel, <>accel,						//vectors
		<>mass, <>size;							//floats
	*new {|world, loc, vel, accel, mass, size|
		^super.newCopyArgs(
			world ?? {RedWorld.new},				//defaults to 2d world
			loc ?? {RedVector2D[0, 0]},
			vel ?? {RedVector2D[0, 0]},
			accel ?? {RedVector2D[0, 0]},
			mass ? 1,
			size ? 10
		).initRedObject
	}
	initRedObject {world.add(this)}
	update {
		vel= (vel+accel).limit(world.maxVel);
		loc= loc+vel;
		accel= 0;
	}
	addForce {|force| accel= accel+(force/mass)}
	gravityForce {|redObj|
		var dir= redObj.loc-loc;
		var dist= dir.mag;
		^dir*(world.gravity.mag*mass*redObj.mass/(dist*dist))
	}
	frictionForce {|constant| ^vel.normalize*constant}	//watch out for NaN here if vel is zero
	viscosityForce {|constant| ^vel*constant}
	spring {|redObj, stiffness= 0.1, damping= 0.9, length= 0|
		var targetLoc, dir, dist;
		if(length==0, {
			targetLoc= redObj.loc;
		}, {
			dir= loc-redObj.loc;
			dist= dir.mag;
			if(dist!=0, {
				targetLoc= redObj.loc+(dir/dist*length);
			}, {
				targetLoc= redObj.loc;
			});
		});
		this.addForce((targetLoc-loc)*stiffness);
		vel= vel*damping;
	}
	contains {|redObj| ^loc.distance(redObj.loc)<(size+redObj.size)}
	containsLoc {|aLoc| ^loc.distance(aLoc)<size}
	collide {|redObj, safety= 3|
		var normal, change, aLoc, bLoc;
		if(this.contains(redObj), {
			aLoc= loc;
			bLoc= redObj.loc;
			while({
				loc= loc-vel;
				redObj.loc= redObj.loc-redObj.vel;
				safety= safety-1;
				safety>0 and:{this.contains(redObj)};
			});
			normal= (redObj.loc-loc).normalize;
			change= normal.dot(vel-redObj.vel)*normal;
			accel= accel-(change*(redObj.mass/mass));
			redObj.accel= redObj.accel+(change*(mass/redObj.mass));
			loc= aLoc;
			redObj.loc= bLoc;
			^true
		});
		^false
	}
}

//--an invisible object
RedHiddenObject : RedObject {
	initRedObject {}								//hides it from the world
}

//--an object that oscillates
RedPendulum : RedObject {							//size is used as radius here
	var <>theta= 1, <>angularVel= 0,					//floats
		taccel= 0;
	update {
		angularVel= angularVel+taccel*(1-world.damping); //no need to limit to maxVel as angular
		theta= theta+angularVel;
		taccel= 0;
		super.update;
	}
	addForceAngular2D {|force|
		var g= RedVector2D[cos(theta), sin(theta).neg];
		taccel= taccel+(g*force/mass/size).sum;
	}
	pendulumOffset2D {								//pendulum location relative to object loc
		^RedVector2D[sin(theta)*size, cos(theta)*size];
	}
	pendulumLoc2D {								//pendulum actual location
		^this.pendulumOffset2D+loc;
	}
	addForceAngular1D {|force|
		var g= RedVector[cos(theta)];
		taccel= taccel+(g*force/mass/size).sum;
	}
	pendulumOffset1D {
		^RedVector[sin(theta)*size];
	}
	pendulumLoc1D {
		^this.pendulumOffset1D+loc;
	}
	addForceAngular3D {|force|
		var g= RedVector3D[cos(theta), sin(theta).neg, cos(theta)];
		taccel= taccel+(g*force/mass/size).sum;
	}
	pendulumOffset3D {
		^RedVector3D[sin(theta)*size, cos(theta)*size, cos(theta)*size];
	}
	pendulumLoc3D {
		^this.pendulumOffset3D+loc;
	}
}

//--an object that ages with time
RedParticle : RedObject {
	var <>age= 1000;								//floats
	update {
		age= age-1;
		super.update;
	}
	alive {^age>0}
}

//--an object with a desire
RedBoid : RedParticle {
	var <>maxForce= 10,							//floats
		<>wtheta= 0, <>wradius= 10,					//floats
		<>wdistance= 50, <>wchange= 0.5;				//floats
	addForceSeek {|redVec| accel= accel+this.steer(redVec, 0)}
	addForceArrive {|redVec, slowdown= 100| accel= accel+this.steer(redVec, slowdown)}
	addForceWander2D {
		var flicker= vel.normalize*wdistance+RedVector2D[wradius*cos(wtheta), wradius*sin(wtheta)];
		var target= flicker+loc;					//target is a point on a circle ahead
		this.addForceSeek(target);
		wtheta= wtheta+wchange.rand2;
		^target									//return target vector
	}
	addForceWander1D {
		var flicker= vel.normalize*wdistance+(wradius*cos(wtheta));
		var target= flicker+loc;					//target is a point on a circle ahead
		this.addForceSeek(target);
		wtheta= wtheta+wchange.rand2;
		^target									//return target vector
	}
	addForceWander3D {
		var flicker= vel.normalize*wdistance+RedVector3D[wradius*cos(wtheta), wradius*sin(wtheta), wradius*tan(wtheta)];
		var target= flicker+loc;					//target is a point on a circle ahead
		this.addForceSeek(target);
		wtheta= wtheta+wchange.rand2;
		^target									//return target vector
	}
	steer {|redVec, slowdown= 0|					//target vector, 0 means no slowdown
		var dir= redVec-loc;
		var dist= dir.mag;
		dir= dir.normalize;
		if(slowdown!=0 and:{dist<slowdown}, {			//if slowdown factor set and close enough
			dir= dir*(dist/slowdown);				//slowdown as approaching
		});
		^(dir*world.maxVel-vel).limit(maxForce);		//return steer vector
	}
}

//--an object that can sense and act.  has a state dict
RedAgent : RedBoid {
	var <>state;									//dictionary
	var >act, >sense;								//functions
	update {
		sense.value(this);
		act.value(this);
		super.update;
	}
}

//--an obstacle with infinite mass
RedRock : RedObject {
	initRedObject {
		mass= 2147483647;		//was inf; but then acting as a black hole
		super.initRedObject
	}
	mass_ {}
}

//--an object with energy
RedFood : RedObject {
	var <>energy= 1000;
	eat {|amount= 1|
		var newEnergy= (energy-amount).max(0);
		var newAmount= energy-newEnergy;
		energy= newEnergy;
		^newAmount
	}
	alive {^energy>0}
}
