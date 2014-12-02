(

Task {	 
	var myname = 'abc'; 
	"\n\n *** Republic - startup file for %.\n".postf(myname); 
	
	r = Republic.default ?? { Republic.new.makeDefault };
	z = RepublicGui(r, 8); 	// 8 is number of participants
	z.switchSize(1);	// hide chat area
	try { z.moveTo(5, 600) }; // add a display if you like.
	r.shareHistory; 

	AllGui.new.moveTo(310, 0);		// global overview
	PdefAllGui(12);
	TdefAllGui(12).moveTo(540, 600);

		// customize post window
	Document.listener
		.bounds_(Rect(500, 2, 600, 270))
		.background_(Color.blue(0.2))
		.stringColor_(Color.yellow);
	Document.postColor_(Color.yellow);
	
	Shout.rect = Rect(10, 10, 1000, 80);	// use Shout positioning
	
		// use Republic exclusively!
	Server.killAll;	
	Server.all.do { |sv| try { sv.window.close } };

			// server setup - reserve busses for shared audio and control signals
	r.options
		.numInputBusChannels_(8)
		.numOutputBusChannels_(8)
		.numReservedControlBuses_(32) 	// for shared control busses
		.numReservedAudioBuses_(16); 		// for combined playing
	
	3.5.wait;		// wait 3 rounds of registrations by all others;
				// this avoids duplicate clientIDs on the servers.
	
			// joining creates the server as well
	r.join(myname);

		// wait until server is there
	2.0.wait; 
			
	fork ({ 3.wait; 
		try { 
		Server.default = s = r.s;
			s.makeWindow.window.bounds_(Rect(315, 280, 288, 98)); 
		};
	}, AppClock);

	
		// share a synthdef for testing
	SynthDef(\xxy, {|out, sustain = 1, freq = 440|
		var env = Line.kr(0.1, 0, sustain, doneAction: 2);
		var son = SinOsc.ar(freq * [1, 1.2, 1.5, 1.7]).sum;
		Out.ar(out, son * env);
	}).share;

		// shared synthdefs would typically be in one big file, and loaded with 
// 	"~/whatever/folder/allMyRepublicSynthDefs.scd".load; 
	
	// put your own starting document here if you like:
	
//		// and probably open a file with some starting events, patterns, etc:
//	Document.open(
//		Republic.filenameSymbol.asString.dirname.dirname 
//		+/+ "republic_basicpatterns.scd"
//	);
			
	"\n*** Republic - startup file for % loaded fine.\n\n\n".postf(myname); 
	
}.play(AppClock);
);
