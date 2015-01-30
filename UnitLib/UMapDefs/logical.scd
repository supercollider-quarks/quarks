/*
\logical

An UMap for applying logical operations upon two binary values.

a: the first value (boolean)
b: the second value (boolean)
mode: value (0-5) for type of operation
	0: 'and'; and/&& operation
	1: 'or'; or operation
	2: 'nand'; nand (not and) operation
	3: 'xor'; xor operation (or and nand)
	4: 'a'; return first value
	5: 'b'; return second value
trueValue: the value (or UMap) to output when the result is true
falseValue: the value (or UMap) to output when the result is false

trueValue and falseValue arg ranges are mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \logical, { |a = 0, b = 0, mode = 0, trueValue = 1.0, falseValue = 0.0|
	var index, and, or, output;
	and = a * b;
	or = a.max(b);
	index = Select.kr( mode, [
		and, // and
		or, // or
		1-and, // nand
		or * (1-and), // xor
		a,
		b
	]) > 0.5;
	output = Select.kr( index, [ falseValue, trueValue ] );
	UMapOut.kr( output );
})
.setSpec( \a, BoolSpec(false) )
.setSpec( \b, BoolSpec(false) )
.setSpec( \mode, ListSpec( [0,1,2,3,4,5], 0, ['and', 'or', 'nand', 'xor', 'a', 'b' ] ) )
.mappedArgs_([ \trueValue, \falseValue ])
.category_( 'trigger_test' )