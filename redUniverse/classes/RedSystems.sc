// this file is part of redUniverse toolkit /redFrik


//--base system
RedSystem {
	var <objects;
	add {|redObj| objects= objects.add(redObj)}
	remove {|redObj| objects.remove(redObj)}
	update {objects.do{|o| o.update}}
	at {|index| ^objects[index]}
	do {|func| objects.do(func)}
}

//--keeper of objects
RedObjectSystem : RedSystem {
	update {
		objects.do{|o|
			o.update;
			o.world.contain(o);
		}
	}
	addForce {|force| objects.do{|o| o.addForce(force)}}
	gravityForce {|redObj| ^objects.collect{|o| o.gravityForce(redObj)}}
	frictionForce {|constant| ^objects.collect{|o| o.vel.normalize*constant}}
	viscosityForce {|constant| ^objects.collect{|o| o.vel*constant}}
	contains {|redObj| ^objects.collect{|o| o.contains(redObj)}}
}

//--particles that die
RedParticleSystem : RedObjectSystem {
	var <>removeAction;							//called once when object is removed
	update {
		objects= objects.select{|o|
			o.update;
			o.world.contain(o);
			if(o.alive.not, {
				this.removeAction.value(o);
				o.world.remove(o);
				false;
			}, {true});
		};
	}
	alive {^objects.size>0}
}

//--keeping waves
RedWaveSystem : RedObjectSystem {
	var <length, <>freqs, <>phases, <>amps, <>angularVel, <>theta= 0, <wave;
	*new {|length, freqs, phases, amps, angularVel|
		^super.new.initRedWaveSystem(length, freqs, phases, amps, angularVel)}
	initRedWaveSystem {|argLen, argFreqs, argPhases, argAmps, argVel|
		length= argLen ? 10;
		freqs= (argFreqs ? {5.0.rand2}).dup(length);
		phases= (argPhases ? {2pi.rand}).dup(length);
		amps= (argAmps ? {0.5.rand}).dup(length);
		angularVel= argVel ? 0.25;
		this.prCalculateWave;
	}
	update {
		this.prCalculateWave;
		theta= theta+angularVel;
		super.update;
	}
	//--private
	prCalculateWave {
		wave= {|i|
			var x= i/length;
			freqs.sum{|f, j| sin(x*f*2pi+phases[j]+theta)*amps[j]}
		}.dup(length)
	}
}

//--TODO
RedFlockSystem : RedObjectSystem {
	//separation			(aka avoidance)
	//alignment			(aka copy)
	//cohesion			(aka center)
}

//--cellular automata TODO
RedCA : RedSystem {
	var <>neighbourFunc, <>rule;
	update {
		objects.do{|o, i|
			var cnt= 0;
			objects.do{|oo, j|
				if(i!=j, {
					if(neighbourFunc.value(o, oo), {
						cnt= cnt+1;
					});
				});
			};
			if(rule[1].indexOf(cnt).notNil, {
				//borne...
				if(rule[0].indexOf(cnt).notNil, {
					//dies...
				}, {
					//nothing...
				});
			});
			o.update;
		}
	}
}
