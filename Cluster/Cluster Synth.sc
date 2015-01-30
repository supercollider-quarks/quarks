/*
    Cluster Library
    Copyright 2009-2012 Miguel Negrão.

    Cluster Library: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

   Cluster Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Cluster Library.  If not, see <http://www.gnu.org/licenses/>.

	library to use a synth or family of related synths playing simetrically in multiple servers/computers.
    usefull for multicomputer spatialization systems.
*/


//when returning values the classes give out a ClusterArg, which is then recognized by the other classes as a valid argument to expand. 

ClusterServer : ClusterBasic{
	*oclass{ ^ Server }

	addToSyncCenter{
		items.do(SyncCenter.add(_))
	}
	
	//need to implement this in ClusterBasic
	*default{
		^ClusterServer([Server.default])
	}		
}

ClusterGroup : ClusterBasic{ 
	*oclass{ ^Group }
	
	*new { arg target, addAction=\addToHead;	
		^this.doesNotUnderstand(\new,target, addAction)	}
	
	*basicNew { arg server, nodeID;
		^this.doesNotUnderstand(\basicNew,server, nodeID)	}	
	
	newMsg { arg clustertarget, addAction = \addToHead;	
		^this.doesNotUnderstand(\newMsg,clustertarget, addAction);
	}
	
	free { arg sendFlag=true;
		this.doesNotUnderstand(\free,sendFlag)
	}	
}

ClusterParGroup : ClusterGroup{ 
	*oclass{ ^ParGroup }
}

ClusterRootNode : ClusterBasic{	
	*oclass{ ^RootNode }

	*new{ |clusterServer|
		^this.doesNotUnderstand(\new,clusterServer)
	}
}

ClusterBus : ClusterBasic{	
	*oclass{ ^ Bus }
	
	//explicitly implemented because of default number of channels
	*control { arg clusterServer,numChannels=1;		
		^super.control(clusterServer,numChannels);
	}
		
	*audio { arg clusterServer,numChannels=1;		
		^super.audio(clusterServer,numChannels);
	}
	
	free{
		this.doesNotUnderstand(\free)
	}
}

ClusterBuffer : ClusterBasic{
	*oclass{ ^ Buffer }

	*new { arg server, numFrames, numChannels, bufnum;
		server = server ? ClusterServer([Server.default]);
		^this.doesNotUnderstand(\new,server, numFrames, numChannels, bufnum)
	}
	
	*alloc { arg server, numFrames, numChannels = 1, completionMessage, bufnum;
		server = server ? ClusterServer([Server.default]);
		^this.doesNotUnderstand(\alloc,server, numFrames, numChannels, completionMessage, bufnum)
	}
	
	*allocConsecutive { |numBufs = 1, server, numFrames, numChannels = 1, completionMessage,bufnum|
		server = server ? ClusterServer([Server.default]);
		^this.doesNotUnderstand(\allocConsecutive,numBufs, server, numFrames, numChannels, completionMessage,bufnum)
	}
	
	*read { arg server,path,startFrame = 0,numFrames, action, bufnum;
		server = server ? ClusterServer([Server.default]);
		^this.doesNotUnderstand(\read,server,path,startFrame ,numFrames, action, bufnum)
	}
	
	*readChannel { arg server,path,startFrame = 0,numFrames, channels, action, bufnum;
		server = server ? ClusterServer([Server.default]);
		^this.doesNotUnderstand(\readChannel,server,path,startFrame,numFrames, channels, action, bufnum)
	}
	
	read { arg argpath, fileStartFrame = 0, numFrames, bufStartFrame = 0, leaveOpen = false, action;
		this.doesNotUnderstand(\read,argpath, fileStartFrame, numFrames, bufStartFrame, leaveOpen, action)
	}			

	readChannel { arg argpath, fileStartFrame = 0, numFrames, bufStartFrame = 0, leaveOpen = false, channels, action;
		this.doesNotUnderstand(\readChannel,argpath, fileStartFrame, numFrames, bufStartFrame, leaveOpen, channels, action)
	}
	
	//implementation of methods that are common to Object and Buffer and thus doesNotUnderstand does not pick up
	//Buffer.methods.collect(_.name).asSet & Object.methods.collect(_.name).asSet
	free{
		this.doesNotUnderstand(\free)
	}
	
	numChannels{
		this.doesNotUnderstand(\numChannels)
	}
}


ClusterOSCBundle : ClusterBasic{
	*oclass{ ^ OSCBundle }
		
	*new{ |clusterServer|
		var items = clusterServer.items.collect{ OSCBundle.new };
		^super.newCopyArgs(items,clusterServer);
		
	}
	
	asClusterBundle{
		^this
	}
	
	dopost{
	
		items.do{ |bundle,i|
			("Bundle "++i++":");
			bundle.messages.dopost;
		}
	}	
}

