//Traer v3.0 physics engine, SuperCollider port by redFrik
//Originally created by Jeffrey Traer Bernstein http://murderandcreate.com/physics/

TraerParticle {
	var <>position;								//TraerVector3D
	var <>velocity;								//TraerVector3D
	var <>force;									//TraerVector3D
	var <>mass;									//Float
	var <>age;									//Float
	var <>dead;									//Boolean
	var <>fixed;									//Boolean
	*new {|m= 1|
		^super.new.initTraerParticle(m);
	}
	initTraerParticle {|m|
		position= TraerVector3D.new;
		velocity= TraerVector3D.new;
		force= TraerVector3D.new;
		mass= m;
		fixed= false;
		age= 0;
		dead= false;
	}
	distanceTo {|p|
		^position.distanceTo(p.position);
	}
	makeFixed {
		fixed= true;
		velocity.clear;
	}
	isFixed {
		^fixed;
	}
	isFree {
		^fixed.not;
	}
	makeFree {
		fixed= false;
	}
	setMass {|m|
		mass= m;
	}
	reset {
		age= 0;
		dead= false;
		position.clear;
		velocity.clear;
		force.clear;
		mass= 1;
	}
}
