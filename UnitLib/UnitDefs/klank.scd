/*
\klank

A bank of resonator filters.

range: the minimum and maximum frequency
exponential: distribution of frequencies: 0: linear, 1: exponential
variation: amount of random variation. 0: no variation, 0.5: linear distribution, 1: full random.
amp: range of random amplitudes
ringTime: range of random ring times. Can not be modulated.
seed: random seed
n: number of oscillators

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

(
var defs, def;
defs = [8,16,32].collect({ |n|
	Udef( "klank_%".format(n).asSymbol, {
		var range, exponential, variation, freqs, amp, ringTime, sig;
		var in;
		range = \range.kr( [440,880] );
		exponential = \exponential.kr( 1 );
		variation = \variation.kr( 0 );
		amp = \amp.asSpec.unmap( \amp.kr([0.1,0.1]) );
		ringTime = \ringTime.kr([0.1,0.1]);
		URandSeed.ir( \seed.ir(12345) );
		freqs = SortedRand.ir( n, 0, 1, \lin, variation * 2 );
		freqs = (exponential * freqs.linexp(0,1,*range)) + 
			((1-exponential) * freqs.linlin(0,1,*range));
		amp = \amp.asSpec.map( Rand(0.dup(n),1).linlin(0,1,*amp) );
		ringTime = Rand(0.dup(n),1).linexp(0,1,*ringTime);
		in = UIn.ar( 0 );
		sig = DynKlank.ar( `[ freqs, amp, ringTime ], in ) * (1/n).sqrt * 0.1;
		UMixOut.ar( 0, sig );
	}, [ 
		[ \range, [440,880], FreqSpec(20,20000).asRangeSpec ],
		[ \exponential, 1, [0,1].asSpec ],
		[ \variation, 0, [0,1].asSpec ],
		[ \amp, [0.1,0.1], \amp.asSpec.asRangeSpec ],
		[ \ringTime, [ 0.1, 0.1 ], [0.01,2,\exp].asSpec.asRangeSpec, false, \init  ],
		[ \seed, 12345, PositiveIntegerSpec(), false, \init ]
	], addToAll: false )
});

def = MultiUdef( \klank, defs, \filter, \n, false );

def.argSpecs.last.spec.labels = [8,16,32].collect(_.asSymbol);

def
)