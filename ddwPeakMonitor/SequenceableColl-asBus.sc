// [0,1].asBus -> Bus.new(\audio, 0, 1, Server.local)

+ SequenceableCollection {
	asBus {
			// array should be index, numchan, server, rate
		^Bus.new(this.at(3) ? \audio, this.at(0) ? 0, this.at(1) ? 2, this.at(2) ? Server.default)
	}
}
