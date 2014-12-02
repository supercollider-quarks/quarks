SynthDef(
       \bufdelays_1,
{
       var src, delay, buf;
       src = WhiteNoise.ar;
       Out.ar(0, 250.collect{|i|
               buf = LocalBuf(SampleRate.ir * 6);
               delay = Rand(0.01, 3.0);

               [BufDelayN, BufDelayL, BufDelayC].wrapAt(i).ar(buf, src, delay)

}.mean)

})
