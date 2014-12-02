//redFrik

RedLZSS {
	classvar <>window= 4096, <>length= 32, <>pad;
	
	*compress {|input|
		var out= "", i= 0, len, off, sub, j;
		var bitsWin= (window-1).numBits;
		var bitsLen= (length-1).numBits;
		while({i<input.size}, {
			len= length;
			while({
				sub= input.copyRange(i, (i+len-1).min(input.size-1));
				len= len.min(sub.size);
				off= input.copyRange((i-window+1).max(0), i-1).find(sub);
				off.isNil and:{len>1};
			}, {
				len= len-1;
			});
			if(off.isNil, {
				out= out++0++sub[0].asBinaryString(8);
				i= i+1;
			}, {
				if(off+len==i, {
					j= 0;
					while({
						input[i+len+j]==input[i+j] and:{len+j<length};
					}, {
						j= j+1;
					});
					len= len+j;
				});
				if(len>2, {
					out= out++1++(len-2).asBinaryString(bitsLen)
						++(i.min(window-1)-off).asBinaryString(bitsWin);
					i= i+len;
					if(i<input.size, {
						out= out++input[i].asBinaryString(8);
						i= i+1;
					});
				}, {
					len.do{|x|
						if(i+x<input.size, {
							out= out++0++input[i+x].asBinaryString(8);
						});
					};
					i= i+len;
				});
			});
		});
		^out;
	}
	*decompress {|input|
		var out= [], i= 0, len, off;
		var bitsWin= (window-1).numBits;
		var bitsLen= (length-1).numBits;
		while({i<input.size}, {
			if(input[i].digit==0, {
				i= i+1;
				out= out++("2r"++input.copyRange(i, i+7)).interpret;
				i= i+8;
			}, {
				i= i+1;
				len= ("2r"++input.copyRange(i, i+bitsLen-1)).interpret+2;
				i= i+bitsLen;
				off= ("2r"++input.copyRange(i, i+bitsWin-1)).interpret;
				i= i+bitsWin;
				while({len>0}, {
					out= out++out[out.size-off];
					len= len-1;
				});
				if(i<input.size, {
					out= out++("2r"++input.copyRange(i, i+7)).interpret;
					i= i+8;
				});
			});
		});
		^out;
	}
	*binaryStringToBytes {|str|
		pad= 0;
		^str.clump(8).collect{|x|
			while({x.size<8}, {
				x= x++0;
				pad= pad+1;
			});
			("2r"++x).interpret;
		};
	}
	*bytesToBinaryString {|arr|
		var str= arr.collect{|x|
			x.asBinaryString(8);
		}.join;
		^str.copyRange(0, str.size-1-pad);
	}
}
