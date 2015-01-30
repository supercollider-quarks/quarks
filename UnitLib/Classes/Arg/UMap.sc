UMapDef : Udef {
	classvar <>all, <>defsFolders, <>userDefsFolder;
	classvar <>defaultCanUseUMapFunc;
	
	var <>mappedArgs;
	var <>outputIsMapped = true;
	var >canInsert;
	var >insertArgName;
	var <>allowedModes = #[ sync, normal ];
	var <>canUseUMapFunc;
	var <>apxCPU = 0;
	
	*initClass{
		this.defsFolders = [ 
			this.filenameSymbol.asString.dirname.dirname.dirname +/+ "UMapDefs"
		];
		this.userDefsFolder = Platform.userAppSupportDir ++ "/UMapDefs/";
		defaultCanUseUMapFunc = { |unit, key, umapdef|
			unit.getSpec( key ).respondsTo( \asControlSpec ) && {
				unit.getDefault( key ).asControlInput.asCollection.size == umapdef.numChannels
			};
		};
	}
	
	*prefix { ^"umap_" } // synthdefs get another prefix to avoid overwriting
	
	*from { |item| ^item.asUDef( this ) }
	
	dontStoreValue { ^false }
	
	asArgsArray { |argPairs, unit, constrain = true|
		argPairs = argPairs ? #[];
		^argSpecs.collect({ |item| 
			var val;
			val = argPairs.pairsAt(item.name) ?? { item.default.copy };
			val = val.deepCopy.asUnitArg( unit, item.name );
			if( constrain && this.isMappedArg( item.name ).not && { val.isKindOf( UMap ).not } ) { 
				val = item.constrain( val ) 
			};
			[ item.name,  val ] 
		}).flatten(1);
	}
	
	isMappedArg { |name|
		^mappedArgs.notNil && { mappedArgs.includes( name ) };
	}
	
	argSpecs { |unit|
		^argSpecs.collect({ |asp|
			if( this.isMappedArg( asp.name ) ) {
				asp.adaptToSpec( unit !? _.spec );
			} {
				asp
			};
		});
	}
	
	args { |unit|
		^this.argSpecs( unit ).collect({ |item| [ item.name, item.default ] }).flatten(1);
	}
		
	getArgSpec { |name, unit|
		var asp;
		asp = argSpecs.detect({ |item| item.name == name });
		if( this.isMappedArg( name ) ) {
			^asp !? _.adaptToSpec( unit !? _.spec )
		} {
			^asp
		};
	}
	
	getSpec { |name, unit|
		var asp;
		asp = argSpecs.detect({ |item| item.name == name });
		if( this.isMappedArg( name ) ) {
			^asp.spec.adaptToSpec( unit.spec );
		} {
			^asp.spec
		};
	}
	
	getDefault { |name, unit|
		var asp;
		asp = this.getArgSpec(name, unit);
		if( asp.notNil ) { ^asp.default; } { ^nil };
	}
	
	setSynth { |unit ...keyValuePairs|
		keyValuePairs = keyValuePairs.clump(2).collect({ |item|
			if( this.isMappedArg( item[0] ) && { item[1].isUMap.not } ) {
				[ item[0], this.getSpec( item[0], unit ) !? _.unmap( item[1] ) ? item[1] ];
			} {
				item
			};
		}).flatten(1);
		this.prSetSynth( unit.synths, *keyValuePairs );
	}
	
	activateUnit { |unit, parentUnit| // called at UMap:asUnitArg
		if( unit.synths.size == 0 && {
			parentUnit.notNil && { parentUnit.synths.size > 0 } 
		}) {
				unit.unit_(parentUnit);
				unit.setUMapBus;
				unit.prepareAndStart( unit.unit.synthsForUMap );
			};
	}
	
	asUMapDef { ^this }
	
	isUdef { ^false }
	
	getControlInput { |unit|
		if( this.hasBus ) {
			if( this.numChannels > 1 ) {
				^this.numChannels.collect({ |i|
					("c" ++ (this.getBus(unit) + i + unit.class.busOffset)).asSymbol;
				});
			} {
				^("c" ++ (this.getBus(unit) + unit.class.busOffset)).asSymbol;
			};
		} {
			^this.value( unit ).asControlInput;
		};
	}
	
	value { |unit|  
		// subclass may put something different here
		^unit !? {|x| x.spec.default } ? 0;
	}
	
	getBus { |unit|
		^unit.get(\u_mapbus) ? 0
	}
	
	setBus { |bus = 0, unit|
		unit.set(\u_mapbus, bus );
	}
	
	hasBus { ^this.argNames.includes( \u_mapbus ); }
	
	createSynth { |umap, target, startPos = 0| // create A single synth based on server
		target = target ? Server.default;
		^Synth( this.synthDefName, umap.getArgsFor( target, startPos ), target, \addBefore );
	}
	
	unitCanUseUMap { |unit, key|
		^(canUseUMapFunc ? defaultCanUseUMapFunc).value( unit, key, this );
	}
	
	canInsert {
		^(canInsert != false) && { this.insertArgName.notNil; };
	}
	
	insertArgName {
		if( insertArgName.isNil ) {
			if( outputIsMapped ) {
				insertArgName = mappedArgs.asCollection.detect({ |item|
					this.getDefault( item ).asControlInput.asCollection.size == this.numChannels
				});
			} {
				insertArgName = argSpecs.select({ |item|
					item.private.not && { mappedArgs.asCollection.includes( item.name ).not }
				}).detect({ |item| 
					item.default.asControlInput.asCollection.size == this.numChannels;
				}) !? _.name;
			};
		};
		^insertArgName;
	}
}

