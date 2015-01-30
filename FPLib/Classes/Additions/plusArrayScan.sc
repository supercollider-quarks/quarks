+ Array {

	scan { |init,f|
		^this.inject([init], { |state,x|
			state.addI( f.(state.last,x) )
		})
	}

	//mapAccumL :: (acc -> x -> (acc, y)) -> acc -> [x] -> (acc, [y])
	// (0..9).collectAccum(0,{ |state,x| T(state+x,x*state) })
	collectAccum { |init, f|
		^this.inject(T(init,[]), { |state,x|
			var at1 = state.at1;
			var at2 = state.at2;
			var y = f.(state.at1, x);
			T(y.at1, state.at2.add(y.at2) )
		})
	}
}

