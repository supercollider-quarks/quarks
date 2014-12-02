
GetValue {
	var <ugenFunc, <server, <value;
	var synth, resp;
	var <>action;

	*new { |ugenFunc, server|
		^super.newCopyArgs(ugenFunc, server ? Server.default).run
	}
	
	run {
		var cmd = "/getValue_" ++ this.identityHash.abs;
		value = 0;
		if(server.serverRunning.not) { "server % not running".format(server.name).warn; ^this };
		server.sendBundle(nil, ['/notify', 0],['/notify', 1]);
		synth = { |updateRate = 5|
			var source = ugenFunc.value(this);
			var change = HPZ2.kr(source) > 0;
			SendReply.kr(
						Impulse.kr(updateRate) * change + Impulse.kr(0), 
						cmd, 
						source
					);
		}.play(server);
		synth.register;
		synth.addDependant(this);
		resp = OSCresponder(server.addr, cmd, { |t,r,msg| 
			value = msg[3..].unbubble;
			action.value(value);
		});
		resp.add;
		CmdPeriod.add(this);
	}
	
	rate_ { |rate|
		synth.set(\updateRate, rate)
	}
	
	cmdPeriod { this.prRemove; }
	
	stop {  this.prRemove; synth.free;  }
	
	update { |who, what|
		if(what == \n_end) { this.run };
	}
	
	prRemove {
		resp !? { resp.remove };
		synth.removeDependant(this); 
		CmdPeriod.remove(this);
	}
		
}


GetMouseX : GetValue {
	var <minval=0, <maxval=1, <warp=0, <lag=0.2;
	
	*new { |minval=0, maxval=1, warp=0, lag=0.2, server|
		^super.new({ this.mouseClass.kr(minval, maxval, warp, lag) }, server).run
	}
	
	*mouseClass {
		^MouseX
	}
	
}

GetMouseY : GetMouseX {
	*mouseClass {
		^MouseY
	}
}

