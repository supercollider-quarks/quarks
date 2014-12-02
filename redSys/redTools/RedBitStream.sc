//redFrik

RedBitStream : Stream {
	var <>collection, byte, <pos= 0;
	*new {|collection, byte= 8|
		^super.newCopyArgs(collection, byte);
	}
	reset {
		pos= 0;
		collection= collection.extend(0);
	}
	pos_ {|argPos|
		pos= argPos.clip(0, collection.size*byte);
	}
	peek {
		^this.peekByte.rightShift(pos%byte)&1;
	}
	next {
		var bit;
		if(this.bytePos>=collection.size, {
			^nil;
		}, {
			bit= this.peek;
			pos= pos+1;
			^bit;
		});
	}
	contents {
		^this.class.new(collection, byte).nextN(collection.size*byte);
	}
	put {|item|
		if(this.pos>=(collection.size*byte), {
			pos= collection.size*byte+1;
			collection= collection.add(0);
		});
		this.poke(item);
		pos= pos+1;
	}
	
	//--
	poke {|bit|
		collection.put(this.bytePos, this.peekByte.setBit(pos%byte, bit!=0));
	}
	bytePos {
		^pos.div(byte);
	}
	bytePos_ {|argBytePos|
		pos= argBytePos.clip(0, collection.size)*byte;
	}
	peekByte {
		^collection[this.bytePos];
	}
	nextByte {
		^this.nextN(byte);
	}
	putByte {|item|
		if(this.bytePos>=collection.size, {
			this.bytePos= collection.size+1;
			collection= collection.add(item);
		}, {
			collection.put(this.bytePos, item);
			this.bytePos= this.bytePos+1;
		});
	}
}
RedBitStream2 : RedBitStream {
	peek {
		^this.peekByte.rightShift(byte-1-(pos%byte))&1;
	}
	poke {|bit|
		collection.put(this.bytePos, this.peekByte.setBit(byte-1-(pos%byte), bit!=0));
	}
}
