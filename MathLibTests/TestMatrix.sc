/*
Matrix.test
UnitTest.gui
*/
TestMatrix : UnitTest {

test_identities {
	var a, b, c, tests;
	// Matrix identities
	// Many more at http://www.cs.toronto.edu/~roweis/notes/matrixid.pdf
	
	[2, 3, 4].do{|matsize|
		
		a = {{2.0.rand}.dup(matsize)}.dup(matsize).as(Matrix);
		b = {{2.0.rand}.dup(matsize)}.dup(matsize).as(Matrix);
		c = {{2.0.rand}.dup(matsize)}.dup(matsize).as(Matrix);
		
		tests = [
			// 01 basic formulae
			[{(a * (b + c))}, {((a * b) + (a * c))}, "distributivity of multiplication"],
			[{(a + b).flop}, {(a.flop + b.flop)}, "commutativity of transposition and addition"],
			[{(a * b).flop}, {(b.flop * a.flop)}, "commutativity of transposition and multiplication"],
			
			// 02 trace, determinant, rank
			[{(a*b).det}, {(a.det * b.det)}, "commutativity of .det and *"],
			[{a.inverse.det}, {a.det.reciprocal}, ".inverse.det == .det.reciprocal"],
			[{(a * b * c).trace}, {(b * c * a).trace}, "ring commutativity of multiplication"],
		];
		
		tests.do{ |t|
			this.assertArrayFloatEquals(t[0].value.asArray, t[1].value.asArray, t[2])
		};
		
		};
	
}

} // end class
