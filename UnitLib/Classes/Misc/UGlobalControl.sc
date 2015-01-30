UGlobalControl : OEM {
	
	classvar <>current;
	classvar <>presetManager;
	classvar <>autoAdd = true;
	classvar <>autoRemove = false;
	
	*initClass {
		Class.initClassTree( PresetManager );
		
		presetManager = PresetManager( this, [ 
			\default, this.new( 
				*(..7).collect({ |x| [ ("global_" ++ x).asSymbol, 0.5 ] }).flatten(1) 
			),
			\nano_sliders, {
				var ccs;
				ccs = [ 2, 3, 4, 5, 6, 8, 9, 12, 13 ];
				this.new(
					*ccs.collect({ |cc, i|
						[ 
							("global_" ++ i).asSymbol, 
							UMap( 'midi_cc', [
								\cc, cc,
								\channel, 0
							] )
						]
					}).flatten(1)
				)
			}
		] )
			.getFunc_({ |obj| obj.deepCopy })
			.applyFunc_({ |object, preset|
			 	object.fromObject( preset );
		 	});
		 	
		 this.fromPreset( \default ).makeCurrent;
	}
		
	*presets { ^presetManager.presets.as(IdentityDictionary) }
	
	fromObject { |obj|
		var keys;
		obj = obj.value; // in case it is a function
		if( autoRemove ) {
			keys = obj.keys;
			this.keys.copy.do({ |key|
				if( keys.includes( key ).not ) {
					this.put( key, nil );
				};
			});
		};
		this.set( *obj.getPairs.deepCopy );
	}
	
	*fromObject { |obj|
		^obj.deepCopy;
	}
	
	*fromPreset { |name| ^presetManager.apply( name ) }
	
	fromPreset { |name| ^presetManager.apply( name, this ); }
	
	makeCurrent {
		current.removeDependant( this.class );
		this.addDependant( this.class );
		current = this;
		this.class.changed( \current );
	}
	
	removeCurrent {
		current.removeDependant( this.class );
		current = nil;
		this.class.changed( \current );
	}
	
	stopUMap { |key|
		if( this[ key ].isUMap ) {
			this[ key ].stop;
			this[ key ].dispose;
		};
	}
	
	put { |key, value|
		if( autoRemove or: { value.notNil } ) {
			if( autoAdd ) {
				if( this[ key ] !== value ) {
					this.stopUMap( key );
				};
				super.put( key, value.asUnitArg(this,key) );
			} {
				if( this.keys.includes( key ) ) {
					if( this[ key ] !== value ) {
						this.stopUMap( key );
					};
					super.put( key, value.asUnitArg(this,key) );
				};
			};
		};
	}
	
	get { |key| 
		^this.at( key ).value;
	}
	
	set { |...keyValuePairs| 
		keyValuePairs.pairsDo({ |key, value|
			this.put( key, value );
		});
	}
	
	getSpec { ^[0,1].asSpec }
	getArgSpec { |key| ^ArgSpec( key, 0.5, [0,1].asSpec, false, \init ) }
	getSpecMode { ^\init }
	
	canUseUMap { |key, umapdef|
		^umapdef.allowedModes.includes( this.getSpecMode ) && {
			umapdef.numChannels == 1
		};
	}
	
	getUMaps { ^this.values.select( _.isUMap ) }
	
	getAllUMaps { 
		var umaps;
		this.getUMaps.do({ |item|
			umaps = umaps.add( item );
			umaps = umaps.addAll( item.getAllUMaps );
		});
		^umaps;
	}
	
	argSpecs { ^this.keys.collect({ |key| this.getArgSpec(key) }) }
	defName { ^"UGlobalControl" }
	guiCollapsed { ^false }
	argSpecsForDisplay { ^this.argSpecs }
	getDefault { ^0.5 }
	
	args { ^this.getPairs }
	
	init { this.changed( \init ) }
	
	prepare { |key, action|
		var act, val;
		if( key.isNil ) {
			act = MultiActionFunc({ action.value });
			this.keys.do({ |key| this.prepare( key, act.getAction ) });
		} {
			val = this[ key ];
			if( val.respondsTo( \unit_ ) ) {
				val.unit = this;
			};
			if( val.respondsTo( \prepare ) ) {
				val.prepare( ULib.servers, 0, action );
			} {
				action.value;
			};
		};
	}
	
	dispose { |key|
		this.values.do({ |value| 
			if( value.respondsTo( \dispose ) ) { 
				value.dispose;
			}
		});
	}
	
	valuesSetUnit {
		this.values.do({ |key, value| 
			if( value.respondsTo( \unit_ ) ) { 
				value.unit = this;
			}
		});
	}
	
	*update { |obj ...args| // redirect changed messages from current
		this.changed( *args );
	}

}