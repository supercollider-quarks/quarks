+ Collection{

	histoBands { |steps, min, max, center=0.5|
		var range, step;
		min = min ?? { this.minItem };
		max = max ?? { this.maxItem };
		range = (max - min);
		step = range / steps;
		
		^{ |i| i + center * step + min }.dup(steps);
	}
}