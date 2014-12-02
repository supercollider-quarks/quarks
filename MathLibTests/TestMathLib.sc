/*
UnitTest.gui

These tests are for "miscellaneous" mathlib functions that don't have their own class.
*/
TestMathLib : UnitTest {
	
	test_correlation {
		var x, y, n, p;
		
		n = 1000000;
		
		// Uncorrelated
		x = {10.0.sum3rand}.dup(n);
		y = {10.0.sum3rand}.dup(n);
		p  = corr(x, y);
		this.assert(p.abs < 0.002, "corr() on uncorrelated data should give near-zero value (this test may [rarely] fail by chance)");
		// Correlated
		x = {10.0.sum3rand}.dup(n);
		y = x.deepCopy;
		p  = corr(x, y);
		this.assert(p == 1.0, "corr() on perfectly correlated data == 1");
		// Anticorrelated
		x = {10.0.sum3rand}.dup(n);
		y = x.collect(0.0 - _);
		p  = corr(x, y);
		this.assert(p == -1.0, "corr() on perfectly anticorrelated data == -1");
		
		
		n = 50000; // spearmanRho is much slower than corr so let's use slightly smaller data
		
		// Uncorrelated
		x = {10.0.sum3rand}.dup(n);
		y = {10.0.sum3rand}.dup(n);
		p  = spearmanRho(x, y);
		this.assert(p.abs < 0.012, "spearmanRho() on uncorrelated data should give near-zero value (this test may [rarely] fail by chance)");
		// Correlated
		x = {10.0.sum3rand}.dup(n);
		y = x.deepCopy;
		p  = spearmanRho(x, y);
		this.assert(p == 1.0, "spearmanRho() on perfectly correlated data == 1");
		// Anticorrelated
		x = {10.0.sum3rand}.dup(n);
		y = x.collect(0.0 - _);
		p  = spearmanRho(x, y);
		this.assert(p == -1.0, "spearmanRho() on perfectly anticorrelated data == -1");
		
	}
	
	test_princomp {
		var x, y, n, p, d;
		
		n = 100000;
		
		// Correlated
		x = {10.0.sum3rand}.dup(n);
		y = x.deepCopy;
		p  = pc1([x, y].flop);
		this.assert(p[0] == p[1], "pc1() on perfectly correlated data gives a vector pointing along [+1, +1] or [-1, -1]");
		// Anticorrelated
		x = {10.0.sum3rand}.dup(n);
		y = x.collect(0.0 - _);
		p  = pc1([x, y].flop);
		this.assert(p[0] == (0.0 - p[1]), "pc1() on perfectly anticorrelated data gives a vector pointing along [+1, -1] or [-1, +1]");
		// A specific bimodal distrib
		d = n.collect{ if(0.5.coin){[-2, -0.5]}{[2, 0.5]}.collect{|v| v + 0.95.sum3rand} };
		// GNUPlot.new.scatter(d);
		p  = pc1(d);
		this.assertArrayFloatEquals(p.abs, [ 0.97016356074164, 0.24245103350099 ], "pc1() on a specific bimodal distribution", 0.01);
	}
}
