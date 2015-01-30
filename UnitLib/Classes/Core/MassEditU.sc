/*
    Unit Library
    The Game Of Life Foundation. http://gameoflife.nl
    Copyright 2006-2011 Miguel Negrao, Wouter Snoei.

    GameOfLife Unit Library: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GameOfLife Unit Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GameOfLife Unit Library.  If not, see <http://www.gnu.org/licenses/>.
*/

MassEditU : U { // mimicks a real U, but in fact edits multiple instances of the same
	
	var <units, <>argSpecs;
	var <>autoUpdate = true;
	
	*new { |units| // all units have to be of the same Udef
		^super.newCopyArgs.init( units );
	}
	
	guiColor { ^units.detect(_.respondsTo(\guiColor)) !? _.guiColor ?? { Color.clear; } }
	
	init { |inUnits|
		var firstDef, defs;
		var dkey, dval;
		units = inUnits.asCollection;
		defs = inUnits.collect(_.def);
		firstDef = defs[0];
		if( defs.every({ |item| item == firstDef }) ) {
			def = firstDef;
			if( def.isKindOf( MultiUdef ).not or: {
				dkey = def.defNameKey;
				dval = units[0].get( dkey );
				units.every({ |unit|
					unit.get( dkey ) == dval
				});
			}) {	
				argSpecs = def.argSpecs( inUnits[0] );
			} {
				argSpecs = [ def.getArgSpec( dkey, units[0] ) ];
			};
			
			argSpecs = argSpecs.collect({ |argSpec|
				var values, massEditSpec, value;
				values = units.collect({ |unit|
					unit.get( argSpec.name );
				});
				
				if( values.any(_.isUMap) ) {
					massEditSpec = MassEditUMapSpec( MassEditUMap( values ) );
				} {	
					massEditSpec = argSpec.spec.massEditSpec( values );
				};
				if( massEditSpec.notNil ) {
					ArgSpec( argSpec.name, massEditSpec.default, massEditSpec,
						argSpec.private, argSpec.mode ); 
				} {
					nil;
				};
			}).select(_.notNil);	
			args = argSpecs.collect({ |item| [ item.name, item.default ] }).flatten(1);
			this.changed( \init );
		} {
			"MassEditU:init - not all units are of the same Udef".warn;
		};
	}
	
	units_ { |inUnits| 
		this.disconnect;
		this.init( inUnits ); 
	}
	
	getArgSpec { |name|
		name = name.asSymbol;
		^argSpecs.detect({ |item| item.name == name });
	}

	guiCollapsed { ^units.select(_.isKindOf(U) ).any(_.guiCollapsed) }
	guiCollapsed_ { |bool|
		units.select(_.isKindOf(U) ).do(_.guiCollapsed_(bool));
		this.changed( \init );
	}
	
	connect {
		units.do(_.addDependant(this));
	}
	
	disconnect {
		units.do(_.removeDependant(this));
	}
	
	resetArg { |key| // doesn't change the units
		var spec, values;
		if( key.notNil ) {
			spec = this.getSpec( key );
			values = units.collect({ |unit| unit.get( key ) });
			if( spec.class != MassEditUMapSpec and: { values.any(_.isUMap).not }) {
				this.setArg( key, spec.massEditValue( values ) );
			};
		} {
			this.keys.do({ |key| this.resetArg( key ) });
		};
	}
	
	update { |obj, what ...args|
		if( autoUpdate ) { 
			if( this.keys.includes( what ) ) {
				this.resetArg( what );
			}; 
		}
	}
	
	set { |...args|
		var autoUpdateWas;
		
		// disable auto updating to prevent loop
		autoUpdateWas = autoUpdate;
		autoUpdate = false;
		
		args.pairsDo({ |key, value|
			var values;
			this.setArg( key, value );
			values = this.getSpec( key ).massEdit( units.collect(_.get(key) ), value );
			units.do({ |unit, i|
				unit.set( key, values[i] );
			});
		});
		
		// re-enable auto updating
		autoUpdate = autoUpdateWas;
	}
	
	getSpec { |name|
		^units[0].getSpec( name );
	}
	
	canUseUMap { |key, umapdef|
		^units.first.canUseUMap( key, umapdef );
	}
	
	defName { ^((this.def !? { this.def.name }).asString + "(% units)".format( units.size )).asSymbol }
	
	def_ { |def|  units.do(_.def_( def ) ); this.init( units ); }
	
	checkDef { units.do(_.checkDef) }
	
	storeArgs { ^[ units ] }
	
}


