/* make a little Rand button that auto-moves a controller. 
(
w = Window("autorand").front; 
w.addFlowLayout;
e = EZSlider(w, 300@20, \freq, \freq);
AutoRand(e, e.controlSpec, 0.2).makeButton(w, Rect(0,0,20,20));

f = EZSlider(w, 300@20, \freq, \freq);
a = AutoMod(f, { |amod, val| (val * rrand(0.9, 1.1)).postln; }, 0.2).makeButton(w, Rect(0,0,20,20));

b = AutoMod(f, { |amod, val| (val * 1.3 + 0.4 % 2000 + 20).postln; }, 0.2).makeButton(w, Rect(0,0,20,20));

b = AutoMod(f, r { |amod| var x; loop { x = rrand(1, 1990); rrand(5, 25).do { yield(amod.ctl.value + x.rand2) } };  }, 0.05).makeButton(w, Rect(0,0,20,20));

)
*/ 

AutoMod { 
	var <>ctl, <>func, <>dt, <task; 
	var butname = "m"; 
	
	*new { |ctl, func, dt| 
		^super.newCopyArgs(ctl, func, dt ? { exprand(0.33, 3) }).init;
	}

	init {
		var spec = ctl.controlSpec;
		
		task = TaskProxy({ 
			var oldval, newval; 
			loop { 
				oldval = ctl.value;
				newval = func.value(this, oldval);
				if (newval.notNil) {  ctl.valueAction_(newval) };
				dt.value.wait;
			}
		}).clock_(AppClock); 
	}
	
	play { task.play }
	stop { task.stop }

	makeButton { |parent, bounds| 
		Button(parent, bounds)
			.states_([[butname], [butname, Color.white, Color.green]])
			.action_({ |btn| if (btn.value == 1) { this.play } { this.stop } });	
	}
}

AutoRand /*: AutoMod */{ 
	var <ctl, <spec, <>rd, <>exp, <>dt, <task; 
	*new { |ctl, spec, rd = 0.1, exp = 2, dt| 
		^super.newCopyArgs(ctl, spec.asSpec, rd, exp).init(dt);
	}

	init { |indt|
		dt = indt ? { exprand(0.33, 3) };
		task = TaskProxy({ 
			var unmapped, jumpVal;
			loop { 
				unmapped = spec.unmap(ctl.value);
				jumpVal = ((1.0.rand ** exp) * rd * [1-unmapped, unmapped.neg].choose);
				ctl.valueAction_(spec.map(unmapped + jumpVal));
				[\jumpVal, jumpVal].postln;
				dt.value.wait;
			}
		}).clock_(AppClock); 
	}
	play { task.play }
	stop { task.stop }

	makeButton { |parent, bounds| 
		Button(parent, bounds)
			.states_([["r"], ["r", Color.white, Color.green]])
			.action_({ |btn| if (btn.value == 1) { this.play } { this.stop } });	
	}
}


+ AutoSlider { 
	slider { ^slider }
}