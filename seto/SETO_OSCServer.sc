/*
Implementation of an OSC server to SETO via the tuio protocol
	http://tuio.lfsaw.de/
	http://modin.yuri.at/publications/tuio_gw2005.pdf

Author: 
	2004, 2005, 2006, 2007
	Till Bovermann 
	Neuroinformatics Group 
	Faculty of Technology 
	Bielefeld University
	Germany
*/

/*
	Changes
		2009-10-29	added SETO_OSCTUIOServer
		2007-10-29	renamed to SETObject
*/

SETO_OSCServer : SETOServer {
	var interface;

	*new {|format, netaddr, setoClass, interactionClass|
		^super.new(format, setoClass, interactionClass).pr_initSETO_OSCServer(netaddr);
	}
	start{
		interface.start;
	}
	stop{
		interface.stop;
	}

	setFunc_{|function|
		setFunc = function;
		OSCReceiverFunction(interface, \set, setFunc);
	}
	aliveFunc_{|function|
		aliveFunc = function;
		OSCReceiverFunction(interface, \alive, aliveFunc);
	}
	pr_initSETO_OSCServer {|netaddr|
		
		interface = OSCReceiver(('/tuio/'++format.asSymbol).asSymbol, netaddr);
		this.setFunc_(setFunc);
		this.aliveFunc_(aliveFunc);
	}
}


SETO_OSCTUIOServer : SETOServer {
	var interface;
	var idHash, class, <>imgRatio;
	
	*new {|format, netaddr, setoClass, interactionClass, imgRatio=0.57142857142857| // 480/640
		^super.new(format, setoClass, interactionClass).pr_initSETO_OSCServer(netaddr, imgRatio);
	}
	start{
		interface.start;
	}
	stop{
		interface.stop;
	}

	setFunc_{|function|
		setFunc = function;
		OSCReceiverFunction(interface, \set, setFunc);
	}
	aliveFunc_{|function|
		aliveFunc = function;
		OSCReceiverFunction(interface, \alive, aliveFunc);
	}
	pr_initSETO_OSCServer {|netaddr, argImgRatio|
		idHash = IdentityDictionary.new;
		class = IdentityDictionary[
			108 -> 0,
			109 -> 0,
			110 -> 0,
			111 -> 0,
			112 -> 0,
			113 -> 0,
			0 -> 1
		];
		imgRatio = argImgRatio;
		
		interface = OSCReceiver(('/tuio/'++format.asSymbol).asSymbol, netaddr);
		this.setFunc_{|id, classID ... args|
			idHash[id] = classID;
			
			
			//[id, classID, args].postln;

			// fix ratio
			args[1] = args[1]*imgRatio;
			
			classID = classID ? -1;
			this.setWithFormat(realFormat, classID, [class[classID]] ++ args);
		};
		this.aliveFunc_{|... argObjectIDs|
			var ids;
			
//			argObjectIDs.postln;
			ids = argObjectIDs.collect{|id| idHash[id]}.select(_.notNil);
//			ids.postln;
			this.alive(ids);
		};
	}
	
	gui {|editable = true|
		var addButton, idBox, classIdBox, xBox, yBox, aBox, eBox;
		hasGUI.not.if({
			window = GUI.window.new("SETObjects", Rect(800, 0, 480, 400))
				.front
				.onClose_{
					hasGUI = false;
				};
			window.view.background = Color(0.918, 0.902, 0.886);
			view = SETOServerView(window,  Rect(5,5,390,370), this);
//			view.background =  Color(0.81960784313725, 0.82352941176471, 0.87450980392157, 0.6);
/*			view.background = Color.fromArray([0.918, 0.902, 0.886] * 0.5 ++ [0.8]);
			view.resize_(5);
			addButton = GUI.button.new(window, Rect(400, 5, 75, 20))
				.states_(	[["add", Color.black, Color.gray(0.5)]])
				.action_{|butt|
					this.setWithFormat("ixya", idBox.value, [classIdBox.value, xBox.value, yBox.value, aBox.value]);
					this.allAlive;
					idBox.value = idBox.value+1;
 				}
 				.resize_(3);

 			GUI.staticText.new(window, Rect(400, 30, 10, 20)).string_("id").resize_(3);
 			idBox = 
 				GUI.numberBox.new(window, Rect(415, 30, 60, 20)).value_(100).resize_(3);
 			GUI.staticText.new(window, Rect(400, 50, 10, 20)).string_("cID").resize_(3);
 			classIdBox = 
 				GUI.numberBox.new(window, Rect(415, 50, 60, 20)).value_(100).resize_(3);
 			GUI.staticText.new(window, Rect(400, 70, 10, 20)).string_("x").resize_(3);
 			xBox  = 
 				GUI.numberBox.new(window, Rect(415, 70, 60, 20)).value_(0.5).step_(0.01).resize_(3);
 			GUI.staticText.new(window, Rect(400, 90, 10, 20)).string_("y").resize_(3);
 			yBox  = 
 				GUI.numberBox.new(window, Rect(415, 90, 60, 20)).value_(0.5).step_(0.01).resize_(3);
 			GUI.staticText.new(window, Rect(400, 110, 10, 20)).string_("a").resize_(3);
 			aBox  = 
 				GUI.numberBox.new(window, Rect(415, 110, 60, 20)).value_(0).step_(0.01).resize_(3);
 			
 			GUI.staticText.new(window, Rect(400, 180, 10, 20)).string_("ext").resize_(3);
 			eBox  = 
 				GUI.numberBox.new(window, Rect(415, 180, 60, 20)).value_(SETO_GUIObj.oExtent).step_(1).resize_(3).action_{|me| SETO_GUIObj.oExtent = me.value};
*/			hasGUI = true;
			^window.front;
		}, {
			^window
		});
	}

}