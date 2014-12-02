
/*
Syncing SuperCollider to Ableton Live.
The atk.scsync max4live device has to be installed correctly, no way to do it without max4live.
Network setup is handled automatically by OpenObject.
Basically we're just sending incremental timing messages from the Live transport, it's just a bit of glue between OpenObject and AudioMulchClock
*/

// You need these Quarks
// OpenObject for easy OSC communication
Quarks.install("OpenObject");

// This will provide us with the network clock over OSC
Quarks.install("AudioMulchClock");

// To initiate
c = AudioMulchClock.new; // this is your clock
c.shift = -3; //latency compensation, adjust;
OpenObject.start;

Pbind.play(c);