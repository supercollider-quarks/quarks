// wslib 2006

Synth_ID : Synth {

    
    // alternative Meta_Synth-new where a nodeID can be specified
    
	*new { arg defName, args, target, addAction=\addToHead, nodeID;
		var synth, server, addNum, inTarget;
		inTarget = target.asTarget;
		server = inTarget.server;
		addNum = addActions[addAction];
		synth = Synth.basicNew(defName, server, nodeID);
		if((addNum < 2), { synth.group = inTarget; }, { synth.group = inTarget.group; });
		server.sendMsg(9, //"s_new"
			defName, synth.nodeID, addNum, inTarget.nodeID,
			*(args.asOSCArgArray)
		);
		^synth
		}
		
	*newPaused { arg defName, args, target, addAction=\addToHead, nodeID;
		var synth, server, addNum, inTarget;
		inTarget = target.asTarget;
		server = inTarget.server;
		addNum = addActions[addAction];
		synth = Synth.basicNew(defName, server, nodeID);
		if((addNum < 2), { synth.group = inTarget; }, { synth.group = inTarget.group; });
		server.sendBundle(nil, [9, defName, synth.nodeID, addNum, inTarget.nodeID] ++
			args.asOSCArgArray, [12, synth.nodeID, 0]); // "s_new" + "/n_run"
		^synth
	}
		
	*after { arg aNode, defName, args, nodeID;	
		^this.new(defName, args, aNode, \addAfter, nodeID);
	}
	*before {  arg aNode, defName, args, nodeID;
		^this.new(defName, args, aNode, \addBefore, nodeID); 
	}
	*head { arg aGroup, defName, args, nodeID; 
		^this.new(defName, args, aGroup, \addToHead, nodeID); 
	}
	*tail { arg aGroup, defName, args, nodeID; 
		^this.new(defName, args, aGroup, \addToTail, nodeID);  
	}
	*replace { arg nodeToReplace, defName, args, nodeID;
		^this.new(defName, args, nodeToReplace, \addReplace, nodeID)
	}
}