// HierSchDict (c) 2007 - 2008 Tom Hall
// version 2007-02-3


HierSchDict : Dictionary { 
	
	// counter is new arg
	addEntry {arg when, pri, fn, streamVal, counter;
		this.put(when, this[when].add([pri, fn, streamVal, counter])); 
	}
	
	beatInfo {arg beat; 
		^[beat.asFloat, this[beat.asFloat]]
	}	
	
	keysInRange {|low, high|
		^this.keys.asArray.select{|key| 
			(key >= low) && (key < high)
		}
	}
		
	beatInfoFuzzy { |beat=0|
		var inRange;
		inRange = this.keysInRange(
			beat.asFloat, (beat.asFloat) + 1
		);
		^inRange.sort.collect{|beat| 
			[beat, this[beat.asFloat]]
		}
	}
	
	allBeatsInfo { 
		^this.keys.asArray.sort.do{|key| 
			format("%  ->  %", key.trunc(0.001), this.at(key)
			).postln;
		}
	}
	
	clearTo { arg beat;
		if (this.isEmpty.not, {
			this.keysDo{|k| 
				if (k.floor < beat, { this.removeAt(k) }) 
			}
		});
	}

}

/* TESTS

// HierDict2

d = HierSchDict.new

// args: when, heirPos, fn
d.addEntry(77, 0, {|pulse| "some fn"+pulse.asString}, 1, 0)
d.addEntry(77, 2, {|pulse| "some fn"+pulse.asString}, 1, 2)
d.addEntry(77.5, 2, {|pulse| "some fn"+pulse.asString}, 1, 1)
d.addEntry(77, 4, {|pulse| "some fn"+pulse.asString},1, 1 )
d.addEntry(77.999, 2, {|pulse| "some fn"+pulse.asString}, 1, 1)

d.addEntry(99, 0, {"some fn"}, 1, 2)
// Adds a second 0 priority
d.addEntry(99, 0, {"some fn"}, 2, 3)


d.beatInfo(77) // => [ 77, [ [ 0, a Function, 1, 0 ], [ 2, a Function, 1, 2 ], [ 4, a Function, 1, 1 ] ] ]

d.keysInRange(77, 78) // => [ 77.999, 77 ]

// Print out the whole dictionary
d.allBeatsInfo // =>  77 -> [ 0, 2, 4 ]

// remove entries before pulse
d.clearTo(77)

*/