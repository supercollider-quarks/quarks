
OSCIIRadioButton {

	var <>action, <>button, <>valueText, stringview, font;
	var fillcolor;
		
	*new { arg w, bounds, string; 
		^super.new.initOSCIIRadioButton(w, bounds, string);
	}
	
	initOSCIIRadioButton { arg w, bounds, string;
	
		var buttrect, light;
		var stringbounds;
		var invButton;
		
		fillcolor = Color.new255(103, 148, 103);
		buttrect = bounds;
	
		button = GUI.button.new(w, buttrect)
					.states_([["", Color.clear, Color.clear],
					   ["", Color.clear,  fillcolor]])
					.canFocus_(false)
					.action_({arg butt;
						action.value(butt.value);
					});
							
		stringbounds = Rect(	buttrect.left+buttrect.width+5,
							buttrect.top,
							string.size * 5, // size of each letter around 5 pixels?
							16);
							
		stringview = GUI.staticText.new(w, stringbounds)
					.string_(string);
		
		// toggle radio when clicked on string
		invButton = GUI.userView.new(w, stringbounds);
		invButton.background = Color.new255(11,11,11,0);
		invButton.canFocus = false;
		invButton.mouseDownAction = { arg  view,x,y;
				//button.valueAction_(1);
				button.valueAction_((button.value -1).abs);
			};

	}
	
	font_ {arg argfont;
		font = argfont;
		stringview.font_(font); 
	}
	
	canFocus_ {arg boolean;
		button.canFocus_(boolean);
	}

	value_ {arg boolean;
		button.valueAction_(boolean);
	}

	switchState {
		button.value_((button.value -1).abs);
	}
	
	color_ {arg col;
		fillcolor = col;
		button.states_([["", Color.clear, Color.clear], ["", Color.clear,  fillcolor]]);
	}
	
	value {
		^button.value;
	}
	
	focus {arg bool;
		button.focus(bool);
	}
	
	states_{arg states;
		button.states_(states);				
	}
	
	valueAction_{arg val;
		button.valueAction_(val);				
	}
}



OSCIIRadioButtonX {

	var <>action, <>button, <>valueText, stringview, font;
	var fillcolor;
		
	*new { arg w, bounds, string; 
		^super.new.initOSCIIRadioButtonX(w, bounds, string);
	}
	
	initOSCIIRadioButtonX { arg w, bounds, string;
	
		var buttrect, light;
		var stringbounds;
		var invButton;
		
		fillcolor = Color.new255(103, 148, 103);
		buttrect = bounds;
	
		button = RoundButton(w, buttrect)
					.states_([["", Color.clear, Color.clear],
					   ["", Color.clear,  fillcolor]])
					.canFocus_(false)
					.relativeOrigin_(false)
					.action_({arg butt;
						action.value(butt.value);
					});
							
		stringbounds = Rect(	buttrect.left+buttrect.width+5,
							buttrect.top,
							string.size * 5, // size of each letter around 5 pixels?
							16);
							
		stringview = SCStaticText(w, stringbounds)
					.string_(string);
		
		// toggle radio when clicked on string
		invButton = GUI.userView.new(w, stringbounds);
		invButton.background = Color.new(11,11,11,0);
		invButton.canFocus = false;
		invButton.mouseDownAction = { arg  view,x,y;
				//button.valueAction_(1);
				button.valueAction_((button.value -1).abs);
			};

	}
	
	font_ {arg argfont;
		font = argfont;
		stringview.font_(font); 
	}
	
	canFocus_ {arg boolean;
		button.canFocus_(boolean);
	}

	value_ {arg boolean;
		button.valueAction_(boolean);
	}

	switchState {
		button.value_((button.value -1).abs);
	}
	
	color_ {arg col;
		fillcolor = col;
		button.states_([["", Color.clear, Color.clear], ["", Color.clear,  fillcolor]]);
	}
	
	value {
		^button.value;
	}
	
	focus {arg bool;
		button.focus(bool);
	}
	
	states_{arg states;
		button.states_(states);				
	}
	
	valueAction_{arg val;
		button.valueAction_(val);				
	}
}



