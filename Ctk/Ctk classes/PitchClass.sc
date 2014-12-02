/* For GUIDO objects - no quartertones yet */

PitchClass {
	var <note, <acc, <octave, <pitch, <pitchclass, <keynum, <freq, <>alter, <alt1, <alt2;
	classvar notenames, notenums, noteToScale, scaleToNote, accToSize, sizeToAcc, accToGuido,
		majScale, qualities, qualIdx;
	// deal with transposition, notenames and PC classes here
	// note and acc are symbols, octave is an integer, where middle c = c4

	*new {arg pitch, octave = 4, alter = 0;
		^super.new.initPC(pitch, octave, alter);
		}


	initPC {arg argpitch, argoctave, argalter;
		alter = argalter;
		this.calcpitch(argpitch, argoctave);
		}

	calcpitch {arg thispitch, thisoctave;
		var str, pitchnum;
		octave = thisoctave ?? {octave};
		thispitch.isKindOf(Number).if({
			octave = (thispitch.round*0.0833333333).floor - 1;
			pitchnum = thispitch % 12;
			(pitchnum == -0).if({pitchnum = 0});
			(pitchnum == 0).if({(thispitch != 0).if({octave = octave + 1})});
 			pitch = notenums[pitchnum];
			}, {
			pitch = thispitch;
			});
		str = pitch.asString;
		(str.size > 2).if({
			case
				{str[str.size-2..str.size-1].asSymbol == \qs} {
					alter = 0.5;
					alt1 = "\\alter<+"++alter++">(";
					alt2 = ")";
					}
				{str[str.size-2..str.size-1].asSymbol == \qf} {
					alter = -0.5;
					alt1 = "\\alter<-"++alter++">(";
					alt2 = ")";
					}
				{true} {
					alt1 = alt2 = ""
					}
				}, {
				alt1 = alt2 = ""
				});
		note = str[0].asSymbol;
		if(str.size > 1,
			{acc = str[1..str.size-1].asSymbol},
			{pitch = (str ++ "n").asSymbol; acc = \n}
			);
		pitch = pitch.asSymbol;
		pitchclass = notenames[pitch];
		(pitchclass >= 0).if({
			keynum = pitchclass + (12 * (1 + octave));
			freq = keynum.midicps;
			}, {
			keynum = 0;
			freq = 0;
			});
		}

	pitch_ {arg newpitch, newoctave;
		this.calcpitch(newpitch, newoctave);
		}

	guidoString {
		var oct, gacc;
		oct = (octave - 3).asInteger;
		gacc = accToGuido[acc];
		(note == \r).if({
			note = "_";
			oct = "";
			});
		^note.asString++gacc++oct;
		}

	lilyString {
		var oct, octString, lacc;
		oct = octave - 3;
		octString = "";
		lacc = (acc != \n).if({acc.asString}, {""});
		case
			{note == \r} {nil}
			{oct > 0} {oct.do({octString = octString ++ "'"})}
			{oct < 0} {oct.abs.do({octString = octString ++ ","})};
		^note.asString++lacc++octString;
		}

	// can be a PitchClass or float that represents a keynum (quartertones are okay this way)
	invert {arg center;
		var change;
		center = center.isKindOf(PitchClass).if({
			center.keynum
			}, {
			center ?? {60}
			});
		change = this.keynum - center * 2;
		^this.class.new(this.keynum - change)
		}

	mapIntoRange {arg range = 6, target, numturns = 0, maxturns = 10, origval;
		var newtestval, targetkey, rangesteps;
		origval = origval ?? {this};
		targetkey = target.keynum;
		rangesteps = range.halfsteps;
		(numturns < maxturns).if({
			((keynum - targetkey).abs > rangesteps).if({
				newtestval = (keynum > targetkey).if({
					PC(pitch, octave - 1, alter)
				}, {
					PC(pitch, octave + 1, alter)
				});
				^newtestval.mapIntoRange(range, target, numturns + 1, maxturns, origval);
			}, {
			^this
			});
		}, {
		"Your PitchClass couldn't be wrapped into the desired range. Check your parameters or increase maxturns.".warn;
		^origval;
		})
	}

	mapIntoBounds {arg low, high;
		var tmp, dif, lowkey, highkey;
		((lowkey = low.keynum) > (highkey = high.keynum)).if({
			tmp = high;
			high = low;
			low = tmp;
		});
		dif = highkey - lowkey * 0.5;
		^this.mapIntoRange(dif, lowkey + dif);
	}

	/*
	a = PC(\fs, 8).mapIntoRange(6, PC(\c, 4));
	a.octave;
	a.pitch;

	a = PC(\fs, 8).mapIntoBounds(PC(\cdfs, 3), PC(\c, 4));
	a.octave;
	a.pitch;
	\fs4.mapIntoRange
	*/

//	// direction should be \up or \down - aPitchInterval can be an instance of PitchInterval
	// OR an + or - integer (direction can be exculded in this case
	transpose {arg aPitchInterval, direction = \up;
		var startscale, endnote, numnotes, newscale, newoctave, newacc, size, sizeAlter;
		var intervalSize, modIntervalSize, intervalQuality, dir;
		dir = case
			{direction == \up}{1}
			{direction == \down}{-1}
			// if neither, set direction to up and return 1
			{true}{direction = \up; 1};
		aPitchInterval.isKindOf(PitchInterval).if({
			intervalSize = aPitchInterval.size;
			modIntervalSize = aPitchInterval.mod;
			intervalQuality = aPitchInterval.quality;
			startscale = noteToScale[note];
			numnotes = intervalSize - 1 * dir;
			newscale = (startscale + numnotes);
			newoctave = (newscale / 7).floor + octave.asFloat;
			endnote = scaleToNote[newscale % 7];
			// distance from the 'natural' note
			size = accToSize[acc];

			// need to work in exceptions for scales!
			sizeAlter =
				case
					{((modIntervalSize == 1) && (intervalQuality == \perf))}
						{0 * dir}
					{((modIntervalSize == 1) && (intervalQuality == \dim))}
						{-1 * dir}
					{((modIntervalSize == 1) && (intervalQuality == \aug))}
						{1 * dir}
					{((modIntervalSize == 2) && (intervalQuality == \dim))}
						{-2 * dir}
					{((modIntervalSize == 2) && (intervalQuality == \minor))}
						{(direction == \up).if({
							case
								{((note == \b) || (note == \e))} {0}
								{true} {-1};
							}, {
							case
								{((note == \c) || (note == \f))} {0}
								{true} {1};
							}
						)}
					{((modIntervalSize == 2) && (intervalQuality == \major))}
						{(direction == \up).if({
							case
								{((note == \b) || (note == \e))} {1}
								{true} {0};
							}, {
							case
								{((note == \c) || (note == \f))} {-1}
								{true} {0};
							}
						)}
					{((modIntervalSize == 2) && (intervalQuality == \aug))}
						{(direction == \up).if({
							case
								{((note == \b) || (note == \e))} {2}
								{true} {1};
							}, {
							case
								{((note == \c) || (note == \f))} {-2}
								{true} {-1};
							}
						)}
					{((modIntervalSize == 3) && (intervalQuality == \dim))}
						{(direction == \up).if({
							case
								{((note == \c) || (note == \f) || (note == \g))} {-2}
								{true} {-1};
							}, {
							case
								{((note == \a) || (note == \b) || (note == \e))} {2}
								{true} {1};
							}
						)}
					{((modIntervalSize == 3) && (intervalQuality == \minor))}
						{(direction == \up).if({
							case
								{((note == \c) || (note == \f) || (note == \g))} {-1}
								{true} {0};
							}, {
							case
								{((note == \a) || (note == \b) || (note == \e))} {1}
								{true} {0};
							}
						)}
					{((modIntervalSize == 3) && (intervalQuality == \major))}
						{(direction == \up).if({
							case
								{((note == \c) || (note == \f) || (note == \g))} {0}
								{true} {1};
							}, {
							case
								{((note == \a) || (note == \b) || (note == \e))} {0}
								{true} {-1};
							}
						)}
					{((modIntervalSize == 3) && (intervalQuality == \aug))}
						{(direction == \up).if({
							case
								{((note == \c) || (note == \f) || (note == \g))} {1}
								{true} {2};
							}, {
							case
								{((note == \a) || (note == \b) || (note == \e))} {-1}
								{true} {-2};
							}
						)}
					{((modIntervalSize == 4) && (intervalQuality == \dim))}
						{(direction == \up).if({
							case
								{(note == \f)} {-2}
								{true} {-1};
							}, {
							case
								{(note == \b)} {2}
								{true} {1};
							}
						)}
					{((modIntervalSize == 4) && (intervalQuality == \perf))}
						{(direction == \up).if({
							case
								{(note == \f)} {-1}
								{true} {0};
							}, {
							case
								{(note == \b)} {1}
								{true} {0};
							}
						)}
					{((modIntervalSize == 4) && (intervalQuality == \aug))}
						{(direction == \up).if({
							case
								{(note == \f)} {-2}
								{true} {-1};
							}, {
							case
								{(note == \b)} {2}
								{true} {1};
							}
						)}
					{((modIntervalSize == 5) && (intervalQuality == \dim))}
						{(direction == \up).if({
							case
								{(note == \b)} {0}
								{true} {-1};
							}, {
							case
								{(note == \f)} {0}
								{true} {1};
							}
						)}
					{((modIntervalSize == 5) && (intervalQuality == \perf))}
						{(direction == \up).if({
							case
								{(note == \b)} {1}
								{true} {0};
							}, {
							case
								{(note == \f)} {-1}
								{true} {0};
							}
						)}
					{((modIntervalSize == 5) && (intervalQuality == \aug))}
						{(direction == \up).if({
							case
								{(note == \b)} {2}
								{true} {1};
							}, {
							case
								{(note == \f)} {-2}
								{true} {-1};
							}
						)}
					{((modIntervalSize == 6) && (intervalQuality == \dim))}
						{(direction == \up).if({
							case
								{((note == \e) || (note == \a) || (note == \b))} {-1}
								{true} {-2};
							}, {
							case
								{((note == \c) || (note == \f) || (note == \g))} {1}
								{true} {2};
							}
						)}
					{((modIntervalSize == 6) && (intervalQuality == \minor))}
						{(direction == \up).if({
							case
								{((note == \e) || (note == \a) || (note == \b))} {0}
								{true} {-1};
							}, {
							case
								{((note == \c) || (note == \f) || (note == \g))} {0}
								{true} {1};
							}
						)}
					{((modIntervalSize == 6) && (intervalQuality == \major))}
						{(direction == \up).if({
							case
								{((note == \e) || (note == \a) || (note == \b))} {1}
								{true} {0};
							}, {
							case
								{((note == \c) || (note == \f) || (note == \g))} {-1}
								{true} {0};
							}
						)}
					{((modIntervalSize == 6) && (intervalQuality == \aug))}
						{(direction == \up).if({
							case
								{((note == \e) || (note == \a) || (note == \b))} {2}
								{true} {1};
							}, {
							case
								{((note == \c) || (note == \f) || (note == \g))} {-2}
								{true} {-1};
							}
						)}
					{((modIntervalSize == 7) && (intervalQuality == \dim))}
						{(direction == \up).if({
							case
								{((note == \c) || (note == \f))} {-2}
								{true} {-1};
							}, {
							case
								{((note == \b) || (note == \e))} {2}
								{true} {1};
							}
						)}
					{((modIntervalSize == 7) && (intervalQuality == \minor))}
						{(direction == \up).if({
							case
								{((note == \c) || (note == \f))} {-1}
								{true} {0};
							}, {
							case
								{((note == \b) || (note == \e))} {1}
								{true} {0};
							}
						)}
					{((modIntervalSize == 7) && (intervalQuality == \major))}
						{(direction == \up).if({
							case
								{((note == \c) || (note == \f))} {0}
								{true} {1};
							}, {
							case
								{((note == \b) || (note == \e))} {0}
								{true} {-1};
							}
						)}
					{((modIntervalSize == 7) && (intervalQuality == \aug))}
						{(direction == \up).if({
							case
								{((note == \c) || (note == \f))} {1}
								{true} {2};
							}, {
							case
								{((note == \b) || (note == \e))} {-1}
								{true} {-2};
							}
						)}
					{true} {0};
			newacc = sizeToAcc[size + sizeAlter];
			^this.class.new((endnote ++ newacc).asSymbol, newoctave);
			}, {
			^this.class.new(this.keynum + (aPitchInterval * dir))
			})
		}

	distanceFrom {arg aPC;
		var thisNote, thatNote, baseInterval, interval, direction, octaves;
		var halfsteps, idx, quality;
		thisNote = noteToScale[note];
		thatNote = noteToScale[aPC.note];
		(aPC.keynum > keynum).if({
			direction = \up;
			(thatNote < thisNote).if({
				thatNote = thatNote + 7;
				})
			}, {
			direction = \down;
			(thatNote > thisNote).if({
				thatNote = thatNote - 7;
				})
			});
		baseInterval = (thatNote - thisNote);
		halfsteps = (aPC.keynum - keynum).abs;
		interval = baseInterval.abs + 1;
		octaves = (halfsteps * 0.083333333333333).floor;
		idx = (halfsteps % 12) - qualIdx[interval];
		quality = qualities[interval][idx];
		^[PitchInterval(quality, interval + (octaves * 7)), direction];
		}

	modalTranspose {arg steps = 0, fromAPitchCollection, toAPitchCollection;
		var degree = 0, pitchNames, idx = 0, test, size, notes, add, fromPC, toPC;
		var newNote, newPitch, newPC, newAcc, curAcc, degPStep, octAdd, scaleDist;
		var curNote, lastNote;
		octAdd = (steps / 7).floor;
		fromAPitchCollection = fromAPitchCollection ?? {PitchCollection.major(\c)};
		toAPitchCollection = toAPitchCollection ?? {fromAPitchCollection};
		fromPC = fromAPitchCollection.pitchCollection;
		toPC = toAPitchCollection.pitchCollection;
		size = fromPC.size;
		notes = fromPC.collect({arg me; me.note});
		test = false;
		while({
			(this.note == notes[degree]).if({
				test = true;
				add = ((this.keynum - fromPC[degree].keynum) % 12).asInteger;
				degPStep = degree + steps;
				newNote = notes[degPStep % 7];
				newPC = toPC[degPStep % 7];
				newAcc = sizeToAcc[(accToSize[newPC.acc] + add)];
				newPitch = (newNote ++ newAcc).asSymbol;
				}, {
				lastNote = noteToScale[notes[degree + steps % 7]];
				degree = degree + 1;
				curNote = noteToScale[notes[degree + steps % 7]];
				(curNote < lastNote).if	({octAdd = octAdd + 1});
				});
			(test == false and: {idx < size});
			});
		^this.class.new(newPitch, octave + octAdd); //
		}


	// cn = c, cs = c-sharp, df = d-flat, dqf = d-quarter-flat, cqs = c-quarter-sharp
	// dtqf = d-three-quarter-flat, ctqs = c-three-quarter-sharp
	*initClass {
		noteToScale = IdentityDictionary[
			\c -> 0,
			\d -> 1,
			\e -> 2,
			\f -> 3,
			\g -> 4,
			\a -> 5,
			\b -> 6
			];
		scaleToNote = IdentityDictionary[
			0 -> \c,
			1 -> \d,
			2 -> \e,
			3 -> \f,
			4 -> \g,
			5 -> \a,
			6 -> \b
			];
		accToSize = IdentityDictionary[
			\ffff -> -4,
			\fff -> -3,
			\ff -> -2,
			\tqf -> -1.5,
			\f -> -1,
			\qf -> -0.5,
			\n -> 0,
			\qs -> 0.5,
			\s -> 1,
			\tqs -> 1.5,
			\ss -> 2,
			\sss -> 3,
			\ssss -> 4
			];
		sizeToAcc = IdentityDictionary[
			-4 -> \ffff,
			-3 -> \fff,
			-2 -> \ff,
			-1.5 -> \tqf,
			-1 -> \f,
			-0.5 -> \qf,
			0 -> \n,
			0.5 -> \qs,
			1 -> \s,
			1.5 -> \tqs,
			2 -> \ss,
			3 -> \sss,
			4 -> \ssss
			];
		notenames = IdentityDictionary[
			\bs -> 0,
			\cn -> 0,
			\dff -> 0,
			\cqs -> 0.5,
			\dtqf -> 0.5,
			\bss -> 1,
			\cs -> 1,
			\df -> 1,
			\ctqs -> 1.5,
			\dqf -> 1.5,
			\css -> 2,
			\dn -> 2,
			\eff -> 2,
			\dqs -> 2.5,
			\etqf -> 2.5,
			\ds -> 3,
			\ef -> 3,
			\dtqs -> 3.5,
			\eqf -> 3.5,
			\dss -> 4,
			\en -> 4,
			\ff -> 4,
			\eqs -> 4.5,
			\fqf -> 4.5,
			\es -> 5,
			\fn -> 5,
			\gff -> 5,
			\fqs -> 5.5,
			\gtqf -> 5.5,
			\fs -> 6,
			\gf -> 6,
			\ftqs -> 6.5,
			\gqf -> 6.5,
			\fss -> 7,
			\gn -> 7,
			\aff -> 7,
			\gqs -> 7.5,
			\atqf -> 7.5,
			\gs -> 8,
			\af -> 8,
			\gtqs -> 8.5,
			\aqf -> 8.5,
			\gss -> 9,
			\an -> 9,
			\bff -> 9,
			\aqs -> 9.5,
			\btqf -> 9.5,
			\as -> 10,
			\bf -> 10,
			\atqs -> 10.5,
			\bqf -> 10.5,
			\ass -> 11,
			\bn -> 11,
			\cf -> 11,
			\bqs -> 11.5,
			\cqf -> 11.5,
			\rn -> -1
			];
		notenums = Dictionary[
			-1 -> \rn,
			0 -> \cn,
			0.5 -> \cqs, //[\c, \qs],
			1 -> \cs,
			1.5 -> \ctqs, //[\c, \tqs],
			2 -> \dn,
			2.5 -> \dqs, //[\d, \qs],
			3 -> \ef,
			3.5 -> \dtqs, //[\d, \tqs],
			4 -> \en,
			4.5 -> \eqs, //[\e, \qs],
			5 -> \fn,
			5.5 -> \fqs, //[\f, \qs],
			6 -> \fs,
			6.5 -> \ftqs, //[\f, \tqs],
			7 -> \gn,
			7.5 -> \gqs, // [\g, \qs],
			8 -> \af,
			8.5 -> \gtqs, //[\g, \tqs],
			9 -> \an,
			9.5 -> \aqs, // [\a, \qs],
			10 -> \bf,
			10.5 -> \atqs, // [\a, \tqs],
			11 -> \bn,
			11.5 -> \bqs, // [\b, \qs]
			];
		accToGuido = IdentityDictionary[
			\ffff -> "&&&&",
			\fff -> "&&&",
			\ff -> "&&",
			\tqf -> "&", // -1.5, fix later
			\f -> "&",
			\qf -> "", //-0.5,
			\n -> "",
			\qs -> "", // 0.5,
			\s -> "#",
			\tqs -> "#", //1.5,
			\ss -> "##",
			\sss -> "###",
			\ssss -> "####"
			];
		majScale = [0, 2, 4, 5, 7, 9, 11];
		qualities = IdentityDictionary[
			1 -> [\perf, \aug],
			2 -> [\dim, \minor, \major, \aug],
			3 -> [\dim, \minor, \major, \aug],
			4 -> [\dim, \perf, \aug],
			5 -> [\dim, \perf, \aug],
			6 -> [\dim, \minor, \major, \aug],
			7 -> [\dim, \minor, \major, \aug],
			8 -> [\dim, \perf, \aug]
			];

		qualIdx = IdentityDictionary[
			1 -> 0,
			2 -> 0,
			3 -> 2,
			4 -> 4,
			5 -> 6,
			6 -> 7,
			7 -> 9,
			8 -> 11,
			];
		}
}

