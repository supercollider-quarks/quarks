BeatListenNode : SWTrigBusNode{

	var <>verbose = false;

	myInit{ 
		vals = [1,0,0,0].dup( inbus.numChannels).flatten;
		network.setData( id, vals );

		//		network.add( \beat, id );
		inbus.numChannels.do{ |it| 
			network.add( (\tempo_++inbus.index++'_'++it).asSymbol, [id,0 + (it*4)] );
			network.add( (\quarter_++inbus.index++'_'++it).asSymbol, [id,1 + (it*4)] );
			network.add( (\eighth_++inbus.index++'_'++it).asSymbol, [id,2 + (it*4)] );
			network.add( (\six10th_++inbus.index++'_'++it).asSymbol, [id,3 + (it*4)] );
		};

		responder = OSCresponderNode.new( Server.local.addr, '/tr', { |t,r,msg|
			if ( msg[1] == synth.nodeID,{
				vals[ msg[2] ] = msg[3];
				network.setData( id, vals );
				if ( this.verbose, { msg.postln; } );
			});
		});
	}

	initSynthDefAndBus{ |s,nc=1|
		synthDef = (\swBeatListen++nc).asSymbol;

		SynthDef(synthDef,{ |
			in=0,
			buf1,
			trate=0.2,bv=1.0,hv=1.0,qv=1.0,
			outk=0,dt1=0.4,dt2=0.4,dt3=0.4,outk1=0|

			var trackb,trackh,trackq,tempo;
			var source,bufs;
			var bsound,hsound,qsound;
			source= In.ar( in, nc);
			
			// these are all two channel now
			#trackb,trackh,trackq,tempo=BeatTrack.kr(
				FFT( {LocalBuf.new(1024,1)}.dup(nc), source)).flop;

			nc.do{ |i|
				SendTrig.kr( Impulse.kr( trate ), 0, tempo );
				SendTrig.kr( trackb[i], 1 + (i*4), trackb[i] );
				SendTrig.kr( trackh[i], 2 + (i*4), trackh[i] );
				SendTrig.kr( trackq[i], 3 + (i*4), trackq[i] );
				SendTrig.kr( 1-trackb[i], 1 + (i*4), trackb[i] );
				SendTrig.kr( 1-trackh[i], 2 + (i*4), trackh[i] );
				SendTrig.kr( 1-trackq[i], 3 + (i*4), trackq[i] );
			};
		}).send(s);
	}

}

