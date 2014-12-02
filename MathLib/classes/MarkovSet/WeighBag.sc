/* 
______________________________________________________________________________________________

WeighBag works similar to Bag: it contains every kind of element only once 
(simple equality == ) counting the instances and calculating an array of associated weights.
it is used as Element in MarkovSet.

____________________________________________________________________________________________
Julian Rohrhuber 2003 
*/



WeighBag  { 
	
	var <items, <weights, <counts, <numItems=0;
	
	//counts are kept to allow efficient weighing
				
				
	*new { 
		//^super.newCopyArgs(Array.new,Array.new,Array.new) 
		^super.new.init;
	}
	
	init {
		items = Array.new;
		weights = Array.new;
		counts = Array.new;
	}
	
	*with { arg items, weights;
		var n;
		items = items.asCollection;
		n = items.size;
		weights = if(weights.isNil)
		{  Array.fill(n, items.size.reciprocal) }
		{ weights.normalizeSum };
		^super.newCopyArgs(items, weights, Array.fill(n, 1), n);
	}
	
	count { arg sum;
			counts = weights.collect { arg item; (item * sum).asInteger };
			numItems = counts.sum;
	}
	
	at { arg index;  ^items.at(index) }
	wrapAt { arg index;  ^items.wrapAt(index) }
	
	indexOf { arg item;
		//^items.indexOfEqual(item);
		items.do({ arg elem, i;
			if ((item.class === elem.class) and: { item == elem }, { ^i })
		});
		^nil
	}
	
	add { arg item, ntimes=1;
			var index, n; 
			index = this.indexOf(item);
			if( index.isNil, { 
				items = items.add(item);
				counts = counts.add(ntimes);
			}, {
				n = counts.at(index);
				counts.put(index, n + ntimes);
			});
			numItems = numItems + ntimes;
			this.weigh;
			
	}
	
	weigh { weights = counts / numItems }
	
	remove { arg item, ntimes=1;
			var index, n, count;
			index = items.indexOf(item);
			^if(index.notNil, {
				n = counts.at(index);
				count = n - ntimes;
				if(count > 0, {
					counts.put(index, count);
					numItems = numItems - ntimes;
				}, {
					items.removeAt(index);
					counts.removeAt(index);
					numItems = numItems - n;
				});
				this.weigh;
				item;
			}, { 
				nil 
			});
			
	}
	
	removeAll { arg item;
			var index, n;
			index = items.indexOf(item);
			if(index.notNil, {
					items.removeAt(index);
					n = counts.at(index);
					counts.removeAt(index);
					numItems = numItems - n;
					this.weigh;
			});
			
	}
	
	/*
	equalize { var n, rec; 
		n = weights.size;
		rec = n.reciprocal;
		weights = Array.fill(n, rec) 
	} 
	*/
	
	manipulate { arg func; 
				counts = counts.collect({ arg count, i;
					func.value(items.at(i), count)
				
				});
				this.weigh; 
	}
	
	collect { arg func; 
		items = items.collect({ arg item, i; func.value(item, i) }); 
	}
	
	wchoose { 		
		^items.wchoose(weights) 
	}
	
	choose { 		
		^items.wchoose(weights) 
	}

	
	
	// returns an array of data each time it is called. [item, weight, number of items]
	infoChoose { var index; 
				index = weights.windex; 
				^[items.at(index), weights.at(index)] 
	}
	
	size { ^items.size }
	
	
		
	++ { arg bag;
			var newitems, n, counts;
			newitems = bag.items;
			counts = bag.counts; 
			newitems.do({ arg item, i; 
				this.add(item, counts.at(i)) 
				});
			^this
	}
	
	printOn { arg stream;
			stream << "[" ;
			this.items.printOn(stream);
			stream << ", ";
			this.weights.printOn(stream);
			stream << ", ";
			this.counts.printOn(stream);
			stream << "]";
			
	}
	storeOn { arg stream;
			stream << this.class.name << ".with(" ;
			this.items.storeOn(stream);
			stream << ", ";
			this.weights.storeOn(stream);
			stream << ", ";
			this.counts.storeOn(stream);
			stream << ")";
			
			
	}
	
}


IdentityWeighBag : WeighBag {
	indexOf { arg item;
		^items.indexOf(item);
	}
	

}
