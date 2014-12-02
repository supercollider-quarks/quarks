// wslib
// simple Pulse train as found in many analog studios

PulseTrain {
	
	*ar { |in, n = 4, t = 0.25, lfirst = 0, lrest = 1 |
		// n pulses with t timespacing added to incoming pulse
		// al args can be control rate
		^TDuty.ar( Dseq([t], n), in, Dseq( [lfirst, Dseq([lrest], inf) ], 1));
		}
		
	*arD { |in, n = 3, t = 0.25, lfirst = 0, lrest = 1, maxN = 3, maxDel = 1.0 |
		// Delay version
		// more cpu but all args can be audio rate
		// tried with TDelay, but that was unstable
		// input can be anything now..
		var delay;
		delay = DelayN.ar( in, maxDel, Array.fill(maxN, { |i| (i+1) * t }) )
			* (if(n.rate == 'scalar')
				 { Array.fill(maxN, { |i| ( i < n ).binaryValue }) }
				 { Array.fill(maxN, { |i| n > i }) } );
		^(in * lfirst) + (delay.sum * lrest);
		}
		
	*kr { |in, n = 4, t = 0.25, lfirst = 0, lrest = 1 |
		^TDuty.kr( Dseq([t], n), in, Dseq( [lfirst, Dseq([lrest], inf) ], 1));
		}
	}