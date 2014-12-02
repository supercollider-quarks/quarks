Shepard { 
		// if all inputs are static, can return static values
	*new { |num = 5, interval = 12, shift = 0, ampExp = 2, masterPhase=0|
		var allPhases, amps, intervals;	
		shift = shift / num; 
		allPhases = (masterPhase + ((0.. num - 1) / num) - shift).wrap(-0.5, 0.5);
		amps = allPhases.collect { |x| cos(x * pi) ** ampExp }; 
		allPhases = allPhases + shift;
		intervals = interval * (allPhases * num);
		
		^[intervals, amps]
	}
		// provide automatic slope if needed, else like *new.
	*kr { |num = 5, slope = 0.02, interval = 12, shift = 0, ampExp = 2|
		var masterPhase = if (slope == 0) { 0 } { 
			A2K.kr(Phasor.ar(1, slope / SampleRate.ir, 0, 1));
		}; 
		^this.new(num, interval, shift, ampExp, masterPhase);
	}
}

ShepardG : Shepard { 
		// gaussian formula, adapted to use ampExp - hack! 
	*new { |num = 5, interval = 12, shift = 0, ampExp = 2, masterPhase=0|
		var allPhases, amps, intervals;	
		shift = shift / num; 
		allPhases = (masterPhase + ((0.. num - 1) / num) - shift).wrap(-0.5, 0.5);
		amps = allPhases.collect { |x| exp(squared(x * 2) / (-2.0 * squared( 0.5 ** ampExp))) }; 
		allPhases = allPhases + shift;
		intervals = interval * (allPhases * num);
		
		^[intervals, amps]
	}
}