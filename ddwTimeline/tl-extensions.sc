// h. james harkins - jamshark70@dewdrop-world.net
// not ready for prime time!

+ Object {
	asTLItem { ^this.value }
}

+ SequenceableCollection {
	asTLItem {
		if(this.size <= 3 and: { this.detect({ |item| item.isNumber.not }).isNil }) {
			^this.asQuant.asTLItem
		} {
			^this
		}
	}
}

+ Quant {
	asTLItem {
		^this.nextTimeOnGrid(thisThread.clock) - thisThread.clock.beats
	}
}

// MAKE SURE TO MOVE THIS LATER

+ Proto {
	asTLItem { ^this }
}


+ Symbol {
	asTLItem {
		if(PR.collection[this].value.notNil) {
			^PR(this).copy
		} {
			^this
		}
	}
}


+ NotificationCenter {
	*registrationsFor { |object, message, listener|
		var	collection = registrations; // Library.at(this);
		[object, message, listener, nil].do({ |match, i|
			if(collection.notNil and: { match.notNil }) {
				collection = collection[match];
			} { ^collection };
		});
		^nil
	}
}
