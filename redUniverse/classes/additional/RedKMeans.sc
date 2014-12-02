// this file is part of redUniverse toolkit /redFrik

RedKMeans {
	var <>k, <>max, <>centroids, <classifications;
	*new {|k= 5, max= 15|
		^super.newCopyArgs(k, max).reset;
	}
	reset {
		centroids= [];
		classifications= [];
	}
	classify {|vec|
		var minDist= inf, class;
		centroids.do{|cent, j|
			var dist= vec.manhattan(cent);			//manhattan distance between vectors
			if(dist<minDist, {
				class= j;
				minDist= 	dist;
			});
		};
		^class									//return index in classifications
	}
	update {|vectors|
		var dirty, cnt= 0, classSum, classCnt;
		if(centroids.isEmpty, {						//only create new centroids after a reset
			centroids= Array.fill(k, {vectors.choose});
		}, {
			if(k!=centroids.size, {					//if k changed then truncate or extend
				if(k<centroids.size, {
					centroids= centroids.copyRange(0, k-1);
				}, {
					(k-centroids.size).do{
						centroids= centroids.add(vectors.choose);
					};
				});
			});
		});
		while({cnt<max}, {
			dirty= false;
			classifications= vectors.collect{|vec, i|
				var class= this.classify(vec);
				if(class!=classifications[i], {		//check if vector changed class
					dirty= true;
				});
				class;
			};
			if(dirty.not, {						//no vector changed class so finish
				cnt= max;
			}, {
				classSum= Array.fill(k, {0});
				classCnt= classSum.copy;
				vectors.do{|vec, i|
					var class= classifications[i];
					classSum[class]= classSum[class]+vec;
					classCnt[class]= classCnt[class]+1;
				};
				classSum.do{|sum, i|
					if(classCnt[i]>0, {
						centroids[i]= sum/classCnt[i];
					});
				};
				cnt= cnt+1;
			});
		});
	}
}
