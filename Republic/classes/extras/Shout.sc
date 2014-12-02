
Shout {
	classvar <win, <txtView, <>tag="//!!";
	classvar <>width=1250, <shouts, <codeDumpFunc;

	classvar <>rect;

	*initClass {
		shouts = List.new;
		codeDumpFunc = { |str| if (str.beginsWith(Shout.tag)) { Shout(str.drop(Shout.tag.size)) } };
		rect = Rect(0, 0, 1024, 80);
	}

	*makeWin { |message="Shout this!"|

		win = Window("Shout'er", rect).front;
		win.alpha_(0.7);
		win.view.background_(Color.clear);
		win.alwaysOnTop_(true);

		txtView = TextView(win, win.bounds.moveTo(0,0));
		txtView.background_(Color.clear);
		txtView.font_(Font("Monaco", 32));
		txtView.resize_(2);

		width = rect.width;

		this.setMessage(message);
	}

		// simple versions of methods, for sc-book chapter
//	*setMessage { |message|
//		txtView.string_(message.asString)
//	}

//	*new { |message="�Shout'er!"|
//		shouts.add(message);
//
//		if (win.isNil or: { win.isClosed }) {
//			this.makeWin(message);
//		} {
//			this.setMessage(message);
//		};
//	}

	*new { |message="�Shout'er!"|
		var currDoc;
		shouts.add(message);

		if (win.isNil or: { win.isClosed }) {
			currDoc = Document.current;
			Task {
				this.makeWin(message);
				0.1.wait;
				currDoc.front;
			}.play(AppClock);
		} {
			this.setMessage(message);
		};
	}

	*setMessage { |message|
		var messSize, fontSize;
		messSize = message.size;

		defer {
			fontSize = (1.64 * txtView.bounds.width) / max(messSize, 32);
			txtView.font_(Font("Monaco", fontSize))
				.string_(message.asString);
		};
		this.animate;
	}

	*animate { |dt=0.2, n=12|
		var colors = [Color.red, Color.green, Color.blue];
		Task {
			n.do { |i|
				try {
					txtView.stringColor_(colors.wrapAt(i));
					dt.wait
				}
			};
			try { txtView.stringColor_(Color.red) }; // make sure we end red
		}.play(AppClock);
	}

	*add { var interp = thisProcess.interpreter;
		interp.codeDump = interp.codeDump.addFunc(codeDumpFunc);
	}
	*remove { var interp = thisProcess.interpreter;
		interp.codeDump = interp.codeDump.removeFunc(codeDumpFunc);
	}

}
/* Tests
Shout("We should consider stopping...me fkjdfgkfjdgkjfdhkgjf")
Shout("We should consider stopping...")
Shout("�Hey Hey Hey Na na na!");

Shout.add;
//!! does this show up
Shout.remove;
//!! does this show up

// 	for pbup setup use, add to Oscresponder(nil, '/share' ...) :
	if (str.beginsWith(Shout.tag)) { Shout(str.drop(Shout.tag.size) + "-" ++ msg[1]) };

*/
