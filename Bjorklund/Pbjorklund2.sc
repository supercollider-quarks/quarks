// by Juan A. Romero
// Based on the Bjorklund Quark
// gives ratios for durations instead of arrays with binaries

Pbjorklund2 : Pbjorklund {
	embedInStream {|inval|
		var kStr= k.asStream;
		var nStr= n.asStream;
		var kVal, nVal;
		length.value(inval).do{
			var outval, b;
			kVal= kStr.next(inval);
			nVal= nStr.next(inval);
			if(kVal.notNil and:{nVal.notNil}, {
				b= Pseq(Bjorklund2(kVal, nVal), 1, offset).asStream;
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
