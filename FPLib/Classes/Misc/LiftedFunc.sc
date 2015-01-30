/*



{ |a,b| a + b }.lift.( Var(1), Var(2) ).now



*/

LiftedFunc {
	var func;

	*new{ |f|
        ^super.newCopyArgs(f)
    }

	value{ |...args|

		var f = { |args2|
			func.value( *args2 )
		};

		^(f <%> args.sequence)
    }

	composeUnaryOp { arg aSelector;
        ^this.class.new( func.composeUnaryOp( aSelector ) )
    }
    composeBinaryOp { arg aSelector, something, adverb;
        ^this.class.new( func.composeBinaryOp(aSelector, something.func, adverb) )
    }
    reverseComposeBinaryOp { arg aSelector, something, adverb;
        ^this.class.new( func.reverseComposeBinaryOp(aSelector, something.func, adverb) )
    }
    composeNAryOp { arg aSelector, anArgList;
        ^this.class.new( func.composeNAryOp(aSelector, anArgList ) )
    }

	<> { |x|
		^this.class.new(func <> x.func)
	}


}

+ AbstractFunction {

	lift {
		^LiftedFunc(this)
	}

}

