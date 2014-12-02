// wslib 2011

/*


x = ActionFunc( \thresh, { |val, dir| [ val, dir ].postln; }, 0.5 );

(
w = Window().front;
w.addFlowLayout;
z = SmoothSlider( w, 30@392 );
z.action = { |sl| x.value( sl.value ) };

)

(
x = ActionFuncList( 
	[ \speedAbove, { |val, speed| [val, speed, \speedAbove].postln }, 2 ],
	[ \match, { |val, index| [val, \match, index].postln }, [0.0,1.0] ],
	[ \changeDirection, { |val, dir| [ val, dir ].postln } ]
);
)

(
// nested (upwards speed above)
x = ActionFunc(  \speedAbove,
		[ \up, { |val, up, speed|  [val, speed, [\up,\speedAbove] ].postln } ],
	 	2
	 );
)

x = [ \delay, { |...args| args.postln; }, 0.1 ].asActionFunc;

*/

ActionFuncList {
	
	var <>nodes;
	
	*new { |...nodes|
		^super.newCopyArgs( nodes ).init;
	}
	
	init {
		nodes = nodes.collect(_.asActionFunc);
	}
	
	addAction { |type, action, extra, startVal|
		nodes = nodes ++ [ ActionFunc( type, action, extra, startVal ? this.lastVal ) ];
	}
	
	removeAll { 
		nodes = [];
	}
	
	lastVal {
		nodes[0].tryPerform( \lastVal ) ? 0;
	}
	
	value { |val|
		^nodes.collect(_.value(val)); // return array of results
	}
	
}

ActionFunc {
	
	classvar <typeDict;
	
	var <type, <>action, <>extra, <>lastVal = 0, <>lastTime;
	var <>testFunc, <>deltaTime;
	var <>task; // may be used by some actions
	
	*new { |type, action, extra, startVal| // extra can be anything
		action =  action.asActionFunc; // allow nested nodes
		^super.newCopyArgs( type ? \thresh, action, extra ? 0.5, startVal ? 0 ).init;
	}
	
	init {
		lastTime = thisThread.seconds;
		testFunc = typeDict[ type ] ?? { { |node, val| val } }; // thru func if not found
	}
	
	type_ { |newType|
		type = newType;
		testFunc = typeDict[ type ] ?? { { |node, val| val } };
	}
	
	asActionFunc { ^this }
	
	*initClass {
		typeDict = ( // return nil if action shouldn't fire
		
			\thresh: { |node, val = 0|
				case { ( val > node.extra ) && { node.lastVal <= node.extra } } 
					{ \up; }
					{ ( val < node.extra ) && { node.lastVal >= node.extra } }
					{ \down; }
					{ nil; };		
			},
			
			\threshUp: { |node, val = 0|
				if( ( val > node.extra ) && { node.lastVal <= node.extra } ) {
					val;
				} { 
					nil;
				};
			},
			
			\threshDown: { |node, val = 0|
				if( ( val < node.extra ) && { node.lastVal >= node.extra } ) {
					val;
				} { 
					nil;
				};
			},
			
			\inRange: { |node, val = 0| // extra: [min, max ? 1]
				var range;
				range = (node.extra.asCollection ++ #[1])[..1];
				if( val.inclusivelyBetween( *range ) ) {
					val;
				} {
					nil;
				};
			},
			
			\match: { |node, val = 0|
				var index;
				index = node.extra.asCollection.indexOf( val );
				if( index.notNil ) {
					index;
				} {
					nil;
				};
			},
			
			\up: { |node, val = 0|
				if( val > node.lastVal ) { val } { nil };
			},
			
			\down: { |node, val = 0|
				if( val < node.lastVal ) { val } { nil };
			},
			
			\change: { |node, val = 0|
				if( val != node.lastVal ) { val } { nil };
			},
			
			\noChange: { |node, val = 0| 
				if( node.lastVal == val ) { val; } { nil; };
			},
			
			\changeDirection: { |node, val = 0|
				var direction, result;
				direction = (node.lastVal - val).sign;
				result = if ( direction != node.extra ) {
					if( direction < node.extra ) { \up; } { \down; };
				} { 
					nil;
				};
				node.extra = direction; // store direction in extra
				result;
			},
			
			\speed: { |node, val = 0 ...moreArgs| // just measure and return the speed
				(val - node.lastVal).abs / node.deltaTime; 
			},
			
			\speedAbove: { |node, val = 0|
				var speed;
				speed =  (val - node.lastVal).abs / node.deltaTime;
				if( speed > node.extra ) {
					speed; // return speed
				} {
					nil;
				};
			},
			
			\speedBelow: { |node, val = 0|
				var speed;
				speed =  (val - node.lastVal).abs / node.deltaTime;
				if( speed < node.extra ) {
					speed; // return speed
				} {
					nil;
				};
			},
			
			\wait: { |node, val = 0| // ignore fast trigger
				if( node.task.isPlaying.not ) {
					node.task = Task({ node.extra.wait; }).start;
					val;
				} {
					nil; 
				};
			},	
			
			\within: { |node, val = 0| // only if a new trigger comes in within <extra> s
				if( node.task.isPlaying.not ) {
					node.task = Task({ node.extra.wait; }).start;
					nil;
				} {
					node.task.stop; 
					node.task = nil;
					val; 
				};
			},
			
			\delay: { |node, val = 0 ...moreArgs| // delay the action, but get the latest value
				if( node.task.isPlaying.not ) {
					node.task = Task({ 
						node.extra.wait; 
						node.action.value( node.lastVal, *moreArgs );
					}).start;
				};
				nil;
			},
			
			\slew: { |node, val = 0 ...moreArgs| // linear slew
				var speed, slew, interval, current, goal;
				
				slew = node.extra.asCollection[0];
				interval = node.extra.asCollection[1] ? 0.01;
				
				current = node.lastVal;
				goal = val;
				
				if( node.task.isPlaying.not ) {
					node.task = Task({ 
						var secs, maxJump;
						maxJump = slew * interval;
						while { current != goal } {
							goal = node.lastVal;
							current = goal.clip( 
								current - maxJump,
								current + maxJump );
							node.action.value( current, goal, *moreArgs );
							interval.wait;
						};
						node.action.value( current, goal );
					}).start;
				};
				nil;
			}
			
		);
	}
	
	value { |val ...moreArgs|
		var result, now;
		now = thisThread.seconds;
		deltaTime =  now - lastTime;
		result = testFunc.value( this, val, *moreArgs );
		if( result.notNil ) {
			result = action.value( val, result, *moreArgs );
		};
		lastTime = now;
		lastVal = val;
		^result;
	}
	
	storeArgs { ^[ type, action, extra, lastVal ] }
	
}

+ Array {
	asActionFunc { 
		^ActionFunc( *this );
	}
}

+ Object {
	asActionFunc {
		^nil
	}
}

+ Symbol {
	asActionFunc { |action, extra, startVal|
		^ActionFunc( this, action, extra, startVal );
	}
}

+ AbstractFunction {
	asActionFunc { |type, extra, startVal|
		if( type.isNil ) {
			^this // a Function is a valid ActionFunc too
		} {	
			ActionFunc( type, this, extra, startVal );
		};
	} 
}

