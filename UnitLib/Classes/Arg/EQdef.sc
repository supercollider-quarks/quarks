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

EQdef {
	
	classvar <>default;
	classvar <>all;
	classvar <>specDict;
	
	var <>names, <>classes, <>argNames, <>defaultSetting, <>specs;
	var <>presets;
	var <>presetManager;
	
	*initClass {
		
		Class.initClassTree(Spec);
		
		all = IdentityDictionary[];
		
		specDict = (
			\freq: [ 20, 20000, \exp, 0, 440 ].asSpec,
			\rq: [ 0.001, 10, \exp, 0.01, 0.707 ].asSpec,
			\rs: [ 0.6, 10, \lin, 0.1, 1 ].asSpec,
			\bw: [ 0.01, 10, \exp, 0.1, 1 ].asSpec,
			\db: [ -36, 36, \lin, 0.25, 0 ].asSpec,
			\a0: [-1,1,\lin, 0.001, 0 ].asSpec,
			\a1: [-1,1,\lin, 0.001, 0 ].asSpec,
			\a2: [-1,1,\lin, 0.001, 0 ].asSpec,
			\b1: [-1,1,\lin, 0.001, 0 ].asSpec,
			\b2: [-1,1,\lin, 0.001, 0 ].asSpec,
			\radius: [ 0,1, \lin,0,0.8].asSpec,
			\coef: [-0.999, 0.999, \lin, 0.001, 0 ].asSpec,
			\order: [0,5,\lin,1,2].asSpec
		);
		
		default = EQdef( 
			'lowShelf', BLowShelf, 
			'peak1', BPeakEQ,
			'peak2', BPeakEQ,
			'peak3', BPeakEQ,
			'hiShelf', BHiShelf,
			'gain', Gain
		).defaultSetting_(
			[ 
				[ 100, 1, 0 ], 
				[ 250, 1, 0 ], 
				[ 1000, 1, 0 ], 
				[ 3500, 1, 0 ], 
				[ 6000, 1, 0 ],
				[ 0 ]
			]
		);
		
		default.presetManager.presets = [ 	
			'flat', [ 
				[ 100.0, 1.0, 0.0 ], [ 250.0, 1.0, 0.0 ], [ 1000.0, 1.0, 0.0 ], 
				[ 3500.0, 1.0, 0.0 ], [ 6000.0, 1.0, 0.0 ], [ 0.0 ] 
			], 
			'low boost', [ 
				[ 100.0, 1.0, 6.0 ], [ 250.0, 1.0, 0.0 ], [ 1000.0, 1.0, 0.0 ],
				[ 3500.0, 1.0, 0.0 ], [ 6000.0, 1.0, 0.0 ], [ 0.0 ] 
			],
			'loudness', [ 
				[ 100.0, 1.0, 6.0 ], [ 250.0, 1.0, 0.0 ], [ 1000.0, 1.0, 0.0 ], 
				[ 3500.0, 1.0, 3.0 ], [ 6000.0, 1.0, 6.0 ], [ 0.0 ] 
			], 
			'telephone', [ 
				[ 200.0, 1.0, -24.0 ], [ 250.0, 1.0, 0.0 ], [ 1500.0, 1.0, 6.0 ], 
				[ 3500.0, 1.0, 0.0 ], [ 3500.0, 1.0, -24.0 ], [ 0.0 ] 
			]
		];
		
		default.name_( \default );
		
	}
	
	*new { |...bandPairs|
		
		// \bandName, Class, \bandName, Class etc..
		// class must respond to *coeffs
		
		^super.newCopyArgs.init( bandPairs );
	}
	
	addToDefs { |key = \new|
		all[ key ] = this;
	}
	
	*fromKey { |key|
		^all[ key ]	
	}
	
	*fromName { |name|
		^all[ name.asSymbol ];
	}
	
	name { ^all.findKeyForValue( this ); }
	name_ { |name|
		all.removeAt( this.name );
		all[ name.asSymbol ] = this;
		presetManager.id_( (this.class.name ++ "_" ++ name).asSymbol );
	}
	
	init { |bandPairs|
		var methods;
		
		bandPairs = bandPairs.clump(2).flop;
		
		names = bandPairs[0];
		classes = bandPairs[1];
		
		methods = bandPairs[1].collect({ |cl|
			var method;
			method = cl.class.findRespondingMethodFor( \coeffs );
			if( method.isNil ) {
				"%:init - method *coeffs not found for %\n"
					.format( this.class, cl )
					.warn;
			};
			method;
		});
		
		argNames = methods.collect({ |method|
			(method.argNames ? [])[2..].as(Array);
		});
		
		defaultSetting = methods.collect({ |method, i|
			if( method.notNil ) {
				method.prototypeFrame[2 .. argNames[i].size + 1]
			} {
				[]
			};
		});
		
		specs = argNames.collect({ |names|
			names.collect({ |name|
				specDict[ name ];
			});
		});
		
		presetManager = PresetManager( this, [ \default, { this.defaultSetting } ] )
			.getFunc_({ |obj| obj.setting.deepCopy })
			.applyFunc_({ |object, preset|
			 	object.setting = preset;
		 	});
		
	}
	
	formatSetting { |setting|
		^this.constrainSetting( this.parseSetting( setting ) );
	}
	
	parseSetting { |setting|
		var default;
		default = this.defaultSetting.deepCopy;
		if( setting.isNil ) {
			^default;
		} {
			if( setting[0].size == 0 ) { // from flat setting
				setting = setting.clumps( default.collect(_.size) );
			};
			^default.collect({ |item, i|
				item.collect({ |subItem, ii|
					(setting[i] ? item).asCollection[ii] ? subItem
				});
			});
		};
	}
	
	constrainSetting { |setting|
		// setting needs to be parsed first
		^setting.collect({ |item, i|
			item.collect({ |subItem, ii|
				if( specs[i][ii].notNil ) {
					specs[i][ii].constrain( subItem );
				} {
					subItem;
				};
			});
		});
	}
	
	indexOf { |name, argName|
		var nameIndex, argNameIndex;
		
		if( name.isNumber ) {
			nameIndex = name;
		} {
			nameIndex = names.indexOf( name );
		};
		
		if( nameIndex.isNil ) {
			"%:indexOf - name '%' not found\n"
				.format( this.class, name )
				.warn;
			^[ nil, nil ];
		} {
			if( argName.isNumber ) {
				argNameIndex = argName;
			} {
				argNameIndex = argNames[ nameIndex ].indexOf( argName );
			};
			
			if( argNameIndex.isNil ) {
				^[ nameIndex, nil ];
			} {
				^[ nameIndex, argNameIndex ];
			};
		};
	}
	
	flatIndexOf { |name, argName|
		var argIndex;
		#name, argIndex = this.indexOf( name, argName );
		if( argIndex.notNil ) {
			^(argNames[..name-1].collect(_.size).sum) + argIndex;
		} {
			if( argName.isNil ) {
				^(argNames[..name-1].collect(_.size).sum) + 
					argNames[name].collect({ |item, i| i });
			} {
				^nil;
			};
		};
	}
	
	constrain { |name, argName, value|
		#name, argName = this.indexOf( name, argName );
		if( specs[ name ][ argName ].notNil ) {
			^specs[ name ][ argName ].constrain( value );
		} {
			^value
		};
	}
	
}

