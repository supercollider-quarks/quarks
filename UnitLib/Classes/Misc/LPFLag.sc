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

LPFLag {
	
	// a lag based on LPF filter
	// switches to input at lag == 0
	// may cause artifacts at fast lagTime changes
	
	*getLag { |rate = \control, in = 0, lagTime = 0|
		var dc;
		dc = DC.perform( DC.methodSelectorForRate( rate ), in); // capture first value
		^LPF.perform( LPF.methodSelectorForRate( rate ),
			 in - dc,
			 1 / ( lagTime.max( 2 / switch( rate, 
			 		\control, {ControlRate.ir},
			 		\audio, {SampleRate.ir}
				)
			))
		) + dc;
	}
	
	*ar { |in, lagTime = 0|
		if ( lagTime.isUGen ) {
			^if( lagTime > 0, this.getLag( \audio, in, lagTime ), in );
		} {
			^if( lagTime > 0 ) { this.getLag( \audio, in, lagTime ); } { in; };
		};
	}
	
	*kr { |in, lagTime = 0|
		if ( lagTime.isUGen ) {
			^if( lagTime > 0, this.getLag( \control, in, lagTime ), in );
		} {
			^if( lagTime > 0 ) { this.getLag( \control, in, lagTime ); } { in; };
		};	
	}
}
