// Bus based nodes for the SWDataNetwork

SWBusNode{
	classvar <all;

	var <>synthDef;

	var <id;
	var <network;

	var <bus, <synth, <server;
	var <inbus;
	var <settings;
	var <restartsettings;

	var <watcher;

	var <>dt = 0.05;

	var <stopwatcher = false;

	var <>verbose = false;

	*initClass{
		all = IdentityDictionary.new;
	}

	*new{|id,network,input,serv,autostart=false|
		^super.new.init( id, network, input,serv,autostart );
	}

	*at{ |id|
		^all.at( id );
	}

	initSynthDefAndBus{ |s,nc=1|
		bus = Bus.control( s, nc );
		// overload in subclass
		synthDef = (\swBusNode++nc).asSymbol;
		SynthDef( synthDef, { arg out=0, in=1, lag=0.05, mul=1, gate=1;
			EnvGen.kr( Env.cutoff( 1 ), gate, doneAction: 2 );
			Out.kr( out, Lag.kr( In.kr(in,nc) * mul, lag ));
		}).send(s);
	}

	clean{
		this.stop;
		all.removeAt( id );
	}

	init{ |ky,ntwork,in,serv,autostart=false|
		id = ky;
		network = ntwork;
		server = serv ? Server.default;
		inbus = in;

		settings = IdentityDictionary.new;
		restartsettings = Set.new;

		network.addExpected( id );

		watcher = SkipJack.new( {
			this.getn( { |v| network.setData( id, v ) } ) }, dt, false
			//{ if ( synth.notNil, { synth.isRunning.not },
			//	{ false } ) }
			, "SWBusNode_"++id, autostart: false );


		all.put( id, this );

		this.initSynthDefAndBus( server, inbus.numChannels );

		settings.put( \out, bus );
		settings.put( \in, inbus );
		settings.put( \lag, 0.05 );
		settings.put( \mul, 1 );

		this.myInit;

		if ( autostart ){
			fork{
				server.sync;
				this.start;
			}
		};
	}

	myInit{
		if( network.isKindOf( SWDataNetworkClient ) ){
			//		"adding setter hook".postln;
			network.addHook( id, {
				network.setData( id, Array.fill( bus.numChannels, 0 ) );
			}, \expected );
			network.addHook( id, {
				this.setLabel;
				this.setBus;
			}, \setter );
		}{
			network.setData( id, Array.fill( bus.numChannels, 0 ) );
			this.setLabel;
			this.setBus;
		};
		// override in subclass
	}

	setBus{
		if ( bus.notNil, { this.node.bus_( bus ); } );
	}

	start{
		this.stop;
		synth = Synth.new( synthDef, settings.asKeyValuePairs, addAction: \addToTail );
		stopwatcher = false;
		watcher.start;

		NodeWatcher.register( synth );
	}

	stop{
		synth.free;
		watcher.stop;
	}

	release{ |releasetime|
		synth.release( releasetime );
		stopwatcher = true;
		Task({releasetime.wait; if ( this.stopwatcher, { watcher.stop});}).play;
	}

	inbus_{ |ibus|
		if ( ibus.rate != inbus.rate ){
			"new inbus rate not the same as the old inbus rate".error;
		};
		inbus = ibus;
		settings.put( \in, inbus );
		if ( synth.notNil, {
			synth.set( \in, inbus );
		});
	}

	set{ arg ... args;
		args.clump( 2 ).do{ |it|
			settings.put( it[0], it[1] );
			if ( synth.notNil,
				{
					if ( restartsettings.includes( it[0] ) ){
						this.stop;
						this.start;
					}{
						synth.set( it[0], it[1] );
					}
				} );
		};
	}

	get{ |func| // get the value of the amplitude; you need to give a function as input to assign the value to your variable, as getting a value from a bus is asynchronous.
		bus.get( func );
	}

	getn{ |func| // get the value of the amplitude; you need to give a function as input to assign the value to your variable, as getting a value from a bus is asynchronous.
		bus.getn( bus.numChannels, func );
	}

	node{
		^network.nodes[id];
	}

	setLabel{
		if ( this.node.key == nil, { network.add(  (synthDef ++ '_' ++ inbus.index ).asSymbol, id ) } );
	}

}