PitchInterval {
	classvar basesteps;
	var <quality, <size, <mod, <halfsteps;
	//quality is a symbol of \major, \minor, \perf, \dim or \aug
	// size is a
	*new {arg quality, size;
		var mod;
		// make sure size is an int, grab its mod
		mod = (size > 7).if({(size.round % 8) + 1}, {size});
		((mod == 1) || (mod == 4) || (mod == 5)).if({
			((quality ==  \perf) || (quality == \aug) || (quality == \dim)).if({
				^super.newCopyArgs(quality, size, mod).initPitchInterval;
				}, {
				"Unisons, fourths, fifths or octaves need to be \\perf or \\aug".warn;
				^nil;
				})
			}, {
			((mod == 2) || (mod == 3) || (mod == 6) || (mod == 7)).if({
				((quality == \major) || (quality == \minor) ||
					(quality == \dim) || (quality == \aug)).if({
				^super.newCopyArgs(quality, size, mod).initPitchInterval;
				}, {
				"Seconds, thirds, sixths or sevents need to be \\major, \\minor, \\dim or \\aug".warn;
				^nil;
				})
			})
		})
	}

	initPitchInterval {
		halfsteps = basesteps[mod - 1];
		((mod == 1) or: {(mod == 4) or: {mod == 5}}).if({
			case {
				quality == \perf
			} {
				nil
			} {
				quality == \aug
			} {
				halfsteps = halfsteps + 1;
			} {
				quality == \dim
			} {
				halfsteps = halfsteps - 1;
			}
		}, {
			case {
				quality == \minor
			} {
				nil
			} {
				quality == \major
			} {
				halfsteps = halfsteps + 1;
			} {
				quality == \dim
			} {
				halfsteps = halfsteps - 1;
			} {
				quality == \aug
			} {
				halfsteps = halfsteps + 2;
			}
		})

	}

	*initClass {
		basesteps = [0, 1, 3, 5, 7, 8, 10];
		}
}

