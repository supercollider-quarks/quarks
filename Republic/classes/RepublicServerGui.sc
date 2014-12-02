/* 
	not fully working yet, just a sketch
	ToDo: 
	rewrite Server:makeGui as ServerGui : JITGui 
	(then this could become ServerAllGui)
	
	
	r.addParticipant(\carlo, s.addr, otherClientID: r.nextFreeID);
	r.addParticipant(\otto, s.addr, otherClientID: r.nextFreeID);
	r.servers
	z = RepublicServerGui(r);
	z.lines
*/


RepublicServerGui : JITGui { 
	var <homeZone, <lines; 
	
	*new { |object, numItems = 6, parent, bounds, makeSkip = true, options = #[]| 
		^super.new(object, numItems, parent, bounds, makeSkip)
	}
	
		// these methods should be overridden in subclasses:
	setDefaults { |options|
		if (parent.isNil) {
			defPos = 10@260
		} {
			defPos = skin.margin;
		};
		minSize = 300 @ (numItems + 1 * skin.buttonHeight + 4 + 120);
	}
	
	getName { ^try { object.name } ? "Republic" }

	accepts { |obj| ^obj.isNil ?? obj.isKindOf(Republic) }
	
	object_ { |obj| 
		// should be used once only ATM
		var win;
		if (obj.isNil) {
			homeZone.children.do(_.remove);
		} { 
			if (obj != object) { 
				homeZone.children.do(_.remove);
				try { win = obj.myServer.window };
				if(win.notNil) {
					win.close;
					obj.myServer.makeGui(homeZone);
				}
			}
		};
		object = obj;
	}
	
	getState { 
		if (object.isNil) { ^[] };
		
		^object.nameList.collect { |name, i|
			var sv = object.servers[name];
			(
				name: 		sv.name, 
				running: 		sv.serverRunning, 
				avgCPU: 		try { sv.avgCPU.round(0.1) },
				peakCPU: 		try { sv.peakCPU.round(0.1) },
				numUGens: 	sv.numUGens,
				numSynths: 	sv.numSynths,
				numGroups: 	sv.numGroups,
				numSynthDefs: sv.numSynthDefs
			)	
		}
	}
	
	makeViews { 
		homeZone = CompositeView(zone, Rect(0,0,300, 100))
			.background_(Color.grey(0.85));
		homeZone.addFlowLayout;
		
		StaticText(zone, Rect(0,0, 300, 20))
			.font_(Font("Monaco", 9))
			.string_(
			"name  status  defs groups synths ugens peak  avg" 
		//	"  10.3 " "  8.8  " "   5   " "  3   " "  93"
		);
		
		lines = numItems.collect { 
			var lineZone = CompositeView(zone, Rect(0, 0,300, 24));
			lineZone.addFlowLayout(2@2, 2@2);
			Button(lineZone, Rect(0, 0, 76, 20))
				.states_([[""],["", Color.black, skin.onColor]]);
			6.do { 
				StaticText(lineZone, Rect(0,0, 34, 20))
					.font_(Font("Monaco", 9))
					.align_(\center)
					.string_("?");
			};
			lineZone;
		};
	}
	
	checkUpdate { 
		var newState = this.getState;
		
		lines.do { |l, i| 
			var svstate = newState[i];
			var str, svname; 
			
			l.visible_(svstate.notNil);
			if (svstate.notNil) { 
				svname = svstate[\name].asString;
				l.children.first
					.value_(svstate[\running].binaryValue)
					.states_([[svname], [svname, Color.black, skin.onColor]]);
					
				l.children[1].string = svstate.numSynthDefs ? "?";
				l.children[2].string = svstate.numGroups ? "?";
				l.children[3].string = svstate.numSynths ? "?";
				l.children[4].string = svstate.numUGens ? "?";
				l.children[5].string = svstate.peakCPU ? "?";
				l.children[6].string = svstate.avgCPU ? "?";
			};
		}
	}
	
	
}
