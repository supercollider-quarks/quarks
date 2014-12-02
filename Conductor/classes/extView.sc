+Integer {

	controlFlag { ^this.bitTest(0) }
	optionFlag { ^this.bitTest(5) }
	shiftFlag { ^this.bitTest(1) }

}

+Env {

	rateWarp {
		// Env defines playback rates as rotios with exponential interpolation
		var newtimes, l1, l2, dif, warp, warpedTimes;
		warpedTimes = times.collect({ | t, i |
			l2 = levels[i+1];
			l1 = levels[i];
			dif = l2 - l1;
			if (abs(dif) > 0.0001) {
				warp = log(l2) - log(l1)/dif
			} {
				warp = 1/l1
			};
			t * warp
		});
		times = warpedTimes;
		curves = warpedTimes.collect { 'exp' };
	}
}
