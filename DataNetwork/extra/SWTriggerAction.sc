// only performs a certain action, if a state has changed
SWTriggerAction{
	
	var <prevValues;
	var <function;

	*new{ |func,size,initVal=0|
		^super.new.init(func,size,initVal);
	}

	init{ |func,size,initVal|
		function = func;
		prevValues = Array.fill( size, initVal );
	}

	value{ |newdata|
		newdata.do{ |it,i|
			if ( it.equalWithPrecision( prevValues[i] ).not ){
				function.value( it, i );
			};
			prevValues[i] = it;
		};
	}
}

// only performs a certain action, if a state has changed
SWChangeAction{
	
	var <prevValues;
	var <function;

	*new{ |func,size,initVal=0|
		^super.new.init(func,size,initVal);
	}

	init{ |func,size,initVal|
		function = func;
		prevValues = Array.fill( size, initVal );
	}

	value{ |newdata|
		var hasChanged = false;
		newdata.do{ |it,i|
			if ( it.equalWithPrecision( prevValues[i] ).not ){
				hasChanged = true;
			};
			prevValues[i] = it;
		};
		if ( hasChanged ){
			function.value( newdata );
		};
	}
}