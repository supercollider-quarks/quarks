// wslib 2006

// full MIDI file implementation
// * write and read
// * includes all (-most all) possible midi and meta data types
// ... not that simple after all ...

// MIDIFiles can be read, altered and written with this class. Also musical data can be
// extracted to use for SC patches. 

// all MIDI and Meta events are converted internally to more understandable arrays of data. 
// Status bytes are replaced by Symbols with their names. Unknown statusbytes stay
// as they were (if there are any; which I find hard to believe). Meta event data is converted to
// usable formats as far as possible; strings become Strings, tempo becomes BPM (instead of MPQN).
// other meta event data types become arrays of raw data. The delta times used in the
// MID-file format are replaced by absolute times, to make editing easier and more safe. Sysex
// data is lost and removed in this version. Might add it in the future, but couldn't find any 
// use for it yet.. 

// partially based on J. Narveson's MIDIFile class 
// (NOT on sc solar / hairi's swiki version which i'm sorry to say contains some annoying flaws!!).
// MIDI file format reference: http://www.sonicspot.com/guide/midifiles.html

// a few notes:
//
// - format 1 midifiles tend to have meta events like 'timeSignature', 'keySignature',
// 'smpteOffset', 'marker' and 'tempo' in the first track (0). 
// MIDI events usually start in the second track (1).
// Meta events likely to be found in all tracks are 'trackName', 'instName' and 'endOfTrack'.
// - pitchBend data might not be right yet; comes up with strange figures sometimes
// - haven't got a method yet to add key signatures; only read. To add you need to understand
// the MIDI file formatting for key signatures.
// - the timeMode method converts times from ticks to seconds and vv. Ticks are tempo-depending,
// seconds are not. So if you want to change tempo but keep the midi events at their original
// time position (i.e. re-map time), change to timeMode = \seconds first.
// - the tempo var is filled by the first tempo meta event found in the file (usually at the 
// start of track 0). If multiple tempo events are found they are all used for the tempo map,
// which is used for conversion from ticks to seconds and vv.

