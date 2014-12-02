
+ Document {

		// search the document for a string, in either direction
		// returns starting position of string or nil if not found before BOF or stopString
	scanFor { arg start, string, stopString, increment = 1;
		var whileFlag = true, foundFlag = false, strtemp;
		
		{ whileFlag }.while({
			strtemp = this.string(start, string.size);
			(strtemp == string).if({
				whileFlag = false; foundFlag = true;	// got it
			}, {
					// check bof/eof condition
				increment.isNegative.if({
					whileFlag = (start > 0);
				}, {
					whileFlag = (strtemp.size == string.size);
				});  // eof, stop now
					// check the stop condition
				(this.string(start, stopString.size) == stopString).if({
					whileFlag = false;
				});
			});
			start = start + increment;
		});
		
		foundFlag.if({ ^(start - increment) }, { ^nil });
	}
	
	scanBackFor { arg start, string, stopString;
		^this.scanFor(start, string, stopString, -1)
	}
}

+ Instr {
	*abstractBrowser { arg action = \gui, instrSuffix, doc;
		// doc arg is ignored now, will be implemented later
		// opens a browser to open the source file of the Instr clicked upon
		instrSuffix = instrSuffix ? "";
		^this.makeBrowserDoc.mouseUpAction_({ arg d;
			var clickpos, tempstr, instrStart, instrEnd, whileFlag;
			clickpos = d.selectionStart;
			tempstr = d.string(clickpos-1, 2);
				// if it's one of these, we have it easy
			(instrStart = tempstr.indexOf($<)).notNil.if({
				instrStart = instrStart + clickpos + 1;
			}, {
					// didn't click next to bracket, so search left until \n
				instrStart = d.scanBackFor(clickpos, "<", "\n");
			});
			instrStart.notNil.if({
				instrEnd = d.scanFor(instrStart, ">", "\n");
				(d.string(instrStart+1, instrEnd-instrStart-1) ++ instrSuffix)
					.interpret.perform(action);
			});
		});
	}
	
	*fileBrowser {
		^this.abstractBrowser(\openTextFile, ".path");
	}
	
	*guiBrowser {
		^this.abstractBrowser(\gui);
	}
	
	*argBrowser {
		^this.abstractBrowser(\listArgs);
	}
	
	*makeBrowserDoc {
		var d, dispFunc;
		d = Document.new("Instr browsing", "");
		this.loadAll;
		d.selectLine(1);
		d.selectedString_("Available Instrs, by category\n");
		d.selectedString_("Click inside angle brackets to open the Instr's definition file\n");
			// need a recursive func to handle multiple levels
		dispFunc = { arg dictSorted, level = 0;
			dictSorted.do({ arg kvPair;
				kvPair.at(1).respondsTo(\keysValuesDo).if({
					d.selectedString_("\n" ++ level.reptChar($\t) ++ kvPair.at(0) ++ "\n");
					dispFunc.value(kvPair.at(1).asSortedArray, level+1);
				}, {
					d.selectedString_("%< % >\n".format(level.reptChar($\t), kvPair[1].asString));
				});
			});
		};
		dispFunc.value(Library.global.at(this).asSortedArray);
		{ d.removeUndo }.defer(2);
		^d.front
	}
}
