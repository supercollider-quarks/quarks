SynthDef(
	\unary_ops_1,
{

var src;
Out.ar(0, 20.collect{

	src = WhiteNoise.ar;
	
	(src.cubed.ampdb + 0.01).dbamp.sqrt.cos.neg.exp.floor.log.midicps.cpsmidi.softclip.distort

}.mean)

})
