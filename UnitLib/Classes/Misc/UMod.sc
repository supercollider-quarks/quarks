UMod : ObjectWithArgs {
	
	var <>key, def, defName;
	var <>outSpec;
	var <>environment;
	var <mod;
	
	*new { |key, def, args|
		^super.new.key_(key).init( def, args );
	}
	
	*defClass { ^UModDef }
	
	asUModFor { |unit|
		if( unit.keys.includes( key ) ) {
			outSpec = unit.getSpec( key );
		} {
			"%:asUModFor - unit doesn't include key %; spec can not be found".postln;
		};
		^this;
	}
	
	get { |key|
		^this.getArg( key );
	}
	
	set { |...args|
		args.pairsDo({ |key, value|
			this.setArg( key, value );
		});
	}
	
	mapSet { |...args|
        var argsWithSpecs = args.clump(2).collect{ |arr|
            var key, value, spec;
            #key, value = arr;
            spec = this.getSpec(key);
            if( spec.notNil ) {
                [key, spec.map(value) ]
            } {
                [key, value ]
            }
        };
        this.set( * argsWithSpecs.flatten )
	}

	mapGet { |key|
		var spec = this.getSpec(key);
		^if( spec.notNil ) {
		    spec.unmap( this.get(key) )
		} {
		    this.get(key)
		}
	}
	
	gui { |parent, bounds| ^UGUI( parent, bounds, this ) }
	
	argSpecs { ^this.def.argSpecs( this ).collect({ |item|
			if( item.spec.class == UModOutSpec ) {
				item = item.deepCopy;
				item.spec.spec_( outSpec );
				item;
			} {
				item;
			} 
		});
	}
	
	getSpec { |key|
		var spec;
		spec = this.def.getSpec( key, this );
		if( spec.class == UModOutSpec ) {
			^spec.deepCopy.spec_( outSpec );
		} {
			^spec;
		} 
	}
	
	init { |inDef, inArgs|
		if( inDef.isKindOf( this.class.defClass ) ) {
			def = inDef;
			defName = inDef.name;
			if( defName.notNil && { defName.asUModDef == def } ) {
				def = nil;
			};
		} {
			defName = inDef.asSymbol;
			def = nil;
		};
		if( this.def.notNil ) {
			args = this.def.asArgsArray( inArgs ? [] );
		} {
			args = inArgs;
			"UModDef '%' not found".format(inDef).warn;
		};
	}
	
	def {
        ^def ?? { defName.asUModDef }
    }

    defName {
        ^defName ?? { def.name }
    }

    def_ { |newDef, keepArgs = true|
        this.init( newDef, if( keepArgs ) { args } { [] }); // keep args
    }

    defName_ { |newDefName, keepArgs = true|
        this.init( newDefName, if( keepArgs ) { args } { [] }); // keep args
    }
	
	use { |func|
		if( this.def.notNil ) {
			if(	environment.notNil ) {
				environment.use({
					func.value;
				});
			} {
				func.value;
			};
		};
	}
	
	mod_ { |newMod|
		this.modPerform( \disconnect );
		mod = newMod.asUModFor( this );
	}
	
	modPerform { |what ...args| mod !? _.perform( what, this, *args ); }
	
	connect { |unit|
		this.use({ this.def.connect( this, unit, key ) });
		this.modPerform( \connect );
	}
	
	disconnect {
		this.use({ this.def.disconnect( this, key ) });
		this.modPerform( \disconnect );
	}
	
	prepare { |unit, startPos = 0|
		this.use({ this.def.prepare( this, unit, key, startPos ) });
		this.modPerform( \prepare, startPos );
	}
	
	start { |unit, startPos = 0, latency = 0.2|
		this.use({ this.def.start( this, unit, key, startPos, latency ) });
		this.modPerform( \start, startPos, latency );
	}
	
	stop { |unit|
		this.use({ this.def.stop( this, unit, key ) });
		this.modPerform( \stop );
	}
	
	dispose { |unit|
		this.use({ this.def.dispose( this, unit, key ) });
		this.modPerform( \dispose );
	}
	
	pause { |unit|
		this.use({ this.def.pause( this, unit, key ) });
		this.modPerform( \pause );
	}
	
	unpause { |unit|
		this.use({ this.def.unpause( this, unit, key ) });
		this.modPerform( \unpause );
	}
	
	printOn { arg stream;
		stream << this.class.name << "( " <<* this.argsForPrint  <<" )"
	}
	
	dontStoreArgNames { ^[] }
	
	getInitArgs {
		var defArgs;
		defArgs = (this.def.args( this ) ? []).clump(2);
		^args.clump(2).select({ |item, i| 
			(item != defArgs[i]) && { this.dontStoreArgNames.includes( item[0] ).not };
		 }).flatten(1);
	}

	argsForPrint {
        var initArgs, initDef;
        initArgs = this.getInitArgs;
        initDef = this.def.name;
        if( initArgs.size > 0 ) {
            ^[ key, initDef, initArgs ];
        } {
            ^[ key, initDef ];
        };
    }
	
	storeArgs { 
		var initArgs, initDef;
		initArgs = this.getInitArgs;
		initDef = this.defName ?? { this.def };
		if( (initArgs.size > 0) ) {
			^[ key, initDef, initArgs ];
		} {
			^[ key, initDef ];
		};
	}
	
	storeModifiersOn { |stream|
		if( mod.notNil ) {
			stream << ".mod_(" <<<* mod << ")";
		};
	}
}

