//redFrik

RedALF {
	*atolf {|str|
		var res= [1/2**5];
		var tre= [2**12, 2**20, 2**28];
		str.do{|chr, i|
			var j= i.div(3);
			if(i%3==2, {
				res= res++(1/2**5);
			});
			res.put(j, res[j]+(chr.ascii/tre[i%3]));
		};
		^res;
	}
	*lftoa {|arr|
		var res= "";
		arr.do{|val|
			var a, b, c;
			val= val-(1/2**5)*(2**12);
			a= val.asInteger;
			val= val-a*(2**8);
			b= val.asInteger;
			val= val-b*(2**8);
			c= val.asInteger;
			res= res++a.asAscii++b.asAscii++c.asAscii;
		};
		^res;
	}
}

/*
a= RedALF.atolf("aber");
RedALF.lftoa(a);
*/
