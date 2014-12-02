/*
* Data Structure for a preference setting.
* Structure holds data regarding
* - the label displayed in the preference dialog
* - the type of the value being adjusted
* - the default value of the setting
* - specifications of possible values
* - optional parameter
*/
ChordtrisPreferenceSetting {
	
	var <label;
	var <type;
	var <defaultValue;
	var <values;
	var <parameter;
	var value;
	
	*new { |label, type, defaultValue, values, parameter| ^super.newCopyArgs(label, type, defaultValue, values, parameter) }
	
	value {
		if(value.isNil)
		{
			^defaultValue;
		}
		
		^value;
	}
	
	value_ { |val|
		value = val;
	}
}