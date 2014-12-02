//redFrik

RedDestroyer {
	*ar {|in= 0, thresh= 1, lag= 0.01|
		^in*(Lag.ar(in.abs, lag)<thresh);
	}
	*kr {|in= 0, thresh= 1, lag= 0.01|
		^in*(Lag.kr(in.abs, lag)<thresh);
	}
}
