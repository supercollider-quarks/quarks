
// using text document:
(
var addr = Republic.default.broadcastAddr;
// var addr = NetAddr("127.0.0.1", 57120);
Blackboard(addr).start;
);


// not using document
// esp. linux and windows

(
var addr = Republic.default.broadcastAddr;
// var addr = NetAddr("127.0.0.1", 57120);
Blackboard(addr).useDocument_(false).start;
);



// tests without Republic
(
var addr = NetAddr("127.0.0.1", 57120);
Blackboard(addr).useDocument_(false).start;
)