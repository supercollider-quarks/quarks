
PolyTouchResponder : MIDIResponder {
	classvar <ptrinit = false,<ptrs;

	*new { arg function, src, chan, num, veloc, install=true,swallowEvent=false;
		^super.new.function_(function)
			.matchEvent_(MIDIEvent(nil, src, chan, num, veloc))
			.swallowEvent_(swallowEvent)
			.init(install)
	}
	*initialized { ^ptrinit }
	*responders { ^ptrs }
	*init {
		if(MIDIClient.initialized.not,{ MIDIIn.connectAll });
		ptrinit = true;
		ptrs = [];
		MIDIIn.polytouch = { arg src, chan, note, touch;
			ptrs.any({ arg r;
				r.respond(src,chan,note,touch)
			});
		}
	}
	*add { arg resp;
		ptrs = ptrs.add(resp);
	}
	*remove { arg resp;
		ptrs.remove(resp);
	}
}

