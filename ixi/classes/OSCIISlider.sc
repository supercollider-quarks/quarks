
OSCIISlider {

	var slider, valueText, nameText, spec, lastval, font;

	*new { arg w, bounds, name, min=0.0, max=1.0, start=0.0, step=0.0, warp=\lin; 
		^super.new.initOSCIISlider(w, bounds, name, min, max, start, step, warp);
		}
	
	initOSCIISlider { arg w, bounds, name, min=0.0, max=1.0, start=0.0, step=0.0, warp=\lin;
		var namerect, numberrect, slidrect;
		
		font = GUI.font.new("Helvetica", 12);
		lastval= start;
		spec = ControlSpec(min,max,warp,step,start);
		
		if ( GUI.id == \cocoa,
			{ 
				namerect= Rect(bounds.left,(bounds.height)+bounds.top,(bounds.width)-30, 20);
				numberrect= Rect(bounds.left+(bounds.width)-20,(bounds.height)+bounds.top, 38, 20);
			},		
			{ // -6 in the Y axis
				namerect= Rect(bounds.left,(bounds.height)+bounds.top-6,(bounds.width)-30, 20);
				numberrect= Rect(bounds.left+(bounds.width)-20,(bounds.height)+bounds.top-6, 38, 20);
			}
		);
		slidrect= Rect(bounds.left,bounds.top,bounds.width,bounds.height);
		


		slider = GUI.slider.new( w, slidrect);
		if ( GUI.id == \cocoa, { slider.background_(Color.new255(160, 170, 255, 100)) });
		slider.value_(spec.unmap(lastval));
		slider.action_({arg sl; var val; 
				val = spec.map(sl.value);  
				valueText.string_(val); 
				lastval=val;
		});

		nameText = GUI.staticText.new(w, namerect)
			.font_(font)		
			.string_(name);
		valueText = GUI.staticText.new(w, numberrect)
					.string_(lastval)
					.font_(font);
				
	}
	
	value_ {arg val;
		slider.value_(spec.unmap(val));
		valueText.string_(val);
		lastval = val;
	}
		
	value{
		^lastval;
	}
		
	action_ { arg func;
		slider.action_({arg sl; var val; 
			val = spec.map(sl.value);  
			valueText.string_(val); 
			lastval=val;  
			func.value(lastval);
		});
	}
	
	valueAction_ { arg value; var val;
		slider.valueAction = spec.unmap(value);
	}
	
	setBgColor_ {arg color;
		slider.background_(color);
	}
	
	canFocus_ {arg bool;
		slider.canFocus_(bool);
	}
	
	keyDownAction_ { arg func;
		slider.keyDownAction_(func);
//		if(char=="a", {
//			startRecPath.value(\modindex);
//		}, {
//			me.defaultKeyDownAction(char, mod, uni);
//		});

	}
	
	font_{arg argfont;
		font = argfont;
		valueText.font_(font); 
		nameText.font_(font);
	}
	
	remove {
		slider.remove; 
		nameText.remove;
		valueText.remove;
	}
} // end of class


OSCIISliderX {

	var slider, valueText, nameText, spec, lastval, font;

	*new { arg w, bounds, name, min=0.0, max=1.0, start=0.0, step=0.0, warp=\lin; 
		^super.new.initOSCIISliderX(w, bounds, name, min, max, start, step, warp);
		}
	
	initOSCIISliderX { arg w, bounds, name, min=0.0, max=1.0, start=0.0, step=0.0, warp=\lin;
		var namerect, numberrect, slidrect;
		
		font =Font("Helvetica", 12);
		lastval= start;
		spec = ControlSpec(min,max,warp,step,start);
		
		namerect= Rect(bounds.left,(bounds.height)+bounds.top,(bounds.width)-30,20);
		numberrect= Rect(bounds.left+(bounds.width)-20,(bounds.height)+bounds.top,38,20);
		slidrect= Rect(bounds.left,bounds.top,bounds.width,bounds.height);
		
		nameText = SCStaticText(w, namerect)
			.font_(font)		
			.string_(name);
		valueText = SCStaticText(w, numberrect)
					.string_(lastval)
					.font_(font);
		slider = SmoothSlider.new( w, slidrect)
					.relativeOrigin_(false)

					//.background_(XiiColors.lightgreen)
					.hilightColor_(XiiColors.darkgreen)
					.knobColor_(Color.black);
		
		//set slider to default value, else will default to 0.0
		slider.value_(spec.unmap(lastval));
		
		//set associated variable to this value, client code will poll this rather than the slider directly
		//so safe for TempoClock use etc
		
		slider.action_({arg sl; var val; 
					val = spec.map(sl.value);  
					valueText.string_(val.round(0.01)); 
					lastval=val;
					});
				
	} // end of main func
	
	// set value from outside
	value_ {arg val;
		slider.value_(spec.unmap(val));
		//slider.update;
		valueText.string_(val);
		lastval = val;
		}
		
	// get the value
	value{
		^lastval;
		}
		
	action_ { arg func;
		slider.action_({arg sl; var val; 
			val = spec.map(sl.value);  
			valueText.string_(val); 
			lastval=val;  
			//lastval = sl;
			func.value(lastval);
		});
	}
	
	valueAction_ { arg value; var val;
		slider.valueAction = spec.unmap(value);
	}
	
	setBgColor_ {arg color;
		slider.background_(color);
	}
	
	canFocus_ {arg bool;
		slider.canFocus_(bool);
	}
	
	keyDownAction_ { arg func;
		slider.keyDownAction_(func);
	}
	
	font_{arg argfont;
		font = argfont;
		valueText.font_(font); 
		nameText.font_(font);
	}
	
	remove {
		slider.remove; 
		nameText.remove;
		valueText.remove;
	}
} // end of class

