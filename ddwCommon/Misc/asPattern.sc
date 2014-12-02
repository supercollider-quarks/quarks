
+ Object {
	asPattern { ^Pn(this, inf) }
}

+ Pattern {
	asPattern { ^this }
}

+ SequenceableCollection {
		// potentially dangerous?
	asPattern { ^Pseq(this, 1) }
}

+ Function {
	asPattern { ^Pfunc(this) }
}

+ PatternProxy {
	asPattern { ^pattern }
}

