/*
k-means classifier implementation (c) 2009 Dan Stowell, published under GPLv2 or later.
*/
KMeans {
	var 
		//newCopyArgs:
		<k,
		// other vars:
		<data, <centroids, <assignments;
	
	*new { |k|
		^this.newCopyArgs(k).init
	}
	init {
		data = [];
		assignments = [];
		centroids = [];
	}
	
	// learns the data point: stores it, and classifies it.
	// (if <k points so far, it becomes an init centroid)
	// This DOES NOT recalculate the centroids, please call .update explicitly when you want to do that.
	add { |datum|
		data = data ++ [datum];
		if(centroids.size==k){
			assignments = assignments ++ this.classify(datum);
		}{
			assignments = assignments ++ centroids.size;
			centroids = centroids ++ [datum];
		}
	}
	
	// Choose random centroids by randomly resampling from the data-derived marginals
	reset {
		k.do{|i|
			centroids[i] = data[0].size.collect{|d| data.choose[d]}
		};
		data.do{|datum, i|
			assignments[i] = this.classify(datum);
		};
	}
	
	// Iterates, updating centroid positions and assignments, until all the class assignments are stable
	update {
		var anyChange=true, centroidsums, centroidcounts, whichcent, dist;
		
		while{anyChange}{
			// Each centroid is recalculated as the mean of its datapoints
			centroidsums   = {{0}.dup(data[0].size)}.dup(k);
			centroidcounts = {0}.dup(k);
			data.do{|datum, index|
				whichcent = assignments[index];
				centroidsums[  whichcent] = centroidsums[  whichcent] + datum;
				centroidcounts[whichcent] = centroidcounts[whichcent] + 1;
			};
			centroidsums.do{ |asum, index|
				if(centroidcounts[index] != 0){
					centroids[index] = asum / centroidcounts[index]
				};
			};
			
			anyChange=false;
			// Datapoint classifications are checked - if any need to be updated then we'll go round again
			data.do{|datum, index|
				whichcent = this.classify(datum);
				if(whichcent != assignments[index]){
					assignments[index] = whichcent;
					anyChange = true;
				}
			};
		}; // end while
	}
	
	// classify a point without adding it to the training set
	classify { |datum|
		var dist=inf, class=nil, adist;
		centroids.do{|cent, index|
			adist = (cent-datum);
			adist = (adist*adist).sum;
			if(adist < dist){
				class = index;
				dist = adist;
			}
		}
		^class
	}
	
}


/*
KMeans.test
*/
TestKMeans : UnitTest {
	test_fuzzybinary {
		var k, p, c;
		
		[1,2,3,4].do{|d|
			k = KMeans.new(2);
			p = [{-2}.dup(d), {2}.dup(d)];
			1000.do{
				k.add(p.choose + {0.5.sum3rand}.dup(d))
			};
			k.reset.update;
			c = k.centroids;
			c.sortBy(0);
			c = (p-c).flat.abs.postln;
			this.assertArrayFloatEquals(c, 0.0, 
				"clustering %D fuzzybinary data should approximately recover the true centroids".format(d), 0.1)
		};
		
		
	}
	test_rangesanity {
		var k, p, c, range, rescaled;
		
		[1,2,3,4].do{|d|
			[1,2,3,4].do{|numclust|
				k = KMeans.new(numclust);
				p = {100.0.rand}.dup(d);
				1000.do{
					k.add(p)
				};
				k.reset.update;
				c = k.centroids;

				c.do{|acent|
					this.assertArrayFloatEquals(acent, p, 
						"clustering %D constant data should give centroids at the constant value".format(d))
				};

				//////
				k = KMeans.new(numclust);
				range = {{100.0.rand}.dup.integrate}.dup(d).flop;
				1000.do{
					k.add(d.collect{|whichd| 1.0.rand.linlin(0,1,range[0][whichd], range[1][whichd])})
				};
				k.reset.update;
				c = k.centroids;

				c.do{|acent|
					rescaled = (acent - range[0]) / (range[1] - range[0]);
					this.assert(rescaled.every{|val| (val>=0) and: {val<=1}},
						"clustering %D range-limited data should give centroids within that range".format(d))
				};

			};
		};
		
		
	}
} // end class TestKMeans


