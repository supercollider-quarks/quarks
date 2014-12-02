
VPTrigSocket : AbstractMIDISocket {
		// responds to noteOn by doing the action of a VoicerProcess
		// this class stores a number of processes

	var	<resp,	// IdentityDictionary[ note num -> VPNoteInfo ]
		thisNote;		// for adding -- index must be retained between add calls
	
		// args are given in pairs: processgroup, starting note# or [note #'s]
		// the destination trick from BasicMIDISocket is used to skip the destination arg.
	init { arg ... args;
		args = [destination] ++ args.flat;	// so that pairs are intact within args
		destination = this;
		
		resp = IdentityDictionary.new;
		
		this.add(args);
	}
	
	add { arg ... args;	// add the pairs to the responder dict
		var	warning = false,
			noteStream, notes;
		args.flat.clump(2).do({ arg a;
			notes = a.at(1);
			notes.isInteger.if({ notes = [notes] });
				// set up stream to read array or return one value then nil.......
			noteStream = Pseq(notes ? [(thisNote ? 59) + 1], 1).asStream;

			a.at(0).processes.do({ arg p, i;
				thisNote = noteStream.next ?? { thisNote + 1 };  // get next from array or +1
				resp.at(thisNote).notNil.if({	// was there already something here?
					warning = true;
					resp.removeAt(thisNote);		// remove old value
				});
				resp.put(thisNote, VPNoteInfo(p, a.at(0), i));
				p.midiNote = thisNote.asMIDINote;
			});
			a.at(0).updateView;	// fix items in view (button or menu)
		});
		warning.if({ "VPTrigSocket-add: collision. Some old responders may be gone now.".warn });
	}
	
	removeAt { arg ... ind;
		ind = ind.flat;
		ind.sort({ arg a, b; a > b });	// need reverse order
		^ind.collect({ arg i;
			resp.group.clearMIDINotes;
			resp.removeAt(i)
		});	// return removed items
	}
	
	remove { arg ... pg;	// remove one or more processgroups
		pg = pg.flat.collect({ arg p; resp.findKeyForValue(p) });  // collect indices
		^this.removeAt(pg);
	}
	
	clear {
		resp = IdentityDictionary.new;	// garbage responders
	}
	
	noteOn { arg num;
		var	r;
		(r = resp.at(num)).notNil.if({	// should only try to do it if there's something to do
			(r.group.value == r.index).if({	// if we're hearing the same note from midi twice,
				r.process.doAction(r.group);	// fire the process
			}, {
				r.group.value_(r.index);  // else set value and wait for a second note to confirm
			});
		});
	}
	
	noteOff {}
	
	active { ^(resp.size > 0) }

		//////////////// PRINTING & GUI SUPPORT ////////////////

	postStatus {	// list all midi note nums & process names in the output window
		("MIDI source: " ++ parent.channel.asString).postln;
		this.asKeyArray.do({ arg a;
			"\t".post; a.at(0).asMIDINote.post; ": ".post; a.at(1).process.tag.postln;
		});
	}
		
	asKeyArray {
		^resp.asSortedArray
	}
}

VPNoteInfo {
	var	<process, <group, <index;
	
	*new { arg process, group, index; ^super.newCopyArgs(process, group, index) }
	
}