Blackboard {

	var <>broadcastAddr, <blackboardName;
	var <doc, <wasChanged = false, <mode = \waiting; // one of the three: \passive, \active, \waiting
	var skippy, resp, id;
	var <>useDocument = true;
	var <>gracePeriod = 0.5;
	
	classvar <>backup, <colors;

	*initClass { 
		colors = (
			board: Color(0.1, 0.3, 0.1),
			passive: Color.grey(0.7),
			waiting: Color(1, 1, 0.75), 
			active: Color.white(0.95)
		);
		StartUp.add {
			if(colors[\font].isNil and: { GUI.scheme.notNil }) {
				colors[\font] = Font("ChalkDuster", 14);
			};
		};
	}
	
	*paintListener { 
		Document.listener.background = colors[\board];
		Document.postColor = colors[\waiting];
		Document.listener.stringColor = colors[\waiting];
		Document.listener.font = colors[\font];
	}
	
	*new { |broadcastAddr, blackboardName = '/blackboard'|
		^super.newCopyArgs(broadcastAddr ?? { Republic.default.broadcastAddr }, blackboardName)
	}
	
	start {
		var test = false;
		skippy = SkipJack({ this.idle }, gracePeriod);
		resp = 
			[
				OSCresponderNode(nil, blackboardName, { |t, r, msg|
					if(msg[1] != id) {
						this.trySetString(msg[2].asString);
					};
					
				}).add,
				OSCresponderNode(nil, this.cmdForFree, { |t, r, msg|
					if(msg[1] != id) {
						defer { this.tryMakeWaiting };
					}
					
				}).add,
				OSCresponderNode(nil, this.cmdForTest, { |t, r, msg|
					if(msg[1] == id) {
						test = true;
					}
					
				}).add
			];
		
		id = 100000.rand;
		this.waiting;
		
		this.makeGUI;
		
		fork {
			broadcastAddr.sendMsg(this.cmdForTest, id);
			2.wait; 
			if(test) { 
				"Blackboard Broadcast is working." 
			} { "Blackboard Broadcast FAILED. Check Address." }.postln 
		};
	}
	
	cmdForFree {
		^(blackboardName.asString ++ "Free").asSymbol
	}
	
	cmdForTest {
		^(blackboardName.asString ++ "Test").asSymbol
	}
	
	stop {
		skippy.stop;
		resp.do(_.remove);
	}
	
	makeGUI {
		var docClass = if(useDocument) { Document } { BlackboardWindow };
		doc = docClass.new("blackboard");
		doc.string = backup ? "";
		try { doc.font = colors[\font] } { doc.font = Font("Courier", 10) };
		doc.onClose = { backup = doc.string; this.stop };
		ShutDown.add({ defer { doc.close } });
	}
	
	// private implementation
	
	idle {
		if(wasChanged.not) { 
			this.waiting;
		};
		wasChanged = false; // will be reset by keyDown
	}
	
	
	trySetString { |str|
		defer { 
			if(mode == \waiting) { this.passive };
			if(mode != \active) {
				doc !? { 
					doc.editable = true; 
					doc.string = str;
					doc.editable = false; 
				};
				wasChanged = true;
			};
		}
	}
	
	tryMakeWaiting { |str|
		if(mode != \active) {
			this.waiting;
		};
	}
	
	waiting {
		if(mode == \active) { 
			broadcastAddr.sendMsg(this.cmdForFree, id); 
		};
		mode = \waiting;
		
		doc !? {
			doc.editable = true;
			doc.background = colors[\board];
			doc.stringColor = colors[\waiting];
			doc.title = "if you like, write on blackboard";
			doc.keyDownAction = {
				wasChanged = true;
				this.active;
			};
		};
	}
	
	active {
		var func = {
				broadcastAddr.sendMsg(blackboardName, id, doc.string);
				wasChanged = true;
		};
		mode = \active;
		doc !? {
			doc.editable = true;
			doc.stringColor = colors[\active];
			doc.title = "writing on blackboard";
			doc.keyDownAction = {
				AppClock.sched(0.03, { func.value; nil });
			};
		};
		func.value;
	}
	
	passive {
		mode = \passive;
		
		doc !? {
			doc.editable = false;
			doc.stringColor = colors[\passive];
			doc.title = "blackboard";
			doc.keyDownAction = nil;
		};
	}
	
	
}

BlackboardWindow {
	var <>textView, <>window;
	var <layerView;
	*new { | title="Untitled", string="" |
		^super.new.init(title, string)
	}
	
	init { |title, string|
		window = Window.new(title, Rect(100, 100, 500, 400));
		textView = TextView(window, Rect(0, 0, 500, 400)).resize_(5);
		layerView = UserView(window, Rect(0, 0, 500, 400));
		window.front;
	}
	
	editable_ { |bool|
		textView.editable_(bool)
	}
	
	background_ { |color|
		textView.background_(color)
	}
	
	title_ { |string|
		window.name_(string)
	}
	
	keyDownAction_ { |func|
		textView.keyDownAction_(func)
	}
	
	string {
		^textView.string
	}
	
	string_ { |string|
		^textView.string_(string)
	}
	
	stringColor_ { |color|
		textView.stringColor_(color)
	}
	
	font_ { |font|
		textView.font_(font)
	}
	
	onClose_ { |func|
		window.onClose_(func)
	}
	
	onClose {
		^window.onClose
	}
	
}

