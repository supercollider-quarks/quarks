SensorSonificator{
	var <inbus,<>outbus;
	var <group;

	var <server;
	var <synths;

	*new{ |input,outbus,server|
		^super.new.init(input,outbus,server);
	}

	init{ |in,out,s|
		server = s ? Server.local;
		// send synthdefs
		SensorSonificatorSynth.loadSynthDefs( server );
		inbus = in;
		outbus = out ? Bus.audio( s, 1 );
		group = Group.new( s );
		synths = [];
	}

	addSonification{ |type|
		synths = synths.add(
			SensorSonificatorSynth.new( (type++\Sonify).asSymbol, group, inbus, outbus );
		);
	}

	start{
		synths.do{ |it| it.play( inbus, outbus ); };
	}

	stop{
		synths.do{ |it| it.stop; };
	}

	query{ |args=true|
		group.dumpTree( args );
	}
}

SensorSonificatorSynth{
	classvar <synthDescLib;
	var <synth;
	var <group;
	var <synthDef;
	var <outbus, <inbus;
	var <argValues;

	*initClass{
		SynthDescLib.initClass;
		synthDescLib = SynthDescLib.new( \SensorSonification );
	}

	*loadSynthDefs{ |s|
		synthDescLib.servers = s;

		// as values get put on the bus, they produce a short toc. The longer the time in between, the higher the pitch
		SynthDef( \intrigSonify, { |inbus=0, outbus=0, amp=0.005, freq=200, dur=0.3, fmmod=100|
			var trig,time;
			trig = InTrig.kr( inbus, 1 );
			time = Timer.kr( trig );
			Out.ar( outbus,
				EnvGen.kr( Env.perc, trig, levelScale: amp, timeScale: dur )*
				SinOsc.ar( min(freq + (fmmod*time),20000) )
			);
		}).add( \SensorSonification );

		// Sonify the value as frequency. As the rate of change is faster, the amplitude goes up (silent when no change, fade out is longer than fade in).
		SynthDef( \valueSonify, { |inbus=0,outbus=0,bfreq=300,freqr=100,amp=0.001,slmul=0.1,lagt=0.3,lagdt=1,rq=0.1|
			var input,slamp,sig,freq;
			input = In.kr( inbus, 1 );
			slamp = Slope.kr( input.lag2(lagt), slmul, amp );
			freq = bfreq + (input.lag2(lagt)*freqr);
			sig = Mix.new( SinOsc.ar( freq*[1,1.25,1.5,2], 0, 1/(1..4) ) );
			sig = BPF.ar( sig, bfreq, rq );
			Out.ar( outbus, sig*slamp.lag2(lagt,lagdt) * AmpCompA.kr( freq ) );
		}).add( \SensorSonification );

		SynthDef( \inrangeSonify, { |inbus=0, outbus=0, amp=0.5, freq=200, dur=0.3, lo=0.7,hi=1|
			var trig,time;
			trig = InRange.kr( In.kr(inbus, 1 ), lo, hi );
			Out.ar( outbus,
				EnvGen.kr( Env.adsr( releaseTime: dur ), trig, levelScale: amp )*
				SinOsc.ar( min(freq,20000) ) * trig;
			);
		}).add( \SensorSonification );
	}


	*new{ |def,group,inbus,outbus|
		^super.new.init( def, group, inbus,outbus );
	}

	init{ |def, gr, in, out|
		synthDef = def ? \valueSonify;
		group = gr ? Server.local;
		outbus = out ? 0;
		inbus = in ? 0;
		argValues = IdentityDictionary.new;
	}

	play{ |in,out|
		outbus = out ? outbus;
		inbus = in ? inbus;
		if ( synth.isNil, {
			synth = Synth.new( synthDef, [\inbus, inbus, \outbus, outbus]++argValues.asKeyValuePairs, group, \addToTail );
			NodeWatcher.register( synth );
		},{
			if ( synth.isPlaying.not,
				{
					synth = Synth.new( synthDef, [\inbus, inbus, \outbus, outbus]++argValues.asKeyValuePairs, group, \addToTail );
					NodeWatcher.register( synth );
				})
		});
	}

	stop{
		if ( synth.notNil, { synth.free; } );
	}

	controlNames{
		^SensorSonificatorSynth.synthDescLib.at( synthDef ).controlNames;
	}

	controls{
		^SensorSonificatorSynth.synthDescLib.at( synthDef ).controls;
	}

	inputs{
		^SensorSonificatorSynth.synthDescLib.at( synthDef ).inputs;
	}

	outputs{
		^SensorSonificatorSynth.synthDescLib.at( synthDef ).outputs;
	}

	set{ |key,val|
		argValues.put( key, val );
		if ( synth.notNil, { synth.set( key, val ); } );
	}

	get{ |key,func|
		if ( synth.notNil, { synth.get( key, func ); },
			{ func.value( argValues.at( key) ); });
	}

}