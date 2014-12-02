//redFrik

//--related: Pbjorklund, Bjorklund2

Bjorklund {
	*new {|k, n|
		^this.fromArray([1].dup(k)++[0].dup(n-k));
	}
	*fromArray {|arr|
		^this.prFromArray(arr.collect{|x| x.asCollection});
	}

	//--private
	*prFromArray {|arr|
		var a, b;
		#a, b= this.prSplit(arr);	//#a, b= arr.separate(_ != _); //optional simplification by jhr
		if(b.size>1 and:{a.size>0}, {
			^this.prFromArray(this.prLace(a, b));
		}, {
			^(a++b).flat;
		});
	}
	*prSplit {|arr|
		var item= arr[arr.size-1];
		var index= arr.indexOfEqual(item);
		^[
			arr.copyRange(0, index-1),
			arr.copyRange(index, arr.size-1)
		];
	}
	*prLace {|a, b|
		^a.collect{|x, i| x++b[i]}++b.copyRange(a.size, b.size-1);
	}
}
