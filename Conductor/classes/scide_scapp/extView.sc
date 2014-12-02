+SCView {

	normalizeXY { | x, y |			// turn mouse in pixels to [0,1] range
		var b = this.bounds;
		x = (x - b.left)/b.width;
		y = 1 - ( (y - b.top)/b.height);
		^[x, y]
	}

}

+SCEnvelopeView {

	addPointAtMouse { | x, y |
		var xs, ys, i, val;
		#x, y = this.normalizeXY(x, y);
		#xs, ys = this.value;
		i = xs.indexOfGreaterThan(x);
		val = Env.perform(\new, ys, xs.differentiate[1..], this.curves).at(x);
		this.value = [
			xs[0..i-1] ++ x ++ xs[i..],
			ys[0..i-1] ++ val ++ ys[i..]
		];
		this.curves = this.curves[0..i - 1] ++ this.curves[i-1] ++ this.curves[i..];
		this.doAction;
	}

	curveSegAtMouse { | x, y, i |
		var xs, ys, j, x1, x2, y1, y2, weight, dev;
		#x, y = this.normalizeXY(x, y);
		#xs, ys = this.value;
		i = i ?? { xs.indexOfGreaterThan(x) };
		if (i.notNil) {
			i = i max: 1;
			x1 = xs[i-1];
			x2 = xs[i];
			y1 = ys[i-1];
			y2 = ys[i];
			weight =  (x - x1)/(x2 - x1) ;
			dev = ((y2 - y1) * weight + y1) - y  * sign(y2 - y1);
			dev = dev * 10;
			this.curves = this.curves.put(i-1, dev);
			this.doAction;
			^i;
		}
	}

	levelsTimes {
		var levels, times;
		#times, levels = this.value;
		times = times.differentiate[1..];
		^[levels, times]
	}

//	computeBendEnvelope { | range = 60 |
//		// env
//		var levels, timepoints, durs, difs, warpFactors;
//		#timepoints, levels = this.value;
//		levels = (levels * range - (range/2)).midiratio;
//		difs = levels.differentiate[1..];
//		durs = timepoints.differentiate[1..];
//		warpFactors = difs.collect({ | d, i |
//			if ( abs(d) > 0.0001) { log(levels[i+1]) - log(levels[i])/d } { 1/levels[i] };
//		});
//		durs = warpFactors * durs;
//		^[levels, durs, 'exp'];
//	}

}
