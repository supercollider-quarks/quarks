Spectrum {
	var <freqs, <amps;

	*new {|freqs, amps|
		^super.newCopyArgs(freqs, amps)
	}


	+ {|other|
		var newfreqs=[], newamps, dict, item;
		dict = Dictionary.new;
		freqs.do ({|freq, index|
			dict.put(freq, amps[index]); // put freq amp pairs in
		});
		other.freqs.do({|freq, index|
			item = dict.at(freq); // Do they both have this freq?
			item.notNil.if({
				item = item + other.amps.at(index);
				dict.put(freq, item);
				}, { //else this is a unique freq
					dict.put(freq, other.amps.at(index));
			});
		});

		newfreqs = dict.keys.asArray.sort; // let's get these back in order
		newfreqs.do({|freq|
			newamps = newamps.add(dict.at(freq))
		});

		^Spectrum(newfreqs, newamps)
	}

}

FFTSpectrum : Spectrum
{


	*new{|buffer, size, cutoff = 0.001, highInterval = 1902, action|
		^super.new.init(buffer, size, cutoff = 0.001, highInterval = 1902, action);
	}

	init {|buffer, size, cutoff = 0.001, highInterval = 1902, action|

		buffer.getn(0, size, {arg buf;

			var spacing, freqs, amps, z, toRemove;

			z = buf.clump(2).flop;
			spacing = buffer.sampleRate / (size );

			freqs = [];
			amps = [];

			// get all the magnitudes from the buffer
			(size /2 ).do({ |index|

				freqs = freqs.add(index * ( 1 + spacing));
				amps = amps.add(Complex(z[0][index], z[1][index]).magnitude);
			});

			// get rid of everything below a cutoff to make processing go faster
			toRemove = [];
			amps.normalize.do({ | amp, index|
				if ( amp < cutoff, {
					//amps.removeAt(index);
					//freqs.removeAt(index);
					toRemove = toRemove.add(index);
				});
			});

			// ok, so toRemove is full of all the indexes to remove in order
			// but every time we remove one, the array gets shorter and
			// all the indexes of go off by one
			// so the thing to do is remove them in reverse order

			toRemove.reverse.do({ |zap|
				amps.removeAt(zap);
				freqs.removeAt(zap);
			});

			//this.init(freqs, amps.normalizeSum, highInterval);

			action.notNil.if({

				action.value(this)
			});
		});
	}
}



FMSpectrum : Spectrum
{


	var bessel, /*<freqs, <amps, */<raw_amps, bandwidth, <carrier, <modulator, <depth;

	*new { |carrier, modulator, depth|

		^super.new.init(carrier, modulator, depth);
	}

	init {|car, mod, dep|

		var amp, bessel, this_band, x, low, high, index, n, band;

		carrier = car;
		modulator = mod;
		depth = dep;

		x = depth / modulator;
		bandwidth = 2 * (depth + modulator) + 1;

		bessel = Bessel(x);

		freqs = [carrier];
		amps = [bessel.j0.abs];
		n = 1;
		band = modulator;

		{band < bandwidth}.while({

			amp = bessel.jn(n).abs;
			low = carrier - band;
			high = carrier + band;

			(freqs.includes(low.abs)).if({
				// an inversion of a previously computed freq
				index = freqs.indexOf(low.abs);
				amps[index] = amps[index] + (amp * low.sign);
			} , {

				freqs = [low.abs] ++ freqs;
				amps = [amp * low.sign] ++ amps;
			});

			(freqs.includes(high.abs)).if({
				// an inversion of a previously computed freq
				index = freqs.indexOf(high.abs);
				amps[index] = amps[index] + (amp * high.sign);
			} , {

				freqs = freqs ++ high.abs;
				amps = amps ++ (amp * high.sign);
			});


			n = n+1;
			band = modulator * n;
		});

		raw_amps = amps.copy; // raw amps may be negative
		amps = amps.abs;
	}



}


			