AmpTrackNode : SWBusNode{
	initSynthDefAndBus{ |s,nc=1|
		bus = Bus.control( s, nc );

		settings.put( \atime, 0.01 );
		settings.put( \rtime, 0.01 );

		if ( inbus.rate == \audio ){
			synthDef = (\AmptrackNode_ar_++nc).asSymbol;
			SynthDef( synthDef, { arg out=0, in=1, lag=0.05, mul=1, gate=1, atime=0.01, rtime = 0.01;
				EnvGen.kr( Env.cutoff( 1 ), gate, doneAction: 2 );
				Out.kr( out, Lag.kr( Amplitude.kr(In.ar(in, nc ) * mul, atime, rtime), lag) );
			}).send(s);
		}{ // control rate:
			synthDef = (\AmptrackNode_kr_++nc).asSymbol;
			SynthDef( synthDef, { arg out=0, in=1, lag=0.05, mul=1, gate=1, atime=0.01, rtime = 0.01;
				EnvGen.kr( Env.cutoff( 1 ), gate, doneAction: 2 );
				Out.kr( out, Lag.kr( Amplitude.kr(In.kr(in, nc ) * mul, atime, rtime), lag) );
			}).send(s);
		}
	}

}

PitchTrackNode : SWBusNode{
	initSynthDefAndBus{ |s,nc=1|
		bus = Bus.control( s, nc );

		settings.put( \lpfreq, 500 );
		settings.put( \hpfreq, 100 );
		settings.put( \initFreq, 300 );
		settings.put( \minFreq, 100 );
		settings.put( \maxFreq, 500 );
		settings.put( \ampThreshold, 0.01 );
		settings.put( \offset, 0.5 );

		if ( inbus.rate == \audio ){
			synthDef = (\PitchtrackNode_ar_++nc).asSymbol;

			SynthDef( synthDef,{
				|out=0,in=1,lag=0.5, mul=1, gate=1,
				lpfreq=500,hpfreq=100, initFreq=300,
				minFreq=100, maxFreq=500, ampThreshold=0.01|

				var input, freq, hasFreq;
				var spec,output;
				//			spec = [minFreq,maxFreq,\exponential].asSpec;
				input = In.ar(in, nc ) * mul;
				3.do{
					input = LPF.ar( HPF.ar( input, hpfreq ), lpfreq );
				};
				# freq, hasFreq = Pitch.kr(
					input, initFreq, minFreq, maxFreq, median: 7, ampThreshold: ampThreshold ).flop;

				EnvGen.kr( Env.cutoff( 1 ), gate, doneAction: 2 );

				output = Gate.kr( freq, hasFreq).lag(lag);
				//			spec.unmap( output ).poll;
				Out.kr( out,output  );
			}).send(s);
		}{
			synthDef = (\PitchtrackNode_kr_++nc).asSymbol;

			SynthDef( synthDef,{
				|out=0,in=1,lag=0.5, mul=1, gate=1,
				lpfreq=2000,hpfreq=400, initFreq=440,
				minFreq=60, maxFreq=4000, ampThreshold=0.01,offset=0.5|

				var input, freq, hasFreq;
				var spec,output;
				//			spec = [minFreq,maxFreq,\exponential].asSpec;
				input = In.kr(in, nc ) - DC.kr( offset );
				/*
				3.do{
					input = LPF.ar( HPF.ar( input, hpfreq ), lpfreq );
				};
				*/
				# freq, hasFreq = Pitch.kr(
					input, initFreq, minFreq, maxFreq, median: 7, ampThreshold: ampThreshold ).flop;

				EnvGen.kr( Env.cutoff( 1 ), gate, doneAction: 2 );

				output = Gate.kr( freq, hasFreq);
				//			spec.unmap( output ).poll;
				Out.kr( out,output  );
			}).send(s);
		};
	}

}

