CVInterpolator : CVPreset {
	var <interpIndices;
	var <interpItems;
	var <base, <target;
	
	var <targetCV;
	var <interpCV;
	
	*new { ^super.new.init }
	
	init { 		
		var preset;
		preset = this;
		presetCV = CV.new.sp(0,0,0,1);
		presetCV.action_({ |cv| preset.set(cv.value)});
		this.items_(items);

		targetCV = CV(presetCV.spec);
		targetCV.action_({|cv| preset.setTarget(cv.value) });
		interpCV = CV(\unipolar,0);
		interpCV.action_({|cv| preset.interpolate(cv.value) });
	}

	interpItems_ { | argCVs |
		interpIndices = nil;
		argCVs.do { | k |
			var ind;
			ind = this.hasItem(k);
			if (ind.notNil) {
				interpIndices = interpIndices.add(ind);
			}
		};
		interpItems = interpIndices.collect { | i | items[i] };
	}
	
	hasItem {  | cv |
		items.do { | c, i | if (cv == c) { ^i } };
		^nil
	}
	
	setTarget { arg index;
		var goal, start;
		if (index.notNil) { 
			goal = presets.clipAt(index);
			goal = interpIndices.collect { | i| goal[i] };
			start = interpItems.collect { | cv| cv.input };
			base = 2 * start - goal;
			target = goal - start;
			interpCV.value_(0); 
		}
		
	}

	interpolate { arg index; var val;
		if (target.notNil, {
			val = (index + 1) * target + base;
			interpItems.do { | cv, i| cv.input_(val[i]) };
		})
	}
	
	draw { |win, name, preset|
		~presetGUI.value(win, name, this);
		~interpolatorGUI.value(win, name, this)
	}
}