PitchCollection {
	var <pitchCollection, <tonic, <octaveSize, <isScale, <pitchBase, <sortedBase;

	*new {arg pitchCollection, tonic, octaveSize = 12, isScale = false;
		pitchCollection = pitchCollection.asArray;
		tonic = tonic ? pitchCollection[0];
		^super.newCopyArgs(pitchCollection, tonic, octaveSize, isScale).init;
		}

	init {
		pitchBase = Array.fill(pitchCollection.size, {arg i; pitchCollection[i].pitchclass});
		sortedBase = pitchBase.copy.sort;
		}

	// will make scales and deal with note-names, transposition, string fomratting
	// random filtering

	*major {arg tonic;
		var steps, scale, start;
		start = tonic = tonic.notNil.if({
			tonic.isKindOf(PitchClass).if({tonic}, {PitchClass(tonic)})
			}, {
			PitchClass.new(\c)
			});
		steps = [\major, \major, \minor, \major, \major, \major, \minor];
		steps.do{arg me, i;
			var note;
			note = start;
			start = start.transpose(PitchInterval(me, 2), \up);
			steps[i] = note;
			};
		^this.new(steps, tonic, 12, true)
		}

	*minor {arg tonic;
		var steps, scale, start;
		start = tonic = tonic.notNil.if({
			tonic.isKindOf(PitchClass).if({tonic}, {PitchClass(tonic)})
			}, {
			PitchClass.new(\c)
			});
		steps = [\major, \minor, \major, \major, \minor, \major, \major];
		steps.do{arg me, i;
			var note;
			note = start;
			start = start.transpose(PitchInterval(me, 2), \up);
			steps[i] = note;
			};
		^this.new(steps, tonic, 12, true)
		}

	*natMinor {arg tonic;
		^this.minor(tonic);
		}

	*harmMinor {arg tonic;
		var steps, scale, start;
		start = tonic = tonic.notNil.if({
			tonic.isKindOf(PitchClass).if({tonic}, {PitchClass(tonic)})
			}, {
			PitchClass.new(\c)
			});
		steps = [\major, \minor, \major, \major, \minor, \aug, \minor];
		steps.do{arg me, i;
			var note;
			note = start;
			start = start.transpose(PitchInterval(me, 2), \up);
			steps[i] = note;
			};
		^this.new(steps, tonic, 12, true)
		}

	*ionian {arg tonic;
		^this.major(tonic);
		}

	*dorian {arg tonic;
		var steps, scale, start;
		start = tonic = tonic.notNil.if({
			tonic.isKindOf(PitchClass).if({tonic}, {PitchClass(tonic)})
			}, {
			PitchClass.new(\c)
			});
		steps = [\major, \minor, \major, \major, \major, \minor, \major];
		steps.do{arg me, i;
			var note;
			note = start;
			start = start.transpose(PitchInterval(me, 2), \up);
			steps[i] = note;
			};
		^this.new(steps, tonic, 12, true)
		}

	*phrygian {arg tonic;
		var steps, scale, start;
		start = tonic = tonic.notNil.if({
			tonic.isKindOf(PitchClass).if({tonic}, {PitchClass(tonic)})
			}, {
			PitchClass.new(\c)
			});
		steps = [\minor, \major, \major, \major, \minor, \major, \major];
		steps.do{arg me, i;
			var note;
			note = start;
			start = start.transpose(PitchInterval(me, 2), \up);
			steps[i] = note;
			};
		^this.new(steps, tonic, 12, true)
		}

	*lydian {arg tonic;
		var steps, scale, start;
		start = tonic = tonic.notNil.if({
			tonic.isKindOf(PitchClass).if({tonic}, {PitchClass(tonic)})
			}, {
			PitchClass.new(\c)
			});
		steps = [\major, \major, \major, \minor, \major, \major, \minor];
		steps.do{arg me, i;
			var note;
			note = start;
			start = start.transpose(PitchInterval(me, 2), \up);
			steps[i] = note;
			};
		^this.new(steps, tonic, 12, true)
		}

	*mixolydian {arg tonic;
		var steps, scale, start;
		start = tonic = tonic.notNil.if({
			tonic.isKindOf(PitchClass).if({tonic}, {PitchClass(tonic)})
			}, {
			PitchClass.new(\c)
			});
		steps = [\major, \major, \minor, \major, \major, \minor, \major];
		steps.do{arg me, i;
			var note;
			note = start;
			start = start.transpose(PitchInterval(me, 2), \up);
			steps[i] = note;
			};
		^this.new(steps, tonic, 12, true)
		}

	*aeolian {arg tonic;
		^this.minor(tonic);
		}

	*locrian {arg tonic;
		var steps, scale, start;
		start = tonic = tonic.notNil.if({
			tonic.isKindOf(PitchClass).if({tonic}, {PitchClass(tonic)})
			}, {
			PitchClass.new(\c)
			});
		steps = [\minor, \major, \major, \minor, \major, \major, \major];
		steps.do{arg me, i;
			var note;
			note = start;
			start = start.transpose(PitchInterval(me, 2), \up);
			steps[i] = note;
			};
		^this.new(steps, tonic, 12, true)
		}

	*chromatic {arg tonic;
		var start, steps, step, newoctave;
		tonic = tonic.notNil.if({
			tonic.isKindOf(PitchClass).if({tonic}, {PitchClass(tonic)})
			}, {
			PitchClass.new(\c)
			});
		start = tonic.pitchclass;
		steps = Array.fill(12, {arg i;
			step = (i+start)%12;
			newoctave = tonic.octave + ((i+start) / 12).floor;
			PitchClass.new(step, octave: newoctave);
			});
		^this.new(steps, tonic, 12, true);
		}

	// takes a keynum, returns the PitchClass closest to that keynum (under octave equivalence)
	filterKeynum {arg keynum;
		var baseKeynum, closestKey, member, thisSortedBase, thisIndex, tmp;
		// round out the octaves
		thisSortedBase = sortedBase ++ (sortedBase[0] + octaveSize);
		// get the mod for searching purposes
		baseKeynum = keynum % 12;
		// find the closes keynum's index in the sorted collection
		closestKey = (thisSortedBase).indexIn(baseKeynum);
		// find that closest keynums position in order of the collection
		thisIndex = pitchBase.indexOf(thisSortedBase[closestKey]%12);
		// figure out which note it is
		member = pitchCollection[thisIndex];
		// return a new PitchClass with the appropriate octave... check for nums close
		// to the next octave at C with the round
		tmp = PitchClass.new(keynum + (((member.keynum % 12) - (keynum % 12)) % 12));
		^PitchClass.new((member.note ++ member.acc), tmp.octave); //(keynum.round / 12).floor - 1);
		}

	at {arg idx;
		^pitchCollection[idx]
		}

	copySeries {arg first, second, last;
		^this.class.new(pitchCollection.copySeries(first, second, last), tonic, octaveSize)
		}

	do {arg func;
		pitchCollection.do({arg me, i;
			func.value(me, i);
			})
		}
	choose {
		^pitchCollection.choose;
		}

	wchoose {arg weights;
		^pitchCollection.wchoose(weights);
		}

	chunk {arg start = 0, end;
		end = end ?? pitchCollection.size - 1
		^this.class.new(pitchCollection[start..end], tonic, 12);
		}

	add {arg aPitchClass;
		^this.class.new(pitchCollection ++ aPitchClass, tonic, 12)
		}

	invert {arg aPitchClass;
		^this.class.new(pitchCollection.collect({arg me; me.invert(aPitchClass)}));
		}

	insert {arg position = 0, aPitchClass;
		^this.class.new(pitchCollection.insert(position, aPitchClass).flat, tonic, 12)
		}

	transpose {arg aPitchInterval, direction = \up;
		^this.class.new(Array.fill(pitchCollection.size, {arg i;
				pitchCollection[i].transpose(aPitchInterval, direction)}),
			tonic.transpose(aPitchInterval, direction), octaveSize)
		}

	modalTranspose {arg steps, fromAPitchCollection, toAPitchCollection;
		^this.class.new(Array.fill(pitchCollection.size, {arg i;
				pitchCollection[i].modalTranspose(steps, fromAPitchCollection, toAPitchCollection)}),
			tonic.modalTranspose(steps, fromAPitchCollection, toAPitchCollection))
		}

//	modalTranspose {arg steps = 0, aPitchCollection, direction = \up;
//		var newCollection, newTonic, baseInterval;
//		newCollection = Array.fill(pitchCollection.size,
//				{arg i; pitchCollection[i].transpose(aPitchInterval, direction)});
//		newTonic = tonic.transpose(aPitchInterval, direction);
//		^this.class.new(newCollection, newTonic, octaveSize)
//		}


	/*
	*quartertone {arg tonic;
		var start, steps, step, newoctave;
		start = tonic.pitchclass;
		steps = Array.fill(24, {arg i;
			step = ((i * 0.5)+start)%12;
			newoctave = tonic.octave + (((i*0.5)+start) / 12).floor;
			PitchClass.new(step, octave: newoctave);
			});
		^this.new(steps, tonic, 12);
		}
	*/

/*
	*octatonic {arg tonic = 0; ^this.new([0, 2, 3, 5, 6, 8, 9, 11], tonic)}
	*octatonic013 {arg tonic = 0; ^this.new([0, 1, 3, 4, 6, 7, 9, 10], tonic)}
	*octatonic023 {arg tonic = 0; ^this.octatonic(tonic)}

*/

}

