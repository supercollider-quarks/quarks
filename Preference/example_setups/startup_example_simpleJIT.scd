
"simple JIT startup example.".postln;

	// choose which server to use, 
	// e.g. for teaching, take internal, so scope works
Server.default = s = Server.internal;
{ try { Server.local.window.close } }.defer(0.5);

s.options
	.numAudioBusChannels_(1024)
	.zeroConf_(false)			// don't wait for zeroconf when booting server
	.memSize_(8192 * 32)		// memSize is in kB, so 8 * 32 = 256 MB for soundfiles, buffers

		// keep several combinations of audio devices + numChannels
	.device_("MicLineOut")
	.hardwareBufferSize_(256)
	.numInputBusChannels_(8)
	.numOutputBusChannels_(8)
	
		// comment out unused devices.
//	.device_("Fireface 400")
//	.hardwareBufferSize_(128)
//	.numInputBusChannels_(18)
//	.numOutputBusChannels_(18)
;

	
q = q ? (); 		// make a global dictionary for everything
p = ProxySpace.push(s); // make a proxyspace and push it.

s.quit; 	// in case the server is still running, quit it.

			// decide what should happen after server has booted: 
s.doWhenBooted({ 
	1.wait;
	"server is booted - do further setup here...".postln;
				// make some JIT guis: 
	try { q.pxmix.win.close }; q.pxmix = ProxyMixer(p, 12);
	try { q.tdg.win.close }; q.tdg = TdefAllGui(16).moveTo(210, 600);
	try { q.pdg.win.close }; q.pdg = PdefAllGui(16).moveTo(420, 600);

	1.wait; 
	
	Env.perc.test;	// and play a test sound if you like
	"setup done.".postln;
});

Task({ 
	1.wait;	// wait till quit has happened - make longer if needed.
	s.boot;	// boot again
}).play(AppClock);

