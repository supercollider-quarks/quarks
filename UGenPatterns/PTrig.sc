//redFrik


PTrig1 : FilterPattern {
	var <>dur;
	*new {|pattern, dur= 1|
		^super.newCopyArgs(pattern, dur);
	}
	storeArgs {^[pattern, dur]}
	embedInStream {|inval|
		var evtStr= pattern.asStream;
		var durStr= dur.asStream;
		var outVal, durVal;
		var counter= 0, prev= 0;
		loop{
			outVal= evtStr.next(inval);
			if(outVal.isNil, {^inval});
			durVal= durStr.next(outVal);
			if(durVal.isNil, {^inval});
			
			if(prev<=0 and:{outVal>0 and:{counter==0}}, {
				counter= durVal;
			});
			inval= (counter>0).binaryValue.yield;
			counter= (counter-1).max(0);
			prev= outVal;
		};
	}
}

PTrig : PTrig1 {
	embedInStream {|inval|
		var evtStr= pattern.asStream;
		var durStr= dur.asStream;
		var outVal, durVal;
		var counter= 0, prev= 0, trig= 0;
		loop{
			outVal= evtStr.next(inval);
			if(outVal.isNil, {^inval});
			durVal= durStr.next(outVal);
			if(durVal.isNil, {^inval});
			
			if(prev<=0 and:{outVal>0 and:{counter==0}}, {
				counter= durVal;
				trig= outVal;
			});
			if(counter>0, {
				inval= trig.yield;
			}, {
				inval= 0.yield;
			});
			counter= (counter-1).max(0);
			prev= outVal;
		};
	}
}

PTDelay : PTrig1 {
	embedInStream {|inval|
		var evtStr= pattern.asStream;
		var durStr= dur.asStream;
		var outVal, durVal;
		var counter= 0, prev= 0;
		loop{
			outVal= evtStr.next(inval);
			if(outVal.isNil, {^inval});
			durVal= durStr.next(outVal);
			if(durVal.isNil, {^inval});
			
			if(prev<=0 and:{outVal>0 and:{counter==0}}, {
				counter= durVal+1;
			});
			if(counter>1, {
				inval= 0.yield;
			}, {
				if(counter==1, {
					inval= 1.yield;
				}, {
					inval= 0.yield;
				});
			});
			counter= (counter-1).max(0);
			prev= outVal;
		};
	}
}

PLatch : FilterPattern {
	var <>trig;
	*new {|pattern, trig= 0|
		^super.newCopyArgs(pattern, trig);
	}
	storeArgs {^[pattern, trig]}
	embedInStream {|inval|
		var evtStr= pattern.asStream;
		var trgStr= trig.asStream;
		var outVal, trgVal;
		var prev= 0, hold;
		loop{
			outVal= evtStr.next(inval);
			if(outVal.isNil, {^inval});
			trgVal= trgStr.next(outVal);
			if(trgVal.isNil, {^inval});
			
			if(prev<=0 and:{trgVal>0}, {
				hold= outVal;
			});
			if(hold.notNil, {
				if(trgVal<=0, {
					hold= nil;
					inval= outVal.yield;
				}, {
					inval= hold.yield;
				});
			}, {
				inval= outVal.yield;
			});
			prev= trgVal;
		};
	}
}

PGate : PLatch {
	embedInStream {|inval|
		var evtStr= pattern.asStream;
		var trgStr= trig.asStream;
		var outVal, trgVal;
		var hold;
		loop{
			outVal= evtStr.next(inval);
			if(outVal.isNil, {^inval});
			trgVal= trgStr.next(outVal);
			if(trgVal.isNil, {^inval});
			
			if(trgVal>0, {
				hold= outVal;
				inval= outVal.yield;
			}, {
				if(hold.isNil, {hold= outVal});
				inval= hold.yield;
			});
		};
	}
}

