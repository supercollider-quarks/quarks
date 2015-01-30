TempoMap {
	
	// a map to convert beats to seconds over a timeline
	// tempo 1 means: 1 beat is 1 second (60 BPM)
	
	// events: a sorted array with elements [ tempo, beat, time ]
	// the 'time' slot is updated automatically for all events when adding one
	
	classvar <>defaultBarMap;
	classvar <>default;
	classvar <>presetManager;
	
	var <events, <size;
	var <>barMap;
	
	*initClass {
		StartUp.defer({
			defaultBarMap = BarMap();
			default = TempoMap();
			presetManager = PresetManager( this, [ \default, default ] )
				.getFunc_({ |obj| obj.duplicate })
				.applyFunc_({ |object, preset|
			 		object.fromObject( preset );
		 		});
		});
	}
	
	*new { |tempo = 1, beat = 0 ...argPairs|
		^super.newCopyArgs.init( tempo ? 1, beat ? 0, *argPairs )
	}
	
	*presets { ^presetManager.presets.as(IdentityDictionary) }
	
	fromObject { |obj|
		this.events = obj.value.events.collect(_.copy);
		this.barMap.events = obj.value.barMap.events.collect(_.copy); // update duration of units
		this.barMap.beatDenom = obj.value.barMap.beatDenom;
	}
	
	*fromObject { |obj|
		^obj.value.deepCopy;
	}
	
	*fromPreset { |name| ^presetManager.apply( name ) }
	
	fromPreset { |name| ^presetManager.apply( name, this ); }
	
	init { |...args|
		events = [];
		args.pairsDo({ |tempo, beat|
			events = events.add([ tempo, beat, 0 ]);
		});
		this.barMap = defaultBarMap.duplicate;
		this.prUpdateTimes;
		this.changed( \init );
	}
	
	events_ { |evts| // needs to be in the correct format
		events = evts ? events;
		this.prUpdateTimes;
		this.changed( \events );
	}
	
	duplicate {
		^this.class.newCopyArgs( events.collect(_.copy), size, barMap.duplicate );
	}
	
	*secondsToBeats { |time = 0, tempo = 1|
		^time * tempo;
	}
	
	*beatsToSeconds { |beat = 0, tempo = 1|
		^beat / tempo;
	}
	
	tempoToBPM { |tempo = 1|
		^tempo * (240 / barMap.beatDenom);
	}
	
	bpmToTempo { |bpm = 60|
		^bpm / (240 / barMap.beatDenom);
	}
	
	prUpdateTimes {
		var time = 0, lastBeat = 0, lastTempo = 1;
		size = events.size; // update size
		events.sort({ |a,b| a[1] <= b[1] });
		events.do({ |item| // update times
			var beat;
			beat = item[1];
			item[2] = time = time + this.class.beatsToSeconds( beat - lastBeat, lastTempo );
			lastBeat = beat;
			lastTempo = item[0];
		});
	}
	
	prUpdateBeats {
		var beat = 0, lastTime = 0, lastTempo = 1;
		size = events.size; // update size
		events.sort({ |a,b| a[2] <= b[2] });
		events.do({ |item| // update times
			var time;
			time = item[2];
			item[1] = beat = beat + this.class.secondsToBeats( time - lastTime, lastTempo );
			lastTime = time;
			lastTempo = item[0];
		});
	}
	
	deleteDuplicates { // remove events that have the same tempo as the event before
		var tempo;
		events = events.collect({ |item|
			if( tempo == item[0] ) {
				nil;
			} {
				tempo = item[0];
				item
			};
		}).select(_.notNil);
		size = events.size; // update size
		this.changed( \events );
	}
	
	put { |...args| // beat, tempo pairs
		args.pairsDo({ |beat, tempo|
			events.removeAllSuchThat({ |item| item[1] == beat });
			if( tempo.notNil ) {
				events = events.add([ tempo, beat, 0 ]);
			};
		});
		this.prUpdateTimes;
		this.changed( \events );
	}
	
	prIndexAtBeat { |beat = 0|
		var i = 1;
		while { (i < size) && { events[i][1] <= beat } } {
			i = i+1;
		};
		^i-1;
	}
	
	prIndexAtTime { |time = 0|
		var i = 1;
		while { (i < size) && { events[i][2] <= time } } {
			i = i+1;
		};
		^i-1;
	}
	
	tempoAtBeat { |beat = 0|
		^events[ this.prIndexAtBeat( beat ) ][0];
	}
	
	tempoAtTime { |time = 0|
		^events[ this.prIndexAtTime( time ) ][0];
	}
	
	bpmAtTime { |time = 0|
		^this.tempoToBPM( this.tempoAtTime( time ) );
	}
	
	bpmAtBeat { |beat = 0|
		^this.tempoToBPM( this.tempoAtBeat( beat ) );
	}
	
	setTempoAtBeat { |tempo = 1, beat = 0, add = false|
		if( add ) {
			this.put( beat, tempo );
		} {
			events[ this.prIndexAtBeat( beat ) ][0] = tempo;
			this.prUpdateTimes;
			this.changed( \events );
		};
	}
	
	setTempoAtTime { |tempo = 1, time = 0, add = false| 
		// !! changes the beats of events after !!
		if( add ) {
			this.put( this.beatAtTime( time ), tempo );
		} {
			events[ this.prIndexAtTime( time ) ][0] = tempo;
			this.prUpdateBeats;
			this.changed( \events );
		};
	}
	
	setTempoAtBar { |tempo = 1, bar = 1, division = 0, add = false| 
		this.setTempoAtBeat( tempo, this.beatAtBar( bar, division ), add );
	}
	
	setBPMAtBeat { |bpm = 60, beat = 0, add = false|
		this.setTempoAtBeat( this.bpmToTempo( bpm ), beat, add );
	}
	
	setBPMAtTime { |bpm = 60, time = 0, add = false|
		this.setTempoAtTime( this.bpmToTempo( bpm ), time, add );
	}
	
	setBPMAtBar { |bpm = 60, bar = 1, division = 0, add = false| 
		this.setTempoAtBeat( this.bpmToTempo( bpm ), this.beatAtBar( bar, division ), add );
	}

	timeAtBeat { |beat = 0|
		var evt;
		evt = events[ this.prIndexAtBeat( beat ) ];
		^evt[2] + ((beat - evt[1]) / evt[0]);
	}
	
	beatAtTime { |time = 0|
		var evt;
		evt = events[ this.prIndexAtTime( time ) ];
		^evt[1] + ((time - evt[2]) * evt[0]);
	}
	
	barAtTime { |time = 0|
		^barMap.barAtBeat( this.beatAtTime( time ) );
	}
	
	timeAtBar { |bar = 1, division = 0|
		^this.timeAtBeat( barMap.beatAtBar( bar, division ) );
	}
	
	useBeat { |time = 0, func|
		^this.timeAtBeat( func.value( this.beatAtTime( time ), this ) );
	}
	
	useBar { |time = 0, func|
		^this.timeAtBar( *func.value( *this.barAtTime( time ) ++ this ) );
	}
	
	timeMoveWithSnap { |time = 0, delta = 1, snap = 0.25|
		var beat, newBeat;
		beat = this.beatAtTime( time );
		newBeat = this.beatAtTime( time + delta );
		^this.timeAtBeat( beat + (newBeat - beat).round(snap) );
	}
	
	signatureAtTime { |time = 0|
		^barMap.signatureAtBeat( this.beatAtTime( time ) );
	}
	
	setSignatureAtTime { |signature, time = 0, addNew = false|
		barMap.setSignatureAtBeat( signature, this.beatAtTime( time ), addNew );
	}
	
	barLines { |startTime = 0, endTime|
		^barMap.barLines( this.beatAtTime( startTime ), this.beatAtTime( endTime ) )
			.collect({ |item| this.timeAtBeat( item ) });
	}
	
	divisionLines { |startTime = 0, endTime, div = 1|
		^barMap.divisionLines( this.beatAtTime( startTime ), this.beatAtTime( endTime ), div )
			.collect({ |item| this.timeAtBeat( item ) });
	}
	
	bpmsBetween { |startTime = 0, endTime = inf|
		^events.select({ |item|
			item[2].inclusivelyBetween( startTime, endTime );
		}).collect({ |item|
			[ this.tempoToBPM( item[0] ).round(0.001), item[2] ]
		});
	}
	
	signaturesBetween { |startTime = 0, endTime = inf|
		var startBeat, endBeat, barMapEvents;
		startBeat = this.beatAtTime( startTime );
		endBeat = this.beatAtTime( endTime );
		barMapEvents = barMap.events.select({ |item|
			item[3].inclusivelyBetween( startBeat, endBeat );
		});
		^barMapEvents.collect({ |item|
			[ item[[0,1]], this.timeAtBeat( item[3] ) ];
		});
	}
	
	== { |that| ^that.class == this.class && { 
			this.events == that.events && {
				this.barMap == that.barMap
			} 
		} 
	}
	
	storeArgs {
		^events.collect({ |item|
			[ item[0], item[1] ];
		}).flatten(1);
	}
	
	storeModifiersOn { |stream|
		if( barMap != defaultBarMap ) {
			stream << ".barMap_(" <<< barMap << ")";
		};
	}
	
	doesNotUnderstand { |selector ...args|
		// refer to barMap
		var res;
		res = barMap.perform( selector, *args );
		if( res != barMap ) {
			^res;
		};
	}
}