//redFrik

RedBencode {
	*encode {|array|
		var res= "";
		array.do{|val, i|
			case
			{val.isInteger} {//integer
				res= res++$i++val++$e;
			}
			{val.isString} {//string
				res= res++val.size++$:++val;
			}
			{val.isKindOf(Set)} {//dictionary
				res= res++$d;
				val.asSortedArray.do{|a| res= res++this.encode([a[0].asString, a[1]])};
				res= res++$e;
			}
			{val.isSequenceableCollection} {//list
				res= res++$l++this.encode(val)++$e;
			}
			{(this.class.name++": error encoding"+val+"at index"+i).error}
		};
		^res;
	}
	*encodeBytes {|array|
		var str= this.encode(array);
		^Int8Array.fill(str.size, {|i| str[i].ascii});
	}
	*decodeBytes {|array|
		^this.decodeString(String.fill(array.size, {|i| array[i].asAscii}));
	}
	*decodeString {|string|
		^this.decode(CollStream(string));
	}
	*decode {|stream|
		var running= true, state= 0, res= [];
		var str, chr, tmp, dict;
		while({running}, {
			chr= stream.next;
			running= chr.notNil;
			switch(state,
				0, {//new item
					case
					{chr.isNil} {
						running= false;
					}
					{chr==$i} {//integer
						state= 1;
						str= "";
					}
					{chr.isDecDigit} {//string length
						state= 2;
						str= ""++chr;
					}
					{chr==$l} {//list recursion call
						res= res.add(this.decode(stream));
					}
					{chr==$d} {//dictionary recursion call
						tmp= this.decode(stream);
						dict= Dictionary(tmp.size);
						tmp.pairsDo{|k, v| dict.put(k.asSymbol, v)};
						res= res.add(dict);
					}
					{chr==$e} {//end of list or dictionary
						running= false;
					}
					{(this.class.name++": error decoding"+chr+"at index"+stream.pos).error}
				},
				1, {//integer
					case
					{chr.isDecDigit or:{chr==$-}} {//integer digits or sign
						str= str++chr;
					}
					{chr==$e} {//end of integer
						res= res.add(str.asInteger);
						state= 0;
					}
					{(this.class.name++": error decoding integer"+chr+"at index"+stream.pos).error}
				},
				2, {//string
					case
					{chr.isDecDigit} {//string length
						str= str++chr;
					}
					{chr==$:} {//string delimiter and content
						res= res.add(stream.nextN(str.asInteger));
						state= 0;
					}
					{(this.class.name++": error decoding string"+chr+"at index"+stream.pos).error}
				}
			);
		});
		^res;
	}
}
