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
// 	Extension: Polar
//	Support for spherical and cartesion (3d) coordinates
//---------------------------------------------------------------------

+ Polar {

	// spherical support
	phi { ^0.0 }
	angles { ^[ this.theta, 0.0 ] }
	phases { ^[ this.theta, 0.0 ] }
	
	// Point, Cartesian
	x { ^this.real }
	y { ^this.imag }
	z { ^0 }

	// conversion
	asSpherical { ^Spherical.new(this.rho, this.theta, 0.0) }
	asCartesian { ^Cartesian.new(this.real, this.imag, 0.0) }
		
	// mirror
	mirrorX { ^this.asPoint.mirrorX.asPolar }
	mirrorY { ^this.asPoint.mirrorY.asPolar }
	mirrorZ { ^this }
	mirrorO { ^this.neg }
	
}
