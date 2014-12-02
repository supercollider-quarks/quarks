

+ ArrayedCollection {

	toBuffer { arg buffer;
		var numChannels, numFrames, coll;
		if(this.at(0).isArray) {
			numChannels = this.size;
			numFrames = this[0].size;
			coll = this.lace;
		} {
			numChannels = 1;
			numFrames = this.size;
			coll = this;
		};
//		buffer.server.makeBundle(nil, {
			buffer.numFrames_(numFrames).numChannels_(numChannels).alloc(buffer.setnMsg(0, coll));
			// buffer.sendCollection(this);
			// doesn't work yet, because of the way makeBundle works.
//		});
	}

}

+ SimpleNumber {
	toBuffer { arg buffer;
		this.asArray.toBuffer(buffer)
	}
}

+ String {
	toBuffer { arg buffer;
		buffer.allocRead(this)
	}
}

+ Nil {
	toBuffer { arg buffer;
		buffer.numFrames_(0).numChannels_(1).alloc
	}

}