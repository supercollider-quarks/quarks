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
// 	Class: Spherical
//	Support for spherical and cartesion (3d) coordinates
//---------------------------------------------------------------------


Spherical : Number {
	var <>rho, <>theta, <>phi;

	*new { arg rho, theta, phi;
		^super.newCopyArgs(rho, theta, phi)
	}

	magnitude { ^rho }

	angle { ^theta }						// implemented as a projection
	phase { ^theta }						// implemented as a projection
	angles { ^[theta, phi] }
	phases { ^[theta, phi] }

	x { ^rho * cos(theta) * cos(phi) }
	y { ^rho * sin(theta) * cos(phi) }
	z { ^rho * sin(phi) }
	real { ^this.x }
	imag { ^this.y }

	asSpherical { ^this }
	asPolar { ^this.asPoint.asPolar }				// implemented as a projection
	asComplex { ^Complex.new(this.real, this.imag) }	// implemented as a projection
	asPoint { ^Point.new(this.x, this.y) }			// implemented as a projection
	asCartesian { ^Cartesian.new(this.x, this.y, this.z) }
	scale { arg scale;
		^Spherical.new(rho * scale, theta, phi)
	}
	rotate { arg angle; // XY-plane, in radians
		^Spherical.new(rho, theta + angle, phi)
	}
	tilt { arg angle; // YZ-plane, in radians
		^this.asCartesian.tilt(angle).asSpherical
	}
	tumble { arg angle; // XZ-plane, in radians
		^this.asCartesian.tumble(angle).asSpherical
	}
	rotateXY { arg angle; // XY-plane,in radians
		^Spherical.new(rho, theta + angle, phi)
	}
	rotateYZ { arg angle; // YZ-plane,in radians
		^this.asCartesian.tilt(angle).asSpherical
	}
	rotateXZ { arg angle; // XZ-plane, in radians
		^this.asCartesian.tumble(angle).asSpherical
	}

	// do math as Cartesian
	// NOTE: value returned is different than Polar.sc, which returns complex values.
	//		One thought was to consider Olariu's 3d complex (tricomplex) numbers here,
	//		although these aren't directly analogous to complex.
	+ { arg aValue;
		^(this.asCartesian + aValue.asCartesian).asSpherical
	}
	- { arg aValue;
		^(this.asCartesian - aValue.asCartesian).asSpherical
	}
	* { arg aValue;
		^(this.asCartesian * aValue.asCartesian).asSpherical
	}
	/ { arg aValue;
		^(this.asCartesian / aValue.asCartesian).asSpherical
	}

	== { arg aSpherical;
		^aSpherical respondsTo: #[\rho, \theta, \phi ] and:
			{ rho == aSpherical.rho and:
				{ theta == aSpherical.theta and:
					{ phi == aSpherical.phi }
				}
			}
	}

	hash { ^(rho.hash bitXor: theta.hash) bitXor: phi.hash }
	
	neg { ^Spherical.new(rho, (theta + 2pi).mod(2pi) - pi, phi.neg) }
	mirrorX { ^this.asCartesian.mirrorX.asSpherical }
	mirrorY { ^this.asCartesian.mirrorY.asSpherical }
	mirrorZ { ^Spherical.new(rho, theta, phi.neg) }
	mirrorO { ^Spherical.new(rho, (theta + 2pi).mod(2pi) - pi, phi.neg) }

	// do math as Cartesian
	// NOTE: value returned is different than Polar.sc, which returns complex values.
	//		One thought was to consider Olariu's 3d complex (tricomplex) numbers here,
	//		although these aren't directly analogous to complex.
	performBinaryOpOnUGen { arg aSelector, aUGen;
		^Cartesian.new(
			BinaryOpUGen.new(aSelector, aUGen, this.x),
			BinaryOpUGen.new(aSelector, aUGen, this.y),
			BinaryOpUGen.new(aSelector, aUGen, this.z)
		).asSpherical;
	}

	printOn { arg stream;
		stream << "Spherical( " << rho << ", " << theta << ", " << phi << " )";
	}
	storeArgs { ^[rho,theta,phi] }
}
