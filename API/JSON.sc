
JSON {

	classvar <tab,<nl;

	*initClass {
		tab = [$\\,$\\,$t].as(String);
		nl = [$\\,$\\,$n].as(String);
	}
	*stringify { arg obj;
		var out;

		if(obj.isString, {
			^obj.asCompileString.replace("\n", JSON.nl).replace("\t", JSON.tab);
 		});
		if(obj.class === Symbol, {
			^JSON.stringify(obj.asString)
		});

		if(obj.isKindOf(Dictionary), {
			out = List.new;
			obj.keysValuesDo({ arg key, value;
				out.add( key.asString.asCompileString ++ ":" + JSON.stringify(value) );
			});
			^("{" ++ (out.join(",")) ++ "}");
		});

		if(obj.isNil, {
			^"null"
		});
		if(obj === true, {
			^"true"
		});
		if(obj === false, {
			^"false"
		});
		if(obj.isNumber, {
			if(obj.isNaN, {
				^"NaN"
			});
			if(obj === inf, {
				^"Infinity"
			});
			if(obj === (-inf), {
				^"-Infinity"
			});
			^obj.asString
		});
		if(obj.isKindOf(SequenceableCollection), {
			^"[" ++ obj.collect({ arg sub;
						JSON.stringify(sub)
					}).join(",")
				++ "]";
		});

		// obj.asDictionary -> key value all of its members

		// datetime
		// "2010-04-20T20:08:21.634121"
		// http://en.wikipedia.org/wiki/ISO_8601

		("No JSON conversion for object" + obj).warn;
		^JSON.stringify(obj.asCompileString)
	}


	/*

	*parse { arg string;


	}

	getQuotedTextIndices {|quoteChar = "\""|
		var quoteIndices;

		quoteIndices = this.findAll(quoteChar.asString);
		// remove backquoted chars
		quoteIndices = quoteIndices.select{|idx, i|
			this[idx-1] != $\\
		} ?? {[]};

		^quoteIndices.clump(2);
	}

	getUnquotedTextIndices {|quoteChar = "\""|
		^((([-1] ++ this.getQuotedTextIndices(quoteChar).flatten ++ [this.size]).clump(2)) +.t #[1, -1])
	}

	getStructuredTextIndices {
		var unquotedTextIndices;

		unquotedTextIndices = this.getUnquotedTextIndices;
		unquotedTextIndices = unquotedTextIndices.collect{|idxs|
			this.copyRange(*idxs).getUnquotedTextIndices($') + idxs.first
		}.flat.clump(2);

		^unquotedTextIndices
	}

	prepareForJSonDict {
		var newString = this.deepCopy;
		var idxs, nullIdxs;
		idxs = newString.getStructuredTextIndices;

		idxs.do{|pairs, i|
			Interval(*pairs).do{|idx|
				(newString[idx] == ${).if({newString[idx] = $(});
				(newString[idx] == $}).if({newString[idx] = $)});

				(newString[idx] == $:).if({
					[(idxs[i-1].last)+1, pairs.first-1].do{|quoteIdx|
						newString[quoteIdx] = $'
					}
				});
			}
		};

		// replace null with nil
		nullIdxs = newString.findAll("null");
		nullIdxs.do{|idx|
			idxs.any{|pairs| idx.inRange(*pairs)}.if({
				newString.overWrite("nil ", idx);
			})

		};

		^newString
	}

	jsonToDict {
		^(this.prepareForJSonDict.interpret)
	}
	*/

}