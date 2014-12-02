// wslib 2011

// EM =-= EnvironmentModel
// supports simple MVC structures for Environments
// whenever a value for a key is changed it calls .changed

/*

e = EM( \freq, 440, \amp, 0.1 );

(
// make a gui for it:
e.makeWindow = { |env|
	var win, cnt;
	win = Window( "e", Rect( 128 + 100.rand, 300 + 100.rand, 400, 52 )).front;
	win.addFlowLayout;
	
	cnt = SimpleController( env );
	win.onClose_({ cnt.remove }); // remove controller at window close
	
	env.keysValuesDo({ |key, value|
		var sl;
		if( value.isNumber ) {	
			sl = EZSmoothSlider( win, 380@20, key, key, { |sl| env[ key ] = sl.value } );
			cnt.put( key, { |ev, what, val| sl.value = ev[ key ] });
			env.changed( key ); // update the slider
		};
	});
	
	win;
};

e.makeWindow;
);

e.makeWindow; // add a second window

e.amp = 0.25; // slider jumps in both windows
e.freq = 220;

e[ \amp ] = 0.5; // slider jumps

e.use({ ~amp = 0.33 }); // slider doesn't jump unfortunately

// now move an amp slider yourself
e.amp.postln; // value changed

*/


EM : Environment {
	
	*new { arg ...pairs;
		^super.new(8, nil,nil, true).putPairs( pairs );
	}
	
	*with { | ... args | // args: array of Associations
		var newColl;
		newColl = this.new();
		newColl.addAll(args);
		^newColl
	}
	
	put { |key, value|
		super.put( key, value );
		this.changed( key, value );
	}
		
	putGet { |key, value|
		var res;
		res = this.at( key );
		this.put( key, value );
		^res;
	}
	
	atPathFail { arg path, function; // support nested dictionaries
		var item;
		item = this;
		path.do({ arg name;
			if( item.respondsTo( \at ) ) {
				item = item.at( name );
				if( item.isNil ) { ^function.value };
			} {
				 ^function.value;
			};
		});
		^item
	}
	
	atPath { arg path;
		^this.atPathFail(path)
	}
	
	// overwrite super methods
	stop { |...args|
		^this.doesNotUnderstand( \stop, *args );
	}
	
	release { |...args|
		^this.doesNotUnderstand( \release, *args );
	}
	
	doesNotUnderstand { arg selector ... args;
		var func;
		if (know) {

			func = this[selector];
			if (func.notNil) {
				^func.functionPerformList(\value, this, args);
			};

			if (selector.isSetter) {
				selector = selector.asGetter;
				if( [ \stop, \release ].includes( selector ).not && { this.respondsTo(selector) }) {
					warn(selector.asCompileString
						+ "exists a method name, so you can't use it as pseudo-method.")
				};
				^this[selector] = args[0];
			};
			func = this[\forward];
			if (func.notNil) {
				^func.functionPerformList(\value, this, selector, args);
			};
			^nil
		};
		^this.superPerformList(\doesNotUnderstand, selector, args);
	}
	
	printOn { arg stream, itemsPerLine = 5;
		var max, itemsPerLinem1, i=0;
		itemsPerLinem1 = itemsPerLine - 1;
		max = this.size;
		stream << this.class.name << "( ";
		this.keysValuesDo({ arg key, val;
			stream <<< key << ", " << val;
			if ((i=i+1) < max, { stream.comma.space;
				if (i % itemsPerLine == itemsPerLinem1, { stream.nl.space.space });
			});
		});
		stream << " )";
	}

	storeOn { arg stream, itemsPerLine = 5;
		var max, itemsPerLinem1, i=0;
		itemsPerLinem1 = itemsPerLine - 1;
		max = this.size;
		stream << this.class.name << "( ";
		this.keysValuesDo({ arg key, val;
			stream <<< key << ", " <<< val;
			if ((i=i+1) < max, { stream.comma.space;
				if (i % itemsPerLine == itemsPerLinem1, { stream.nl.space.space });
			});
		});
		stream << " )";
	}


	
}

// ordered variant : keys are sorted by order of addition
OEM : EM {
	var <keys;
	
	put { |key, value|
		if( value.isNil ) {
			keys.remove( key );
		} {
			if( keys.isNil or: { keys.includes( key ).not } ) {
				keys = keys.add(key);
			};
		};
		super.put( key, value );
	}
	
	putFirst { |key, value| this.insert( 0, key, value ); }
	
	insert { |index, key, value|
		keys.remove( key ); // always remove (changes order)
		if( keys.isNil or: { keys.includes( key ).not } ) {
			keys = (keys ? {[]}).insert(index, key);
		};
		super.put( key, value );
	}
	
	removeAt { |key|
		keys.remove( key );
		^super.removeAt( key );
	}
	
	removeAtFail { |key, function|
		keys.remove( key );
		^super.removeAtFail( key, function );
	}
	
	atIndex { |index|
		^this.at((keys ?? {[]})[ index ] );
	}
	
	keysValuesDo { |function|
		var arr;
		arr = Array.new(keys.size * 2);
		keys.do({ |key|
			arr.add( key );
			arr.add( this[ key ] );
		});
		this.keysValuesArrayDo(arr, function);
	}
	
	keys_ { |newKeys|
		if( this.prCheckKeys( newKeys ) ) {
			keys = newKeys;
		} {
			"%:keys_ - new keys don't match the existing keys, not using them"
				.format(this.class).warn;
		};
	}
	
	prCheckKeys { |newKeys|
		if( keys.size != newKeys.size ) { ^false };
		keys.do({ |item|
			if( newKeys.includes( item ).not ) { ^false };
		});
		^true
	}
	
	pairsDo { |function|
		this.keysValuesDo(function);
	}
	

}