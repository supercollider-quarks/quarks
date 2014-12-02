// Till Bovermann, Uni Bielefeld 2006
Trafo {
	var <matrix; // a Matrix in homogene coordinates
	
	*basicNew{|matrix|
		^super.new.init(matrix);
	}
	*newFromRotTrans{|rot, trans|
		var matrix;
		
		if (rot.isKindOf(Matrix), {
			matrix = rot.addCol(trans).addRow([0,0,0,1]);
		},{
			matrix = Matrix[
				rot[0] ++ trans[0],
				rot[1] ++ trans[1],
				rot[2] ++ trans[2],
				[0,0,0, 1]
			];
		})
		^this.basicNew(matrix)
	}
	*new{|basis, d|
		var matrix, fBasis;
		
		d = d ? (0!3);
		if (basis.isNil, {
			basis = [
				[1, 0, 0],
				[0, 1, 0],
				[0, 0, 1]
			];
		}, {
			basis = basis.collect{|vec| this.pr_normVec(vec)};
			if (basis.flatten.any{|item| item.isNaN}, {
				"Meta_Trafo-new: basis is collinear.".error;
				this.dumpBackTrace;
//				this.halt;
			});

		});
		
		fBasis = basis.flop;
		^this.newFromRotTrans(fBasis, d);
	}
	*from3dPoints{|a, b, c|
		// right-handed basis
		// a, b, c counter-clockwise
		// a is position-vector
		
		var x, y, z;

		// set up difference vecs, _not_ normalized.
		x = b-a;
		y = c-a; // at the moment y is _not_ orthogonal to x.

		// compute z and the real y (both not normalized)
		z = this.pr_cross(x, y);
		y = this.pr_cross(z, x);		
		
		^this.new	([x, y, z], a);
	}
	*inverseFrom3dPoints {|a, b, c|
		var x, y, z;

		#x, y, z = this.pr_Point3toBasis(a, b, c);

		a = a.collect{|elem, i| (x[i].neg*elem) + (y[i].neg*elem) + (z[i].neg*elem)};
		^this.basicNew(Matrix.with([x++a[0], y++a[1], z++a[2], [0,0,0,1]]));
	}	
	*pr_Point3toBasis {|a, b, c|
		var x, y, z;
		
		// set up difference vecs, _not_ normalized.
		x = b-a;
		y = c-a; // at the moment y is _not_ orthogonal to x.
		
		// compute z and the real y (both not normalized)
		z = this.pr_cross(x, y);
		y = this.pr_cross(z, x);		
		
		^[x, y, z].collect{|vec| this.pr_normVec(vec)};
	}
	init {|argmatrix|
		if(argmatrix.isKindOf(Matrix), {
			matrix = argmatrix;
		},{
//			argmatrix.postln;
			matrix = Matrix.with(argmatrix);
		})
	}

	printOn { | stream |
		if (stream.atLimit) { ^this };
		stream << this.class.name << ".basicNew( " ;
		stream << matrix;
		stream << " )" ;
	}

	inverse {
		var invRot, invTrans;
//				 _      _
//				|  R  d  |
//		T 	  = 	|  0  1  |
//			 	 -      -
//			 	 _                         _
//				|  R^(-1)    (-R^(-1) * d)  |
//		T^(-1) =	|  0           1            |
//				 -                         -
//				 _                  _
//				|  R^t    (-R^t * d) |
//		  	  =	|  0           1     |            (only if R is rotational matrix)
//				 -                  -
		invRot = matrix.sub(3, 3).flop;
		invTrans = (invRot.neg) * (Matrix[matrix.getCol(3)[0..2]].flop);
		^this.class.newFromRotTrans(invRot, invTrans.getCol(0));	
	} // end inverse
	vmul{|aVec, scale = 1|
		var a;
		
		// build a vector (4x1)
		a = Matrix.with([aVec ++ scale]).flop;
		// multiply and transform back to Array
		a = (matrix * a).getCol(0);
		
		// homogene -> normal representation and return
		//    ( pop last element and divide vector by it )
		^(a.pop.reciprocal * a);
	}
	rotMatrix {
		^matrix.sub(3, 3);
	}
	basis {
		^Array.newFrom(this.rotMatrix.flop);
	}
	position {
		^(matrix.getCol(3)[0..2]);
	}
	////////////////////// private /////////////////
	*pr_normVec{|a|
		^(a * a.squared.sum.sqrt.reciprocal);
	}
	*pr_cross {|a, b|
		var res;

		res = Array.newClear(3);
		res[0]=(a[1]*b[2])-(a[2]*b[1]);
		res[1]=(a[2]*b[0])-(a[0]*b[2]);
		res[2]=(a[0]*b[1])-(a[1]*b[0]);
		^res;
	}

}