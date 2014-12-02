GLine : UGen {
	*gr { arg strength = 1.0, vector1 = [0, 0, 0], vector2 = [1, 1, 0];
		^this.multiNew('audio', strength,  vector1[0], vector1[1], vector1[2], vector2[0], vector2[1], vector2[2]);
	}
}

