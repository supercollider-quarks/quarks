// There are also draw and drawAndPlay methods at Function, which allows 
// one to see what would be 'play'ed.

s.boot;

(
{
	f = LFSaw.kr(0.4, 0, 24, LFSaw.kr([8,7.23], 0, 3, 80)).midicps;
	CombN.ar(SinOsc.ar(f, 0, 0.04), 0.2, 0.2, 4)
}.drawAndPlay
)
