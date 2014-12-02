ClockFace {
	var <starttime, <tempo, <>inc, <cursecs, isPlaying = false, <clock, <window, timeString;
	var remFun, <mod, start, <>onMod, <>onBeat;
	
	*new{ arg starttime = 0, tempo = 1, inc = 0.1;
		^super.newCopyArgs(starttime, tempo, inc).init;
		}
		
	init {
		cursecs = starttime;
		this.digitalGUI;
		}
		
	play {
		var cur, last, floor;
		clock = TempoClock.new(tempo);
		start = clock.elapsedBeats;
		remFun = {this.stop};
		CmdPeriod.add(remFun);
		last = 0.0;
		isPlaying = true;
		clock.sched(inc, {
			cur = clock.elapsedBeats - start + starttime;
			mod.notNil.if({
				cur = cur%mod;
				(cur < last).if({
					{onMod.value; 
					onBeat.value(cur.floor.asInt);
					}.defer;
					});
				});				
			this.cursecs_(cur, false);
			onBeat.notNil.if({
				(((floor = cur.floor) - last.floor) == 1).if({
					{onBeat.value(floor.asInt)}.defer;
					});
				});
			last = cur;
			inc;
			})
		}
	
	cursecs_ {arg curtime, updateStart = true;
		var curdisp;
		cursecs = curtime;
		curdisp = curtime.asTimeString;
		curdisp = curdisp[0 .. (curdisp.size-3)];
		updateStart.if({starttime = cursecs; (isPlaying).if({
			start = clock.elapsedBeats;
			});
		});
		{timeString.string_(curdisp)}.defer;
		}
		
	stop {
		starttime = cursecs;
		isPlaying = false;
		clock.clear;
		CmdPeriod.remove(remFun);
		clock.stop;
		}
	
	tempo_ {arg newBPS = 1;
		tempo = newBPS;
		isPlaying.if({clock.tempo_(tempo)});
		}
		
	mod_ {arg newMod = 0;
		(newMod == 0).if({
			mod = nil;
			}, {
			mod = newMod;
			})
		}
	
	digitalGUI {
		window = GUI.window.new("Digital Clock", Rect(10, 250, 450, 110)).front;
		timeString = GUI.staticText.new(window, Rect(0, 0, 430, 100))
			.string_(cursecs.asTimeString)
			.font_(Font("Arial", 40));
		window.onClose_({this.stop});
		}

}

