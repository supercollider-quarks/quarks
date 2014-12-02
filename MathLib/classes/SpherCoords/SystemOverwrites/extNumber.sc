/*
	Copyright the ATK Community and Joseph Anderson, 2011
		J Anderson	j.anderson[at]ambisonictoolkit.net 


	This file is part of SpherCoords.
	
	SpherCoords is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.
	
	SpherCoords is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.
	
	You should have received a copy of the GNU General Public License
	along with SpherCoords.  If not, see <http://www.gnu.org/licenses/>.
*/

//---------------------------------------------------------------------
//	SpherCoords is a spherical and 3D cartesian coordinate support library.
//
// 	Extension: Number
//	Support for spherical and cartesion (3d) coordinates
//---------------------------------------------------------------------

+ Number {

	performBinaryOpOnCartesian { arg op, aCartesian, adverb;
		^Cartesian.new(
			this.perform(op, aCartesian.x, adverb),
			this.perform(op, aCartesian.y, adverb),
			this.perform(op, aCartesian.z, adverb)
		);
	}

	// spherical support
	phi { ^0.0 }

	// conversion
	@ { arg aValue;								// overload default method
		aValue.isKindOf(SimpleNumber).if(
			{ ^Point.new(this, aValue) },			// default SC
			{ ^Cartesian.new(this, aValue.x, aValue.y) }
		)
		}
	spherical { arg theta, phi; ^Spherical.new(this, theta, phi) }
}
