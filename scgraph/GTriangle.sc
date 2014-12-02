GTriangle : UGen {
	*gr { arg vector1 = [0, 0, 0], vector2 = [1, 0, 0], vector3 = [1, 1, 0], normal = [0, 0, 1];
		if (((vector1 ++ vector2 ++ vector3 ++ normal).size == 12), {
			^this.multiNewList(['audio'] ++ vector1 ++ vector2 ++ vector3);
		}, {
			^this.multiNew('audio', 'argh, vector and normal in wrong format');
		})
	}
}

