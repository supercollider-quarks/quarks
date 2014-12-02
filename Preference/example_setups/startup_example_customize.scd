"startup example - customising Menus.".postln;

	// choose which server to use, 
	// e.g. for teaching, take internal, so scope works
Server.default = s = Server.internal;
{ try { Server.local.window.close } }.defer(0.5);

s.options
	.numAudioBusChannels_(1024)	// lots of busses for JITLib
	.zeroConf_(false)			// don't wait for zeroconf when booting server
//	.device_()				// maybe set a specific device ? 
//	.numInputBusChannels_()		// with a specific number of ins
//	.numOutputBusChannels()		// ... and outs  - default is 8.
	.memSize_(8192 * 32)		// memSize is in kB, so 8 * 32 = 256 MB for soundfiles, buffers
	
	;		


	// JIT preparation
q = q ? (); 		// make a global dictionary for everything
p = ProxySpace.push(s); // make a proxyspace and push it.
q.numChans = 8; 	// number of channels of audio setup
	
			//text editor tweaks & Library Menu		
Platform.case(\osx, {
			// put the post window where you prefer 
	Document.listener.bounds_(Rect(Window.screenBounds.width - 400, 2, 400, 300));	Document.listener.background_(Color.blue(0.2, 0.9)); // and color it. // .alwaysOnTop_(true);
	Document.listener.stringColor_(Color(1, 0.62, 0));
	Document.postColor_(Document.listener.stringColor);

	
	SkipJack.verbose = false; 			// reduce info posting
	

			// put things in the Library menu: 
			// Custom Menu Items - OSX only - configure here
	CocoaMenuItem.add(["server meter"],{ Server.default.meter });
	CocoaMenuItem.add(["server window"],{ Server.default.window });
	CocoaMenuItem.add(["scope Ins"],{ q.scope = s.scope(q.numChans, index: s.options.numOutputBusChannels, zoom: 9) });
	CocoaMenuItem.add(["scope Outs"],{ q.scope = s.scope(q.numChans, index: 0, zoom: 9) });
	CocoaMenuItem.add(["FreqScope"],{ q.fscope = FreqScope.new });

			// an 8 chan test sound
	CocoaMenuItem.add(["TestSound8"],{ 
		p[\zztest] = { BPF.ar(Dust.ar(5 ! q.numChans, 50), { exprand(200, 2010) } ! q.numChans, 0.05) 
			* GrayNoise.ar(0.1, 1) };
		p[\zztest].playN(vol: 0.25);
	});
	
	CocoaMenuItem.add(["places", "recordings"],{ unixCmd("open" + quote(thisProcess.platform.recordingsDir)) });
	CocoaMenuItem.add(["places", "~/scwork"],{ unixCmd("open ~/scwork") });
	CocoaMenuItem.add(["places", "supportDirs"],{ 
		unixCmd("open " ++ Platform.userAppSupportDir.asCompileString);
		unixCmd("open " ++ Platform.systemAppSupportDir.asCompileString);
	});
	CocoaMenuItem.add(["places", "extensionDirs"],{ 
		unixCmd("open " ++ Platform.userExtensionDir.asCompileString);
		unixCmd("open " ++ Platform.systemExtensionDir.asCompileString); 
	});
		
	CocoaMenuItem.add(["quarks", "Quarks.find"], { Window("find in Quarks folder dialog ...").front });
	CocoaMenuItem.add(["quarks", "Quarks.listInstalled"], { 
		var str = "/**** All currently installed quarks: ****/\n"; 
		Quarks.installed.do { |quark|
			str = str + ("\t" ++ quark.name.asCompileString ++ ".include;\n");
		};
		Document("All currently installed quarks", str);
	});
	CocoaMenuItem.add(["quarks", "Quarks.gui"], { Quarks.gui; });
	CocoaMenuItem.add(["quarks", "Quarks.update"], { Quarks.checkoutAll; });
});

		// now really start the server, then load setup in doWhenBooted
Task { 
	0.5.wait; 
	s.quit; 		// sometimes the server must be quit first, 
	1.wait;		// and waiting between steps can be helpful.
				// tell me the new server configuration when booting:
	s.boot;	// now boot.
	s.sync; 	// wait for doWhenBooted before continuing 
	
	1.wait;
			// do whatever else belongs to setup: e.g. load some buffers, 
//	q.bufs = "~/scwork/XYZ/sounds/*".pathMatch.do { |path| Buffer.read(s, path) };
//	s.sync; 	// wit till buffers are there

			// load some code files, wait between loads
//	1.wait; 
//	"~/scwork/XYZ/patches/*.rtf".pathMatch.collect { |path| 
//		path.load; 0.5.wait; 
//	}; 
	
	// s.scope(q.numChans, zoom: 9);	
	
	"startup example with custom Menus done.".postln; 
	
}.play(AppClock);

