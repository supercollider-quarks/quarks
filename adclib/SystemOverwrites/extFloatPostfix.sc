+ Float {
	asCompileString {
		^if (this.frac == 0.0) {
			this.asString ++ ".0"
		} { this.asString }
	}
}