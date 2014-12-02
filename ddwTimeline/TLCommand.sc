// h. james harkins - jamshark70@dewdrop-world.net
// not ready for prime time!

// out of date: done notification needs to send resume time
// see proto cmds

TLAbstractCommand {
	var	<>isRunning = false, <>id, <>dur;
	var	<>shouldSync = true;
	*new { |parms|
		^super.new.init(parms)
	}
	
	init { |parms|
		^this.subclassResponsibility(\init);
	}
	
	play { |parms|
		if(isRunning.not) {
			isRunning = true;
			this.setDoneSignal(parms);
			this.fire(parms);
			this.changed(\play, parms); //.debug("TLCommand: sent play notification");
			this.schedDone(parms);
		}
	}
	
	schedDone { |parms|
		this.doneSignal.isNil.if({
			thisThread.clock.sched(this.dur.value(parms) ? 0, { this.stop });
		});
	}
	
	setDoneSignal { |parms|
	}
	
	stop { |parms|
		if(isRunning) {
			this.done(parms);
			this.clearDoneSignal(parms);
			isRunning = false;
//debug("TLCommand: sending done notification");
//this.dependants.debug("my dependants");
			this.changed(\done, parms);
		};
	}
	
	clearDoneSignal { |parms|
	}
	
	free {}
	
	copy {
		var	new = this.copy;
		new.isRunning = false;
		^new.reset
	}
	
	isTLCommand { ^true }
	
	*loadCmds {
		(PathName(this.filenameSymbol.asString).pathOnly ++ "proto-cmds.scd").loadPath;
	}
}

// totally customizable command: uses an environment to store parameters:
// ~fire = function to execute
// ~setDoneSignal = create the signal receiver to move on
// OR:
// ~dur = time to wait until next command
// ~clearDoneSignal = remove the signal receiver
// ~done = action to run when complete
// ~free = destructor (called manually from outside)

TLEnvirCmd : TLAbstractCommand {
	var	<env, originalEnv;
	init { |parms|
		originalEnv = parms;
		env = parms.copy;
	}
	
	fire { |parms|
		env.fire(this, parms);
	}
	setDoneSignal { |parms| env.setDoneSignal(this, parms) }
	done { |parms|
		env.done(env, parms);
	}
	clearDoneSignal { |parms| env.clearDoneSignal(this, parms) }
	free { env[\free].value(env) }

	doneSignal { ^env[\doneSignal] }
	dur { ^env[\dur] }
	id { ^env[\id] }
	
	reset { env = originalEnv }
}

