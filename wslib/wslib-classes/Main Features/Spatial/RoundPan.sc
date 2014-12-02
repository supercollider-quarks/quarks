RoundPan {
	*ar { |numChans = 2, in, pos = 0.0, level = 1, distance = 1, orientation = 0.5, 
			speakerRadius = 0.19, dbFactor = 0, panAzAmt = 1|
		var mics, sourcePos, panAz, virtMic;
		
		mics = ({ |i| 
			Polar( speakerRadius, (i - 0.5).linlin( 0, numChans, 0, 2pi, \none ) ).asPoint
		} ! numChans );
		
		sourcePos = Polar( distance * speakerRadius, pos * pi ).asPoint;
		
		panAz = PanAz.kr( numChans, 1, pos );
		panAz = LinXFade2.kr(  1.dup(numChans), panAz, panAzAmt.linlin(0,1,-1,1) );
		virtMic = VirtualMics.ar( mics, in, sourcePos, dbFactor );
		^virtMic * panAz * level;
		
	}
}


VirtualMics {
	*ar { |micPos = (0@0), in, pos = (0@0), dbFactor = (-6), dbLimit = 0|
		
		var distances, delays, amps, limit;
		distances = micPos.asCollection.collect(_.dist(pos));
		delays = distances / Number.speedOfSound;
		limit = dbLimit.dbamp;
		amps = ( limit.dbamp / distances.pow( dbFactor / -6 ) ).min( limit );
		^DelayC.ar( in, 32768 / SampleRate.ir, delays ) * amps;
	}
}