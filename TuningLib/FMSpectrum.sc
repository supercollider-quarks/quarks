FMSpectrum
{


	var bessel, <freqs, <amps, <raw_amps, bandwidth, <carrier, <modulator, <depth;
	
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
			
				
			