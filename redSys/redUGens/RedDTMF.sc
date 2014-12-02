RedDTMF {
	classvar dict;
	*initClass {
		dict= (
			$1 : #[697, 1209],
			$2 : #[697, 1336],
			$3 : #[697, 1477],
			$A : #[697, 1633],
			$4 : #[770, 1209],
			$5 : #[770, 1336],
			$6 : #[770, 1477],
			$B : #[770, 1633],
			$7 : #[852, 1209],
			$8 : #[852, 1336],
			$9 : #[852, 1477],
			$C : #[852, 1633],
			$* : #[941, 1209],
			$0 : #[941, 1336],
			$# : #[941, 1477],
			$D : #[941, 1633]
		)
	}
	*new {|char|
		^dict[char] ? [0, 0]
	}
	*string {|str|
		^Array.fill(str.size, {|i| this.new(str[i])})
	}
	*ar {|char, mul= 1|
		^Mix(FSinOsc.ar(this.new(char), 0, mul*0.5))
	}
	*kr {|char, mul= 1|
		^Mix(FSinOsc.kr(this.new(char), 0, mul*0.5))
	}
	*dial {|str, rate= 2, mul= 1|
		^Mix(
			SinOsc.ar(
				Demand.kr(
					Impulse.kr(rate),
					0,
					Dseq(this.string(str))
				),
				0,
				EnvGen.kr(Env.linen(0.01, rate.reciprocal*0.8, 0.01, mul), Impulse.kr(rate))
			)*EnvGen.kr(Env.linen(0, rate.reciprocal*str.size, 0, 0.5));
		)
	}
	*signal {|rate= 0.2, mul= 1|
		^FSinOsc.ar(1112)
		*FSinOsc.ar(419)
		*FSinOsc.ar(173)
		*FSinOsc.ar(13)
		*mul
		*Clip.kr(FSinOsc.kr(rate, 0, 100), 0, 1)
	}
}



/*
//--testcode

s.boot

{RedDTMF.dial("12AA34", 4)}.play
{RedDTMF.signal(0.4)}.play

{
Pan2.ar(SinOsc.ar(1112)*SinOsc.ar(419)*SinOsc.ar(173)*SinOsc.ar(13)*Clip.ar(SinOsc.ar(0.2, 0, 100), 0, 1))
}.play

*/
