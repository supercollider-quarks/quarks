
+CV {	
	touch { | dur, delta |
		^spec = spec.asTouch(this, dur, delta)	
	}
	
	setValue{ | v |
		value = v;
	}

}

+ControlSpec {
	asTouch { | cv, dur, delta | ^Touch(this, cv, dur, delta) }
}



