+ Stream {
	setConvert {
		^this.collect { |set|
			set.asArray.flat.sort
		}
	}
}

+ Pattern {
	setConvert {
		^this.collect { |set|
			set.asArray.flat.sort
		}
	}
}

