/*
    Unit Library
    The Game Of Life Foundation. http://gameoflife.nl
    Copyright 2006-2011 Miguel Negrao, Wouter Snoei.

    GameOfLife Unit Library: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GameOfLife Unit Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GameOfLife Unit Library.  If not, see <http://www.gnu.org/licenses/>.

(
Udef(\test, {
	var env = UEnvGen.ar(Env([200,400,100,200],  [2.0,2.0,2.0], \lin));
	var out = SinOsc.ar( env ) * 0.2;
	UOut.ar(0, out )
});
Udef(\test2, {
	var env = UXLine.ar(200, 400, 10, argName: \freq);
	var out = SinOsc.ar( env ) * 0.2;
	UOut.ar(0, out )
});
Udef(\test1Rel, {
	var env = UEnvGenRel.ar(Env([200,400,100,200],  [2.0,4.0,2.0], \lin));
	var out = SinOsc.ar( env ) * 0.2;
	UOut.ar(0, out )
});
Udef(\test2Rel, {
	var env = UXLineRel.ar(200,400, argName: \freq);
	var out = SinOsc.ar( env ) * 0.2;
	UOut.ar(0, out )
});
Udef(\test3Rel, {
	var env = UXLineRel.ar(200,400, 0.5, argName: \freq);
	var out = SinOsc.ar( env ) * 0.2;
	UOut.ar(0, out )
})
)

(
UScore(
UChain(3,1,10, \test2, \stereoOutput),
UChain(0,0,6, \test, \stereoOutput)
).gui
)


//every time different dur
(
UScore(
	UChain(0,0,5, \test1Rel, \stereoOutput),
	UChain(5,1,5, \test2Rel, \stereoOutput),
    UChain(15,2,5, \test3Rel, \stereoOutput)
).gui
)
*/

UEnvGen {

	*prepareEnv { |env, spec|
		env = env ?? { Env() };
		case { env.isKindOf(Env) or: env.isArray } {
			env = env.asArray;
		} { env.isKindOf(Symbol) } {
			// uncomment when EnvSpec is added
			Udef.addBuildSpec(
				ArgSpec(env, EnvM(), UEnvSpec( EnvM(), spec ), mode: \init)
			);
			env = env.ir( Env(0!32,0!31).asArray );
		};
		^[ 0, env[0], env[1], env[5,9..(env.size-1)*4].sum ] ++
			env[4..].clump(4).collect({ |item|
				[ item[1], item[2], item[3], item[0] ]
			}).flatten(1);
	}
	
	*getLineArgs { |env, timeScale = 1|
		var start, dur;
		start = \u_startPos.kr(0.0);
		timeScale = this.getTimeScale( timeScale );
		dur = env[3];
		^[ start / timeScale, dur, ((dur * timeScale)-start).max(0) ]
	}
	
	*getTimeScale { |timeScale = 1|
		if( timeScale.class == Symbol ) {
			Udef.addBuildSpec(
				ArgSpec( timeScale, 1, [0.25,4,\exp,0,1].asSpec )
			);
			^timeScale.kr( 1 );
		} {
			^timeScale;
		};
	}
	
	*getLoopMode { |loop = 1|
		if( loop.class == Symbol ) {
			Udef.addBuildSpec(
				ArgSpec( loop, 0, ListSpec((..2),0,["off", "loop", "alternate"]) )
			);
			^loop.kr( 1 );
		} {
			^loop;
		};
	}
	
	*getDelay { |delay = 0|
		if( delay.class == Symbol ) {
			Udef.addBuildSpec(
				ArgSpec( delay, 0, SMPTESpec(), mode: \init )
			);
			^delay.ir( 1 );
		} {
			^delay;
		};
	}
	
	*getTrigger { |trigger = 0|
		if( trigger.class == Symbol ) {
			Udef.addBuildSpec(
				ArgSpec( trigger, 1, TriggerSpec() )
			);
			^trigger.tr( 1 ) * (1 - Impulse.kr(0)); // ignore first trigger
		} {
			^trigger;
		};
	}
	
	*getLineKr { |dur, timeScale = 1, loop = 0, delay = 0, trigger = 0|
		var start, phasor;
		start = \u_startPos.ir(0.0);
		timeScale = this.getTimeScale( timeScale );
		loop = this.getLoopMode( loop );
		delay = this.getDelay( delay );
		trigger = this.getTrigger( trigger );
		phasor = Phasor.kr( trigger, 
			(ControlDur.ir + SampleDur.ir) / timeScale, 
			(start - delay) / timeScale, inf, 0 
		).max(0);
		if( loop.isUGen ) {
			^Select.kr( loop, [ 
				phasor.clip(0,dur),
				phasor.wrap(0,dur),
				phasor.fold(0,dur)
			]);
		} {
			^switch( loop.asInt,
				0, { phasor.clip(0,dur); },
				1, { phasor.wrap(0,dur); },
				2, { phasor.fold(0,dur); }
			);
		};
	}

	*ar { |env, spec, timeScale = 1|
		var phasor;
		if( env.isKindOf( Env ) && { spec.isNil } ) {
			spec = ControlSpec( env.levels.minItem, env.levels.maxItem );
			env.levels = env.levels.normalize;
		};
		env = this.prepareEnv(env, spec);
		spec = spec.asSpec;
		phasor = Line.ar( *this.getLineArgs(env, timeScale));
		^spec.map( IEnvGen.ar(env, phasor) )
	}

	*kr { |env, spec, timeScale = 1, loop = 0, delay = 0, trigger = 0|
		var phasor;
		if( env.isKindOf( Env ) && { spec.isNil } ) {
			spec = ControlSpec( env.levels.minItem, env.levels.maxItem );
			env.levels = env.levels.normalize;
		};
		env = this.prepareEnv(env, spec);
		spec = spec.asSpec;
		phasor = this.getLineKr( env[3], timeScale, loop, delay, trigger );
		^spec.map( IEnvGen.kr(env, phasor) )
	}

}

