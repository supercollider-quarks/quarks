// convert unipolar to bipolar range,
// and optionally add some noise for control value dithering.

+ SimpleNumber {
	unibi { |noise = 0.0| ^(this + noise.bilinrand).clip(0,1) * 2 - 1 }
	biuni { |noise = 0.0| ^(this + 1 * 0.5 + noise.bilinrand).clip(0,1) }
}

+ Collection {
	unibi { |noise = 0.0| ^this.collect(_.unibi(noise)) }
	biuni { |noise = 0.0| ^this.collect(_.biuni(noise)) }
}

+ AbstractFunction {
	unibi { arg function = 0.0, adverb; ^this.composeBinaryOp('unibi', function) }
	biuni { arg function = 0.0, adverb; ^this.composeBinaryOp('biuni', function) }
}
