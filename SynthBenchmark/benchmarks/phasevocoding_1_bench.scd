SynthDef(\phasevocoding_1, {
var bufs1, chains1;

bufs1 = [512, 1024, 2048, 4096].stutter(10).collect{|f| LocalBuf(f) };
chains1 = bufs1.collect{|b| FFT(b, WhiteNoise.ar) };

chains1 = chains1.collect{|chain|
	chain = PV_MagAbove(chain, 0.01);
	chain = PV_MagBelow(chain, 10000.01);
	chain = PV_MagMul(chain, chain);
	chain = PV_BrickWall(chain, 0.5);
};

Out.ar(0, chains1.collect{|ch| IFFT(ch)}.mean)

})