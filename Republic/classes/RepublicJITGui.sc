
RepublicGui : JITGui {
	var <nicknameView, <idView, <privateBtn, 
	<countView, <sendBut, <listDrag, 
	<strengthViews, <listView, 
	<chatViewWrite, <chatView, shoutView,
	<synthdefView;
	var resp, heights;

	*new { |object, numItems = 12, parent, bounds, makeSkip = true, options = #[]|
		^super.new(nil, numItems, parent, bounds, makeSkip, options)
			.object_(object);
	}
	
		// these methods should be overridden in subclasses: 
	setDefaults { |options|
		if (parent.isNil) { 
			defPos = 10@260
		} { 
			defPos = skin.margin;
		};
		minSize = 230 @ (numItems * skin.buttonHeight + 40 + 350);
	}
	
	accepts { |obj| ^obj.isKindOf(Republic) or: obj.isNil }
	
	winName { ^"Republic" + this.getName }
	
	getName { ^try { object.republicName } ? "- none -" }
	
	switchSize { |index = 0| 
		var newheight = heights[index]; 
		var currTop = parent.bounds.bottom;
		parent.bounds = parent.bounds.height_(newheight).bottom_(currTop);
	}
	
	makeViews {
		
		var lifeComp;
		var width = zone.bounds.width; 
		
		heights = zone.bounds.height - [0, 200]; 
		prevState.put(\object, -999); // so first time nil updates
		
		countView = StaticText(zone, 230@20).string_("0 citizens, 0 servers, 0 synthdefs")
			.font_(Font("Helvetica", 14))
			.align_(\center);



		StaticText(zone, 57@20).string_("show:")
			.align_(\center);

		Button(zone, Rect(0,0, 57, 20))
			.states_([["history"],["history", Color.black, skin.onColor]])
			.action_({ |b| try { object.shareHistory } })
			.value_(1);

		Button(zone, Rect(0,0, 57, 20))
			.states_([["chat"], ["chat", Color.black, skin.onColor]])
			.action_({ |b| this.switchSize(b.value.asInteger) })
			.value_(1);

		Button(zone, Rect(0,0, 57, 20))
			.states_([["clock"], ["clock", Color.black, skin.onColor]])
			.action_({ |b| "Telepathic Clock - not used yet.".postln });



		StaticText(zone, 57@20).string_("server(s):")
			.align_(\center);	

		Button(zone, Rect(0,0, 57, 20))
			.states_([["show"]])
			.action_({ |b| 
				// try { object.servers.do(_.makeWindow); };
				try { RepublicServerGui(this.object); };
			});


		sendBut = Button(zone, Rect(0,0, 57, 20))
			.states_([["inform"]])
			.action_({ |b| try { object.informServer } });

		Button(zone, Rect(0,0, 57, 20))
			.states_([["STOP", Color.white, Color.red(0.8)]])
			.action_({ |b, mod| 
				if (mod.isAlt) { 
					"PANIC - stopping all servers!\n".postln;
					try { object.servers.do(_.freeAll(true)) };
				} {
					try { object.myServer.freeAll(true) };
					"PANIC - stopping myServer.\n"
					"click with alt-key to stop all servers.\n".postln;
				}
			});
	

		StaticText(zone, 57@20).string_("synthdefs:")
			.align_(\center);	


		Button(zone, Rect(0,0, 57, 20))
			.states_([["request"]])
			.action_({ |b| 
				try { object.requestSynthDefs };
			});

		Button(zone, Rect(0,0, 57, 20))
			.states_([["show"]])
			.action_({ |b| try { object.postSynthDefs } });

		Button(zone, Rect(0,0, 57, 20))
			.states_([["share"]])
			.action_({ |b, mod| 
				if (mod.isAlt) { 
					"sending all synthdefs!\n".postln;
					object.shareSynthDefs(sendAll: true);
				} {
					object.shareSynthDefs;
				}
			});


		StaticText(zone, 57@20).string_("examples:")
			.align_(\center);	
		Button(zone, Rect(0,0, 57, 20))
			.states_([["events"]])
			.action_({ |b| try { object.postEvents(true, false) } });

		Button(zone, Rect(0,0, 57, 20))
			.states_([["Pdefs"]])
			.action_({ |b| try { object.postPdefs } });

		Button(zone, Rect(0,0, 57, 20))
			.states_([["Tdefs"]])
			.action_({ |b| try { object.postTdefs } });

		zone.decorator.shift(0, 5);

		listDrag = DragSource(zone, 230@20)
			.object_([]);

		zone.decorator.shift(0, 5);
		
		
		nicknameView = StaticText(zone, Rect(0,0, 90, 20))				.align_(\center)
			.string_("<name>");

		idView = StaticText(zone, Rect(0,0, 50, 20))
			.align_(\center)
			.string_("<id>");

		StaticText(zone, Rect(0,0, 50, 20))
			.align_(\center)
			.string_("private:");
		
		privateBtn = Button(zone, Rect(0,0, 40, 20))
			.states_([["no"], ["yes"]])
			.action_({ |b| object.private = b.value > 0 });

		zone.decorator.shift(0, 5);


		listView = ListView(zone, Rect(0, 0, width * 0.5 - 6, numItems * 18)).resize_(5);
		listView.background_(Color.clear);
		listView.hiliteColor_(Color.green(alpha:0.6));

		lifeComp = CompositeView(zone, Rect(0, 0, width * 0.5 - 6, numItems * 18)).resize_(3);
		lifeComp.addFlowLayout(0@0, 1@1);
		
		strengthViews = numItems.collect { 
			var view = StaticText(lifeComp, Rect(0,0, 1, 17))
				.background_(Color(0.1, 0.7, 0.1));
				lifeComp.decorator.nextLine;
			view;
		};
		
		zone.decorator.shift(0, 5);
		
		
		
		shoutView = TextView(zone, Rect(0, 0, width - 4, 24))
			.resize_(8).font_(Font("Helvetica", 14))
			.string_("shout here - use tab to send")
			.usesTabToFocusNextView_(false) 
			.enterInterpretsSelection_(false)
			.background_(Color.black)
			.stringColor_(Color.white)
			.keyDownAction_ { |view, char| 
				if (char === Char.tab) { 
					
					History.forwardFunc.value("//!!" + view.string);
				};
			};

		zone.decorator.shift(0, 5);
			
		chatViewWrite = TextView(zone, Rect(0, 0, width - 4, 24)).resize_(8)
			.string_("chat here - use tab to send");

		zone.decorator.shift(0, 5);
		chatView = TextView(zone, Rect(0, 0, width - 4, 170)).resize_(8);
		
		chatViewWrite.font = Font("Helvetica", 14);
		chatView.font = Font("Helvetica", 12);
		chatView.hasVerticalScroller_(true);

		parent.onClose = { this.stop; this.removeChatResponder; };
		
		chatViewWrite
			.usesTabToFocusNextView_(false)
			.enterInterpretsSelection_(false)
			.keyDownAction_({Êarg v, char;
				var string;
				if(char === Char.tab) 
				{ 
					this.sendChat(v.string.copy); 
					AppClock.sched(0.1, { v.string = "" });
				} 
			});
				
		this.startChatResponder;	
		
	}
	
	stop { 
		skipjack.stop;
		this.removeChatResponder;
	}
	
	getState { 
		
		var state, names; 
		
		if (object.isNil) { 
			^(	name: "<name>", id: "", 
				names: [], showNames: [],
				presence: (), numSynthDefs: -1, 
				private: false, numCitizens: 0, numServers: 0
			)
		};

		names = object.nameList.array.copy;

		state = (
			object: object, 
			names: names, 
			nickname: object.nickname,
			presence: object.presence.copy, 
			showNames: names.collect(_.asString),
			private: object.private, 
			numCitizens: object.presence.size,
			numServers: object.servers.size
		);
		
		if (object.isKindOf(Republic)) { 
			state.putPairs([
				\id, object.clientID, 
				\numSynthDefs, object.synthDefNames.size, 
				\groupNames, object.groups.keys.asArray.sort,
				\showNames, names.collect { |name| 
					name.asString + ":" + (object.allClientIDs[name] ? "")
				}
			])
		};
		
		^state;
	}
	
	checkUpdate {
		 
		var newState = this.getState; 
		var presence = newState[\presence];
		
		if (newState == prevState) { 
			^this
		};

		if (object != prevState[\object]) { 
			if (object.isNil) { 
				this.name_(this.getName);
				nicknameView.string = "nickname: - ".asString;
				idView.string = "id: - ";
				privateBtn.value = 0; 
				listView.items = [];
				privateBtn.visible = false;
				countView.string = "0 citizens, 0 servers, 0 synthdefs";
				strengthViews.do { |v| v.bounds_(v.bounds.width_(0)) };
				prevState = newState;
				^this
			} { 
				this.name_(this.getName);
				privateBtn.visible = true;
			};
		};

			// object is still the same republic:

		if (newState[\numSynthDefs] != prevState[\numSynthDefs] or: 
			{ newState[\numCitizens] != prevState[\numCitizens] }) {
			countView.string = "% citizens, % servers, % synthdefs"
				.format(newState[\numCitizens], newState[\numServers], newState[\numSynthDefs]);
		};
					
		if (newState[\nickname] != prevState[\nickname]) { 
			nicknameView.string = (object.nickname ? "<name>").asString;
			idView.string = ("id: " + newState[\id].asString);
		}; 
		
		if (newState[\private] != prevState[\private]) { 
			privateBtn.value = newState[\private].binaryValue; 
		};
		
		if (newState[\showNames] != prevState[\showNames]) { 
			listDrag.object = newState[\names];
			listView.items = newState[\showNames];
		};

		if (newState[\presence] != prevState[\presence]) { 
			strengthViews.do { |v, i|
				var strength = presence[newState[\names][i]];
				var strengthWidth = ((strength ? 0) / object.graceCount * 100);
				v.bounds_(v.bounds.width_(strengthWidth));
				// v.refresh;
			};
		};
		
		prevState = newState;
	}
	
	sendChat { |str|
		str.postln;
		object.send(\all, '/chat', object.nickname, str)
	}
	
	
	startChatResponder {
		resp = OSCresponder(nil, '/chat', { |t, r, msg|
			var name, string;
			name = msg[1];
			string = msg[2];
			defer { 
				chatView.string = chatView.string ++ (name ++ ":" + string) ++ "\n";
			};
		}).add;
	}
	
	removeChatResponder {
		resp.remove;
	}

}
