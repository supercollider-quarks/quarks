

P3n1 : FilterPattern {
	var <>endWhen;
	*new { arg pattern, endWhen;
		^super.new(pattern).endWhen_(endWhen)
	}
	embedInStream { arg inval;
		var str, x, func;
		str = pattern.asStream;
		func = { |x, inval|
				x = x.asInteger;
			 	x = if(x.even) { x div: 2 } { x * 3 + 1 };
				if(endWhen == x) { nil } { x }
		}.flop;
		
		loop {
			if(x.isNil) { 
				x = str.next(inval);
				if(x.isNil) { ^nil.alwaysYield }
			} {
				inval = x.yield;
				x = func.(x, inval).unbubble
			};
			
		};
	}
	
	storeArgs {
		^[pattern] ++ endWhen
	}
}


/*
P3n1(10).asStream.nextN(15) // no end
P3n1(Pn(10,1),1).asStream.nextN(15) // end with 1

P3n1(Pseq([10, 11]), 1).asStream.nextN(24) // series of starts

P3n1([101, 103]).asStream.nextN(10) // multichannel expansion
*/


// the more general formulation of the collatz problem, not in its 3n + 1 form
// watch out for numerical overflow!

Pcollatz : FilterPattern {
	var <>rules, <>endCondition;
	
	*new { arg pattern, rules, endCondition = false;
		^super.new(pattern).rules_(rules).endCondition_(endCondition)
	}
	
	embedInStream { arg inval;
		var str, x, func;
		str = pattern.asStream;
		func = { |x, inval|
			x = this.calcValue(x.asInteger);
			if(endCondition.value(x)) { nil } { x }
		}.flop;
		
		loop {
			if(x.isNil) { 
				x = str.next(inval);
				if(x.isNil) { ^nil.alwaysYield }
			} {
				inval = x.yield;
				x = func.(x, inval).unbubble;
			};
		};
	}
	
	calcValue { arg x;
		rules.pairsDo { |key, val|
			if(key.(x)) { ^val.(x) };
		};
		^nil
	}
	
	storeArgs {
		^[pattern, rules, endCondition]
	}
}

/*
(
a = Pcollatz(10, [
	{ |x| x % 2 == 0 }, { |x| x div: 2 },
	{ |x| x % 2 == 1 }, { |x| x * 3 + 1 },
]);
a.asStream.nextN(18);
);
*/


// Pascal Michel's collatz-like functions.
// after http://logica.ugent.be/liesbeth/TagColOK.pdf
// quoting Pascal Michel Small Turing machines and generalized Busy Beaver competition, 
// in: Theoretical Computer Science 326

PcollatzLike : Pcollatz {
	var <mod, <muls, <adds;
	*new { arg pattern, mod = 2, muls = [1, 6], adds = [0, 4], endCondition = false;
		^super.new(pattern, nil, endCondition).makeRule(mod, muls, adds)
	}
	
	makeRule { arg argMod, argMuls, argAdds;
		muls = argMuls;
		adds = argAdds;
		mod = argMod;
		mod.do { |i|
			rules = rules.add( { |x| x % mod == i } );
			rules = rules.add( { |x| 
				muls[i] * (x - i) 
					div: 
					mod 
				+ adds[i] 
			} )
		};
		
	}
	storeArgs {
		^[pattern, mod, muls, adds]
	}
}


/*
a = PcollatzLike(10, 2, [1, 6], [0, 4]);
b = P3n1(10);
a.asStream.nextN(18);
b.asStream.nextN(18);

(
SynthDef("sine", { arg out=0, freq=1000, attack=0, sustain=0.1, release=0.05, amp=0.1, iphase=0, pan; 
	var env, u;
	amp = AmpCompA.kr(freq) * amp;
	env = EnvGen.ar(
		Env(
			[0, amp, amp, 0], 
			[attack, sustain, release], 
			[\lin, \lin, \lin]
		),
		doneAction:2);
	u = Pan2.ar(SinOsc.ar(freq, iphase, env), pan);
	OffsetOut.ar(out, u)
}).store;
);

(
Pdef(\x, 
	Pbind(
		\instrument, \sine,
		\freq, PcollatzLike([111, 14, 23], 2, [1, 2], [0, 8]).trace * 30 + 300,
		\dur, 0.14
	)

).play
);
*/

/*
a =  { |mod=2| PcollatzLike(5, mod, { 10.rand }.dup(mod), { 10.rand }.dup(mod)) };
(
x = a.value(5.rand + 2);
x.postcs;
y = x.asStream;
y.nextN(100).postcs; "";
)
*/

