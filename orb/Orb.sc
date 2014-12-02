Orb {
	var netAddr;
	var <colors;
	
	*new {|netAddr|
		^super.newCopyArgs(netAddr ? NetAddr("localhost", 12000), Color.black!3)
	}
	set {|which = 0, color, dt = 0, action|
		var floppedArgs;
	
		floppedArgs = [which, color, dt, action].flop;
		if (floppedArgs.size > 1, {
			floppedArgs.do{|args| 
				this.performList(\set, args);
			};
			^this;
		});
		
		{
			this.pr_set(which, color, dt);
	    		action.value(this);
		}.fork;
 		colors[which] = color;
	}
	
	setHSV{|which, color, dt = 1, action|
    
		var hue, sat, val;
		var startHue, startSat, startVal, startAlpha;

		var actColor, timeStep = 0.05, numSteps;
		var dHue, actHue;
		var dVal, actVal;
		var dSat, actSat;

		var floppedArgs;
	
		floppedArgs = [which, color, dt, action].flop;
		if (floppedArgs.size > 1, {
			floppedArgs.do{|args| 
				this.performList(\setHSV, args);
			};
			^this;
		});
		
		if (color == colors[which], {^this});
		#hue,      sat,      val,      startAlpha = color.asHSV;
		#startHue, startSat, startVal, startAlpha = colors[which].asHSV;

		if(hue.isNaN, {hue = 1});
		if(sat.isNaN, {sat = 0});
		if(startHue.isNaN, {startHue = 1});
		if(startSat.isNaN, {startSat = 0});

		numSteps = dt div: timeStep;
		dHue = (hue - startHue) / numSteps;
		dSat = (sat - startSat) / numSteps;
		dVal = (val - startVal) / numSteps;
		
		actHue = startHue;
		actSat = startSat;
		actVal = startVal;
		actColor = colors[which];
		{
			numSteps.do{
				
//				this.pr_set(which, actColor, timeStep);

				netAddr.sendMsg("/orb",
					which,
		     		(actColor.red*255).asInt,
		     	   	(actColor.green*255).asInt,
		     	   	(actColor.blue*255).asInt,
		     	   	timeStep.asFloat
		    		);

		    		actHue = (actHue + dHue) mod: 1.0;
		    		actSat = (actSat + dSat) min: 1.0;
		    		actVal = (actVal + dVal) min: 1.0;
				actColor = Color.hsv(actHue, actSat, actVal);
			    	timeStep.wait;

			};
			action.value(this);
		}.fork;
		colors[which] = color;
	} // end setHSV

	flash {|which = 0, flashColor, dt = 0.05, action|
		var startColor;
		
		var floppedArgs;
		
		floppedArgs = [which, flashColor, dt, action].flop;
		if (floppedArgs.size > 1, {
			floppedArgs.do{|args| 
				this.performList(\flash, args);
			};
			^this;
		});

		startColor = colors[which];
		{
//			this.pr_set(which, flashColor, 0.005);

			netAddr.sendMsg("/orb",
		    		which,
		        	(flashColor.red*255).asInt,
		        	(flashColor.green*255).asInt,
		        	(flashColor.blue*255).asInt,
		        	0.005 // dt*0.5
		    );
		    (dt-0.005).wait;

//			this.pr_set(which, startColor, 0.005);

		    netAddr.sendMsg("/orb",
		    		which,
		        	(startColor.red*255).asInt,
		        	(startColor.green*255).asInt,
		        	(startColor.blue*255).asInt,
		        	0.005
	    		);
	    		0.05.wait;

			action.value(this);
	    	}.fork
	} // end flash
	
	env {|which = 0, flashColor, env, action|
		var startColor, actColor;
		var levels, times;
		
		var floppedArgs;
		
		
		env = env ? Env.perc;
		levels = env.levels;
		times  = env.times;
		
		floppedArgs = [which, flashColor, env, action].flop;
		if (floppedArgs.size > 1, {
			floppedArgs.do{|args| 
				this.performList(\env, args);
			};
			^this;
		});

		startColor = colors[which].copy;
		{
			actColor = startColor.blend(flashColor, levels[0]);
			this.pr_set(which, actColor).value;
			times.do{|time, i|
				actColor = startColor.blend(flashColor, levels[i+1]);
				this.pr_set(which, actColor, time);
		    	};
			action.value(this);
	    	}.fork;
	    	colors[which] = colors[which].blend(flashColor, levels.last);
	}
	
	pr_set {|which, color, dt = 0|
		netAddr.sendMsg("/orb",
			which.asInteger,
			(color.red*255).asInt,
			(color.green*255).asInt,
			(color.blue*255).asInt,
			dt.asFloat
		);
		dt.wait;
	}
}


/*
VOrb : Orb {
	

}
*/