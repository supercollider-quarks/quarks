GRotate : UGen {
	*gr { arg in, axis = [0, 1, 0], angle = 0;
		^this.multiNew('audio', in, axis[0], axis[1], axis[2], angle);
	}
}

