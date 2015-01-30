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

// a simple class mainly to display an angle in a compileString

Angle {
	
	var <>value;
	
	*new { |value|
		^super.newCopyArgs( value ? 0);
	}
	
	printOn { |stream|
		stream << case { 
			value == 0 
			} { 
				value 
			} { 
				value == pi 
			} { 
				"pi" 
			} { 
				(value / pi).asString ++ "pi" 
			};
	}
	storeOn { |stream|
		this.printOn( stream );
	}
	
}