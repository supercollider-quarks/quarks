//Traer v3.0 physics engine, SuperCollider port by redFrik
//Originally created by Jeffrey Traer Bernstein http://murderandcreate.com/physics/

// attract positive repel negative

TraerAttraction : TraerForce {
	var <>a, <>b;									//TraerParticle
	var <>k;										//Float
	var <>on;										//Boolean
	var <>distanceMin;								//Float
	var <>distanceMinSquared;						//Float
	*new {|a, b, k, distanceMin|
		^super.new.initTraerAttraction(a, b, k, distanceMin);
	}
	initTraerAttraction {|argA, argB, argK, argDistanceMin|
		a= argA;
		b= argB;
		k= argK;
		on= true;
		distanceMin= argDistanceMin;
		distanceMinSquared= distanceMin*distanceMin;
	}
	setA {|p|
		a= p;
	}
	setB {|p|
		b= p;
	}
	getMinimumDistance {
		^distanceMin;
	}
	setMinimumDistance {|d|
		distanceMin= d;
		distanceMinSquared= d*d;
	}
	turnOff {
		on= false;
	}
	turnOn {
		on= true;
	}
	setStrength {|k|
		this.k= k;
	}
	getOneEnd {
		^a;
	}
	getTheOtherEnd {
		^b;
	}
	apply {
		var a2bX, a2bY, a2bZ, a2bDistanceSquared, force, length;
		if(on and:{a.isFree or:{b.isFree}}, {
			a2bX= a.position.x-b.position.x;
			a2bY= a.position.y-b.position.y;
			a2bZ= a.position.z-b.position.z;
			a2bDistanceSquared= (a2bX*a2bX)+(a2bY*a2bY)+(a2bZ*a2bZ);
			if(a2bDistanceSquared<distanceMinSquared, {
				a2bDistanceSquared= distanceMinSquared;
			});
			force= k*a.mass*b.mass/a2bDistanceSquared;
			length= a2bDistanceSquared.sqrt;
			
			//make unit vector
			a2bX= a2bX/length;
			a2bY= a2bY/length;
			a2bZ= a2bZ/length;
			
			//multiply by force
			a2bX= a2bX*force;
			a2bY= a2bY*force;
			a2bZ= a2bZ*force;
			
			//apply
			if(a.isFree, {
				a.force.add(TraerVector3D(0-a2bX, 0-a2bY, 0-a2bZ));
			});
			if(b.isFree, {
				b.force.add(TraerVector3D(a2bX, a2bY, a2bZ));
			});
		});
	}
	getStrength {
		^k;
	}
	isOn {
		^on;
	}
	isOff {
		^on.not;
	}
}
