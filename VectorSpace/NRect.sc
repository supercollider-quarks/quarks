// ©2012 Miguel Negrão
// GPLv3 - http://www.gnu.org/licenses/gpl-3.0.html

//An N-dimensional rectangle
NRect {
    /*
    origin : subclass of AbstractVector
    diagonal: subclass of AbstractVector
    origin and vector should be of the same size.

    arguments named v, dw, etc, should be instances of RealVector or it's subclasses.
    */
    var <origin, <diagonal;
    var <sortedConstraints;

    *new { |origin, diagonal|
        ^super.newCopyArgs(origin, diagonal).init
    }

    *aboutPoint { |v, dw|
        ^this.new(v - dw, v + (2*dw) )
    }

    init {
        sortedConstraints = [origin, this.endPoint].flopWith{ |a,b|
            [a,b].sort
        }
    }

    endPoint {
        ^origin + diagonal
    }

    endPoint_ { |v|
        diagonal = v - origin;
        this.init;
    }

    origin_ { |v|
        origin = v;
        this.init;
    }

    center {
        ^origin + (diagonal/2)
    }

    prFlop { |v,f|
        ^[origin, this.endPoint, v].flopWith(f)
    }

    prflopSorted { |v,f|
        ^[sortedConstraints, v].flopWith(f)
    }

    containsVector { |v|
        ^this.prflopSorted(v){ |ab,x|
            (x >= ab[0] ) && (x <= ab[1])
        }.reduce('&&')
    }

    clipVector { |v|
        ^this.prflopSorted(v){ |ab,x|
            x.clip(ab[0], ab[1])
        }
    }

    mapVector { |v|
        ^this.constrain( this.prDoFlopWith(v){ |a,b,x|
            x.linlin(0, 1, a, b, \none)
        } )
    }

    unmapVector { |v|
         ^this.prDoFlopWith( this.constrain(v) ){ |a,b,x|
            x.linlin(a, b, 0, 1, \none)
        }
    }

    asNRect {
        ^this
    }

    moveBy { |v|
        ^this.class.new(origin + v, diagonal)
    }

    moveTo { |v|
        ^this.class.new(v, diagonal)
    }

    resizeBy { |dw|
		^this.class.new(origin, diagonal  + dw)
	}

	resizeTo { |dw|
		^this.class.new(origin, dw)
	}

	insetBy { |dw|
        ^this.class.new(origin + dw, diagonal - (dw*2) )
    }

    storeArgs { ^[origin, diagonal] }

	printOn { arg stream;
		stream << this.class.name << "(" <<* [origin, diagonal] << ")";
	}

}