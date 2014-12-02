// wslib 2012
// this not only checks for bad values, but also removes them from the audio/control stream for
// those cases where you don't know why your code is producing them, but you have to perform
// the next day..

RemoveBadValues {
	
	*ar { |in, post = 2|
		var res;
		res = in.asArray.collect({ |item, i|
			var good;
			good = BinaryOpUGen('==', CheckBadValues.ar(item, i, post), 0);
			Select.ar(good, [ DC.ar(0), item ]);
		});
		if( res.size > 1 ) {
			^res;
		} {
			^res[0];
		};
	}
	
	*kr { |in, post = 2|
		var res;
		res = in.asArray.collect({ |item, i|
			var good;
			good = BinaryOpUGen('==', CheckBadValues.kr(item, i, post), 0);
			Select.kr(good, [ DC.kr(0), item ]);
		});
		if( res.size > 1 ) {
			^res;
		} {
			^res[0];
		};
	}
	
}