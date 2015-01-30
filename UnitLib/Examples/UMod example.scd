(
UModDef( \sine_lfo, ( 
	start: { |mod, unit, key, startPos = 0|
		var func, time = startPos;
		func = { |time = 0|
			time.sin.linlin(-1,1, *mod.get( \range ) );
		};
		~lfo.stop; 
		unit.mapSet( key, func.value( time ) );
		~lfo = { 
			inf.do({
				mod.get(\res).wait;
				time = time + (mod.get(\res) * mod.get( \speed ));
				unit.mapSet( key, func.value( time ) );
			});
		}.fork;
	},
	dispose: { |mod, unit|
		if( unit.isPlaying.not ) {
			~lfo.stop;
			~lfo = nil;
		};
	}
), [ 
	[ \speed, 1, [ 0.1, 20, \exp ].asSpec], 
	[ \res, 0.05, [0.01,0.2,\exp].asSpec ], 
	[ \range, [0,1], RangeSpec( 0,1 ) ] 
]
);
)

a = UScore( UChain([ 'sine', [ 'lag', 0.1 ], UMod( \freq, \sine_lfo ) ], 'output'));
a.gui;

UGUI( unit: a[0][0].mod );
