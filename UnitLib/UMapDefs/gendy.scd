/*
\gendy

A GENDY generator (by Nick Collins). This is implementation of the dynamic stochastic synthesis generator conceived by Iannis Xenakis and described in Formalized Music (1992, Stuyvesant, NY: Pendragon Press) chapter 9 (pp 246-254) and chapters 13 and 14 (pp 289-322).

ampdist: Choice of probability distribution for the next perturbation of the amplitude of a control point. The distributions are (adapted from the GENDYN program in Formalized Music):
	0: LINEAR.
	1: CAUCHY.
	2: LOGIST.
	3: HYPERBCOS.
	4: ARCSINE.
	5: EXPON.
The "SINUS" distribution, which needs time-varying input adparam and ddparam, is left out as it would not work in this udef.

durdist: Choice of distribution for the perturbation of the current inter control point duration.
adparam: A parameter for the shape of the amplitude probability distribution
ddparam: A parameter for the shape of the duration probability distribution
freq: Minimum and maximum allowed frequencies of oscillation for the Gendy1 oscillator, so gives the smallest and largest period the duration is allowed to take on.

ampscale: Multiplier for the distribution's delta value for amplitude. An ampscale of 1.0 allows the full range of -1 to 1 for a change of amplitude.

durscale: Multiplier for the distribution's delta value for duration. An ampscale of 1.0 allows the full range of -1 to 1 for a change of duration.

initCPs: Initialise the number of control points in the memory. Xenakis specifies 12. There would be this number of control points per cycle of the oscillator, though the oscillator's period will constantly change due to the duration distribution.

knum: Current number of utilised control points (can be modulated).

seed: random seed (positive whole number). The same seed will always result in exactly the same signal on any computer. If you want different signals for different UChains that play at the same moment, use different seeds.

range: the output value range

The 'range' arg is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \gendy, { |ampdist = 1, durdist = 1, adparam = 1, ddparam = 1, freq = #[0.1,100], ampscale = 0.5, durscale = 0.5, initCPs = 12, knum = 12, seed = 12345, range = #[0,1]|
	var sig;
	URandSeed.ir(seed);
	
	freq[0] = freq[0].min( ControlRate.ir / knum );
	freq[1] = freq[1].min( ControlRate.ir / knum );
		
	sig = Gendy1.kr( ampdist, durdist, adparam, ddparam, freq[0], freq[1], 
		ampscale, durscale, initCPs, knum ).linlin(-1,1,*range);
		
	UMapOut.kr( sig ) 
} ).category_( \modulation )
	.mappedArgs_( [ \range ] )
	.setSpec( \ampdist, ListSpec( (..5), 1, [ \LINEAR, \CAUCHY, \LOGIST, \HYPERBCOS, \ARCSINE, \EXPON ] ) )
	.setSpec( \durdist, ListSpec( (..5), 1, [ \LINEAR, \CAUCHY, \LOGIST, \HYPERBCOS, \ARCSINE, \EXPON ] ) )
	.setSpec( \adparam, [0.0001, 1, \exp].asSpec )
	.setSpec( \ddparam, [0.0001, 1, \exp].asSpec )
	.setSpec( \freq, [0.01,1000,\exp].asSpec.asRangeSpec )
	.setDefault( \freq, [0.1,100] )
	.setSpec( \ampscale, [0,1].asSpec )
	.setSpec( \durscale, [0,1].asSpec )
	.setSpec( \initCPs, IntegerSpec( 12, 1, 40 ), \init )
	.setSpec( \knum, [1,40,\lin,1,12].asSpec )
	.setSpec( \lag, [0,1,\lin].asSpec )
	.setSpec( \seed, PositiveIntegerSpec(12345) );