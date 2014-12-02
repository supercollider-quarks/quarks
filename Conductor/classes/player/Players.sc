NodeProxyPlayer {
	var <>nodeProxy, <>args, <>bus, <>numChannels, <>group, <>multi;
	*new { | nodeProxy, args = #[], bus, numChannels, group, multi = false | 
		^super.newCopyArgs (nodeProxy, args, bus, numChannels, group, multi)
	}

	play { 
		nodeProxy.awake = true; 
		nodeProxy.play(bus, numChannels ? nodeProxy.numChannels, group ? nodeProxy.group, multi); 
		if (args.notNil) { nodeProxy.setControls(args.value) };
	}
	stop { 
		Task{ 
			nodeProxy.stop(nodeProxy.fadeTime); 
			nodeProxy.fadeTime.wait; 
//			nodeProxy.clear
		}.play;	
	}
	pause { nodeProxy.group.run(false)  }
	resume { nodeProxy.group.run(true)  }
	
}



// TaskWithStreamKill handles sleeping on a signal correctly and provides clock, quant option
TaskPlayer {
	var <>func, <>clock, <>quant, player;
	*new { |func, clock, quant|
		^super.newCopyArgs(func, clock? TempoClock.default, quant? 0)
	}
	
	play { player = Task(func).play(clock, false, quant) }
	stop { player.stop; player.originalStream.stop; }
	pause { player.pause }
	resume { player.resume }
}

PatternPlayer {
	var <>pattern, <>clock, <>event, <>quant, eventStreamPlayer;
	
	*new { |pattern, clock, event, quant| 
		^super.newCopyArgs(pattern, clock ? TempoClock.default, event ? Event.default, quant ? 0)
	}
	
	play {
		eventStreamPlayer = pattern.play(clock, event.value, quant)
	}
	
	pause { eventStreamPlayer.pause }
	resume { eventStreamPlayer.resume }
	stop { eventStreamPlayer.stop; eventStreamPlayer = nil }
}

ActionPlayer {
	var <>playFunc, <>stopFunc, <>pauseFunc, <>resumeFunc;
	
	*new { |play, stop, pause, resume| ^super.newCopyArgs(play, stop, pause, resume) }
	
	play { this.playFunc.value }
	stop { this.stopFunc.value }
	pause { this.pauseFunc.value }
	resume { this.resumeFunc.value }

}