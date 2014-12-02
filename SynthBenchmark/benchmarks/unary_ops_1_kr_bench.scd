SynthDef(
	\unary_ops_1_kr,
{

var src;
Out.ar(0, K2A.ar(300.collect{

	src = WhiteNoise.kr;
	
	(src.cubed.ampdb + 0.01).dbamp.sqrt.cos.neg.exp.floor.log.midicps.cpsmidi.softclip.distort

}.mean))

})
