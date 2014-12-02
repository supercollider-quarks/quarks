

GenStream : Stream 
{
	// a stream wrapper for the Gen class
	var <gen, prResetGen, firstTime=true;
	*new { arg gen;
		^super.newCopyArgs(gen, gen)
	}
	reset {
		gen = prResetGen;
		firstTime = true;
	}
	next {
		if (firstTime) { firstTime = false; }
		{
			gen = gen.genNext;
		}
		^gen.genCurrent
	}
	gen_ { arg inGen;
		gen = prResetGen = inGen;
		firstTime = true;
	}
}
