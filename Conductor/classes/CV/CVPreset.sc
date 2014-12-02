CVPreset {

	var <>items;			// items to preset
	var <presets;			// arrays of input values corresponding to different presets
	var <presetCV;		// CV that controls preset selection

	
	*new { ^super.new.init }
	
	init { 
		var preset;
		preset = this;
		presetCV = CV.new.sp(0,0,0,1);
		presetCV.action_({ |cv| preset.set(cv.value)});
	}
	
	value  {  ^[[], presets] }
	value_ { | v| this.presets = v[1]; }  
	
	input_ { | input | input.do { |in, i|	items[i].input_(in)} }
	input { ^items.collect{|cv| cv.input } }

	asInput { | vals | vals.collect { | v, i | items[i].asInput(v) } }
	
//	input_ { | input | presetCV.input_(input) }
//	input { ^presetCV.input }
		
	
	presets_ { | argPresets|
		presets = argPresets;
		presetCV.spec.maxval = presets.size;
	}
	
	addPreset { 	
		this.presets = presets.add(this.input); 
		presetCV.spec.maxval = presets.size;
	}
	
	removePreset { arg index;
		if (presets.notNil, {
			presets.removeAt(index);
			presetCV.spec.maxval = presets.size;
		});
	}

	set { arg index;
		var preset;
		preset = presets[index];
		items.do { | p, i | p.input_(preset[i]) }
	}
	
	draw { |win, name, preset|
		~presetGUI.value(win, name, this)
	}
	

}

NullPreset {
	*value_ {}
	*value { ^1 }
	*input_ {}
	*input { ^1 }
	*draw {}
}