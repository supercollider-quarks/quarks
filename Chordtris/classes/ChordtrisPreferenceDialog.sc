ChordtrisPreferenceDialog {
	
	var width = 400;
	var preferenceHeight = 20;
	var window;
	var preferences;
		
	*new { ^super.new.init }
	
	init {
		var vLayoutView;
		var height; 
		
		preferences = ChordtrisPreferences.getPreferences;
		
		height = (preferences.size + 4) * preferenceHeight;
		
		window = Window("Chordtris Preferences", Rect(300, 400, width, height)).front;
		vLayoutView = VLayoutView(window, Rect(0,0,width,height));
		window.view.background_(Color.grey(0.8));
		this.displayPreferenceControls(vLayoutView);
		this.createButtons(vLayoutView);
	}
	
	displayPreferenceControls { |view|
		
		preferences.do { |setting|
			
			var type = setting.type;
			
			case
			{ type == \string } {
				var v = HLayoutView(view, Rect(0,0,width, preferenceHeight));
				var menu, text;
				var itemsAsSymbols = setting.values.collect({ |item| item.asSymbol });
				
				StaticText(v, Rect(0,0,150,preferenceHeight)).string_(setting.label);
				menu = PopUpMenu(v, Rect(0,0,200,20)).items_(itemsAsSymbols).action_{ |menu|
					setting.value_(menu.item.asString);
				};
				
				menu.value_(menu.items.indexOf(setting.value.asSymbol));
				
				//menu.items.indexOf(setting.value.asSymbol).postln;
				
			}
			
			{ type == \float } {
				var v = HLayoutView(view, Rect(0,0,width, preferenceHeight));
				var slider, text;
				
				StaticText(v, Rect(0,0,150,preferenceHeight)).string_(setting.label);
				slider = Slider(v, Rect(0,0,200,20)).action_{ |slider|
					var value = slider.value.linlin(0, 1, setting.values[0], setting.values[1]).round(setting.parameter);
					text.string_(value);
					setting.value_(value);
				};
				
				text = StaticText(v, Rect(0,0,50,20)).align_(\center);
				text.string_(setting.value);
				slider.value_(setting.value.linlin(setting.values[0], setting.values[1], 0, 1));
				
			}
			
			// default case
			{ ("can not handle preference type" + type).postln }; 
			
		}
	}
	
	createButtons { |view|
		var v;
		
		// for spacing
		HLayoutView(view, Rect(0,0,width,30));
		
		v = HLayoutView(view, Rect(0,0,width,20));
		
		
		Button(v, Rect(0,0,100,20))
		.states_([["Save", Color.black, Color.new(0.4, 0.4, 0.8)]])
		.resize_(2)
		.action_{ this.save };
		
		Button(v, Rect(0,0,100,20))
		.states_([["Close", Color.black, Color.new(0.7, 0.3, 0.3)]])
		.resize_(2)
		.action_{ window.close };
		
	}
	
	save {
		ChordtrisPreferences.savePreferences;
	}
}