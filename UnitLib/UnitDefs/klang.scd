/*
\klang

A bank of sine oscillators.

range: the minimum and maximum frequency
exponential: distribution of frequencies: 0: linear, 1: exponential
variation: amount of random variation. 0: no variation, 0.5: linear distribution, 1: full random.
amp: range of random amplitudes
phase: range of random phases
seed: random seed
n: number of oscillators

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

(
var defs, def;
defs = [8,16,32].collect({ |n|
	Udef( "klang_%".format(n).asSymbol, {
		var range, exponential, variation, freqs, amp, phase, sig;
		range = \range.kr( [440,880] );
		exponential = \exponential.kr( 1 );
		variation = \variation.kr( 0 );
		amp = \amp.asSpec.unmap( \amp.kr([0.1,0.1]) );
		phase = \phase.kr([0,0]);
		URandSeed.ir( \seed.ir(12345) );
		freqs = SortedRand.ir( n, 0, 1, \lin, variation * 2 );
		freqs = (exponential * freqs.linexp(0,1,*range)) + 
			((1-exponential) * freqs.linlin(0,1,*range));
		amp = \amp.asSpec.map( Rand(0.dup(n),1).linlin(0,1,*amp) );
		phase = Rand(0.dup(n),1).linlin(0,1,*phase);
		sig = DynKlang.ar( `[ freqs, amp, phase ] ) * (1/n).sqrt * Line.ar(0,1,1/range[0]);
		UMixOut.ar( 0, sig, 0, true );
	}, [ 
		[ \range, [440,880], FreqSpec(2,20000).asRangeSpec ],
		[ \exponential, 1, [0,1].asSpec ],
		[ \variation, 0, [0,1].asSpec ],
		[ \amp, [0.1,0.1], \amp.asSpec.asRangeSpec ],
		[ \phase, [ 0, 0 ], AngleSpec().asRangeSpec ],
		[ \seed, 12345, PositiveIntegerSpec(), false, \init ]
	], addToAll: false )
});

def = MultiUdef( \klang, defs, \synthesis, \n, false );

def.argSpecs.last.spec.labels = [8,16,32].collect(_.asSymbol);

def
)