 

    /////////////////// NETWORK TESTS  //////////////////////




    /////////////////////////////
   // testing if broadcast works
                
                

 //________________________Step 1_______________________________
// get broadcast ip. usually this works:

    unixCmd("ifconfig | grep broadcast | awk '{print $NF}'") 

// Or: "ifconfig" in Terminal and search the printout for "broadcast". It should end with 255.



 //________________________Step 2_______________________________

(
var broadcastIP = "192.168.178.255"; // <--- put it here!
var id = 10000.rand;
var ok = false;
if(NetAddr.langPort != 57120) { Error("restart supercollider. langport != 57120").throw };
NetAddr.broadcastFlag = true;

n = NetAddr(broadcastIP, 57120);
OSCresponder(nil, '/broadcastPing', { |t, r, msg| ok = (msg[1] == id) }).add;
n.sendMsg('/broadcastPing', id);
fork { 
	"wait for test running ".post;
	4.do { 0.5.wait; ".".post; };
	"".postln;
	if(ok) 
	{ "test succeeded. broadcast roundtrip works".postln } 
	{ "test failed. broadcast roundtrip does not work.".postln } 
	};
)



    ////////////////////////////
   /// direct transmission test
                 
                 

 //________________________Step 1_______________________________
// get my own ip. 
// the last one here should it be:
    
    unixCmd("ifconfig | grep inet");
    
   // or look it up in SystemPreferences.
 
 
//________________________Step 2_______________________________
(
var myIP = "192.168.178.57"; // <--- put it here!
var id = 10000.rand;
var ok = false;
if(NetAddr.langPort != 57120) { Error("restart supercollider. langport != 57120").throw };
NetAddr.broadcastFlag = true;

n = NetAddr(myIP, 57120);
OSCresponder(nil, '/ping', { |t, r, msg| ok = (msg[1] == id) }).add;
n.sendMsg('/ping', id);
fork { 
	"wait for test running ".post;
	4.do { 0.5.wait; ".".post; };
	"".postln;
	if(ok) 
	{ "test succeeded. ping roundtrip works".postln } 
	{ "test failed. ping roundtrip does not work.".postln } 
	};
n.disconnect;
)




       ////////////////////////////
      // test if someone else is able to send a message

(
thisProcess.recvOSCfunc = { |time, replyAddr, msg| 
	if(#['status.reply', '/status.reply'].includes(msg[0]).not) {
		"At time %s received message % from %\n".postf( time, msg, replyAddr )
	}  // post all incoming traffic except the server status messages
}
);

// stop posting.
thisProcess.recvOSCfunc = nil;

