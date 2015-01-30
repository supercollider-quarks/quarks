UMarker : UEvent {
	
	classvar <>defaultAction;
	classvar <>presetManager;
	
	var <name = "marker";
	var <>score; // set at playback from score
	var <action; 
	var <notes;
	var <autoPause = false;
	
	*initClass {
		
		Class.initClassTree( PresetManager );
		
		presetManager = PresetManager( this, [ \default, { UMarker() } ] )
			.getFunc_({ |obj| obj.deepCopy })
			.applyFunc_({ |object, preset|
			 	object.fromObject( preset );
		 	});
		 	
		 presetManager.put( \pause, UMarker( 0,0, "pause", autoPause: true ) );
		 presetManager.put( \post, UMarker( 0,0, "post", { |marker, score| 
	// post the name of the current marker
	"passed marker '%' at %\n".postf( 
		marker.name, 
		score.pos.asSMPTEString(1000) 
	); 
}) );
		 presetManager.put( \jump_2s, UMarker( 0,0, "jump_2s", { |marker, score|
	// jump 2 seconds ahead 
	score.jumpTo( score.pos + 2 );
}) );

		presetManager.put( \jump_to_prev, UMarker( 0,0, "jump_to_prev", { |marker, score| 
	// jump to the previous marker and play (basic looping)
	score.toPrevMarker;
}) );
	
		presetManager.put( \reset_eq_and_level, UMarker(0,0, "reset_eq_and_level", { |marker, score| 
	// set global level to 0dB:
	UGlobalGain.gain = 0;
	
	// set the eq:
	UGlobalEQ.setting = [ 
		[ 100, 1, 0 ], 
		[ 250, 1, 0 ], 
		[ 1000, 1, 0 ], 
		[ 3500, 1, 0 ], 
		[ 6000, 1, 0 ], 
		[ 0 ] 
	];
	
	// to get the current eq setting: 
	/*
	UGlobalEQ.setting.postln;
	*/
}) );

		defaultAction = { |marker, score| };
	}
	
	*new { |startTime = 0, track, name, action, notes, autoPause = false|
		^super.newCopyArgs
			.startTime_( startTime )
			.track_( track ? 0 )
			.name_( name ? "marker" )
			.action_( action ? defaultAction )
			.notes_( notes )
			.autoPause_( autoPause ? false );
	}
	
	fromObject { |obj|
		this.name = obj.name;
		this.action = obj.action; // only copy the action from presets (perhaps more later)
		this.autoPause = obj.autoPause;
	}
	
	*fromObject { |obj|
		^obj.value.deepCopy;
	}
	
	*fromPreset { |name| ^presetManager.apply( name ) }
	
	fromPreset { |name| ^presetManager.apply( name, this ); }
	
	start { |target, startPos = 0, latency| 
		if( startPos == 0 ) { 
			if( autoPause ) {
				if( this.score.startedAt.notNil && {
					startTime > (this.score.startedAt[0] + 0.125) 
				}) { 
					this.score.pause; 
				};
			};
			action.value( this, this.score ); 
			this.score = nil; 
		}
	}
	
	prepare { |target, startPos = 0, action| action.value( this ) }
	prepareAndStart{ |target, startPos = 0| this.start( target, startPos ); }
	waitTime { ^0 }
	prepareWaitAndStart { |target, startPos = 0| this.start( target, startPos ); }
	eventSustain{ ^0 }
	preparedServers {^[] }
	getAllUChains { ^[] }
	
	stop { this.score = nil; }
	release { this.score = nil;  }
	
	mute { }
	unmute { }
	
	dispose { this.score = nil; }
	
	releaseSelf { ^true }
	releaseSelf_ { "%:releaseSelf - can't use releaseSelf\n".postf( this.class ) }
	
	duration { ^0 }
	duration_{ }
     dur_ { }
     
     getTypeColor {
        ^this.displayColor ? Color.yellow.alpha_(0.75);
	}
    
    	name_ { |x| name = x; this.changed(\name, name) }
    	action_ { |x| action = x; this.changed(\action, action) }
    	notes_ { |x| notes = x; this.changed(\notes, notes) }
    	autoPause_ { |bool| autoPause = bool; this.changed(\autoPause, autoPause) }

	makeView{ |i,minWidth,maxWidth| ^UMarkerEventView(this,i,minWidth, maxWidth) }
	
	duplicate{
	    ^this.deepCopy;
	}
	
	storeArgs { ^[ startTime, track, name, if( action != defaultAction ) { action }, notes, autoPause ] }
}