// some statistics, adc 2005, plus ds 2007/08.

+ Collection {
			// same as sum, but sum initialized as float 0.0 to avoid numerical
			// errors for sums of large integer arrays.
	sumF { | function |			
		var sum = 0.0;			
		if (function.isNil) { 		// optimized version if no function
			this.do { | elem | sum = sum + elem; }
		}{
			this.do {|elem, i| sum = sum + function.value(elem, i); }
		};
		^sum
	}
	
	meanF { | function | ^this.sumF(function) / this.size }
	
	geoMean { 
		// 	this.product ** (this.size.reciprocal);	// fails for big arrays, 
		^2 ** this.mean({ |el| el.log2 })			// log2 method is slower but safer.
	}
	
	harmMean { 
		^this.size / this.sumF({ |el| el.reciprocal })
	}
	
	variance { arg mean;	// supply mean if known
		mean = mean ?? { this.meanF };
		
		^(this.sumF { |el| (el - mean).abs.squared } / (this.size - 1).max(1))
	}
	variancePop { arg mean;	// supply mean if known
		mean = mean ?? { this.meanF };
		
		^(this.sumF { |el| (el - mean).abs.squared } / (this.size).max(1))
	}
	
	stdDev { arg mean; ^this.variance(mean).sqrt; }
	stdDevPop { arg mean; ^this.variancePop(mean).sqrt; }
	
	skew { arg mean;	// supply mean if known
		mean = mean ?? { this.meanF };		
	
		^this.sumF({ |el| (el - mean).cubed }) / (this.size * this.stdDev(mean).cubed)
	}
	skewPop { arg mean;	// supply mean if known
		mean = mean ?? { this.meanF };		
	
		^this.sumF({ |el| (el - mean).cubed }) / (this.size * this.stdDevPop(mean).cubed)
	}
	
		// kurtosis is tails size : kurtosis > 0 is leptokurtic, i.e. large tails; 
		// 0 is normal distribution, < 1 is platykurtic = small tails.
		// not quite sure how to test that this formula is correct. 
	kurtosis { arg mean;	// supply mean if known
		mean = mean ?? { this.meanF };		
		
		^(this.sumF({ |el| (el - mean).abs.squared.squared })
		/ ((this.size - 1)  * this.variance.squared)) - 3
	}
		
						// standard normal distribution form:
						// avg = 0, stdDev = 1.
	zTable { arg mean, stdDev;	// supply mean and stdDev if you can
						// compact formula is:
	//	^this - this.meanF / this.stdDev; 

		mean = mean ?? { this.meanF };
		stdDev = stdDev ?? { this.stdDev(mean) };	
		^this - mean / stdDev
	}
	
					// typically assumes this is sorted! 
	atPercent { |index=0.5, interpol=true| 
		if (interpol) 
			{ ^this.blendAt(index * (this.size - 1)) }
			{ ^this.at( index * (this.size - 1).round.asInteger) }
	}

	/* naive method
	percentile { |percent=#[0.25, 0.5, 0.75], interpol=true| 
		^this.copy.sort.atPercent(percent, interpol)
	}
	*/
	// Faster method
	percentile { |percent=#[0.25, 0.5, 0.75], interpol=true|
		var sorted, index, indexint;
		sorted = this.copy;
		// We don't actually sort the entire array since that may be expensive for large arrays.
		// Instead we use Hoare's partitioning method to sort it "just enough"
		percent.asArray.do{|pc|
			index = pc * (this.size - 1);
			indexint = index.floor.asInteger;
			if(index == indexint){
				sorted.hoareFind(indexint);
			}{
				sorted.hoareFind(indexint);
				sorted.hoareFind(indexint + 1);
			};
		};
		^sorted.atPercent(percent, interpol)
	}
			// median exists already, median2 interpolates.
	median2 {
		^this.percentile(0.5, true);
	}
	
	trimedian { |interpol=true|
		^this.percentile([0.25, 0.5, 0.5, 0.75], interpol).meanF;
	}
}


