SensorSonificator helpfile

SensorSonificator allows you to sonify realtime sensor data that is put on a control bus. These are tools to understand the nature of the data.

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
a = GeneralHID.open( d[3] ); // this example using an Impact Gamepad

// create buses for the absolute axes
a.slots[3].do{ |it| it.createBus( s ); };

// create a sonificator for the first bus:
x = SensorSonificator.new( a.slots[3][0].bus );

// right now there are three different kinds of sonificators
// \value : sonifies the current value as frequency, amplitude is controlled by the rate of change in value
// \inrange : makes sound when the value is within a certain range
// \intrig : makes a sound whenever new data is coming in. Pitch is higher when the time since the last trigger was longer ago

// add value sonification:
x.addSonification( \value );

// start the sonification
x.start;

// set parameters for it
x.synths[0].set( \amp, 0.01 ); // base amplitude
x.synths[0].set( \slamp, 1 );  // slope amplitude

// post a list of available parameter names:
x.synths[0].controlNames;

// add inrange sonification:
x.addSonification( \inrange );

x.start;

// set parameters:
x.synths[1].set( \amp, 0.2 );
x.synths[1].set( \freq, 600 );
x.synths[1].set( \lo, 0.8 );

x.stop;
// arguments are kept when the sonification is stopped and started
x.start;

// add another inrange sonificator
x.addSonification( \inrange );
x.synths[2].set( \amp, 0.2 );
x.synths[2].set( \freq, 700 );
x.synths[2].set( \lo, 0 );
x.synths[2].set( \hi, 0.2 );
x.start;

// add an intrig sonificator:
x.addSonification( \intrig );

x.start;
x.synths[3].set( \amp, 0.5 );
x.synths[3].set( \dur, 0.05 );
x.synths[3].set( \freq, 300 );
x.synths[3].set( \fmmod, 100 );

x.synths[3].controlNames;

// query the group of sonificators (including argument values)
x.query

// with argument values:
x.query( false );

// stop the sonification:
x.stop;
