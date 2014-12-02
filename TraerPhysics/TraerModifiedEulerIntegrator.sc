//Traer v3.0 physics engine, SuperCollider port by redFrik
//Originally created by Jeffrey Traer Bernstein http://murderandcreate.com/physics/

TraerModifiedEulerIntegrator : TraerIntegrator {
	var <>s;										//TraerParticleSystem
	*new {|s|
		^super.new.initTraerModifiedEulerIntegrator(s);
	}
	initTraerModifiedEulerIntegrator {|argS|
		s= argS;
	}
	step {|t|
		var halftt;
		s.clearForces;
		s.applyForces;
		halftt= 0.5*t*t;
		s.numberOfParticles.do{|i|
			var p= s.getParticle(i);
			var ax, ay, az;
			if(p.isFree, {
				ax= p.force.x/p.mass;
				ay= p.force.y/p.mass;
				az= p.force.z/p.mass;
				p.position.add(TraerVector3D(p.velocity.x/t, p.velocity.y/t, p.velocity.z/t));
				p.position.add(TraerVector3D(ax*halftt, ay*halftt, az*halftt));
				p.velocity.add(TraerVector3D(ax/t, ay/t, az/t));
			});
		};
	}
}
