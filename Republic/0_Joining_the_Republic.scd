
 
	     /////////////////////////////////////////////////////////
	    /////////////////// Joining the Republic  ///////////////
	   /////////////////////////////////////////////////////////
	
	    
	    // The class Republic allows a very simple organisation of
	   // cooperation in changing groups over a local network.
	  // It automatically administers each member's address and server.
	 // More details: see SimpleRepublic and Republic helpfiles



	 //________________________Step 1 - INSTALLING ___________________________
	// is it installed already?
	
	
if(\Republic.asClass.notNil) { "Republic installed already!" } { "please install first!" }; 	
		// if you have svn installed, and Quarks are working, do this:
"Republic".include; // ... and recompile if needed.
	
		// if not, get the Republic folder from a colleague 
		// and drop it into the folder for  SuperCollider userExtensions:
unixCmd("open" + quote(Platform.userExtensionDir))


		// other Quarks recommended to install:
(			// switching between different setups that are loeded on startup
"Preference (switch startup)".include; 
			// an overview of many global variables
"AllGui".include;
			// effects on the main outs, including filtering for bad numbers
"SpeakersCorner".include;
)

		// now, recompile - from Lang Menu, or with this: 
thisProcess.recompile;
		
		// test that all went well:
if(\Republic.asClass.notNil) { "Republic installed!" } { "Please install Republic first!" }; 


	
	//________________________Step 2 - AGREE ON A COMMON NETWORK________________
	// two options: 
	// 2.1 Take an existing network; or 
	// 2.2 One person creates a local network, and all the others join it.
	// 2.3 MAKE SURE: are you in the same network?
	
	
	
	//________________________Step 3 - CREATE A REPUBLIC _______________________
(	
r = Republic.new.makeDefault;
r.gui(12); // add a display if you like.
)	

	// Optional global variables gui: 
	AllGui();
		
	//________________________Step 4 - JOIN THE REPUBLIC _______________________
	// let's give every person 
	//		a unique NICKNAME (a symbol),
	//     	and optionally an ID - a unique integer between (0..31).
	// then join the Republic:
	
		// put YOUR unique name and id here!
		// you can also leave out id and you will be assigned a random one
	
r.join(<\yourName>, <id>);  // <-- your unique name (and id if you like) here

r.join(\abc);		// <-- this is for a member called abc
		

		// talk to the others by means of Shout:

//!! Hello republic! 
//!! "//!!" is the prefix for shouting.

	
	//________________________Step 5 - TEST AND KEEP THE SETUP _______________________
	
		
		// now the different participants should appear by themselves
		// you see the members appear on the post window.
	
		r.addrs; // see whose addresses are collected already
		r.servers; // see whose servers are collected already
		
	
	// NOW save your personal copy of this file, 
	// e.g. under the name Republic_install_<yourName>.scd.
	
	// OR you can adapt the example startup script:
	
"startup_republic_abc".openHelpFile;





    /////////////////////////////////////////////////////////
   //////////////// Welcome to the Republic  ///////////////
  /////////////////////////////////////////////////////////


// to share your code, evaluate the following:
// (use tab to chat from chat window)

r.shareHistory;

// what to do now?

// e.g. share a SynthDef with all participants
(
SynthDef(\xxy, {|out, sustain = 1, freq = 440|
	var env = Line.kr(0.1, 0, sustain, doneAction: 2);
	var son = SinOsc.ar(freq * [1, 1.2, 1.5, 1.7]).sum;
	Out.ar(out, son * env);
}).share;
)

	// play an event with that synthdef on your computer: 
(server: r.myServer, instrument: \xxy, freq: exprand(300, 1000)).play;

	// send a synth to everyone:
(server: r.s, where: \all, instrument: \xxy, freq: exprand(300, 1000)).play;

// or to a random member:
(server: r.servers.choose, instrument: \xxy, freq: exprand(300, 1000)).play;

// or to a specific member (replace \XXX with someone's nickname)
(server: r.s, instrument: \xxy, where: \XXX, freq: exprand(300, 1000)).play;

	// or to a random subset of members
r.nameList.scramble.keep(2).postln;
(server: r.s, where: r.nameList.scramble.keep(2).postln, instrument: \xxy, freq: exprand(300, 1000)).play;


	// play a pattern that goes around all members
(
Pdef(\x,
	Pbind(
		\instrument, \xxy,
		\server, r.s,
		\where, Prand(r.nameList, 8), // 
		\dur, 1,
		\legato, 0.3, 
		\freq, Pwhite().linexp(0, 1, 500, 2000)
	).trace
).play;
)

// For documentation of the session in the end:
History.document;


// For quitting republic:
(
g.remove;
r.leave;
);

// when you just quit SC or recompile, you will leave automatically.


// you can save the republic setup as a startup file, 
// which contains something like this file: 

"startup_republic_abc".openHelpFile;

// --- 

//////////////////////////////////////////////////////////////////////////////////////

// A Blackboard is a public writing area - optional
// using text document:
Blackboard.new.start;

// not using document
// esp. linux and windows

Blackboard.new.useDocument_(false).start;

