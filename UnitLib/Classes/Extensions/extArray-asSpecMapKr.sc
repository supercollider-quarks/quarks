/*
use like this:

(
SynthDef( "test_map", {
	var sig, freq;
	freq =  \freq.asSpecMapKr( Line.kr(0,1,10,doneAction: 2) );
	Out.ar( 0, SinOsc.ar( freq, 0, 0.1) );
}).add;
)

x = Synth( "test_map", [ \freq, [220,440,\exp].asSpec ] );
*/

+ ControlSpec {
	
	asControlInput {
		var curveval, curvenum;
		curvenum = warp.asSpecifier;
		if( curvenum.isNumber ) {
			curveval = curvenum;
			curvenum = 5;
		} {
			curvenum = ( 
				\lin: 1, 
				\linear: 1, 
				\exp: 2, 
				\exponential: 2, 
				\cos: 3, 
				\sin: 4, 
				\amp: 6, 
				\db: 7
			)[curvenum ];
		};
		^[ minval, maxval, curvenum, curveval ? -2, step ];
	}
	
	asOSCArgEmbeddedArray { | array| ^this.asControlInput.asOSCArgEmbeddedArray(array) }
}

+ Spec {

	asControlInput {
		^this.asControlSpec.asControlInput;
	}
	
	asOSCArgEmbeddedArray { | array| ^this.asControlInput.asOSCArgEmbeddedArray(array) }
}

+ Array {
	
	asSpecMapKr { |value = 0|
		var minval, maxval, curvenum, curve, step;
		var range, dbrange, ratio, grow, a, b;
		#minval, maxval, curvenum, curve, step = [0,1,1,-2,0].overWrite( this );
		value = value.clip(0,1);
		range = maxval - minval;
		dbrange = maxval.dbamp - minval.dbamp;
		ratio = maxval / minval.max(1e-12);
		curve = if( InRange.kr(curve, -0.001, 0.001 ), 0.001, curve );
		grow = exp(curve);
		a = range / (1.0 - grow);
		b = minval + a;
		^Select.kr( curvenum, [
			value.round(1) * range + minval, // step
			value * range + minval, // lin
			(ratio ** value) * minval, // exp
			(0.5 - (cos(pi * value) * 0.5)).linlin(0,1,minval,maxval), // cos
			sin(0.5pi * value).linlin(0,1,minval,maxval), // sin
			b - (a * pow(grow, value)), // curve
			if(range >= 0,  // amp
				value.squared * range + minval, 
				(1 - (1-value).squared) * range + minval 
			),
			if(dbrange >= 0, // db
				(value.squared * range + minval.dbamp).ampdb, 
				((1 - (1-value).squared) * range + minval.dbamp).ampdb 
			)
		]).round(step);
	}
}

+ Symbol {
	asSpecMapKr { |value = 0| ^this.kr([0,1,1,-2,0]).asSpecMapKr(value); }
}