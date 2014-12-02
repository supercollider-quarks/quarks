// wslib

// number testing

+ String {
	
	couldBeNumber { //test if a string could be converted to number using .interpret
		^this.every({ |item, i| 
			((item.isDecDigit) or: (item == $.))
			or: ((item == $-) && (i == 0))
			});
		}
	
	couldBeHex {
	 	^this.every({ |item, i|
			(item.isDecDigit) or: ([$a, $b, $c, $d, $e, $f].includes(item.toLower) ) }); 
		}
			
	asNumberIfPossible {
		if(this.couldBeNumber && (this != "-") )
		{^this.interpret} {^this}
		}
	
	asHexIfPossible { |else|
		else = else ? { ^this };
		if(this.couldBeHex) 
		{^("16x" ++ this).interpret} { else.value };
		}

}