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

MIDINoteBox : RoundNumberBox {
	
	init { |parent, bounds|
		super.init( parent, bounds );
		clipLo = 0;
		clipHi = 127;
		step = 1;
		scroll_step = 1;
		ctrl_scale = 12;
		alt_scale = 0.01; // cents
		shift_scale = 36;
		allowedChars = "abcdefgABCDEFG#b-+. ";
		
		formatFunc = { |value|
			var output;
			output = value.midiname;
			case { output.cents > 0 } {
				output = output + "+" ++ output.cents;
			} { output.cents < 0 } {
				output = output + output.cents;
			};
			output;
		};
		
		interpretFunc = { |string| 
			var output, numbers;
			output = { string.namemidi }.try;
			if( output.isNil ) { 
				output = { string.interpret }.try;
				if( output.notNil && { output > clipHi } ) {
					output = output.cpsmidi;
				};
			} {
				numbers = string.extractNumbers;
				if( numbers.size > 1 ) {
					output = output + (numbers[1] / 100);
				};
			};
			output;
		};
	}
	
	
	
	applySmoothSkin {
		this.applySkin( ( 
			extrude: false,
			border: 0,
			background: Color.white.alpha_(0.5),
			typingColor: Color.red(0.5).alpha_(0.75)
		) );
	}
	
	applyRoundSkin {
		this.applySkin( ( 	
			extrude: true,
			border: 2,
			background: Color.white,
			typingColor: Color.red
		) );
	}

}
