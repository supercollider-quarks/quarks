// HierSch (c) 2007-2011 Tom Hall
// version 2011-03-24

HierSch { 
	var <>t, <dict, myRate, myQuant, <isRunning=true;
	var mySingleH = true, myMute= false, numHs2play=1;
	var myflipH = false, myPlayEach = false, myHierFlip=false;
	var ignoredHiers, <>muteH0=false, ceaseAllBool=false;
		
	*new { arg clock; 
		// if (clock.isNil, {^"Please specify a TempoClock".error});
		^super.new.initRulesSched(clock)
	}
		
	initRulesSched { arg aClock;
		t = aClock ? TempoClock.default;
//		t.clear; // in case a schedChecker is already running
		dict = HierSchDict.new; 
		myRate = 2 ** 5; // 32
		ignoredHiers = [];
		this.schedChecker(dict);
		CmdPeriod.doOnce {ÊisRunning = false; };
	}
	
	rate_ { |int = 32|
		^myRate = int.nextPowerOfTwo;
	}
	
	rate { ^myRate }
	
	cease { 
		isRunning = false; // stops the schedChecker 
		// this is a kludge maybe, see comment in schedStream
		dict = HierSchDict.new; // _not_ clear
		ceaseAllBool= true; 
		r{ 
			1.wait;
			ceaseAllBool= false; 
			this.restart(false); // bool is for verbose
		}.play(t);
	}
	
	c { this.cease }
	
	stop { 
		isRunning = false // scheduling continues, playing doesn't
	} 
		
	// needed after CmdPeriod
	restart { |verbose=true|
		if (isRunning, {
			if(verbose, {"schedChecker is already running".postln});
		}, {
			if(verbose, { "schedChecker restarted".postln });
			isRunning = true;
			dict = dict.clear; // clear old
			this.schedChecker(dict);
			CmdPeriod.doOnce {ÊisRunning = false; };
		});
	}
	
	schedChecker {|dict|
		var keysInRange, itemsToPlay, rate;
		rate = 1/myRate;
		t.schedAbs(t.beats.ceil, {|time, secs|  
			// check for any entries in the dict for this time
			keysInRange = dict.beatInfo(time);
			// if (itemsToPlay.isEmpty.not) {itemsToPlay.postln};
			if((ceaseAllBool.not and: {keysInRange[1].notNil}), {
				itemsToPlay = this.decide(keysInRange);
				this.playItems(itemsToPlay);	
			});
			if ((time%1)==0, { // on the beat only
				// for removing old beats in the dictionary
				if (ceaseAllBool, { 
					dict.clear; 
				}, {
					// delete up beginning of current bar
					if (time == (t.bars2beats(t.bar)), { 
						dict.clearTo(time)
					});
				});
				// update rate as required (on the whole beat only);
				rate = 1/myRate;
			});	
			if(isRunning.not, {nil}, {rate});
		});
	}

	decide  {| list |
		var selection, min, priorities, beat, data, selection2;
		var priors2Use, finalData, tmpArr;
		priorities = list[1].flop[0];
		data = list[1]; // all events
		priors2Use = this.heirsChooser(priorities);
		// this doesn't filter out duplicates for each hier
		selection = data.select({|x| priors2Use.includes(x[0])}); 
		priors2Use.do{|i| 
			// may select multiple elements having the same priority
			tmpArr = selection.select({|x| x[0]==i}); 
			// filter out duplicates for each hier if nec
			// UPDATED
			// if prior 0, always play
			if ((mySingleH and: {i != 0}), {tmpArr = [tmpArr.choose]});
			finalData = finalData ++ tmpArr;
		};
		^[list[0], finalData]
	}	

	// accepts an array (could contain duplciates)
	// rtns sorted arr (no duplicates)
	heirsChooser { |priorities|
		if (myMute, {^[]});
		// UPDATED
		// take out muted hiers plus 0 for now, added again below
		priorities = priorities.reject({|i| (ignoredHiers ++ 0).includes(i)});
		priorities = priorities.asSet.asArray; // remove duplicate hiers
		priorities = priorities.sort;
		if (myPlayEach.not, {
			if (myHierFlip, {priorities = priorities.reverse});
			// Here the main hiers are selected
			priorities = priorities.keep(numHs2play);
		});
		// UPDATED
		// unless muted, add hier 0 back into arr
		if (muteH0.not, {priorities = priorities ++ 0}); 
		^priorities
	}
	
	ignore {^ignoredHiers}

	ignore_ {|arr|
		if(arr.isInteger, {arr = arr.bubble});
		if(arr.isNil, {arr = []});
		ignoredHiers = arr;
	}
	
	unignore { ignoredHiers = [] }
		
	// Change the num of hiers to play		
	// default is 1
	width_ { |int|
		myMute = false;
		myPlayEach = false;	
		^numHs2play = int
	}
	
	width { ^numHs2play }	
			
	flip_ { |bool|
		^myHierFlip = bool
	}
	
	flip { ^myHierFlip }	

	playNoneBut_ { |arr|
		myMute = false;
		myPlayEach = false;	
		ignoredHiers = (1..12).reject({|i| arr.includes(i)});
	}
	
	// as above
	playOnly_ { |arr|
		^this.playNoneBut_(arr);
	}
	
	playGT_ { |int|
		myMute = false;
		myPlayEach = false;	
		ignoredHiers = (1..int);
	}
	
	playLT_ { |int|
		myMute = false;
		myPlayEach = false;	
		ignoredHiers = (int..12);
	}
		
	mute { myMute = true }

	unmute { this.play }

	m {this.mute}
	
	p { this.play }
	
	play { myMute = false }
	
	playEach {
		ignoredHiers = [];
		myMute = false;
		mySingleH = true;
		myPlayEach = true
	}

	playAll {
		myMute = false;
		mySingleH = false;
		ignoredHiers = [];
		myPlayEach = true
	}

	single_ { |newVal|
		^mySingleH = newVal;
	}

	s { this.single_(true) }

	single { ^mySingleH }	

	default { 
		ignoredHiers = [];
		myMute = false;
		myHierFlip = false;	
		mySingleH = true;
		myPlayEach = false;
	}

	playItems {|list|	
		var schedAhead, data, priority, fn, count, dur;
		schedAhead = list[0];
		data = list[1];	
		t.schedAbs(schedAhead, {|time|
			data.do{|item|
				priority = item[0];
				fn = item[1];
				dur = item[2];
				count = item[3];
				fn.value(time, priority, dur, count);
			};
		});		
	}

	schedAbs { |beat, priority, function, interval, quant, schedQuant|
		var now;
		now = t.beats;
		quant = quant ? this.rate;
		quant = min(quant, this.rate);
		// schedQuant ensures that if the scheduling misses 
		// the desired beat (because of the offset or required quant)
		// then the wait until the next quant is as per schedQuant
		// thus schedQuant is more like quant in the rest of SC
		schedQuant = schedQuant ? quant.reciprocal;
		this.schedStream(priority, function, interval, quant, now, beat, schedQuant, dict)
	}

	// next pulse
	sched { |priority, function, interval, quant|
		this.schedAbs(
			t.beats.ceil, priority, function, interval, quant, 1
		)
	}

	schedBar { |priority, function, interval, quant|
		this.schedAbs(
			t.nextBar, priority, function, interval, quant, t.beatsPerBar
		)
	}
	
	// returns the next beat as though with given quant
	// useful for matching starting times of streams with differnt quants
	nextBeatQ {|qnt|	
		qnt = qnt ? this.rate;
		^(t.nextBeatOffset(1 - (qnt.reciprocal + myRate.reciprocal)) 
			+ (qnt.reciprocal + myRate.reciprocal)
		);
	} 

	// returns the next bar as though with given quant
	// useful for matching starting times of streams with differnt quants
	nextBarQ {|qnt|	
		qnt = qnt ? this.rate;
		^(t.nextTimeOnGrid(t.beatsPerBar, t.beatsPerBar 
			- (qnt.reciprocal + myRate.reciprocal)) + (qnt.reciprocal + myRate.reciprocal))
	} 

	schedStream  {|prio, fn, durPat, quant, now, when, schedQuant, dict|
	// Having the dict as an arg means that if cease method is called
	// the t.schedAbs will post to the now _local_ version of the dict
	// whose entries will never be played. The only way to get around 
	// say, a long wait bn durVals where the ceaseAllBool has changed back to false?
		var durStream, durVal, prioVal, counter, counterVal, qRecip; 
		var whenMinusRate, schedTime, localQuant, offset, oneShot=false;
		// check that schedChecker is running
		if(isRunning.not, {
			"HierSch is not running, please use restart method".error
			}, {
			if (quant.isNil or: {quant > myRate}, {quant = myRate});
			qRecip = myRate.reciprocal;
			// make sure requested quant is mod myrte.reciprocal;
			localQuant = (qRecip *  (myRate/quant)).round(qRecip);
			offset = localQuant + qRecip; 
			whenMinusRate = when - offset;
			schedTime = if(now < whenMinusRate, {
				whenMinusRate // is in the future
				}, {
				(now.roundUp(qRecip) + offset).roundUp(schedQuant) - offset;
			});
			oneShot = if (durPat.isNil, {true}, {false});
			durStream = durPat.asStream;
			prioVal = prio.asStream;
			counter = {: x, x <- (0 .. inf)};
			// schedule ahead by the next value of stream
			t.schedAbs(schedTime, {|beat|
				var localPrio, beat2play;
				// .value here in case durPat is a fn such as {t.beatsPerBar}
				durVal = if (ceaseAllBool, {nil}, {durStream.next.value});
				localPrio = prioVal.value;
				// Don't add entries at the end of streams
				if ((durVal.notNil or: { oneShot}), {
					counterVal = counter.next; 
				// using .value here means prio can be a fn or a Pattern 
				// in case round rounds down, (quant + schedQuant) gives a latency of
				// at least 1 rate - using roundUp would enable (1 * rate),
				// but mean that quantising is less accurate
					beat2play = (beat+offset).round(localQuant);
					dict.addEntry(
						beat2play,
						localPrio, 
						fn, 
						durVal,
						counterVal
					);
				});
				durVal;
			})
		});
	}

	testAbstract { |rate, iterations, priority|
		["beats, priority, durVal, counter"].postln;
		this.sched(
			priority,
			{|b, p, d, c| [b, p, d, c].postln }, 
			Pser.new([rate], iterations)
		)
	}

	testRate {|priority=1|
		format("rate is %", myRate).postln;
		this.testAbstract(1/myRate, myRate, priority);
	}
	
	test { |priority=1|
		this.testAbstract(1, t.beatsPerBar, priority);
	}

}	

	