ChordtrisEngine
{
	// pointer to the Chordtris instance
	var chordtris;
	
	// Matrix of cells, instance of ChordtrisMatrix
	var matrix;
	
	// Task to update the matrix
	var updateTask;
	
	// the current chord which must be played
	var currentBrick;
	
	// the next brick to come
	var nextBrick;
	
	// already played notes for this chord
	var playedNotes;
	
	// initial time interval in seconds between brick updates
	var startInterval = 2;
	
	// time interval between brick updates
	var updateInterval;
	
	// time delta in which subsequent MIDI notes are considered as a chord
	var chordTimeDelta = 0.05;
	
	// Task which is used as a chord collection gate
	var chordCollectionTask;
	
	// Set to collect MIDI notes which are played
	var collectionNoteSet;
	
	// last highest note of correct chord
	var lastHighestChordNote;
	
	// last degree of the current scale that was played
	var lastSingleNoteDegree;
	
	// boolean indicating whether the game is over
	var gameOver = false;
	
	// matrix for the preview of the next brick
	var previewMatrix;
	
	// size of the matrix squares
	var squareSize;
	
	// score manager
	var scoreManager;
	
	// Preferences settings (Dictionary)
	var preferences;
	
	// module for playing sounds
	var soundModule;
	
	// boolean indicating whether the game is paused
	var isPaused = false;
	
	// MIDIdef to respond to MIDI input
	var midiDef;
	
	
	*new { |chordtris, numRows, numColumns, squareSize| ^super.new.init(chordtris, numRows, numColumns, squareSize) }
	
	init { |chordTrisArg, numRows, numColumns, squareSizeArg|
		chordtris = chordTrisArg;
		squareSize = squareSizeArg;
		this.initShutdown;
		this.loadPreferences;
		this.initPreview;
		this.initScoring;
		this.initSoundModule;
		this.initMidi;
		this.initBrickGeneration(numRows, numColumns, squareSize);
		this.initChordDetection;
		this.resetMenu;
		this.resume(false);
	}
	
	loadPreferences {
		preferences = ChordtrisPreferences.getPreferences;
		chordTimeDelta = preferences.at(\chordDetectionTime).value;
	}
	
	initSoundModule {
		soundModule = ChordtrisSoundModule.new;
		soundModule.startMusic;
	}
	
	initChordDetection {
		collectionNoteSet = Set.new;
		chordCollectionTask = Task({
			chordTimeDelta.wait;
			this.chordCollectionFinished;
		});
	}
	
	// intitializes MIDI input
	initMidi {
		Server.default.latency = nil;
		MIDIIn.connectAll;
		midiDef = MIDIdef.noteOn(\midiHandler, { |velocity, note|
			this.handleNote(note, velocity);
		});
	}
	
	initBrickGeneration { |numRows, numColumns, squareSize|
		// init brick matrix
		matrix = ChordtrisMatrix(numRows, numColumns, squareSize);
		
		playedNotes = Set.new;
		nextBrick = this.generateNewBrick;
		this.brickHitTheGround;
		
		updateInterval = startInterval;
		
		// Task to make the currentBrick go down
		updateTask = Task({
			inf.do {
				if(currentBrick.notNil && currentBrick.goDown(matrix).not) {
					this.brickHitTheGround;
				};
				updateInterval.wait;
			}
		});
	}
	
	initScoring {
		scoreManager = ChordtrisScoreManager.new;
	}
	
	initShutdown {
		chordtris.window.onClose_{
			updateTask.stop;
			soundModule.stopMusic;
			chordtris.setPauseMenuItemEnabled(false);
			chordtris.setResumeMenuItemEnabled(false);
			chordtris.unregisterInstance;
		};
	}
	
	resetMenu {
		chordtris.setPauseMenuItemEnabled(true);
		chordtris.setResumeMenuItemEnabled(false);
	}
	
	
	reset {
		matrix.clear;
		previewMatrix.clear;
		scoreManager.reset;
		playedNotes = Set.new;
		nextBrick = this.generateNewBrick;
		this.brickHitTheGround;
		updateInterval = startInterval;
		gameOver = false;
		isPaused = false;
		updateTask.resume;
		this.initSoundModule;
		this.resetMenu;
	}
	
	
	initPreview {
		previewMatrix = ChordtrisMatrix(5, 5, squareSize);
		
		// function which is called if the next brick view is refresehed
		chordtris.nextBrickView.drawFunc = {
			var chordNameX, chordNameY;
			
			Pen.color_(Color.white);
			Pen.stringAtPoint("Next", Point(60, 10));
			previewMatrix.draw(squareSize, squareSize);
			
			// draw the chord name on the brick
			if(nextBrick.brickType == \j or: { nextBrick.brickType == \t}) { Pen.color = Color.black } { Pen.color = Color.white };
			chordNameX = ((nextBrick.x) * squareSize) - squareSize;
			chordNameY = ((nextBrick.y) * squareSize) + (3*squareSize);
			Pen.font_(Font("Courier", 22));
			Pen.stringAtPoint(nextBrick.chord.asString, Point(chordNameX, chordNameY));
		};

	}
	
	pause {
		updateTask.pause;
		isPaused = true;
		chordtris.setPauseMenuItemEnabled(false);
		chordtris.setResumeMenuItemEnabled(true);
		soundModule.pauseMusic;
		soundModule.playPauseSound;
	}
	
	resume { |playSound = true|
		updateTask.resume;
		isPaused = false;
		chordtris.setPauseMenuItemEnabled(true);
		chordtris.setResumeMenuItemEnabled(false);
		if(playSound) { soundModule.playPauseSound; };
		soundModule.resumeMusic;
	}
	
	brickHitTheGround {
		var completeLines;
		
		// check if there are complete lines
		completeLines = matrix.checkLines;
		
		fork {
			completeLines.do { soundModule.playExplosion; 0.1.wait };
		};
		
		// update the score
		scoreManager.linesCompleted(completeLines);
		
		// update the score GUI elements
		{
		chordtris.levelBox.string_(scoreManager.level);
		chordtris.lineBox.string_(scoreManager.lines);
		chordtris.scoreBox.string_(scoreManager.score);
		}.defer;
		
		//{ Chordtris.scoreView.refresh }.defer;
		
		// generate a new brick
		currentBrick = nextBrick;
		
		// delete the next brick from the preview matrix
		nextBrick.y = nextBrick.y + 2;
		nextBrick.x = nextBrick.x - 2;
		previewMatrix.deleteBrickCells(nextBrick);
		nextBrick.y = nextBrick.y - 2;
		nextBrick.x = nextBrick.x + 2;
		
		nextBrick = this.generateNewBrick;
		
		// draw the brick further down and further left in the preview
		nextBrick.y = nextBrick.y + 2;
		nextBrick.x = nextBrick.x - 2;
		previewMatrix.colorBrickCells(nextBrick);
		nextBrick.y = nextBrick.y - 2;
		nextBrick.x = nextBrick.x + 2;
		
		{ chordtris.nextBrickView.refresh }.defer;
		
		// reset last correctly played chord note
		lastHighestChordNote = nil;
		// reset last single note
		lastSingleNoteDegree = nil;
		
		// if the brick does not fit in the matrix, we reached the top and the game is over
		if(currentBrick.fitsInMatrix(matrix).not)
		{
			this.gameOver;
		};
		
		// transpose the music
		if(soundModule.notNil && currentBrick.notNil) {
			soundModule.setContextChord(currentBrick.chord);
		};
		
		updateInterval = startInterval / (scoreManager.level+1);
		
	}
	
	gameOver {
		updateTask.stop;
		soundModule.stopMusic;
		soundModule.playGameOverSound;
		currentBrick = nil;
		chordtris.setPauseMenuItemEnabled(false);
		gameOver = true;
	}
	
	generateNewBrick {
		^ChordtrisBrick.new(
			[\i, \j, \l, \o, \s, \t, \z].choose,
			this.generateRandomChord,
			4, //x
			0, //y
			0 //orientation
		);
	}
	
	generateRandomChord {
		var chord = Chord.new(12.rand, [\minor, \major].choose); 
		chord.chordLanguage = ChordtrisPreferences.getPreferences.at(\chordNameLanguage).value.asSymbol;
		^chord;
	}
	
	
	
	// draw the matrix and the current brick
	animate
	{
		var bounds = chordtris.window.bounds;
		var chordNameX, chordNameY;
		
		this.drawLines;
		
		Pen.font_(Font("Courier", 22));
		
		// draw the current brick
		if(currentBrick.notNil)
		{
			matrix.colorBrickCells(currentBrick);
			
			// paint all colored cells in the matrix
			matrix.draw;
			
			// draw the chord name on the brick
			if(currentBrick.brickType == \j or: { currentBrick.brickType == \t}) { Pen.color = Color.black } { Pen.color = Color.white };
			chordNameX = (currentBrick.x) * squareSize;
			chordNameY = (currentBrick.y) * squareSize;
			Pen.stringAtPoint(currentBrick.chord.asString, Point(chordNameX, chordNameY));
		};
		
		if(gameOver)
		{
			Pen.color = Color.white;
			Pen.stringAtPoint("Game Over", Point(90, 260));
		};
		
		if(isPaused)
		{
			Pen.color = Color.white;
			Pen.stringAtPoint("Game Paused", Point(90, 260));
		};
		
			
	}
	
	drawLines {
		var lineLength = matrix.numRows * squareSize;
		
		// draw vertical lines
		Pen.color = Color.new(0.2, 0.2, 0.4, 0.8);
		(matrix.numColumns-1).do { |i|
			var x = squareSize + (i*squareSize);
			Pen.line(x@0, x@lineLength);
		};
		Pen.stroke;
		
		// draw horizontal lines
		lineLength = matrix.numColumns * squareSize;
		(matrix.numRows-1).do { |i|
			var y = squareSize + (i*squareSize);
			Pen.line(0@y, lineLength@y);
		};
		Pen.stroke;
	}

	handleKeyDown { |view, char, modifiers, unicode, keycode|
		//[modifiers, unicode, keycode].postln;
		if(gameOver.not)
		{
			if(isPaused.not)
			{
				case
				{ keycode == 124} { // cursor right
					this.goRight;
				}
				{ keycode == 123} { // cursor left
					this.goLeft;
				}
				{ keycode == 126} { // cursor up
					this.rotateRight;
				}
				{ keycode == 125} { // cursor down
					this.fall;
				}
			};
			
			case
			{ unicode == 112} { // P
				if(isPaused) { this.resume } { this.pause };
			}
			{ unicode == 27} { // Escape
				chordtris.window.close;
			};
		}
		{
			this.reset;
		}
		
	}
	
	goRight {
		currentBrick.goRight(matrix);
	}
	
	goLeft {
		currentBrick.goLeft(matrix);
	}
	
	rotateLeft {
		currentBrick.rotateLeft(matrix);
	}
	
	rotateRight {
		currentBrick.rotateRight(matrix);
	}
	
	fall {
		if(currentBrick.notNil)
		{
			currentBrick.fall(matrix);
			matrix.colorBrickCells(currentBrick);
			this.brickHitTheGround;
		}
	}
	
	handleNote { |note, velocity|
		var noteSet, currentTime, timeDelta;
		
		soundModule.playMidiNote(note, velocity);
		
		if(currentBrick.notNil)
		{
			if(chordCollectionTask.isPlaying)
			{
				collectionNoteSet.add(note);
			}
			{
				collectionNoteSet.clear;
				collectionNoteSet.add(note);
				chordCollectionTask.reset;
				chordCollectionTask.play;
			};
		}
	}
	
	chordCollectionFinished {
		if(collectionNoteSet.size > 1)
		{
			// the note is part of a played chord
			this.handleChord(collectionNoteSet);
		}
		{
			// single note, part of a scale
			this.handleSingleNote(collectionNoteSet.asArray[0]);		};
	}
	
	handleChord { |noteSet|
		var highestNote;
		
		if(this.chordIsCorrect(noteSet))
		{
			//"richtiger Akkord :)".postln;
			highestNote = noteSet.maxItem;
			if(lastHighestChordNote.isNil)
			{
				lastHighestChordNote = highestNote;
			}
			{
				if(highestNote > lastHighestChordNote)
				{
					this.rotateRight;
				};
				
				if(highestNote < lastHighestChordNote)
				{
					this.rotateLeft;
				};
				
				lastHighestChordNote = highestNote;
			};
		}
		{
			//"FAIL!".postln;
			lastHighestChordNote = nil;
		};
	}
	
	chordIsCorrect { |noteSet|
		var normalizedSet = Set.new;
		var referenceSet = currentBrick.chord.noteSet;
		noteSet.do { |note|
			normalizedSet.add(note % 12);
		}
		
		^referenceSet.isSubsetOf(normalizedSet);
		
	}
	
	handleSingleNote { |note|
		var normalizedNote;
		var currentChord;
		var currentScale;
		var scaleNotes;
		//("single note: " + note).postln;
		
		currentChord = currentBrick.chord;
			
		// check if a low note is played
		if(note <= 48 && (note % 12) == currentChord.baseKey)
		{
			// let the brick fall down if this is a low base key
			this.fall;
		}
		{
			// get the current scale
			if(currentChord.kind == \major) { currentScale = Scale.major} { currentScale = Scale.harmonicMinor };
			// compute which notes are in the scale corresponding to the context chord
			scaleNotes = currentScale.degrees + currentChord.baseKey % 12;
			
			normalizedNote = note % 12;
			// check if the note played is in the current scale
			if(scaleNotes.includes(normalizedNote))
			{
				var scaleDegree = scaleNotes.indexOf(normalizedNote);
				("scale degree: " + scaleDegree).postln;
				
				if(lastSingleNoteDegree.isNil)
				{
					lastSingleNoteDegree = scaleDegree;
				}
				{
					if(scaleDegree == (lastSingleNoteDegree+1) or: { scaleDegree == 0 && (lastSingleNoteDegree == 6)})
					{
						this.goRight;
					};
					
					if(scaleDegree == (lastSingleNoteDegree-1) or: { scaleDegree == 6 && (lastSingleNoteDegree == 0)})
					{
						this.goLeft;
					};
					
					lastSingleNoteDegree = scaleDegree;
				};
			}
			{
				//"FAIL, note not in scale!".postln;
				lastSingleNoteDegree = nil;
			};
		
		}
		
	}
	
}