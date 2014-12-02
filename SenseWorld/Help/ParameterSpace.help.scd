ParameterSpace is a 2D mapping space where you can assign a set of parameters to points in space. In between these points a weighted average of the parameter values is taken, based on an inverse distance (to the defined points) weighting algorithm.

(
// Define a space:
~space = ParameterSpace.new( Rect( 0, 0, 10, 10 ) );
// the rect is the space in which we can move

// add some points to the space:
~space.addPoint( Point.new( 1,1 ) );
~space.addPoint( Point.new( 9,9 ) );
~space.addPoint( Point.new( 1,9 ) );
~space.addPoint( Point.new( 9,1 ) );

// add parameters we want to control, the values indicated are default values, the value at inbetween points is calculated as the sum over the points of:
// default value + weight * (point value - default value)
// so far from all the points, the value will tend to the default value
~space.addParameter( \amp, 0.5 );
~space.addParameter( \dur, 0.5 );
~space.addParameter( \freq, 300 );
~space.addParameter( \freqmod, 50 );
);
(
// The update function determines what will be done with the current values, when they have been updated, so this is where you put e.g. what the synth you want to control will do:
~space.updateFunc = {
	x.set(
		\amp, ~space.currentPars.at( \amp ),
		\dur, ~space.currentPars.at( \dur ),
		\freq, ~space.currentPars.at( \freq ),
		\freqmod, ~space.currentPars.at( \freqmod )
	)
};
);
// current values are always found in the dictionary: currentPars.
(
// the moveFunc is the function to determine the movement in each time frame:
~space.moveFunc = { ~movevect;  };
~movevect = [0,0];

// then, for each point, define the values at that point for each parameter:
~space.parameterDict.put( ~space.points[0], \amp, 0.2 );
~space.parameterDict.put( ~space.points[0], \dur, 0.1 );
~space.parameterDict.put( ~space.points[0], \freq, 1000 );
~space.parameterDict.put( ~space.points[0], \freqmod, 200 );

~space.parameterDict.put( ~space.points[1], \amp, 0.5 );
~space.parameterDict.put( ~space.points[1], \dur, 2 );
~space.parameterDict.put( ~space.points[1], \freq, 300 );
~space.parameterDict.put( ~space.points[1], \freqmod, 50 );

~space.parameterDict.put( ~space.points[2], \amp, 0.3 );
~space.parameterDict.put( ~space.points[2], \dur, 0.5 );
~space.parameterDict.put( ~space.points[2], \freq, 500 );
~space.parameterDict.put( ~space.points[2], \freqmod, 50 );

~space.parameterDict.put( ~space.points[3], \amp, 0.2 );
~space.parameterDict.put( ~space.points[3], \dur, 0.2 );
~space.parameterDict.put( ~space.points[3], \freq, 500 );
~space.parameterDict.put( ~space.points[3], \freqmod, 300 );

);

s = Server.local.boot;
(
SynthDef( \ParameterSpaceHelp, { |out=0,amp=0.2,freq=100,freqmod=40|
	Out.ar(out,
		SinOsc.ar( SinOsc.ar( freqmod, 0, 20, freq ), 0, amp )
	);
}).send(s);
)

x = Synth.new( \ParameterSpaceHelp );

// start the update routine:
~space.start;

// show a gui:
~space.makeGui;
// you can move the slider to move through the space

// update time (in seconds):
~space.dt = 1;

~space.stop

// manual update:
~space.move( [0.5, 0.5 ] ); // move with vector
// or:
~space.set( [3,3].asPoint ); // set to a specific value

~space.set( [5,5].asPoint ); // set to a specific value
~space.set( [2,9].asPoint ); // set to a specific value
~space.set( [9,3].asPoint ); // set to a specific value
~space.set( [9,9].asPoint ); // set to a specific value


// Minimum distance (both for determining whether a move results in a new calculation of parameters, as well as determining whether we are exactly at a point or not:
~space.mindist = 0.02;

// WEIGHTS:
weights can be determined for each parameter:
~space.weightfactor_( 1 ); // set for all (sets all weightfactors which are equal to the default value)
~space.weightDict
// set the weightfactor for a specific parameter
~space.weightDict.put( \amp, 5 );

// the higher the weightfactor, the more localised the value at the point will be.

x.free;
