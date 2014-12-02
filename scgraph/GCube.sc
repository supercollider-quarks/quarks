GCube : UGen {
	*gr {
		arg size = 1.0;
		^this.multiNew('audio', size);
	}
}

