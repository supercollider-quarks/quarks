// This code by Charles Celeste Hutchins
// Contains the tuning theories of Harry Partch via Ellen Fullman

Diamond
{

	// an object which holds one row of a tuning table based on overtones

	/*@
	shortDesc: An object which holds one row of a tuning table based on overtones
	longDesc: An N-limit tonaility diamond, as used by Harry Partch.

With a 9-limit diamond in base 2, one row looks like:
1/1	5/4	3/2	7/4	9/8 for otonality or
1/1	8/5	4/3	8/7	19/6	for utonality

It also is able to generate and navigate a 2 dimentinal table:

1/1  5/4  3/2  7/4  9/8
8/5  1/1  6/5  7/5  9/5
4/3  5/3  1/1  7/6  3/2
8/7  10/7 12/7 1/1  9/7
16/9 10/9 4/3  14/9 1/1
	@*/


	var <>overtones, /*<>otonality, <>utonality,*/ <>lastPivot, base;

	*new { arg otones = nil, base = 2;

	/*@
	desc: Creates a new Daimond
	otones: An array of numbers to use for numerators in otonality. Defaults to
	[ 2, 5, 3, 7, 9, 11, 13, 15, 17, 19, 21, 23]
	base: For octave based systems, this is 2, but if you wanted to base your system on 3s,
	you could use that instead. Defaults to 2.
	ex:
	l = Diamond.new([2, 5, 3, 7, 9]);
	// generates 1/1	5/4	3/2	7/4	9/8 for otonality as described above
	@*/
		if (otones.notNil, {
			if (otones.isKindOf(SimpleNumber), {
				^Diamond.limit(otones, base);
		})});

		^super.new.init(otones, base);
	}

	*limit {arg limit=5, base=2;

		var arr, int, min;

		// figure out the minimum limit based on the base
		min = base + 3;
		// min must be odd, so if it comes out to an even number, add 1
		((min % 2) == 0).if ({
			min = min + 1
		});


		(limit < min).if({
			("The limit must be at least " ++ min).warn;
			limit = min;
		});

		// the diamond starts [2, 5, 3] for base 2. Keep that order, but generalise for other bases
		arr = [];

		[2, 5, 3].do({|i|
			(i >= base).if ({
				arr = arr.add(i);
			});
		});


		(limit > 5).if({
			int = 7;
			{int <= limit}. while ({
				(int >= base).if ({
					if ((int % base) != 0, {
						arr = arr.add(int);
					});
				});
				int = int +2;
			})
		});

		^super.new.init(arr, base);

	}



	init { arg otones = nil, bas = 2;

		var numerator, denominator;

		base = bas;

		if (otones == nil , {
			overtones  = [ 2, 5, 3, 7, 9, 11, 13, 15, 17, 19, 21, 23];
		} , {
			overtones = otones;
		});

		// Instead of computing these arrays, we'll just adjust the octave as needed.


		// if no overtone series or base is provided, we go for
		// Ellen's tuning table with a base of 2

		/*
		// we generate two arrays.  One with the utonal series and
		// one with otnal series.  compute from the overtones and the base

		// otanlity is the (overtone / base) * base ^^x
		// utonality is the (base / overtone) * base ^^x
		otonality = overtones.collect({ arg num;

			// the numerator represents the base * base^^x in utonality
			// the denominator represents the base * base^^x in otonality
			numerator = base;
			denominator =base;


			// we want the fractions to be between 1 & 2
			// so, except for n/n, the numerator in utonality will
			// always be twice as large as the denominator in otonality.
			// we multiply each by the base until the numerator variable
			// is larger than the overtone.

			{numerator <= num}.while {
				denominator = numerator;
				numerator = numerator * base;
			};

			utonality = utonality.add(numerator);
			denominator;
		});
		*/


	}

	*adjustOctave { arg ratio, base =2;
	/*@
	desc: For a given ratio, does octave transpositions and returns a ratio between 1 and 2
		ratio: the ratio to adjust
		base: the octave multiplier. Usually this should be left as 2, for for some tuning systems, like Bohlen-Pierce, it might be another number.
	ex:
	Diamond.adjustOctave(9/2) // returns 1.125, which is 9/8
	@*/
		/*
		{ratio < 1}.while ({
			ratio = ratio * 2;
		});
		{ratio > 2}.while ( {
			ratio = ratio / 2;
			//ratio.postln;
		});

		//ratio.postln;
		*/

		ratio = ratio / (ratio.previousPowerOf(base))

		^ratio;
	}


	makeOvertoneIntervals { arg start, orientation, niad=3;
	/*@
	desc: For a given index, return a n-iad of ratios
	start: The index in the lattice row in which to start. This wraps if need be.
	orientation: Use true for utonality or false for otonailty.
		niad: The number of contigious ratios to return in the chord. This defaults to 3 for a triad
	The n-iad is computed in regards to the base.
	ex:
	d = Diamond.new([2, 5, 3, 7, 9]);
	d.makeOvertoneIntervals(4, true); // returns [9/8, 2/2, 5/4]
	d.makeOvertoneIntervals( 2, false); // returns [4/3, 8/7, 16/9]
	@*/

		// this method returns a triad of three notes above the start
		// if the start is too close to the end of the array, we wrap
		// back around to the beginning of the array
		var result, arr;

		// otonality = true orientation
		// utonality = false orientation

		/*
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
		*/

		arr = Array.series(niad); // do the offsets

		result = arr.collect({arg offset;
			overtones.wrapAt(start + offset);
		});

		if ( orientation,
			{
				result = result /2;
			},
			{
				result = 2/result;
		});

		result = result.collect({arg ratio;
			Diamond.adjustOctave(ratio, base);
		});

		^result;
	}

	makeTriad { arg interger, orientation;
		/*@
		desc: for a given numerator or denominator, return a triad of ratios
		interger: the numerator or denominator for the first item in the triad. The triad will wrap around if three spaces past the integer goes beyond the limit.
		orientation: Use true for utonality or false for otonailty.
		ex:
		d = Diamond.limit(9);
		d.makeTriad(7, true); // returns [7/4, 9/8, 2/2]
		@*/

		var index;

		index = this.pr_indexOf(interger);
		^this.makeOvertoneIntervals(index, orientation, 3);

	}

	makeNiad { arg interger, orientation, n=3;
		/*@
		desc: for a given numerator or denominator, return a chord of n ratios
		interger: the numerator or denominator for the first item in the triad. The triad will wrap around if three spaces past the integer goes beyond the limit.
		orientation: Use true for utonality or false for otonailty.
		n: the number of conitguious ratios for the N-iad;
		ex:
		d = Diamond.limit(9);
		d.makeTriad(7, true); // returns [7/4, 9/8, 2/2]
		@*/

		var index;

		index = this.pr_indexOf(interger);
		^this.makeOvertoneIntervals(index, orientation, n);
	}

	makeIntervals { arg x, y, orientation, niad=3;
	/*@
	desc: For a given x and y index, return a n-iad of ratios
	x: The index in the diamond row for o[x]/o[y] This wraps if need be.
	y: The index in the diamond column for o[x]/o[y] This wraps if need be.
	orientation: Use true for utonality such that the returned triads will change in the numerator, but not the demonimator.  To change the denominator, use false.
		niad: The number of contigious ratios to return in the chord. This defaults to 3 for a triad

	ex:
	d = Diamond.new([2, 5, 3, 7, 9]);
	d.makeIntervals(4, 3, true); // returns [9/7, 8/7, 10/7]
	d.makeIntervals(2, 2, false); // returns [1/1, 12/7, 4/3]
	@*/

		var result, ratio, arr;

		arr = Array.series(niad); // do the offsets

		if (orientation, {

			result = arr.collect({ arg offset;
				Diamond.adjustOctave(overtones.wrapAt(x + offset) /
					overtones.wrapAt(y)) });
				//overtones.at((x + offset) % overtones.size) /
				//	overtones.at(y % overtones.size);
			//});

		} , {
			result = arr.collect({ arg offset;
				Diamond.adjustOctave(overtones.wrapAt(x) /
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
		d = Diamond.new([2, 5, 3, 7, 9]);
		d.getOvertoneInterval(2, true); // returns 3/2
		d.getOvertoneInterval(3, false); // returns 8/7
		@*/

		var result;

		if (orientation, {

			result = overtones.at(index % overtones.size) / base;
		} , {
			result = base / overtones.at(index % overtones.size);
		});

		^Diamond.adjustOctave(result, base);
	}



	getInterval { arg x, y;

	/*@
	desc: For a given x and y index, return a ratio
	x: The index in the diamond row for o[x]/o[y] This wraps if need be.
	y: The index in the diamond column for o[x]/o[y] This wraps if need be.
	@*/
		var result;


		result = overtones.wrapAt(x) / overtones.wrapAt(y);

		^Diamond.adjustOctave(result, base);
	}


	d1Pivot { arg start, niad=3;

	/*@
	desc: Find a pivot based on a start point. Finding the new pivot point means picking one of the fractions in a n-iad. Finding the new start means figuring out wether the new pivot is the top, middle, or bottom
	member of the n-iad and computing a new start index based on that computation
	start: The starting index of the n-iad, in which we wish to pivot
		niad: The number of contigious ratios to return in the chord. This defaults to 3 for a triad
	@*/

		var pivot, new_start;

		pivot = start + (niad.rand);
		new_start = pivot - (niad.rand);

		^[new_start, pivot];
	}

	d2Pivot { arg x, y, orientation, niad=3;

	/*@
	desc: Find a pivot based on a start point, describe by o[x]/o[y].
	Finding the new pivot point means picking one of the fractions in a n-iad
	Finding the new start means figuring out whether the new pivot is the top, middle, or bottom
	member of the triad and computing a new start index based on that computation
	x: The starting index of the numerator of the n-iad in which we wish to pivot
	y: The starting index of the denominator of the n-iad in which we wish to pivot
	orientation: If true, the overtones will change in the pivot. If false, the
	undertones will change in the pivot.
		niad: The number of contigious ratios to return in the chord. This defaults to 3 for a triad.
	@*/
		var pivotx, pivoty, startx, starty;

		//DeprecatedError(this, "d2Pivot", "pivotxy", Diamond).throw;

		if (orientation, {

			pivotx = (x + niad.rand) % overtones.size;
			pivoty = y;
			startx = (pivotx - niad.rand) % overtones.size;
			starty = y;

		} , {

			pivotx = x;
			pivoty = (y + niad.rand) % overtones.size;
			startx = x;
			starty = (pivoty - niad.rand) % overtones.size;

		});

		^[startx, starty, pivotx, pivoty];

	}

	d2WalkXY { arg startX, startY, orientation, niad=3;

	/*@
	desc: Start walking based on a start point, describe by o[x]/o[y].
	Finding the new pivot point means picking one of the fractions in a n-iad
	Finding the new start means figuring out whether the new pivot is the top, middle, or bottom
	member of the triad and computing a new start index based on that computation
	x: The starting index of the numerator of the n-iad which we will launch from
	y: The starting index of the denominator of the n-iad in which we will launch from
	orientation: If true, the overtones will change in the pivot for the first step. If false, the
	undertones will change in the pivot for the first step. This will alternate for every step.
		niad: The number of contigious ratios to return in the chord
		@*/
		^Routine({
			var x, y, orient, arr;

			x = startX;
			y = startY;
			orient = orientation;

			{true}.while({
				arr = this.d2Pivot (x, y, orient);

				x = arr[0];
				y = arr[1];
				//"% / %\n".postf(overtones[x], overtones[y]);
				//this.makeIntervals(x, y, orient, niad).postln;
				this.makeIntervals(x, y, orient, niad).yield;

				orient = orient.not;
			})
		})
	}

	walk { arg numerator, denominator, orientation, niad=3;
	/*@
	desc: Start walking based on a start point, describe by numerator and denominator.
	Finding the new pivot point means picking one of the fractions in a triad
	Finding the new start means figuring out whether the new pivot is the top, middle, or bottom
	member of the triad and computing a new start index based on that computation
	numerator: The starting numerator of the triad in which we wish to pivot
	denominator: The starting denominator of the triad in which we wish to pivot
	orientation: If true, the overtones will change in the pivot. If false, the
	undertones will change in the pivot for the first step.
		niad: The number of contigious ratios to return in the chord
		@*/

		^this.d2WalkXY(this.pr_indexOf(numerator), this.pr_indexOf(denominator), orientation, niad);
	}

	pivot { arg numerator, denominator, orientation, niad=3;
	/*@
	desc: Find a pivot based on a ratio, given as the numerator and denominator.
	Finding the new pivot point means picking one of the fractions in a triad
	Finding the new start means figuring out whether the new pivot is the top, middle, or bottom
	member of the triad and computing a new start index based on that computation
	numerator: The starting numerator of the triad in which we wish to pivot
	denominator: The starting denominator of the triad in which we wish to pivot
	orientation: If true, the overtones will change in the pivot. If false, the
	undertones will change in the pivot.
		niad: The number of contigious ratios to return in the chord
	@*/

		var x, y, arr;
		x = this.pr_indexOf(numerator);
		y = this.pr_indexOf(denominator);
		arr = this.d2Pivot(x, y, orientation);
		x = arr[0];
		y = arr[1];
		^[makeIntervals(x, y, orientation, niad), [overtones.wrapAt(x), overtones.wrapAt(y)]]
	}

	pivotxy { arg x,y, orientation, niad=3;
	/*@
	desc: Find a pivot based on a start point, describe by o[x]/o[y].
	Finding the new pivot point means picking one of the fractions in a triad
	Finding the new start means figuring out whether the new pivot is the top, middle, or bottom
	member of the triad and computing a new start index based on that computation
	x: The starting index of the numerator of the triad in which we wish to pivot
	y: The starting index of the denominator of the triad in which we wish to pivot
	orientation: If true, the overtones will change in the pivot. If false, the
	undertones will change in the pivot.
		niad: The number of contigious ratios to return in the chord
	@*/

		var arr;
		arr = this.d2Pivot(x, y, orientation);
		x = arr[0];
		y = arr[1];
		^[this.makeIntervals(x, y, orientation, niad), [x % overtones.size, y % overtones.size]]
	}

	pr_indexOf{ arg digit;

		/*@
		desc: Adjusts the 'octave' of the digit, by dividing by the base
		@*/
		var mod, reduced;


		mod = digit % base;
		if (mod ==0, {
			reduced = (digit / base).asInteger;
			//reduced.postln;
			if (reduced == 1,
				{
					digit = base;
				} , {
					if ( reduced >= base, {
						digit = reduced;
						//digit.postln;
		})})});

		if (digit < base, { digit = base});

		^overtones.indexOf(digit);
	}
}

Lattice : Diamond {

	* new { arg otones = nil, base = 2;

		DeprecatedError(this, "Lattice", "Diamond", Diamond).throw

		^super.new.init(otones, base);
	}
}