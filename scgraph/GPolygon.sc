GPolygon : UGen {
	*gr { arg vectors = [[0,0,0]], normal = [0, 0, 1];
		// we need to pass the number of vectors, so the unit knows what
		// control inputs are valid
		if (((vectors.flat.size / vectors.size) == 3), {
			^this.multiNewList(['audio'] ++ vectors.size ++ vectors.flat ++ normal);
		}, {
			^this.multiNew('audio', 'vector not array of arrays of size 3');
		})
	}
}

