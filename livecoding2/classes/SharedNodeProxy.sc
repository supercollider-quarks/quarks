
SharedNodeProxy : NodeProxy { // todo: should pass in a bus index/numChannels.
	var <constantGroupID;
	
	*new { arg broadcastServer, groupID; // keep fixed group id (< 999)
		^super.newCopyArgs(broadcastServer).initGroupID(groupID).init	}
	
	shared { ^true }
	
	initGroupID { arg groupID;  
		constantGroupID = groupID;
		awake = true;
	}
	
	reallocBus {} // for now: just don't. server shouldn't be rebooted.
		
	homeServer { ^server.homeServer }
	
	generateUniqueName {
		^asString(constantGroupID)
	}
	
	shouldAddObject { arg obj, index;
			^if(index.notNil and: { index > 0 }) {
				"only one object per proxy in shared node proxy possible".inform;
				^false
			} {
				if(obj.distributable.not) { 
					"this type of input is not distributable in a shared node proxy".inform;
					false
				} {
					obj.readyForPlay 
				}
			}
	}

	// map to a control proxy
	map { arg key, proxy ... args;
		args = [key,proxy]++args;
		// check if any not shared proxy is passed in
		(args.size div: 2).do { arg i; 
			if(args[2*i+1].shared.not, { Error("shouldn't map a local to a shared proxy").throw }) 
		};
		nodeMap.map(*args);
		if(this.isPlaying) { nodeMap.sendToNode(group) }
	}
	
	mapEnvir {}
	
	// use shared node proxy only with functions that can release the synth.
	// this is checked and throws an error in addObj
	stopAllToBundle { arg bundle;
			bundle.add([ 15, constantGroupID, "gate", 0, "fadeTime", this.fadeTime ])
	}
	
	removeToBundle { arg bundle, index;
		this.removeAllToBundle(bundle);
	}
	
	removeAllToBundle { arg bundle;
		var dt, playing;
		dt = this.fadeTime;
		playing = this.isPlaying;
		if(playing) { this.stopAllToBundle(bundle) };
		objects.do { arg obj; obj.freeToBundle(bundle, dt) };
		objects.makeEmpty;
	}
	
	clear { this.free; }
	
	reallyClear { super.clear; }
	
	group_ {}
	bus_ {}
	
	

	///////////////////
	
	prepareToBundle { arg argGroup, bundle; // ignore ingroup
		if(this.isPlaying.not) {
				group = Group.basicNew(
					this.homeServer,   // NodeWatcher should know when local group stopped
					this.constantGroupID // but not care about any remote groups
				);
				group.isPlaying = true;
				NodeWatcher.register(group);
		};
		bundle.add([21, constantGroupID, 0, 1]); // duplicate sending is no problem
	}

}


