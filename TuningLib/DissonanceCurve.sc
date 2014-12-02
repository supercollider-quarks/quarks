// This code by Charles CŽleste Hutchins, with some code ported to SC from code written by Morgan Tunder
// Mostly based on the writing of Bill Sethares
// With some additional ideas of Musicquantics by Clarence Barlow

DissonanceInterval

{

	var <dissonance, <interval, <numerator, <denominator, <cents, <>rms;
	
	*new { arg dissonance, interval;
	
		^super.new.init(dissonance, interval);
	}
	
	*newRatio {  arg dissonance, numerator, denominator;
	
		^super.new.initFromRatio(dissonance, numerator, denominator);
	}
	
	
	init { arg diss, inval;
		
		dissonance = diss;
		interval = inval;
		cents=1200/log10(2)*log10(interval);
		
		#numerator, denominator = interval.asFraction(fasterBetter: false);
		
	}
	
	initFromRatio { arg diss, num, dem;
	
		dissonance = diss;
		numerator = num;
		denominator = dem;
		
		interval = num/dem;
		
		cents=1200/log10(2)*log10(interval);
	}
	
	ratio {
	
		^ (numerator / denominator)
	}
	
	
	> { arg other;
	
		other.isKindOf(DissonanceInterval).if ({
		
			^ (interval > other.interval)
		}, {
			^nil
		});
		
	}
	
	< { arg other;
	
		other.isKindOf(DissonanceInterval).if ({
		
			^ (interval < other.interval)
		}, {
			^nil
		});
		
	}
	

	== { arg other;
	
		other.isKindOf(DissonanceInterval).if ({
		
			^ (interval == other.interval)
		}, {
			^nil
		});
		
	}
	
	
}


