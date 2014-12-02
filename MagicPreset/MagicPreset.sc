

MagicPreset {
	classvar
		prefix = "/*___MAGIC_PRESET(",
		suffix = "*/",
		separatorChar = $);

	*at {
		arg key, doc = Document.current;
		var docstr, strs, bounds;
		
		key = key.asString;
		MagicPreset.prCheckKey(key);
		
		docstr = doc.string;
		bounds = MagicPreset.prFindPreset(docstr,key);

		if (bounds.notNil) {
			^docstr[bounds[0]..bounds[1]].interpret;
		};
		^nil;
	}
	
	*put {
		arg key, value, doc = Document.current;
		var docstr, strs, assoc, bounds;

		key = key.asString;
		MagicPreset.prCheckKey(key);

		docstr = doc.string;
		
		bounds = MagicPreset.prFindPreset(docstr,key);

		if (bounds.notNil) {
			doc.string_(value.asCompileString++" ",bounds[0]+1,bounds[1]-bounds[0]);
		} {
			doc.string_(docstr[docstr.size-1] ++ "\n" ++ prefix ++ 
				key ++ ") " ++ value.asCompileString ++ " " ++ suffix,docstr.size);
		};
		^value;
	}
		
	*prFindPreset {
		arg docstr, key;
		var str, separator, starts,ends;
		starts = docstr.findAll(prefix);
		if (starts.notNil) {
			starts = starts + prefix.size;
			ends = docstr.findAll(suffix)-1;
			starts.do {
				arg start;
				var end;
				end = ends[ends.indexOfGreaterThan(start)];
				if (end.notNil) {
					str = docstr[start..end];
					separator = str.indexOf(separatorChar);
					if (separator.notNil && str[..separator-1] == key) {
						^[start+separator+1,end];
					}
				}
			}
		};
		^nil
	}
		
			
	*prCheckKey {
		arg key;
		if (key.indexOf(separatorChar).notNil) {
			"MagicPreset key cannot contain parentheses".error;
		}
	}
}
