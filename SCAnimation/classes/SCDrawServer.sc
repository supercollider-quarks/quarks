SCDrawServer {
	var queue, orch, frameRate, responder, resp2, window, view, active, func, ndict, perc, bgcolor, clear, bounds;
	
	*new { arg dict, addr=nil, rate=25.0, width=500, height=500, color=Color.black, demand=false, fullscreen=false, hideCursor=false, refresh=true;
		^super.new.init(dict, addr, rate, width, height, color, demand, fullscreen, hideCursor, refresh);
	}
	
	init { arg dict, addr, rate, width=500, height=500, color=Color.black, demand=false, fullscreen=false, hideCursor=false, refresh=true;
		queue = Array.new;
		frameRate = rate;
		active = true;
		orch = dict;
		bgcolor = color;
		clear = 0;
		if(fullscreen == true, {
			window = Window.new("cdraw", Rect(200, 200, width, height), resizable: false, border: false).front.fullScreen;
			view = UserView(window, Window.screenBounds()).background_(bgcolor).clearOnRefresh_(refresh);
			if(hideCursor == true, { SCNSObject("NSCursor", "hide"); });
			bounds = Window.screenBounds();
			}, {
			window = Window.new("cdraw", Rect(200, 200, width, height), resizable: false).front;
			view = UserView(window, window.view.bounds).background_(bgcolor).clearOnRefresh_(refresh);
			bounds = window.view.bounds;
			});
		window.view.background_(bgcolor);
		//window.view.keyDownAction_({ arg view, char, mod, uni, key; key.postln; char.postln; if(key == 3, { "it was an f!".postln; }); });
		window.onClose_({
			window.endFullScreen;
			active = false;
			SCNSObject("NSCursor", "unhide");
			responder.remove;
			resp2.remove;
			"finished!".postln;
			});
		if(demand == false, {
			responder = OSCresponderNode(addr, 'draw', { arg time, resp, msg; this.addToQueue(msg); }).add;
			resp2 = OSCresponderNode(addr, 'drawTrig', { arg time, resp, msg; this.addToQueue2(msg); }).add;
			}, {
			responder = OSCresponderNode(addr, 'draw', { arg time, resp, msg; this.drawOneFrame(msg); }).add;
			resp2 = OSCresponderNode(addr, 'drawTrig', { arg time, resp, msg; this.drawOneFrame2(msg); }).add;
			});
	}
	
	addToQueue { arg msg;
		var dict;
		//msg.postln;
		if(msg[1] == \clear, { clear = 1; },
			{
			if(msg.size > 3, { dict = Dictionary.new();
							((msg.size-3)/2).do({ arg j; dict.put(msg[j*2+3], msg[j*2+4]); });
							});
			queue = queue.add([ 0, (frameRate*msg[2]).asInteger, msg[1], dict]);
			});
	}
			
	drawOneFrame { arg msg;
		var dict = Dictionary.new();
		//msg.postln;
		if(msg.size > 3, { ((msg.size-3)/2).do({ arg j; dict.put(msg[j*2+3], msg[j*2+4]); }); });
		func = orch.at(msg[1]);
		ndict = dict;
		perc = msg[2];
		view.drawFunc = {
			if(msg[1] == \clear, { Pen.fillColor = bgcolor; Pen.fillRect(bounds); });
			func.value(perc, ndict);
			};
		{ view.refresh; }.defer;
	}

	addToQueue2 { arg msg;
		var dict;
		//msg.postln;
		if(msg[2] == 0, { clear = 1; },
			{
			if(msg.size > 4, { dict = Array.new();
							(msg.size-4).do({ arg j;
							dict = dict.add(msg[j+4]);
							});
						});
			queue = queue.add([ 0, (frameRate*msg[3]).asInteger, msg[2], dict]);
			});
	}
		
	drawOneFrame2 { arg msg;
		var dict = Array.new();
		//msg.postln;
		if(msg.size > 4, { dict = Array.new();
						(msg.size-4).do({ arg j;
							dict = dict.add(msg[j+4]);
							});
						});
		func = orch.at(msg[2]);
		ndict = dict;
		perc = msg[3];
		view.drawFunc = {
			if(msg[2] == 0, { Pen.fillColor = bgcolor; Pen.fillRect(bounds); });
			func.value(perc, ndict);
			};
		{ view.refresh; }.defer;
	}
			
	run {
		SystemClock.sched(0.0, {
			if(active == true, {
				view.drawFunc = {
				var removeThese = Array.new();
				if(clear == 1, { Pen.fillColor = bgcolor; Pen.fillRect(bounds); clear = 0; });
				queue.do({ arg it, i;
					if(it[0] == (it[1]-1), { removeThese = removeThese.add(i.asInteger); });
					(orch.at(it[2])).value(it[0]/(it[1]-1), it[3]);
					it[0] = it[0] + 1;
					});
				removeThese.reverse.do({ arg it; queue[it][3] = nil; queue.removeAt(it); });
				};
				{ view.refresh }.defer;
				frameRate.reciprocal;
				}, {
				window.close; nil;
				});
			});
	}
	
	*run	{ arg dict, addr=nil, rate=25.0, width=500, height=500, color=Color.black, fullscreen=false, hideCursor=false, refresh=true;
		this.new(dict, addr, rate, width, height, color, false, fullscreen, hideCursor, refresh).run();
	}

	*onDemand	{ arg dict, addr=nil, rate=25.0, width=500, height=500, color=Color.black, fullscreen=false, hideCursor=false, refresh=true;
		this.new(dict, addr, rate, width, height, color, true, fullscreen, hideCursor, refresh);
	}	
}

+ NetAddr {
	
	sendAdjMsg { arg ... args;
		args = args.insert(1, 0);
		this.sendMsg(*args);
	}
}