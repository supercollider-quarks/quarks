BinauralDistance {
	*new { |d = 1.4, a = 0, hr = 0.09|
	
	    // a = hoek van bron (0-2pi) - 0.5pi = recht voor
		// d = afstand van bron
		// hr = head radius (afstand tussen oren / 2)
		// s = afstand lijn tot waar hoofd geraakt wordt bij omlopend geluid (voor l/r hetzelfde)
		// sa = hoek afwijking relatief aan a voor linkerkant
		// la / ra = afstand via hoofd naar oren in radians (0-2pi)
		// ld / rd = afstand van bron naar oren in rechte lijn
		// l / r = eindresultaat: afstand (in m)
				
	    var s, sa, la, ra, ld, rd, l, r;
	    
		s = (d.squared - hr.squared).sqrt;
		sa = (s / hr).atan;
		
		la = max( (a - pi).wrap(-pi, pi).abs - sa, 0);
		ra = max(  a      .wrap(-pi, pi).abs - sa, 0);
		
		ld = ( ( ( d * cos(a) ) + hr ).squared + ( d * sin(a) ).squared ).sqrt;
		rd = ( ( ( d * cos(a) ) - hr ).squared + ( d * sin(a) ).squared ).sqrt;
		
		l = min( s, ld ) + ( la * hr );
		r = min( s, rd ) + ( ra * hr );
		
		^[  l,  r,   // left / right distance in m via head
		    ld, rd,  // left / right distance straight line
		    la, ra, // only the distance against head
		    s,      // distance where hits head
		    sa      // angle offset where head is hit 
		  ];
		}
		
	*calcDelays { |l = 0, r = 0, speedOfSound = 334|
		^[ l / speedOfSound, r / speedOfSound ]
		}
		
	*calcDelaysNoDoppler { |l = 0, r = 0, speedOfSound = 334|
		var diff;
		diff = l-r;
		^[ diff / speedOfSound, diff.neg / speedOfSound ] / 2;
		}
		
	*calcLevels { |l = 0, r = 0, ref = 0.5| 
	     // ref the distance where level is 0dB meters
	     // should not be 0
	     // the level will never exceed 0dB
		^[  ref / (l.max(0) + ref),
		    ref / (r.max(0) + ref) ];
		}
	
	}
	
PanBin {
	classvar <>speedOfSound = 334, <>headRadius = 0.09, <>ref = 0.09;
	
	*ar { |in, distance = 1.4, angle = 0, maxDist = 500|
		var binVals;
		binVals = BinauralDistance( distance, angle, headRadius );
		^DelayC.ar( in * BinauralDistance.calcLevels(  binVals[0], binVals[1], ref ), 
			maxDist / speedOfSound, 
			BinauralDistance.calcDelays( binVals[0], binVals[1], speedOfSound ) )
		}
	
	}
	
	
PanBinAz { // a panned input for Kemar2 in circle 2D setup
	// outputs pairs of PanBin-ed channels
	
	*ar { |numChans, in, distance = 1.4, angle = 0, maxDist = 500|
		^LinPanAz.ar( numChans,
	   			PanBin.ar( in ,distance, angle )
	   			, 0.5 - (angle / pi), orientation: 0 ) 
		}
	}
	
