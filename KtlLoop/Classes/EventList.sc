/* EventList is a list of recorded events.
   It follows a few conventions:
*  recorded events always have an abstime,
*  plus any other key/value combinations that help
     to store the events in semantically rich form.
TimeLoop
* reserved keys - overwrite at your own risk:
   relDur: is used for storing delta-time between events,
   dur: is used to calculate actual logical duration,
        e.g. when soft-quantizing an EventList to a time grid.

* recording a List is terminated by .finish(absTime),
  which puts an end event at the end of the list.

e = EventLoop(\x);
e.startRec;
e.recordEvent((absTime: 2));


(meta: (absTime: x, relDur: y), lx: 0.24, ly: 0.56);
protected keyNames!

*

a = EventList[];
a.addEvent((absTime: 0));// events should begin with time 0;
a.addEvent((absTime: 0.3));
a.addEvent((absTime: 0.52));
a.addEvent((absTime: 0.72));
a.addEvent((absTime: 0.93));
a.finish(1.88);

a.print;
a.print([\dur]);
a.print([\dur], false);
a.print([\absTime, \dur], false);

a.quantizeDurs(0.25, 2).printAll;"";
a.totalDur;
a.playingDur;


a.collect(_.absTime);
a.collect(_.type);

? also put a startEvent before all others?
    esp. if one wants to record silence first?

*/

EventList : List {
	var <totalDur = 0, <playingDur = 0;

	print { |keys, postRest = true|
		var postKeys;
		if (postRest.not) {
			postKeys = keys;
		} {
			postKeys = this[1].keys.asArray.sort;
			if (keys.notNil) {
				postKeys = (keys ++ postKeys.removeAll(keys));
			};
		};
		this.do { |ev|
			var ev2 = ev.copy;
			postKeys.do { |key|
				"%: %, ".postf(key, ev2.removeAt(key));
			};
			if (ev2.size > 0) {
				".. %\n".postf(ev2);
			} {
				"".postln;
			};
		}
	}

	start { |absTime = 0|
		this.add((absTime: absTime, type: \start, relDur: 0));
	}

	addEvent { |ev|
		if (array.size == 0) { this.start(ev[\absTime]) };
		super.add(ev);
		this.setRelDurInPrev(ev, this.lastIndex);
	}

	calcRelDurs {
		this.doAdjacentPairs { |prev, next|
			var newRelDur = next[\absTime] - prev[\absTime];
			prev.put(\relDur, newRelDur);
			prev.put(\dur, newRelDur);
		};
		this.last.put(\relDur, 0).put(\dur, 0);
	}

	finish { |absTime|
		this.addEvent((absTime: absTime, type: \end, relDur: 0));
		totalDur = absTime - this.first[\absTime];
		playingDur = totalDur;
		this.setPlayDursToRelDur;
	}

	setRelDurInPrev { |newEvent, newIndex|
		var prevEvent;
		newIndex = newIndex ?? { array.indexOf(newEvent) };
		prevEvent = array[newIndex - 1];

		if (prevEvent.notNil) {
			prevEvent[\relDur] = newEvent[\absTime] - prevEvent[\absTime];
		};
	}

	setPlayDurs { |func| this.do { |ev| ev.put(\playDur, func.value(ev)) } }

	setPlayDursToRelDur { this.setPlayDurs({ |ev| ev[\relDur] }); }

	quantizeDurs { |quant = 0.25, fullDur|
		var durScaler = 1;
		fullDur !? {
			playingDur = fullDur;
			durScaler = fullDur / totalDur;
		};

		this.doAdjacentPairs({ |ev1, ev2|
			var absNow = (ev2[\absTime] * durScaler).round(quant);
			var absPrev = (ev1[\absTime] * durScaler).round(quant);
			ev1.put(\playDur, (absNow - absPrev));
		});
		// leaves end event untouched.
	}

	restoreDurs {
		this.setPlayDursToRelDur;
	}
}
