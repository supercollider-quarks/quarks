/*
    Unit Library
    The Game Of Life Foundation. http://gameoflife.nl
    Copyright 2006-2011 Miguel Negrao, Wouter Snoei.

    GameOfLife Unit Library: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GameOfLife Unit Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GameOfLife Unit Library.  If not, see <http://www.gnu.org/licenses/>.
*/

// collects and analyzes a UChain's inputs and outputs

UChainAudioAnalyzer {
	
	var <chain;
	var <ins, <outs;
	
	*new { |chain|
		^super.newCopyArgs( chain ).init;
	}
	
	*inGetter { ^\getAudioIn }
	*outGetter { ^\getAudioOut }
	*mixOutLevelGetter { ^\getAudioMixOutLevel }
	*allInGetter { ^\audioIns }
	*allOutGetter { ^\audioOuts }
	
	*getterFor { |mode = \in|
		^switch( mode, \in, this.inGetter, \out, this.outGetter );
	}
	
	*allGetterFor { |mode = \in|
		^switch( mode, \in, this.allInGetter, \out, this.allOutGetter );
	}
	
	init { 
		
		var units;
		units = chain.units;

		// collects ins in format:
		// [  unit, index-of-unit, [indices], [buses] ]
		
		ins = units.collect({ |unit, i| 
			[ unit, i, unit.perform( this.class.allInGetter ) ]; 
		})			
			.select({ |item| item[2].size > 0 })
			.collect({ |item|
				item ++ [ 
					item[2].collect({ |index| 
						item[0].perform( this.class.inGetter, index ) 
					}) 
				];
			});
		
		// collects outs in format:
		// [  unit, index-of-unit, [indices], [buses], [mixoutlevels] ]
		// mixoutlevels: nil or value; value means it has a mixout
	
		outs = units.collect({ |unit, i| 
			[ unit, i, unit.perform( this.class.allOutGetter ) ]; 
		})			
			.select({ |item| item[2].size > 0 })
			.collect({ |item|
				item ++ [ 
					item[2].collect({ |index| 
						item[0].perform( this.class.outGetter, index ) 
					}),
					item[2].collect({ |index| 
						item[0].perform( this.class.mixOutLevelGetter, index ) 
					})
				];
			});
		
		this.changed( \init );
	}
	
	// analysis
	
	usedBuses { // all used buses
		
		var buses = Set();
		
		ins.do({ |item| item[ 3 ].do({ |bus| buses.add( bus ) }); });
		outs.do({ |item| item[ 3 ].do({ |bus| buses.add( bus ) }); });
		
		^buses.asArray
	}
	
	busSource { |bus = 0, i = 0| // returns index of unit that outputs to bus before i
		var item;
		if( i > 0 ) {
			^outs.reverse.detect({ |item| 
				item[3].includesEqual( bus ) && { item[1] < i } 
			});
		} {
			^nil;
		};
	}
	
	busDest { |bus = 0, i = 0| // returns [unit, index-of-unit] that gets input from bus after i
		^ins.detect({ |item| item[3].includesEqual( bus ) && { item[1] > i } });
	}
	
	busConnection { |mode = \in, bus = 0, i = 0|
		^switch( mode, \in, { this.busSource( bus, i ) }, \out, { this.busDest( bus, i ) } );
	}
	
	insFor { |i = 0|
		^ins.detect({ |item| item[1] == i });
	}
	
	outsFor { |i = 0|
		^outs.detect({ |item| item[1] == i });
	}
	
	ioFor { |mode = \in, i = 0|
		^switch( mode, \in, { this.insFor( i ) }, \out, { this.outsFor( i ) } );
	}
	
	mixOutLevelsFor { |i = 0|
		i = this.outsFor( i );
		^if( i.notNil ) { i[4] } { nil };
	}
		
	numInputsFor { |i = 0|
		i = this.insFor( i );
		^if( i.notNil ) { i[ 2 ].size } { 0 };
	}
	
	numOutputsFor { |i = 0|
		i = this.outsFor( i );
		^if( i.notNil ) { i[ 2 ].size } { 0 };
	}
	
	numIOFor { |i = 0|
		^this.numInputsFor( i ) + this.numOutputsFor( i );
	}
	
}

UChainControlAnalyzer : UChainAudioAnalyzer {
	
	*inGetter { ^\getControlIn }
	*outGetter { ^\getControlOut }
	*mixOutLevelGetter { ^\getControlMixOutLevel }
	*allInGetter { ^\controlIns }
	*allOutGetter { ^\controlOuts }
	
	
	/*
	init { 
		
		var units;
		units = chain.units.collect({ |unit|
			if( unit.class == MetaU ) { 
				unit.unit 
			} {
				unit;
			};
		});
		
		// collects ins and outs in format:
		// [  unit, index-of-unit, [indices], [buses] ]
		
		ins = units.collect({ |unit, i| [ unit, i, unit.def.controlIns ]; })			.select({ |item| item[2].size > 0 })
			.collect({ |item|
				item ++ [ item[2].collect({ |index| item[0].getControlIn( index ) }) ];
			});
		
		outs = units.collect({ |unit, i| [ unit, i, unit.def.controlOuts ]; })
			.select({ |item| item[2].size > 0 })
			.collect({ |item|
				item ++ [ item[2].collect({ |index| item[0].getControlOut( index ) }) ];
			});
		
	}
	*/
	
}