+ SequenceableCollection { 
			// Pearson correlation.
	corr { arg that; 
		var num, denom, thisSum, thatSum; 
		
		if (this.size != that.size, { 
			"No correlation between colls of unequal size.".error; 
			^nil 
		}); 
		 
		thisSum = this.sumF;
		thatSum = that.sumF;
		
		num = this.sumF({ |el, i| el * that[i] }) - (thisSum * thatSum / this.size); 
		
		denom = sqrt( 
			(this.sumF({ |el| el.squared }) - (thisSum.squared / this.size)) 
			* (that.sumF({ |el| el.squared }) - (thatSum.squared / that.size))
		);
		^num / denom
	}
	
	// Same as corr but Wikipedia says it has better numerical stability
	// http://en.wikipedia.org/wiki/Correlation
	/* NO WORKY
	corr2 { |that|
		var sum_sq_x = 0;
		var sum_sq_y = 0;
		var sum_coproduct = 0;
		var mean_x = this[0];
		var mean_y = that[0];
		var sweep, delta_x, delta_y, pop_sd_x, pop_sd_y, cov_x_y;
		var n = this.size;
		(1 .. n-1).do{|i|
			sweep = i / (i+1);
			delta_x = this[i] - mean_x;
			delta_y = that[i] - mean_y;
			sum_sq_x      = sum_sq_x      + delta_x * delta_x * sweep;
			sum_sq_y      = sum_sq_y      + delta_y * delta_y * sweep;
			sum_coproduct = sum_coproduct + delta_x * delta_y * sweep;
			mean_x = mean_x + (delta_x / (i+1));
			mean_y = mean_y + (delta_y / (i+1));
		};
		pop_sd_x = sqrt( sum_sq_x / n );
		pop_sd_y = sqrt( sum_sq_y / n );
		cov_x_y = sum_coproduct / n;
		^ cov_x_y / (pop_sd_x * pop_sd_y)
	}
	*/
	
	// Wilcoxon Signed-Rank test 
	// Non-parametric test for whether the PAIRED differences between two sets of values is of zero median.
	// If onetailed==true, test is unidirectional: we're testing for whether the SECOND array ("that") is higher.
	// See Mendenhall et al, "Mathematical Statistics with Applications", sec 15.4
	// Also http://www.fon.hum.uva.nl/Service/Statistics/Signed_Rank_Test.html
/*
a = [78,24,64,45,64,52,30,50,64,50,78,22,84,40,90,72];
b = [78,24,62,48,68,56,25,44,56,40,68,36,68,20,58,32];

a = [135, 102, 108, 141, 131, 144];
b = [129, 120, 112, 152, 135, 163];

# w, n = wilcoxonSR(b, a);
z = wilcoxonSRzScore([w, n]);
*/
	/*
wilcoxonSR([2,3,4,5,6,7,8], [2,3,4,5,6,7,8]);
wilcoxonSR([2,3,4,5,6,7,8], [2,3,4,5,6,7,8].reverse);
wilcoxonSR({1.0.rand}.dup(10  ), {1.0.rand}.dup(10));
wilcoxonSR({1.0.rand}.dup(100 ), {1.0.rand}.dup(100));
wilcoxonSR({1.0.rand}.dup(1000), {1.0.rand}.dup(1000));
wilcoxonSR({1.0.rand}.dup(1000), {1.0.rand}.dup(1000) + 1);
wilcoxonSR({1.0.rand}.dup(1000), {1.0.rand}.dup(1000) + 100);
wilcoxonSR({1.0.rand}.dup(1000), {1.0.rand}.dup(1000) - 1);   
wilcoxonSR({1.0.rand}.dup(1000), {1.0.rand}.dup(1000) - 100); 
	*/
	wilcoxonSR { |that, onetailed=false|
		var diffs, absdiffs, indicator, ranks, i, j, statistic;
		
		diffs = (that - this).reject{|d| d==0 }; // zero-difference entries are filtered out
		
		// Sort according to ABSOLUTE value
		diffs = diffs.sort{ |a, b| (a.abs) <= (b.abs) };
		
		// Now find rank - it's just like array-index except that for tie breaks you must choose the average
		ranks = (0 .. diffs.size-1);
		i=0;
		while{i<diffs.size}{
			j = i; // j is the starting position, i will be the finishing position
			while{diffs[i+1].notNil and:{diffs[i+1] == diffs[i]}}{
				i = i + 1;
			};
			if(j != i){ // We need to meanify
				ranks[j..i] = ranks[j..i].mean
			};
			i = i + 1;
		};
		
		//"diffs".postln;
		//diffs.postln;
		//"ranks".postln;
		//(ranks+1).postln;
		
		statistic = diffs.sum{|diffval, diffindex|
			// Indicator (the "if") makes it onetailed
			// Rank indexed starting from 1 not zero.
			if(diffval > 0, {ranks[diffindex]+1}, 0)
		};
		if(onetailed.not){
			statistic = min(statistic,
				diffs.sum{|diffval, diffindex|
					// Indicator (the "if") makes it onetailed
					// Rank indexed starting from 1 not zero.
					if(diffval < 0, {ranks[diffindex]+1}, 0)
				}
			)
		};
		
		// The size is returned since it may be less than the number of values you put in!
		^[statistic, diffs.size]
		
	}
	// For data more than, say, 15 points, we can calculate a z-score based on the normal distribution approximation.
	// This formula is as on http://www.fon.hum.uva.nl/Service/Statistics/Signed_Rank_Test.html
	// and should produce a Z on the Standard Normal Distribution, e.g. Prob(|Z|>=1.96) < 0.05
	// Some other sources use slightly different calculations.
	wilcoxonSRzScore {
		var w, n, corrector, stdev;
		
		w = this[0];
		n = this[1];
		
		if(n<16){ ("wilcoxonSRzScore: N=%. this approximation should only be used with large N, e.g. over 15."
					+ "Recommend you calculate exact significance instead.").format(n).warn };
		
		corrector = n.asFloat * (n+1).asFloat / 4.0;
		
		stdev = sqrt(  n.asFloat * (n+1).asFloat * ((n+n+1).asFloat /24.0)); // NB division early helps prevent limit errors in case of massiveness
		
		^(w - 0.5 - corrector)/stdev;
	}
		
	// Kendall's W statistic
	kendallW { |postChiSq=true|
		var m, n, s, w;
		
		m = this.size.asFloat;    // m == number of raters
		n = this[0].size.asFloat; // n == number of objects ranked
		
		s = this.sumF.variancePop * n; // Variance of sum-of-ranks (summed for each object) times n
		
		//"m  = %, n = %, s = %".format(m,n,s).postln;
		
		// The W statistic is:
		w = 12.0 * s / ( (m * m) * (n * n * n - n) );
		
		if(postChiSq){
			// Can use online tools to derive a p-value from this, e.g. http://faculty.vassar.edu/lowry/tabs.html#csq
			"For Kendall's W = %, Chi-square = % (% d.f.)".format(w, m * (n-1) * w, n-1).postln;
		};
		
		^w
	}
	
	// Given a list of values, returns their rank indices (starting at 1), assigning the mean rank in case of ties
/*
[5,3,7,6,8,8,5,4].rankVals
[6.34, 7, 2.4, 5.5, 6.34].rankVals
*/
	rankVals { |greatestFirst=true|
		var ranks, origIndices, vals, i, j;
		
		// An array of Association s in which the "value" is used as a key pointing to its original key
		origIndices = this.collectAs({|item, index| item->index }, Array);
		
		// Association means we sort by the keys, i.e. the data values
		origIndices = if(greatestFirst){
			origIndices.sort{ |a,b| a.key >= b.key }
		}{
			origIndices.sort{ |a,b| a.key <= b.key }
		};
				
		// We don't need the vals except to check for ties
		vals = origIndices.collect(_.key);
		// Then discard the values, now we just have the indices
		origIndices = origIndices.collect(_.value);
		
		// Remember the ranks must start from 1
		ranks = (1 .. this.size);
		// Now we need to go through and assign mean ranks in case of ties
		i=0;
		while{i<this.size}{
			j = i; // j is the starting position, i will be the finishing position
			while{vals[i+1].notNil and:{vals[i+1] == vals[i]}}{
				i = i + 1;
			};
			if(j != i){ // We need to meanify
				ranks[j..i] = ranks[j..i].mean
			};
			i = i + 1;
		};
		
		// Now again we need to sort back according to original indices
		^ranks.collect{|rank, index| origIndices[index] -> rank}
				.sort{ |a,b| a.key <= b.key }
				.collect(_.value);
	}
	
	// Spearman's rho is the Pearson correlation applied to the ranks
	/*
	spearmanRho([106, 86, 100, 101, 99, 103, 97, 113, 112, 110], [7, 0, 27, 50, 28, 29, 20, 12, 6, 17]);  // -0.17575757575758
	*/
	spearmanRho { |that|
		^corr(this.rankVals.asFloat, that.rankVals.asFloat)
	}

	// Simple paired nonparametric test of difference.
	// http://en.wikipedia.org/wiki/Sign_test
	// Returns: [test statistic, adjusted count, p-value]
	// Currently p-value calc only works for smallish total numbers - for larger numbers you'd use the normal approximation.
	signtest { |that, twotailed=true|
		var pval;
		var signs = this.size.collect{|i| (this[i] - that[i]).sign};
		var m = signs.count(_ != 0); // adjusted total excluding matches
		var w = signs.count(_ > 0); // test statistic, follows 0.5 binomial distrib with under H0
		if(w<(m*0.5)){w = m - w}; // ensure the statistic is in the top half, simplify signif test
		
		//if(m > 25){
			// approximate by normal with mean np, variance np(1-p).
			//z = (w - (m * 0.5)) / sqrt(m * 0.25);
			// NOT DONE: hmm sc doesn't have a way to find values of normal cdf.
		//}{
			pval = 0.5.binomial((w..m), m);
		//};
		if(twotailed){pval = pval + pval};
		^[w, m, pval]
	}

	
	// The Jarque-Bera test is a test of normality.
	// See http://en.wikipedia.org/wiki/Jarque-Bera_test
	jarqueBera { |skewness, kurtosis, n|
		var mean;
		// First derive the moments if needed
		if(skewness.isNil){
			if(kurtosis.isNil){
				mean = this.mean;
				skewness = this.skew(    mean);
				kurtosis = this.kurtosis(mean);
			}{
				skewness = this.skew
			}
		}{
			if(kurtosis.isNil){
				kurtosis = this.kurtosis
			}
		};
		if(n.isNil){ n = this.size };
		
		kurtosis = kurtosis - 3;
		// Now here's the actual JB statistic
		^(n / 6.0) * ( (skewness * skewness) + ((kurtosis*kurtosis)/4.0)  )
	}
/*
a = {1.0.rand}.dup(10000) // Shouldn't pass the test
a = {1.0.sum3rand}.dup(10000) // Should?
a.jarqueBera
// Now we need a function to assess the significance of the statistic's output value
*/
	
	// Order-Statistics Correlation Coefficient
	// see Xu et al 2007, DOI 10.1109/TSP.2007.899374
/*
x = (0 .. 100);
y = (0 .. 100);
oscorr(x,y);
x = (0 .. 100);
y = (100 .. 0);
oscorr(x,y);
x = (0 .. 100);
y = 100.dup(100) ++ 5;
oscorr(x,y); // Strange result here! Perfect correlation, despite drastic difference
x = (0 .. 100);
y = 100.dup(98) ++ [5, 50, -50]; // Hmm
oscorr(x,y);
x = {100.rand}.dup(101)
y = x
y = x + {100.rand}.dup(101)
y = x + {1000.rand}.dup(101)
oscorr(x,y);
*/
	oscorr { |that|
		// "this" is x, "that" is y
		var xsorted, ysorted, yconcomitant, last;
		
		// Compiles x and y into vector values, then sorts purely using x values
		#xsorted, yconcomitant = this.collect{|xitem, index| [xitem, that[index]]}.sortBy(0).flop;
		// Sorts y, we don't need to know the concomitant x values for the calculation
		ysorted = that.copy.sort; // NEED TO COPY for nondestructive! Grr
		
		last = this.lastIndex;
		
		^
			(xsorted.sumF{|xi, i| (xi - xsorted[last - i]) * yconcomitant[i] })
				/
			(xsorted.sumF{|xi, i| (xi - xsorted[last - i]) * ysorted[i] })
	}
	
		// return n sorted indices and values for a given sort function
	nSorted { |n, func| 
		var sorted, indexedArr;
		func = func ? { arg a, b; a < b }; 
		n = n ? this.size; 
		
		sorted = SortedList(n, { |a, b| func.value(a[1], b[1]) }); 
		
		this.do { |el, i| sorted.add([i, el]); 
			if(sorted.size > n) { sorted.removeAt(n) } 
		};
		^sorted.array
	}
	
	// Weighted mean and variance of a list of values.
	// To estimate mean and variance from a histogram:
	//    this = bin centres, weights = bin frequencies (heights)
	wmean { |weights|
		^this.sum{|val, index| val * weights[index]} / weights.sum;
	}
	wvariance { |weights, wmean|
		if(wmean.isNil, {wmean = this.wmean(weights)});
		^this.sum{|val, index| (val - wmean) * (val - wmean) * weights[index]} / weights.sum;
	}
	
	// Normalised autocorrelation, calculated for lags of zero up to "num"
	autocorr { |num, mean, sd|
		var data, sum, n;
		n = this.size;
		num  = num  ?? { n - 1 };
		mean = mean ?? { this.mean };
		sd   = sd   ?? { this.stdDev(mean) };
		
		data = this - mean;
		
		^num.collect{ |k|
			sum = 0;
			(0 .. n-k-1).do{ |t|
				sum = sum + (data[t] * data[t+k]);
			};
			sum = sum / ((n-k) * sd);
		};
	}
	
	// Find the first principal component in a data distribution, using expectation-maximisation method. "e" is termination threshold.
	// The data must already be centred (mean removed) and any scaling issues dealt with appropriately.
	pc1 { |e=0.000000000001|
		var dims = this[0].size, t={0.0}.dup(dims), tmag=0, tmag_old, p=this.choose, inc=inf;
		while{ inc > e }{
			t = this.sumF{|datum|
				((datum * p).sumF * datum)
			};
			tmag_old = tmag;
			tmag = t.sumF{|i| i*i}.sqrt;
			//"t: %  (|t|: %)".format(t, tmag).postln;
			p = t / tmag;
			inc = tmag - tmag_old;
			//"p: %  (inc: %)".format(p, inc).postln;
		};
		^p
	}
}


