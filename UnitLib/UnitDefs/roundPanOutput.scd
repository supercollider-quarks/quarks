/*
\roundPanOutput

Sends sounds from the previous units in the UChain to hardware output busses, using a panning algorithm for clockwise circular speaker setups.

bus: the channels are sent out starting with this bus.
point: the position of the source (0@0: center, positive x is left, positive y is front)
dbRollOff: number of dB's amplitude rolloff per distance doubling
speakerRadius: the distance of the virtual microphones from the center of the room, in meters. When 0, no inter-speaker delays are applied (only a distance-dependant doppler delay). Default value is 0.19, which is okay for normal speaker setups. Optimum value may differ per setup.
orientation: the speaker index of the front. 0.5 (default) means that the front position is in between the first and second speaker (normal for stereo and quad setups). This value can also be modulated to rotate the whole spatial image.
numChannels: number of speakers (*)

	
(*) only the following numbers of channels are allowed:
2,3,4,5,6,7,8,10,12,16,24,32

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

MultiChannelUdef( \roundPanOutput, { |bus = 0, point = #[0,0], dbRollOff = (-6), speakerRadius = 0.19|
	var sig, panned, pankr, polar, angle, amp;
	var mics, numChans, orientation;
	
	sig = UIn.ar( 0 );
	
	numChans = Udef.numChannels;
	
	orientation = \orientation.ukr( 0.5, 0, numChans );
	
	mics = ({ |i| 
		Polar( speakerRadius, (i - orientation).linlin( 0, numChans, 
			0.5pi, -1.5pi, \none ) ).asPoint
	} ! numChans );
	
	point = point.asPoint;
	panned = VirtualMics.ar( mics, sig, point, dbRollOff );
	polar = point.asPolar;
	angle = polar.angle.linlin(-0.5pi,1.5pi,1,-1,\none);
	amp = (0.15 / polar.rho).linlin(0.15,1,0,1,\minmax);
	
	pankr = PanAz.kr( numChans, 1, angle, orientation: orientation ).max(amp);

	Out.ar( bus, UGlobalEQ.ar( panned ) * pankr * UEnv.kr  );
}, channels: [2,3,4,5,6,7,8,10,12,16,24,32] )
	.category_( \io )
	.setSpec( \bus, PositiveIntegerSpec( 0 ) )
	.setSpec( \point, PointSpec( 200, step: 0.1 ) )
	.setSpec( \dbRollOff,  [-24, 0, \lin, 1, -6 ].asSpec )
	.setSpec( \speakerRadius, [0,1,\lin,0,0.19].asSpec );