
	// a gui for displaying status messages on a gui window

	// I added some hacks to String-post and -postln, which slows down things just slightly
	// but it's worth it to have the flexibility of displaying main output on the gui

	// primitive output and server messaging is not displayed

StatusBox : SCViewHolder {
	classvar	<default;
	var	<>display,
		strToPost = "", notScheduled = true;

	*initClass {
		// actually I'm not sure this is *really* necessary,
		// since all classvars are destroyed on shutdown anyway,
		// but it was in the old code so... eh...
		ShutDown.add {
			StatusBox.clearDefault;
		}
	}

	*new { arg argParent, argBounds;
		^super.new.init(argParent, argBounds)
	}

	init { |argParent, argBounds|
		view = GUI.textView.new(argParent, argBounds)
			.hasVerticalScroller_(true)
			.hasHorizontalScroller_(true)
			.onClose_({ this.remove });
		default.isNil.if({ this.makeDefault });
	}

	remove {
		(default === this).if({ default = nil });	// forget about me if I'm going away
		view.notClosed.if({ super.remove; });
	}

		// this is necessary to make sure lines post in the right order
	startPostThread {
		notScheduled.if({
			AppClock.sched(0, {
				try {
					view.notClosed.if({
						view.string_(view.string ++ strToPost);
						view.select(view.string.size - 2, 0);
						strToPost = "";
					});
				} { |error|
					error.isKindOf(Error).if({
						"\nError while posting to a StatusBox:\n".prPost;
						error.errorString.prPostln;
						strToPost = "";
					});
				};
				notScheduled = true;
				nil
			});
			notScheduled = false;
		});

	}

	post { arg str;
		strToPost = strToPost ++ str.asString;
		this.startPostThread;
	}

	postln { arg str;
		this.post(str.asString ++ "\n");
	}

	makeDefault {
		default = this;
	}

	*clearDefault { default = nil; }

	*post { |str|
		default.notNil.if({ default.post(str) });
	}

	*postln { |str|
		default.notNil.if({ default.postln(str) });
	}
}
