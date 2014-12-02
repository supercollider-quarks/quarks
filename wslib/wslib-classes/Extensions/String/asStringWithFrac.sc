+ Integer {
	asStringWithFrac { |fracSize = 3|
		^(this.asString ++ ".".extend( fracSize+1, $0 ));
		}
	}
	
+ Float {
	asStringWithFrac { |fracSize = 3|
		var val;
		val = this.round( 10 ** (fracSize.neg) );
		^(val.asInt.asString ++ "." ++ 
			( val.abs.frac* (10**fracSize) ).round(1).asInt.asStringToBase(10,fracSize));
		}
	}
	
+ ArrayedCollection {
	preExtend { |size, item| 
		var arraySize;
		case { (arraySize = this.size) > size } {
			^this[ arraySize - size .. ];
		} { arraySize < size } {
			^(item!(size-arraySize)).as(this.class) ++ this;
		} { ^this };
	}
}