// this file is part of redUniverse /redFrik


+SequenceableCollection {
	fingerprint {|normalize= true| ^RedFingerprint(this, normalize)}
}

+Env {
	fingerprint {|normalize= true, size= 400| ^this.asSignal(size).fingerprint(normalize)}
}

+Wavetable {
	fingerprint {|normalize= true| ^this.asSignal.fingerprint(normalize)}
}

+ListPattern {
	fingerprint {|normalize= true|
		if(repeats==inf, {
			"sorry, cannot fingerprint inf patterns".warn;
		}, {
			^this.asStream.all.fingerprint(normalize)
		})
	}
}
