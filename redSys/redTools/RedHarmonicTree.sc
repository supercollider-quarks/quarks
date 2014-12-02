//redFrik

RedHarmonicTree {
	*sorted {|base= 110, num= 8, range= 3|
		var list, ratio;
		ratio= {
			var temp= {range.rand+1}.dup(2).sort;
			temp[0]/temp[1];
		};
		list= {ratio.value}.dup(num.max(1)-1).sort{|a, b| a>b}.pow(-1);
		^[base]++list.collect{|x| base= base*x};
	}
}
