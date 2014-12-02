//Traer v3.0 physics engine, SuperCollider port by redFrik
//Originally created by Jeffrey Traer Bernstein http://murderandcreate.com/physics/

TraerParticleSystem {
	classvar <>default_gravity= 0;					//Float
	classvar <>default_drag= 0.001;					//Float
	var <>particles;								//List
	var <>springs;								//List
	var <>attractions;								//List
	var <>customForces;							//List
	var <>integrator;								//TraerIntegrator
	var <>gravity;								//TraerVector3D
	var <>drag;									//Float
	var <>hasDeadParticles= false;					//Boolean
	setIntegrator {|integrator|
		switch(integrator,
			\RUNGE_KUTTA, {
				this.integrator= TraerRungeKuttaIntegrator(this);
			},
			\MODIFIED_EULER, {
				this.integrator= TraerModifiedEulerIntegrator(this);
			}
		);
	}
	setGravity {|x, y, z|
		gravity.set(x, y, z);
	}
	setDrag {|d|
		drag= d;
	}
	tick {|t= 1|
		integrator.step(t);
	}
	makeParticle {|mass= 1, x= 0, y= 0, z= 0|
		var p= TraerParticle(mass);
		p.position.set(x, y, z);
		particles.add(p);
		^p;
	}
	makeSpring {|a, b, ks, d, r|
		var s= TraerSpring(a, b, ks, d, r);
		springs.add(s);
		^s;
	}
	makeAttraction {|a, b, k, minDistance|
		var m= TraerAttraction(a, b, k, minDistance);
		attractions.add(m);
		^m;
	}
	clear {
		particles.clear;
		springs.clear;
		attractions.clear;
	}
	*new {|gx, gy, gz, drag|
		^super.new.initTraerParticleSystem(gx, gy, gz, drag);
	}
	initTraerParticleSystem {|gx, gy, gz, argDrag|
		integrator= TraerRungeKuttaIntegrator(this);
		particles= List();
		springs= List();
		attractions= List();
		gravity= TraerVector3D(gx?0, gy?default_gravity, gz?0);
		drag= argDrag?default_drag;
		customForces= List();
	}
	applyForces {
		if(gravity.isZero.not, {
			particles.size.do{|i|
				var p= particles[i];
				p.force.add(gravity);
			};
		});
		particles.size.do{|i|
			var p= particles[i];
			p.force.add(TraerVector3D(p.velocity.x*(0-drag), p.velocity.y*(0-drag), p.velocity.z*(0-drag)));
		};
		springs.size.do{|i|
			var f= springs[i];
			f.apply;
		};
		attractions.size.do{|i|
			var f= attractions[i];
			f.apply;
		};
		customForces.size.do{|i|
			var f= customForces[i];
			f.apply;
		};
	}
	clearForces {
		particles.do{|p|
			p.force.clear;
		};
	}
	numberOfParticles {
		^particles.size;
	}
	numberOfSprings {
		^springs.size;
	}
	numberOfAttractions {
		^attractions.size;
	}
	getParticle {|i|
		^particles[i];
	}
	getSpring {|i|
		^springs[i];
	}
	getAttraction {|i|
		^attractions[i];
	}
	addCustomForce {|f|
		customForces.add(f);
	}
	numberOfCustomForces {
		^customForces.size;
	}
	getCustomForce {|i|
		^customForces[i];
	}
	removeCustomForce {|f|
		^customForces.remove(f);	//TODO check what returned, resizing, etc?
	}
	removeParticle {|p|
		particles.remove(p);		//TODO check if working, resizing, etc?
	}
	removeSpring {|s|
		^springs.remove(s);		//TODO check if working, resizing, etc?
	}
	removeAttraction {|a|
		^attractions.remove(a);		//TODO check if working, resizing, etc?
	}
}