PPulseCount : FilterPattern {
	var <>reset;
	*new {|pattern, reset= 0|
		^super.newCopyArgs(pattern, reset);
	}
	storeArgs {^[pattern, reset]}
	embedInStream {|inval|
		var evtStr= pattern.asStream;
		var rstStr= reset.asStream;
		var outVal, rstVal;
		var counter= 0, prev= 0, prev2= 0;
		loop{
			outVal= evtStr.next(inval);
			if(outVal.isNil, {^inval});
			rstVal= rstStr.next(outVal);
			if(rstVal.isNil, {^inval});
			
			if(prev<=0 and:{outVal>0}, {
				counter= counter+1;
			});
			if(prev2<=0 and:{rstVal>0}, {
				counter= 0;
			});
			inval= counter.yield;
			prev= outVal;
			prev2= rstVal;
		};
	}
}

PPeak : PPulseCount {
	embedInStream {|inval|
		var evtStr= pattern.asStream;
		var rstStr= reset.asStream;
		var outVal, rstVal;
		var peak, prev= 0;
		loop{
			outVal= evtStr.next(inval);
			if(outVal.isNil, {^inval});
			rstVal= rstStr.next(outVal);
			if(rstVal.isNil, {^inval});
			
			if(peak.isNil, {peak= outVal});
			if(outVal.abs>peak, {
				peak= outVal.abs;
			});
			if(prev<=0 and:{rstVal>0}, {
				peak= outVal.abs;
			});
			inval= peak.yield;
			prev= rstVal;
		};
	}
}

PRunningMin : PPeak {
	embedInStream {|inval|
		var evtStr= pattern.asStream;
		var rstStr= reset.asStream;
		var outVal, rstVal;
		var peak, prev= 0;
		loop{
			outVal= evtStr.next(inval);
			if(outVal.isNil, {^inval});
			rstVal= rstStr.next(outVal);
			if(rstVal.isNil, {^inval});
			
			if(peak.isNil, {peak= outVal});
			if(outVal<peak, {
				peak= outVal;
			});
			if(prev<=0 and:{rstVal>0}, {
				peak= outVal;
			});
			inval= peak.yield;
			prev= rstVal;
		};
	}
}

PRunningMax : PPeak {
	embedInStream {|inval|
		var evtStr= pattern.asStream;
		var rstStr= reset.asStream;
		var outVal, rstVal;
		var peak, prev= 0;
		loop{
			outVal= evtStr.next(inval);
			if(outVal.isNil, {^inval});
			rstVal= rstStr.next(outVal);
			if(rstVal.isNil, {^inval});
			
			if(peak.isNil, {peak= outVal});
			if(outVal>peak, {
				peak= outVal;
			});
			if(prev<=0 and:{rstVal>0}, {
				peak= outVal;
			});
			inval= peak.yield;
			prev= rstVal;
		};
	}
}

PToggleFF : FilterPattern {
	*new {|pattern|
		^super.newCopyArgs(pattern);
	}
	storeArgs {^[pattern]}
	embedInStream {|inval|
		var evtStr= pattern.asStream;
		var outVal;
		var toggle= 0, prev= 0;
		loop{
			outVal= evtStr.next(inval);
			if(outVal.isNil, {^inval});
			
			if(prev<=0 and:{outVal>0}, {
				toggle= 1-toggle;
			});
			inval= toggle.yield;
			prev= outVal;
		};
	}
}

PInRange : FilterPattern {
	var <>lo, <>hi;
	*new {|pattern, lo= 0, hi= 1|
		^super.newCopyArgs(pattern, lo, hi);
	}
	storeArgs {^[pattern, lo, hi]}
	embedInStream {|inval|
		var evtStr= pattern.asStream;
		var lowStr= lo.asStream;
		var higStr= hi.asStream;
		var outVal, lowVal, higVal;
		loop{
			outVal= evtStr.next(inval);
			if(outVal.isNil, {^inval});
			lowVal= lowStr.next(outVal);
			if(lowVal.isNil, {^inval});
			higVal= higStr.next(outVal);
			if(higVal.isNil, {^inval});
			
			inval= (outVal>=lowVal and:{outVal<=higVal}).binaryValue.yield;
		};
	}
}

