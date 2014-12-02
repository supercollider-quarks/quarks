
TestSample : UnitTest {
	
	test_standardizePath {
		//this.assertEquals( Sample.standardizePath("a11wlk01.wav"), "a11wlk01.wav" )
		this.assert(
				Sample.standardizePath("a11wlk01.wav").find("//").isNil,
				"should not have two // anywhere in the path");
	}
	test_defaultControl {
		var s;
		s = \sample.asSpec.defaultControl;
		this.assert(s.beatsize.isNaN.not,"beatsize should not be NaN");
	}
	test_gui {
		var w,c,y,v;
		w = Window.new("soundfile should be in top left of the blue composite", Rect(200, 200, 800, 400));

		//c = SCCompositeView(w,Rect(100,100,700,300));
		//c.relativeOrigin = true;
		c = FlowView(w,Rect(100,100,700,300));

		c.background = Color.blue;

		y = Sample("a11wlk01.wav").gui(c);
		w.front;
		
		v = y.view.view.children.select(_.isKindOf(SCSoundFileView)).first;
		
		this.assert( 		c.absoluteBounds.containsRect( v.absoluteBounds ),
				"the sound file view should absolutely be inside the flow view");
		// but its in the top left corner
	}
}

