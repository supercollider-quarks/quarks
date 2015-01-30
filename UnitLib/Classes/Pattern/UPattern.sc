UPattern : UChain {
	
	var repeats = inf;
	var <>sustain = 1;
	var <>timeToNext = 1;
	var task;
	
	prPrepareUnit { |unit|
		var prepareThese;
		prepareThese = unit.valuesToPrepare.select({ |item|
			item.isKindOf( UMap ) && { item.def.isKindOf( FuncUMapDef ) };
		});
		prepareThese.do({ |umap|
			this.prPrepareUnit( umap );
		});
		prepareThese.do({ |umap|
			umap.def.doFunc( umap );
		});
	}
	
	prPatternsToValues { |unit|
		unit.args.pairsDo({ |key, item|
			if( item.isKindOf( UMap ) ) {
				if( item.def.isKindOf( UPatDef ) ) {
					unit.set( key, item.value );
				} {
					this.prPatternsToValues( item );
				};
			};
		});
	}
	
	prResetStreams { |unit|
		unit.args.pairsDo({ |key, item|
			if( item.isKindOf( UMap ) ) {
				item.resetStream;
				this.prResetStreams( item );
			};
		});
	}
	
	next {
		var next;
		next = UChain( *units.deepCopy );
		next.fadeIn = this.fadeIn;
		next.fadeOut = this.fadeOut;
		next.units.do({ |unit|
			this.prPrepareUnit( unit );
			this.prPatternsToValues( unit );
		});
		next.duration = this.getSustain;
		^next;
	}
	
	getSustain {
		if( sustain.isKindOf( UMap ) && { sustain.def.isKindOf( FuncUMapDef ) } ) {
			this.prPrepareUnit( sustain );
			sustain.def.doFunc( sustain );
		};
		^sustain.next;
	}
	
	getTimeToNext {
		if( timeToNext.isKindOf( UMap ) && { timeToNext.def.isKindOf( FuncUMapDef ) } ) {
			this.prPrepareUnit( timeToNext );
			timeToNext.def.doFunc( timeToNext );
		};
		^timeToNext.next;
	}
	
	prepareWaitAndStart { |target|
		task.stop; // first stop any existing playback task
		units.do({ |unit| this.prResetStreams( unit ); });
		task = Task({
			var time = 0, n = 0;
			var next, timeToNext;
			while { (time <= duration) && (n < repeats) } {
				next = this.next;
				timeToNext = this.getTimeToNext;
				next.prepareWaitAndStart( target );
				timeToNext.wait;
				time = time + timeToNext;
				n = n + 1;
			};
			"done playing".postln;
		}).start;
	}
	
	prepareAndStart { |target|
		this.prepareWaitAndStart( target );
	}
	
	start { |target|
		this.prepareWaitAndStart( target );
	}
	
	stop {
		task.stop;
	}
	
	release {
		task.stop;
	}
	
}