SumBusesNode : SWBusNode{
	initSynthDefAndBus{ |s,nc=1|
		bus = Bus.control( s, 1 );

		synthDef = (\SumbusesNode_++nc).asSymbol;
		SynthDef( synthDef, { arg out=0, in=1, lag=0.05, mul=1, gate=1;
			EnvGen.kr( Env.cutoff( 1 ), gate, doneAction: 2 );
			Out.kr( out, Mix.new( Lag.kr( In.kr(in, nc ) * mul, lag ) ) );
		}).send(s);
	}
}

LeakyNode : SWBusNode{
	initSynthDefAndBus{ |s,nc=1|
		bus = Bus.control( s, nc );

		settings.put( \posSlope1, 1.0 );
		settings.put( \posSlope2, -1.0 );
		settings.put( \negSlope, 0.015 );
		settings.put( \minValue, 0.0 );

		synthDef = (\LeakyNode++nc).asSymbol;
		SynthDef( synthDef, { arg out=0, in=1, lag=0.05, mul=1, gate=1, posSlope1=1,posSlope2,negSlope=0.015, minValue=0.0;
			EnvGen.kr( Env.cutoff( 1 ), gate, doneAction: 2 );

			Out.kr( out, mul * Lag.kr(
				FOS.kr( In.kr( in, nc ), posSlope1, posSlope2, -1*negSlope ).max( minValue );
				//	Integrator.kr( In.kr( in, nc ), coef ) * (1-coef)
				, lag) );
		}).send(s);
	}
}

TimerNode : SWBusNode{
	initSynthDefAndBus{ |s,nc=1|
		bus = Bus.control( s, nc );

		synthDef = (\TimerNode++nc).asSymbol;
		SynthDef( synthDef, { arg out=0, in=1, lag=0.05, mul=1, gate=1, posSlope1=1,posSlope2,negSlope=0.015, minValue=0.0;
			EnvGen.kr( Env.cutoff( 1 ), gate, doneAction: 2 );

			Out.kr( out, Lag.kr(
				Timer.kr( In.kr( in, nc )*mul );
				, lag) );
		}).send(s);
	}
}

FrictionNode : SWBusNode{
	initSynthDefAndBus{ |s,nc=1|
		bus = Bus.control( s, nc );

		settings.put( \friction, 0.5 );
		settings.put( \spring, 0.414 );
		settings.put( \damp, 0.313 );
		settings.put( \mass, 0.1 );
		settings.put( \beltmass, 1 );

		synthDef = (\FrictionNode++nc).asSymbol;
		SynthDef( synthDef, { arg out=0, in=1, lag=0.05, mul=1, gate=1, friction=0.5, spring=0.414, damp=0.313, mass =0.1, beltmass = 1;
			EnvGen.kr( Env.cutoff( 1 ), gate, doneAction: 2 );
			Out.kr( out, Lag.kr( Friction.kr( In.kr(in, nc ) * mul, friction, spring, damp, mass, beltmass), lag) );
		}).send(s);
	}
}

/*
BallNode : SWBusNode{
	initSynthDefAndBus{ |s,nc=1|
		bus = Bus.control( s, nc );

		settings.put( \gravity, 0.5 );
		settings.put( \spring, 0.414 );
		settings.put( \damp, 0.313 );
		settings.put( \mass, 0.1 );
		settings.put( \beltmass, 1 );

		synthDef = (\FrictionNode++nc).asSymbol;
		SynthDef( synthDef, { arg out=0, in=1, lag=0.05, mul=1, gate=1, friction=0.5, spring=0.414, damp=0.313, mass =0.1, beltmass = 1|;
			EnvGen.kr( Env.cutoff( 1 ), gate, doneAction: 2 );
			Out.kr( out, Lag.kr( Ball.kr( In.kr(in, nc ) * mul, friction, spring, damp, mass, beltmass), lag) );
		}).send(s);
	}
}
*/

