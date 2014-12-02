// ©2009 Miguel Negr‹o, Fredrik Olofsson
// GPLv3 - http://www.gnu.org/licenses/gpl-3.0.html

//Vector class for vectors of any dimension, with optimized classes for 2D and 3D.

AbstractVector[slot] : ArrayedCollection {
	
	species { ^this.class }
	
	//usual notation up to 3D vectors
	x { ^this.at(0) }
	
	y { ^this.at(1) }
	
	z { ^this.at(2) }	
	
	//inner product
	<|> { |vec| 
		^this.subclassResponsibility 
	}
	
	norm {
		^(this <|> this).sqrt
	}
	
	dist { |vec|
		^(this - vec).norm
	}
	
	normalize {
		^this / this.norm
	}
	
	limit { |max|
		if(this.norm > max, { ^this.normalize * max })
	}
	
	isOrthogonal { |vector|
		^(this <|> vector) == 0
	}
	
	asAbstractVector { ^this }
	
	asPoint {
		^Point(this[0],this[1])
	}
	
	asRealVector {
		^RealVector[this.x, this.y]
	}

	asRealVector2D {
		^RealVector2D[this.x, this.y]
	}
	
	angle{ |vector|
		^acos((this<|>vector)/(this.norm*vector.norm))
	}
	
	transpose{
		^this.asMatrix.flop
	}
	
	asMatrix{
		^Matrix.with(this.collect{ |a| [a] })		
	}
	
	//project a vector onto this vector
	proj{ |vector|
		^this*(this<|>vector)/(this<|>this)
	}
	
	*gramSchmidt{ |vectors|
		
		^vectors.collect{ |vector1,j|
			vectors.do{ |vector2,k|
				if(j<k){
					vector1 = vector1 - vector2.proj(vector1)
				}
			};
			vector1.normalize;
		}		
	}
		
}

// for vectors in R^N
RealVector[slot] : AbstractVector {
	
	species {^this.class}

	*canonB { |i,size|
		^this.newFrom(size.collect { |j| (i == j).binaryValue })
	}

	*rand { |size = 2, lo = 0.0, hi = 1.0|
		^this.newFrom(size.collect { rrand(lo, hi) })
	}
		
	*rand2D { |xlo, xhi, ylo, yhi|
		^this.newFrom([rrand(xlo, xhi), rrand(ylo, yhi)])
	}	
	
	//inner product
	<|> { |vec|	
		var size = this.size;
		
		if(size == 2) {
			^(this[0] * vec[0]) + (this[1] * vec[1])
		}{
			if(size == 3) {
				^(this[0] * vec[0]) + (this[1] * vec[1]) + (this[2] * vec[2])
			}{
				^this.size.collect { |i|
					this[i] * vec[i]
				}.sum
			}
		}
	}
	
	//outerProduct
	cross { |vector| 
		var a1, a2, a3, b1, b2, b3;
		#a1, a2, a3 = this;
		#b1, b2, b3 = vector;
		^this.class.newFrom([(a2 * b3) - (a3 * b2), (a3 * b1) - (a1 * b3), (a1 * b2) - (a2 * b1)])
	}

	norm {
		var size = this.size;
		
		if(size == 2) {
			^this[0].sumsqr(this[1]).sqrt
		}{
			if(size == 3){
				^(this[0].sumsqr(this[1])+this[2].pow(2)).sqrt
			}{
				^this.sum { |x| x * x }.sqrt
			}
		}
	}
	
	dist { |vec|
		var size = this.size;
		
		if(size == 2){
			^(vec[0]-this[0]).hypot(vec[1]-this[1])
		}{
			if(size == 3){
				^(vec[0]-this[0]).hypot((vec[1]-this[1]).hypot(vec[2]-this[2]))
			}{
				^(this-vec).norm
			}
		}
	}
	
	theta { 
		^atan2(this.at(1), this.at(0))
	}
	
}

//vectors in C^N
ComplexVector[slot] : AbstractVector {
	
	<|> { |vec|
		^this.sum { |item, i|
			item * vec[i].conjugate
		}.abs
	}
}

//--2d vector optimised for speed, around 10% faster.
RealVector2D[slot] : RealVector {
	
	norm {
		^this[0].sumsqr(this[1]).sqrt
	}
	
	dist { |vec| 
		^(vec[0] - this[0]).hypot(vec[1] - this[1])
	}
	
	<|> { |vec| 
		^(this[0] * vec[0]) + (this[1] * vec[1])
	}
	
	asRealVector2D { ^this }
	
}

//--3d vector optimised for speed, around 10% faster.
RealVector3D[slot] : RealVector {
	
	norm {
		^(this[0].sumsqr(this[1]) + this[2].pow(2)).sqrt
	}
	
	dist { |vec| 
		^(vec[0] - this[0]).hypot((vec[1] - this[1]).hypot(vec[2] - this[2]))
	}
	
	<|> { |vec| 
		^(this[0] * vec[0]) + (this[1] * vec[1]) + (this[2] * vec[2])
	}

	rotate { arg angle; // XY-plane, in radians
		var sinr, cosr;
		sinr = angle.sin;
		cosr = angle.cos;
		^RealVector3D[(this[0] * cosr) - (this[1] * sinr), (this[0] * sinr) + (this[1] * cosr), this[2]]
	}

	phi { ^atan2(this[2], (this[0].squared + this[1].squared).sqrt) }

	asRealVector3D{ ^this }

}

+ Point {	
	
	asRealVector {
		^RealVector[x, y]
	}
	
	asRealVector2D {
		^RealVector2D[x, y]
	}
}

+ SimpleNumber {

    asRealVector3D{
        ^3.collect{ this }.as(RealVector3D)
    }

}