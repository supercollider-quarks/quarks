// wslib 2011

// thresh and knee args are in dB
// knee: 0-xx knee range; amount of dB's under *and* above thresh; 0 means no knee
// makeUp: 0-1; make up gain - 0 means no gain, 1 means max gain
// rms: 0-xx amount of rms samples (runs in peak mode if 0 (default) )
// ratio: dB ratio above thresh; 1 (default means no compression)

// using .ar or .kr doesn't change how it works internally. The compressor uses the appropriate
// rates for the input signal and control signal. If the control and/or the input are audio rate,
// the whole ugen becomes audio rate, otherwise it operates at control rate. If the control input
// is control rate, the amplitude changes also happen at control rate.
// The input can also be scalar rate, but the control input cannot. 

/*

(
// plot the compressor curve with 3 different knee settings
{ var sig;
	sig = Line.ar(-60,0,0.1).dbamp;
	SoftKneeCompressor.ar( 
		sig,				// input (also used as control)
		thresh: -20,  	// dB
		ratio: 0.125,  	// dB ratio above thresh
		knee: 10, 	// number of dB's around thresh
		makeUp: 0		   	// amount of make-up gain (dB ratio)
	).ampdb; 				// convert to dB's
	
}.plot2( 0.1, minval: -60, maxval: 0 ).superpose_(true);
)

(
// plot the reduction curve
{ var sig;
	sig = Line.ar(-60,0,0.1).dbamp;
	SoftKneeCompressor.ar( 
		1, 	// use 1 as input signal to multiply
		sig,	// use the actual signal as control
		thresh: -20, 
		ratio: 0.125, 
		knee: [0,10,20],
		makeUp: 0
	).ampdb.neg; 
	
}.plot2( 0.1, minval: 0, maxval: 60 ).superpose_(true);
)

*/

SoftKneeCompressor {
	
	*ar { |in, control, thresh = -10, ratio = 1, knee = 6, attack = 0, release = 0.05, 
				makeUp = 0, rms = 0|
		if( rms > 0 ) {
			^this.rms( in, control ? in, thresh, ratio, knee, attack, release, makeUp, rms );
		} {	
			^this.peak( in, control ? in, thresh, ratio, knee, attack, release, makeUp );
		};
	}
	
	*kr { |in, control, thresh = -10, ratio = 1, knee = 6, attack = 0, release = 0.05, 
			makeUp = 0, rms = 0|
		// detects internally whether it is kr or ar
		^this.ar( in, control ? in, thresh, ratio, knee, attack, release, rms);
	}
	
	*peak { |in, control, thresh = -10, ratio = 1, knee = 6, attack = 0, release = 0.05, 
			makeUp = 0|
		// accepts both control and audio rate
		var db, reduction, frac;
		
		control = control ? in;
		
		makeUp = ( (thresh.neg * ( 1 - ratio )) * makeUp ).dbamp; // autogain
		
		db =  Amplitude.perform( Amplitude.methodSelectorForRate( control.rate ),
			control, attack, release 
		).ampdb;
		
		frac = ((db+(knee-thresh))/(2 * knee.max(1e-12))).clip(0,1);
		
		ratio = 1 + (frac * (ratio - 1));
		thresh = thresh - ( (1-frac) * knee );
		
		reduction = ((thresh - db).min(0) * (1-ratio)).dbamp;

		^reduction * in * makeUp;
	}
	
	*rms { |in, control, thresh = -10, ratio = 1, knee = 6, attack = 0, release = 0.05, 
			makeUp = 0, rms = 40|
		// takes an rms value for control
		control = control ? in;
		control = ( RunningSum.perform( RunningSum.methodSelectorForRate( control.rate ),
			control.squared, rms ) * ( rms.reciprocal ) ).sqrt;
		^this.peak(  in, control, thresh, ratio, knee, attack, release, makeUp);
	}
	
	*d { |in, control, thresh = -10, ratio = 1, knee = 6, attack = 0.05, release = 0.1, 
			makeUp = 0, rms = 0|
		// delays input by attack time
		control = control ? in;
		in = DelayC.perform( DelayC.methodSelectorForRate( in.rate ), 
				in, 0.2.max(attack), attack );
		^this.ar( in, control, thresh, ratio, knee, attack, release, makeUp, rms );
	}

}

+ Amplitude {
	*new { |in| ^in.abs } // does this interfere with anything?
}