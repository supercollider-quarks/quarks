//////////
// when varying the order of the set is needed, this object is better than MarkovSet.
// the lookup speed is not constant, it decreases with a high number of elements
// adding data is extremely fast though. see ShannonFinger for a compromise
// 2003,  rohrhuber, carle, after a hint by de Campo / Ryan


ShannonMarkovSet {
	var <data, <>maxSearchLength;
	var <>notify=false;
	var <>seeds; // todo: seeds. also Pmswitch should use a standard interface to all msets
				// deterministicNext(prev, index)
				// nextSeed, deterministicNextSeed, embedSpyInStream (?)
				// don't work:  read(prev, next)
				// ++
	
	*new { arg size=8;
		^super.new.clear(size)
	}
	*fill { arg size, stream;
		^this.new(size).parse(stream, size);
	}
	clear { arg size;
		data = ClearList.newClear(size);
	}
	
	data_ { arg array;
		this.clear(0).addAll(array);
	}
	
	addAll { arg array;
		data.grow(array.size); 
		array.do({ arg item; this.add(item) });
	}
	
	add { arg item;
		this.put(data.size, item)
	}
	
	put { arg i, item;
		data.put(i, item)
	}
	
	wrapPut { arg i, item;
		this.put(i % data.array.size, item) // check again data.size?
	}
	
	parse { arg pattern, length;
		var stream;
		stream = pattern.asStream;
		data.grow(length);
		length.do { this.add(stream.value) };
	}
	
	next { arg object, order=1;
		var index, res;
		index = this.indexOf(object);
		if(index.isNil, { ^nil });
		res = this.nextIndex(index,order);
		
		^if(res.notNil, { data.at(res) }, { nil })
	
	}
	
	nextIndex { arg prevIndex, order=1;
		var index, b;
		b = this.chooseIndex;
		//("chosen: "++ b ).postln;
		index = this.scanForNextIndex(b, prevIndex, order);
		^if(index.notNil and: {index < data.size}, { 
				index + 1 
		}, {
				prevIndex + 1 //nil
		})
	}
	
	indexOf { arg obj;
		var randOffset;
		if(obj.isNil, { ^nil });
		randOffset = data.size.rand;
		(data.size - 1).do({ arg i;
			var o;
			i = (i + randOffset).wrap(0, data.size);
			[i].debug;
			o = data[i];
			if(o == obj, { ^i });
		});
		^nil
	}
	
	scanForNextIndex { arg index, prevIndex, order=1;
		var obj,  func, n;
		//"prevIndex: ".post; prevIndex.postln;
		if(index.isNil, { ^nil });
		n = data.size - 1;
		n = n.min(maxSearchLength ? n);
		if(order == 1, {
			obj = data.at(prevIndex);
			
			if(obj.isNil, { ^nil });
			n.do({ arg i;
				i = i + 1 + index;
				if(data.wrapAt(i) == obj, { ^i % data.size })
			});
			^nil
			
		}, {
			n.do({ arg i;
				var x;
				x = i + index + 1; //search index
				if(this.fitKey(x, prevIndex, order), { ^x % data.size });
			});
			^nil
			
		});
	}
	
	fitKey { arg index, prevIndex, order=1;
				var obj;
				obj = data.at(prevIndex);
				//"comparing with: ".post; obj.asSymbol.postln;
				if(obj.isNil, {  ^false });
				order.do({ arg offset;
					obj = data.wrapAt(prevIndex + offset);
					if((data.wrapAt(index + offset) == obj).not, { ^false })
				});
				^true
	}
	
	chooseIndex {
		var res, n;
		if(notify) { "random lookup due to lack of key".postln };
		res = data.size.rand;
		n = data.size;
		(maxSearchLength ? n).do({ arg i;
			var index;
			index = (i + res) % n;
			if(data.at(index).notNil, { ^index })
		});
		^nil
	}
	
	asStream { arg order=1, repeats=inf;
		^Routine({ arg inval;
			var index, orderStr, outval;
			orderStr = order.asStream;
			while({
				if(outval.isNil or: {index.isNil})
						{ index = this.chooseIndex } 
						{ index = this.nextIndex(index, orderStr.next) };
				
				index = index ?? { "choose index!".postln; this.chooseIndex }; // ??
				if(index.isNil) { inval = nil.yield };
				outval = data[index];
				outval.notNil
			}, {
				inval = outval.embedInStream(inval)
			});
		}).repeat(repeats);
	}
	
	
}


///////////faster lookup using a dictionary

ShannonFinger : ShannonMarkovSet {
	
	var <lookUp;
	
	clear { arg size;
		this.initDict;
		super.clear(size);
	}
	
	initDict {
		//lookUp = FuzzyDictionary.new;
		lookUp = FuzzySet.new;
	}


	put { arg index, obj;
		if(obj.notNil, {
			super.put(index, obj); 
			lookUp.put(obj.asSymbol, index)
		});
	}
	
	indexOf { arg obj;
		^lookUp.at(obj.asSymbol)
	}
	
	chooseIndex {
		if(notify) { "random lookup due to lack of key".postln };
		^lookUp.choose
	}
	
	nextIndex { arg prevIndex, order=1;
		var obj, index, list, randOffset, n;
		
		if( order > 1) {
			obj = data.at(prevIndex); // last data point
			list = lookUp.keyAt(obj.asSymbol); // where is this point in the lookUp?
			if(list.isNil) { ^prevIndex + 1 }; //nil: take next item
			n = list.size;
			if(maxSearchLength.notNil) { n = min(n, maxSearchLength) };
			randOffset = n.rand;
			
			n.do { arg i;
				var tryIndex;
				//tryIndex = list.wrapAt(i + randOffset);
				tryIndex = list.choose;
				if(
					(prevIndex != tryIndex) and: { 
						this.fitKey(tryIndex, prevIndex, order)
					}
				) 
				{ 
					^tryIndex + 1  
				}
			};
			//^nil
			^prevIndex + 1 // take next item
		} {
			obj = data.at(prevIndex+1);
			^lookUp.at(obj.asSymbol);
		
		};
	}
	
}
// use internally later

// pre cleared list
ClearList : List {
	var <size = 0;
	
	add { arg item; array = array.put(size, item); size = size + 1 }
	put { arg i, item;
		array.put(i, item); 
		size = max(size, i+1) 
	}
	insert { arg index, item; array = array.insert(index, item); size = size + 1 }
	removeAt { arg index; var res; 
		res = array.removeAt(index);
		array = array.add(nil);
		if(res.notNil) {size = size - 1 }; 
		^res 
	}
	pop { ^this.removeAt(size - 1) }
	do { arg function;
		this.size.do({ arg i; function.value(array[i],i) });
	}
	reverseDo { arg function;
		this.size.reverseDo({ arg i; function.value(array[i],i) });
	}
	
	grow { arg n=1;
		array = array ++ Array.newClear(n)
	}
}


