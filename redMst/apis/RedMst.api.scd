(
	play: {|reply, startSection|
		RedMst.play(startSection);
		reply.value;
	},
	goto: {|reply, gotoSection|
		RedMst.goto(gotoSection);
		reply.value;
	},
	next: {|reply|
		RedMst.next;
		reply.value;
	},
	prev: {|reply|
		RedMst.prev;
		reply.value;
	},
	isJumping: {|reply|
		reply.value(RedMst.isJumping);
	},
	stop: {|reply|
		RedMst.stop;
		reply.value;
	},
	clear: {|reply|
		RedMst.clear;
		reply.value;
	},
	//tracks
	//clock
	//clock_
	quant: {|reply|
		reply.value(RedMst.quant);
	},
	quant_: {|reply, val|
		RedMst.quant= val;
		reply.value;
	},
	section: {|reply|
		reply.value(RedMst.section);
	},
	maxSection: {|reply|
		reply.value(RedMst.maxSection);
	},
	jumpSection: {|reply|
		reply.value(RedMst.jumpSection);
	},
	stopAheadTime: {|reply|
		reply.value(RedMst.stopAheadTime);
	},
	stopAheadTime_: {|reply, val|
		RedMst.stopAheadTime= val;
		reply.value;
	},
	skipEmpty: {|reply|
		reply.value(RedMst.skipEmpty);
	},
	skipEmpty_: {|reply, val|
		RedMst.skipEmpty= val;
		reply.value;
	},
	//action
	//action_
	isPlaying: {|reply|
		reply.value(RedMst.isPlaying);
	},
	//at
	//add
	//remove
	makeWindow: {|reply, size, skin|
		RedMst.makeWindow(size, skin);
		reply.value;
	}
)
