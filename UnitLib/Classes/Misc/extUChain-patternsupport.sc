+ UChain {
	
	*defaultProtoUChain {
		var chain;
		chain = UChain.fromPreset( \default );
		chain[0].defName = \default;
		^chain;
	}
	
	*defaultProtoEvent {
		^this.defaultProtoUChain.asProtoEvent;
	}
	
	asProtoEvent {
		var evt;
		evt = ();
		evt[ \play ] = {
			var newChain, freqs;
			freqs = ~freq = ~detunedFreq.value;
			if( ~isRest != true && { freqs.isRest.not }) {
				newChain = this.deepCopy;
				newChain.fromEnvironment;
				newChain.prepareWaitAndStart; // beware of different wait times per event
			};
		};
		^evt;
	}
	
	fromEnvironment { |env|
		var unitKeysValues = Order();
		env = env ? currentEnvironment ?? { () };
		
		env.keys.do({ |item| 
			var key = item.asString;
			var index;
			if( key.includes( $: ) ) {
				#index, key = key.split( $: );
				index = index.interpret;
				key = key.asSymbol;
				if( (unitKeysValues[ index ] ? []).pairsAt( key ).notNil ) {
					unitKeysValues[ index ].pairsPut( key, env[ item ].value ); 
				} {
					unitKeysValues[ index ] = unitKeysValues[ index ]
						.addAll( [ key, env[ item ].value ] )
				};
			} {
				if( key.includes( $. ) ) {
					key = key.split( $. ).first.asSymbol;
				} {
					key = key.asSymbol;
				};
				units.do({ |unit, index|
					if( unit.keys.includes( key ) && {
						 (unitKeysValues[ index ] ? []).pairsAt( key ).isNil
					} ) {
						unitKeysValues[ index ] = unitKeysValues[ index ]
							.addAll( [ item, env[ item ].value ] )
					}
				});
			}
		});
		
		unitKeysValues.do({ |item, index|
			var unit;
			unit = units[ index ];
			if( unit.notNil ) {
				unit.set( *item );
			} {
				"unit #% not available in chain\n".postf( index );
			};
		});
				
		~fadeIn.value !? this.fadeIn_(_);
		~fadeOut.value !? this.fadeOut_(_);
		~gain.value !? this.gain_(_);
		~track.value !? this.track_(_);
		~sustain.value !? this.duration_(_);
	}
	
}

+ UScore {
	
	*fromPattern { |pattern, proto, startTime = 0, maxEvents = 200|
		^this.new.fromPattern( pattern, proto, startTime, maxEvents );
	}
	
	fromPattern { |pattern, proto, startTime = 0, maxEvents = 200|
		var newChain;
		var stream, time, count = 0, event;
		
		time = startTime ? 0;
		
		proto = proto ?? { 
			var chain;
			chain = UChain.fromPreset( \default );
			chain[0].defName = \default;
			chain;
		};
		
		stream = pattern.asStream;
		
		while { (event = stream.next(Event.default)).notNil && 
				{ (count = count + 1) <= maxEvents } } { 
			event.use({
				
				newChain = proto.deepCopy;
				newChain.fromEnvironment;
				newChain.startTime = time;
				
				this.add( newChain );
				
				time = time + event.delta;
			});	
		};
	}
}

+ Pattern {
	asUScore { |proto, maxEvents = 200|
		^UScore.fromPattern( this, proto, 0, maxEvents );
	}
}