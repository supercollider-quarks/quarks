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

USampleOffset : Spec {
	
	*ar { |in, use = 1|
		var delay;
		Udef.addBuildSpec( 
			ArgSpec( \u_sampleOffset, this, this, true, \init ) 
		);
		delay = (\u_sampleOffset.ir( 0 ) * use) / SampleRate.ir;
		^DelayN.ar( in, delay, delay );	
	}
	
	*getCurrent {
		var round, count;
		if( SyncCenter.current.notNil && { SyncCenter.mode === \sample } ) {
			round = SyncCenter.round;
			count = SyncCenter.current.masterSampleCount( Server.default.latency );
			^round - (count.roundUp( round ) - count);
		} {
			^0
		};
	}

	*asUGenInput { ^this.getCurrent }
	*asControlInput { ^this.getCurrent }
	*asOSCArgEmbeddedArray { | array| ^this.getCurrent.asOSCArgEmbeddedArray(array) }

	
	// fixed output: 
	*new { ^this } // only use as class
	
	*asSpec { ^this }
	
	*constrain { ^this } // whatever comes in; UGlobalEQ comes out
	
	*default {  ^this }
	
	*massEditSpec { ^nil }
	
	*findKey {
		^Spec.specs.findKeyForValue(this);
	}
}
