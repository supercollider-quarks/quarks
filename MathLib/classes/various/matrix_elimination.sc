+ Array {// gauss-jordan matrix elimination, by Kathi Vogt
	
	eliminateMatrix {
		var array = this.deepCopy;
		var lastRow = this.size - 1, lastCol = this[0].size - 1;
		var n = min(lastRow, lastCol);
		var z, diag, fk, ee;
		(0 .. n - 1).do {|i|
				diag = array[i][i];
				(i + 1 .. lastRow).do { |j|
						fk = array[j][i] / diag;
						(i .. lastCol).do { |k|
								array[j][k] = array[j][k] - (fk * array[i][k])
						}
				}

		};

		z = {|i| array[i][n + 1] }.dup(n + 1);
		(n .. 1).do {|i|
				diag = array[i][i];
				ee = z[i];
				(i-1 .. 0).do {|j|
						z[j] = z[j] - (ee * array[j][i] / diag);
				}
		};
		^z.collect {|el, i| el / array[i][i] };	
	}
}