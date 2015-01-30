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


// a UChain is a serial chain of U's (also called "units").
// they are played together in a Group. There should be only one chain playing
// per Server at a time, although it is not impossible to play multiple instances
// of at once.
//
// UChain implements the UEvent interface, therefore it has a startTime, track, duration, muted, releaseSelf variables.

UChain : UEvent {
	
	classvar <>verbose = false;
	classvar <>groupDict;
	classvar <>presetManager;
	
	classvar <>makeDefaultFunc;
	classvar <>nowPreparingChain;

	var <units; //, <>groups;
	var <prepareTasks;
	var <>preparedServers;
	var <muted = false;
	
	var <>addAction = \addToHead;
	var <>global = false;
	var <>ugroup;
	var <>handlingUndo = false;
	
	var <lastTarget;
	
	var <>serverName; // name of a server to match for playback

	*initClass {
		
		Class.initClassTree( PresetManager );
		
		presetManager = PresetManager( this, [ \default, { UChain.default } ] )
			.getFunc_({ |obj| obj.deepCopy })
			.applyFunc_({ |object, preset|
			 	object.fromObject( preset );
		 	});
		
		groupDict = IdentityDictionary( );
		makeDefaultFunc = {
			UChain( [ \sine, [ \freq, 440 ] ], \output ).duration_(10).fadeIn_(1).fadeOut_(1);
		};
	}
	
	*new { |...args|
		^super.new.init( args );
	}
	
	*presets { ^presetManager.presets.as(IdentityDictionary) }
	
	fromObject { |obj|
		this.units = obj.value.units.deepCopy;
		this.duration = this.duration; // update duration of units
	}
	
	*fromObject { |obj|
		^obj.value.deepCopy;
	}
	
	*fromPreset { |name| ^presetManager.apply( name ) }
	
	fromPreset { |name| ^presetManager.apply( name, this ); }
	
	handleUndo { |obj|
		if( obj.notNil ) {
			handlingUndo = true;
			this.fromObject( obj );
		};
	}
	
	*clear {
		groupDict.do({ |groups| 
			groups.do({ |group| if( group.isPlaying ) { 
					group.free 
				} {
					group.changed( \n_end );
				}; 
			});
		});
		groupDict = IdentityDictionary();
	}
	
	*current { ^UChainGUI.current !? _.chain }


	/*
	* Syntaxes for UChain creation:
	* UChain(\defName)
	* UChain(startTime,\defName1,\defName2,...)
	* UChain(startTime,track,\defName1,\defName2,...)
	* UChain(startTime,track,duration,\defName1,\defName2,...)
	* Uchain(startTime,track,duration,releaseSelf,\defName1,\defName2,...)
	*/

	init { |args|
		var tempDur;
		
		if( args[0].isNumber ) { 
			startTime = args[0]; 
			args = args[1..] 
		};
		if( args[0].isNumber ) { 
			track = args[0]; 
			args = args[1..] 
		};
		if( args[0].isNumber ) { 
			tempDur = args[0]; 
			args = args[1..] 
		};
		if( args[0].class.superclass == Boolean ) { 
			releaseSelf = args[0]; args = args[1..] 
		};
		
		units = args.collect(_.asUnit);
		if( tempDur.notNil ) { this.duration = tempDur };
		
		prepareTasks = [];
		
		units.reverse.do(_.uchainInit( this ));
		
		this.changed( \init );
	}

    name { 
	    var names = [], last;
	    units.do({ |unit|
		    var name;
		    name = unit.name.asSymbol;
		    if( last === name ) {
			    names[ names.size-1 ] = names[ names.size-1 ].add( name );
		    } {
			    names = names.add( [ name ] );
		    };
		    last = name;
	    });
	    ^names.collect({ |item|
		    if( item.size == 1 ) {
			    item[0]
		    } {
			    item[0] ++ " * " ++ item.size
		    };
	    }).asString;
	}

    *default_ { |chain| makeDefaultFunc = { chain.deepCopy } }
    *default { ^makeDefaultFunc.value; }

    //will this work ? Yes
	duplicate{
	    ^this.deepCopy;
	}
	
	// global setters (acces specific units inside the chain)
	
	prGetCanFreeSynths { // returns the units that can free synths (they will free the whole group)
		^units.select({ |unit| unit.canFreeSynth });
	}

	canFreeSynth{ ^this.prGetCanFreeSynths.size != 0 }
	
	prSetCanFreeSynths { |...args|
		units.do({ |unit|
			if( unit.canFreeSynth ) {
				unit.set( *args );
			};
		});
	}
	
	units_ { |newUnits|
		units = newUnits.collect(_.asUnit);
		this.changed( \units );
	}

    eventSustain { ^duration - this.fadeOut; }

	fadeIn_ { |fadeIn = 0|

		fadeIn = fadeIn.max(0).min(duration - this.fadeOut);

		units.do({ |unit|
		    var unitDur, unitFadeIn, unitFadeOut;
		    //each unit might have a different dur and fadeOut
			if( unit.def.canFreeSynth ) {
				unitDur = unit.get( \u_dur );
				if( unitDur != inf ) {
			        unitFadeOut = unit.get( \u_fadeOut );
			        unitFadeIn = fadeIn.min( unitDur - unitFadeOut );
			        unit.set( \u_fadeIn, unitFadeIn );
		        } {
				    unit.set( \u_fadeIn, fadeIn );
				}
			};
		});
		this.changed( \fadeIn );	
	}
	
	fadeOut_ { |fadeOut = 0|

		fadeOut = fadeOut.max(0).min(duration - this.fadeIn);

		units.do({ |unit|
		    var unitDur, unitFadeOut, unitFadeIn;
		    //each unit might have a different dur and fadeIn
			if( unit.canFreeSynth ) {
				unitDur = unit.get( \u_dur );
				if( unitDur != inf ) {
			        unitFadeIn = unit.get( \u_fadeIn );
			        unitFadeOut = fadeOut.min( unitDur - unitFadeIn );
			        unit.set( \u_fadeOut, unitFadeOut );
		        } {
				    unit.set( \u_fadeOut, fadeOut );
				}
			};
		});
		this.changed( \fadeOut );
	}
	
	fadeOut {
		^this.prGetCanFreeSynths.collect({ |item| item.get( \u_fadeOut ) }).maxItem ? 0;
	}
	
	fadeIn {
		^this.prGetCanFreeSynths.collect({ |item| item.get( \u_fadeIn ) }).maxItem ? 0;
	}

	fadeTimes { ^[this.fadeIn, this.fadeOut] }
	
	fadeInCurve_{ |curve = 0|
		this.prSetCanFreeSynths(\u_fadeInCurve, curve);
    this.changed( \fadeInCurve );
	}

	fadeOutCurve_{ |curve = 0|
		this.prSetCanFreeSynths(\u_fadeOutCurve, curve);
    this.changed( \fadeOutCurve );
	}

	fadeCurve_{ |curve = 0|
		this.fadeInCurve_(curve);
		this.fadeOutCurve_(curve.neg);
	}

	fadeInCurve {
		^this.prGetCanFreeSynths.collect({ |item| item.get( \u_fadeInCurve ) }).maxItem ? 0;
	}

	fadeOutCurve {
		^this.prGetCanFreeSynths.collect({ |item| item.get( \u_fadeOutCurve ) }).maxItem ? 0;
	}

	useSndFileDur { // look for SndFiles in all units, use the longest duration found
		var durs;
		units.do({ |unit|
			unit.allValues.do({ |val|
				if( val.isKindOf( AbstractSndFile ) ) {
					durs = durs.add( val.duration );
				}
			});
		});
		if( durs.size > 0 ) { // only act if a sndFile is found
			this.dur_( durs.maxItem );
		};
	}
	
	getMaxDurUnit { // get unit with longest non-inf duration
		var dur, out;
		units.do({ |unit|
			var u_dur;
			if( unit.canFreeSynth ) {
				u_dur = unit.get( \u_dur );
				if( (u_dur > (dur ? 0)) && { u_dur != inf } ) {
					dur = u_dur;
					out = unit;
				};
			};
		});
		^out;	
	}
	
	prGetChainsDur { // get longest duration
		var unit;
		unit = this.getMaxDurUnit;
		if( unit.isNil ) { 
			^inf 
		} {
			^unit.get( \u_dur );
		};
	}

    /*
	* sets same duration for all units
	* clipFadeIn = true clips fadeIn
	* clipFadeIn = false clips fadeOut
	*/
	prSetChainsDur { |dur = inf, clipFadeIn = true| //
		units.do(_.setDur(dur));
		if( releaseSelf ) {
			this.prSetCanFreeSynths( \u_doneAction, 14 );
		} {
			this.prSetCanFreeSynths( \u_doneAction, 14, \u_dur, inf );
		};
		if( clipFadeIn ) {
		    this.fadeIn = this.fadeIn.min(dur);
		    this.fadeOut = this.fadeOut.min(dur - this.fadeIn);
		} {
		    this.fadeOut = this.fadeOut.min(dur);
		    this.fadeIn = this.fadeIn.min(dur - this.fadeOut);
		}
	}

	duration_{ |dur|
        duration = dur;
        this.changed( \dur );
        this.prSetChainsDur(dur);
        this.fadeOut_(this.fadeOut.min(dur));
        this.fadeIn_(this.fadeIn.min(dur));
    }

    releaseSelf_ { |bool|

        if(releaseSelf != bool) {
	        releaseSelf = bool;
	        this.changed( \releaseSelf );
             this.prSetChainsDur(duration);
        }
    }
	
	dur { ^this.duration }
	dur_ { |x| this.duration_(x)}
	
	getTypeColor {
		^case {
	        this.displayColor.notNil;
        } {
	        this.displayColor;
        } { 
	        this.duration == inf 
	   } {
	        Color(0.4, 0.4, 0.8)
        } {
	       this.releaseSelf == true;
        } {
	        Color(0.768, 0.3,0.768);
        } {
	        Color(0.4, 0.6, 0.6);
        };
	}
	
	gain { ^this.getGain }
	gain_ { |gain = 0| ^this.setGain(gain) }
	
	muted_ { |bool|
		muted = bool.booleanValue;
		this.prGetCanFreeSynths.do({ |unit|
			unit.set( \u_mute, muted );
		});
		this.changed( \muted );
	}
	
	mute { this.muted = true; }
	unmute { this.muted = false; }
	
	setGain { |gain = 0| // set the average gain of all units that have a u_gain arg
		var mean, add;
		mean = this.getGain;
		add = gain - mean;
		this.prGetCanFreeSynths.do({ |unit|
			 unit.set( \u_gain, unit.get( \u_gain ) + add );
		});
		this.changed( \gain );		
	}
	
	getGain {
		var gains;
		gains = this.prGetCanFreeSynths.collect({ |item| item.get( \u_gain ) });
		if( gains.size > 0 ) { ^gains.mean } { ^0 };
	}
	
	setDoneAction { // set doneAction 14 for unit with longest non-inf duration
		var maxDurUnit;
		maxDurUnit = this.getMaxDurUnit;
		if( maxDurUnit.isNil ) { // only inf synths
			this.prGetCanFreeSynths.do({ |item, i|
		        	item.set( \u_doneAction, 14 );        			});
		} {	 
			this.prGetCanFreeSynths.do({ |item, i|
		        	if( item == maxDurUnit or: { item.get( \u_dur ) == inf } ) {
			        	item.set( \u_doneAction, 14 );
		        	} {
			        	item.set( \u_doneAction, 14 );
		        	};
	        	});
		};
	}

	 //events can become bigger
	trimEnd { |newEnd, removeFade = false|
		var delta = newEnd - startTime;
		if( delta > 0) {
			this.dur = delta;
			if( removeFade ) {
				this.fadeOut_(0)
			};
		}
	}

	//events can only become smaller
	cutEnd{ |newEnd, removeFade = false|
        var delta;

        if((this.startTime < newEnd) && (( this.startTime + this.dur ) > newEnd) ) {
            this.dur = newEnd - startTime;
            if( removeFade ) {
                this.fadeOut_(0)
            };
        }
    }

    //events can become bigger
	trimStart{ |newStart,removeFade = false|
		var delta1,delta2;
		if( lockStartTime.not ) {	
			delta1 = newStart - startTime;
			if(newStart < this.endTime) {
	            startTime = newStart;
				this.dur = this.dur - delta1;
				if(removeFade){
			        this.fadeIn = 0
				};
				if(delta1 > 0) {
					//do something when making event shorter
				} {	//making event bigger
					//do something when making event bigger
				}
	
			}
		};
	}

	//events can only become smaller
	cutStart{ |newStart, belongsToFolder = false, removeFade = false|
        var delta1;
	    if( belongsToFolder ) {
	        delta1 = newStart - startTime;
	        startTime = delta1.neg.max(0);
	        if( (this.startTime < newStart) && (this.endTime > newStart) ) {
                this.dur = this.dur - delta1;
                if( removeFade ){
                    this.fadeIn = 0
                };
                units.do( _.cutStart( delta1 ) );
            }
        } {

	        if( (this.startTime < newStart) && (this.endTime > newStart) ) {
                delta1 = newStart - startTime;
	            startTime = newStart;
                this.dur = this.dur - delta1;
                if(removeFade){
                    this.fadeIn = 0
                };
                 units.do( _.cutStart( delta1 ) );
            }

        }
	}
	
	bounce { |index = 0, path, action, replace = true, single = true|
		var tempChain, playbackUnit, dur, newAction;
		var usedBuses;
		path = path.getGPath.replaceExtension( "aiff" );
		dur = this.duration;
		
		tempChain = this.deepCopy;
		tempChain.units = tempChain.units[..index];
		
		if( single == true ) {
			usedBuses = tempChain.units.last.audioOuts.collect({ |item|
				tempChain.units.last.getAudioOut( item );
			});
		} {
			usedBuses = UChainAudioAnalyzer( tempChain ).usedBuses.sort;
		};
		
		usedBuses.do({ |bus, i|
			tempChain.add( U( \output, [ \bus, i ] ).setAudioIn( 0, bus ) );
		});
		
		if( replace == true ) {	
			playbackUnit = U( \diskSoundFile, [ \soundFile, DiskSndFile.newBasic(
					path, 
					(dur * 44100).floor, 
					usedBuses.size
				) ] 
			);
			
			usedBuses.do({ |bus, i|
				playbackUnit.setAudioOut( i, bus );
			});
			
			if( single == true ) {
				this.units = this.units.put( index, playbackUnit );
			} {
				this.units = [ playbackUnit ] ++ (this.units[index + 1..]);
			};
			
			this.duration = dur;
			newAction = {
				playbackUnit.soundFile.path = playbackUnit.soundFile.path;
				action.value;
			};
		};
		
		tempChain.writeAudioFile( path, sampleFormat: "float", 
			numChannels: usedBuses.size, 
			action: newAction ? action
		);
	}

	 makeView{ |i=0,minWidth, maxWidth| ^UChainEventView(this, i, minWidth, maxWidth) }

	isPlaying { ^units.any(_.isPlaying) }
	
	connect { units.do(_.connect) }
	disconnect { units.do(_.disconnect) }
	
	/// creation
	
	groups { ^groupDict[ this ] ? [] }
	
	groups_ { |groups| groupDict.put( this, groups ); }
	
	addGroup { |group|
		 groupDict.put( this, groupDict.at( this ).add( group ) ); 
		 this.class.changed( \groupDict, \add, this ); 
	}
	
	removeGroup { |group|
		var groups;
		groups = this.groups;
		groups.remove( group );
		if( groups.size == 0 ) {
			groupDict.put( this, nil ); 
		} {
			groupDict.put( this, groups );  // not needed?
		};
		this.class.changed( \groupDict, \remove, this); 
	}

	makeGroupAndSynth { |target, startPos = 0|
		var maxDurUnit;
	    var group;
	    var started = false;
	    if( this.shouldPlayOn( target ) != false ) {
	    		group = Group( target, addAction )
	                .startAction_({ |group|
	                    // only add if started (in case this is a bundle)
	                    this.changed( \go, group );
	                    started = true;
	                })
	                .freeAction_({ |group|
		                if( started == false ) { group.changed( \n_go ) };
	                    this.removeGroup( group );
	                    UGroup.end( this );
	                    this.changed( \end, group );
	                });
	        
	        units.do( _.makeSynth(group, startPos) );
	        this.addGroup( group );
	        this.changed( \start, group );
	    };
	}

	makeBundle { |targets, startPos = 0, withRelease = false|
		var bundles;
		this.setDoneAction;
	    bundles = targets.asCollection.collect{ |target|
	        target.asTarget.server.makeBundle( false, {
                this.makeGroupAndSynth(target, startPos);
                if( withRelease ) {
                    this.release
                }
            })
		};
		if( verbose ) {
		    ("Bundles for "++this).postln;
		    bundles.postln;
		};
		^bundles;
	}

	prStartBasic { |target, startPos = 0, latency, withRelease = false|
        var targets, bundles;
        startPos = startPos ? 0;
        target = preparedServers ? target ? ULib.servers ? Server.default;
        preparedServers = nil;
        targets = target.asCollection;
         if( verbose ) { "% starting on %".format( this, targets ).postln; };
        latency = latency ?? { Server.default.latency; };
        units.do( _.modPerform(\start, startPos, latency) );
        bundles = this.makeBundle( targets, startPos , withRelease );
        targets.do({ |target, i|
            if( bundles[i].size > 0 ) {
                target.asTarget.server.sendSyncedBundle( latency, nil, *bundles[i] );
            };
        });
        if( target.size == 0 ) {
            ^this.groups[0]
        } {
            ^this.groups;
        };
	}
	
	start { |target, startPos = 0, latency|
		this.prStartBasic(target, startPos, latency, false )
	}

	startAndRelease { |target, startPos = 0, latency|
        this.prStartBasic(target, startPos, latency, true )
	}
	
	stopPrepareTasks {
		if( prepareTasks.size > 0 ) { 
			prepareTasks.do(_.stop);
			prepareTasks = [];
		};
	}
	
	free { this.groups.do(_.free) }
	stop { this.stopPrepareTasks; this.free; }
	
	release { |time, keepFadeOutIfInf = true|
		var releaseUnits;
		releaseUnits = units.select({ |unit| unit.def.canFreeSynth });
		if( releaseUnits.size > 0 ) {
			if( keepFadeOutIfInf && { this.duration == inf } ) {
				time = nil;
			};
			releaseUnits.do( _.release( time, 14 ) );
		} {
			this.stop; // stop if no releaseable synths
		};
	}
	
	shouldPlayOn { |target|
		var res;
		res = units.collect({ |unit|
			unit.shouldPlayOn( target );
		}).select(_.notNil);
		case { res.size == 0 } {
			^nil;
		} { res.any(_ == true) } { // if any of the units specifically shouldPlayOn, all play
			^true;
		} {
			^false;
		};
	}

	apxCPU { |target|
		if( target.notNil ) {
			if( this.shouldPlayOn( target.asTarget ) ? true ) {
				^units.collect({ |u| u.apxCPU( target ) }).sum;
			} {
				^0
			};
		} {
			^units.collect(_.apxCPU).sum;
		};
	}

	prepare { |target, startPos = 0, action|
		var cpu;
		
		nowPreparingChain = this;
		
		// lastTarget = target;
		action = MultiActionFunc( action );
		target = target.asCollection;
		if( target.size == 0 ) {
			target = (ULib.servers ? Server.default).asCollection;
		};
		
		if( serverName.notNil ) {
			target = target.select({ |trg|
				if( trg.isKindOf( LoadBalancer ).not ) {
					serverName.asCollection.includes( trg.asTarget.server.name );
				} {
					true;
				};
			});
		};
		
		if( global ) {
			target = target.collect({ |trg|
				if( trg.isKindOf( LoadBalancer ) ) {
					trg.servers;
				} {
					trg;
				};
			}).flat;
		};
		
		target = target.select({ |tg|
			this.shouldPlayOn( tg ) != false;
		});
		//cpu = this.apxCPU;
		target = target.collect({ |tg|
			tg = UGroup.start( ugroup, tg, this );
			tg = tg.asTarget;
			tg.server.loadBalancerAddLoad(this.apxCPU(tg));
			tg;
		});
		preparedServers = target;
		units.do( _.prepare(target, startPos, action.getAction ) );
	     action.getAction.value; // fire action at least once
	     
	     if( verbose ) { "% preparing for %".format( this, preparedServers ).postln; };
	     
	     nowPreparingChain = nil
	     
	     ^target; // return array of actually prepared servers
	}

	prepareAndStart{ |target, startPos = 0|
		var task, cond;
		if( target.isNil ) {
			target = ULib.servers ? Server.default;
		};
		cond = Condition(false);
		task = fork { 
			target = this.prepare( target, startPos, { cond.test = true; cond.signal } );
			cond.wait;
	       	this.start(target, startPos);
			if( releaseSelf.not ) {
				(this.duration + Server.default.latency).wait;
				this.release;
			};
	       	prepareTasks.remove(task);
		};
	    prepareTasks = prepareTasks.add( task );
	}
	
	waitTime { ^this.units.collect(_.waitTime).sum }
	
	prepareWaitAndStart { |target, startPos = 0|
		var task;
		task = fork { 
			target = this.prepare( target, startPos );
			this.waitTime.wait; // doesn't care if prepare is done
	       	this.start(target, startPos);
	       	prepareTasks.remove(task);
		};
	    prepareTasks = prepareTasks.add( task );
	}

	dispose { 
		units.do( _.dispose );
		preparedServers.do({ |srv|
			srv.asTarget.server.loadBalancerAddLoad( this.apxCPU.neg );
		});
		preparedServers = [];
	}
	
	disposeSynths {
		units.do( _.disposeSynths );
		this.groups.do({ UGroup.end( this ); });
		this.groups.copy.do( _.changed( \n_end ) );
	}
	
	collectOSCBundleFuncs { |server, startOffset = 0, infdur = 60|
		var array;
		// returns a set of OSC bundles to be used by Score for NRT purposes
		
		if( disabled.not ) {	
			server = server ? Server.default;
			
			array = [ 
				[ startOffset, { 
					this.prepare(server);
					this.start(server); 
				}]
			];
			
			if( this.releaseSelf.not ) {
				if( this.duration != inf ) {
					array = array.add( [ startOffset + this.eventSustain, { this.release }] );
				} {
					array = array.add( [ infdur - this.fadeOut, { this.release }] );
				};
			};
			
			if( this.duration == inf )  {
				array = array.add( [ infdur, { this.disposeSynths }]);
			} {
				array = array.add( [ startOffset + this.duration, { this.disposeSynths }]
				);
			};
			
			^array;
		} {
			^[]
		}
		
	}
	
	collectOSCBundles { |server, startOffset = 0, infdur = 60|
		var array, prepareArray;
		// returns a set of OSC bundles to be used by Score for NRT purposes
		
		if( disabled.not ) {	
			server = server ? Server.default;
			
			this.useNRT({	
				
				prepareArray = server.makeBundle( false, { this.prepare(server); } );
				
				prepareArray.do({ |item|
					array = array ++ [ [ startOffset, item ] ];
				});
				
				array = array ++ [ 
					[ startOffset ] ++ server.makeBundle( false, { this.start(server); }) 
				];
				
				if( this.releaseSelf.not ) {
					if( this.duration != inf ) {
						array = array.add( 
							[ startOffset + this.eventSustain ] ++ 
								server.makeBundle( false, { this.release })
						);
					} {
						array = array.add( 
							[ infdur - this.fadeOut ] ++ 
								server.makeBundle( false, { this.release }) 
						);
					};
				};
				
				if( this.duration == inf )  {
					array = array.add( [ infdur ] ++ server.makeBundle( false, { this.disposeSynths }) );
				} {
					array = array.add( 
						[ startOffset + this.duration ] ++ 
							server.makeBundle( false, { this.disposeSynths })
					);
				};
				
			});
			
			^array.sort({ |a,b| a[0] <= b[0] });
		} {
			^[];
		};
	}
	
	resetGroups { this.groups = nil; } // after unexpected server quit
	
	// indexing / access
		
	at { |index| ^units[ index ] }
	copySeries { |first, second, last| ^units.copySeries( first, second, last ) }
	collect { |func|  ^units.collect( func );  }
	do { |func| units.do( func ); }
	last { ^units.last }
	first { ^units.first }
	indexOf { |obj| ^units.indexOf( obj ); }
	
	set { |key, value| // sets all units that respond to  key
		var extid, subkey;
		subkey = key;
		extid = key.asString.find(".");
		if( extid.notNil ) {
			subkey = key.asString[..extid-1].asSymbol;
		};
		units.select({ |u| u.keys.includes( subkey ) }).do(_.set( key, value ));
	}
	
	get { |key|
		^units.select({ |u| u.keys.includes( key ) }).collect(_.get(key));
	}
	
	setAt { |index, key, value|
		this.units.at(index).set(key, value)
	}

	getAt{ |index, key|
		^this.units.at(index).get(key)
	}

	add { |unit|
		units = units.add( unit.asUnit );
		this.changed( \units );
	}
	
	addAll { |inUnits| // a UChain or Array with units
		if( inUnits.class == this.class ) { inUnits = inUnits.units; };
		units = units.addAll( inUnits.collect(_.asUnit) );
		this.changed( \units );
	}
	
	put { |index, unit|
		units.put( index, unit.asUnit );
		this.changed( \units );
	}
	
	insert { |index, unit|
		units = units.insert( index, unit.asUnit );
		this.changed( \units );

	}

	insertCollection { |index,newUnits|
	    units = units[..(index-1)]++newUnits++units[(index+1)..];
	    this.changed( \units )
	}
	
	removeAt { |index|
		var out;
		out = units.removeAt( index );
		this.changed( \units );
		^out;
	}
	
	/*
	*   uchain: UChain
	*/
	<< { |uchain|
	    ^UChain(*(units++uchain.units))
	}

	/*
	*   unit: U or Array[U]
	*/
	<| { |unit|
	    ^UChain(*(units++unit.asCollection))
	}

    getAllUChains{ ^this }

	printOn { arg stream;
		stream << this.class.name << "( " <<* units.collect(_.defName)  << " )"
	}
	
	getInitArgs {
		var numPreArgs = -1;
		if( releaseSelf != true ) { 
			numPreArgs = 3
		} {
			if( duration != inf ) {
				numPreArgs = 2
			} {
				if( track != 0 ) {
					numPreArgs = 1
				} {
					if( startTime != 0 ) {
						numPreArgs = 0
					}
				}
			}
		};
		
		^([ startTime, track, duration, releaseSelf ][..numPreArgs]) ++ 
			units.collect({ |item| 
				item = item.storeArgs;
				if( item.size == 1 ) {
					item[0] 
				} {
					item
				};
			});
	}
	
	storeArgs { ^this.getInitArgs }
	
	storeModifiersOn { |stream|
		this.storeTags( stream );
		this.storeDisplayColor( stream );
		this.storeDisabledStateOn( stream );
		if( ugroup.notNil ) {
			stream << ".ugroup_(" <<< ugroup << ")";
		};
		if( serverName.notNil ) {
			stream << ".serverName_(" <<< serverName << ")";
		};
		if( addAction != \addToHead ) {
			stream << ".addAction_(" <<< addAction << ")";
		};
		if( global != false ) {
			stream << ".global_(" <<< global << ")";
		};
	}

}