//redFrik

//--related: Bjorklund, Pbjorklund2

Pbjorklund : Pattern {
	var <>k= 4, n= 8, <>length= inf, offset= 0;
	*new {|k, n, length= inf, offset= 0|
		^super.newCopyArgs(k, n, length, offset);
	}
	storeArgs {^[k, n, length, offset]}
	embedInStream {|inval|
		var kStr= k.asStream;
		var nStr= n.asStream;
		var kVal, nVal;
		length.value(inval).do{
			var outval, b;
			kVal= kStr.next(inval);
			nVal= nStr.next(inval);
			if(kVal.notNil and:{nVal.notNil}, {
				b= Pseq(Bjorklund(kVal, nVal), 1, offset).asStream;
				while({outval= b.next; outval.notNil}, {
					inval= outval.yield;
				});
			}, {
				inval= nil.yield;
			});
		};
		^inval;
	}
}

/*
Pbjorklund {
	*new {|k, n, repeats= 1, offset= 0|
		^Pseq(Bjorklund(k, n), repeats, offset);
	}
}
*/