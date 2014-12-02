Formatter {
	classvar <typeDict;
	
	var <>dict;
	
	*initClass {
		typeDict = (
			m: [  ( id: "m", alternatives: [ "meter", "mtr" ] ),
				  ( id: "cm", alternatives: [ "centimeter" ], mul: 100 ),
				  ( id: "mm", alternatives: [ "millimeter" ], mul: 1000 ),
				  ( id: "km", alternatives: [ "kilometer" ], mul: 1/1000 ),
				  ( id: "in", alternatives: [ "inch, i" ], mul: 39.370079 ),
				  ( id: "mi", alternatives: [ "mile", "miles" ], mul: 1/1609.344 ),
				  ( id: "yd", alternatives: [ "yard", "yards" ], mul: 1.0936133 )
				],
			deg: [ ( id: "¡".asciiCorrect, alternatives: [ "deg" ] ),
			  	   ( id: "rad", mul: 2pi/360 ),
			  	   ( id: "pi", mul: 2/360, default: 1 )
				]
			);
		}
	
	*new { |dict|
		case { dict.class == Symbol }
			{ dict = typeDict[ dict ]; }
			{ dict.class == String }
			{ dict = typeDict[ dict.asSymbol ]; }
		^super.newCopyArgs( dict );
		}
	
	identifiers { ^dict.collect( _.id ) ++ dict.collect( _.alternatives ).flatten(1); }
	
	from { |str, prefID, verbose = false|
		var foundTypes, type, typeFuncs, val, default;
		prefID = prefID ?? { dict.first[\id] };
		str = str.trimRight( " " );
		
		this.identifiers.do({ |id| // collect possible matches
			var i;
			i = str.findBackwards( id );
			if( i == (str.size - id.size))
				{ foundTypes = foundTypes ++ [ id ]; }
			});
			
		if( foundTypes.notNil )
			{ type = foundTypes.sort({|a,b| a.size <= b.size }).last; // find longest match
			  str = str[..((str.size - type.size)-1)]; // remove it from string
			} { type = prefID.asString; };
			
		if( verbose ) { (this.class.asString + "chose" + type + "from" + foundTypes).postln; };
		
		typeFuncs = dict.detect({ |item| (item[ \id ] == type) or: 
			{ item[\alternatives].asCollection.includes( type ) }; });
			
		default = typeFuncs[ \default ] ? dict.first[\default] ? 0;
		
		val = (typeFuncs[ \interpretFunc ] ?? { { |str| str.interpret; } }).value( str );
		^(typeFuncs[ \fromFunc ] ?? { { |val|
			 (val ? default) / (typeFuncs[ \mul ] ? 1) } }).value( val );
		}
		
	to { |val, round, type|
		var typeFuncs, default;
		if( type.isNil )
			{ type = dict.first[\id];
			  typeFuncs = dict.first; }
			{ typeFuncs = dict.detect({ |item| (item[ \id ] == type) or: 
			  { item[\alternatives].asCollection.includes( type ) }; }) ?? {()};
			};
		
		default = typeFuncs[ \default ] ? dict.first[\default] ? 0;

		^(typeFuncs[ \toFunc ] ?? 
			{ { |val| ((val ? default) * (typeFuncs[ \mul ] ? 1)).round( round ? 0 ).asString 
				++ type } }
			).value( val );
		}
		
	fromto { |str, processFunc, round, type| // reformat
		var val;
		val = this.from( str, type );
		processFunc !? { val = processFunc.value( val ) };
		^this.to( val, round, type );
		}
	
	}
	 
		