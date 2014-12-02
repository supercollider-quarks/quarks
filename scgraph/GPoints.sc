GPoints : UGen {
	*gr { arg size = 1.0, vectors = [[0, 0, 0]];
		if (((vectors.flat.size / vectors.size) == 3), {
			^this.multiNewList(['audio'] ++ size ++ vectors.size ++ vectors.flat);
		}, {
			^this.multiNew('audio', size, 'vectors not Array of Arrays of size 3');
		})
	}
}
