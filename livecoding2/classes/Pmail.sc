
Pinbox : EventPatternProxy {
	var <>channel;
	
	*new { arg channel, quant, condition = true, source;
		^super.new.source_(source).channel_(channel).condition_(condition).quant_(quant)
	}
	
	receiveEvent { arg inval;
		var incoming, news = inval.eventAt(\news), values;
		if(news.isNil) { ^nil };
		
		^if(channel.isSequenceableCollection) {
			channel.do { arg key;
				if(news.at(key).notNil) {
					values = values.add(news.at(key));
				}
			};
			if(values.isEmpty) { nil } { this.class.parallelise(values) }
		} {
			news.at(channel)
		}
	}
	/*
	embedInStream { arg inval;
		if(channel.isKindOf(Pattern)) {
			channel.do { arg each;
				inval = Pinbox(each, quant, condition, source).embedInStream(inval)
			};
		} {
			inval = super.embedInStream(inval);
		};
		inval ?? { ^nil.yield };
		^inval;
	}
	*/

	
}

Poutbox : EventPatternProxy {
	*new {
		^super.new.init
	}
	init {
		this.envir = this.class.event;
		this.source_(Pfunc { |inval| composeEvents(inval, envir) })
	}
	getNews {
		var ev = super.get(\news);
		if(ev.isNil) { super.set(\news, ev = ()) };
		^ev
	}
	set { arg ... args;
		this.getNews.putPairs(args)
	}
	get { arg ... args;
		this.getNews.getPairs(args)
	}
}

// Pmail(\key, \channel, val, \channel, val...)

Pmail : Poutbox {
	classvar <>all;
	var <>key;
	
	*initClass { 
		all = IdentityDictionary.new;
	}
	*new { arg key ... args;
		var res = all.at(key);
		if(res.isNil) { res = super.new; all.put(key, res) };
		^res.set(*args);
	}

}

// Precv(\key, \channel, ... args)

// maybe we don't need such a thing for global access.
/*

Precv : Pinbox {
	classvar <>all;
	var <>key;
	
	*initClass { 
		all = IdentityDictionary.new;
	}
	*new { arg key, channel, quant, condition, source;
		var res = all.at(key);
		if(res.isNil) { res = super.new; all.put(key, res) };
		if(channel.notNil) { res.channel = channel };
		if(source.notNil) { res.source = source };
		if(condition.notNil) { res.condition = condition };
		if(quant.notNil) { 
			res.quant = if(quant < 0) { nil } { quant } // -1 overrides value.
		};
		^res
	}
}

*/