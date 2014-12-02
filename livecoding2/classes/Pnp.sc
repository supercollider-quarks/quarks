Pnp : FilterPattern {
	var <>proxy, <>timepattern, <>index, <>cachesize;
	
	*new { arg proxy, pattern, timepattern = 1.0, index, cachesize=32;
		^super.new(pattern).proxy_(proxy).timepattern_(timepattern).index_(index).cachesize_(cachesize)
	}
	
	embedInStream { |inval|
		var stream = pattern.asStream;
		var times = timepattern.asStream;
		var obj, index, name, time, prevSource;
		prevSource = if(index.isNil) { proxy.source } { proxy.objects.at(index).source };
		
		while {
			obj = stream.next(inval);
			time = times.next(inval);
			obj.notNil and: { time.notNil }
		} {		
			proxy.put(index, obj); 			
			inval = Event.silent(time).yield;
			if(inval.isNil) { 
				proxy.put(index, prevSource); 
				^nil.yield; 
			}
		
		};
		
		proxy.source = prevSource;
		^inval
		
	}
}
	
/*
Pnp : FilterPattern {
	var <>proxy, <>timepattern, <>cachesize;
	
	*new { arg proxy, pattern, timepattern = 1.0, cachesize=32;
		^super.new(pattern).proxy_(proxy).timepattern_(timepattern).cachesize_(cachesize)
	}
	
	embedInStream { |inval|
		var objcache = Array.new;
		var namecache = Array.new;
		var stream = pattern.asStream;
		var times = timepattern.asStream;
		var obj, index, name, time, control;
		
		while {
			obj = stream.next(inval);
			time = times.next(inval);
			obj.notNil and: { time.notNil }
		} {
			index = objcache.indexOf(obj);
			if(index.notNil) {
				obj = namecache.at(index);
			};
						
			proxy.source = obj;
				
			if(index.isNil and: { objcache.size <= cachesize }) {
				
			// maybe implement a method in proxy instead of digging for name.
				control = proxy.objects.first;
				name = control.synthDef.name.asSymbol;
				objcache = objcache.add(obj);
				namecache = namecache.add(name);
			};
			
			inval = Event.silent(time).yield;
			if(inval.isNil) { proxy.source = nil; ^nil.yield; }
		
		};
		proxy.source = nil;
		^inval
		
	}

}

*/