EQSetting {
	
	classvar <>global;
	
	var <def, <setting;
	var <>action;
	
	*initClass {
		Class.initClassTree(EQdef);
		global = this.new;
	}
	
	*new { |def, setting|
		^this.newCopyArgs( def, setting ).init;
	}
	
	fromPreset { |name|
		this.getEQdef.presetManager.apply( name, this );
	}
	
	getEQdef {
		var eqdef;
		
		if( def.isKindOf( EQdef ) ) {
			eqdef = def;
			def = def.name;
			if( def.isNil ) { def = eqdef };
		} {
			eqdef = EQdef.all[ def ];
			if( eqdef.isNil ) { eqdef = EQdef.fromKey( \default ) };
		};
		
		^eqdef;
	}
	
	init {
		var eqdef;
		
		def = def ? \default;
		eqdef = this.getEQdef;
		setting = eqdef.formatSetting( setting );		
	}
	
	set { |name, argName, value, constrain = true, active = true|
		#name, argName = this.getEQdef.indexOf( name, argName );
		if( argName.notNil ) {
			if( constrain == true ) {
				value = this.getEQdef.constrain( name, argName, value );
			};
			setting[name][argName] = value;
			this.changed( \setting, this.names[name], this.argNames[name][argName], value );
			action.value( this );
		} {
			if( value.isArray ) {
				this.getEQdef.argNames[ name ].do({ |argName, i|
					if( value[i].notNil ) {
						this.set( name, argName, value[i], constrain, false );
					};
				});
				action.value( this );
			} {
				"%:set - value needs to be an array if no argName specified\n"
					.format( this.class )
					.postf;
			};
		};
	}
	
	get { |name, argName|
		var argIndex;
		#name, argIndex = this.getEQdef.indexOf( name, argName );
		if( argIndex.notNil ) {
			^setting[name][argIndex];
		} {
			if( name.notNil ) {
				if( argName.isNil ) {
					^setting[name];
				} {
					^nil;
				};
			} {
				^nil;
			};
		};
	}
	
	at { |name, argName|
		^this.get( name, argName );
	}
	
	put { |...args|
		var value;
		value = args.pop;
		args = args.extend( 2, nil );
		this.set( args[0], args[1], value );
	}
	
	setting_ { |new| 
		setting = this.getEQdef.formatSetting( new );
		this.changed( \setting );
		action.value( this );
	}
	
	 names { ^this.getEQdef.names }
	 classes { ^this.getEQdef.classes }
	 argNames { ^this.getEQdef.argNames }	
	 defaultSetting { ^this.getEQdef.defaultSetting }
	 specs { ^this.getEQdef.specs }
	 
	 asUGenInput { ^setting.flat.asUGenInput }
	 asControlInput { ^setting.flat.asControlInput }
	 asOSCArgEmbeddedArray { | array| ^setting.flat.asOSCArgEmbeddedArray(array) }
	 
	 doesNotUnderstand { |selector ...args|
		 var split, ids;
		 split = selector.asString.split( $_ ).select({|x|x.size>0}).collect(_.asSymbol);
		 ids = this.getEQdef.indexOf( *split );
		 if( ids[0].notNil ) {
			 if( selector.isSetter ) {
				 ^this.set( ids[0], ids[1], *args)
			 } {
				 ^this.get( ids[0], ids[1] );
			 };
		 };
	 }
	 
	 *ar { |in, setting, def|
		^this.new( def ).ar( in, setting );
	}
	 
	 ar { |in, setting|
		var eqdef;
	 	setting = setting.flat;
	 	eqdef = this.getEQdef;
		this.classes.do({ |class, i|
			in = class.ar( in, *setting[ eqdef.flatIndexOf( i ) ] );
		});
		^in;
	 }
	 
	 magResponses { |freqs|
		 ^this.classes.collect({ |class, i|
			class.magResponse( freqs ? 1000, 44100, *setting[i] );
		});
	 }
	 
	 storeArgs { ^[ def, setting ] }
	 
	 gui { |parent, bounds|
		 ^EQView( parent, bounds, this ); 
	 }

}