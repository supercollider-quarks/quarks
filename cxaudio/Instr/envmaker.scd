

Instr("envmaker.fixed.triangle",{ arg dur=1.0;
	Env.triangle(dur)
},[
	StaticSpec(0.01,10.0,\exp)
],\envtriangle);


Instr("envmaker.fixed.sine",{ arg dur=1.0;
	Env.sine(dur)
},[
	StaticSpec(0.01,10.0,\exp)
],\envsine);

Instr("envmaker.fixed.perc",{ arg attackTime=0.01, releaseTime=1.0, curve = -4.0;
	Env.perc(attackTime,releaseTime,1.0,curve)
},[
	StaticSpec(0.01,10.0,\exp),
	StaticSpec(0.05,10.0,\exp),
	StaticSpec(-20.0,20.0,\lin)
],\envperc);


Instr("envmaker.fixed.linen",{ arg attackTime=0.01, sustainTime=1.0,releaseTime=1.0, curve = -4.0;
	Env.linen(attackTime,releaseTime,1.0,curve)
},[
	StaticSpec(0.01,10.0,\exp),
	StaticSpec(0.05,10.0,\exp),
	StaticSpec(0.05,10.0,\exp),
	StaticSpec(-20.0,20.0,\lin)
],\envperc);


//
//	*cutoff { arg releaseTime = 0.1, curve = \lin;
//		^this.new([level, 0], [releaseTime], curve, 0)
//	}
//	*dadsr { arg delayTime=0.1, attackTime=0.01, decayTime=0.3, 
//			sustainLevel=0.5, releaseTime=1.0,
//				peakLevel=1.0, curve = -4.0, bias = 0.0;
//		^this.new(
//			[0, 0, peakLevel, peakLevel * sustainLevel, 0] + bias,
//			[delayTime, attackTime, decayTime, releaseTime], 
//			curve,
//			3
//		)
//	}
//	*adsr { arg attackTime=0.01, decayTime=0.3, 
//			sustainLevel=0.5, releaseTime=1.0,
//				peakLevel=1.0, curve = -4.0, bias = 0.0;
//		^this.new(
//			[0, peakLevel, peakLevel * sustainLevel, 0] + bias,
//			[attackTime, decayTime, releaseTime], 
//			curve,
//			2
//		)
//	}
//	
//	*asr { arg attackTime=0.01, sustainLevel=1.0, releaseTime=1.0, curve = -4.0;
//		^this.new(
//			[0, sustainLevel, 0],
//			[attackTime, releaseTime], 
//			curve,
//			1
//		)
//	}
//