PInRect : FilterPattern {
	var <>y= 0, <>rect;
	*new {|pattern, y= 0, rect|
		^super.newCopyArgs(pattern, y, rect);
	}
	storeArgs {^[pattern, y, rect]}
	embedInStream {|inval|
		var xStr= pattern.asStream;
		var yStr= y.asStream;
		var rectStr= rect.asStream;
		var xVal, yVal, rectVal;
		loop{
			xVal= xStr.next(inval);
			if(xVal.isNil, {^inval});
			yVal= yStr.next(inval);
			if(yVal.isNil, {^inval});
			rectVal= rectStr.next(inval);
			if(rectVal.isNil, {^inval});
			
			inval= rectVal.containsPoint(Point(xVal, yVal)).binaryValue.yield;
		};
	}
}

PFold : PInRange {
	embedInStream {|inval|
		var evtStr= pattern.asStream;
		var lowStr= lo.asStream;
		var higStr= hi.asStream;
		var outVal, lowVal, higVal;
		loop{
			outVal= evtStr.next(inval);
			if(outVal.isNil, {^inval});
			lowVal= lowStr.next(outVal);
			if(lowVal.isNil, {^inval});
			higVal= higStr.next(outVal);
			if(higVal.isNil, {^inval});
			
			inval= outVal.fold(lowVal, higVal).yield;
		};
	}
}

PClip : PInRange {
	embedInStream {|inval|
		var evtStr= pattern.asStream;
		var lowStr= lo.asStream;
		var higStr= hi.asStream;
		var outVal, lowVal, higVal;
		loop{
			outVal= evtStr.next(inval);
			if(outVal.isNil, {^inval});
			lowVal= lowStr.next(outVal);
			if(lowVal.isNil, {^inval});
			higVal= higStr.next(outVal);
			if(higVal.isNil, {^inval});
			
			inval= outVal.clip(lowVal, higVal).yield;
		};
	}
}

PWrap : PInRange {
	embedInStream {|inval|
		var evtStr= pattern.asStream;
		var lowStr= lo.asStream;
		var higStr= hi.asStream;
		var outVal, lowVal, higVal;
		loop{
			outVal= evtStr.next(inval);
			if(outVal.isNil, {^inval});
			lowVal= lowStr.next(outVal);
			if(lowVal.isNil, {^inval});
			higVal= higStr.next(outVal);
			if(higVal.isNil, {^inval});
			
			inval= outVal.wrap(lowVal, higVal).yield;
		};
	}
}

PSchmidt : PInRange {
	embedInStream {|inval|
		var state= 0;
		var evtStr= pattern.asStream;
		var lowStr= lo.asStream;
		var higStr= hi.asStream;
		var outVal, lowVal, higVal;
		loop{
			outVal= evtStr.next(inval);
			if(outVal.isNil, {^inval});
			lowVal= lowStr.next(outVal);
			if(lowVal.isNil, {^inval});
			higVal= higStr.next(outVal);
			if(higVal.isNil, {^inval});
			
			if(state==1, {
				if(outVal<lowVal, {
					state= 0;
				});
			}, {
				if(outVal>higVal, {
					state= 1;
				});
			});
			inval= state.yield;
		};
	}
}

PLastValue : FilterPattern {
	var <>diff;
	*new {|pattern, diff= 0.01|
		^super.newCopyArgs(pattern, diff);
	}
	storeArgs {^[pattern, diff]}
	embedInStream {|inval|
		var evtStr= pattern.asStream;
		var difStr= diff.asStream;
		var outVal, difVal;
		var prev= 0, hold;
		loop{
			outVal= evtStr.next(inval);
			if(outVal.isNil, {^inval});
			difVal= difStr.next(outVal);
			if(difVal.isNil, {^inval});
			
			if((outVal-prev).abs>difVal, {
				hold= prev;
			});
			if(hold.isNil, {hold= outVal});
			inval= hold.yield;
			prev= outVal;
		};
	}
}
