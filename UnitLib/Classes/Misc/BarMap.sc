BarMap {
	
	// a map to convert beats to bar numbers over a time line
	// signatures are notated as arrays: [3,4] means a signature of 3/4
	
	// events: a sorted array with elements [ num, denom, bar, beat, numBars ]
	// the last two items of the array (beat, numBars) are updated internally
	
	var <events;
	var <beatDenom = 4; // a beat is 1/4; 1s at default tempo and bar
	
	*new { |signature = #[4,4], bar = 1...argPairs|
		^super.newCopyArgs.init( (signature ? [4,4]).asCollection, bar ? 1, *argPairs )
	}
	
	init { |...argPairs|
		events = [];
		argPairs.pairsDo({ |signature, bar|
			events = events.add( this.class.formatEvent( signature, bar ) );
		});
		this.prUpdateBeats;
		this.changed( \init );
	}
	
	duplicate {
		^this.class.newCopyArgs( events.collect(_.copy), beatDenom );
	}
	
	*formatSignature { |signature = #[4,4]|
		if( signature.isFloat ) {
			signature = signature.asFraction;
		} {
				signature = signature.asCollection;
		};
		^[ signature[0], signature.wrapAt(1).nextPowerOfTwo ];
	}
	
	*formatEvent { |signature = #[4,4], bar = 1, event|
		event = event ? [4,4,1,0,inf];
		signature = this.formatSignature(signature);
		event.put(0, signature[0] );
		event.put(1, signature[1] );
		event.put(2, bar.asInt );
		^event;
	}
	
	firstBar { ^events[0][2]; }
	
	beatDenom_ { |new = 4|
		if( beatDenom != new ) {
			beatDenom = new;
			this.prUpdateBeats;
		};
	}
	
	startBeat_ { |new = 0|
		events[0][3] = new;
		this.prUpdateBeats;
		this.changed(\events);
	}
	
	startBeat { ^events[0][3] }
	
	deleteDuplicates { |force = false|
		// remove events that have the same tempo as the event before
		var signature;
		this.events = events.sort({ |a,b| a[2] <= b[2]; }).collect({ |item|
			if( signature == item[[0,1]] ) {
				nil;
			} {
				signature = item[[0,1]];
				item
			};
		}).select(_.notNil);
	}
	
	prUpdateBeats {
		var beat, lastBar, num, denom;
		events = events.sort({ |a,b| a[2] <= b[2]; });
		events.last[4] = inf;
		num = events[0][0];
		denom = events[0][1];
		lastBar = events[0][2];
		beat = events[0][3];
		events[1..].do({ |item, i|
			item[3] = beat = (beat + ( (item[2] - lastBar) * (num/denom) * beatDenom ));
			#num, denom = item[..1];
			lastBar = item[2];
			events[i][4] = lastBar - events[i][2];
		});
	}
	
	eventAtBeat { |beat|
		^events.lastForWhich({ |item| item[3] <= beat }) ? events[0];
	}
	
	eventAtBar { |bar|
		^events.lastForWhich({ |item| item[2] <= bar }) ? events[0];
	}
	
	barAtBeat { |beat = 0, clip = false|
		var raw, mul;
		var num, denom, bar, sigStartBeat;
		if( clip ) { beat = beat.max( 0 ) };
		#num, denom, bar, sigStartBeat = this.eventAtBeat(beat);
		mul = (num/denom) * beatDenom;
		raw = bar + ((beat - sigStartBeat) / mul);
		^[ raw.floor.asInt, raw.frac * num ]; // bar, division
	}
	
	beatAtBar { |bar = 1, division = 0, clip = false|
		var num, denom, sigStartBar, beat;
		#num, denom, sigStartBar, beat = this.eventAtBar(bar);		bar = bar + (division / num) - sigStartBar;
		beat = beat + (bar * (num/denom) * beatDenom);
		if( clip ) { beat = beat.max( 0 ) };
		^beat;
	}
	
	barAtBar { |bar = 1, division = 0|
		var raw;
		var num;
		num = this.eventAtBar(bar)[0];
		raw = bar + (division / num);
		raw = raw.max( events[0][2] );
		^[ raw.floor.asInt, raw.frac * num ]; // bar, division
	}
	
	signatureAtBeat { |beat = 0|
		^this.eventAtBeat(beat)[[0,1]];
	}
	
	signatureAtBar { |bar = 1|
		^this.eventAtBar(bar)[[0,1]];
	}
	
	setSignatureAtBar { |signature, bar = 1, addNew = false|
		if( addNew != true ) {
			bar = this.eventAtBar(bar)[2];
		};
		this.put( bar, signature );
	}
	
	setSignatureAtBeat { |signature, beat = 0, addNew = false|
		var bar;
		if( addNew ) {
			bar = this.barAtBeat(beat)[0];
		} {
			bar = this.eventAtBeat(beat)[2];
		};
		this.put( bar, signature );
	}
	
	events_ { |evts| // needs to be in the correct format
		events = evts ? events;
		if( events.size == 0 ) {
			events = [ [ 4,4,1,0,inf ] ];
		};
		this.prUpdateBeats;
		this.changed( \events );
	}
	
	put { |...args| // beat, tempo pairs
		args.pairsDo({ |bar, signature|
			var oldEvent;
			bar = bar.asInt;
			oldEvent = (events.removeAllSuchThat({ |item| item[2] == bar }) ? [])[0];
			if( signature.notNil ) {
				events = events.add( this.class.formatEvent( signature, bar, oldEvent ) );
			};
		});
		this.prUpdateBeats;
		this.changed( \events );
	}
	
	== { |that| ^that.class == this.class && { this.events == that.events } }
	
	eventsForLines { |startBeat = 0, endBeat|
		var startIndex, endIndex, startBar;
		var tempEvents;
		endBeat = endBeat ?? { events.last[3] + 10 };
		startIndex = events.lastIndexForWhich({ |item| item[3] <= startBeat }) ? 0;
		endIndex = events.lastIndexForWhich({ |item| item[3] <= endBeat }) ? 0;
		tempEvents = events[ startIndex..endIndex ].deepCopy;
		startBar = this.barAtBeat(startBeat)[0];
		tempEvents[0] = tempEvents[0][..1] ++ [ startBar, 
			this.beatAtBar(startBar), 
			tempEvents[0][4] - (startBar - tempEvents[0][2] )
		];
		tempEvents.last[4] = tempEvents.last[4].min( (this.barAtBeat(endBeat)[0].ceil + 1) - tempEvents.last[2] );
		^tempEvents;
	}
	
	barLines { |startBeat = 0, endBeat|
		^this.eventsForLines( startBeat, endBeat ).collect({ |item|
			var num, denom, bar, beat, numBars;
			#num, denom, bar, beat, numBars = item;
			Array.series( numBars, beat, (num/denom) * beatDenom );
		}).flatten(1);
	}
	
	divisionLines { |startBeat = 0, endBeat, div = 1|
		^this.eventsForLines( startBeat, endBeat ).collect({ |item|
			var num, denom, bar, beat, numBars;
			#num, denom, bar, beat, numBars = item;
			Array.series( numBars * num / div, beat, (beatDenom / denom) * div );
		}).flatten(1);
	}

	storeArgs {
		^events.collect({ |item|
			[ [ item[0], item[1] ], item[2] ];
		}).flatten(1);
	}
	
	storeModifiersOn { |stream|
		if( beatDenom != 4 ) {
			stream << ".beatDenom_(" <<< beatDenom << ")";
		};
		if( this.startBeat != 0 ) {
			stream << ".startBeat_(" <<< this.startBeat << ")";
		};
	}
	
}