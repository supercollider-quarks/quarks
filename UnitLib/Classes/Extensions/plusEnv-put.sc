+ Env {
	
	put { |time = 0, level = 1, precision = 0.001| // put a level at an absolute time
		var absTimes, index;
		absTimes = [0] ++ this.times.integrate;
		index = absTimes.detectIndex({ |item| time.equalWithPrecision( item, precision ) });
		if( index.notNil ) {
			this.levels = this.levels.copy.put( index, level );
		} {
			index = absTimes.detectIndex({ |item| item > time }) ?? { absTimes.size };
			absTimes = absTimes.insert( index, time );
			this.times = absTimes.differentiate[1..];
			this.levels = this.levels.copy.insert( index, level );
		};
	}
}