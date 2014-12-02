//(c) 2009, Marije Baalman (nescivi)
// gnu/gpl

NPdef : Pdef {

	*initClass{
		Event.addEventType( \npset,
			#{
				var freqs, lag, dur, strum, bndl, msgFunc;
				var proxy;
				freqs = ~freq = ~detunedFreq.value;
				proxy = ~id.value;
				
				if (freqs.isKindOf(Symbol).not) {
					freqs = ~freq;
					~amp = ~amp.value;	
					bndl = proxy.controlKeys.envirPairs;
					
					thisThread.clock.sched(~lag + ~timingOffset, { 
						//	[proxy.dump, bndl].postln;
						proxy.set( *bndl );
					});
				};
			};
		);
	}

	*new{ |proxy,src, key|
		var pat,res;
		key = key ?? { proxy.key };

		if ( proxy.source.isNil, {^nil});
		res = super.at(key); // look for the pdef. If it exists replace the source
		if ( src.isNil, { ^res });
		src = src <> ( type: \npset, id: proxy );
		if(res.isNil) {
			res = super.new(key,src);
		} {
			res.source = src;
		}
		^res
	}

}

NPxdef : Pdef {

	*initClass{
		Event.addEventType( \npxset,
			#{
				var freqs, lag, dur, strum, bndl, msgFunc;
				var proxy;
				freqs = ~freq = ~detunedFreq.value;
				proxy = ~id.value;
				
				if (freqs.isKindOf(Symbol).not) {
					freqs = ~freq;
					~amp = ~amp.value;	
					bndl = proxy.controlKeys.envirPairs;
					
					thisThread.clock.sched(~lag + ~timingOffset, { 
						//	[proxy.dump, bndl].postln;
						proxy.xset( *bndl );
					});
				};
			};
		);
	}

	*new{ |proxy,src, key|
		var pat,res;
		key = key ?? { proxy.key };

		if ( proxy.source.isNil, {^nil});

		res = super.at(key); // look for the pdef. If it exists replace the source
		if ( src.isNil, { ^res });
		src = src <> ( type: \npxset, id: proxy );
		if(res.isNil) {
			res = super.new(key,src);
		} {
			res.source = src;
		}
		^res
	}

}