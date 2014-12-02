// ©2010 Miguel Negr‹o
// GPLv3 - http://www.gnu.org/licenses/gpl-3.0.html

LinTrans {
	var<> matrix;
	
	*new{ |matrix|
		^super.newCopyArgs(matrix)
	}
	
	*rotation2D{ |theta|
		^super.newCopyArgs(Matrix.with([[cos(theta),sin(theta).neg],[sin(theta),cos(theta)]]))
	}
	
	value{ |vector|
		^(matrix*vector.asMatrix).getCol(0).as(vector.class)
	}
	
	det{
		^matrix.det
	}
	
	inverse{
		^LinTrans(matrix.inverse)
	}
	
	+{ |lintrans|
		^this.class.new(this.matrix+lintrans.matrix)
	}

	-{ |lintrans|
		^this.class.new(this.matrix-lintrans.matrix)
	}

	*{ |operand|
		if(operand.class == LinTrans){operand = operand.matrix};
		^this.class.new(this.matrix*operand)
	}
			
}