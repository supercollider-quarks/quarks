
ENDef {
	// Tuple3([IO(IO())], [EventStream[IO[Unit]]], [IO] )
	classvar <>tempBuilder;
	var <func;
	//Writer( Unit, Tuple3([IO(IO())], [EventStream[IO[Unit]]], [IO] ) )
	var <resultWriter;

	*new { |func|
		^super.newCopyArgs(func).init
	}

	*appendToResult { |writer|
		if( tempBuilder.isNil ) { Error("Ran ENDef.appendToResult with uninitialized ENDef.tempBuilder").throw };
		tempBuilder = tempBuilder |+| writer.w;
		^writer.a
	}

	*evaluate { |f, args|
		var r;
		tempBuilder = T([],[],[]);
		r = f.value(*args);
		^Writer(r, tempBuilder);
	}

	init {
		resultWriter = ENDef.evaluate(func);
	}

}

+ Object {

	enIn { |...args|
		^ENDef.appendToResult( this.asENInput(*args) );
	}

	enInES { |...args|
		^ENDef.appendToResult( this.asENInputES(*args) );
	}

	enSink { |signal|
		ENDef.appendToResult( this.sink(signal) );
    }

	enSinkValue { |signal|
		ENDef.appendToResult( this.sinkValue(signal) );
	}

}

+ FPSignal {

	enOut {
		ENDef.appendToResult( this.reactimate );
	}

	enOut2 {
		ENDef.appendToResult( this.reactimate2 );
	}


	withKey { |key|
		^this.collect{ |v| [key,v] }
	}

	enDebug { |string|
		ENDef.appendToResult( this.debug(string) )
	}

}

+ EventStream {

	enOut {
		ENDef.appendToResult( this.reactimate );
	}

	enDebug { |string|
		ENDef.appendToResult( this.debug(string) )
	}

}