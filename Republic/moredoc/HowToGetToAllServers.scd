/* There are many ways to get to the different servers in a Republic. */


	//	One can directly choose one of the servers, 
r.servers.choose; 
r.servers[\abc]; // by name
	
	// use that in a Synth:
Synth(\default, nil, r.servers[\otto]);

	// or in an event:
(server: r.servers.choose.postln).play



	// to stop all that is playing on the current server: 
r.myServer.freeAll(true); // or do
s.freeAll(true);



	// In events and patterns, one can also use the 'where' argument, 
	// with specific names, or indices.

	// the republic server names are here:
r.nameList;

	
(			// get to specific servers in patterns, by name: 
Pbind(
	\server, r.s, 
	\where, \annie, 
	\instrument, \zzz, 
	\note, Prand([0, 2, 3, 5], 8), 
	\dur, 0.25
).play;
)

		// if the Republic's server, r.s, is the default, 
		// one can leave out the server argument:

Server.default = s = r.s; 
(			// get to specific servers in patterns, by name: 
Pbind(
	// \server, r.s, // <- not needed
	\where, \annie, 
	\instrument, \zzz, 
	\note, Prand([0, 2, 3, 5], 8), 
	\dur, 0.25
).play;
)

(			// choose one, play entire pattern on it
Pbind(
	\where, r.nameList.choose, 
	\instrument, \zzz, 
	\note, Prand([0, 2, 3, 5], 8), 
\dur, 0.25).play;
)

(		// make a subset of the server names, choose from 3:
Pdef(\test, 
	Pbind(
		\where, Prand(r.nameList.scramble.keep(3), inf), 
	\instrument, \zzz, 
	\note, Prand([0, 2, 3, 5], 20), 
	\dur, 0.25)
).play;
)

		// one can also use numbers as indices 
		// relative to one's own name in the namelist.

Pbind(\where, 0, \instrument, \xxy).play; 	// on my machine
Pbind(\where, 1, \instrument, \xxy).play; 	// my next neighbor in r.nameList

		// cycle thru all of them
Pbind(\where, Pseries().trace, \instrument, \xxy).play; 	

		// pick one randomly
Pbind(\where, r.nameList.size.rand, \instrument, \xxy).play; 

