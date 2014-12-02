SynthDef(
	\binary_ops_1,
{

var src1, src2;
Out.ar(0, 20.collect{

	src1 = WhiteNoise.ar;
	src2 = WhiteNoise.ar;
	
	((src1 * src2) + src1)
		+
	max(src1, src2)
		+
	min(src1, src2)
		-
	src1.pow(0.9).mod(0.3)
		+
	((src2 * 100).round - src1)
		+
	thresh(src1, src2)

}.mean)

})
