+ Object {

	deepAt1 { |...args|
		^args.inject(this, {|state,x|
			state.at(x)
		})
	}
}