ClusterSynth : ClusterBasic{
	*oclass{ ^ Synth }
	
	*new { arg defName, args, target, addAction=\addToHead;
		if('SyncCenter'.asClass.notNil){
			^this.doesNotUnderstand(\basicNew,defName, target.server).syncedPlay(target,args,addAction)
		}{
			^this.doesNotUnderstand(\new,defName, args, target, addAction)
		}			
	}
	
	//in case one needs to do an unsynced play and has SyncCenter installed.
	*unsyncedNew{ arg defName, args, target, addAction=\addToHead;
		^this.doesNotUnderstand(\new,defName, args, target, addAction);
	}
	
	syncedPlay{ |target,args,addAction|		
		var bundle;
		if(SyncCenter.ready){
				bundle = ClusterOSCBundle.new(target);
				bundle.add(this.newMsg(target,args,addAction));
				SyncCenter.sendPosClusterBundle(1,bundle,target.clusterServer)
		}	
	}
	
	//methods with defaults for args
	run { arg flag=true;
		this.doesNotUnderstand(\run,flag)
	}
	
	runMsg { arg flag=true;
		^this.doesNotUnderstand(\runMsg,flag)
	}
	
	newMsg { arg target, args, addAction = \addToHead;
		^this.doesNotUnderstand(\newMsg,target, args, addAction)
	}
	
	free { arg sendFlag=true;
		this.doesNotUnderstand(\free,sendFlag)
	}
	
	*grain { arg defName, args, target, addAction=\addToHead;
		target = target ? ClusterServer([Server.default]);
		this.doesNotUnderstand(\grain,defName, args, target, addAction)
	}
	
	//not implemented in original Synth
	registerNodeWatcher{
		items.do{ |synth| NodeWatcher.register(synth)}
	}	
	
	unregisterNodeWatcher{
		items.do(NodeWatcher.unregister(_))
	}
	
	*prDefName{ |defName,i|
		if(defName.isString){
			^defName.asSymbol
		}{
			^if(defName.size==0){defName}{defName[i] }
		
		}
	
	}
	
	*fromArray{ |synths|
		^super.newCopyArgs(synths,
			ClusterGroup.fromGroups(synths.collect(_.group)),
			ClusterServer(synths.collect(_.server))
		)
	}
}

ClusterSynthDef : ClusterBasic{
	*oclass{ ^ SynthDef }
		
	*new { arg name, ugenGraphFuncs, rates, prependArgs, variants, clusterdata;

		^this.doesNotUnderstand(\new,name, ugenGraphFuncs, rates, prependArgs, variants, clusterdata)
	}	
}


ClusterMonitor : ClusterBasic{
	*oclass{ ^ Monitor }
	
	*new{ |clusterServer|
		^super.newCopyArgs(clusterServer.items.collect{ Monitor.new});
	}
	
	playNToBundle{ |bundle, argOuts, argAmps, argIns, argVol, argFadeTime, inGroup, addAction, defName="system_link_audio_1"|
				
		argOuts = argOuts ? items.collect{ |monitor| (0..monitor.ins.size-1) }.asClusterArg;
		argAmps = argAmps ? items.collect{ |monitor| monitor.amps }.asClusterArg;
		argIns = argIns ? items.collect{ |monitor| monitor.ins }.asClusterArg;
		argFadeTime =  argFadeTime ? items.collect{ |monitor| monitor.fadeTime }.asClusterArg;
		
		^this.doesNotUnderstand(\playNToBundle,bundle, argOuts, argAmps, argIns, argVol, argFadeTime, inGroup, addAction, defName)		
	}
	
	playToBundle{ |bundle, fromIndex, fromNumChannels=2, toIndex, toNumChannels, inGroup, multi = false, volume, inFadeTime, addAction|
		this.doesNotUnderstand(\playToBundle,bundle, fromIndex, fromNumChannels, toIndex, toNumChannels, inGroup, multi, volume, inFadeTime, addAction)
	}
	
	play { arg fromIndex, fromNumChannels=2, toIndex, toNumChannels,target, multi=false, volume, fadeTime=0.02, addAction;
		^this.doesNotUnderstand(\play,fromIndex, fromNumChannels, toIndex, toNumChannels,target, multi, volume, fadeTime, addAction)
	}
	
	newGroupToBundle { arg bundle, target, addAction=(\addToTail);
		this.doesNotUnderstand(\newGroupToBundle,bundle, target, addAction)
	}
}




