//Traer v3.0 physics engine, SuperCollider port by redFrik
//Originally created by Jeffrey Traer Bernstein http://murderandcreate.com/physics/

TraerRungeKuttaIntegrator : TraerIntegrator {
	var <>originalPositions;						//List
	var <>originalVelocities;						//List
	var <>k1Forces;								//List
	var <>k1Velocities;							//List
	var <>k2Forces;								//List
	var <>k2Velocities;							//List
	var <>k3Forces;								//List
	var <>k3Velocities;							//List
	var <>k4Forces;								//List
	var <>k4Velocities;							//List
	var <>s;										//TraerParticleSystem
	*new {|s|
		^super.new.initTraerRungeKuttaIntegrator(s);
	}
	initTraerRungeKuttaIntegrator {|argS|
		s= argS;
		originalPositions= List();
		originalVelocities= List();
		k1Forces= List();
		k1Velocities= List();
		k2Forces= List();
		k2Velocities= List();
		k3Forces= List();
		k3Velocities= List();
		k4Forces= List();
		k4Velocities= List();
	}
	allocateParticles {
		while({s.particles.size>originalPositions.size}, {
			originalPositions.add(TraerVector3D.new);
			originalVelocities.add(TraerVector3D.new);
			k1Forces.add(TraerVector3D.new);
			k1Velocities.add(TraerVector3D.new);
			k2Forces.add(TraerVector3D.new);
			k2Velocities.add(TraerVector3D.new);
			k3Forces.add(TraerVector3D.new);
			k3Velocities.add(TraerVector3D.new);
			k4Forces.add(TraerVector3D.new);
			k4Velocities.add(TraerVector3D.new);
		});
	}		
	step {|deltaT|
		this.allocateParticles;
		
		/////////////////////////////////////////////////////////
		//save original positions and velocities
		s.particles.size.do{|i|
			var p= s.particles[i];
			if(p.isFree, {
				originalPositions[i]= p.position.copy;
				originalVelocities[i]= p.velocity.copy;
			});
			p.force.clear;						//and clear the forces
		};
		
		/////////////////////////////////////////////////////////
		//get all the k1 values
		s.applyForces;
		
		//save the intermediate forces
		s.particles.size.do{|i|
			var p= s.particles[i];
			if(p.isFree, {
				k1Forces[i]= p.force.copy;
				k1Velocities[i]= p.velocity.copy;
			});
			p.force.clear;
		};
		
		/////////////////////////////////////////////////////////
		//get k2 values
		s.particles.size.do{|i|
			var p= s.particles[i];
			var originalPosition, k1Velocity, originalVelocity, k1Force;
			if(p.isFree, {
				originalPosition= originalPositions[i];
				k1Velocity= k1Velocities[i];
				p.position.x= originalPosition.x+(k1Velocity.x*0.5*deltaT);
				p.position.y= originalPosition.y+(k1Velocity.y*0.5*deltaT);
				p.position.z= originalPosition.z+(k1Velocity.z*0.5*deltaT);
				originalVelocity= originalVelocities[i];
				k1Force= k1Forces[i];
				p.velocity.x= originalVelocity.x+(k1Force.x*0.5*deltaT/p.mass);
				p.velocity.y= originalVelocity.y+(k1Force.y*0.5*deltaT/p.mass);
				p.velocity.z= originalVelocity.z+(k1Force.z*0.5*deltaT/p.mass);
			});
		};
		s.applyForces;
		
		//save the intermediate forces
		s.particles.size.do{|i|
			var p= s.particles[i];
			if(p.isFree, {
				k2Forces[i]= p.force.copy;
				k2Velocities[i]= p.velocity.copy;
			});
			p.force.clear;
		};
		
		/////////////////////////////////////////////////////////
		//get k3 values
		s.particles.size.do{|i|
			var p= s.particles[i];
			var originalPosition, k2Velocity, originalVelocity, k2Force;
			if(p.isFree, {
				originalPosition= originalPositions[i];
				k2Velocity= k2Velocities[i];
				p.position.x= originalPosition.x+(k2Velocity.x*0.5*deltaT);
				p.position.y= originalPosition.y+(k2Velocity.y*0.5*deltaT);
				p.position.z= originalPosition.z+(k2Velocity.z*0.5*deltaT);
				originalVelocity= originalVelocities[i];
				k2Force= k2Forces[i];
				p.velocity.x= originalVelocity.x+(k2Force.x*0.5*deltaT/p.mass);
				p.velocity.y= originalVelocity.y+(k2Force.y*0.5*deltaT/p.mass);
				p.velocity.z= originalVelocity.z+(k2Force.z*0.5*deltaT/p.mass);
			});
		};
		s.applyForces;
		
		//save the intermediate forces
		s.particles.size.do{|i|
			var p= s.particles[i];
			if(p.isFree, {
				k3Forces[i]= p.force.copy;
				k3Velocities[i]= p.velocity.copy;
			});
			p.force.clear;
		};
		
		/////////////////////////////////////////////////////////
		//get k4 values
		s.particles.size.do{|i|
			var p= s.particles[i];
			var originalPosition, k3Velocity, originalVelocity, k3Force;
			if(p.isFree, {
				originalPosition= originalPositions[i];
				k3Velocity= k3Velocities[i];
				p.position.x= originalPosition.x+(k3Velocity.x*deltaT);
				p.position.y= originalPosition.y+(k3Velocity.y*deltaT);
				p.position.z= originalPosition.z+(k3Velocity.z*deltaT);
				originalVelocity= originalVelocities[i];
				k3Force= k3Forces[i];
				p.velocity.x= originalVelocity.x+(k3Force.x*deltaT/p.mass);
				p.velocity.y= originalVelocity.y+(k3Force.y*deltaT/p.mass);
				p.velocity.z= originalVelocity.z+(k3Force.z*deltaT/p.mass);
			});
		};
		s.applyForces;
		
		//save the intermediate forces
		s.particles.size.do{|i|
			var p= s.particles[i];
			if(p.isFree, {
				k4Forces[i]= p.force.copy;
				k4Velocities[i]= p.velocity.copy;
			});
		};
		
		/////////////////////////////////////////////////////////
		//put them all together and what do you get?
		s.particles.size.do{|i|
			var p= s.particles[i];
			var originalPosition, k1Velocity, k2Velocity, k3Velocity, k4Velocity;
			var originalVelocity, k1Force, k2Force, k3Force, k4Force;
			p.age= p.age+deltaT;
			if(p.isFree, {
				
				//update position
				originalPosition= originalPositions[i];
				k1Velocity= k1Velocities[i];
				k2Velocity= k2Velocities[i];
				k3Velocity= k3Velocities[i];
				k4Velocity= k4Velocities[i];
				p.position.x= originalPosition.x+(deltaT/6*(k1Velocity.x+(2*k2Velocity.x)+(2*k3Velocity.x)+k4Velocity.x));
				p.position.y= originalPosition.y+(deltaT/6*(k1Velocity.y+(2*k2Velocity.y)+(2*k3Velocity.y)+k4Velocity.y));
				p.position.z= originalPosition.z+(deltaT/6*(k1Velocity.z+(2*k2Velocity.z)+(2*k3Velocity.z)+k4Velocity.z));
				
				//update velocity
				originalVelocity= originalVelocities[i];
				k1Force= k1Forces[i];
				k2Force= k2Forces[i];
				k3Force= k3Forces[i];
				k4Force= k4Forces[i];
				p.velocity.x= originalVelocity.x+(deltaT/(6*p.mass)*(k1Force.x+(2*k2Force.x)+(2*k3Force.x)+k4Force.x));
				p.velocity.y= originalVelocity.y+(deltaT/(6*p.mass)*(k1Force.y+(2*k2Force.y)+(2*k3Force.y)+k4Force.y));
				p.velocity.z= originalVelocity.z+(deltaT/(6*p.mass)*(k1Force.z+(2*k2Force.z)+(2*k3Force.z)+k4Force.z));
			});
		};
	}
}
