// this file is part of redUniverse toolkit /redFrik


//--vector of any dimension
RedVector[float] : FloatArray {
	species {^this.class}
	mag {^this.sum{|x| x*x}.sqrt}
	distance {|redVec| ^(this-redVec).mag}
	manhattan {|redVec| ^(this-redVec).abs.sum}
	dot {|redVec| ^(this*redVec).sum}
	normalize {^this/this.mag}
	limit {|max| if(this.mag>max, {^this.normalize*max})}
}

//--2d vector optimised for speed
RedVector2D[float] : RedVector {
	mag {^this[0].sumsqr(this[1]).sqrt}
	distance {|redVec| ^(redVec[0]-this[0]).hypot(redVec[1]-this[1])}
	manhattan {|redVec| ^(this[0]-redVec[0]).abs+(this[1]-redVec[1]).abs}
	dot {|redVec| ^(this[0]*redVec[0])+(this[1]*redVec[1])}
	asPoint{^Point(this[0], this[1])}
	* {|item, adverb| if(item.isNumber, {^RedVector2D[this[0]*item, this[1]*item]}, {^this.performBinaryOp('*', item, adverb)})}
	/ {|item, adverb| if(item.isNumber, {^RedVector2D[this[0]/item, this[1]/item]}, {^this.performBinaryOp('/', item, adverb)})}
	+ {|item, adverb| if(item.isNumber, {^RedVector2D[this[0]+item, this[1]+item]}, {^this.performBinaryOp('+', item, adverb)})}
	- {|item, adverb| if(item.isNumber, {^RedVector2D[this[0]-item, this[1]-item]}, {^this.performBinaryOp('-', item, adverb)})}
}

//--3d vector optimised for speed
RedVector3D[float] : RedVector {
	mag {^(this[0].sumsqr(this[1])+this[2].pow(2)).sqrt}
	distance {|redVec| ^(redVec[0]-this[0]).hypot((redVec[1]-this[1]).hypot(redVec[2]-this[2]))}
	manhattan {|redVec| ^(this[0]-redVec[0]).abs+(this[1]-redVec[1]).abs+(this[2]-redVec[2]).abs}
	dot {|redVec| ^(this[0]*redVec[0])+(this[1]*redVec[1])+(this[2]*redVec[2])}
	cross {|redVec|
		var x1, y1, z1, x2, y2, z2;
		#x1, y1, z1= this;
		#x2, y2, z2= redVec;
		^RedVector3D[(y1*z2)-(z1*y2), (z1*x2)-(x1*z2), (x1*y2)-(y1*x2)];
	}
	* {|item, adverb| if(item.isNumber, {^RedVector3D[this[0]*item, this[1]*item, this[2]*item]}, {^this.performBinaryOp('*', item, adverb)})}
	/ {|item, adverb| if(item.isNumber, {^RedVector3D[this[0]/item, this[1]/item, this[2]/item]}, {^this.performBinaryOp('/', item, adverb)})}
	+ {|item, adverb| if(item.isNumber, {^RedVector3D[this[0]+item, this[1]+item, this[2]+item]}, {^this.performBinaryOp('+', item, adverb)})}
	- {|item, adverb| if(item.isNumber, {^RedVector3D[this[0]-item, this[1]-item, this[2]-item]}, {^this.performBinaryOp('-', item, adverb)})}
}
