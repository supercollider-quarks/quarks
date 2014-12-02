
// this was intended to allow replacement of child streams on the fly
// but it is really only of limited use
// i should delete it but oh well...

PbindFuncStream : FuncStream {
	var	<>array;
	*new { arg nextFunc, resetFunc, array;
		^super.new(nextFunc, resetFunc).array_(array)
	}
	next { arg inval;
		^nextFunc.value(inval, array)
	}
	reset { 
		^resetFunc.value(array)
	}
	at { |index| ^array[index] }
	put { |index, value| array.put(index, value.asStream) }
		// array is assumed to be streampairs a la Pbind
	atKey { |key|
		// error should be thrown if you reference a key that isn't there
		// error will be '+' not understood
		^array[this.keyIndex(key) + 1]
	}
	putKey { |key, value|
		array.put(this.keyIndex(key) + 1, value.asStream)
	}
	keyIndex { |key|
		var i, j = 0, endval;
		endval = array.size-1;
		{ i.isNil and: { j < endval }}.while({
				// equivalence, not identity -- because keys may be arrays
			(array[j] == key).if({ i = j });
			j = j + 2;
		});
		^i
	}
}
