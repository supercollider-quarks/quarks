//run this first
Udef.loadAllFromDefaultDirectory.do(_.loadSynthDef(s));

// example
~path = "sounds/a11wlk01-44_1.aiff";

//buffer
//not looped
x = BufSndFile(~path);
//looped 
x = BufSndFile(~path, startFrame: 44100*0.2, rate:1.5, loop: true);

x.postcs; "";
//disk
x = DiskSndFile(~path, startFrame: 44100*0.1, endFrame:44100*2);
//looped (infinite duration)
x = DiskSndFile(~path, loop: true);

//play
(
y = x.makeUnit;
z = UChain(y,\output);
z.useSndFileDur; // use duration
z.prepareAndStart(s);
)

// or shorter:
(
z = x.makeUChain;
z.prepareAndStart(s);
)

z.release(0.5); // release of 0.5s (for looping variants)

// play while keeping buffers 
(
z = x.makeUChain;
z[0].disposeOnFree = false;
z.prepare(s);
)

z.start(s);  // as many times as you want

z.release; // stop all

z.dispose(s); // frees all active buffers

//this should be empty when nothing is playing or after dispose
z[0].get(\soundFile).buffers;


// this will create Chain, allocate buffer, play, and when finished, free the buffer.
z = x.play(s);

z.gui;

x.plot

//RichBuffer
(
Udef(\testBuffer,{ |buf|
	Out.ar(0,BufAllpassN.ar(buf, Decay.ar(Dust.ar(1,0.5), 0.2, WhiteNoise.ar), 0.2, 3) )
});
x = U(\testBuffer,[\buf,RichBuffer(44100*1)]);
x.prepareAndStart(s);
)

x.get( \buf ).buffers; // try again after stop

x.stop;
