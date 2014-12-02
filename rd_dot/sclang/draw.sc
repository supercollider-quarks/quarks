// draw.sc - (c) rohan drape, 2004-2007

+ SynthDef {
	draw { 
		DotViewer.draw(this);
	}
}

+ Function {
	draw { 
		DotViewer.draw(this.asSynthDef);
	}
}
