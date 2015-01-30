/*
\clip

The clip Udef can clip the incoming signal in various ways, including a softness area where the signal is distorted to create less harsh clipping curves.

scale: a scaling ratio applied to the input signal
offset: a DC offset added to the scaled input signal
clip: clip amplitude level (0-1)
clipMode: the type of clipping applied (0-3)
	- 0: softClip; normal clipping with softclip area
	- 1: sineClip; normal clipping with a sinusoidial/s-curve softclip area
	- 2: fold; the signal is mirrored between the clipping borders
	- 3: wrap; the signal is wrapped around (modulo / %) the clipping borders
	- 4: excess; inverse/center cipping - only the signal outside the clipping area is kept
clipSoftness: amount of softening (0-1). This can be applied to any of the modes. Mode 0 (softClip) applies softening through non-linear distortion. Modes 1-4 apply softening through a sine function
makeUp: amount of automatic make-up gain added to compensate for the lost signal amplitude (0-1)
cutoff: frequency of a 2-pole high-cut filter applied after the clipping stage

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \clip, { |scale = 1, offset = 0, clip = 1, clipMode = 0, clipSoftness = 0.0, makeUp = 0.0, cutoff = 20000|
	var sig;
	clipSoftness = K2A.ar( clipSoftness );
	clip = clip.max(1.0e-12);
	sig = ((UIn.ar( 0 ) * scale) + offset) / clip;
	sig = Select.ar( clipMode, [
		sig.softclip2(1, clipSoftness),
		sig.sineclip2(1, clipSoftness),
		sig.softfold2(1, clipSoftness),
		sig.softwrap2(1, clipSoftness),
		sig.softexcess2(1, clipSoftness) / scale * (2-clip),
		sig
	]);
	sig = sig * (((1-makeUp) * clip) + makeUp);
	sig = BHiCut.ar( sig, cutoff.clip(20,20000), 1 );
	UOut.ar( 0, sig )
} )
	.category_( \distortion )
	.setSpec( \scale, [0.25, 4, \exp, 0, 1 ].asSpec )
	.setSpec( \offset, [-1, 1, \lin, 0, 0 ].asSpec )
	.setSpec( \clip, \amp.asSpec )
	.setSpec( \clipMode, ListSpec( (..4), 0, [ \softClip, \sineClip, \fold, \wrap, \excess ] ) )
	.setSpec( \cutoff, FreqSpec().default_(20000) );