GTranslate : UGen {
	*gr { arg in, displacement_vector = [0, 0, 0];
		^this.multiNew(
			'audio', 
			in, 
			displacement_vector[0], displacement_vector[1], displacement_vector[2]
		);
	}
}

