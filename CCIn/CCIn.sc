CCIn {
	classvar <>verbose = true, <>softWithin = 0.05, lastSrc, lastChan, lastNum;
	var <busses, <responder, <server, <>source;
	
	*new { | server, source | 
		^super.new.init(server, source) 
		}
	
	init { | serverArg, sourceArg | 
			
		server = serverArg;
		source = sourceArg;
		
		busses = Array.fill(16, {()} );
		
		responder = CCResponder({ |src,chan,num,value|
			if (verbose and:{lastNum != num or:{ lastChan != chan or:{ lastSrc != src}}} ) { 
				("| CCIn" + "src:" + src + "chan:" + chan + "num:" + num + "|").postln; 
				lastNum = num; lastChan = chan ; lastSrc = src;
				};
			if ( source.isNil or:{ src == source } and: {busses[chan][num].notNil} ) {
				this.prSetBusValue(chan, num, value/127, busses[chan][num].last) 
				};
			});
		}
	
	prSetBusValue{ |chan, num, value, last|
		busses[chan][num].bus.get( { |busValue|			if ( abs(last - busValue) < 1e-6 or: { abs(busValue-value) <= softWithin }) {
				busses[chan][num].bus.set(value);
				busses[chan][num].last = value;
				}
			})
		}
	
	prGetBus { |chan, num|
		if ( busses[chan][num].isNil ) {
			busses[chan][num] = ();
			busses[chan][num].bus = Bus.control(server,1);
			busses[chan][num].last = 0;
			};
		^busses[chan][num].bus
		}
	
	kr { |chan = 0, num = 0, spec = \amp, lag = 0.05|
		var outArray = [chan, num, spec, lag].flop.collect{ |args| 
			var ch, n, sp, lg;
			# ch, n, sp, lg = args;
			(sp.asSpec.map( In.kr(this.prGetBus(ch, n).index) ).lag3(lg))
			};
		if (outArray.size>1) {^outArray} {^(outArray[0])} //fix to work with muliout
		}
		
	free {
		responder.remove;
		16.do{  |ch| busses[ch].do{ |num| num.bus.free } };
		super.free;
		}
}
