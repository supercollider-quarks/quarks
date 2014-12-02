SWTrigBusNode : SWBusNode{

	var <responder;
	var <vals;

	initSynthDefAndBus{ |s,nc=1|
		bus = Bus.control( s, nc*3 );

		// overload in subclass
		synthDef = (\swTrigBusNode++nc).asSymbol;
		SynthDef( synthDef, { arg out=0, in=1, lag=1, mul=1, gate=1, threshold=0.5;
			var trig,trig2,input;
			EnvGen.kr( Env.cutoff( 1 ), gate, doneAction: 2 );
			input = In.kr( in, nc );
			trig = input - threshold;
			trig2 = threshold - input; // reverse trigger
			SendTrig.kr( trig, Array.series( nc, 0, 1 ), trig );
			SendTrig.kr( trig2, Array.series( nc, nc, 1 ), trig2.abs );
			Out.kr( out, [trig, trig2].flatten * mul);
		}).send(s);
	}

	myInit{
		settings.put( \threshold, 0.5 );

		vals = Array.fill( inbus.numChannels * 3, 0 );

		network.setData( id, vals ); 

		responder = OSCresponderNode.new( server.addr, '/tr', 
			{ |t,r,msg| 
				if ( verbose, { msg.postln; } );
				if ( msg[1] == synth.nodeID,{
					vals[ msg[2] ] = msg[3]; // strength of trigger
					if ( msg[2] < inbus.numChannels, { // on
						vals[ msg[2] + (2*inbus.numChannels) ] = 1;
					},{ // off
						vals[ msg[2] + inbus.numChannels ] = 0;
					});
					network.setData( id, vals );
				});
			} );
	}

	start{
		synth = Synth.new( synthDef, settings.asKeyValuePairs, addAction: \addToTail );
		responder.add;

		NodeWatcher.register( synth );
	}
	
	stop{
		synth.free;
		responder.remove;
	}
}

TransitNode : SWTrigBusNode{
	initSynthDefAndBus{ |s,nc=1|
		//	bus = Bus.control( s, nc+7 );

		// overload in subclass
		synthDef = (\swTransitNode++nc).asSymbol;

		settings.put(\time, 3.0);

		SynthDef( \swTransitNode3, { arg out=0, in=1, lag=1, mul=1, gate=1, time=3.0, in2=(2..4);
			var env,igate,trans;
			//			var trig,trig2,input;
			EnvGen.kr( Env.cutoff( 1 ), gate, doneAction: 2 );

			igate = In.kr( in, 1 );

			env = EnvGen.kr( 
				Env.new( [1,1,1,0], [0.1,time,0.05], ['step','step','linear'], 1 ),
		//		Env.new( [1,1,0], [0.1,time], 'step', 1 ),
				igate );

			trans = In.kr( in2, 1 );
			SendTrig.kr( [igate,1-igate], [0,3] , [igate,igate] );
			SendTrig.kr( [trans*env,1-(trans*env)].flatten, [ (1..3) , (1..3)+3 ].flatten, trans*env );

			//	Out.kr( out, [igate,trans*env] * mul);
		}).send(s);

		SynthDef( \swTransitNode5, { arg out=0, in=1, lag=1, mul=1, gate=1, time=3.0, in2=(2..6);
			var env,igate,trans;
			//			var trig,trig2,input;
			EnvGen.kr( Env.cutoff( 1 ), gate, doneAction: 2 );

			igate = In.kr( in, 1 );

			env = EnvGen.kr( 
				Env.new( [1,1,1,0], [0.1,time,0.05], ['step','step','linear'], 1 ),
		//		Env.new( [1,1,0], [0.1,time], 'step', 1 ),
				igate );

			trans = In.kr( in2, 1 );
			SendTrig.kr( [igate,1-igate], [0,5] , [igate,igate] );
			SendTrig.kr( [trans*env,1-(trans*env)].flatten, [ (1..5) , (1..5)+5 ].flatten, trans*env );

			//	Out.kr( out, [igate,trans*env] * mul);
		}).send(s);

		SynthDef( \swTransitNode8, { arg out=0, in=1, lag=1, mul=1, gate=1, time=3.0, in2=(2..9);
			var env,igate,trans;
			//			var trig,trig2,input;
			EnvGen.kr( Env.cutoff( 1 ), gate, doneAction: 2 );

			igate = In.kr( in, 1 );

			env = EnvGen.kr( 
				Env.new( [1,1,1,0], [0.1,time,0.05], ['step','step','linear'], 1 ),
		//		Env.new( [1,1,0], [0.1,time], 'step', 1 ),
				igate );

			trans = In.kr( in2, 1 );
			SendTrig.kr( [igate,1-igate], [0,9] , [igate,igate] );
			SendTrig.kr( [trans*env,1-(trans*env)].flatten, [ (1..8) , (1..8)+9 ].flatten, trans*env );

			//	Out.kr( out, [igate,trans*env] * mul);
		}).send(s);
	}

	myInit{
		vals = Array.fill( bus.numChannels, 0 );
		network.setData( id, vals ); 
		responder = OSCresponderNode.new( server.addr, '/tr', 
			{ |t,r,msg| 
				if ( verbose, { msg.postln; } );
				if ( msg[1] == synth.nodeID,{
					vals[ msg[2]%2 ] = msg[3]; // strength of trigger		
					network.setData( id, vals );
				});
			} );
	}
}

MouseGridNode : SWTrigBusNode{

	initSynthDefAndBus{ |s,nc=24|
		bus = Bus.control( s, nc*2 );

		// overload in subclass
		synthDef = (\swMouseGridNode++nc).asSymbol;
		SynthDef( synthDef, { arg out=0, in=1, lag=1, mul=1, gate=1, rate=1;
			var x,y,trigxy,itrig,impuls;
			EnvGen.kr( Env.cutoff( 1 ), gate, doneAction: 2 );

			impuls = Impulse.kr( rate ); // regular update interval
			x = MouseX.kr( 0, 5 );
			y = MouseY.kr( 0, 3 );
			trigxy = (2.55..-0.45).collect{ |yi|
				(-0.45..4.55).collect{ |xi|
					InRect.kr( x, y, Rect( xi, yi, 0.9, 0.9 ) )
				}
			}.flatten;
			itrig = 1 - trigxy;

			SendTrig.kr( impuls + trigxy, (0..23), trigxy );
			SendTrig.kr( itrig, (24..47), trigxy );
			
			Out.kr( out, trigxy ++ itrig; );
		}).send(s);
	}

	myInit{
		vals = Array.fill( 24, 0 );
		network.setData( id, vals ); 
		responder = OSCresponderNode.new( server.addr, '/tr', 
			{ |t,r,msg|
				if ( verbose, { msg.postln; } );
				if ( msg[1] == synth.nodeID,{
					vals[ msg[2]%24 ] = msg[3]; // strength of trigger
					network.setData( id, vals );
				});
			} );
	}

}