UModDef : GenericDef {
	
	classvar <>all;
	
	var <>funcDict;
	
	var <>category;
	
	*new { |name, funcDict, args, category, addToAll=true|
		if( name.isNil ) { addToAll = false };
		^super.new( name, args ? [], addToAll )
			.funcDict_( funcDict ).initNew.category_( category ? \default );
	}
	
	asUModDef { ^this }
	
	addToAll { |name|
		this.class.all ?? { this.class.all = IdentityDictionary() };
		this.class.all[ name ] = this; // name doesn't need to be a Symbol
	}
	
	initNew {
		if( funcDict.isFunction ) {
			funcDict = (\start -> funcDict);
		};
		if( funcDict.isArray ) {
			funcDict = Event.newFrom(funcDict)
		};
	}
	
	doFunc { |which, mod, unit, key ...args|
		funcDict[ which ].value( mod, unit, key, *args );
	}
	
	init { |mod, unit, key|
		this.doFunc( \init, mod, unit, key );
	}
	
	stop { |mod, unit, key|
		this.doFunc( \stop, mod, unit, key );
	}
	
	doesNotUnderstand { |selector ...args|
		if( selector.isSetter.not ) {
			^this.doFunc( selector, *args );
		} {
			^super.doesNotUnderstand( selector, *args );
		};
	}
	
}

UModOutSpec : Spec {
	
	var >spec;
	
	*new { |spec| ^this.newCopyArgs( spec ) }
	
	spec { ^spec ? ControlSpec( 0,1,\lin,0,0 ) }
	
	constrain { |value| ^ControlSpec( 0,1,\lin,0,0 ).constrain( value ); }
	default { ^this.spec.default }
	massEditSpec { ^nil }
	asSpec { ^this }
	
	map { |value| ^value }
	unmap { |value| ^value }
	
	makeView { |parent, bounds, label, action, resize|
		^this.spec.makeView( parent, bounds, label, 
			{ |vws, value| action.value( vws, this.spec.unmap( value ) ) }, resize );
	}
	setView { |vws, value, active = false|
		this.spec.setView( vws, this.spec.map( value ), active );
	}
	
	mapSetView { |vws, value, active = false|
		this.spec.mapSetView( vws, this.spec.map( value ), active );
	}
}

+ Object {
	asUModDef { ^this }
	asUModFor { |unit| ^this } // accept any object as umod (for now)
}

+ Function {
	asUModDef { |name| ^UModDef( name, this ); }
}

+ Symbol { 
	asUModFor { |unit, key| ^UMod( key, this ) }
	asUModDef { ^UModDef.fromName( this ); }
}

+ Array {
	asUModFor { |unit| ^UMod( this[0], *this[1..] ) }
}