// Andrea Valle jan 2010


// Extensions fo SynthDef and Synth, see SynthDefAutogui help 
+ SynthDef {
	

	autogui { arg aSynth, rate = \audio, target, args, addAction=\addToTail, 
					closeOnCmdPeriod = true, freeOnClose = true, 
					window, step = 50, hOff = 0, vOff = 0, scopeOn = true, specs, onInit = true ;
			
		SynthDefAutogui(
			name, aSynth, rate, target,args,addAction, 
			closeOnCmdPeriod, freeOnClose , 
			window, step, hOff, vOff, scopeOn, specs, onInit)
	}

}


+ Synth {
	
	autogui { arg rate = \audio, closeOnCmdPeriod = true, freeOnClose = false, 
					window, step = 50, hOff = 0, vOff = 0, scopeOn = true, specs, onInit = true ;
		SynthDefAutogui
			(defName, this, rate, 
				closeOnCmdPeriod: closeOnCmdPeriod, freeOnClose: freeOnClose, 
				window: window, hOff: hOff, vOff: vOff, step:step, scopeOn:scopeOn, specs:specs, onInit:onInit)
	}
}