MassEditUChain {
	
	var <uchains;
	var <units;
	var <>prepareTasks;
	
	*new { |uchains|
		^super.newCopyArgs( uchains ).init;
	}
	
	lockStartTime { ^uchains.any(_.lockStartTime) }
	lockStartTime_ { |bool| ^uchains.do(_.lockStartTime_(bool)) }
	
	init {
		var allDefNames = [], allUnits = Order();
		
		uchains.do({ |uchain|
			uchain.units.select({|x| x.def.class != LocalUdef}).do({ |unit|
				var defName, index;
				defName = unit.defName;
				if( allDefNames.includes( defName ).not ) {
					allDefNames = allDefNames.add( defName );
				};
				index = allDefNames.indexOf( defName );
				allUnits.put( index, allUnits[ index ].add( unit ) );
			});
		});
		
		units = allUnits.asArray.collect({ |item, i|
			if( allDefNames[i].notNil ) {
				if( item.size == 1 ) {
					item[0];
				} {
					MassEditU( item );
				};
			} {
				nil
			};
		}).select(_.notNil);
		
		this.changed( \init );
	}
	
	connect {
		uchains.do(_.addDependant(this));
	}
	
	disconnect {
		uchains.do(_.removeDependant(this));
	}
	
	update { |obj, what ...args|
		this.changed( what, *args );
	}
	
	groups { ^uchains.collect(_.groups).flatten(1); } // don't know any groups
	
	releaseSelf { ^uchains.collect(_.releaseSelf).every(_==true); }
	releaseSelf_ { |bool|
		uchains.do(_.releaseSelf_(bool));
	}
	
	getTypeColor {
		^Color( 
			*uchains.collect(_.getTypeColor).select(_.isKindOf( Color ) ).collect(_.asArray).mean
		 );
	}
	
	displayColor { 
		^if( uchains.any({ |item| item.displayColor != nil }) ) {
			this.getTypeColor
		}; 
	}
	
	displayColor_ { |color| 
		uchains.do({ |item| item.displayColor = color }); 
		this.changed( \displayColor, color );
	}
	
	fadeIn_ { |fadeIn = 0|
		var add = fadeIn - this.fadeIn;
		
		uchains.do({ |item|
			item.fadeIn_( item.fadeIn + add );
		});	
	}
	
	fadeOut_ { |fadeOut = 0|
		var add = fadeOut - this.fadeOut;
		
		uchains.do({ |item|
			item.fadeOut_( item.fadeOut + add );
		});	
	}
	
	fadeOut {
		^uchains.collect({ |item| item.fadeOut }).maxItem ? 0;
	}
	
	fadeIn {
		^uchains.collect({ |item| item.fadeIn }).maxItem ? 0;
	}
	
	fadeInCurve_ { |curve = 0|
		uchains.do({ |item|
			item.fadeInCurve_( curve );
		});
	}

	fadeOutCurve_ { |curve = 0|
		uchains.do({ |item|
			item.fadeOutCurve_( curve );
		});
	}

	fadeOutCurve {
		^uchains.collect({ |item| item.fadeOutCurve }).maxItem ? 0;
	}

	fadeInCurve {
		^uchains.collect({ |item| item.fadeInCurve }).maxItem ? 0;
	}

	useSndFileDur { // look for SndFiles in all units, use the longest duration found
		var durs;
		uchains.do(_.useSndFileDur);
	}
	
	getMaxDurChain { // get unit with longest non-inf duration
		var dur, out;
		uchains.do({ |uchain|
			var u_dur;
			u_dur = uchain.dur;
			if( (u_dur > (dur ? 0)) && { u_dur != inf } ) {
				dur = u_dur;
				out = uchain;
			};
		});
		^out;	
	}
	
	dur { // get longest duration
		var uchain;
		uchain = this.getMaxDurChain;
		if( uchain.isNil ) { 
			^inf 
		} {
			^uchain.dur;
		};
	}

    /*
	* sets same duration for all units
	* clipFadeIn = true clips fadeIn
	* clipFadeIn = false clips fadeOut
	*/
	dur_ { |dur = inf, clipFadeIn = true|
		var currentDur, mul;
		currentDur = this.dur;
		if( (currentDur != inf) && { dur != inf } ) {
			mul = dur.max(1.0e-11) / currentDur.max(1.0e-11);
			uchains.do({ |uchain|
				if( uchain.dur != inf ) {
					uchain.dur_( uchain.dur * mul, clipFadeIn );
				};
			});
		} {
		    uchains.do(_.dur_( dur ))
		};
		this.changed(\dur)
	}
	
	duration { ^this.dur }
	duration_ { |x| this.dur_(x)}
	
	muted { ^uchains.collect({ |ch| ch.muted.binaryValue }).mean > 0.5 }
	muted_ { |bool| 
		uchains.do({ |ch| ch.muted = bool });
		this.changed( \muted );
	} 
	
	startTime {
		^uchains.collect({ |ch| ch.startTime ? 0 }).minItem;
	}
	
	startTime_ { |newTime|
		var oldStartTime, delta;
		oldStartTime = this.startTime;
		if( newTime.notNil ) {
			delta = newTime - oldStartTime;
		} {
			delta = 0;
		};
		if( delta != 0 ) {
			uchains.do({ |ch|
				ch.startTime = (ch.startTime ? 0) + delta;
			});
		};
	}
	
	setGain { |gain = 0| // set the average gain of all units that have a u_gain arg
		var mean, add;
		mean = this.getGain;
		add = gain - mean;
		uchains.do({ |uchain|
			 uchain.setGain( uchain.getGain + add );
		});
		this.changed( \gain );		
	}
	
	getGain {
		var gains;
		gains = this.uchains.collect(_.getGain);
		if( gains.size > 0 ) { ^gains.mean } { ^0 };
	}
	
	
	start { |target, latency|
		^uchains.collect( _.start( target, latency ) );
	}
	
	free { uchains.do(_.free); }
	stop { uchains.do(_.stop); }
	
	release { |time|
		uchains.do( _.release( time ) );
	}

	prepare { |target, loadDef = true, action|
		action = MultiActionFunc( action );
	     uchains.do( _.prepare(target, loadDef, action.getAction ) );
	     action.getAction.value; // fire action at least once
	     ^target; // return array of actually prepared servers
	}

	prepareAndStart{ |target, loadDef = true|
		var task, cond;
		cond = Condition(false);
		task = fork { 
			var action;
			action = { cond.test = true; cond.signal };
			target = this.prepare( target, loadDef, action );
			cond.wait;
	       	this.start(target);
		};
	}
	
	waitTime { ^this.units.collect(_.waitTime).sum }
	
	prepareWaitAndStart { |target, loadDef = true|
		var task;
		task = fork { 
			this.prepare( target, loadDef );
			this.waitTime.wait; // doesn't care if prepare is done
	       	this.start(target);
	       	prepareTasks.remove(task);
		};
	}

	dispose { uchains.do( _.dispose ) }
	
	// indexing / access
		
	at { |index| ^units[ index ] }
		
	last { ^units.last }
	first { ^units.first }
	
	printOn { arg stream;
		stream << "a " << this.class.name << "(" <<* units.collect(_.defName)  <<")"
	}
	
	gui { |parent, bounds, score| ^UChainGUI( parent, bounds, this, score ) }
	
}