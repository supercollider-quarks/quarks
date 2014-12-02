SensorLog helpfile

SensorLog helps you log and playback logged incoming sensor data (or any other data that is written to a control bus).

// NOTE this class is still buggy. Don't rely on it to work properly

/// ------------
/// Example:
/// ------------

s = Server.local.boot;

GeneralHID.buildDeviceList;
// Get the list of devices:
d = GeneralHID.deviceList;
// Check which devices have been found:
GeneralHID.postDevices;
// Pick the 6th device and open it and create an instance of it:
a = GeneralHID.open( d[4] ); // this example using an Impact Gamepad

// create buses for the absolute axes
a.slots[3].do{ |it| it.createBus( s ); };

// create the logger
x = SensorLog.new( a.slots[3][0].bus ); // right joystick x-axis

// start the log:
x.startLog;
// stop the log:
x.stopLog;

a.slots[3][1].bus.scope( bufsize: 1024 );

a.slots[3].do{ |it| it.bus.postln; };

a.slots[3].do{ |it| it.freeBus; };

Bus.freeAl

b = Bus.new( \control, 0, 5, s );
j = b.scope( bufsize: 4096 );
j.buffer
j.ki
j.ai

s.dump

s.queryAllNodes( true );
s.scope( 4 )
s.scope.buffer
s.scope


// logfiles contains the logfiles:
x.logfiles;
// per default buffers of length 4096 are created; values are only written to it, when a value is written to the bus; one channel in the buffer contains the time between two values.

// the logbuffer:
x.logbuffer;

// plot to view the data:
x.logbuffer.plot;

// pack the resulting files together (this puts a tar-file in the workdirectory, containing all the buffer files):
x.pack( "test" );

// unpack can unpack the files:

x.unpack( "test" );
x.playback;
x.stop;

// to create a new instance from a pack:
y = SensorLog.newFromPack( "test" );

y.logfiles;

y.playback;

y.stop;

