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
// AmbMrEnc Class
//
// implements Ambisonic encoders up to 3rd order
// restricted meridian source shape
//
////////////////////////////////////////////////////////////////////////////

AmbMrEnc {
	*ar { |in, azimuth=0, norm='N3D'|
		^ in * this.kr(azimuth,norm);
	}

	*kr { |azimuth|

	}
}

AmbMrEnc1 : AmbMrEnc{
	classvar <order = 1;

	*kr { |a, norm|
		// elevation, norm

		var w,x,y,z;

		w = 1;
		//
		y = 0;
		z = (2 * sqrt(3)) / pi;
		x = 0;

		^[w, y, z, x];
	}
}

AmbMrEnc2 : AmbMrEnc{
	classvar <order = 2;

	*kr { |a, norm|
		// elevation, norm

		var w,x,y,z,v,t,r,s,u;

		var sqrt15=sqrt(15);

		w = 1;
		//
		y = 0;
		z = (2 * sqrt(3)) / pi;
		x = 0;
		//
		v = (sqrt15 * cos(a) * sin(a)) / 2;
		t = 0;
		r = sqrt(5) / 4;
		s = 0;
		u = (sqrt15 * cos(2*a)) / 4;

		^[w, y, z, x, v, t, r, s, u];
	}
}

AmbMrEnc3 : AmbMrEnc {
	classvar <order = 3;



	/* Class method: *kr
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

		var sqrt15 = sqrt(15);
		var cos_2a = cos(2*a);
		//
		var sqrt35_3 = sqrt(35/3);


		w = 1;
		//
		y = 0;
		z = (2 * sqrt(3)) / pi;
		x = 0;
		//
		v = (sqrt15 * cos(a) * sin(a)) / 2;
		t = 0;
		r = sqrt(5) / 4;
		s = 0;
		u = (sqrt15 * cos_2a) / 4;
		//
		q = 0;
		o = (sqrt35_3 * sin(2*a) ) / pi;
		m = 0;
		k = sqrt(7) / (3pi);
		l = 0;
		n = (sqrt35_3 * cos_2a) / pi;
		p = 0;

		^[w, y, z, x, v, t, r, s, u, q, o, m, k, l, n, p];
	}
}
