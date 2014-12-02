+ EZText { 
		// fixes unexpected behaviour in EZText up to v. 3.4.4 
		// when the inval is a string. 
	value_ { |inval|
		var string; 
		value = inval; 
		if (inval.isKindOf(String).not) { 
			string = value.asCompileString 
		} { 
			string = value
		};
		textField.string = string;
	}
}