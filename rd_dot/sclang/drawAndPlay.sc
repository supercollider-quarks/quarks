// drawAndPlay.sc - (c) rohan drape, 2004-2007

// Method to compose the play and draw methods.

+ SynthDef {
	drawAndPlay { 
		this.play;
		DotViewer.draw(this);
	}
}

+ Function {
	drawAndPlay { 
		var synthDef;
		synthDef = this.asSynthDef;
		synthDef.play;
		DotViewer.draw(synthDef);
	}
}