UXLine {

	*makeControl {  |start, end, argName|
		^argName !? {
			Udef.addBuildSpec(ArgSpec(argName, [start,end], RangeSpec(start, end)));
			argName.kr([start,end])
		} ?? { [start, end] }
	}

	*kr{ |start=1.0, end=2.0, time = 1.0, loop = 0, delay = 0, trigger = 0, argName|
		#start, end = this.makeControl(start, end, argName);
		^[ start, end, \exp ].asSpec.map( UEnvGen.getLineKr( 1, time, loop, delay, trigger ) );
	}

	*ar{ |start=1.0, end=2.0, time = 1, argName|
		#start, end = this.makeControl(start, end, argName);
		^UEnvGen.kr( Env([0,1],[time],\lin), [start, end, \exp] );
	}

}

ULine {

	*kr{ |start=0.0, end=1.0, time = 1.0, loop = 0, delay = 0, trigger = 0, argName|
		#start, end = UXLine.makeControl(start, end, argName);
		^[ start, end, \lin ].asSpec.map( UEnvGen.getLineKr( 1, time, loop, delay, trigger ) );
	}

	*ar{ |start=0.0, end=1.0, time = 1, argName|
		#start, end = UXLine.makeControl(start, end, argName);
		^UEnvGen.ar( Env([0,1],[time],\lin), [start, end, \lin] );
	}

}

UEnvGenRel : UEnvGen {
	
	*getLineArgs { |env, timeScale = 1|
		var start, dur, envdur;
		start = \u_startPos.kr(0.0);
		dur = \u_dur.kr(1.0)+start;
		timeScale = this.getTimeScale( timeScale );
		envdur = env[3] / timeScale;
		^[ envdur * (start/dur), envdur, (dur-start).max(0) ]
	}
	
	*getLineKr { |dur, timeScale = 1, loop = 0, delay = 0, trigger = 0|
		timeScale = this.getTimeScale( timeScale ) * ((\u_dur.kr(1.0) + \u_startPos.ir(0)) / dur);
		^super.getLineKr( dur, timeScale, loop, delay, trigger )
	}
	
}

UXLineRel {

	*kr{ |start=0.0, end=1.0, timeScale = 1.0, loop = 0, delay = 0, trigger = 0, argName|
		#start, end = UXLine.makeControl(start, end, argName);
		^[ start, end, \exp ].asSpec.map( UEnvGen.getLineKr( 1, timeScale, loop, delay, trigger ) );
	}

	*ar{ |start=1.0, end=2.0, timeScale = 1.0, argName|
		#start, end = UXLine.makeControl(start, end, argName);
		^UEnvGenRel.ar( Env([0,1],[1],\lin), [start, end, \exp], timeScale );
	}

}

ULineRel {

	*kr{ |start=0.0, end=1.0, timeScale = 1.0, loop = 0, delay = 0, trigger = 0, argName|
		#start, end = UXLine.makeControl(start, end, argName);
		^[ start, end, \lin ].asSpec.map( UEnvGenRel.getLineKr( 1, timeScale, loop, delay, trigger ) );
	}

	*ar{ |start=0.0, end=1.0, timeScale = 1.0, argName|
		#start, end = UXLine.makeControl(start, end, argName);
		^UEnvGenRel.ar( Env([0,1],[1],\lin), [start, end, \lin], timeScale );
	}

}

UIEnvGen : UEnvGen {
	
	*kr { |env, spec, index = 0| // index is always 0-1
		if( env.isKindOf( Env ) && { spec.isNil } ) {
			spec = ControlSpec( env.levels.minItem, env.levels.maxItem );
			env.levels = env.levels.normalize;
		};
		env = this.prepareEnv(env, spec);
		spec = spec.asSpec;
		index = index * env[3];
		^spec.map( IEnvGen.kr(env, index) )
	}
	
}