//completes brackets and quotes in a semi-intelligent way for typing with less effort.
//Batuhan Bozkurt 2009

LessKeys
{
	classvar keyActionFunc, acters, ucTriggers;
	
	*initClass
	{
		//will try to match bracks if one of these is the next char conditionally
		acters = [" ", ")", "}", "\t", "\r", "\n", "]", ";"];
		//these keys will invoke
		ucTriggers = [40, 123, 34, 91, 39, 124];
		
		keyActionFunc = 
		{
			arg doc, char, mod, unicode, keycode;
			var initPos, nextChar;
			
			if(ucTriggers.detect({|i| i == unicode }).notNil,
			{ 
				initPos = doc.selectionStart;
				doc.selectRange(initPos, 1);
				nextChar = doc.selectedString;
				if((doc.selectionSize == 0) or: {acters.detect({|i| nextChar == i }).notNil},
				{
					doc.selectRange(initPos, 0);
					unicode.switch
					(
						40, { doc.selectedString = ")" },
						123, { doc.selectedString = "}" },
						34, { doc.selectedString = "\"" },
						//using ] below confuses lexer (if in double quotes), a sclang bug...
						91, { doc.selectedString = 93.asAscii.asString },
						39, { doc.selectedString = "'" },
						124, { doc.selectedString = "|"};
					);
					doc.selectRange(initPos, 0);
				}, { doc.selectRange(initPos, 0); });
			}) 
		};
	}
	
	*enable
	{
		Document.globalKeyDownAction = Document.globalKeyDownAction.addFunc(keyActionFunc);
		"LessKeys enabled...".postln;
	}
	
	*disable
	{
		Document.globalKeyDownAction = Document.globalKeyDownAction.removeFunc(keyActionFunc);
		"LessKeys disabled...".postln;
	}
}