PC : PitchClass { }
PI : PitchInterval { }
PColl : PitchCollection { }

+ String {
	pc {
		^this.calcPC
		}

	note {
		^this
		}

	keynum {
		^this.calcPC.keynum;
		}

	hertz {
		^this.calcPC.freq;
		}

	pitchClass {
		^this.calcPC.keynum % 12.0
		}

	noteName {
		^this.calcPC.note.asString;
		}

	noteAccidental {
		^this.calcPC.acc.asString;
		}

	noteOctave {
		^this.calcPC.octave
		}

	calcPC {
		var noteName, octave, idx;
		this.do({arg char, i;
			char.isDecDigit.if({
				idx = i;
				octave = char.digit
				});
			});
		idx.isNil.if({
			octave = 4;
			idx = this.size;
			});
		^PC(this[0..(idx-1)].toLower.asSymbol, octave);
		}

	}

+ Symbol {
	note {^this}
	keynum {^this.asString.keynum}
	hertz {^this.asString.hertz}
	pitchClass {^this.asString.pitchClass}
	noteName {^this.asString.noteName.asSymbol}
	noteAccidental {^this.asString.noteAccidental.asSymbol}
	noteOctave {^this.asString.noteOctave}
	pc {^this.asString.calcPC}
	}

+ SimpleNumber {
	pc {arg hertz = false, round = 1.0; ^this.calcPC(hertz, round)}
	note {arg hertz = false, round = 1.0; var pc; pc = this.calcPC(hertz, round);
		^(pc.pitch ++ pc.octave)}
	keynum {arg hertz = false, round = 1.0; ^this.calcPC(hertz, round).keynum}
	hertz {arg hertz = false, round = 1.0; ^this.calcPC(hertz, round).freq}
	pitchClass {arg hertz = false, round = 1.0; ^this.calcPC(hertz, round) % 12.0}
	noteName {arg hertz = false, round = 1.0; ^this.calcPC(hertz, round).note}
	noteAccidental {arg hertz = false, round = 1.0; ^this.calcPC(hertz, round).acc}
	noteOctave {arg hertz = false, round = 1.0; ^this.calcPC(hertz, round).octave}
	halfsteps {^this}
	calcPC {arg hertz = false, round = 1.0;
		var tmp;
		tmp = hertz.if({
			this.cpsmidi
			}, {
			this
			});
		^PC(tmp.round(round))}
	}

+ SequenceableCollection {
	pc {arg hertz = false, round = 1.0; ^this.collect({arg me; me.pc(hertz, round)})}
	note {arg hertz = false, round = 1.0; ^this.collect({arg me; me.note(hertz, round)})}
	keynum {arg hertz = false, round = 1.0; ^this.collect({arg me; me.keynum(hertz, round)})}
	hertz {arg hertz = false, round = 1.0; ^this.collect({arg me; me.hertz(hertz, round)})}
	pitchClass {arg hertz = false, round = 1.0; ^this.collect({arg me;
		me.pitchClass(hertz, round)})}
	noteName {arg hertz = false, round = 1.0; ^this.collect({arg me; me.noteName(hertz, round)})}
	noteAccidental {arg hertz = false, round = 1.0;
		^this.collect({arg me; me.noteAccidental(hertz, round)})}
	noteOctave {arg hertz = false, round = 1.0; ^this.collect({arg me;
		me.noteOctave(hertz, round)})}
	}