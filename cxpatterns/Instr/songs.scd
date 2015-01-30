

Instr("songs.jazztrio", { arg chordChanges, headNotes, headDurations, instr1, instr2, instr3, numChoruses = 2, swing = 0.57,
		jamp=0.3, mcamp=0.3, bamp=0.3, speedFactor = 0.25;

	var makeHead, rest;

	var d, d2, d3, makeSolo, makeBass, lastNote, beat;

	d2 = Instr("degreeStream.walker");

	// more walking, roots
	d3 = Instr("degreeStream.walker2");

	makeHead = { arg instr, count=1, amp=1.0;
		Pbind(
			\note, Pseq(headNotes, count),
			// \octave, 3,
			\dur, Pseq(headDurations, count),
			\amp, amp.poll,
			\instrument, instr
			);
	};

	makeSolo = { arg instr, count=1, amp=1.0;
		var n=4;
		amp = amp.poll;
		e =  Pbind(
				\degree , d2.value,
				\octave, 4,
				\amp, Pbrown(amp * 0.8, amp * 1.3, amp * 0.1, inf),
		 		\scale, Pn(Pseq(chordChanges, count), n),
				\dur , Pseq([swing.poll, 1 - swing.poll] * speedFactor.poll, inf),
				\instrument, instr
			);
	};
	makeBass = { arg count=1, amp=1.0;
		var n=2;
		e =  Pbind(
				\degree , d3.value,
				\octave, 3,
				\amp, amp.poll,
		 		\scale, Pn(Pseq(chordChanges, count), n),
				\dur , speedFactor.poll,
				\instrument, instr3
			);
	};
	rest = { arg count=1;
		Pbind(
			\freq, \rest,
			\dur, Pseq([chordChanges.size * 2 * count * speedFactor.poll, nil], 1)
		)
	};
	lastNote = { arg instr, amp=1.0;
		Pbind(
			\note, headNotes.first,
			\dur, Pseq([speedFactor.poll * 4, nil], 1),
			\amp, amp.poll,
			\instrument, instr1
			);
	};

	Pseq([
		// head
		Ppar([ makeBass.value(1, bamp),
			makeHead.value(instr1, 1, jamp),
			makeHead.value(instr2, 1, mcamp)
			]),
		// john
		Ppar([ makeBass.value(1, bamp),
			makeSolo.value(instr1, 1, jamp)
			]),
		// mc coy
		Ppar([ makeBass.value(1, bamp),
			makeSolo.value(instr2, 1, mcamp)
			]),
		// head
		Ppar([ makeBass.value(1, bamp),
			makeHead.value(instr1, 1, jamp),
			makeHead.value(instr2, 1, mcamp)
			]),
		// bam
		Ppar([ lastNote.value(instr1, jamp),
				lastNote.value(instr2, mcamp),
				lastNote.value(instr3, bamp)
			])
	], 2);

	//makeBass.value(1, bamp)

}, [
	\chordChanges,
	ArraySpec(\note.asSpec.as(StaticSpec), 16),
	ArraySpec(\dur.asSpec.as(StaticSpec), 16),
	InstrNameSpec(\audioEvent, true, false),
	InstrNameSpec(\audioEvent, true, false),
	InstrNameSpec(\audioEvent, true, false),
	StaticIntegerSpec(1, 8, 3),
	StaticSpec(0.1, 0.9, \lin, 0.0, 0.57),
	StaticSpec(0.1, 0.9, \lin, 0.0),
	StaticSpec(0.1, 0.9, \lin, 0.0),
	StaticSpec(0.1, 0.9, \lin, 0.0),
	StaticSpec(0.25, 4.0, \lin, 0.0) // speed factor, could be a stream

], EventStreamSpec.new);


