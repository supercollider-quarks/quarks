// part of wslib 2005
//
// Spread multiple audio-rate sources over specific number of speakers
// using PanAz.

+ Mix {
	*panSpread {arg array, nCha = 2, width = 2, offset = 0;
	//array must be audio-rate
	this.deprecated( thisMethod, Meta_SplayAz.findMethod(\ar) );
	^Mix.fill( array.size, { |i|
			PanAz.ar(nCha, array[i], ((width * i)/array.size) + offset)
			});
	}
	*panSpreadFill { arg n, func, nCha = 2, width = 2, offset = 0;
		this.deprecated( thisMethod, Meta_SplayAz.findMethod(\arFill));
		^Mix.fill( n, { |i|
			PanAz.ar(nCha, func.(i), ((width/n) * i) + offset)
			});
	}	
}