DissonanceCurve {

/*@
shortDesc: Analyzes timbres in order to create tunings
longDesc: Uses Bill Seathares' Dissoncance Curve algorithm in order to compute tunings for a given timbre.  
For more information, 
@*/

//see http://eceserv0.ece.wisc.edu/~sethares/consemi.html

	var <curve, <cents_scale, frequ, ampl, <just_curve, <just_scale, highInterval;

	
	*new { arg freqs, amps, highInterval = 1902;
	/*@
	desc: Create a new curve based on arrays of data. This would mostly be used for additive synthesis.
	freqs: An array of signficant frequencies of a timbre
	amps: An array of amplitudes for the given frequencies
	highInterval: The largest interval to compute in cents. For octave-based systems, you would usually use 1200.
	@*/
	
		^super.new.init(freqs, amps, highInterval);
	}
	
	*just { arg freqs, amps, highInterval = 1902;
	
		^super.new.init(freqs, amps, highInterval);
	}
	
	*fm { arg carrier, modulator, depth, highInterval = 1902;
	/*@
	desc: Create a curve based on the timbre of a given FM specification
	carrier: The carrier frequency in Hz
	modulator: The modulation frequency in Hz
	depth: The depth (or delta range) of the modulator, in Hz
	highInterval: The largest interval to compute in cents. For octave-based systems, you would usually use 1200.
	ex:
	(
		SynthDef("fm", {arg out, amp, carrier, modulator, depth;
			
			var sin;
			
			sin = SinOsc.ar(SinOsc.ar(modulator, 0, depth, carrier));
			Out.ar(out, sin * amp);
		}).memStore;
	)
	(
		var carrier, modulator, depth, curve, scale;
		
		carrier = 440;
		modulator = 600;
		depth = 100;
		
		curve = DissonanceCurve.fm(carrier, modulator, depth, 2);
		scale = curve.scale;
	
	)
	@*/
		
	
	
		^super.new.fmInit(carrier, modulator, depth, highInterval);
		
	}
	
	* fft { arg buffer, size, cutoff = 0.001, highInterval = 1902, action;
	/*@
	desc: Create a curve based on the timbre of a given FFT buffer. THIS IS VERY SLOW and uses a lot of system resources
	buffer: An FFT Buffer
	size: The size of the Bufer in Frames
	cutoff: The lower cutoff amplitude for a bin. Defaults to -60 dB
	highInterval: The largest interval to compute in cents. For octave-based systems, you would usually use 1200.
	action: A function to be evaluated once computing the curve is complete.
	@*/
	
	
		^super.new.initFromFFT(buffer, size, cutoff, highInterval, action);
	}


	fmInit { |carrier, modulator, depth,  highInterval = 1902|
	
		/*
		var bessel, x, bandwidth, amp, n, band;
		
		x = depth / modulator;
		bessel = Bessel(x);
		frequ = [carrier];
		ampl = [bessel.j0.abs];
		
		bandwidth = 2 * (depth + modulator);
		n = 1;
		band = modulator;
		
		{band < bandwidth}.while({
		
			amp = bessel.jn(n);
			frequ = [(carrier - band).abs] ++ frequ ++ (carrier + band);
			ampl = [amp] ++ ampl ++ amp;
			n = n+1;
			band = modulator * n;
		});
		
		this.init(frequ, ampl.normalizeSum, highInterval);
		*/
		
		var spectrum;
		
		spectrum = FMSpectrum(carrier, modulator, depth);
		frequ = spectrum.freqs;
		ampl = spectrum.amps.normalizeSum;
		
		this.init(frequ, ampl, highInterval);
	}

	
	initFromFFT{ | buffer, size, cutoff = 0.001, highInterval = 1902, action|
			
		// default cutoff is equal to -60.dbamp
		
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
			
			this.init(freqs, amps.normalizeSum, highInterval);
			
			action.notNil.if({
			
				action.value(this)
			});
		});
	}
	
	
	init { | freq, amp, high|
	
		// This is from Bill Sethares via Morgan Tunder
		// see http://eceserv0.ece.wisc.edu/~sethares/comprog.html
	
		var dstar, s, s1, s2, c1, c2, a1, a2, fdif, arg1, arg2, dnew, d, exp1, exp2, index,
			i, j, k, fmin, lowint, highint, intervals, allpartialsatinterval, inc, size, 
			diss, dissvalues, numpartials, interval, find_minima, prev, prever, just;


		frequ = freq;
		ampl = amp;
		highInterval = (high / 100).midiratio;

		numpartials = frequ.size;

		dstar= 0.24;  // this is the point of maximum dissonance - the value is derived from a model
			// of the Plomp-Levelt dissonance  curves for all frequencies.
		s1 = 0.0207;// s1 and s2 are used to allow a single functional form to interpolate beween
		s2 = 18.96;  //  the various P&L curves of different frequencies by sliding, stretching/compressing
		//  the curve so that its max dissonance occurse at dstar. A least-square-fit was made
							// to determine the values.
		
		c1 = 5;       // these parameters have values to fit the experimental data of Plomp and Levelt
		c2 = -5;
		a1 = -3.51; // theses values determine the rates at which the function rises and falls and 
		a2 = -5.75; // and are based on a gradient minimisation of the squared error between 
						  //  Plomp and Levelt's averaged data and the curve
		
		// If the point of maximum dissonance for a base frequency f occurs at dstar, then the dissonance between
		// f1 with amp1 and f2 with amp2, is - amp1*amp2(e^-a1s[f2-f1] - e^-a2s[f2-f1]) where s = dstar/(s1f1+f2).....
		
		//fdif will hold the difference between frequency values
	
		index = 0;

		lowint = 1 ;
		highint = highInterval.asFloat; // let's do for bohlen! 2.3 ; // sets the upper limit of intervals for which dissonance will be calculated.
		inc = 2 ** (1/1200); //0.01; do it in cents!
		
		//size=((highint-lowint)/inc); not used
	
		size = ((highint / lowint).log / inc.log).ceil;
		
		//diss = Array.newClear(size); // declare array of dissonance values for each step from lowint to highint.(1-2)
		intervals = Array.newClear(size); //declare array of intervals for final curve
		curve = Array.newClear(size);
		allpartialsatinterval = Array.newClear(frequ.size); // 1024? declare array of partials pitched up by 'interval'
		
		interval = lowint;
		{interval <= highint}. while ({ 
			//perform below for interval values lowint - highint.
			
			diss = this.pr_compute_partial_sethares(interval, numpartials);
			curve[index] = diss;
			
			if( index > 0, {
			
				if (curve[index -1].ratio != diss.ratio, {
				
					just_curve = just_curve.add(
								this.pr_compute_partial_just(diss.numerator, diss.denominator,
											numpartials));
				});
			}, {
				just_curve = just_curve.add(
							this.pr_compute_partial_just(diss.numerator, diss.denominator,
											numpartials));
		
			});
					
			index = index + 1;

			//interval = interval * inc; // multiply instead of add for cents
			interval = lowint * (2 ** (index/1200)); //0.01; do it in cents!

		});
	
	
		
		cents_scale = this.pr_find_minima(curve);
		//just_scale = this.pr_find_minima(just_curve);
		just_scale = just_curve.copy;
		just_scale = just_scale.sort({|a, b| a.dissonance < b.dissonance});
		//just_scale = just_curve.sort({|a, b| a.dissonance < b.dissonance}); // this was modifying the just_curve
		
	}
	
	
	tuning{
	/*@
	desc: Return a Tuning based on the minima of the Dissonance Curve
	@*/
		var tuning;
		
		tuning = [];
		
		cents_scale.do({|item|
		
			tuning = tuning ++ (item.cents / 100);
		});
		
		tuning = tuning.sort;
		
		^Tuning(tuning, highInterval, "Dissonance Curve");
	}
		
	scale{ |size = inf|
	/*@
	desc: Returns a scale in which the Tuning is the minima of the curve.  Every degree of the Tuning is a Scale degree.
	size: The number of degrees of the scale. For size n, it pickes the n most consonant degrees. Defaults to inf, which makes a Scale degree for every degree of the Tuning.

	@*/
		var tuning, degrees, scale, tune, tuning_cents, index;
		
		tuning = this.tuning;
		
		(size < tuning.size). if({
		
			// pick the size most consontant degrees
			degrees = [];
			tuning_cents = tuning.cents;
			scale = cents_scale.sort({|a, b| a.dissonance < b.dissonance});
			
			size.do({ |i|
			
				tune = scale[i].cents;
				index = tuning_cents.indexInBetween(tune);
				index = index.round.asInt;
				degrees = degrees ++ index;
			});
			
			degrees = degrees.sort;
			//degrees.post;
			
		} , {
			// take all of the degrees
			degrees = Array.series(tuning.size, 0, 1);
		});
		
		^Scale(degrees, tuning.size, tuning: tuning, name: tuning.name);
	
	}
	
	plot {
	
	/*@
	desc: Plot the DissonanceCurve
	ex:

	(
		d = DissonanceCurve.fm(1563, 560, 1200, 2400);
		d.plot;
	)

	@*/
	
		var dissonances;
		
		dissonances = curve.collect({|diss| diss.dissonance});
		dissonances.plot;
	}
		
		
	digestibleTuning { |window = 100|
	/*@
	desc: Returns a Tuning related to Sethares' algorithm, but calculated using a modification of 
	Clarence Barlow's Digestibility Formula.  Basically, it does a comparison of every partial at 
	every tuning, but instead of looking at Plomp and Levit's ideas of local consonance, it 
	compares the ratio relationships between the partials.  The numerator and denominator of this 
	ratio is summed and then multiplied by the amplitude of the quieter partial.
	
	This algorythm just gives a list of the relative dissonance at all possible tunings. 
	To figure out scale degrees, the most consonant tuning is picked from it's neighbors, 
	given the window size.
	
	The tuning generated by this way and by the Sethares / Plomp and Levit algorithms may
	have nothing to do with each other.
	
	window: The idealized step size, in cents. To do normal-ish semitones, 
	this would be set to 100
	ex:
	
	(
	
		d = DissonanceCurve.fm(100, 200, 300, 1200);
		t = d.digestibleTuning(100);
		t.cents.postln;
	)
	
	@*/
	
		var tuning;
		
		tuning = [];
		
		this.pr_find_just_scale(window);
		
		just_scale.do({|item|
		
			tuning = tuning ++ (item.cents / 100);
		});
		
		tuning = tuning.sort;
		
		^Tuning(tuning, highInterval, "Dissonance Curve");
	}
	
	
	digestibleScale{ |window = 100, size = inf|
	/*@
	desc: Returns a scale based on the digestibleTuning.	window: The window size used to compute the digestibleTuning
	size: The number of degrees of the scale. For size n, it pickes the n most consonant degrees. Defaults to inf, which makes a Scale degree for every degree of the Tuning.
	@*/
		var tuning, degrees, scale, tune, tuning_cents, index;
		
		tuning = this.digestibleTuning(window);
		
		(size < tuning.size). if({
		
			// pick the size most consontant degrees
			degrees = [];
			tuning_cents = tuning.cents;
			scale = just_scale.sort({|a, b| a.dissonance < b.dissonance});
			
			size.do({ |i|
			
				tune = scale[i].cents;
				index = tuning_cents.indexInBetween(tune);
				index = index.round.asInt;
				degrees = degrees ++ index;
			});
			
			degrees = degrees.sort;
			//degrees.post;
			
		} , {
			// take all of the degrees
			degrees = Array.series(tuning.size, 0, 1);
		});
		
		^Scale(degrees, tuning.size, tuning: tuning, name: tuning.name);
	
	}

	
		
	// 'private' methods	

	pr_compute_partial_sethares{ |interval, numpartials|
			
		var s1, s2, c1, c2, d, allpartialsatinterval,fmin, s, fdif, 
			arg1, arg2, exp1, exp2, dnew, dstar, a1, a2;
		
		numpartials.isNil.if({ numpartials = frequ.size; });

		dstar= 0.24;  // this is the point of maximum dissonance - the value is derived from a model
			// of the Plomp-Levelt dissonance  curves for all frequencies.
		s1 = 0.0207;// s1 and s2 are used to allow a single functional form to interpolate beween
		s2 = 18.96;  //  the various P&L curves of different frequencies by sliding, stretching/compressing
		//  the curve so that its max dissonance occurse at dstar. A least-square-fit was made
							// to determine the values.
		
		c1 = 5;       // these parameters have values to fit the experimental data of Plomp and Levelt
		c2 = -5;
		a1 = -3.51; // theses values determine the rates at which the function rises and falls and 
		a2 = -5.75; // and are based on a gradient minimisation of the squared error between 
						  //  Plomp and Levelt's averaged data and the curve
		

		
		d=0;
		// fill allpartialsatinterval array with each element of freq array multiplied by interval.
		
			
		allpartialsatinterval = frequ * interval;
		
		// Calculate the dissonance between frequ[] and interval*frequ[]
		frequ.do({ |freq, i|
			allpartialsatinterval.do({ |partial, j|
				// if an element of allpartials is less than this element of freq
				// then give its value to fmin
				//(allpartialsatinterval[j]<frequ[i]).if(//if 1.x by each fq component is less than the current component
					//	{

					//		fmin = allpartialsatinterval[j] ;
							//fmin takes on the lesser of freq*1.x and frequ
					//	}, { //else 
							// fmin takes on the current frequ value  
					//		fmin = frequ[i] ;
					//	});
					
				fmin = partial.min(freq);
							
				s=dstar/((s1*fmin)+s2); 
				// define s with interpolating values s1 and s2.
				fdif=(partial-freq).abs ; // (fabs gives absolute value)
				//	establishes the frequency difference

				arg1=a1*s*fdif; arg2=a2*s*fdif ; // gives arg1/arg2 the powers for e [i.e., as(f2-f1)] 
				// in the equation for dissonance given above.

				exp1=arg1.exp; // EXP returns a value containing e
				//(the base of natural logarithms) raised to the specified power.

				exp2=arg2.exp;

				(ampl[i]<ampl[j]).if({

					dnew=ampl[i]*((c1*exp1)+(c2*exp2));//  The lesser of amp(i) and amp(j) is used
					//the idea is to give larger dissonance for louder sounds 
				}, // and to smoothly go to zero as one of the partials disappears 
					//else
				{
					dnew=ampl[j]*((c1*exp1)+(c2*exp2)); // use ampl(j) if it is smaller
				});
				d = d+dnew;  // keep adding the dissonances of each loop, where d iterates the dissonance of 1 partial(frequ[i]) with 
				// each(all) of the timbre's other partials as they'd be at the current interval. This is done for 
				// for each partial(freq[i]) and every interval.
			});
			
		});		
						
		^DissonanceInterval(d, interval);
	}

	pr_compute_partial_just { |num, dem, numpartials|
	
		// This is sort of inspired by a mix of Sethare's ideas and Clarence Barlow's ideas 
		// about digestibility (see bleow)
		
		// Fixed:
		// I'm leaving the "just curve" as undocumented because once the curve is generated, there
		// is not  clear heuristic about which intervals are worth including and which aren't.
		// But if you look at:
		// DissonanceCurve.fm(400, 200, 100).just_curve.collect({|i| i.dissonance}).plot
		// I suspect those deep spikes are the good scale degrees.

		var interval,allpartialsatinterval, diss, new_diss, r_n, r_d;
		
		numpartials.isNil.if({ numpartials = frequ.size; });
		interval = num / dem;
		// fill allpartialsatinterval array with each element of freq array multiplied by interval
				
		allpartialsatinterval = frequ * interval;
		
		diss = 0;
		
		// Calculate the dissonance between frequ[] and interval*frequ[]
		// This calculation is based on an adaptation of Calrence Barlow's Digestibility formula
		// so that smaller ratios are preferred.  So the numerator and denominator are summed. 
		// Smaller ratios have smaller sums.  This is then scaled by the amplitude of the
		// quieter bin.
		
		frequ.do({ |freq, i|
			allpartialsatinterval.do({ |partial, j|
			
				#r_n, r_d = (partial / freq).asFraction(fasterBetter: false);
				
				diss = diss + ((r_n + r_d) * ampl[i].min(ampl[j]));
				
			})
		});
		
		^DissonanceInterval.newRatio(diss, num, dem);
	}
	
	
	pr_find_minima { |dcurve|
	
		var sc, prev, prever, prev_sum, prever_sum, item_sum;
		
		sc = [];

		dcurve.do({ |item, index|
			prever.isNil.if({ prever = item;}, {
				prev.isNil.if({ 
					
					prev = item;
					// check for first one
					(prever.dissonance < prev.dissonance).if({ sc = sc.add(dcurve[0])});
				}, {
		
					// we've got the two prevs
			
					((item.dissonance > prev.dissonance) && 
						(prev.dissonance < prever.dissonance)).if ({ // minima
							//sc = sc.add(prev);
							// find the one with the better ratio
							prev_sum = prev.numerator + prev.denominator;
							//postf("% / %\t", prev.numerator, prev.denominator);
							prever_sum = prever.numerator + prever.denominator;
							//postf("% / %\t", prever.numerator, prever.denominator);
							item_sum = item.numerator + item.denominator;
							//postf("% / %\n", item.numerator, item.denominator);
							
							((prever_sum < prev_sum) && (prever_sum < item_sum)).if({
								sc = sc.add(prever);
							} , { ((item_sum < prever_sum) && (item_sum < prev_sum)).if({
								sc = sc.add(item);
							} , { //((prev_sum < prever_sum) && (prev_sum < item_sum)).if({
								// the middle one is the default case
								sc = sc.add(prev);
							})})//});
							
						});
			
					prever = prev;
					prev = item;
				})});
		});
			
		// check for last one
		(prev.dissonance < prever.dissonance).if({ sc = sc.add(dcurve.last); });

		^sc;
	}
	
	
	pr_rms{ |dcurve, window = inf|
	
		// I ended up not using this
	
		var mins, rms, num;
		
		//mins = this.pr_find_minima(dcurve);
		
		mins = dcurve.collect({|d| d.dissonance});
		//mins = mins.differentiate;
		
		rms = mins.squared;
		rms = rms.sum;
		rms = rms / dcurve.size;
		rms = rms.sqrt;
		
		^rms
	}


	pr_find_just_scale{ |window = 100|
	
		var arr, rms, scale, last_rms, mins, min, lastwin, half, size;
		
		lastwin = [just_curve[0]];
		just_scale = [just_curve[0]];
		half = (window / 2).ceil;
				
		// find the minima
		//scale = this.pr_find_minima(just_curve);
		
		just_curve.do({|d, index|	
			
			lastwin = just_curve[(index - half).asInt .. (index+ half).asInt];
			
			min = lastwin.minItem({|i| i.dissonance});
			
			((d.dissonance <= min.dissonance)).if({
			
				// ok, this one is the least dissonant in the window
			
				(/*(scale.includes(d)) &&*/ just_scale.includes(d).not).if ({
			
					// make sure it's also a minima
					// this is redundant, because if it wasn't, it wouldn't be the
					// least dissonant in the window
					
					// make sure it's not already in the scale
			
					just_scale = just_scale ++ d;
				})
			});

		});
		
		
	}
		

}
		