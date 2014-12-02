/**
	2006  Till Bovermann, Alberto de Campo 
	(IEM)
*/

HIDnode {
	var dict;
	
	*new {
		^super.new.initHIDnode;
	}
	initHIDnode {
		dict = IdentityDictionary.new;
	}
	add {|resp|
		dict.put(resp.cookie, resp);
	}
	remove {|resp|
		dict.removeAt(resp.cookie);
	}
	removeAll {
		dict = dict.class.new;
	}
	doAction {|cookie, val|
		var resp = dict[cookie];
		// early return
		resp.isNil.if{^false};	
		
		resp.value(val);
		^true;
	}
}



HIDresponder {
	classvar <locDict;
	classvar <>nodeClass;
	var <>locID; 	// was addr
	var <>cookie; // was cmdName, 
	var <>action;
	var <>spec;	// normalize input
	
	*new { arg locID, cookie, action, spec;
		^super.newCopyArgs(locID, cookie, action, spec);
	}
	
	*initClass {
		locDict = IdentityDictionary.new;
		nodeClass = HIDnode
	} 
	
	/**
	 * load default eventloop action into the eventloop of HIDDeviceService and 
	 * start the eventloop.
	 */
	*load {
		HIDDeviceService.action_({arg productID, vendorID, locID, cookie, val;
				HIDresponder.respond(locID, cookie, val);
		});
		HIDDeviceService.runEventLoop;
	}
	*releaseAll {
		this.locDict.keysValuesDo{|locID, node|
			(format("Meta_HIDresponder-releaseAll: dequeuing device %\n", locID)).inform;

			HIDDeviceService.dequeueDevice(locID);
			node.removeAll;
			this.locDict.removeAt(locID); // trash HIDnode
		};
		HIDDeviceService.stopEventLoop;
	}
	
	*add {|responder|
		var obj;
		var locID = responder.locID;

		obj = locDict[locID];
		obj.isNil.if({
			locDict[locID] = obj = nodeClass.new(locID);
			(format("Meta_HIDresponder-add: queuing device %\n", locID)).inform;
			HIDDeviceService.queueDevice(locID);
		});
		obj.add(responder);
	}
	add {
		this.class.add(this);
	}

	*remove {|responder|
		locDict[responder.locID].remove(responder)
	}
	remove {
		this.class.remove(this);
	}	
	
	
	*respond { arg locID, cookie, val;
		var node = locDict[locID];
		node.notNil.if({
			^node.doAction(cookie, val); // calls resp.value and returns true/false
		}, {
			^false
		});
	}
	
	value { arg val;
		action.value(val, spec, this);
	}
	== { arg that;
		^that respondsTo: #[\cookie, \locID]
			and: { cookie == that.cookie and: { locID == that.locID }}
	}
	hash {
		^locID.hash bitXor: cookie.hash
	}


	removeWhenDone {
		action = {this.remove} <> action;
	}
}


/*//used to manage HIDresponderNodes. do not use directly.

HIDMultiResponder : HIDresponder {
	var <>nodes;
	
	value { arg time, msg;
		var iterlist;
		iterlist = nodes.copy;
		iterlist.do({ arg node; node.action.value(time, node, msg) });
	}
	isEmpty { ^nodes.size == 0 }
	
}


OSCresponderNode {
	var <locID, <cmdName, <>action;
	*new { arg locID, cmdName, action;
		^super.newCopyArgs(locID, cmdName.asSymbol, action);
		
	}
	//i.zannos fix
	add {
		var made, found;
		made = OSCMultiResponder(locID, cmdName);
		found = OSCresponder.all.findMatch(made);
		if(found.isNil, { made.nodes = [this]; made.add; ^this });
		if (found.class === OSCresponder, {
			found.remove;
			made.nodes = [found, this];
			made.add;
		},{
			found.nodes = found.nodes.add(this)
		});
	}
	
	removeWhenDone {
		var func;
		func = action;
		action = { arg time, responder, msg, locID;
			func.value(time, responder, msg, locID);
			this.remove;
		}
	}
	
	remove { 
		var resp, alreadyThere;
		resp = OSCMultiResponder(locID, cmdName);
		alreadyThere = OSCresponder.all.findMatch(resp);
		if(alreadyThere.notNil) 
		{ 
			alreadyThere.nodes.remove(this);
			if(alreadyThere.isEmpty, { alreadyThere.remove });
		}; 
	}
	
	value { arg time, msg, locID;
		action.value(time, this, msg, locID);
	}

}

*/