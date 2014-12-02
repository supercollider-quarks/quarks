//redFrik

//todo:
//implement table as tree instead of array for speed?

RedLZ78 {
	*compress {|input|
		var tab= [], out= [], i= 0, j, match, sub, last;
		while({i<input.size}, {
			j= 0;
			last= 0;
			while({
				sub= input.copyRange(i, i+j);
				match= tab.find([sub]);
				match.notNil and:{i+j<(input.size-1)};
			}, {
				last= match;
				j= j+1;
			});
			tab= tab++[sub];
			if(j==0, {
				out= out++0++input[i+j];
			}, {
				out= out++(last+1)++input[i+j];
			});
			i= i+1+j;
		});
		^out;
	}
	*decompress {|input|
		var tab= [], out= [], i= 0, j, match, sub, val;
		while({i<input.size}, {
			match= input[i];
			val= input[i+1];
			if(match==0, {
				sub= [val];
			}, {
				sub= tab[match-1]++val;
			});
			tab= tab.add(sub);
			out= out++sub;
			i= i+2;
		});
		^out;
	}
}
