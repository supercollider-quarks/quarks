PitchSet { //a set of harmonic vectors that are partitioned into islands (harmonic & timbral)
/*   Implementation inspired on the article: 
	Fokker, A, “Unison vectors and periodicity blocks in the three-dimensional (3-5-7-)harmonic
	lattice of notes”, Koninklijke Nederlandse Akademie van Wetenschappen - Amsterdam, Proceedings, 	Series B 72, No. 3, 1969
*/
	classvar <unisons;
	// unisonvectorS!?  
	var <>unisonvector, <>set, <>harmonicSet, <>timbralSet, <>setH, <>setT, <>ratios;
	// should belong to HarmonicField:
	var <>ratioM, <>metricM, <>ranks, <>weights, <>invweights, <>equweights;
	var <>currentRanks, <>markov, <>markovStream, <>metric;
	
	*initClass {
		unisons = (
			dim2: (
			 	et12: (
			 			num: 2, 
				 		1: #[ [0, -3], [4, -1] ], 
				 		2: #[ [4, -1], [8, 1] ]
			 		),
			 	et19: (
			 			num: 2, 
				 		1: #[ [4, -1], [-1, 5] ], 
				 		2: #[ [4, -1], [-5, 6] ]
			 		),
			 	et22: (
			 			num: 2, 
				 		1: #[ [4, 2], [-1, 5] ], 
				 		2: #[ [4, 2], [3, 7] ]
			 		),
			 	et31: (
			 			num: 2, 
				 		1: #[ [4, -1], [3, 7] ], 
				 		2: #[ [1, -8], [4, -1] ]
			 		),
			 	et41: (
			 			num: 2, 
				 		1: #[ [8, 1], [-1, 5] ], 
				 		2: #[ [8, 1], [7,  6] ]
			 		),
			 	et53: (
			 			num: 2, 
				 		1: #[ [8, 1], [-5, 6] ], 
				 		2: #[ [8, 1], [3,  7] ]
			 		)
				),
			dim3: ( 
				et12: (
			 			num: 1, 
				 		1: #[ [4,2,0], [4,-3,2], [2,2,-1] ]
			 		),
			 	et19: (
			 			num: 3, 
				 		1: #[ [4,-1,0], [2,2,-1], [-3,3,1] ],
				 		2: #[ [4,-1,0], [2,2,-1], [-7,4,1] ],
				 		3: #[ [-7,-1,3], [2,2,-1], [5,-6,0]]
			 		),
			 	et22: (
			 			num: 1, 
				 		1: #[ [-1,3,2], [-7,-1,3], [2,2,-1]]
			 		),
			 	et31: (
			 			num: 3, 
				 		1: #[ [4,-1,0], [2,2,-1], [1,0,3]],
				 		2: #[ [4,-1,0], [0,3,5], [1,0,3]],
				 		3: #[ [1,5,1], [-1,-2,4], [1,-3,-2]]
			 		),
			 	et41: (
			 			num: 1, 
				 		1: #[ [2,2,-1], [-7,-1,3], [4,-3,2]]
			 		),
			 	et53: (
			 			num: 3, 
				 		1: #[ [4,-3,2], [2,2,-1], [-1,3,2]],
				 		2: #[ [-7,4,1], [1,-3,-2], [3,0,4]],
				 		3: #[ [-7,4,1], [1,5,1], [1,-3,-2]]
			 		)
			 	),
			 	
			 /* 
			 	two periodicity blocks for representing Partch's 43 tone 11 limit scale
				 (approximating it to 41 degrees instead of 43)...
			  */
			 dim4: ( 
			 	et41: (
			 		num: 2,
			 		1: #[ [4,0,-1,1], [2,-1,2,-1], [-5,1,2,0], [-2,2,0,1] ],
					2: #[ [-2,2,0,-1], [5,0,0,-2], [-5,1,2,0], [2,2,-1,0] ]
			 	)
			 )
		)	
	}
	
	*new { ^super.new.init }
	
	init { 
		this.set = Set.new;
		this.harmonicSet = Set.new;
		this.timbralSet = Set.new;	
		this.ratios = Array.newClear; 	
	}

	*with {|array, unisonvector| var pitchSet = this.new; 
		pitchSet.unisonvector = unisonvector ? unisons.dim3.et12[1]; // the default matrix
		pitchSet.ratios = array;
		pitchSet.ratios.asHvector.do{|x|			
			pitchSet.add(x);
		};
		// this.makeProbMatrix; 
		^pitchSet.partition(pitchSet.unisonvector)
	}
	
	add {|item|  
		if (item.isKindOf(HarmonicVector).not) {
			this.set.add(HarmonicVector.from(item)); // item should be a [p,q] ratio 
		}{
			this.set.add(item)
		};
		this.partition(this.unisonvector);
		this.asRatios;
		^this
	}
	
	play {|fund = 440| Pbind(\freq, Pseq(this.ratios.ratioToFreq * fund) ).play }
	
	playChord {|fund = 440| 
		Pbind(
			\freq, Pseq([this.ratios.ratioToFreq * fund]), 
			\db, this.ratios.size.neg * 2
		).play 
	}
	
	/* add remove & other methods related to set & collection */
	
	partition {|unisonvector|
		this.harmonicSet = Set.new;
		this.timbralSet = Set.new;			
		this.unisonvector = unisonvector ? unisons.dim3.et12[1];
		this.set.do{|pitch|
			if (pitch.isInIsland(this.unisonvector)) {
				this.harmonicSet = this.harmonicSet.add(pitch)
			}{
				this.timbralSet = this.timbralSet.add(pitch)
			}
		};
		//converted into arrays for easy handling and for complete ratios 
		// (not just the octave reduced ones):
		this.setT = this.timbralSet.collect{|x| x.ratio}.asArray; 
		^this.setH = this.harmonicSet.collect{|x| x.ratio}.asArray;		
	}
		
	makeProbMatrix { |pwr = 15, add = 1, metric | // rank-to-weight formula: (ranks + add) ** pwr
		var order, invrank, indx, metricArray;
		metric = metric ? \harmonicity; 
		metric = HarmonicMetric(metric);
		this.metric = metric; 
		metricArray = metric.value(this.ratios); 
		this.ratios.do{|n,i| 
			this.ratios.do{|m, j|
				this.ratioM = this.ratioM.add(m.ratioDiv(n))
			};
		};
		this.ratioM = this.ratioM.reshape(this.ratios.size, this.ratios.size, 2);
		this.metricM = this.ratioM.collect{|x| metric.value(x)};
		order = this.metricM.collect{|x| x.order };
		this.ranks = order.collect{|ord|
			metricArray.size.collect{|i| 
				metricArray[ord].indexOf(metricArray[i])
			}
		}; 
		indx = this.ranks.collect{|x| x.indexOf(x.minItem) }; // a way of deriving inverse ranks,
		invrank = this.ranks.collect{|x| x.neg % (x.size - 1) }; // almost works...
		invrank.do{|x, i| x[indx[i]] = x.size - 1 }; 	// but needs this extra dirty trick
		this.weights = this.ranks.collect{|x| ((x + add) ** pwr).normalizeSum}; 
		this.invweights = invrank.collect{|x| ((x + add) ** pwr).normalizeSum}; 
		this.equweights = ({1}!this.ratios.size).normalizeSum;
		^"READY...";
	}
	
	calcPolarity { |polarity, filteredScale| 
		var interweights, data, equ, rnk, inv, wgt;
		
		if (filteredScale.isNil) {filteredScale = this.ratios};
		if (this.weights.isNil) {"use makeMatrix first!".inform; ^this};
		if (this.ratios.size != filteredScale.size) { \danger.postln;
			rnk = []!this.ranks.size; 
			inv = []!this.invweights.size; 
			wgt = []!this.weights.size; 
			this.weights[0].size.do{|x, i| 
				wgt[i] = this.weights[i].structureAs(filteredScale, this.ratios).normalizeSum;
				inv[i] = this.invweights[i].structureAs(filteredScale, this.ratios).normalizeSum;
				rnk[i] = this.ranks[i].structureAs(filteredScale, this.ratios); 
			};
		}{ 
			wgt = this.weights;
			inv = this.invweights;
			rnk = this.ranks;
		};
		equ = ({1}!filteredScale.size).normalizeSum;
		
		this.currentRanks = rnk; // this is uselful for accent calculations
	// this is the nitty gritty of the method: 
		interweights = inv.collect{|x,i| x.interpolate3(equ, wgt[i], polarity) };
		data = filteredScale.collect{|x,i| [x, filteredScale, interweights[i] ] };
	// make markov stream: 
		if (this.markov.isNil) { this.markov = MarkovSet.new(data) };
		this.markov.putAll(data); 
		this.markov.makeSeeds;
		this.markovStream = this.markov.asStream;
	// return the calculated weights according to harmonic field strength: 
		^interweights
	}
	
	asString { 
		^"Harmonic " ++ this.harmonicSet.collect{|x| x.ratio.ratioPost } ++
		"\nTimbral "  ++ this.timbralSet.collect{|x| x.ratio.ratioPost } ++ 
		"\n"
	}
	
	postcs {
		^"Harmonic:  " ++ this.setH.ratioPost  ++
		"\nTimbral: " ++ this.setT.ratioPost ++ "\n"
	}
	
	gradusSuavitatis {var h = this.setH.ratioSort, 
		t = this.setT.ratioSort, 
		b = (h ++ t).ratioSort;		
		postf("Complete: %\nHarmonic: %\nTimbral: %\n", 
			b.gradusSuavitatisN,
			h.gradusSuavitatisN,
			t.gradusSuavitatisN);
	}
	
	asHarmonicSeries { var h = this.setH.ratioSort, 
		t = this.setT.ratioSort, 
		b = (h ++ t).ratioSort;
		postf("complete: %\n\nharmonic: %\n\ntimbral: %\n\n", 
			b.ratioPost, h.ratioPost, t.ratioPost);
		postf("Complete: %\nHarmonic: %\nTimbral: %\n", 
			b.ratioToHarmonics,
			h.ratioToHarmonics,
			t.ratioToHarmonics)
		^b.ratioToHarmonics;
	}
	
	reducedRatios { ^this.set.collect{|x| x.reducedRatio}.asArray.ratioSort }
	
	sort {|metric|
		if (metric.class != HarmonicMetric) {metric = HarmonicMetric(metric)};
		this.metric = metric;
		^metric.order(this.ratios)	
	}	
		
	
	asScale {|ffreq = 440| var b = (this.setH ++ this.setT).ratioSort, 
		c = this.reducedRatios, d, e;
		postf("Complete (absolute): %\tSize: %\n", b.ratioPost, b.size);
		postf("Complete (adjacency): %\n", b.ratioDifferentiate.ratioPost);
		postf("Complete (cents): %\n", b.cents.round(1));
		postf("Reduced: %\tSize: %\n", c.ratioPost, c.size);
		postf("Reduced (adjacency): %\n\n", c.ratioDifferentiate.ratioPost);
		postf("Notes: %\n", (b.ratioToFreq * ffreq).asNote);
//		postf("Sorted by %: %\n", this.metric, d = this.sort.ratioPost);
//		e = d.collect(_.order);
//		postf("Ranks: %\n", e);
		
	}
	
	asRatios {var b = this.setH ++ this.setT;
		this.ratios = b[b.ratioToFreq.order];	
	}
	
	
	// it is still producing octave reduced intervals, check how to do it without octave reduction
	asETdegrees {|tet = 53, highestPrime = 7| 
		var kh, kt, hset, tset, hOrd, tOrd, hRatios, tRatios, primes;
		primes = Array.primes(highestPrime)[1..]; 
		
		if (this.harmonicSet.isEmpty.not) {
			hset = this.harmonicSet.collect{|x| [x.reduced, x.ratio]}.asArray.flop;
			hOrd = hset[1].ratioToFreq.order;
			hRatios = hset[1][hOrd];
			hset = hset[0][hOrd].collect{|x| x.as(Array)};
			hset = hset.collect{|x| x.collect{|y,i| y * primes[i].log2}.sum};
			kh = hset.collect{|a| (tet*a.frac).round(1)};
		}{
			kh = [];
		};
		if (this.timbralSet.isEmpty.not) {
			tset = this.timbralSet.collect{|x| [x.reduced, x.ratio]}.asArray.flop;
			tOrd = tset[1].ratioToFreq.order;
			tRatios = tset[1][tOrd];
			tset = tset[0][tOrd].collect{|x| x.as(Array)};
			tset = tset.collect{|x| x.collect{|y,i| y * primes[i].log2}.sum};
			kt = tset.collect{|a| (tet*a.frac).round(1)};
		}{
			kt = []
		};		
		postf(
		"%-TET\nHarmonic Ratios: %\nHarmonic Degrees: %\nTimbral Ratios: %\nTimbral Degrees: %\n", 
		tet, hRatios.ratioPost, kh, tRatios.ratioPost, kt);
		^[kh,kt]
	}
	
/* 

			Visualization tools (requires GNUPlot & GNUPlot quark)

*/

// what = \metric, \ranks, \weights, \currRanks; 
// if hollow = true then the diagonal i=j will be the minval (only in the case of metricM)
	plotHarmonicField {|what = \metric, hollow = true, minval = 0.01, title | 
		var data, field, gnuplot;
		if (Class.allClasses.includes(GNUPlot).not) {				"GNUPlot quark required for this method to work".inform; ^""};
		gnuplot = GNUPlot.new; 
		title = title ? "Probability according to %".format(this.metric.asString);

	// falta ver qué onda con interweights para que sea una variable de instancia
		data = what.switch(
			\metric, 		{ this.metricM 	},
			\ranks, 		{ this.ranks 		},
			\weights, 	{ this.weights	},
			\currRanks,	{ this.currentRanks}
		);
		if (data.isNil) {"options are: metric, ranks, weights and currRanks...".error; ^""};
		field = 	(0..(this.ratios.size-1)).collect{|w, i|
					(0..(this.ratios.size-1)).collect{|z, j|
						if ((i == j) and: (hollow) and: (what == \metric)) { 
							[w, z, minval]
						}{ 
							[w, z, data[i][j] ] 
						}
				}
		};
		gnuplot.surf3(
			field, "Ratios: " ++ this.ratios.ratioPost, 
			false, 
			true, 
			title, extracmds: "set view 30, 145, 1, 1"
		);
		^gnuplot;		
	}
	
// possible types: \235 (octaves, fifths, thirds) \357 (5s, 3rds, 7ths) or \5711
// not working yet, only plots \235 space still
	plotHarmonicSpace {|type = \235|
		var vectors, ratioS, data, gnuplot;
		// if('GNUPlot'.asClass.isNil, { "this code requires the GNUPlot Quark".error } )
		if (Class.allClasses.includes(GNUPlot).not) {
			"GNUPlot quark required for this method to work".error; ^""};
		gnuplot = GNUPlot.new; 
		vectors = this.set.asArray.collect(_.vector);
		ratioS = this.set.asArray.collect(_.ratio).collect{|x| 
			(x[0].asString ++ "/" ++ x[1].asString).asCompileString};
		data = vectors.collect(_.asArray).collect{|x| if (x.size < 3) {x ++ [0]} {x}};
		data = data.reshape(data.size, 1, 3);
//		data = data.collect{|x,i| [ ratioS[i], x[0][0], x[0][1], x[0][2]] };
		^gnuplot.scatter(data)
	}
	
	makeMDSfile {|path| var recip, file;
			file = File.new(path ++ ".txt", "w");
			file.write( (this.metricM.size + 1).asString ++ " labelled\n");
			this.metricM.do{|x, i| var char;
				file.write(this.ratios[i].ratioPost ++ "\t");
				x.do{|y,j| 
					if (j == (x.size - 1)) {char = "\n"} {char = "\t"};
					if (this.metric.type == \harmonicity) {
						file.write(y.reciprocal.round(0.001).asString ++ char);
					}{	file.write(y.round(0.001).asString ++ char); };
				};
			};
			file.close;
			^"done"
	}
	
	selectPrime {|prime = 7| 
			^this.ratios.select{|x| 
					(x[0].factors.includes(prime)) or: (x[1].factors.includes(prime))
			}; 
	}
	
	rejectPrime {|prime = 7| 
			^this.ratios.reject{|x| 
					(x[0].factors.includes(prime)) or: (x[1].factors.includes(prime))
			}; 
	}
	
	separateIntoPrimes	{ var pr, maxP, multi, primes, primesL, separate;
		pr = this.reducedRatios.ratioToHarmonics.collect{|x| x.factors};
		maxP = 2;
		pr.do{|x| var maxy = x.maxItem; 
//			[x, maxy].postln; 
			if (x.isEmpty) {
					\teCache.postln;
					^List[ [ [ 9, 8 ], [ 128, 81 ], [ 32, 9 ] ], [ [ 15, 8 ], [ 81, 40 ], [ 5, 2 ], [ 45, 16 ], [ 16, 5 ], [ 5, 1 ], [ 160, 27 ], [ 32, 5 ], [ 20, 3 ], [ 36, 5 ] ], [ [ 9, 7 ], [ 7, 5 ], [ 35, 16 ], [ 16, 7 ], [ 24, 7 ], [ 27, 7 ], [ 35, 8 ], [ 28, 3 ] ], [ [ 16, 11 ], [ 33, 4 ] ] ] 
				}{
					if (maxy > maxP) {maxP = maxy} 
				};
		};
		primes = Array.primes(maxP);
		multi = primes.collect{|x| this.selectPrime(x)};
		separate = List[];
		primesL = primes.asList;
		multi.do{|x, i| var filt = x;
			primesL.remove(primes[i]);	// remove primes higher than the list we're dealing with
			primesL.do{|p|
				filt = filt.reject{|ratio|
					(ratio.first.factors.includes(p)) or:
					(ratio.last.factors.includes(p))
				};
			};
			if (filt.isEmpty.not) {
				separate = separate.add(filt); 
			};
		};
		^separate
	}

}

+ SequenceableCollection {
	
	asPitchSet {|unisonvector|
		
		^PitchSet.with(this, unisonvector ? PitchSet.unisons.dim3.et12[1]);
	}
		
}	


/*
TO DO: derive unison vectors for 4 and 5 dimensions! We need to include 11 and 13 somehow.
*/