+ SimpleNumber {
	/*
	0.1.binomial(2,3)
	0.1.binomial(3,3)
	0.1.binomial(2,3) + 0.1.binomial(3,3)
	0.1.binomial((2..3), 3) // prob that it's either two or three, i.e. that it's >=2
	0.1.binomial((2..3), 30)
	0.1.binomial((2..30), 30)
	0.0000001.binomial((5..10), 10)
	
	// Roll a die ten times. How likely are we to get 3 sixes?
	(1/6).binomial(3, 10)
	// ...or at least 3 sixes?
	(1/6).binomial((3..10), 10)
	*/
	binomial { |numcorrect, numtrials|
		var diff, nfac, xfac, dfac;
		
		if(numcorrect.isArray){
			// probability that it's ANY of the options.
			^numcorrect.asSet.asArray.sum{|ncor| this.binomial(ncor, numtrials)};
		};
		
		diff = numtrials-numcorrect;
		nfac = (1..numtrials).asFloat.product;
		if(nfac==0){ nfac = 1 };
		xfac = (1..numcorrect).asFloat.product;
		if(xfac==0){ xfac = 1 };
		dfac = (1..diff).asFloat.product;
		if(dfac==0){ dfac = 1 };
		
		//"(% / % / %)".format(nfac, xfac, dfac).postln;
		
		^ (nfac / xfac / dfac) * (this ** numcorrect) * ((1-this) ** (numtrials-numcorrect))
	}
}
