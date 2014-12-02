Dissonance {
	/** Dissonance curve analisis 
	 * Juan S. Lach Lau, (2006-2008)
	 * 
	 * 	Use: d = Dissonance.make(f, a, start, end, inc, method, max); 
	 * 	where f is an array of frequencies and a is one of amplitudes
	 * 	arguments: start & end are the intervals to sweep thorugh in steps of inc
	 *	the resulting object will contain the analysis in the following instance variables:
	 *
	 * 		dcurve: the dissonance curve itself, use the plot method to visualize
	 *		scale: the scale resulting from the local consonant points (the minima in the plot)
	 *		ratios: the scale expressed in ratios in the form [ [p1,q1], ... , [pn,qn] ]
	 *		roughness: the roughness of each scale degree
	 *		harmonicity: Barlow's harmonicity for each ratio
	 *		fund: the fundamental, i.e., the frequency of the first partial, or the one with
	 *			highest amplitude (depending on the max argument being true or false). 
	 *		partials: the originial partials [f,a]
	 *		intervals: the intervals for calcualting the local minima (private).
	 *
	 *		method:  \sethares takes args in freqs-amps and is coarser (and 29% slower)
	 *				\parncutt takes args in freqs (which converts to barks) and sones
	 *				its more precise and faster but you have to calculate the sones first
	 *				(to do that, use the LoudnessModel class and the phonToSone method)
	 *
	 *	For calculating dissonance measures between two spectra use Dissonance.make2 
	 *
	 **/
	
	classvar dStar = 0.24, s1 = 0.0207, s2 = 18.96, c1 = 5.0, c2 = -5.0, a1 = -3.51, a2 = -5.75;

	var 	<>intervals, 
		<>partials,
		<>roughness, <>gradIdx, <>gradIdx2, 
		<>dcurve,
		<>fund, 
		<>scale, <>scale2,
		<>origScale,
		<>ratios, 
		<>harmonicity,
		<>cents, 
		<>ratioM, <>harmonicityM, <>ranks,
		<>weights, <>invweights, <>equweights, <>currentRanks, 
		<>markov, <>markovStream, 
		<>pitchSet,<>metric, 
		<>info;

	*new{ ^super.new.init }
	
	init { 
		intervals 	= Array.newClear; 
		roughness 	= Array.newClear; 
		dcurve 		= Array.newClear;
		partials 	= Array.newClear; 
		info 	 		= ();
	}
	
	*make { |f, a, start = 0.99, end = 2.01, inc = 0.01, method = \parncutt, max = false|
		var diss;
		diss = this.new;
		if (max) 	{diss.fund = f[a.indexOf(a.maxItem)]}
				{diss.fund = f[0]};
		diss.partials = [f,a];
		method.switch(
			\parncutt, {diss.intervalP(f,a,start,end,inc) },
			\sethares, {diss.interval(f,a,start,end,inc)  },
			{^"Invalid method" }
		); 
		diss.scale = diss.minima(diss.dcurve); 
//		diss.scale2 = diss.minima2(diss.dcurve);
		diss.ratios = diss.scale.asRatio(inc.reciprocal, false); // a first crude approximation
		diss.cents = diss.scale.cents; 
		^diss;	
	}
	
	// a Dissonance object from 2 different timbres (f,a) & (g,b)
	*make2 { |f, g, a, b, start = 0.99, end = 2.01, inc = 0.01, method = \parncutt, max = false|
		var diss;
		diss = this.new;
		if (max) {diss.fund = f[a.indexOf(a.maxItem)]}
				{diss.fund = f[0]};
		diss.partials = [f,g,a,b];
		method.switch(
			\parncutt, {diss.intervalP2(f,g,a,b,start,end,inc)},
			\sethares, {diss.interval_2(f,g,a,b,start,end,inc)},
			{^"Invalid method"}
		);
		diss.scale = diss.minima(diss.dcurve).round(inc);
		diss.ratios = diss.scale.asRatio(inc.reciprocal, false); // a first crude approximation
		diss.cents = diss.scale.cents;
		^diss;
	}		

	*default {|start = 0.49, end = 4.01, inc = 0.01, metric = \harmonicity| 
		^Dissonance.make(
					[100,200,300,400,500],[32, 16, 8, 4, 2], start, end, inc)
			.harmonicAnalysis(16, metric)
			.makeMatrix;
	}	

	harmonicAnalysis {|tolerance = 16, metric, type = \size, max, unisonvector, post = false|
		if (metric.class != HarmonicMetric) {metric = HarmonicMetric(metric)};
		this.metric = metric;
		if (post) {postf("Old ratios: %\n", this.ratios.ratioPost)};
		this.ratios = this.scale.rationalize(tolerance, metric, type, max);
		this.harmonicity = metric.value(this.ratios);	
		this.pitchSet = PitchSet.with(this.ratios, unisonvector); 
		if (post) {	
			postf("New ratios: %\n\nPitchSet: %\n", this.ratios.ratioPost, this.pitchSet);
		};
		^this;
	}

	// same as make but the input is directly an array in the format of a Signal.stft
	*makeFromSpectrum { |f, start = 1.0, end = 2.3, inc = 0.01, method = \parncutt, max = false,
						numPartials = 10, win = 256, sr = 44100|
		var partials = f.asFreqMag(win, sr/2).findNlargest(numPartials, true);
		^Dissonance.make(partials.first, partials.last, start, end, inc, method, max)
	}

	*makeFrom2Spectrums { |f, g, start = 0.99, end = 2.01, inc = 0.01, method = \parncutt, max = false,
						numPartials = 10, win = 256, sr = 44100|
		var partials1 = f.asFreqMag(win, sr/2).findNlargest(numPartials, true),
		    partials2 = g.asFreqMag(win, sr/2).findNlargest(numPartials, true);
		^Dissonance.make2(
			partials1.first, partials2.first, partials1.last, partials2.last, start, end, inc, method, max)
	}	
	
	*load {|path| var dis = Object.readArchive(path);
		if (dis.info.isNil) {dis.info = ()};
		if (dis.pitchSet.isNil) {
			dis = dis.harmonicAnalysis; 
			dis.pitchSet.makeProbMatrix;
		} 
		^dis
	}
		
	save {|path| this.writeArchive(path)}

// for use in saving multiple dissonance objects as arrays of dicts: 
	writeZArchive {| akv | var d = ();
		akv = akv.asZArchive;
		d = this.instanceToDict;
		akv.writeItem(d);
		akv.writeClose; 
	}
	
	*readZArchive {| akv | var d = Dissonance.make([100], [16]), za, dict;
		za = ZArchive.read(akv);
		dict = za.readItem;
		za.close;
		d.dictToInstance(dict);
		^d.update
	}
	
		
	dictToInstance {|d| 	
		intervals 		= d.intervals;
		partials 			= d.partials;
		roughness 		= d.roughness;
		dcurve 			= d.dcurve;
		fund 			= d.fund;
		scale 			= d.scale;
		origScale			= d.origScale;
		ratios			= d.ratios;
		harmonicity		= d.harmonicity;
		ratioM			= d.ratioM;
		harmonicityM		= d.harmonicityM;
		ranks			= d.ranks;
		weights			= d.weights;
		invweights		= d.invweights;
		equweights		= d.equweights;
		info				= d.info;
		^this
	}			
	
	instanceToDict { var d = ();
		d.intervals 		= intervals;
		d.partials 		= partials;
		d.roughness 		= roughness;
		d.dcurve 			= dcurve;
		d.fund 			= fund;
		d.scale 			= scale;
		d.origScale 		= origScale;
		d.ratios 			= ratios;
		d.harmonicity 	= harmonicity;
		d.ratioM 			= ratioM;
		d.harmonicityM 	= harmonicityM;
		d.ranks 			= ranks;
		d.weights 		= weights;
		d.invweights 		= invweights;
		d.equweights 		= equweights;	
		d.info			= info;	
		^d
	}
		
	play { |fund, dur = 0.33, amp = 0.1| fund ?? {fund = this.fund}; 
			Pbind(\freq, Pseq( this.scale * fund, 1), \dur, dur, \amp, amp).play(quant:0)
	}
	
	plot { var view;
		view = Plotter.new("dissonance curve", Rect(780, 456, 500, 300));
		view.value = this.dcurve;
		view.specs = [this.dcurve.minItem, this.dcurve.maxItem, \exp, 0.0, 0, " "].asSpec;
		view.domainSpecs = 
			[this.intervals.first, this.intervals.last, \lin, 0.0, 0, " "].asSpec;
		^view;
	}
	
	printOn { arg stream;
		stream << this.class.name << "( " <<  ratios.ratioPost  <<" )";
	}


		
	// filter scales by eliminating denominators that are too big
	reduce {|maxDenom = 18| 
			this.ratios = this.ratios.reject{|x| x[1] > maxDenom}; 
			^this.update;
	}
	
// filter scales by eliminating ratios with a maximum prime factor
// (should factor a single prime from the ratos, not all greater than)
	factor {|maxPrime = 7| 
			this.ratios = this.ratios.reject{|x| 
					(x[0].factors.maxItem > maxPrime) or: (x[1].factors.maxItem > maxPrime)}; 
			^this.update;
	}
	
// filter out all degrees that contain a certain prime factor:
	filter {|aPrime = 5| 
			this.ratios = this.ratios.reject{|x| 
					x[0].factors.includes(aPrime) or: x[1].factors.includes(aPrime)};
			^this.update;
	}
	
	update {|new| 
			origScale  = origScale ? this.scale; // backup original scale before updating
			this.scale = this.ratios.collect{|x| x[0]/x[1]};
			this.harmonicity = this.ratios.harmonicity;
			this.cents = this.scale.cents;
			^this; 
	}
	
	makeMatrix{ |pwr = 15, add = 1, metric |
		metric = metric ? \harmonicity;
		this.pitchSet.makeProbMatrix(pwr, add, metric); 
		// the following is for backwards compatibility: 
		this.ratioM = this.pitchSet.ratioM;
		this.harmonicityM = this.pitchSet.metricM;
		this.ranks = this.pitchSet.ranks;
		this.weights = this.pitchSet.weights;
		this.invweights = this.pitchSet.invweights;
		this.equweights = this.pitchSet.equweights;
		"READY...".postln;
		^this
	}
	
	calcPolarity {|polarity, filteredScale|
		var interweights;
		interweights = this.pitchSet.calcPolarity(polarity, filteredScale);
		// the following is for backwards compatibility: 
		this.currentRanks = this.pitchSet.currentRanks;
		this.markov = this.pitchSet.markov;
		this.markovStream = this.pitchSet.markovStream;
		^interweights
	}
	
	postMratio { ^this.ratioM.collect{|r| format("%\n", r.ratioPost("\t\t")) }.join }
	
	postMfloat {|m, round = 0.0001 | m = m ? this.harmonicityM; // default float matrix post is harmonicity
			^m.collect{|h| 
				format("%\n", 
					h.collect{|h0| var str = h0.round(round).asString;
						str ++ (12 - str.size).collect{ Char.space }.join 
					}.join("\t")
				) 
			}.join 
	}
	
	postMint { |m | m = m ? this.ranks; 
			^m.collect{|h| 
				format("%\n", 
					h.collect{|h0| var str = h0.asString;
						str ++ (12 - str.size).collect{ Char.space }.join 
					}.join("\t")
				) 
			}.join 
	}
	
	makeFileForMDS {|path| var recip, file;
			file = File.new(path ++ ".txt", "w");
			file.write( (this.pitchSet.metricM.size + 1).asString ++ " labelled\n");
			this.pitchSet.metricM.do{|x, i| var char;
				file.write(this.ratios[i].ratioPost ++ "\t");
				x.do{|y,j| 
					if (j == (x.size - 1)) {char = "\n"} {char = "\t"};
					file.write(y.reciprocal.round(0.001).asString ++ char);
				};
			};
			file.close;
			^"done"
	}
/*	
		
			The following methods provide the actual calculations for the roughness analysis 
			They are called by *make, and *make2 and don't need to be used separately					 

*/

//	dissonance measure between arrays of partials f & g (in Hz) with amp arrays a & b
// 	algorithm taken from Sethares[97] out of his BASIC & matlab code
	dissmeasure {|f, g, a, b|
		var d = 0, fdif, s, arg1, arg2, exp1, exp2, dnew;
		f.size.do{|i|
			g.size.do{|j|
				s = dStar / (s1 * min(g[j], f[i]) + s2);
				fdif = absdif(g[j],f[i]);
				arg1 = a1 * s * fdif;
				arg2 = a2 * s * fdif;
				if (arg1 < 88.neg) {exp1 = 0} {exp1 = exp(arg1)};
				if (arg2 < 88.neg) {exp2 = 0} {exp2 = exp(arg2)};
				dnew = min(a[i], b[j]) * ( (c1 * exp1) + (c2 * exp2) );
				d = d + dnew;
			};
		};
		^d
	} 
	
//   this is another dissonance measure based on Parncutt and Barlow: D = sqrt(s1 * s2) * P(bk1 - bk2)
//   where s1 & s2 are arrays in sones, b1 & b2 arrays in barks and P is the Parncutt dissonance measure
	dissmeasure2 {|bk1, bk2, s1, s2| 
		var diss = 0, freqDiff, dnew;
		bk1.size.do{|i|
			bk2.size.do{|j|
				freqDiff = absdif(bk2[j], bk1[i]);
				dnew =  sqrt(s1[i] * s2[j]) * (4 * freqDiff * (exp(1-(4 * freqDiff))));
				diss = diss + dnew; 
			};
		};
		^diss
	}

// 	Sweep array for the parncutt dissmeasure method: 
	intervalP {|f, s, startInt = 0.99, endInt = 2.01, inc = 0.01|
		intervals = []; 
		forBy(startInt, endInt, inc, {|alpha|
			intervals = intervals.add(alpha);
			dcurve = dcurve.add(
					this.dissmeasure2(f.asBark, (f * alpha).asBark, s, s)
			);
		});
		^dcurve
	}
	
// 	Sweep between two different arrays, parncutt dissmeasure method: 
	intervalP2 {|f, g, s, t, startInt = 0.99, endInt = 2.01, inc = 0.01|
		intervals = []; 
		forBy(startInt, endInt, inc, {|alpha|
			intervals = intervals.add(alpha);
			dcurve = dcurve.add(
					this.dissmeasure2(f.asBark, (g * alpha).asBark, s, t)
			);
		});
		^dcurve
	}
	
// 	Sweep an array of partials f and amps a with itself from startInt to endInt by inc
	interval {|f, a, startInt = 0.99, endInt = 2.01, inc = 0.01| 
		intervals = [];
		forBy(startInt, endInt, inc, {|alpha|
			intervals = intervals.add(alpha);
			dcurve = dcurve.add(this.dissmeasure(f, f * alpha, a, a))
		});
		^dcurve
	}

// 	Sweep between two different sounds: (f,a) and (g,b), same as previous
	interval_2 {|f, g, a, b, startInt = 1.0, endInt = 2.3, inc = 0.01|
		intervals = [];
		forBy(startInt, endInt, inc, {|alpha|
			intervals = intervals.add(alpha);
			dcurve = dcurve.add(this.dissmeasure(f, g * alpha, a, b));
		});
		^dcurve
	}


// Get the minima of a dissonance curve:
	minima {|dissArray|
		var gradient = dissArray.differentiate,
		prev = gradient.first, res = [], min = [];
		gradient.do{|a| 
			if (a.sign != prev.sign)  // changes in sign indicating changes in curvature
			{ 
				res = res.add(gradient.indexOf(a) - 1); // local minima indexes
				prev = a;
			}
		};
		res.do{|r| // r are the indexes of all inflection points, filter out maxima:
			if (dissArray[r] < dissArray.wrapAt(r - 1)) { // only need to check to the left
				min = min.add(this.intervals[r]); // we want the intervals at which these minima occur
				roughness = roughness.add(dissArray[r]); // and their roughness
			}
		};
		gradIdx  = res;
		^min
	}
	

// The following methods are experimental: 
// sweep roughness measure by iterating through a list of rationals, 
// parncutt method (amps should be in sones) 
	rationalRoughness {|f, s, ratioList | var ratios;
		f = f ? partials[0];
		s = s ? partials[1]; 
		ratios = ratioList ? IntervalTable.table.ratios;
		ratios = ratios[ratios.cents.order];
		^ratios.collect{|ratio| this.dissmeasure2(f.asBark, (f * ratio[0]/ratio[1]).asBark, s, s) };
	}		 

	rationaRoughnessHarm {|f, s, ratioList | var ratios;
		f = f ? partials[0];
		s = s ? partials[1]; 
		ratios = ratioList ? IntervalTable.table.ratios;
		ratios = ratios[ratios.harmonicity.abs.order].reverse;
		[f,s,ratios].postln; 
		^ratios.collect{|ratio| this.dissmeasure2(f.asBark, (f * ratio[0]/ratio[1]).asBark, s, s) };
	}		 
	
	minimaForRationals { }
	
	
// get the second derivative for the dissonance curve. Experimental.
	minima2 {|dissArray|
		var gradient = dissArray.differentiate.differentiate, // second derivative for changes in curvature
		prev = gradient.first, res = [], min = []; 
		gradient.do{|a| 
			if (a.sign != prev.sign) {
				res = res.add(gradient.indexOf(a) - 1);
				prev = a;
			}
		}; 
		res.do{|r| 
			if (dissArray[r] < dissArray.wrapAt(r - 1)) {
				min = min.add(intervals[r]);
			}
		};	
		gradIdx2 = res; 		
		^min
	}
}

/*

TO DO:

/*
a maxima method for obtaining the roughest intervals
*/

Dissonance: (2006-2008) jsl

*/