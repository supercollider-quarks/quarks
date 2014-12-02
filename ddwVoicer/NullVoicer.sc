
// the sole purpose of NullVoicer is to trap method calls and do nothing
// NullVoicer is the model of a VoicerProxy when the proxy is pointing to nothing

NullVoicer {
	var <proxy;	// proxy_ is handled by Voicer

	*new {
		^super.new.init
	}
	
	proxy_ { arg pr;
		proxy = pr;
		proxy.notNil.if({ proxy.switchControlProxies });
	}

	draggedIntoVoicerGUI { arg dest;
		var oldProxy;
		oldProxy = proxy;		// must clear from old gui if there was one
		dest.model.voicer_(this);	// set new gui's proxy to this voicer
		oldProxy.notNil.if({ oldProxy.voicer_(nil) });	// clear old proxy
	}

	nodes { ^Array.new }
	voices { ^0 }
	target { ^nil }
	target_ {}
	addAction { ^\addToTail }
	addAction_ {}
	bus { ^nil }
	stealer { ^\preferEarly }
	stealer_ {}
	globalControls { ^IdentityDictionary.new }
	globalControlsByCreation { ^Array.new }
	latency { ^nil }
	isRunning { ^true }	// to display "run" in gui
	clock {}
	clock_ {}
	init {}
	makeNode {}
	nonplaying { ^[]}
	earliest {}
	latest {}
	firstNodeFreq {}
	strictCycle {}
	cycle {}
	random {}
	preferEarly {}
	preferLate {}
	trigger1 {}
	trigger {}
	gate1 {}
	gate {}
	release1 {}
	release {}
	releaseAll {}
	releaseNow1 {}
	releaseNow {}
	set {}
	mapGlobal {}
	unmapGlobal {}
	addProcess {}
	removeProcess {}
	removeProcessAt {}
	free {}
	active { ^false }
	panic {}
	cleanup {}
	guiClass { ^VoicerGUI }
	asString { ^"inactive" }

}