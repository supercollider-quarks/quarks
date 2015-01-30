+CV {

	cvWidgetConnect { |view|
		^CV.viewDictionary[view.class].new(this, view);
	}

	cvWidgetDisconnect { |object|
		object.remove;
		// ^object = nil;
	}

	cvSplit {
		^value.collect { |v, i| CV(spec.split[i]).value_(v) }
	}

}

+Array {

	cvWidgetConnect { |view|
		^CV.viewDictionary[view.class].new(this, view);
	}

	cvWidgetDisconnect { |object|
		object.remove;
		// ^object = nil;
	}

}