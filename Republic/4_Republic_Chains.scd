/* 	Markov Chains - send events along chains 

	one node starts sending a message with some values to a next node. 
	the next node plays a sound based on the values, and  
	passes the data on to a next node, until
	the countdown has reached its end.
*/

(
q = q ?? (); 
q.chain = ();

		// the waittime berfore sending on
q.chain[\dt] =  { 0.2 };

		// the function that decides who gets sent to next
q.chain[\whonext] = { r.nameList.choose };

		// what to do when a message comes in: 
		// play a short sound with an incoming freq
q.chain[\func] = { |countDown, args|
	var freq = args[0] ? 5000;
	freq = freq * rrand(0.98, 1.02);
	(instrument: \xxy, freq: freq * [1, 1.2, 1.5], sustain: 0.03).play;
};

		// this listens to incoming network messages with the name \chain:
q.resp = OSCresponder(nil, '/chain', { |t, resp, msg|
	var countDown = min(msg[1], 50);	// limit maximum chain length
	var howMany = msg[2]; 				// avalanching factor
	var nextAddr; 						// where to send to
	"countdown: %\n".postf(countDown);	
	
	if(countDown > 0) { 
		q.chain[\func].value(countDown, msg[3..]); 
	
		fork { 
			q.chain[\dt].value.wait;
			
			min(howMany, 2).do { |i| 
				nextAddr = r.addrs[q.chain[\whonext].value];
				nextAddr.sendMsg('/chain', countDown - 1, howMany, *(msg[3..] ++ i))
			}
		};
	};		
}).add;
);

	// test the playing func - single event once
q.chain[\func].value(12, [500]);

		// five values counting down
r.addrs.choose.sendMsg('/chain', 5, 1, 500);

		// making avalanches: howMany can spread to several next nodes - dangerous.
r.addrs.choose.sendMsg('/chain', 5, 2, 500);


	// stop listening
q.resp.remove;
r.addrs.choose.sendMsg('/chain', 5, 1, 5000);

	// start listening again
q.resp.add;
r.addrs.choose.sendMsg('/chain', 5, 1, 4000);





	// change what the chainfunc does:
(
q.chain[\func] = { |countDown, args|
	var freq = args[0] ? 5000;
	var sndef = args[1] ? \xxy;
	freq = freq * rrand(0.98, 1.02);
	(instrument: sndef, freq: freq * [1, 1.2, 1.5], sustain: 0.01).play;
};
);

	// change where the chainfunc should go 
q.chain[\whonext] = { r.nameList.choose };

	// how fast to send to the next address:
q.chain[\dt] = { exprand(0.02, 0.2) * 5 };

// test:
q.chain[\func].value(12, [9000]);

	// start a new single chain:
r.addrs.choose.sendMsg('/chain', 8, 1, 2000, [\xxy, \zzz].choose.postln);

	// start a new avalanche:
r.addrs.choose.sendMsg('/chain', 8, 2, 2000, [\xxy, \zzz].choose.postln);



/// possible extensions: 

* transform values that are passed along

* send on as fast as possible 
	-> network latency / jitter sonification
		
	q.chain[\dt] = 0.01;
	r.addrs.choose.sendMsg('/chain', 200, 1, rrand(500, 2000), [\xxy, \zzz].choose.postln);

* modify chainfunc locally on every node 

* when avalanching, spread out time? 

* different sound functions 


	// A limitation: 
OSC messages without timetags get quantized to hardwareBufferSize, 
which is usually 44100 / 512 = 86.133 Hz. 
This destroys fine timing differences between the incoming events
on a single machine; on multiple machines, the different quantizations 
between them smooth that out. 