MedianNode : SWBusNode{
	initSynthDefAndBus{ |s,nc=1|
		bus = Bus.control( s, nc );

		settings.put( \length, 21 );

		synthDef = (\MedianNode++nc).asSymbol;
		SynthDef( synthDef, { arg out=0, in=1, lag=0.05, mul=1, gate=1, length=50;
			EnvGen.kr( Env.cutoff( 1 ), gate, doneAction: 2 );
			Out.kr( out, Lag.kr( Median.kr(length, In.kr(in, nc ) * mul), lag) );
		}).send(s);
	}
}

MeanStdDevNode : SWBusNode{
	initSynthDefAndBus{ |s,nc=1|
		bus = Bus.control( s, nc*2 );

		settings.put( \length, 50 );
		restartsettings.add( \length );

		synthDef = (\MeanStddevNode++nc).asSymbol;
		SynthDef( synthDef, { arg out=0, in=1, lag=0.05, mul=1, gate=1, length=50;
			var mean, stddev, input;
			EnvGen.kr( Env.cutoff( 1 ), gate, doneAction: 2 );
			input = In.kr(in, nc ) * mul;
			mean = RunningSum.kr( input, length )/length;
			stddev = StdDevUGen.kr( input, length, mean);
			Out.kr( out, Lag.kr( mean ++ stddev , lag) );
		}).send(s);
	}
}

MeanNode : SWBusNode{
	initSynthDefAndBus{ |s,nc=1|
		bus = Bus.control( s, nc );

		settings.put( \length, 50 );
		restartsettings.add( \length );

		synthDef = (\MeanNode++nc).asSymbol;
		SynthDef( synthDef, { arg out=0, in=1, lag=0.05, mul=1, gate=1, length=50;
			var mean, input;
			EnvGen.kr( Env.cutoff( 1 ), gate, doneAction: 2 );
			input = In.kr(in, nc ) * mul;
			mean = RunningSum.kr( input, length )/length;
			//	stddev = StdDevUGen.kr( input, length, mean);
			Out.kr( out, Lag.kr( mean, lag) );
		}).send(s);
	}
}


StdDevNode : SWBusNode{
	initSynthDefAndBus{ |s,nc=1|
		bus = Bus.control( s, nc );

		settings.put( \length, 50 );
		restartsettings.add( \length );

		synthDef = (\StddevNode++nc).asSymbol;
		SynthDef( synthDef, { arg out=0, in=1, lag=0.05, mul=1, gate=1, length=50;
			EnvGen.kr( Env.cutoff( 1 ), gate, doneAction: 2 );
			Out.kr( out, Lag.kr( StdDevUGen.kr(In.kr(in, nc ) * mul, length), lag) );
		}).send(s);
	}
}

SumStdDevNode : SWBusNode{
	initSynthDefAndBus{ |s,nc=1|
		bus = Bus.control( s, 1 );

		synthDef = (\SumStddevNode++nc).asSymbol;

		settings.put( \length, 50 );
		restartsettings.add( \length );

		SynthDef( synthDef, { arg out=0, in=1, lag=0.05, mul=1, gate=1, length=50;
			EnvGen.kr( Env.cutoff( 1 ), gate, doneAction: 2 );
			Out.kr( out, Mix.new( Lag.kr( StdDevUGen.kr(In.kr(in, nc ) * mul, length), lag) ) );
		}).send(s);
	}
}



ASRNode : SWBusNode{

	initSynthDefAndBus{ |s,nc=1|
		bus = Bus.control( s, nc );
		// overload in subclass
		synthDef = (\ASRNode++nc).asSymbol;
		SynthDef( synthDef, { arg out=0, in=1, lag=1, mul=1, gate=1, attack=0.5, release = 3.0, curve = 0;
			var input;
			EnvGen.kr( Env.cutoff( 1 ), gate, doneAction: 2 );
			input = In.kr( in,nc );
			Out.kr( out,
				EnvGen.kr(
					Env.asr( attack,1,release,curve ),
					input // gate
				) * mul);
		}).send(s);
	}

}


