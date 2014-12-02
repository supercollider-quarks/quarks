ScalaTunings {
	*initClass {
		var archiveFolder;

		Class.initClassTree(Tuning);

		archiveFolder = PathName(ScalaTunings.filenameSymbol.asString).pathOnly +/+ "archive";

		PathName(archiveFolder).files.do {|i|
			File.use(i.fullPath, "r", {|f|
				var name = i.fileNameWithoutExtension;
				var tuning = Tuning.fromScala( f.readAllString );
				Tuning.all.put( name.asSymbol, tuning )
			})
		}
	}
}

+Tuning {
	*fromScala { |aString|
		var lines, name, size, pitches, octaveRatio;

		lines = aString.split($\n);
		lines = lines.collect( _.stripWhiteSpace );
		lines = lines.reject( _.beginsWith("!") );
		lines = lines.reject { |i| i.size == 0 };

		name = lines[0];
		size = lines[1].asInteger;
		pitches = lines[2..];

		pitches = pitches.collect {|line|
			case
			{ line.contains(".") } {
				(line.asFloat * 0.01)
			}

			{ line.contains("/") } {
				var parts = line.split($/);
				var ratio = parts[0].asFloat / parts[1].asFloat;
				ratio.ratiomidi
			}

			{ true } {
				line.asFloat
			}
		};

		if (pitches.size != size) {
			Error("scl: size mismatch (% != %)".format(pitches.size, size)).throw
		};

		octaveRatio = pitches.last.midiratio;
		if (octaveRatio == 2.ratiomidi.midiratio) { octaveRatio = 2 }; // prevent floating point weirdness

		pitches = [0] ++ pitches.drop(-1);
		^Tuning(pitches, octaveRatio, name)
	}
}
