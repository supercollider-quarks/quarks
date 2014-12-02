/*
"And here there dawned on me the notion that we must admit, in some sense, a fourth dimension of space for the purpose of calculating with triples ... An electric circuit seemed to close, and a spark flashed forth." Hamilton, Dublin Philosophical Magazine and Journal of Science, vol. xxv (1844), pp 489Ð95.
*/

Quaternion {
	var <a, <b, <c, <d;
	
	*new { |a, b, c, d| 
		^super.newCopyArgs(a, b, c, d) 
	}
	
	*rand { |min, max|
		^this.new(*{ rrand(min, max) } ! 4);		
	}
	
	coordinates {
		^[a, b, c, d]	
	}
	
	* { |other|
		var a2, b2, c2, d2;
		
		if(other.isKindOf(Quaternion).not) {
			 ^other.performBinaryOpOnQuaternion('*', this)
		};
		
		#a2, b2, c2, d2 = other.coordinates;
	
		^Quaternion(
		   	(a * a2) - (b * b2) - (c * c2) - (d * d2),
    			(a * b2) + (b * a2) + (c * d2) - (d * c2),
    			(a * c2) - (b * d2) + (c * a2) + (d * b2),
    			(a * d2) + (b * c2) - (c * b2) + (d * a2)
    		) 
	}
	
	/ { |other|
		if(other.isKindOf(Quaternion).not) {
			 ^other.performBinaryOpOnQuaternion('/', this)
		};
		^this * other.reciprocal	
	}
	
	+ { |other|
		var a2, b2, c2, d2;
		
		if(other.isKindOf(Quaternion).not) {
			 ^other.performBinaryOpOnQuaternion('+', this)
		};
		
		#a2, b2, c2, d2 = other.coordinates;
		
		^Quaternion(a + other.a, b + other.b, c + other.c, d + other.d)
	}
	
	- { |other|
		var a2, b2, c2, d2;
		if(other.isKindOf(Quaternion).not) {
			 ^other.performBinaryOpOnQuaternion('-', this)
		};
		
		#a2, b2, c2, d2 = other.coordinates;
		
		^Quaternion(a - other.a, b - other.b, c - other.c, d - other.d)
	}
	
	conjugate {
		var i = Quaternion(0.0, 1.0, 0.0, 0.0);
		var j = Quaternion(0.0, 0.0, 1.0, 0.0);
		var k = Quaternion(0.0, 0.0, 0.0, 1.0);
		^(this + (i * this * i) + (j * this * j) + (k * this * k)) * -0.5
	}
	
	norm {
		^sqrt(sum(collect(this.coordinates, _.squared)))
	}
	
	reciprocal {
		^conjugate(this) * reciprocal(squared(norm(this)))
	}
	
	distance { |other|
		^norm(this - other)
	}
	
	// double dispatch
	performBinaryOpOnSimpleNumber { arg aSelector, aNumber, adverb;
		^aNumber.asQuaternion.perform(aSelector, this, adverb)
	}
	performBinaryOpOnQuaternion { arg aSelector, aNumber, adverb;
		^error("Math operation failed.\n")
	}
	
	
	== { |other|
		^other.asQuaternion.coordinates == this.coordinates
	}
	
	hash {
		^this.coordinates.hash	
	}
	
	asQuaternion {
		^this	
	}
	
	printOn { arg stream;
		stream << "Quaternion(" << a << ", " << b << ", " << c << ", " << d << ")";
	}
}

+ SimpleNumber {
	asQuaternion {
		^Quaternion(this, 0.0, 0.0, 0.0)
	}
	
	performBinaryOpOnQuaternion { arg aSelector, quat, adverb; 
		^quat.perform(aSelector, this.asQuaternion, adverb) 
	}
	
}

+ Complex {
	asQuaternion {
		^Quaternion(this.real, this.imag, 0.0, 0.0)
	}
	performBinaryOpOnQuaternion { arg aSelector, quat, adverb; 
		^quat.perform(aSelector, this.asQuaternion, adverb) 
	}
}