UMap : U {
	
	// This class is under development. For now it plays a line between min and max.
	// it can only be used for args that have a single value ControlSpec
	// gui doesn't work yet
	
	/* 
	example:
	x = UChain([ 'sine', [ 'freq', UMap() ] ], 'output');
	x.prepareAndStart;
	x.stop;
	*/
	
	classvar <>allUnits;
	classvar <>currentBus = 0, <>maxBus = 499;
	classvar >guiColor;
	classvar <>allStreams;
	classvar <>currentStreamID = 0;
	
	var <spec;
	var <>unitArgName;
	var <>unmappedKeys;
	var <>streamID;
	
	*busOffset { ^1500 }
	
	*guiColor { ^guiColor ?? { guiColor = Color.blue.blend( Color.white, 0.8 ).alpha_(0.4) }; }
	guiColor { ^this.class.guiColor }
	
	init { |in, inArgs, inMod|
		super.init( in, inArgs ? [], inMod );
		this.setunmappedKeys( inArgs );
		this.mapUnmappedArgs;
	}
	
	setunmappedKeys { |args|
		args = (args ? []).clump(2).flop[0];
		this.def.mappedArgs.do({ |item|
			if( args.includes( item ).not ) {
				unmappedKeys = unmappedKeys.add( item );
			};
		});
	}
	
	*initClass { 
	    allUnits = IdentityDictionary();
	    allStreams = Order();
	}
	
	*defClass { ^UMapDef }
	
	asControlInput {
		^this.def.getControlInput(this);
	}
	
	asOSCArgEmbeddedArray { | array| ^this.asControlInput.asCollection.asOSCArgEmbeddedArray(array) }
	
	getBus { 
		^this.def.getBus( this );
	}
	
	setBus { |bus = 0|
		this.def.setBus( bus, this );
	}
	
	nextBus {
		var res, nextBus, n;
		n = this.def.numChannels;
		nextBus = currentBus + n;
		if( nextBus > (maxBus + 1) ) {
			nextBus = 0 + n;
			res = 0;
		} {
			res = currentBus;
		};
		currentBus = nextBus;
		^res;
	}
	
	setUMapBus {
		if( this.hasBus ) {
			this.setBus( this.nextBus );
		};
	}
	
	set { |...args|
		var keys;
		if( unmappedKeys.size > 0 ) {	
			keys = (args ? []).clump(2).flop[0];
			keys.do({ |item|
				if( unmappedKeys.includes( item ) ) {
					unmappedKeys.remove(item);
				};
			});
		};
		^super.set( *args );
	}
	
	isUMap { ^true }
	
	hasBus { ^this.def.hasBus }
	
	setUMapBuses { } // this is done by the U for all (nested) UMaps
	
	u_waitTime { ^this.waitTime }
	
	dontStoreArgNames { ^[ 'u_dur', 'u_doneAction', 'u_mapbus', 'u_spec', 'u_store', 'u_prepared' ] ++ if( this.def.dontStoreValue ) { [ \value ] } { [] } }
	
	spec_ { |newSpec|
		if( spec.isNil ) {
			if( newSpec.notNil ) {
				spec = newSpec;
				this.mapUnmappedArgs;
			};
		} {
			if( newSpec != spec ) {	
				this.def.mappedArgs.do({ |key|
					var val;
					val = this.get( key );
					if( val.isUMap.not ) {
						this.set( key, this.getSpec( key ).unmap( this.get( key ) ) );
					} {
						val.spec = nil;
					};
				});
				spec = newSpec;
				unmappedKeys = this.def.mappedArgs.copy;
				this.mapUnmappedArgs;
			}
		} 
	}
	
	mapUnmappedArgs {
		if( spec.notNil ) {
			unmappedKeys.copy.do({ |key|
				var val;
				val = this.get( key );
				if( val.isUMap.not ) {
					this.set( key, this.getSpec( key ).map( val ) );
				} {
					val.spec = this.getSpec( key );
				};
			});
		};
	}
	
	// UMap is intended to use as arg for a Unit (or another UMap)
	asUnitArg { |unit, key|
		if( unit.canUseUMap( key, this.def ) ) {
			this.unitArgName = key;
			if( key.notNil ) {
				if( unit.isUMap && { unit.def.isMappedArg( key ) } ) {
					if( unit.spec.notNil ) {
						this.spec = unit.getSpec( key ).copy;
						this.set( \u_spec, [0,1,\lin].asSpec );
					};
				} {
					this.spec = unit.getSpec( key ).copy;
					this.set( \u_spec, spec );
				};
				this.def.activateUnit( this, unit );
				this.valuesAsUnitArg
			};
			^this;
		} {
			^unit.getDefault( key );
		};
	}
	
	unit_ { |aUnit|
		if( aUnit.notNil ) {
			case { this.unit == aUnit } {
				// do nothing
			} { allUnits[ this ].isNil } {
				allUnits[ this ] = [ aUnit, nil ];
			} {
				"Warning: unit_ \n%\nis already being used by\n%\n".postf(
					this.class,
					this.asCompileString, 
					this.unit 
				);
			};
		} {
			allUnits[ this ] = nil; // forget unit
		};
	}
	
	unit { ^allUnits[ this ] !? { allUnits[ this ][0] }; }
	
	unitSet { // sets this object in the unit to enforce setting of the synths
		if( this.unit.notNil ) {	
			if( this.unitArgName.notNil ) {
				this.unit.set( this.unitArgName, this );
			};
		};
	}
	
	getSynthArgs {
		var nonsynthKeys;
		nonsynthKeys = this.argSpecs.select({ |item| item.mode == \nonsynth }).collect(_.name);
		^this.args.clump(2).select({ |item| nonsynthKeys.includes( item[0] ).not })
			.collect({ |item|
				if( this.def.isMappedArg( item[0] ) && { item[1].isUMap.not }) {
					[ item[0], this.getSpec( item[0] ) !? _.unmap( item[1] ) ? item[1] ];
				} {
					item
				};
			})
			.flatten(1);
	}
	
	/// UPat
	
	stream {
		^allStreams[ streamID ? -1 ];
	}
	
	stream_ { |stream|
		this.makeStreamID;
		allStreams[ streamID ] = stream;
	}
	
	*nextStreamID {
		^currentStreamID = currentStreamID + 1;
	}
	
	makeStreamID { |replaceCurrent = false|
		if( replaceCurrent or: { streamID.isNil }) {
			streamID = this.class.nextStreamID;
		};
	}
	
	makeStream {
		this.def.makeStream( this );
	}

	resetStream {
		this.stream.reset;
	}

	next { ^this.asControlInput }
	
	disposeFor {
		if( this.unit.notNil && { this.unit.synths.select(_.isKindOf( Synth ) ).size == 0 }) {
			this.unit = nil;
		};
		if( this.def.isKindOf( FuncUMapDef ) ) {
			this.values.do{ |val|
	       	 if(val.respondsTo(\disposeFor)) {
		            val.disposeFor( *args );
		        }
		    };
		};
	}
	
	dispose {
	    this.free;
	    this.values.do{ |val|
	        if(val.respondsTo(\dispose)) {
	            val.dispose
	        }
	    };
	    this.modPerform( \dispose );
	    preparedServers = [];
	    this.unit = nil;
	}

}

