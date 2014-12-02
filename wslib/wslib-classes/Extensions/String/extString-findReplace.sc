// wslib 2005
// find and replace functionality for Strings / arrays of strings
// slightly different from joshlib
// 03/2008 -- some of there may be obsolete in SC3.2, will look at it later

+ String {
	replaceAt {arg replaceString, pos = 0, size;
		// overwrite a part of a string
		// more flexible than .overWrite
		// not in place
		// can use a specified size of the part to remove
		if(size.isNil) 
			{size = replaceString.size};
		^if(pos > 0)
			{ this[0..(pos - 1)] ++ replaceString ++ this[(pos + size)..]; }
			{ replaceString ++ this[(pos + size)..]; };
		}
	
	findReplace {arg findString, replaceString = "", ignoreCase = false, offset = 0;
		//find a phrase and replace it once
		var i;
		i = this.find(findString, ignoreCase, offset);
		^this.replaceAt(replaceString, i, findString.size);
	}
	
	findReplaceAll {arg findString, replaceString = "", ignoreCase = false;
		// find a phrase and replace all occurences of it
		// same as 'replace' in JoshLib, but doesn't freeze at not-alphaNumeric chars
		// 03/2008 :: obsolete now ??
		var offset = 0, i;
		var thisCopy;
		thisCopy = this.copy;
		while {
			i = thisCopy.find(findString, ignoreCase, offset);
			i.notNil;
		}{
			thisCopy = thisCopy.replaceAt(replaceString, i, findString.size);
			offset = i + 1;
		};
	^thisCopy;
	}
}


+ Array {
	selectFindString { arg stringToFind, ignoreCase = false;
		//find a part of a string in an array and
		//return an array of results
		// also works on arrays with non-string contents
		// as long as the contents accept the .asString method
		// 03-2008 :: renamed from 'find' to 'selectFindString'
		var outArray, outIndices;
		outArray = [];
		outIndices = [];
		this.do({ |item, i|
			var foundItem;
			if(item.asString.find(stringToFind.asString, ignoreCase).notNil)
				{outArray = outArray.add(item);
				outIndices = outIndices.add(i)};
			});
		outArray.addUniqueMethod('indices', {outIndices});
		^outArray;
	}	
	
	findReplace { arg findString, replaceString = "", ignoreCase = false;
		// find / replace part of string in every slot of array
		// everything in the array is converted to string
		// numbers which after the find/replace action still
		// could be interpreted as numbers are converted
		// back to float or int
		^this.collect({|item| 
			item.asString
				.findReplaceAll(findString.asString, replaceString.asString, ignoreCase)
				.asNumberIfPossible;
			});
		}
	 
		
	}