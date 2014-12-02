+ SCWindow {
	
	/*
	asNSWindow {
		var view, window;
		view = SCNSObject.newFromRawPointer(dataptr);
		window = view.invoke("window");
		view.release;
		^window; // you own it - call release once you do not need it anymore
	}
	*/
	
	background_ { |color|
		var ns;
		ns = this.asNSWindow;
		if( color.notNil )
			{
			ns.invoke("setOpaque:", [false]);
			ns.invoke("setBackgroundColor:", [color]);
			 }
			 {
			ns.invoke("setOpaque:", [true]);
			ns.invoke("setBackgroundColor:", [nil]);
			 };
		ns.release;
		}
	
}