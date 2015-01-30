/*
\splay

divides an arbitrary number of inputs over an arbitrary number of outputs via equal power intensity panning. Can also be used to manipulate fixed arrays of channels (i.e. rotate, leak etc.).

spread: the amount of spreading; 0 means all inputs go to first output, 1 (default) means all outputs will be used
width: the width sets how many outputs are used for each input. A cosine window is applied here.
center: rotates the outputs; 0 means the first input goes to the first output, 1 or -1 means the first input goes to the middle output.
numChannels: number of input channels (*).
numOutputs: number of output channels (1-8) (**).

(*) only the following numbers of channels are allowed:
1,2,3,4,5,6,7,8,10,12,16,24,32

(**) in the UChainGUI these numbers are displayed as normal numbers, but in the source code the actual value is a Symbol formatted as follows: 'splay_1', 'splay_2' .. 'splay_8'

example:

numChannels = 4, numOutputs = 8

inputs   0 1 2 3
         | | | |
         | | | +++++++
         | | +++++   |
         | +++   |   |
         |   |   |   |
outputs  0 1 2 3 4 5 6 7

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

(
var defs, def;
defs = (1..8).collect({ |n|
	MultiChannelUdef( "splay_%".format(n).asSymbol, { |spread = 1, width = 2, center = 0|
		var sig;
		sig = UIn.ar( 0, Udef.numChannels );
		switch( n,
			1, { UOut.ar( 0, sig.asCollection.sum * sig.asCollection.size.reciprocal.sqrt ) },
			2, { UOut.ar( 0, Splay.ar( sig.asCollection, spread, 1, center ) ) },
			{
				if( Udef.numChannels == 1 ) {
					UOut.ar( 0, SplayAz.ar( n, sig.dup, spread, 1, width, center, 0 ) );
				} {
					UOut.ar( 0, SplayAz.ar( n, sig, spread, 1, width, center, 0 ) );
				};
			}
		);
	}, addToAll: false)
		.setSpec( \spread, [0,1,\lin,0,1].asSpec )
		.setSpec( \width, [1,n.max(2),\exp,0,2].asSpec )
		.setSpec( \center, [-1,1,\lin,0,0].asSpec );
});

def = MultiUdef( \splay, defs, \utility, \numOutputs, false );

def.argSpecs.last.spec.labels = (1..8).collect(_.asSymbol);

def
)