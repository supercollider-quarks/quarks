// synthTracker methods
// wrapper methods for other classes 
// part of wslib 2005
// W. Snoei 2005

+ Synth {

	track { arg canRelease, isRunning = true; 
		SynthTracker.register( if( isRunning ) { this } { this.defName }, canRelease);
		^this;
		}
		
	*track { arg defName, args, target, addAction=\addToHead, canRelease, maxCount = inf;
		// wrapper for SynthTracker with exact same args as Meta_Synth-new
		if( target.notNil ) { SynthTracker.server = target };
		^SynthTracker(defName, args, canRelease, maxCount, addAction);
		}
	
	findInTracker {
		^SynthTracker.findSynth(this);
		}
	
	isInTracker { ^this.findInTracker.notNil }
		
	trackFree { 
		// find the synth in the SynthTracker dictionary and free it if it's there.
		// If it's not there free it anyway
		var index;
		index = this.findInTracker;
		if(index.notNil)
			{ ^SynthTracker.freeAt(this.defName.asSymbol, index); }
			{ "Synth was not in SynthTracker, but was freed anyway.".postln; 
			^this.free }
		}
	trackRelease { 
		var index;
		index = this.findInTracker;
		if(index.notNil)
			{ ^SynthTracker.releaseAt(this.defName.asSymbol, index); }
			{ "Synth was not in SynthTracker, but was released anyway.".postln;
			^this.release }
		}
}
				
+ SynthDef {

	willFreeSynth { // check if the synth will free itself (oneshot)
		 ^this.canFreeSynth and: { this.hasGateControl.not } 
		}
	
	track {	
		if (this.willFreeSynth.not) { SynthTracker.register( this.name, this.canReleaseSynth ); };
		^this;
		}
		 
	trackPlay { arg target, args, addAction=\addToHead;
		if(this.willFreeSynth)
			{ ^this.play(target, args.asArgsArray, addAction); }
			{ ^this.play(target, args.asArgsArray, addAction).track(this.canReleaseSynth); }
		}
	
	isInTracker { ^{ SynthTracker.isReleasable.at(this.name.asSymbol) }.try.notNil; }
		
	release { SynthTracker.release(this.name); ^this }
	releaseLast { SynthTracker.releaseLast(this.name); ^this }
	releaseAt { |i| SynthTracker.releaseAt(this.name, i); ^this }
	releaseAll { SynthTracker.releaseAll(this.name); ^this }
	
	free { SynthTracker.free(this.name); ^this }
	freeLast { SynthTracker.freeLast(this.name); ^this }
	freeAt { |i| SynthTracker.freeAt(this.name, i); ^this }
	freeAll { SynthTracker.freeAll(this.name); ^this }

	runningSynths { 
		if(this.isInTracker) { ^SynthTracker.runningSynths.at(this.name.asSymbol) } { ^nil }
		}
	
	}

+ SynthDesc {
	willFreeSynth { // check if the synth will free itself (oneshot)
		 ^def.canFreeSynth and: hasGate.not ;
		}
	
	canReleaseSynth { ^def.canFreeSynth and: hasGate }

	track {	 
		if (this.willFreeSynth.not) { SynthTracker.register( this.name, this.canReleaseSynth ); };
		^this;
		}
	}
		 
		