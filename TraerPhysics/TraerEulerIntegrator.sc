//Traer v3.0 physics engine, SuperCollider port by redFrik
//Originally created by Jeffrey Traer Bernstein http://murderandcreate.com/physics/

TraerEulerIntegrator : TraerIntegrator {
	var <>s;										//TraerParticleSystem
	*new {|s|
		^super.new.initTraerEulerIntegrator(s);
	}
	initTraerEulerIntegrator {|argS|
		s= argS;
	}
	step {|t|
		s.clearForces;
		s.applyForces;
		s.numberOfParticles.do{|i|
			var p= s.getParticle(i);
			if(p.isFree, {
				p.velocity.add(TraerVector3D(p.force.x/(p.mass*t), p.force.y/(p.mass*t), p.force.z/(p.mass*t)));
				p.position.add(TraerVector3D(p.velocity.x/t, p.velocity.y/t, p.velocity.z/t));
			});
		};
	}
}
