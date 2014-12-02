GColor : UGen {
	*gr { arg in, color = [1, 1, 1, 1];
		^this.multiNew('audio', in, color[0], color[1], color[2], color[3]);
	}
}

+ Color {
	gr {|in|
		^GColor.gr(in, this.asArray)
	}

}