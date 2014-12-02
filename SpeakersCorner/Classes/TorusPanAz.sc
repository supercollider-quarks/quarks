TorusPanAz { 
	*ar { arg numSpeakers = #[ 16, 12, 8, 4 ], 
		in, hPos = 0.0, vPos = 0.0, level = 1.0, 
		hWidth = 2, vWidth = 2, 
		hOrients=0, vOrient=0;

		 ^PanAz.ar(numSpeakers.size, in, vPos, level, vWidth, vOrient)
		 	.collect({ arg vChan, i; var numSpk; 
		 		numSpk = numSpeakers[i];
		 		if (numSpk == 1, { 
		 			[vChan] 
		 		}, {
					PanAz.ar(numSpk, 
						vChan, 
						hPos, 
						width: hWidth.min(numSpk),
						orientation: hOrients.asArray.clipAt(i)
					)
				});
		}).flat;
	}
}