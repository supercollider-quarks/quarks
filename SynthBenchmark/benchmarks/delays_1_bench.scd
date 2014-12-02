SynthDef(
	\delays_1,
{

var src, delay;
src = WhiteNoise.ar;
Out.ar(0, 250.collect{|i|

	delay = Rand(0.01, 3.0);
	
	[DelayN, DelayL, DelayC].wrapAt(i).ar(src, delay, delay)
	
}.mean)

})
