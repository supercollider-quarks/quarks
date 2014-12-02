// little class for getting the frames of the markers from a FCP XML file
// other uses aren't tested but may work for uncomplicated tagging systems

MovieMarkers {
	var <domDoc;
	var <names, <frames, <times, <lengths, <durations;
	
	*new {|path|
		^super.new.init(path)
	}
	
	init {|path|
		if(path.isNil) { 
			Error("No path entered.\nPlease specify an XML File and a search tag").throw 
		};
		this.fillWithFile(path)	
	}
	
	fillWithFile { |path|
		domDoc = DOMDocument(path);
		frames = this.findTags("in").collect(_.asInteger);
		times = frames.framesecs;
		names = this.findTags("name").collect(_.asSymbol);
		lengths = this.findTags("out").collect(_.asInteger);
		durations = this.lengths.collect { |x| if(x < 0) { nil } { x.framesecs } };
	}
	
	findTags { |tag|
		^domDoc.getElementsByTagName("xmeml").at(0)
			.getElementsByTagName("sequence").at(0)
			.getElementsByTagName("marker").collect({|element|
						element
						.getElementsByTagName(tag)
						.at(0)
						.getText;
			});
	}


}

+ SimpleNumber {

	framesecs { |fps=25|
		^this/fps
	}
	
}

+ SequenceableCollection {

	relativeTime {
		^this.differentiate
	}

	framesecs { |fps|
		^this.performUnaryOp('framesecs', fps)
	}
}