SimpleMIDIFile  {

	classvar <>tempoScale = 1024;
	var <tempo = 120;
	var 	<>pathName;
	
	var <>midiEvents;    // format: [trackNumber, absTime, type, channel, val1, val2] 
	var <>metaEvents;  // format: [trackNumber, absTime, type, [ values ] / value / string ]
	
	var <timeMode = \ticks;  // \ticks or \seconds	
	
	var <>sysexEvents;   /// not yet implemented (probably never will) 
		
	var	<>format;		// single, multi, or pattern oriented MIDI sequence
	var	<>division;	// division: number of ticks per quarter note
	var  <>tracks;
	
	var	<>theCmd, <>theChan, <>numbytes, <>theTrackNumber, <>curTime; // for read/write
	
	var <pitchBendMode = \int8; // no conversion needed for this mode (default; do not change here) 
	
	*new { arg pathName; 
		^super.new.pathName_(pathName).init;
	}
	
	*read { arg pathName; ^SimpleMIDIFile( pathName ).read; }
	
	init { 
		pathName = pathName ?? { "~/scwork/midi.mid" };
		pathName = pathName.standardizePath;
		midiEvents = [];
		metaEvents = [];
		tracks = 1;
		format = 1;
		division = tempoScale; 
		}
		
	init0 { |inTempo = 120, timeSignature| // create empty format 0 file
		timeSignature = timeSignature ? "4/4";
		if( timeSignature.class != String )
			{ timeSignature =  timeSignature.join( $/ ) };
		tempo = inTempo ? tempo;
		this.setTempo;
		this.addTimeSignatureString( timeSignature );
		format = 0;
		tracks = 1;
		}
		
	init1 { | inTracks = 2, inTempo = 120, timeSignature|  // create empty format 1 file
		timeSignature = timeSignature ? "4/4";
		if( timeSignature.class != String )
			{ timeSignature = timeSignature.join( $/ ) };
		tempo = inTempo ? tempo;
		this.setTempo;
		this.addTimeSignatureString( timeSignature );
		format = 1;
		tracks = inTracks;
		(inTracks - 1).do({ |i| 
			this.addMetaEvent( [ i+1, 0, \trackName, "track " ++ (i+1) ] ); });
		this.adjustEndOfTrack;
		this.adjustTracks;
		}
		

/////////////////////  read support:
	
	read { var file, time;
		file = File(pathName,"r");
		theTrackNumber = -1;
		while (
			{file.pos < (file.length-8) },
			{this.processChunk(file)}
		);
		file.close;
		this.getTempo;
	}
	
	convertToVLInteger { arg dT;
		var dTArray;
		dT = dT.max(0).round.asInteger; // changed 03/19/08 & 06/24/08 ws
		dTArray = [dT & 127];
		dT = dT >> 7;
		while ({dT != 0;},
		{
			dTArray = [dT & 127 - 128] ++ dTArray;
			dT = dT >> 7;
		});
		^dTArray;
	}
	
	convertToInt8 { |val, size=2|
		^Array.fill( size, { |i| ( (val  / (2** (i * 8) ) ) % 256).floor }).reverse;
		}		

	getVl  { arg file; 
		var accum = 0, cur = 0;
		while ( {(cur = file.getInt8) < 0}, { accum = (accum << 7) + 128 + cur });
		^accum = (accum << 7)  + cur;
	}
	
	getTime { arg file; 
		curTime = curTime + this.getVl(file);
	}

	// meta events:
	
	handleMeta { arg file; var id, len, str;
		var type;
		id = file.getInt8;
		len = this.getVl(file);
		case { id == 0  } // text type
			{ str = file.getInt16; type = \sequenceNumber; }
			{ id < 8  } // text type
			{ str = String.fill(len, { file.getChar } );
				type = [\text, \copyright, \trackName, \instName, 
					\lyrics, \marker, \cuePoint][id-1]; }
			{ id == 32 }
			{ str = Array.fill( len, { file.getInt8 } );
				type = \midiChannelPrefix; }
			{ id == 47 }
			{ str = nil; type = \endOfTrack;  }
			{ id == 81 }
			{ str =  ( ( file.getInt8 & 255 ) * ( 2**16 ) ) + 
					( ( file.getInt8 & 255 ) * 256 ) + (file.getInt8 & 255); // int24
				str =  60000000 / str ;
				type = \tempo; }
			{ id == 84 }
			{ str = SMPTE.fromMIDIFileArray( Array.fill( len, { file.getInt8 } ) );
				type = \smpteOffset; }
			{ id == 88 }
			{ str = Array.fill( len, { file.getInt8 } );
				type = \timeSignature; }
			{ id == 89 }
			{ str = Array.fill( len, { file.getInt8 } );
				type = \keySignature; }
			{ id == 127 }
			{ str = Array.fill( len, { file.getInt8 } );
				type = \sequencerSpecific; }
			{ true }
			{ str = Array.fill( len, { file.getInt8 } ); 
				type = id; };
		//file.seek(len,1);
		metaEvents = metaEvents.asCollection.add( [theTrackNumber, curTime, type, str ]
			.select( _.notNil ) );
	}
	
	handleSysex { arg file; var len;
		len = this.getVl(file);
		file.seek(len,1);
	}
	
	// midi events: 
	
	handleMIDI { arg cmd, file;
		theCmd = (cmd + 128) >> 4;
		theChan = (cmd + 128) & 15;
		numbytes = [2,2,2,2,1,1,2].at(theCmd);
		this.handleRunningStatus(file.getInt8, file);
	}
	
	handleRunningStatus { arg val, file; 
		var packet;
				
		packet = [curTime,  val];
		if(numbytes == 2, {packet = packet ++ [file.getInt8];});
		
		if( ( theCmd > -1 ) and: { theCmd < 7 } )
			{ midiEvents = midiEvents.asCollection.add( 
				 [theTrackNumber, packet[0]] ++ [
					[ \noteOff, \noteOn, // not used
					\polyTouch, \cc, 
					\program, \afterTouch,
					\pitchBend ][theCmd] ] ++ [theChan] ++ packet[1..]
				)  };
		// format: [trackNumber, absTime, type, channel, val1, val2]
	}

	processChunk { arg file;
		var header, length;
		var val, trackEnd;
		
		header = String[file.getChar,file.getChar,file.getChar,file.getChar];
		length = file.getInt32;
		
		curTime = 0;
		if(header == "MThd", {
			format = file.getInt16;
			tracks = file.getInt16;
			division = file.getInt16;
			
		});
		
		if(header == "MTrk", {
			trackEnd = length + file.pos;
			theTrackNumber = theTrackNumber + 1;
			while(
				{trackEnd != file.pos}, 
				{ 
				
				this.getTime(file); 
				val = file.getInt8;
				 if (val < 0, {
					if (val >= -16, {
				 		if (val == -1, { this.handleMeta(file)});
					 	if (val == -9, { this.handleSysex(file)});
						if (val == -16,{ this.handleSysex(file)});
					}, {
						this.handleMIDI(val, file);
					});
				 }, {
						this.handleRunningStatus(val, file);
				 });
			});
		});
	}
	
	getTempo { 
		^tempo = 
			if( this.tempi.notNil )
				{ this.tempi[0]  ? tempo; }
				{ tempo };
		}
		
	setTempo { |newTempo| // set tempo also in metaEvents (only first tempo)
		newTempo = newTempo ? tempo;
		tempo = newTempo;
		this.addTempo( tempo );
		}
	
/////////////////// write support	
	
	writeFile { |theFile|
		var activeSeqs;
		
		this.adjustEndOfTrack;
		this.adjustTracks;
		this.convertPitchBend( \int8 ); // convert back to default !!
				
		activeSeqs = this.asMIDIFileChunks.select({ arg seq; seq.size != 0});

		theFile.putString("MThd");			// make the MID file header
		theFile.putInt32(6);				// 6 data bytes: format, numberOfTracks, division
		theFile.putInt16(format);			
		theFile.putInt16(activeSeqs.size);	
		theFile.putInt16(division);
		
		activeSeqs.do({ arg seq;			// now write the tracks
			seq = seq.flat;
			theFile.putString("MTrk");
			theFile.putInt32(seq.size);
			seq.do({ arg b; if( b.isNumber ) { theFile.putInt8(b) } { theFile.putChar(b) }; })
		});
		
		theFile.close;
		
		}
	
	write { arg newFileName;   //// write the current data to new file
		var theFile;

		newFileName = newFileName ?? pathName;
		newFileName = newFileName.standardizePath;
		
		theFile = File(newFileName,"wb+");
		
		this.writeFile( theFile );
	}

	adjustTracks {  /// adjust track count from midi event information
		var maxTrackNumber = 0;
		
		midiEvents.do({ |event| 
			if( event[0] > maxTrackNumber )
				{ maxTrackNumber = event[0] };
				});
				
		metaEvents.do({ |event| 
			if( event[0] > maxTrackNumber )
				{ maxTrackNumber = event[0] };
				});
		tracks = maxTrackNumber + 1;
		
		//if( (tracks == 1) && (format == 1 ) ) { format = 0 }  
		
		if( (tracks > 1) && (format == 0 ) ) { format = 1 };
		}	
		
	// conversion back to midi file data:
	
	asMIDIChunks { // with absolute time
		^this.midiTracks.collect({ |track|
			track.collect({ |event|
				[ event[0], (					// absolute time
					'noteOff':	0x80 ,
					'noteOn':		0x90 ,
					'polyTouch':	0xA0 ,
					'cc':		0xB0 ,
					'program':	0xC0 ,
					'afterTouch':	0xD0 ,
					'pitchBend':	0xE0 )[event[1]] + event[2] ] ++ event[3..];
				});
			});
		}
		
	asMetaChunks {  // [ absTime, 255, typeId .. rest ] -- strings will be array of chars
		^this.metaTracks.collect({ |track|
			track.collect({ |event|
				var type = \other; // if event id is unknown
				var out;
				if( event[1].class == Symbol )
					{ type = event[1]; };
				out = [ event[0], 255 ,
					(	'sequenceNumber':  0,
						'text': 1,
						'copyright': 2, 
						'trackName': 3, 
						'instName': 4, 
						'lyrics': 5, 
						'marker': 6, 
						'cuePoint': 7,
						'midiChannelPrefix': 32,
						'endOfTrack': 47,
						'tempo': 81,
						'smpteOffset': 84,
						'timeSignature': 88,
						'keySignature': 89,
						'sequencerSpecific': 127,
						'other': event[1]
					)[ type ] ] ++ [event[2].size] ++ event[2];
				if( type == 'sequenceNumber' )
					{ out = out[0..2] ++ [2] ++ this.convertToInt8( out[4], 2 ); };
				if( type == 'tempo' )
					{ out = out[0..2] ++ [3] ++ this.convertToInt8( 60000000 / out[4], 3 ); };
				if( type == 'smpteOffset' )
					{ out = out[0..3] ++ out[4].asMIDIFileArray };
				out;
				});
			});
		}
		
	asChunks {  // combine meta and midi chunks
		var temp, midiChunks, metaChunks;
		temp = this.copy.timeMode_( \ticks );
		midiChunks = temp.asMIDIChunks;
		metaChunks = temp.asMetaChunks;
		^midiChunks.collect({ |track, i|
			(track ++ metaChunks[i]).sort({ |a,b| a[0] <= b[0] }); });
		}
	
	asDeltaChunks {
		 // delta times in ticks
		var chunks;
		chunks = this.asChunks;
		^chunks.collect({ |track|
			var lastTime = 0;
			track.sort({ |a, b| a[0] <= b[0] }); //sort to absolute times (just in case.. )
			track.collect({ |event|
				var out, now;
				now = event[0].round(1); // round absolute times to prevent drift
				out = [ now - lastTime ] ++ event[1..];
				lastTime = now;
				out;
				});
			});
		}
		
	asMIDIFileChunks {  // delta times in ticks
		var chunks;
		chunks = this.asDeltaChunks;
		^chunks.collect({ |track|
			track.collect({ |event|
				this.convertToVLInteger( event[0] ) ++ event[1..];
				});
			});
		}
		
	// extra ( not used for writing )
	
	asMetaDeltaChunks {  // delta times in ticks
		var chunks, temp;
		temp = this.copy.timeMode_( \ticks );
		chunks = temp.asMetaChunks;
		^chunks.collect({ |track|
			var lastTime = 0;
			track.sort({ |a, b| a[0] <= b[0] }); //sort to absolute times (just in case.. )
			track.collect({ |event|
				var out;
				out = [ event[0] - lastTime ] ++ event[1..];
				lastTime = event[0];
				out;
				});
			});
		}
	
	asMIDIDeltaChunks {  // delta times in ticks
		var chunks, temp;
		temp = this.copy.timeMode_( \ticks );
		chunks = temp.asMIDIChunks;
		^chunks.collect({ |track|
			var lastTime = 0;
			track.sort({ |a, b| a[0] <= b[0] }); //sort to absolute times (just in case.. )
			track.collect({ |event|
				var out;
				out = [ event[0] - lastTime ] ++ event[1..];
				lastTime = event[0];
				out;
				});
			});
		}
	
	
///////////// internal conversion:
	
	convertNoteOns { |noteOffVelo = 64|  // with velo == 0 to noteOffs
		midiEvents = midiEvents.collect({ |item|
			if( (item[2] == \noteOn) and: { item.last == 0 } )
				{ item[0..1] ++ [\noteOff] ++ item[3..4] ++ [noteOffVelo]; }
				{ item };
				});
		^this;	
		}
		
	convertNoteOffs { // to noteOns with velo == 0
		midiEvents = midiEvents.collect({ |item|
			if( item[2] == \noteOff  )
				{ item[0..1] ++ [\noteOn] ++ item[3..4] ++ [0]; }
				{ item };
				});
		^this;	
		}
		
	convertPitchBend { |to = \int16| 
		// int8 : array of 2 int8, as stored in the midifile
		// int16 : a single int16
		// float : a floating point value between -1.0 and 1.0
		var conversionDict;
		conversionDict = (	
			//	to:		from:
			
				int16: (	int8: { |item| item[0..3] ++ [ ( (item[5] * 128) + item[4] ) - (2**13) ]; },
						float: { |item| item[0..3] ++ [ (item[4] * (2**13)).round(1).asInt ]; } ),
						
				int8: ( 	int16: { |item| var item4;
							item4 = (item[4] + (2**13)) / 128;
							item[0..3] ++ [ item4.frac * 128, item4.floor ]; },
						float: { |item| var item4;
							item4 = ((item[4] + 1) * (2**13)).round(1) / 128;
							item[0..3] ++ [ item4.frac * 128, item4.floor ]; } ),
							
				float: (	int8: { |item| item[0..3] ++ [ (( (item[5] * 128) + item[4] ) / (2**13)) - 1 ];  },
						int16: { |item| item[0..3] ++ [ (item[4] / (2**13)) ]; } )
					);
					
		if( to != pitchBendMode )
			{ midiEvents = midiEvents.collect({ |item|
				if( item[2] == \pitchBend )
					{ conversionDict[to][pitchBendMode].value( item ); }
					{ item };
					}); };
		pitchBendMode = to;
		}
		
	pitchBendMode_ { |to, convert = true|
		to = to ? pitchBendMode;
		if( convert )
			{ this.convertPitchBend( to ); }
			{ pitchBendMode = to; };
		}
	
	convertTimes { |newTimeMode = \seconds|
		var tempoEnv;
		if( timeMode != newTimeMode )
			{ 	tempoEnv = this.tempoEnv;
				midiEvents = midiEvents.collect({ |event|
					[event[0]] ++ [ tempoEnv[ event[1] ] ] ++ event[2..] });
				metaEvents = metaEvents.collect({ |event|
					[event[0]] ++ [ tempoEnv[ event[1] ] ] ++ event[2..] });
				timeMode = newTimeMode; 
			};
		}
		
	timeMode_  { |newTimeMode, convert = true|
		if( newTimeMode.notNil )
			{ if( convert )
				{ this.convertTimes( newTimeMode ) }
				{ timeMode = newTimeMode };
			};
		}
		
	tempo_ { |newTempo = 120|   // converts midiEvents and metaEvents if needed
							/// does not add any tempo events to metaEvents
							// ** converts only if no tempo metaEvents are present
		if( timeMode == \seconds )
			{	this.convertTimes( \ticks );
				tempo = newTempo;
				this.convertTimes( \seconds ); }
			{ tempo = newTempo };
		}
	
	sortMIDIEvents {
		var tracksArray;
		tracksArray = Array.fill( tracks, { |i| midiEvents.select({ |item| item[0] == i }) } );
		tracksArray.collect({ |item| item.sort({ |a, b| a[1] <= b[1] }); });
		midiEvents = tracksArray.flatten(1);
		}
		
	sortMetaEvents {
		var tracksArray;
		tracksArray = Array.fill( tracks, { |i| metaEvents.select({ |item| item[0] == i }) } );
		tracksArray.collect({ |item| item.sort({ |a, b| a[1] <= b[1] }); });
		metaEvents = tracksArray.flatten(1);
		}


////////////  getting events
	
	////// getting midi events on specific channels/tracks
	
	midiChannelEvents { |channel, track| // select channels / tracks
		var tempMIDIEvents;
		if(channel.notNil)
			{ tempMIDIEvents = midiEvents.select({ |item| item[3] == channel }); }
			{ tempMIDIEvents = midiEvents };
		if(track.notNil)
			{ tempMIDIEvents = tempMIDIEvents.select({ |item| item[0] == track }); };
		^tempMIDIEvents;
		}
		
	midiTrackTypeEvents { |track = 0, type = \noteOn, channel|
		^this.midiChannelEvents(channel, track).select({ |item| item[2] == type });
		}
		
	midiDeltaEvents { |track = 0, type = \noteOn, channel|
		var tempEvents, lastTime = 0;
		tempEvents = this.midiTrackTypeEvents( track, type, channel );
		tempEvents.sort({ |a,b| a[1] <= b[1] });
		^tempEvents.collect({ |event| var out, tempTime;
			out = event.copy;
			tempTime = out[1];
			out[1] = tempTime - lastTime;
			lastTime = tempTime;
			out;
			});
		}
		
	midiDeltaCCEvents { |track = 0, cc = 1, channel|
		var tempEvents, lastTime = 0;
		tempEvents = this.midiTrackTypeEvents( track, \cc, channel );
		tempEvents = tempEvents.select({ |item| item[4] == cc });
		tempEvents.sort({ |a,b| a[1] <= b[1] });
		^tempEvents.collect({ |event| var out, tempTime;
			out = event.copy;
			tempTime = out[1];
			out[1] = tempTime - lastTime;
			lastTime = tempTime;
			out;
			});
		}
		
	envFromType { |track = 0, type = \pitchBend, channel|
		var tempEvents;
		tempEvents = this.midiDeltaEvents( track, type, channel );
		^Env(	tempEvents.collect({ |item| item[4] }), 
				tempEvents.collect({ |item| item[1] })[1..], \step );
		}
		
	envFromCC { |track = 0, cc, channel|
		var tempEvents;
		tempEvents = this.midiDeltaCCEvents( track, cc, channel );
		^Env(	tempEvents.collect({ |item| item[5] }), 
				tempEvents.collect({ |item| item[1] })[1..], \step );
		}
	
	
	midiTypeEvents { | ... args| // multiple types
		^midiEvents.select({ |item| args.includes( item[2] ); });
		}

	noteEvents { |channel, track| 
			^this.midiChannelEvents(channel, track)
				.select({ |item| [\noteOn, \noteOff].includes( item[2] ) }); }
	
	noteOnEvents { |channel, track| 
			^this.midiChannelEvents(channel, track)
				.select({ |item| \noteOn == item[2] }); }
				
	realNoteOnEvents { |channel, track|
			var temp;
			temp = this.copy.convertNoteOns; 
			^temp.midiChannelEvents(channel, track)
				.select({ |item| \noteOn ==item[2] }); }
	
	noteOffEvents { |channel, track| 
			^this.midiChannelEvents(channel, track)
				.select({ |item| \noteOff == item[2] }); }
				
	noteSustainEvents { |channel, track|  
		// [track, absTime, \noteOn, channel, note, velo, dur, upVelo]
			var temp, noteOns, noteOffs;
			temp = this.copy.convertNoteOns;
			noteOns = temp.noteOnEvents(channel, track);
			noteOffs = temp.noteOffEvents(channel, track);
			^noteOns.collect({ |item|
				var note, noteOff;
				note = [ item[0], item[3], item[4] ]; // [track, channel, note]
				noteOff = noteOffs.detect({ |subitem|
					[ subitem[0], subitem[3], subitem[4] ] == note });
				noteOffs.remove( noteOff );
				if( noteOff.isNil ) { // infinite sustain if no noteOff found
					noteOff = [ nil, inf, 64 ];
				};
				item ++ [ noteOff[1] - item[1], noteOff.last ]; // add sustain and upVelo
				});
			}
			
	firstNote { |trackArray|
		if( trackArray.isNil )
			{ ^this.firstNote( Array.series( tracks ) ) }
			{ ^trackArray.asCollection
				.collect({ |item| this.firstNoteOnTrack( item ) ? [item, inf]; })
				.sort({ |a,b| a[1] <= b[1] })
				.first };
		}
	
	firstNoteOnTrack { |track = 0| ^this.noteOnEvents( track: track ).first; }
			
	pitchBendEvents { |channel, track| 
			^this.midiChannelEvents(channel, track)
				.select({ |item| \pitchBend == item[2] }); }
				
	afterTouchEvents { |channel, track| 
			^this.midiChannelEvents(channel, track)
				.select({ |item| \afterTouch == item[2] }); }
				
	controllerEvents { |cc, channel, track|
			^this.midiChannelEvents(channel, track)
				.select({ |item| ( \cc == item[2] ) 
				and: { if( cc.notNil ) { item[4] == cc } { true }; };
			}); 
		}
	
	ccEvents { |cc, channel, track| ^this.controllerEvents( cc, channel, track ) } // shortcut
	modulationEvents { |channel, track| ^this.controllerEvents( 1, channel, track ) }
	breathEvents { |channel, track| ^this.controllerEvents( 2, channel, track ) }
	volumeEvents { |channel, track| ^this.controllerEvents( 7, channel, track ) }
	panEvents {  |channel, track| ^this.controllerEvents( 10, channel, track) }
	expressionEvents { |channel, track| ^this.controllerEvents( 11, channel, track ) }
	damperEvents { |channel, track| ^this.controllerEvents( 64, channel, track ) }
	
	timeSignatureEvents { 
		^metaEvents.select({ |event|
			event[2] == \timeSignature });
		}
		
	timeSignatures { // always on track 0
		^this.timeSignatureEvents.collect({ |event|
			var array;
			array = event[3];
			[ event[1] ] ++ [ [ array[0], 2**array[1] ].join("/") ];
			});
		}
	
	keySignatureEvents {
		^metaEvents.select({ |event|
			event[2] == \keySignature });
		}
	
	keySignatures {
		^this.keySignatureEvents.collect({ |event|
		var array;
		array = event[3];
		event[[0,1]] ++ [ 
		["Fb","Cb","Gb","Ab","Eb","Bb","F",
			"C","G","D","A","E","B","F#","C#","G#","D#","A#"][ array[0] + 7 + ( array[1] * 3)]
		++ [" major", " minor"][array[1]] ];
			});
		}
		
	smpteOffsetEvents {
		^metaEvents.select({ |event|
			event[2] == \smpteOffset });
		}	
		
	smpteOffset { ^this.smpteOffsetEvents[0][3] }
	
	/// getting track events
	
	midiTrackEvents { |trackNumber = 0| // on a specific track
		^midiEvents.select({ |item| item[0] == trackNumber }); }
		
	metaTrackEvents { |trackNumber = 0|
		^metaEvents.select({ |item| item[0] == trackNumber }); }
	
	midiTracks {   /// format:  [absTime, type, channel .. rest]
		^Array.fill( tracks, { |i|
			this.midiTrackEvents( i ).collect({ |item| item[1..] }); } ); 
		}
	
	metaTracks {  /// format: [absTime, type, [ rest ]]
		^Array.fill( tracks, { |i|
			this.metaTrackEvents( i ).collect({ |item| item[1..] }); } ); 
		}
	
	/// getting meta events
		
	trackNames { ^metaEvents.select({ |item|
		item[2] == \trackName; }).collect({ |item| item[[0,3]] }); }
		
	instNames { ^metaEvents.select({ |item|
		item[2] == \instName; }).collect({ |item| item[[0,3]] }); }
		
	trackName { |track = 0|
		var trackNameEvent;
		trackNameEvent = metaEvents.select({ |event| (event[2] == \trackName) and: 
			{ event[0] == track } }).first;
		if( trackNameEvent.notNil )
			{ ^trackNameEvent.last }
			{ ^nil };
		}
		
	instName { |track = 0|
		var trackNameEvent;
		trackNameEvent = metaEvents.select({ |event| (event[2] == \instName) and: 
			{ event[0] == track } }).first;
		if( trackNameEvent.notNil )
			{ ^trackNameEvent.last }
			{ ^nil };
		}
		
	endOfTrack { |track|
		^metaEvents.select({ |item| 
			( item[2] == \endOfTrack ) and:
				{ if( track.notNil ) { item[0] == track } { true } } });
		}
	
	length { ^(this.endOfTrack.sort({ |a, b| a[1] <= b[1] }).last ? [nil,0])[1]; }
	 // based on endOfTrack info
		
		
	tempoEvents {  ^metaEvents.select({ |item| item[2] == \tempo; });
		}
		
	tempi { ^metaEvents.select({ |item|
		item[2] == \tempo; }).collect({ |item| item[3] }); }
		
	tempoMap { // [ time, tempo ]
		^metaEvents.select({ |item|
		item[2] == \tempo; }).collect({ |item| item[[1,3]] });
		}
		
	tempoEnv { 
		// envelope for ticks -> seconds or seconds -> ticks (depending on timeMode)
		// to get the time at a tick or vv simply call .at( ... ) to this env
		// this is also used internally for \ticks <-> \seconds and time <-> measure conversion
		
		var map, times, timesDelta, prevTime = 0, tempi, seconds, ticks;
		var prevSeconds = 0, prevTicks = 0, lastTime;
		map = this.tempoMap;
		if( map.size == 0 )
			{ map = [ [0, tempo] ]; };
		lastTime = this.length;
		#times, tempi = map.flop;
		times = times ++ [ lastTime ];
		
		timesDelta = Array.fill( times.size - 1, { |i|
			var out;
			out = times[i+1] - prevTime;
			prevTime = times[i+1];
			out;
			});
		
		if( timeMode == \ticks )
			{ 
			seconds = [0] ++ Array.fill( times.size - 1, { |i|
				var out;
				out = prevSeconds + ( ( timesDelta[i] / division ) * (60 / tempi[i]) );
				prevSeconds = out;
				out; });
			^Env( seconds, timesDelta );
			} {
			ticks = [0] ++ Array.fill( times.size - 1, { |i|
				var out;
				out = prevTicks + ( ( timesDelta[i] * division ) / (60 / tempi[i]) );
				prevTicks = out;
				out; });
			^Env( ticks, timesDelta );
			};			
		
		}
	
	//// getting and converting events
			
	asDicts {  // all events
		^midiEvents.collect({ |item|
			var event;
			event = ( 
				track: item[0],
				absTime: item[1],
				type: item[2],
				channel: item[3],
				val1: item[4],
				val2: item[5] );
			if( [\noteOn, \noteOff].includes( event.type ) )
				{ event.put( \note, item[4] ); event.put( \velo, item[5] ); };
			if( \cc == event.type )
				{ event.put( \cc, item[4] ); event.put( \val, item[5] ); };
			event;
			});			
		}
		
	asNoteDicts { |channel, track|  // note events on specific channel/track (all ch/tracks if nil)
		^this.noteSustainEvents( channel, track ).collect({ |item|
			( 	track: item[0],
				absTime: item[1],
				type: item[2], 
				channel: item[3],
				note: item[4],
				velo: item[5],
				dur: item[6],
				upVelo: item[7] )
			});			
		}
	
///////////  adding events
	
	addMIDIEvent { |event, sort = true|   /// must have correct format
		event = event ? [0, 0, \noteOn, 0, 64, 64];  
		midiEvents = midiEvents.add( event );
		if(sort) { this.sortMIDIEvents; }
		}
		
	addAllMIDIEvents { |events, sort = true|
		events.do({ |event| this.addMIDIEvent( event, false ); });
		if(sort) { this.sortMIDIEvents; };
		}
		
	addMIDIEventToTrack { |event, track = 0, sort = true| // [absTime, type, channel, ... data]
		event = event ? [0, \noteOn, 0, 64, 64];
		this.addMIDIEvent( [track] ++ event, sort );
		}
	
	addAllMIDIEventsToTrack { |events, track = 0, sort = true|
		events.do({ |event| this.addMIDIEventToTrack( event, track, false ); });
		if(sort) { this.sortMIDIEvents; };
		}
		
	addMIDITypeEvent { |type = \cc, channel = 0, args, absTime = 0, track = 0, sort=true|
		args = args ? [7, 127];
		this.addMIDIEvent( [track, absTime, type, channel] ++ args, sort )
		}
	
	addAllMIDITypeEvents { |type = \cc, channel = 0, args, absTime, track = 0, sort=true|
		args = args ? [ [7, 127], [7, 64], [7,127] ];
		absTime = absTime ? [0, 0.5, 1];
		 args.do({ |argsItem, i|
		 	this.addMIDIEvent( [ track, absTime.wrapAt(i), type, channel ] ++ argsItem, false )
		 	});
		if(sort) { this.sortMIDIEvents; };
		}
		 
	addMetaEvent { |event, sort = true|
		event = event ? [0, 0, \text, "added text"];  
		metaEvents = metaEvents.add( event );
		if(sort) { this.sortMetaEvents; }
		}

	addNote { |noteNumber = 64, velo = 64, startTime = 0, dur = 1, upVelo, channel=0, track=0, 
				sort=true|
		upVelo = upVelo ? velo;
		this.addMIDIEvent( [ track, startTime, \noteOn, channel, noteNumber, velo ], false );
		this.addMIDIEvent( [ track, startTime + dur, \noteOff, channel, noteNumber, upVelo ], 
			sort );
		}
		
	addCC { |cc = 7, val = 127, startTime = 0, channel = 0, track = 0|
		this.addMIDIEvent( [ track, startTime, \cc, channel, cc, val ] );
		}
		
	addTrack { |name|
		this.adjustTracks;
		this.addMetaEvent( [ tracks, 0, \trackName, name ? ("track " ++ (tracks + 1)) ] );
		tracks = tracks + 1;
		}
		
	addTimeSignature { |div = 4, denom = 4, time = 0, sort = true, removeOld = true| 
		this.removeTimeSignature( time, removeOld );
		^this.addMetaEvent( [0, time, \timeSignature, [div, denom.log2.round.asInteger, 24, 8 ]], sort );
		}
		
	addTimeSignatureString { |string = "4/4", time = 0, sort = true, removeOld = true|
		string = string.split( $/ ).collect( _.interpret );
		^this.addTimeSignature( string[0], string[1], time, sort, removeOld );
		}
	
	addTempo { |tempo = 120, time = 0, sort = true, removeOld = true| 
		// please note that adding a tempo in \seconds timeMode will change the
		// timing in ticks; all events remain at their absolute time in the current
		// timeMode.
		this.removeTempo( time, removeOld );
		^this.addMetaEvent( [0, time, \tempo, tempo], sort );
		}
		
/////////// changing / removing events
		
	setTrackName { |name, track = 0|   // remove old name
		var oldName;
		name = name ?? { "track " ++ track };
		this.removeMetaEvents( \trackName, nil, track );
		this.addMetaEvent( [track, 0, \trackName, name.asString] );
		}
	
	setInstName { |name, track = 0|
		var oldName;
		name = name ?? { "inst " ++ track };
		this.removeMetaEvents( \instName, nil, track );
		this.addMetaEvent( [track, 0, \instName, name.asString] );
		}
	
	shiftTime { |deltaTime|  // by default shifts first note to start
		deltaTime = deltaTime ?? { this.firstNote[1].neg };
		midiEvents = midiEvents.collect({ |event|
			[event[0]] ++ [( event[1] + deltaTime ).max(0)] ++ event[2..];
			});	
		metaEvents = metaEvents.collect({ |event|
			[event[0]] ++ [( event[1] + deltaTime ).max(0)] ++ event[2..];
			});
		}
		
	correctTempoEvents { |removeDuplicates = true, removeDoubles = true|
		var tempoEvents, analyzed, lastTempo, toDelete = [];
		tempoEvents = this.tempoEvents;
		
		// duplicates:
		if( removeDuplicates )
			{ 	analyzed = ();
				tempoEvents.do({ |event|
					if( analyzed[ event[1] ].isNil )
						{	analyzed[ event[1] ] = event; }
						{ 	toDelete = toDelete.add( analyzed[ event[1] ] );
							analyzed[ event[1] ] = event; };
					});
				toDelete.do({ |item| tempoEvents.remove( item ); }); // remove them from temp
			};
			
		// doubles: (2 times the same tempo ; no change)
		if( removeDoubles )
			{ 	tempoEvents.do({ |event|
					if( event[3] == lastTempo )
						{	toDelete = toDelete.add( analyzed[ event[1] ] ); }
						{	lastTempo = event[3]; };
					});
			};
		
		^toDelete.do({ |item| metaEvents.remove( item ) }); // return removed items
		}
		
	removeMetaEvents { |type = \tempo, time = nil, track = 0|
		var oldEvents;
		oldEvents = metaEvents.select({ |item|
			((item[2] == type) &&
			(item[0] == track)) &&
			(	if( time.notNil )
					{  item[1] == time; }
					{ true; } ) });
		if( oldEvents.size != 0 )
			{ oldEvents.do({ |item| metaEvents.remove( item ); }); };
		^oldEvents;
		}
		
	removeTimeSignature { |time = 0, doIt = true| 
		if( doIt )
			{ ^this.removeMetaEvents( \timeSignature, time, 0 ); }
			{ ^[]; };
		}
	
	removeTempo { |time = 0, doIt = true| 
		if( doIt )
			{ ^this.removeMetaEvents( \tempo, time, 0 ); }
			{ ^[]; };
		}
		
	/// endOfTrack correction
		
	testEndOfTrack { |track|
		var eot, lastEvent;
		if( track.isNil )
			{ ^Array.fill( tracks, { |i| this.testEndOfTrack(i) }); }
			{ eot = this.endOfTrack( track )[0];
			lastEvent = midiEvents.select({ |item| item[0] == track }).last ? [0,0];
			if( eot.notNil )
				{ ^(lastEvent[1] <= eot[1]) }
				{ ^false }
			}
		}
		
	prAdjustEndOfTrack { |track = 0, wait = 0|
		var lastEvent;
		metaEvents.remove( this.endOfTrack( track )[0] );
		lastEvent = midiEvents.select({ |item| item[0] == track }).last ? [0,0];
		this.addMetaEvent( [ track, lastEvent[1] + wait, \endOfTrack ] );
		}
		
	adjustEndOfTrack { |track, wait = 0|
		this.testEndOfTrack( track ).do({ |item, i|
			if( item.not ) { this.prAdjustEndOfTrack( track ? i, wait ) }
			});
		}
		
//////// measure support (no timeSignature map yet; only the first timeSignature is used)
    
    	/////  time -> measure
	
	beatAtTime { |time = 0|
		if( timeMode == \ticks )
			{ ^(time / division)  }
			{ ^(this.tempoEnv[ time ] / division) };
		}
		
	tempoAtTime { |time = 0|
		var tempoMap, outTempo;
		tempoMap = this.tempoMap;
		tempoMap = tempoMap.select({ |item| item[0] <= time });
		outTempo = tempoMap.last ? [0, tempo]; // last tempo change before time
		^outTempo[1];		
		}
	
	timeSignatureAsArray { 
		var ts;
		ts = this.timeSignatures[0] ? [0, "4/4"];
		^ts[1].split( $/ ).collect( _.interpret );
		}
		
	rawMeasureAtBeat { |beat = 0|  // a beat is always 1/4 note
		var ts;
		ts = this.timeSignatureAsArray;
		^( (beat / ts[0]) * (ts[1] / 4) ) + 1; // first measure is always 1, not 0
		}		
		
	measureAtBeat { |beat = 0, measureFormat|  // a beat is always 1/4 note
		var ts, measure, out;
		ts = this.timeSignatureAsArray;
		measureFormat = measureFormat ? [ts[1], ts[1]*4]; // [4,16] for "4/4"
		measure = this.rawMeasureAtBeat( beat );
		out = [measure.copy.floor]; 
		measure = measure - out[0];
		measureFormat.do({ |subFormat|
			subFormat = subFormat * (ts[0] / ts[1]);
			out = out ++ [ ( (measure + (1.0e-15)) * subFormat).floor.max(0) ];
			measure = measure - ( out.last / subFormat );
			});
		^out ++ [ measure.round(1.0e-15) * ( measureFormat.last * (ts[0] / ts[1]) )];
		}
		
	measureAtTime { |time = 0, measureFormat|
		^this.measureAtBeat( this.beatAtTime( time ), measureFormat );
		}
		
	rawMeasureAtTime { |time = 0|  // a beat is always 1/4 note
		^this.rawMeasureAtBeat( this.beatAtTime( time ) );
		
		}
	
	// the other way round
	
	beatAtRawMeasure { |rawMeasure = 0|
		var ts;
		ts = this.timeSignatureAsArray;
		^( (rawMeasure - 1) * ts[0]) / (ts[1] / 4);
		}
	
	beatAtMeasure { |measure, measureFormat|
		var ts, rawMeasure;
		measure = measure ? [1,0,0,0];
		ts = this.timeSignatureAsArray;
		measureFormat = measureFormat ? [ts[1], ts[1]*4]; // [4,16] for "4/4"
		rawMeasure = measure.first;
		measureFormat.do({ |subFormat, i|
			subFormat = subFormat * (ts[0] / ts[1]);
			rawMeasure = rawMeasure + (measure[i+1] / subFormat);
			});
		rawMeasure = rawMeasure + ( 
			( measure[ measureFormat.size + 1 ] ? 0 ) / 
				( measureFormat.last * (ts[0] / ts[1]) ); );
		^this.beatAtRawMeasure( rawMeasure );
		}
	
	timeAtBeat { |beat = 0|
		if( timeMode == \ticks )
			{ ^(beat * division)  }
			{ ^this.tempoEnv[ (beat * division) ]; };
		}
		
	timeAtMeasure { |measure, measureFormat|
		^this.timeAtBeat( this.beatAtMeasure( measure, measureFormat ) );
		}
		
	timeAtRawMeasure { |rawMeasure = 0|
		^this.timeAtBeat( this.beatAtRawMeasure( rawMeasure ) );
		}
		

////////// info / report

	analyzeTypes { // return all MIDI event types and their number of ocurrences in this file
		var types;
		types = ();
		midiEvents.do({ |event|
			if( types.keys.includes( event[2] ) )
				{ types[ event[2] ] = types[ event[2] ] + 1; }
				{ types.put( event[2], 1 ) }
			});
		^types;
		}
		
	analyzeMetaTypes { // return all MIDI event types and their number of ocurrences in this file
		var types;
		types = ();
		metaEvents.do({ |event|
			if( types.keys.includes( event[2] ) )
				{ types[ event[2] ] = types[ event[2] ] + 1; }
				{ types.put( event[2], 1 ) }
			});
		^types;
		}
		
	analyzeChannels { |type| 
		// return all MIDI event types and their number of ocurrences  per channel in this file
		var channels;
		var types;
		if( type.notNil )
			{ 	channels = ();
				midiEvents.do({ |event|
					if( event[2] == type )
						{ if( channels.keys.includes( event[3] ) )
							{ channels[ event[3] ] = channels[ event[3] ] + 1; }
							{ channels.put( event[3], 1 ) }
						}
				});
				^channels;
			}
			{ types = this.analyzeTypes;
				types.keys.do({ |key| types[key] = this.analyzeChannels( key ) });
				^types;
			}
		}
		
	usedChannels { arg track; // array of channel numbers used by midi events
		var tempEvents, usedChannels = [];
		
		if( track.isNil )
			{ tempEvents = midiEvents }
			{ tempEvents = midiEvents.select({ |event| event[0] == track }); };
		
		tempEvents.do({ |event|
				if( usedChannels.includes( event[3] ).not)
					{ usedChannels = usedChannels.add( event[3] );  };
				});
			
		^usedChannels.sort;
		}
		
	usedTracks { arg channel; // array of track numbers used by midi events
		var tempEvents, usedTracks = [];
		
		if( channel.isNil )
			{ tempEvents = midiEvents }
			{ tempEvents = midiEvents.select({ |event| event[3] == channel }); };
		
		tempEvents.do({ |event|
				if( usedTracks.includes( event[0] ).not)
					{ usedTracks = usedTracks.add( event[0] );  };
				});
			
		^usedTracks.sort;
		}
		
	analyzeChannel { |channel = 0, track| 
			// array containing arrays of the following format:
			// [ type, ccNumber, minVal, maxVal ] -> ccNumber only if type == cc, else nil
		var tempEvents, outArray = [];
		if( track.isNil )
			{ tempEvents = midiEvents.select({ |event| event[3] == channel });  }
			{ tempEvents = midiEvents.select({ |event| 
				(event[0] == track) && { event[3] == channel } }); 
			};
			
		[ \noteOn, \pitchBend, \afterTouch ].do({ |type, i|
		
			tempEvents.do({ |event|
				var index;
				if( event[2] == type )
					{ 
						if( outArray.select({ |item| item[0] == type }).size == 0 )
							{ outArray = outArray.add( [ type, nil, event[4], event[4] ] ) };
							
						index = outArray.detectIndex({  |item| item[0] == type });
						
						outArray[ index ] = 
							[ type, nil, 
								outArray[ index ][2].min( event[4] ),  
								outArray[ index ][3].max( event[4] ) ];
					};
				});
			
			});
		
		tempEvents.do({ |event|
				var index;
				if( event[2] == 'cc' )
					{ 
						if( outArray.select({ |item| 
							(item[0] == 'cc') && { item[1] == event[4] }
							 }).size == 0 )
							{ outArray = outArray.add( [ 'cc', event[4], event[5], event[5] ] ) };
							
						index = outArray.detectIndex({ |item| 
							(item[0] == 'cc') && { item[1] == event[4] }
							 });
						
						outArray[ index ] = 
							[ 'cc', event[4], 
								outArray[ index ][2].min( event[5] ),  
								outArray[ index ][3].max( event[5] ) ];
					};
				});
		
		^outArray;
			
		}
		
	analyzeUsedChannels { |track|
		^this.usedChannels.collect({ |channel|
			[ channel, this.analyzeChannel( channel, track ) ]
			});
		}
	
	analyzeUsedTracks {
		^this.usedTracks.collect({ |track|
			[ track ] ++ this.analyzeUsedChannels( track );
			});
		}
	
	analyzeUsedEvents {
		var outArray = [];
		
		// format: [ track, channel, type, nil/ccNumber, minVal, maxVal ]
		
		this.usedTracks.do({ |track|
			var subArray;
			subArray = this.analyzeUsedChannels( track );
			subArray.do({ |channelEvent|
				
				channelEvent[1..].do({ |midiEvents|
					
					midiEvents.do({ |midiEvent|
						outArray = outArray.add( [ track, channelEvent[0] ] ++ midiEvent );
						});
					
					});
				});
			});
			
		^outArray;
		}
		
	analyzeTracks { |type|
		// return all MIDI event types and their number of ocurrences  per track in this file
		var trx;
		var types;
		if( type.notNil )
			{ 	trx = ();
				midiEvents.do({ |event|
					if( event[2] == type )
						{ if( trx.keys.includes( event[0] ) )
							{ trx[ event[0] ] = trx[ event[0] ] + 1; }
							{ trx.put( event[0], 1 ) }
						}
				});
				^trx;
			}
			{ types = this.analyzeTypes;
				types.keys.do({ |key| types[key] = this.analyzeTracks( key ) });
				^types;
			}
		}

	
	analyzeCC { // return used cc channels and numbers in this file
		var ccChannels;
		ccChannels = this.analyzeChannels('cc');
		ccChannels.keys.do({ |channel|
			var ccNrs;
			ccNrs = ();
			midiEvents.do({ |event|
				if( ( event[2] == 'cc' ) and: ( event[3] == channel ) )
					{  if( ccNrs.keys.includes( event[4] ) )
							{ ccNrs[ event[4] ] = ccNrs[ event[4] ] + 1; }
							{ ccNrs.put( event[4], 1 ) }
						 }
					});
			ccChannels[channel] = ccNrs;
			});
		^ccChannels;
		}
		
	analyzeCCTracks { // return used cc channels and numbers per track in this file
		var ccChannels;
		ccChannels = this.analyzeTracks('cc');
		ccChannels.keys.do({ |channel|
			var ccNrs;
			ccNrs = ();
			midiEvents.do({ |event|
				if( ( event[2] == 'cc' ) and: ( event[0] == channel ) )
					{  if( ccNrs.keys.includes( event[4] ) )
							{ ccNrs[ event[4] ] = ccNrs[ event[4] ] + 1; }
							{ ccNrs.put( event[4], 1 ) }
						 }
					});
			ccChannels[channel] = ccNrs;
			});
		^ccChannels;
		}
		
	countMIDIEvents { |type = \noteOn, track, channel|
		var counter = 0;
		midiEvents.do({ |item|
			if ( ( (item[2] == type) and:
			{ if( track.notNil )
				{ item[0] == track } { true } } ) and:
			{ if( channel.notNil )
				{ item[3] == channel } { true } } )
				{ counter = counter + 1; };
			});
		^counter;
		}
		
	info {
		Post << "'" << pathName.basename << "'" << nl;
		Post << "\tTempo (first): " << tempo << nl;
		Post << "\tTime Signature (first): " << 
			(this.timeSignatures.first ? [0,0,"4/4"])[2] << nl;
		Post << "\tDivision: " << division << nl;
		Post << "\tFormat: " << format << nl;
		tracks.do({ |track, i|
			Post << "\tTrack " << i << " ( " << this.trackName(i) << " ):" << nl;
			Post << "\t\tmidi events: " << midiEvents.select({ |a| a[0] == i }).size << " ( ";
			Post << this.copy.convertNoteOns.countMIDIEvents( \noteOn, i ) << " noteOns )" << nl;
			Post << "\t\tmeta events: " << metaEvents.select({ |a| a[0] == i }).size << nl;
			});
		}
}