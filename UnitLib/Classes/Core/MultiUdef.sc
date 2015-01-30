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

HiddenUdef : Udef {
	
	// these are not added to the global udef dict
	
	classvar <>all;
	
	*prefix { ^"uh_" } // synthdefs get another prefix to avoid overwriting
    
    addToAll { |name|
		this.class.all ?? { this.class.all = IdentityDictionary() };
		this.class.all[ name ] = this; // name doesn't need to be a Symbol
	}
	
}

HiddenFreeUdef : FreeUdef {
	
	// these are not added to the global udef dict
	
	classvar <>all;
	
	*prefix { ^"uh_" } // synthdefs get another prefix to avoid overwriting
    
    addToAll { |name|
		this.class.all ?? { this.class.all = IdentityDictionary() };
		this.class.all[ name ] = this; // name doesn't need to be a Symbol
	}
	
}


MultiUdef : Udef {

	var <>udefs;
	var <>chooseFunc;
	var >defNameKey;
	var tempDef;
	
	*defNameKey { ^\u_defName }
	defNameKey { ^defNameKey ? this.class.defNameKey }
	
	*new { |name, udefs, category, setter, setterIsPrivate = true| // first udef in list is default
		^super.basicNew( name, [ 
			ArgSpec( setter ? this.defNameKey, 
				udefs[0].name, ListSpec( udefs.collect(_.name) ), setterIsPrivate, \nonsynth )
		], category )
			.defNameKey_( setter )
			.udefs_( udefs );
	}
	
	findUdef{ |name|
		^udefs.detect({ |item| item.name == name }) ? udefs[0];
	}
	
	findUdefFor { |unit|
		^tempDef ?? { this.findUdef( unit.get( this.defNameKey ) ); };
	}
	
	asArgsArray { |argPairs, unit, constrain = true|
		var defName, argz, newDefName;
		defName = (argPairs ? []).detectIndex({ |item| item == this.defNameKey });
		if( defName.notNil ) {
			defName = argPairs[ defName + 1 ];
		} {
			defName = udefs[0].name;
		};
		tempDef = this.findUdef( defName );
		argz = tempDef.asArgsArray( argPairs ? [], unit, constrain );
		if( chooseFunc.notNil ) {
			newDefName = chooseFunc.value( argz );
			if( newDefName != defName ) { // second pass
				defName = newDefName;
				tempDef = this.findUdef( defName );
				argz = tempDef.asArgsArray( argPairs ? [], unit, constrain );
			};
		};
		tempDef = nil;
		^argz ++ [ this.defNameKey, defName ];
	}
	
	args { |unit| 
		^(this.findUdefFor( unit ).args( unit ) ? []) ++ 
			argSpecs.collect({ |item| [ item.name, item.default ] }).flatten(1) 
	}
	
	argNamesFor { |unit|
		^(this.findUdefFor( unit ).argNamesFor( unit ) ? []) ++ this.argNames;
	}
	
	synthDef { ^udefs.collect(_.synthDef).flat }
	
	createSynth { |unit, target, startPos|
		^this.findUdefFor( unit ).createSynth( unit, target, startPos );
	}
		
	prIOids { |mode = \in, rate = \audio, unit|
		^this.findUdefFor( unit ).prIOids( mode, rate, unit );
	}
	
	canFreeSynth { |unit| ^this.findUdefFor( unit ).canFreeSynth( unit ) }
	
	chooseDef { |unit|
		var currentDefName, newDefName;
		if( chooseFunc.notNil ) {
			currentDefName = unit.get( this.defNameKey );
			newDefName = chooseFunc.value( unit.args );
			if( currentDefName != newDefName ) {
				unit.setArg( this.defNameKey, newDefName );
				unit.init( unit.def, unit.args );
			};
		};
	}
	
	setSynth { |unit ...keyValuePairs|
		this.chooseDef( unit ); // change def based on chooseFunc if needed
		if( keyValuePairs.includes( this.defNameKey ) ) {
			unit.init( unit.def, unit.args );
		} {
			^this.findUdefFor( unit ).setSynth( unit, *keyValuePairs );
		};
	}
	
	getArgSpec { |key, unit|
		if( key === this.defNameKey ) {
			^argSpecs[0];
		} {
			^this.findUdefFor( unit ).getArgSpec( key, unit );
		};
	}
	
	getSpec { |key, unit|
		if( key === this.defNameKey ) {
			^argSpecs[0].spec;
		} {
			^this.findUdefFor( unit ).getSpec( key, unit );
		};
	}
	
	getDefault { |name, unit|
		var asp;
		asp = this.getArgSpec(name, unit);
		if( asp.notNil ) { ^asp.default } { ^nil };
	}
	
	findUdefsWithArgName { |key|
		if( key === this.defNameKey ) {
			^[ this ];
		} {
			^udefs.select({ |udef|
				udef.argNames.includes( key );
			});
		};
	}
	
	setSpec { |name, spec, mode, constrainDefault = true| // set the spec for all enclosed udefs
		this.findUdefsWithArgName( name ).do({ |item|
			item.setSpec( name, spec, mode, constrainDefault );
		});
	}
	
	setSpecMode { |...pairs|
		pairs.pairsDo({ |name, mode|
			this.findUdefsWithArgName( name ).do({ |item|
				item.setSpecMode( name, mode );
			});
		});
	}
	
	setArgSpec { |argSpec|
		argSpec = argSpec.asArgSpec;
		this.findUdefsWithArgName( argSpec.name ).do({ |item|
			item.setArgSpec( argSpec );
		});
	}
	
	argSpecs { |unit|
		^this.findUdefFor( unit ).argSpecs( unit ) ++ argSpecs;
	}
	
}