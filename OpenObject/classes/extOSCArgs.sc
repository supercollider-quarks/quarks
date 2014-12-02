
// arrayed osc argument support
// counterpart to asOSCArgBundle, asOSCArgArray

+ SequenceableCollection {

	unfoldOSCArrays {
		var stack, current, prev;

		this.do { |item|
			if(item === $[ /*]*/) {
				current !? { stack = stack.add(current) };
				current = nil;
			} {
				if(item === /*[*/ $]) {
					prev = stack.pop;
					prev = prev.add(current);
					current = prev;
				} {
					current = current.add(item);
				}
			}
		};

		// empty stack, embed unclosed brackets
		stack.reverseDo { |prev|
				prev = prev.add(current);
				current = prev;
		};

		^current

	}

	// for convenience and clarity.

	foldOSC {
		^this.asOSCArgArray
	}

	unfoldOSC {
		^this.unfoldOSCArrays
	}

}

