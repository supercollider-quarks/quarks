// wslib 2005

+ SimpleNumber { 
		
	breakUp { |div| // sum equals input
		// 2.1.breakUp == [1, 1, 0.1]
		// 2.1.breakUp(2) == [1.05, 1.05]
		var outArray = [];
		div = div ? this;
		div.ceil.do({ |i|
			outArray = outArray.add((this / div).min(this - outArray.sum));
			});
		^outArray;
		}
}

+ SequenceableCollection {
	breakUp { |div| 
		if(div.size > 0)
			{^this.collect({ |x,i| x.breakUp(div.at(i)) }); }
			{^this.collect( _.breakUp(div) );} 
		}
	}