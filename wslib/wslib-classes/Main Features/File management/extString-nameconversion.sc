// wslib 2005 / 2007
// utils for name conversion

+ String {
	
	capsToSpaces { arg space = " ", toLower = true; 
		// convert "BlahBlah" to "blah blah"
		// or "blah_blah" when 'space' is set to "_"
		// or "Blah Blah" when 'toLower' is false
		var newStr = "";
		this.do({
			| char, i|
			if(char.isUpper)
				{
				if(toLower) {char = char.toLower }; 	
				if(i != 0)
					{ newStr = newStr ++ space ++ char }
					{ newStr = newStr ++ char };
				}
			{ newStr = newStr ++ char;  }
		});
		^newStr;
	}
	
	firstToUpper { ^this[0].toUpper.asString ++ this[1..] }
	firstToLower { ^this[0].toLower.asString ++ this[1..] }
	
	spacesToCaps { arg space = " ";
		var newStr = "";
		this.do({
			| char, i|
			if(space.includes(char).not)
				{ if( space.includes(this[i-1]) )
					{ newStr = newStr ++  char.toUpper }
					{ newStr = newStr ++ char;  };
				};
		});
		^newStr;
	}
	
	splitNumbers { arg convert = true; // separate numbers from the rest of the string
		var array = [];
		var word = "";
		var currentWordType = \text;
		this.do({ arg char, i;
			if(char.asString.couldBeNumber)
				{ if(currentWordType == \number)
					{ if(char != $-)
						{ word = word ++ char}
						{ array = array.add(word);
							word = "" ++ char;
							if(this[i+1].asString.couldBeNumber.not)
								{currentWordType = \text; } } }
					{ 	case {char == $.}
							{word = word ++ char}
							{(char == $-) && this[i+1].asString.couldBeNumber.not}
							{	currentWordType = \text;
								word = word ++ char; }
							{true}
							{	array = array.add(word);
								word = "" ++ char; 
								currentWordType = \number; };
					 } } 
				{ if(currentWordType == \text)
					{word = word ++ char}
					{	array = array.add(word);
						word = "" ++ char; 
						currentWordType = \text;
					 } };
			});
		array = array.add(word);
		array = array.select({ |item| item.size != 0 });
		if(convert)
			{^array.collect( _.asNumberIfPossible );}
			{^array; };
		}
	
	extractNumbers { ^this.splitNumbers(true).select(_.isNumber) }
	
	removeNumbers { ^this.splitNumbers(true).select(_.isString).join }
	
	doToNumbers { arg func;
		func = func ? { |item| item };
		 ^this.splitNumbers(true).collect({ |item|
		 	if(item.isNumber) 
		 		{ func.value(item) }
		 		{item } }).join;
		 }

	numbersToAlpha { arg offset = 0, toUpper = true, prepend = "", append = "";
		// will leave non-matching numbers untouched
		var alphaTable = "abcdefghijklmnopqrstuvwxyz";
		if(toUpper) { alphaTable = alphaTable.toUpper;};
		^this.doToNumbers({ |item|
			item = alphaTable[item - offset] ? item;
			if(item.class == Char) 
			  	{prepend ++ item ++ append}
			  	{item}
			});
		}
	
		
	trimLeft { arg whatToTrim = " "; // can be string multiple items
        var i = 0, s = this.copy;
        whatToTrim = whatToTrim.asString;
        if ( s.size > 0 , {
            if ( whatToTrim.includes(s[i]) , {
                while ( { (i < (s.size-1)) && (whatToTrim.includes(s[i])) } , { i = i + 1 } );
                if ( ((i < (s.size-1)) || (not (whatToTrim.includes(s[i])))) , {
                    ^s.copyToEnd(i);
                },{
                    ^"";
                });
            },{
                ^s;
            });
        },{
            ^"";
        });
    }    
         
    trimRight { arg whatToTrim = " ";       
        var s = this.copy;
        var i = s.size - 1;
        whatToTrim = whatToTrim.asString;
		if ( i >= 0 , {
            if ( whatToTrim.includes(s[i]) , {
                while ( { ( i > 0 ) && whatToTrim.includes(s[i]) } , { i = i - 1 } );
                if ( ((i > 0) || (not (whatToTrim.includes(s[i])))) , {
                    ^s.copyFromStart(i);
                },{
                    ^"";
                });
            },{
                ^s;
            });
        },{
            ^"";
        });
    }
    
    trim { arg whatToTrim = " ";
    		^this.trimLeft(whatToTrim).trimRight(whatToTrim);
    		}     
}

/*
+ Char { 
	isUpper { //testing if char is upper case
	^((this.ascii < 91) && (this.ascii > 64));
		}
		
	isLower { // testing if char is lower case
	^((this.ascii < 123) && (this.ascii > 96));
		}
	}

*/