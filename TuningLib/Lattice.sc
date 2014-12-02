// This code by Charles CŽleste Hutchins
// Contains the tuning theories of Ellen Fullman

Lattice
{

	// an misleadingly named object which holds one row of a tuning table based on overtones
	
	/*@
	shortDesc: An object which holds one row of a tuning table based on overtones
	longDesc: This uses the sort of Tuning Table used by Ellen Fullman. One row looks like
	@*/
	/*	
	1/1	5/4	3/2	7/4	9/8 for otonality or
	1/1	8/5	4/3	8/7	19/6	for utonality
	
	It also is able to generate and navigate a 3 dimentinal table:
	
	1/1	5/4	3/2	7/4	9/8
	8/5	1/1	6/5	7/5	9/5
	4/3	5/3	1/1	7/6	3/2
	8/7	10/7	12/7	1/1	9/7
	16/9	10/9	4/3	14/9	1/1
	*/


	var <>overtones, <>otonality, <>utonality, <>lastPivot;
	
	*new { arg otones = nil, base = 2;
	
	/*@ 
	desc: Creates a new Lattice
	otones: An array of numbers to use for numerators in otonality. Defaults to 
	[ 2, 5, 3, 7, 9, 11, 13, 15, 17, 19, 21, 23]
	base: For octave based systems, this is 2, but if you wanted to base your system on 3s, 
	you could use that instead. Defaults to 2.
	ex:
	l = Lattice.new([2, 5, 3, 7, 9]);
	// generates 1/1	5/4	3/2	7/4	9/8 for otonality as described above
	@*/
	
		^super.new.init(otones, base);
	}
	
	
	init { arg otones = nil, base = 2;
	
		var numerator, denominator;
	
		if (otones == nil , {
			overtones  = [ 2, 5, 3, 7, 9, 11, 13, 15, 17, 19, 21, 23];
		} , {
			overtones = otones;
		});
		// if no overtone series or base is provided, we go for
		// Ellen's tuning table with a base of 2
			
			
		// we generate two arrays.  One with the utonal series and
		// one with otnal series.  compute from the overtones and the base

		// otanlity is the (overtone / base) * 2 ^^x
		// utonality is the (base / overtone) * 2 ^^x
		otonality = overtones.collect({ arg num;

			// the numerator represents the base * 2^^x in utonality
			// the denominator represents the base * 2^^x in otonality
			numerator = base;
			denominator =base;
		
			
			// we want the fractions to be between 1 & 2
			// so, except for n/n, the numerator in utonality will
			// always be twice as large as the denominator in otonality.
			// we multiply each by 2 until the numerator variable
			// is larger than the overtone.
			
			{numerator <= num}.while {
				denominator = numerator;
				numerator = numerator * 2;
			};
			
			utonality = utonality.add(numerator);
			denominator;
		});
		
		
	}
	
	*adjustOctave { arg ratio;
	/*@
	desc: For a given ratio, does octave transpositions and returns a ratio between 1 and 2
	ex:
	Lattice.adjustOctave(9/2) // returns 1.125, which is 9/8
	@*/			
		{ratio < 1}.while ({
			ratio = ratio * 2;
		});
		{ratio > 2}.while ( {
			ratio = ratio / 2;
			//ratio.postln;
		});
		
		//ratio.postln;
		
		^ratio;
	}
	
	
	makeOvertoneIntervals { arg start, orientation;
	/*@
	desc: For a given index, return a triad of ratios
	start: The index in the lattice row in which to start. This wraps if need be.
	orientation: Use true for utonality or false for otonailty. 
	The triad is computed in regards to the base.
	ex:
	l = Lattice.new([2, 5, 3, 7, 9]);
	l.makeOvertoneIntervals(4, true);
	l.makeOvertoneIntervals( 2, false);
	@*/
	
		// this method returns a triad of three notes above the start
		// if the start is too close to the end of the array, we wrap
		// back around to the beginning of the array
		var result;
		
		// otonality = true orientation
		// utonality = false orientation
		if (orientation, {
		
			// if were smarter the utonality and otonality arrays would 
			// contain the RESULTS of these computations
			
			result = [0, 1, 2].collect({ arg offset;
				overtones.at((start + offset) % overtones.size) / 
					otonality.at((start + offset) % otonality.size) });
		} , {
			result = [0, 1, 2].collect({ arg offset;
				utonality.at((start + offset) % utonality.size) / 
					overtones.at((start + offset) % overtones.size) });
		});
		
		^result;
	}
	
	
	makeIntervals { arg x, y, orientation;
	/*@
	desc: For a given x and y index, return a triad of ratios
	x: The index in the lattice row for o[x]/o[y] This wraps if need be.
	y: The index in the lattice row for o[x]/o[y] This wraps if need be.
	orientation: Use true for utonality such that the returned triads will change in the numerator, but not the demonimator.  To change the denominator, use false. 
	ex:
	l = Lattice.new([2, 5, 3, 7, 9]);
	l.makeIntervals(4, 3, true); // returns [9/7, 8/7, 10/7] 
	l.makeIntervals(2, 2, false); // returns [1/1, 12/7, 4/3]
	@*/
		
		var result, ratio;
		
		if (orientation, {
		
			result = [0, 1, 2].collect({ arg offset;
				Lattice.adjustOctave(overtones.wrapAt(x + offset) /
					overtones.wrapAt(y)) });
				//overtones.at((x + offset) % overtones.size) /
				//	overtones.at(y % overtones.size);
			//});
					
		} , {
			result = [0, 1, 2].collect({ arg offset;
				Lattice.adjustOctave(overtones.wrapAt(x) /
					overtones.wrapAt(y + offset)) });
				
				//overtones.wrapAt(x) /
				//	overtones.wrapAt(y + offset);
			//});
		});
		
		^result;
	}
	
	getOvertoneInterval { arg index, orientation;
	
		/*@ 
		desc: Returns the fraction at the index
		index: The index of the overtone or undertone
		orientation: true for otonality, false for utonality
		ex:
		l = Lattice.new([2, 5, 3, 7, 9]);
		l.getOvertoneInterval(2, true); // returns 3/2
		l.getOvertoneInterval(3, false); // returns 8/7
		@*/
	
		var result;
		
		if (orientation, {
		
			result = overtones.at(index % overtones.size) / otonality.at(index % otonality.size);
		} , {
			result = utonality.at(index % utonality.size) / overtones.at(index % overtones.size);
		});
		
		^result;
	}
	
	
	
	getInterval { arg x, y;
	
	/*@
	desc: For a given x and y index, return a ratio
	x: The index in the lattice row for o[x]/o[y] This wraps if need be.
	y: The index in the lattice row for o[x]/o[y] This wraps if need be.
	@*/
		var result;
		
		
		result = overtones.wrapAt(x) / overtones.wrapAt(y);
		
		^result;
	}
	
	
	d2Pivot { arg start;
	
	/*@
	desc: Find a pivot based on a start point. Finding the new pivot point means picking one of the fractions in a triad. Finding the new start means figuring out wether the new pivot is the top, middle, or bottom 
	member of the triad and computing a new start index based on that computation
	start: The starting index of the triad, in which we wish to pivot
	@*/
	
		var pivot, new_start;
		
		pivot = start + (3.rand);
		new_start = pivot - (3.rand);
		
		^[new_start, pivot];
	}
	
	d3Pivot { arg x, y, orientation;
	
	/*@
	desc: Find a pivot based on a start point, describe by o[x]/o[y].
	Finding the new pivot point means picking one of the fractions in a triad
	Finding the new start means figuring out wether the new pivot is the top, middle, or bottom 
	member of the triad and computing a new start index based on that computation
	x: The starting index of the numerator of the triad in which we wish to pivot
	y: The starting index of the denominator of the triad in which we wish to pivot
	orientation: If true, the overtones will change in the pivot. If false, the 
	undertones will change in the pivot.
	@*/
		var pivotx, pivoty, startx, starty;
		
		if (orientation, {

			pivotx = (x + 3.rand) % overtones.size;
			pivoty = y;
			startx = (pivotx - 3.rand) % overtones.size;
			starty = y;		
			
		} , {
		
			pivotx = x;
			pivoty = (y + 3.rand) % overtones.size;
			startx = x;
			starty = (pivoty - 3.rand) % overtones.size;

		});
		
		^[startx, starty, pivotx, pivoty];
	
	}
	
	
}