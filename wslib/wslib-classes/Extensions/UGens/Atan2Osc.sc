Atan2 {
	
	// a wavetable based arctangent table reader
	// this takes a little more average cpu then atan2, but doesn't produce any cpu spikes
	// benefits mostly at audio rate, not so much at control rate
	
	var <>buffer;
	
	*ar { |a = 0, b = 1|
		^super.new.init.ar(a,b);
	}
	
	*kr { |a = 0, b = 1|
		^super.new.init.kr(a,b);
	}
	
	init { buffer = Atan2Table(); }
	
	ar { |a = 0, b = 1|
		var value;
		value = Atan2Table.scaleInput(a/b);
		if( value.rate === \control ) { value = K2A.ar( value ) };
		^BufRd.ar( 1, buffer, value, 0, 4 );
	}
	
	kr { |a = 0, b = 1|
		^BufRd.kr( 1, buffer, Atan2Table.scaleInput(a/b), 0, 4 );
	}
	
		
}

Atan2Table {
	// creates a new LocalBuf, unless there was already one made in the current buildSynthDef
	classvar <>tableSize = 4096, <>range = 100;
	classvar <>buffer, <>synthdef;
	
	*new { if( UGen.buildSynthDef != synthdef )
			{ synthdef = UGen.buildSynthDef;
			  ^this.createBuffer }
			{ ^buffer };
	}
	
	*createBuffer {
		// "buffer created".postln;
		^buffer = LocalBuf( tableSize, 1 )
			.set( { |i| i.linlin(0, tableSize-1, range.neg, range ).atan; } ! tableSize )
	}
	
	*scaleInput { |input|
		^input.linlin(range.neg, range, 0, tableSize-1, \minmax );
	}
	
}

+ AbstractFunction {
	atan2WT { |function = 1|
		^case { this.rate === \control }
			{ Atan2.kr( this, function ); }
			{ this.rate === \audio }
			{ Atan2.ar( this, function ); }
			{ this.atan2( function ) };
	}
}

+ SequenceableCollection {
	atan2WT { |function = 1|
		function = function.asCollection;
		^this.collect({ |item,i| item.atan2WT(function.wrapAt(i)); })
	}
}