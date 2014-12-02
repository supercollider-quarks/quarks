ParsedString {

	/// parses strings into dictionary (Event) 
	
	// ParsedString( "a=1; b=2" ).dict
	// --> ( 'a': 1, 'b': 2 )
	
	// ParsedString( "a:1, b:2" ).dict
	// --> ( 'a': 1, 'b': 2 )
	
		
	var <string, <dict, <keys; // keys stay in ordered form
	var <>newKey = ",;";
	var <>newValue = ":="; 
	
	*new { | string = ""|
		var dict = (), keys = [];
		^super.newCopyArgs( string, dict, keys).parseString;
		}
		
	parseString {
		var pos = 0, fNewKey, fNewValue;
		var mode = \key, skipSpaces = true;
		var currentKey = "";
		var currentValue = "";
		dict = ();
		keys = [];
		fNewKey = newKey;
		fNewValue = newValue;
		
		while { pos < string.size }
			{ 
			case { mode == \key }
				{ if ( fNewValue.includes( string[pos] ) )
					{ 	fNewValue = string[pos].asString;
						mode = \value;
						keys = keys.add( currentKey.trim.asSymbol );
						//("found key" + currentKey + "; switch to value").postln;
						//("newValue =" + fNewValue).postln;
						pos = pos + 1;
					} { currentKey = currentKey ++ string[pos];
						pos = pos + 1;  };
				 } 
				 { mode == \value }
				 { if ( fNewKey.includes( string[pos] ) )
					{ 	fNewKey = string[pos].asString;
						mode = \key;
						dict[currentKey.trim.asSymbol] = currentValue.trim;
						//("found value" + currentValue + "; switch to new key").postln;
						//("newKey =" + fNewKey).postln;
						currentValue = "";
						currentKey = "";
						pos = pos + 1;
					} { currentValue = currentValue ++ string[pos];
						pos = pos + 1; };
				};
			
			};
		
		dict[currentKey.trim.asSymbol] = currentValue.trim;
			
		newKey = fNewKey;
		newValue = fNewValue;
		
		}
		
	renderString {
		var newString = "";
		keys.do({ |key, i|
			newString = newString ++ key ++ newValue[0] + dict[ key ];
			if( i < (keys.size - 1) )
				{ newString = newString ++ newKey[0] ++ " " };
			});
		string = newString;
			
		}
	
	}
	