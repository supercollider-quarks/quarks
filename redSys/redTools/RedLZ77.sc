//redFrik

RedLZ77 {
	classvar <>window= 4096, <>length= 32;
	
	*compress {|input|
		var out= [], i= 0, len, off, sub, j;
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
				out= out++[0, 0, sub[0]];
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
				out= out++[len, i.min(window-1)-off];
				i= i+len;
				if(i<input.size, {
					out= out++input[i];
					i= i+1;
				});
			});
		});
		^out;
	}
	*decompress {|input|
		var out= [], i= 0, len, off;
		while({i<input.size}, {
			len= input[i];
			if(len==0, {
				out= out++input[i+2];
			}, {
				off= input[i+1];
				while({len>0}, {
					out= out++out[out.size-off];
					len= len-1;
				});
				if(i+2<input.size, {
					out= out++input[i+2];
				});
			});
			i= i+3;
		});
		^out;
	}
}
