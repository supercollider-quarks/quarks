/*

KeyPlayer.all.clear;
g = KeyPlayerGui();
h = KeyPlayerGui(options: [\useList]);

"asdfg".do { |char| KeyPlayer(char.asSymbol) }
"hjkl".do { |char| KeyPlayer(char.asSymbol) }

g.object_(KeyPlayer(\f));
h.object_(g.object);

g.object.put($t, {"t!".postln });
g.object.put($x, {"x".postln }, both: true);
g.object.putUp($x, {"x up!".postln });
g.object.putUp($x, {"x up!".postln }, both: true);

g.object.putUp($y, {"y up only!".postln }, both: true);

KeyPlayerGui.keyboard.join.size
options:
    attach little KeyLoopGui on the side?
    future: make whole gui with Pen for elegant rescaling?
   make window non-resizable?

*/


KeyPlayerGui : JITGui {

	classvar <>colors;
	classvar <>lineOffsets;
	classvar <>keyboard;
	classvar <>keyboardNoShift;
	classvar <>keyboardShift;

	var <buttons, <drags, <font, <listView, <lpGuiBut;
	var <>activeColor;
	var <dragZone;

	*initClass {

		colors = [
			Color(0.8, 0.8, 0.8, 0.5),	// normal - 	grey
			Color(0.8, 0.2, 0.2, 1),	// ctl		red
			Color(0.2, 0.8, 0.2, 1),	// shift		green
			Color(0.8, 0.8, 0.2, 1),	// alt		blue
			Color(0.2, 1, 1, 1),		// alt shift	blue+green - cyan
			Color(1, 1, 0.2, 1),		// ctl shift	red+green - yellow
			Color(1, 0.2, 1, 1),		// ctl alt	red+blue - violet
			Color(1, 1, 1, 1)			// ctl alt shift	red green blue - white
		];

		// these describe the keyboard to show;
				// horizontal offsets for keys.
		lineOffsets = #[42, 48, 57, 117];

				// these are the keys you normally see (US, big keyb.)
				// customise for german or other keyboard layouts.
		keyboard = #["`1234567890-=", "QWERTYUIOP[]\\", "ASDFGHJKL;'", "ZXCVBNM,./"];

			// NOT USED YET:
				// the maps I get on my PB, US keyb., no shift
		keyboardNoShift = #["1234567890-=", "qwertyuiop[]\\", "asdfghjkl;'", "`zxcvbnm,./"];
				// and shifted	- /* double-quote */ only there for syntax colorize.
		keyboardShift = #["!@#$%^&*()_+", "QWERTYUIOP{}", "ASDFGHJKL:\"|" /*"*/, "~ZXCVBNM<>?"];

	}

	winName { ^"KeyPlayer" + (try { object.key } ? "") }

			// these methods should be overridden in subclasses:
	setDefaults { |options|
		var minWidth, minHeight;
		if (options.includes(\useList)) {
			minWidth = 480; minHeight = 170;
		} {
			// if (options.includes(\butLeft)) {
			// 	//	minWidth = 540; minHeight = 170;
			// } {
				minWidth = 420; minHeight = 150;
				minHeight = minHeight +
				(numItems * skin.buttonHeight + skin.headHeight);
		// };
		};

		if (parent.isNil) {
			defPos = 10@260
		} {
			defPos = skin.margin;
		};
		minSize = minWidth @ minHeight;
		//	"KPGui2 - minSize: %\n".postf(minSize);
	}

	calcBounds {
		var defBounds;
		if(bounds.isKindOf(Rect)) {
			bounds.setExtent(
				max(bounds.width, minSize.x),
				max(bounds.height, minSize.y)

			);
			^this
		};

		defBounds = Rect.fromPoints(defPos, defPos + minSize + (skin.margin * 2));
		if (bounds.isNil) {
			bounds = defBounds;
			^this
		};

		if (bounds.isKindOf(Point)) {
			bounds = defBounds.setExtent(
				max(bounds.x, minSize.x),
				max(bounds.y, minSize.y));
		}
	}

	accepts { |obj| ^obj.isNil or: { obj.isKindOf(KeyPlayer) } }

	object_ { |obj|
		if(this.accepts(obj).not) { ^this };

		if (object.notNil) { object.deactivate; };

		object = obj;
		if (obj.notNil) {
			parent.asView.keyDownAction_(object.makeKeyDownAction);
			parent.asView.keyUpAction_(object.makeKeyUpAction);
			this.addAltFuncs;
			object.activate;
		};
	}

	// better in JITGui?
	front { if (hasWindow) { parent.front } }

	getState {
		// get all the state I need to know of the object I am watching
		var news = (
			kpKeys: KeyPlayer.all.keys.asArray.sort,
			object: object);

		if (object.notNil) {
			news.put(\downKeys, object.actions.keys);
			news.put(\upKeys, object.upActions.keys);
			if (object.rec.notNil) {
				news.put(\recIsOn, object.rec.isOn);
				news.put(\recIsPlaying, object.rec.task.isActive);
				news.put(\recLoop, object.rec.loop);
				news.put(\recListSize, object.rec.lists.size);
				news.put(\recCurrList, object.rec.list);
			};
		};
		^news
	}

	checkUpdate {
		var news = this.getState;

		if (news[\object] != prevState[\object] or:
			(news[\kpKeys] != prevState[\kpKeys])
		) {
			this.updateGlobal(news);
		};

		if (news[\object] != prevState[\object] or:
			(news[\kpKeys] != prevState[\kpKeys])
		) {
			this.updateGlobal(news);
		};

		this.updateDrags(news); // does the checking

		prevState = news;
	}

	updateGlobal { |news|
		var keys = news[\kpKeys];

		var myIndex = -1;
		if (object.notNil) { myIndex = keys.indexOf(object.key); };

			// if buttons == nil, nothing
		buttons.do { |b, i|
			var col = if (i == myIndex) { skin.onColor } { skin.offColor };
			var key = keys[i];
			var keyExists = keys[i].notNil;
			b.states_([[key ? "", Color.black, col]]).enabled_(keyExists);
		};
		
		lpGuiBut.enabled_(object.notNil and: { object.rec.notNil });

		listView !? { listView.items_(keys).value_(myIndex); };
	}

	updateDrags { |news|
		var downKeys = news[\downKeys];
		var upKeys = news[\upKeys];

		if (downKeys == prevState[\downKeys]
			and: { upKeys == prevState[\upKeys] }) {
			^this
		};
		downKeys = downKeys ? [];
		upKeys = upKeys ? [];

		drags.keysValuesDo { |uni, drag|
			var val = 0;
			if (downKeys.includes(uni)) { val = val + 1 };
			if (upKeys.includes(uni)) { val = val + 2 };
			drag.background_(colors[val]);
			[uni, val, colors[val]];
		};
		zone.refresh;
	}

	///// make all the view elements: //////

	makeViews { |options|
		zone.addFlowLayout; // use a flat one
		// seethru!
		parent.asView.background_(Color(0.5, 0.5, 0.5, 0.25));
		zone.background_(Color(0.5, 0.5, 0.5, 0.25));

		if (options.includes(\useList)) {
			this.makeListView;
			zone.decorator.nextLine;
			zone.decorator.bounds = zone.bounds.left_(64);
			zone.decorator.top_(zone.bounds.top);
			dragZone = CompositeView(zone, zone.bounds);
		} {
			if (options.includes(\butLeft)) {
				this.makeButtonsLeft;
				zone.decorator.bounds = zone.bounds.left_(150);
				zone.decorator.top_(zone.bounds.top);
				dragZone = CompositeView(zone,
					zone.bounds.resizeBy(-10, 0));
			} {
				this.makeButtons;
				dragZone = CompositeView(zone, zone.bounds);
			};
			// this.miniLoopCtl;
			// zone.decorator.nextLine;
		};

		this.makeDrags;
	}


	makeListView {
		listView = ListView(zone, Rect(2,2,60,160));
		listView.background_(Color.grey(1.0, 0.5));
		listView.keyDownAction = { |l, char|
			if (char == $\r) { this.object_( KeyPlayer.all[l.items[l.value]]) };
		};
	}

	makeButtons {
		var keys = KeyPlayer.all.keys.asArray.sort;

		var navButL, navButR;

	//	navButL = Button(zone, Rect(0, 0, 14, 14)).states_([[ "<" ]]);

	//	zone.decorator.shift(20, 0);

		buttons = 10.collect { |i|
			Button(zone, Rect(0, 0, 34, 16)).states_([[ keys[i] ? "-" ]])
				.action_({ |b|
					var nuKP = KeyPlayer.all[b.states[0][0]];
					if (nuKP.notNil) { this.object_(nuKP); 
					};
					this.checkUpdate;
					b.focus(false);
				})
		};

		lpGuiBut = Button(zone, Rect(0, 0, 34, 18)).states_([[ "lpGui" ]])
		.action_({ this.openLoopGui; });

	//	navButR = Button(zone, Rect(0, 0, 14, 14)).states_([[ ">" ]]);
	}

	makeButtonsLeft {
		var keys = KeyPlayer.all.keys.asArray.sort;
		var butZone = CompositeView(zone, Rect(2,2, 110,160));
		var navButL, navButR;

	//	navButL = Button(zone, Rect(0, 0, 14, 14)).states_([[ "<" ]]);

	//	zone.decorator.shift(20, 0);

		butZone.addFlowLayout;
		buttons = 16.collect { |i|
			Button(butZone, Rect(0, 0, 48, 16)).states_([[ keys[i] ? "-" ]])
				.action_({ |b|
					var nuKP = KeyPlayer.all[b.states[0][0]];
					if (nuKP.notNil) { this.object_(nuKP); };
					this.checkUpdate;
				})
		};

		Button(butZone, Rect(0, 0, 50, 18)).states_([[ "lgui" ]])
		.action_({ if (object.notNil) { this.openLoopGui; } });

	//	navButR = Button(zone, Rect(0, 0, 14, 14)).states_([[ ">" ]]);
	}

	// miniLoopCtl {
	// 	Button(zone, Rect(0, 0, 18, 18)).states_([[\P], [\_], ['|']]);
	// 	Button(zone, Rect(0, 0, 18, 18)).states_([[\R], [\_]]);
	// 	Button(zone, Rect(0, 0, 18, 18)).states_([[\L], ['1']]);
	// 	EZNumber(zone, 26@18, "", [0,99, \lin, 1].asSpec, labelWidth: 0);
	// }


	makeDrags {

		dragZone.addFlowLayout;

		font = Font("Courier-Bold", 14);
		drags = ();

				// make the rows of the keyboard
		keyboard.do {|row, i|
			row.do {|key| this.makeKey(key) };
			if (i==0) { this.makeKey(127.asAscii, "del", 38 @ 24) };
			if (i==2) { this.makeKey($\r, "retrn", 46 @ 24) };
			dragZone.decorator.nextLine;
			dragZone.decorator.shift(lineOffsets[i]);
		};

		this.makeKey($ , "space", 150 @ 24);
		this.makeKey(3.asAscii, "enter", 48 @ 24);

	}

	makeKey { |char, keyname, bounds|
		var v;
		keyname = keyname ? char.asString;
		bounds = bounds ? (24 @ 24);

		v = DragBoth(dragZone, bounds);
		// v.font = font;
		v.string = keyname;
		v.align = \center;
		v.setBoth = false;
		drags.put(char.toLower.asUnicode, v);
	}

	// extra functions

	addAltFuncs {
		object.putAlt($k.asUnicode, {
			"go to curr player on listview or buttons".postln;
			this.focusToPlayerName(object.name);
		});
		object.putAlt($g.asUnicode, {
			"open TimeLoopGui next to main KeyPlayerGui.".postln;
			this.openLoopGui;
		});
	}

	focusToPlayerName { |name|
		var index;
		if (listView.notNil) {
			index = listView.items.indexOf(name);
			if (index.notNil) { listView.value_(index) };
			listView.focus;
		} {
			index = buttons.detectIndex { |bt, i|
				bt.states[0][0].postcs == name;
			};
			if (index.notNil) { buttons[index].focus; };
		}
	}

	openLoopGui {
		var loopgui, coords; 
		if (object.isNil or: { object.rec.isNil }) { 
			"no object or no loop, so no loopgui.".postln;
			^this
		};
		loopgui = EventLoopGui(object.rec);
		if (parent.isClosed.not) {
			coords = parent.bounds.rightTop;
			loopgui.moveTo(coords.x, coords.y);
		};
	}

}
