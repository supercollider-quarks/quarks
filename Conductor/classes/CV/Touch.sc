
/* 
Touch provides a "touch" style control for CV's.  
The CV responds in a normal manner as long as it is untouched.
When the Touch is changed, the CV is changed to the new value and becomes 'touched'

Changes to a touched CV are gradually interpolated into place.
The interpolation value is directly related to how long ago the CV was touched.
The parameter 'dur' determines how long a CV remains in the touched state.
The parameter 'delta' determines how frequently the touched CV's value is updated.


Touch acts both as a controlspec and as a surrogate CV.

example:
(
a = Conductor.make{| con, a| 
	~touchA = a.touch(10);
	con.gui.keys = con.gui.keys.add(\touchA);	
};

a.show;

) 
*/
Touch : ControlSpec {
	var <>cv, <>newInput,  <>touchInput;
	var <>untouchedFlag, <>dur, <>delta, <>startTime;
	
	*new { | spec, cv, dur, delta |
		^super.new(spec.minval, spec.maxval, spec.warp, spec.step, spec.default, spec.units)
			.cv_(cv).dur_(dur ? 1).delta_(delta ? 0.05).untouchedFlag_(true)
	}
	
	asTouch {^this}

// Touch replaces the CV's standard constrain with its own
// It is the same as before for untouched CV's
// CV's that have been touched have their values updated by the function being
// scheduled by AppClock
	constrain { | val|
		if (untouchedFlag) {
			val = super.constrain(val);		// constrain value
			cv.setValue(val);				// set the value of the CV 
			this.changed(\synch);			// so we can update the Touch's dependents
			^val;						// then return the value to the CV
										// which will set its value once again
										// and update its own dependents
		} {
			newInput = this.unmap(super.constrain(val));
			^cv.value
		}
	}
	
	cmdPeriod { untouchedFlag = true }
	
	touch { | in |
		touchInput = in max: 0 min: 1;
		newInput = touchInput;
		startTime = Main.elapsedTime;
		if (untouchedFlag) {
			untouchedFlag = false;
			CmdPeriod.add(this);
			AppClock.sched (0, { var interp;
				
				interp = Main.elapsedTime - startTime/dur.value min: 1 ;
				cv.setValue(this.map((interp * newInput) + (1 - interp * touchInput)));
				this.changed(\synch);		// update dependents of both the Touch
				cv.changed(\synch);		// and the CV
				if (interp != 1) {
					delta
				} {
					CmdPeriod.remove(this);
					untouchedFlag = true;
					nil
				}
			});
			
		};
	}	

	input_ { | in | 
		this.touch(in);
		cv.setValue(this.map(in max: 0 min: 1)) 
	}
	value_ { | val | 
		this.touch(this.unmap(val));
		cv.setValue(super.constrain(val));
		this.changed(\synch); 
	}

	input { ^this.unmap(cv.value) }
	value { ^cv.value }

	draw { |win, name =">"|
		if (this.value.isKindOf(Array) ) {
			~multicvGUI.value(win, name, this);
		} {
			~cvGUI.value(win, name, this);
		}
	}		

}