ASRMulNode : SWBusNode{

	initSynthDefAndBus{ |s,nc=1|
		bus = Bus.control( s, nc );
		// overload in subclass
		synthDef = (\ASRMulNode++nc).asSymbol;
		SynthDef( synthDef, { arg out=0, in=1, lag=1, mul=1, gate=1, attack=0.5, release = 3.0, curve = 0;
			var input, maxval;
			EnvGen.kr( Env.cutoff( 1 ), gate, doneAction: 2 );
			input = In.kr( in,nc );
			maxval = RunningMax.kr( input, input ); // assumes input is going from 0 to a value, like a trigger (use with a previous InRange)
			Out.kr( out,
				EnvGen.kr(
					Env.asr( attack,maxval,release,curve ),
					input // gate
				) * mul);
		}).send(s);
	}

}


InRangeNode : SWBusNode{

	initSynthDefAndBus{ |s,nc=1|
		bus = Bus.control( s, nc );
		// overload in subclass

		settings.put(\low, 0.0);
		settings.put(\hi, 1.0);

		synthDef = (\InRangeNode++nc).asSymbol;
		SynthDef( synthDef, { arg out=0, in=1, lag=1, mul=1, gate=1, low=0.1, hi=1;
			EnvGen.kr( Env.cutoff( 1 ), gate, doneAction: 2 );
			Out.kr( out,
				InRange.kr( In.kr( in,nc ), low, hi )
				* mul);
		}).send(s);
	}

}

InRangeGateNode : SWBusNode{

	initSynthDefAndBus{ |s,nc=1|
		bus = Bus.control( s, nc );

		settings.put(\low, 0.0);
		settings.put(\hi, 1.0);

		// overload in subclass
		synthDef = (\InRangeGateNode++nc).asSymbol;
		SynthDef( synthDef, { arg out=0, in=1, lag=1, mul=1, gate=1, low=0.1, hi=1;
			var input = In.kr( in,nc );
			EnvGen.kr( Env.cutoff( 1 ), gate, doneAction: 2 );
			Out.kr( out,
				Gate.kr( input, InRange.kr(input, low, hi ) )
				* mul);
		}).send(s);
	}

}


SchmidtNode : SWBusNode{

	initSynthDefAndBus{ |s,nc=1|
		bus = Bus.control( s, nc );
		// overload in subclass

		settings.put(\low, 0.0);
		settings.put(\hi, 1.0);

		synthDef = (\SchmidtNode++nc).asSymbol;
		SynthDef( synthDef, { arg out=0, in=1, lag=1, mul=1, gate=1, low=0.1, hi=0.5;
			EnvGen.kr( Env.cutoff( 1 ), gate, doneAction: 2 );
			Out.kr( out,
				Schmidt.kr( In.kr( in,nc ), low, hi )
				* mul);
		}).send(s);
	}

}

SchmidtGateNode : SWBusNode{

	initSynthDefAndBus{ |s,nc=1|
		bus = Bus.control( s, nc );
		// overload in subclass

		settings.put(\low, 0.0);
		settings.put(\hi, 1.0);

		synthDef = (\SchmidtGateNode++nc).asSymbol;
		SynthDef( synthDef, { arg out=0, in=1, lag=1, mul=1, gate=1, low=0.1, hi=0.5;
			var input = In.kr( in, nc );
			EnvGen.kr( Env.cutoff( 1 ), gate, doneAction: 2 );
			Out.kr( out,
				Gate.kr( input, Schmidt.kr( input, low, hi ) )
				* mul);
		}).send(s);
	}

}

