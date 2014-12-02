////////////////////////////////////////////////////////////////////////////
//
// Copyright (C) Fundació Barcelona Media, October 2014 [www.barcelonamedia.org]
// Author: Andrés Pérez López [contact@andresperezlopez.com]
// original Copyright (C) IEM 2005, Christopher Frauenberger [frauenberger@iem.at]
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; withot even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>
//
////////////////////////////////////////////////////////////////////////////
//
// AmbSMEnc Class
//
// implements Ambisonic encoders up to 3rd order
// semi-meridian source shape
//
////////////////////////////////////////////////////////////////////////////

AmbSMEnc {
	*ar { |in, azimuth=0, norm='N3D'|
		^ in * this.kr(azimuth,norm);
	}

	*kr { |azimuth|

	}
}

AmbSMEnc1 : AmbSMEnc{
	classvar <order = 1;

	*kr { |a, norm|
		// elevation, norm

		var w,x,y,z;

		var sqrt3 = sqrt(3);
		var sin_a = sin(a);
		var cos_a = cos(a);

		w = 1;
		//
		y = (2 * sqrt3 * sin_a) / pi;
		z = 0;
		x = (2 * sqrt3 * cos_a) / pi;

		^[w, y, z, x];
	}
}

AmbSMEnc2 : AmbSMEnc{
	classvar <order = 2;

	*kr { |a, norm|
		// elevation, norm

		var w,x,y,z,v,t,r,s,u;

		var sqrt3 = sqrt(3);
		var sin_a = sin(a);
		var cos_a = cos(a);
		//
		var sqrt15 = sqrt(15);

		w = 1;
		//
		y = (2 * sqrt3 * sin_a) / pi;
		z = 0;
		x = (2 * sqrt3 * cos_a) / pi;
		//
		v = (sqrt15 * cos_a * sin_a ) / 2;
		t = 0;
		r = sqrt(5) / 4;
		s = 0;
		u = (sqrt15 * cos(2*a)) / 4;

		^[w, y, z, x, v, t, r, s, u];
	}
}

AmbSMEnc3 : AmbSMEnc {
	classvar <order = 3;

	/* 	Class method: *kr
	Control rate method providing the Ambisonics coeffs
	Parameter
	azi: Azimuth angle of source
	elev: Elevation angle of source
	Return
	coeffs: Array[16] of Ambisonics coeffs
	*/

	*kr { |a, norm|
		// azimuth, norm

		var w,x,y,z,v,t,r,s,u,q,o,m,k,l,n,p;

		var sqrt3 = sqrt(3);
		var sin_a = sin(a);
		var cos_a = cos(a);
		//
		var sqrt15 = sqrt(15);
		//
		var sqrt70 = sqrt(70);
		var sqrt14_3 = sqrt(14/3);

		w = 1;
		//
		y = (2 * sqrt3 * sin_a) / pi;
		z = 0;
		x = (2 * sqrt3 * cos_a) / pi;
		//
		v = (sqrt15 * cos_a * sin_a ) / 2;
		t = 0;
		r = sqrt(5) / 4;
		s = 0;
		u = (sqrt15 * cos(2*a)) / 4;
		//
		q = (sqrt70 * sin(3*a)) / (3pi);
		o = 0;
		m = (sqrt14_3 * sin_a) / pi;
		k = 0;
		l = (sqrt14_3 * cos_a) / pi;
		n = 0;
		p = (sqrt70 * cos(3*a)) / (3pi);

		^[w, y, z, x, v, t, r, s, u, q, o, m, k, l, n, p];
	}
}
