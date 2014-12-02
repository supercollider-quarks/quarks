
+ Ref {
	draggedIntoVoicerGUI { |dest| value.draggedIntoVoicerGUI(dest) }
}

+ Object {
	draggedIntoVoicerGCGUI { |gui|
		if(this.respondsTo(\asSpec)) {
			gui.model.spec = this.asSpec;
		}
	}

	// asTestUGenInput { ^this.asUGenInput }
}

	// needed because Cocoa GUI no longer interprets strings for you
+ String {
	draggedIntoVoicerGUI { |gui|
		^this.interpret.draggedIntoVoicerGUI(gui)
	}

	draggedIntoVoicerGCGUI { |gui|
		^this.interpret.draggedIntoVoicerGCGUI(gui)
	}
}
