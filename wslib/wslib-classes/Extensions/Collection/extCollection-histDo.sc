// wslib 2006

+ Collection {
	
	// I find this very useful, but maybe there already is something like this
	// somewhere in the lib?

	histDo { arg function;
		
		var i=0;
		
		// use
		// [...].histDo({ |item, prevItem, i| ...  });
		
		while ({ i < this.size }, {
			function.value(this[i], this[i-1], i);
			i = i + 1;
		})
	
		}
		
	 clumpSubsequent { |delta = 1| 
	  
	  	// group numbers spaced *delta* from each other:
	  	// [1,2,4,5,6].clumpSubsequent => [[1,2],[4,5,6]]
	  	
		var list, sublist;
		list = Array.new;
		sublist = this.species.new;
		this.histDo({ arg item, prevItem;
			if( prevItem.notNil )
				{ if( ( item - prevItem ) == delta )
					{ sublist = sublist.add(item); }
				 	{ list = list.add(sublist);
					  sublist = this.species.new.add(item); }
				 } { sublist = sublist.add(item); };
			});
			
		if( sublist.size > 0 )
			{ list = list.add(sublist); };
		^list;
		}
			
	}