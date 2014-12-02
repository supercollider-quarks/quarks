//Traer v3.0 physics engine, SuperCollider port by redFrik
//Originally created by Jeffrey Traer Bernstein http://murderandcreate.com/physics/

TraerSpring : TraerForce {
	var <>springConstant;							//Float
	var <>damping;								//Float
	var <>restLength;								//Float
	var <>a;										//TraerParticle
	var <>b;										//TraerParticle
	var <>on;										//Boolean
	*new {|a, b, ks, d, r|
		^super.new.initTraerSpring(a, b, ks, d, r);
	}
	initTraerSpring {|argA, argB, ks, d, r|
		springConstant= ks;
		damping= d;
		restLength= r;
		a= argA;
		b= argB;
		on= true;
	}
	turnOff {
		on= false;
	}
	turnOn {
		on= true;
	}
	isOn {
		^on;
	}
	isOff {
		^on.not;
	}
	getOneEnd {
		^a;
	}
	getTheOtherEnd {
		^b;
	}
	currentLength {
		^a.position.distanceTo(b.position);
	}
	strength {
		^springConstant;
	}
	setStrength {|ks|
		springConstant= ks;
	}
	setDamping {|d|
		damping= d;
	}
	setRestLength {|l|
		restLength= l;
	}
	apply {
		var a2bX, a2bY, a2bZ, va2bX, va2bY, va2bZ, a2bDistance, springForce, dampingForce, r;
		if(on and:{a.isFree or:{b.isFree}}, {
			a2bX= a.position.x-b.position.x;
			a2bY= a.position.y-b.position.y;
			a2bZ= a.position.z-b.position.z;
			a2bDistance= ((a2bX*a2bX)+(a2bY*a2bY)+(a2bZ*a2bZ)).sqrt;
			if(a2bDistance==0, {
				a2bX= 0;
				a2bY= 0;
				a2bZ= 0;
			}, {
				a2bX= a2bX/a2bDistance;
				a2bY= a2bY/a2bDistance;
				a2bZ= a2bZ/a2bDistance;
			});
			
			//spring force is proportional to how much it stretched
			springForce= (0-(a2bDistance-restLength))*springConstant;
			
			//want velocity along line b/w a & b, damping force is proportional to this
			va2bX= a.velocity.x-b.velocity.x;
			va2bY= a.velocity.y-b.velocity.y;
			va2bZ= a.velocity.z-b.velocity.z;
			dampingForce= (0-damping)*((a2bX*va2bX)+(a2bY*va2bY)+(a2bZ*va2bZ));
			
			//forceB is same as forceA in opposite direction
			r= springForce+dampingForce;
			a2bX= a2bX*r;
			a2bY= a2bY*r;
			a2bZ= a2bZ*r;
			
			if(a.isFree, {
				a.force.add(TraerVector3D(a2bX, a2bY, a2bZ));
			});
			if(b.isFree, {
				b.force.add(TraerVector3D(0-a2bX, 0-a2bY, 0-a2bZ));
			});
		});
	}
	setA {|p|
		a= p;
	}
	setB {|p|
		b= p;
	}
}
