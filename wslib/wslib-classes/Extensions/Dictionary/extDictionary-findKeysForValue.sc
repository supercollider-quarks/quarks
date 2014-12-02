// wslib 2005

// find all keys for a value

+ Dictionary {
	findKeysForValue { arg argValue;
		var result = [];
		this.keysValuesArrayDo(array, { arg key, val, i;
			if (argValue == val, { result = result ++ [key]})
		});
		^result
	}
}