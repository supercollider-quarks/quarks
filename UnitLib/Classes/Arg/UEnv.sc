/*
    Unit Library
    The Game Of Life Foundation. http://gameoflife.nl
    Copyright 2006-2011 Miguel Negrao, Wouter Snoei.

    GameOfLife Unit Library: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GameOfLife Unit Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GameOfLife Unit Library.  If not, see <http://www.gnu.org/licenses/>.
*/

/*
UEnv automatically creates a standard envelope for a U (unit).

It creates the following private controls automatically:

	u_gate (1) : a gate to release the envelope
	u_dur (inf) : a duration (s) for the unit, may be inf (default)
	u_doneAction (0) : a doneAction for when the envelope is released or finished
	                   this may release the unit itself (2) or the whole UChain (14)
	u_gain (0) : overall gain in dB (0 == no change, -inf = silent)
	u_fadeIn (0) : fade in time (s)
	u_fadeOut (0) : fade out time (s), within the duration or after the release

An UEnv is typically applied once in a unit that has output(s) to the hardware buses.
In UChains it is used internally for releasing, gain and setting the overal level.
Units containing a UEnv are usually found at the tail of a UChain, and UChains should
contain at least one unit with a UEnv (otherwise they have no control over fade times, 
duration etc.).
 
*/

UEnv : UIn {
	
	*initClass {
		specs.addAll([
			\dur -> ControlSpec(0, inf),
			\doneAction -> ControlSpec(0, 14, \lin, 1),
			\gain -> ControlSpec( -inf, 24, \db ),
			\mute -> BoolSpec( false ),
			\fadeIn -> ControlSpec( 0, inf ),
			\fadeOut -> ControlSpec( 0, inf ),
			\fadeInCurve -> ControlSpec(-20, 20),
			\fadeOutCurve -> ControlSpec(-20, 20);
		]);
	}
	
	*kr { |gain = 0, fadeIn = 0, fadeOut = 0, extraSilence = 0, useGlobalGain = 1, ignoreFadeIn = false|
		var name = this.getControlName( );
		var gate = this.getControl( \kr, name, 'gate', 1 );
		var mute = this.getControl( \kr, name, 'mute', 0 );
		var dur = this.getControl( \kr, name, 'dur', inf );
		var doneAction = this.getControl( \kr, name, 'doneAction', 0, argMode: \init );

		var fadeInCurve = this.getControl( \kr, name, 'fadeInCurve', 0 );
		var fadeOutCurve = this.getControl( \kr, name, 'fadeOutCurve', 0 );

		gain = this.getControl( \kr, name, 'gain', gain, 0.5 ); // 0.5s lag time
		if( ignoreFadeIn != true ) {
			fadeIn = this.getControl( \kr, name, 'fadeIn', fadeIn );
		};
		fadeOut = this.getControl( \kr, name, 'fadeOut', fadeOut );
		
		^DemandEnvGen.kr( 
				Dseq( [ 0, 1, 1, 0, 0 ], 1 ), 
				Dseq( [ fadeIn, dur - (fadeIn+fadeOut), fadeOut, extraSilence ], 1 ),
				//5, Dseq( [ fadeInCurve, 1, fadeOutCurve, 1 ], 1 ),
				doneAction: doneAction ) *
			Env([ 1, 0, 0 ],[ fadeOut, extraSilence ], \lin, 0 )
				.kr( doneAction, RunningMin.kr( gate ) + Impulse.kr(0) ) *
			UGlobalGain.kr( useGlobalGain ) * gain.dbamp * (1-mute);
	}
	
	*ar { ^this.shouldNotImplement }
}


