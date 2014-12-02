
// example for accessing the different systems (processing, pd, max)
// see attached files. SuperCollider listens on port 57120, so messages must be sent to this port.

(

s.boot;
OpenObject.start;

Ndef(\y, { |freq=440, numharm = 200|
Blip.ar(freq * [1, 1.2], numharm) * 0.3
});

Ndef(\y).publish(\brussels); // accessible via \brussels

)

/*
OSC messages may look like:

"/oo", "brussels", "set", "freq", 300, "numharm", 34
"/oo_k", "brussels", "play", "fadeTime", 4, "out", 1, "vol", 0.5


*/



// for using the live coding examples, open the interpreter (only in safe networks)

OpenObject.openInterpreter;

/*
OSC messages may look like:

"/oo_p", "brussels", "{ |freq| SinOsc.ar(freq) }"
"/oo_i", "hello earth.postln"

*/
