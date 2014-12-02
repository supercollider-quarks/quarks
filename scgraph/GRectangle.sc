GRectangle : UGen {
	*gr { arg width = 1, height = 1;
		^this.multiNew('audio', width, height);
	}
}