ToggleFFNode : SWBusNode{

	initSynthDefAndBus{ |s,nc=1|
		bus = Bus.control( s, nc );
		// overload in subclass
		synthDef = (\ToggleFFNode++nc).asSymbol;
		SynthDef( synthDef, { arg out=0, in=1, lag=1, mul=1, gate=1;
			var input;
			EnvGen.kr( Env.cutoff( 1 ), gate, doneAction: 2 );
			input = In.kr( in,nc );
			Out.kr( out,
				ToggleFF.kr( input )
				* mul);
		}).send(s);
	}

}

ToggleFFGateNode : SWBusNode{

	initSynthDefAndBus{ |s,nc=1|
		bus = Bus.control( s, nc );
		// overload in subclass
		synthDef = (\ToggleFFGateNode++nc).asSymbol;
		SynthDef( synthDef, { arg out=0, in=1, lag=1, mul=1, gate=1;
			var input;
			EnvGen.kr( Env.cutoff( 1 ), gate, doneAction: 2 );
			input = In.kr( in,nc );
			Out.kr( out,
				Gate.kr( input, ToggleFF.kr( input ) )
				* mul);
		}).send(s);
	}

}


LagUDNode : SWBusNode{

	initSynthDefAndBus{ |s,nc=1|
		bus = Bus.control( s, nc );
		// overload in subclass

		settings.put( \lagup, 0.1 );
		settings.put( \lagdown, 0.1 );

		synthDef = (\LagUDNode++nc).asSymbol;
		SynthDef( synthDef, { arg out=0, in=1, lag=1, mul=1, gate=1, lagup = 0.1, lagdown=0.1;
			var input;
			EnvGen.kr( Env.cutoff( 1 ), gate, doneAction: 2 );
			input = In.kr( in,nc );
			Out.kr( out,
				LagUD.kr( input, lagup, lagdown )
				* mul);
		}).send(s);
	}

}

SlopeNode : SWBusNode{

	initSynthDefAndBus{ |s,nc=1|
		bus = Bus.control( s, nc );
		// overload in subclass
		synthDef = (\SlopeNode++nc).asSymbol;
		SynthDef( synthDef, { arg out=0, in=1, lag=1, mul=1, gate=1;
			var input;
			EnvGen.kr( Env.cutoff( 1 ), gate, doneAction: 2 );
			input = In.kr( in,nc );
			Out.kr( out,
				Slope.kr( input )
				* mul);
		}).send(s);
	}

}

SlewNode : SWBusNode{

	initSynthDefAndBus{ |s,nc=1|
		bus = Bus.control( s, nc );

		settings.put( \upSlope, 1);
		settings.put( \downSlope, 1);

		// overload in subclass
		synthDef = (\SlewNode++nc).asSymbol;
		SynthDef( synthDef, { arg out=0, in=1, lag=1, mul=1, gate=1, upSlope, downSlope;
			var input;
			EnvGen.kr( Env.cutoff( 1 ), gate, doneAction: 2 );
			input = In.kr( in,nc );
			Out.kr( out,
				Slew.kr( input, upSlope=1, downSlope=1 )
				* mul);
		}).send(s);
	}

}



/// quite specific:

GridVectorNode : SWBusNode{
	initSynthDefAndBus{ |s,nc=2|
		bus = Bus.control( s, 2 );

		synthDef = (\GridVectorNode++nc).asSymbol;

		SynthDef( synthDef, { arg out=0, in=1, lag=0.05, mul=1, gate=1;
			var vectors,input,res;

			EnvGen.kr( Env.cutoff( 1 ), gate, doneAction: 2 );

			vectors = [-1,0,1].collect{ |it| [1,0,-1].collect{ |jt| [it,jt] } }.flatten;
			input = In.kr( in, 9 ); // 9 channels of directional input, these are like triggers, so we have to set a minimum amount of movement for them to settle down to still movement again, which is like triggering an envelope.
			res = (vectors * input).sum;
			Out.kr( out, res );
		}).send(s);
	}
}
