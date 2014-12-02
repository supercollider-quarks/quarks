/* Controller.sc - (c) rohan drape, 2004-2007 */

Controller {
	
	var <ctl, <size ;
	
	*new {
		arg s, size = 256;
		var ctl;
		ctl = Array.newClear(size);
		size.do ({
			arg index;
			ctl.put(index, Ctl.new(s, index));
		});
		OSCresponderNode.new(s.addr, "/c_set", {
			arg time, responder, message;
			var index, value;
			index = message.at(1);
			value = message.at(2);
			ctl.at(index).value = value;
		}).add;
		^super.newCopyArgs(ctl, size);
	}
	
	*newP {
		arg ctl,size;
		^super.newCopyArgs(ctl,size);
	}
	
	derive {
		arg n;
		^Controller.newP(ctl.copyRange(n,size-n), size-n);
	}
	
	midiInitialize {
		if(MIDIClient.initialized.not,{
			MIDIClient.init;
		});
		MIDIClient.sources.do({
			arg endPoint;
			MIDIIn.connect(0, endPoint);
		});
		MIDIIn.control = {
			arg source, channel, number, value;
			var index;
			index = (channel * 127) + number ;
			ctl.at(index).internal = value / 127.0;
		}
	}
	
	at {
		arg i;
		^ctl.at(i);
	}
	
	copySeries {
		arg first, second, last;
		^ctl.copySeries(first, second, last);
	}

	snapshot {
		arg left, right;
		^(left..right).collect({
			arg index;
			[index, this.at(index).value];});
	}

	restore {
		arg snapshot;
		snapshot.do({arg slot; this.at(slot.at(0)).value = slot.at(1);});
	}

	update {
		arg left=0, right=size-1;
		(left..right).do({arg n; this.at(n).update;});
	}
}

