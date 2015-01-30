/*
\stereoOutput

Sends sounds from the previous units in the UChain to hardware output busses, using a panning algorithm for stereo panning

bus: the channels are sent out starting with this bus.
point: the position of the source (0@0: center, positive x is left, positive y is front - although the difference between front and back is not audible in this algorithm)
dbRollOff: number of dB's amplitude rolloff per distance doubling
speakerRadius: the distance of the virtual microphones from the center of the room (i.e. half the distance between the speakers), in meters. When 0, no inter-speaker delays are applied (only a distance-dependant doppler delay). Default value is 0.3, which is optimal for normal speaker setups. For headphone, use 0.1.
lag: a lag time applied on all parameters

	
(*) only the following numbers of channels are allowed:
2,3,4,5,6,7,8,10,12,16,24,32

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \stereoOutput, { |bus = 0, point = #[0,0], dbRollOff = (-6), speakerRadius = 0.3|
	var sig, panned, pankr, polar, angle, amp;
	
	sig = UIn.ar( 0 );
	speakerRadius = speakerRadius;
	point = point.asPoint;
	panned = VirtualMics.ar( [ speakerRadius.neg @ 0,  speakerRadius @ 0 ], sig, point, dbRollOff );
	polar = point.asPolar;
	angle = polar.angle.fold(0,pi).linlin(0,pi,1,-1,\none);
	amp = (0.15 / polar.rho).min( 1 );
	
	pankr = Pan2.kr( 1, angle ).max(amp);

	Out.ar( bus, UGlobalEQ.ar( panned ) * pankr * UEnv.kr  );
} )
	.category_( \io )
	.setSpec( \bus, PositiveIntegerSpec( 0 ) )
	.setSpec( \point, PointSpec( 200, step: 0.1 ) )
	.setSpec( \dbRollOff,  [-24, 0, \lin, 1, -6 ].asSpec )
	.setSpec( \speakerRadius, [0,1,\lin,0,0.3].asSpec );