MassEditUMap : MassEditU {
	
	var <>mixed = false;
	
	init { |inUnits|
		var firstDef, defs;
		units = inUnits.asCollection;
		if( units.every(_.isUMap) ) {	
			defs = inUnits.collect(_.def);
			firstDef = defs[0];
			if( defs.every({ |item| item == firstDef }) ) {
				def = firstDef;
				argSpecs = units[0].argSpecs.collect({ |argSpec|
					var values, massEditSpec;
					values = units.collect({ |unit|
						unit.get( argSpec.name );
					});
					if( values.any(_.isUMap) ) {
						massEditSpec = MassEditUMapSpec( MassEditUMap( values ) );
					} {	
						massEditSpec = argSpec.spec.massEditSpec( values );
					};
					if( massEditSpec.notNil ) {
						ArgSpec( argSpec.name, massEditSpec.default, massEditSpec, argSpec.private, argSpec.mode ); 
					} {
						nil;
					};
				}).select(_.notNil);
				args = argSpecs.collect({ |item| [ item.name, item.default ] }).flatten(1);
				this.changed( \init );
			} {
				mixed = true;
			};
		} {
			mixed = true;
		};
	}
	
	unitArgName { ^units.detect(_.isUMap).unitArgName }
	
	asUnitArg { }
	
	isUMap { ^true }
	
	defName { 
		var numUMaps, numValues;
		if( mixed ) {
			numUMaps = units.count(_.isUMap);
			numValues = units.size - numUMaps;
			^("mixed" + "(% umaps%)".format( numUMaps, if( numValues > 0 ) { 
				", % values".format( numValues ) 
			} { "" }
			)).asSymbol
		} {
			^((this.def !? { this.def.name }).asString + 
				"(% umaps)".format( units.size )).asSymbol
		};
	}
	
	def {
        ^if( mixed ) { ^nil } { def ?? { defName.asUdef( this.class.defClass ) } };
    }
	
	def_ { |def| 
		units.do({ |item|
			if( item.isUMap ) { item.def = def };
		});
		this.init( units ); 
	}	
	
	getInitArgs {
		if( mixed ) { ^nil } { ^super.getInitArgs };
	}
	
	remove {
		units.do({ |item|
			if( item.isUMap ) { item.remove };
		});
		this.init( units ); 
	}
}

MassEditUMapSpec : Spec {
	
	var <>default;
	// placeholder for mass edit MassEditUMap
	
	*new { |default|
		^super.newCopyArgs( default );
	}
	
	viewNumLines { 
		if( default.mixed ) { 
			^1.1 
		} {
			^UMapGUI.viewNumLines( default );
		};
	}
	
	constrain { |value| ^value }
	
	map { |value| ^value }
	unmap { |value| ^value }
	
}

+ Object {
	isUMap { ^false }
}