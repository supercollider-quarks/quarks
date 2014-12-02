GTransform : UGen {
	*gr { arg in, components = [1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1];
		if ((components.size == 9), {
			^this.multiNewList(['audio'] ++ components);
		}, {
			^this.multiNew('audio', 'component size is not == 9');
		})
	}
}

