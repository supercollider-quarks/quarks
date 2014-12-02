XiiStyles {	
	
	var <normal; 
	var <dropDownWidth;
	var <grey;

	
	*new { arg numChannels;
		^super.new.init(numChannels);
		}
		
	init { arg ch;

		normal = Font("Helvetica", 9);

		if 	( ch == 1,
			{ dropDownWidth = 40 },
			{ dropDownWidth = 55 }
		);	
		
		grey = Color.grey;
	}
	
}
