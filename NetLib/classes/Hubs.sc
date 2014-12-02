
AbstractHub : Participation {
	var <>hub, <>locked=false;
	
	init {
		this.addResponder('/theHubIsMe', {|t,r,msg|
			var who = msg[1].asSymbol, addr;
			if(this.canBeChanged) {
				addr = collective.everybody[who];
				if(addr.notNil) { 
					hub = addr 
				} { 
					"something went wrong: hub % could not be assigned.".format(who).warn
				}
			} { 
				if(hub.name !== who) {
					"can't change hub to %, it is locked.".format(who).warn
				}
			};
			
		})
	}
	
	canBeChanged {
		^locked.not or: hub.isNil
	}
	
	theHubIsMe {
		collective.sendToAll('/theHubIsMe', collective.myName)
	}
	
	isTheHubMe {
		^hub == collective.myAddr
	}
	
	stop {
		super.stop;
	}
	
}




TheDiningPhilosophers : Participation {
	var <forks, <addresses;
	
	// to do..
	// how to distribute the forks in the first place?
	// maybe there needs to be a hub for this at least.
	
	init {
		this.addResponder('/pingThere', { arg r, t, msg; 
			var name = msg[1];
			name !? { collective.sendToName(name, '/pingBack', collective.myName) } 
		});
		this.addResponder('/pingBack', { arg r, t, msg;
			var name, addr;
			name = msg[1];
			addr = collective.everybody.at(name);
			if(addr.notNil and: { addresses.includes(addr).not }) {
				addresses = addresses.add(addr);
			};
		});
		addresses = [];
		
	}
	
	createGlobalOrder {
		collective.sendToAll('/pingThere', collective.myName);
	} 
	
	numPhilosophers { ^addresses.size }
	nextPhilosopher {
		addresses.indexOf(collective.myAddr) + 1 % this.numPhilosophers
	}
	prevPhilosopher {
		addresses.indexOf(collective.myAddr) - 1 % this.numPhilosophers
	}
}

/*
AbstractTransference : Participation {
	var <channel='/transference', <>nextFunc, <>action;
	init {
		this.addResponder(channel, { arg r, t, msg; 
			var follow = this.nextIndex;
			var delta, n, blendFactor, recvedData;
			
			#delta, n ... recvedData = msg[1..];
			
			recvedData = this.transform(recvedData);
			this.react(recvedData, delta, n);
			
			if(n > 0 and: { follow.notNil }) {
				SystemClock.sched(delta, {
					collective.sendToIndex(follow, channel, delta, n - 1, *recvedData)
				});
			};
			
			
		})
	}
	transform { arg data;
		^data
	}
	react { arg data; action.value(*data) }
	nextIndex { ^nextFunc.value ? 0 } // for now
	newChain { arg delta=0.1, length=4, data;
		var i = this.nextIndex;
		collective.sendToIndex(i, channel, delta, length - 1, *data)
	}
	
}
*/

// todo: generalize generation of next message

MarkovNet : Participation {
	var <>data, <>transitions, <>weights, <channel='/markov';
	var <>function;
	
	init {
		this.addResponder(channel, { arg r, t, msg; 
			var delta, n, blendFactor, recvedData, forwardData;
			var sendData, infoSize, inject;
			// length
			n = msg[1];
			infoSize = msg[2];
			// info
			delta = msg[3];
			blendFactor = msg[4];
			inject = msg[5] != 0;
			// data
			recvedData = msg[3 + infoSize ..];
			sendData = blend(data, recvedData, blendFactor.postln);
			forwardData = if(inject) { sendData } {recvedData };
			recvedData.postln;
			if(n > 0) {
				SystemClock.sched(delta, {
					this.newChain(delta, n - 1, blendFactor, inject, forwardData);				});
			};
			function.value(sendData);			
		})
	}
			
	nextIndex {
		var res;
		if(transitions.isNil) { ^collective.addresses.size.rand };
		^if(weights.isNil) { 
			transitions.size.rand 
		} { 
			res = transitions[weights.windex];
			if(res.isNil) {  Error("not the right number of transitions / weights").throw };
			res
		};
	}
	newChain { arg delta=0.1, length=4, blendFactor=0.5, inject=false, sendData;
		var info;
		info = [delta, blendFactor, inject.binaryValue];
		sendData = sendData ? data;
		collective.sendToIndex(this.nextIndex, 
			channel, length, info.size, *(info ++ sendData)
		)
	}
	
}
