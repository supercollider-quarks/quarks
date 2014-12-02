//+ String {
//	
//	getQuotedTextIndices {|quoteChar = "\""|
//		var quoteIndices;
//		
//		quoteIndices = this.findAll(quoteChar.asString);
//		// remove backquoted chars
//		quoteIndices = quoteIndices.select{|idx, i|
//			this[idx-1] != $\\
//		} ?? {[]};
//		
//		^quoteIndices.clump(2);
//	}
//		
//	getUnquotedTextIndices {|quoteChar = "\""|
//		^((([-1] ++ this.getQuotedTextIndices(quoteChar).flatten ++ [this.size]).clump(2)) +.t #[1, -1])
//	}
//
//	getStructuredTextIndices {
//		var unquotedTextIndices;
//		
//		unquotedTextIndices = this.getUnquotedTextIndices;
//		unquotedTextIndices = unquotedTextIndices.collect{|idxs| 
//			this.copyRange(*idxs).getUnquotedTextIndices($') + idxs.first
//		}.flat.clump(2);
//		
//		^unquotedTextIndices
//	}
//
//	prepareForJSonDict {
//		var newString = this.deepCopy;
//		var idxs, nullIdxs;
//		idxs = newString.getStructuredTextIndices;
//	
//	
//		idxs.do{|pairs, i|
//			Interval(*pairs).do{|idx|
//				(newString[idx] == ${).if({newString[idx] = $(});
//				(newString[idx] == $}).if({newString[idx] = $)});
//			
//				(newString[idx] == $:).if({
//					[(idxs[i-1].last)+1, pairs.first-1].do{|quoteIdx|
//						newString[quoteIdx] = $'
//					}
//				});
//			}
//		};
//		
//		// replace null with nil
//		nullIdxs = newString.findAll("null");
//		nullIdxs.do{|idx|
//			idxs.any{|pairs| idx.inRange(*pairs)}.if({
//				newString.overWrite("nil ", idx);
//			})
//
//		};
//
//		^newString
//	}
//
//	jsonToDict {
//		^(this.prepareForJSonDict.interpret)
//	}
//
//}