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
// AmbEnc Class
//
// based on Frauenberger implementation of PanAmbi.sc
//
// implements Ambisonic encoders up to 3rd order
// N3D normalization,
// ACN order : W, Y,Z,X, ...
//
// TODO:
// - contemplate other normalization factors
// - optimize operations
//
////////////////////////////////////////////////////////////////////////////

AmbEnc {
	*ar { |in, azi=0, elev=0, norm='N3D'|
		^ in * this.kr(azi,elev,norm);
	}

	*kr { |azi, elev|

	}
}

// fist order

AmbEnc1 : AmbEnc{
	classvar <order = 1;

	*kr { |phi, theta, norm|

		var cosTheta, cosPhi, sinTheta, sinPhi;
		var w,x,y,z;
		var factor;
		var sqrt3 = sqrt(3);

		// factor = switch (norm)
		// {'N3D'} {sqrt(3)};

		cosTheta=cos(theta);
		cosPhi=cos(phi);
		sinTheta=sin(theta);
		sinPhi=sin(phi);

		w = 1;
		y = sqrt3 * cosTheta * sinPhi;
		z = sqrt3 * sinTheta;
		x = sqrt3 * cosTheta * cosPhi;

		^[w, y, z, x];
	}
}

// second order

AmbEnc2 : AmbEnc {
	classvar <order = 2;

	*kr { |phi, theta, norm|

		var cosTheta, cosPhi, sinTheta, sinPhi;
		var sin2Phi, cos2Phi, sin2Theta, cosTheta2, sinTheta2;
		var w,x,y,z,v,t,r,s,u;
		// var f1,f2;
		var sqrt3=sqrt(3);
		var sqrt5=sqrt(5);


/*		switch (norm)
		{'N3D'} {f1=sqrt(3); f2=sqrt(5)};*/

		cosTheta=cos(theta);
		cosPhi=cos(phi);
		sinTheta=sin(theta);
		sinPhi=sin(phi);

		sin2Phi=sin(2*phi);
		cos2Phi=cos(2*phi);
		sin2Theta=sin(2*theta);
		cosTheta2=cosTheta*cosTheta;
		sinTheta2=sinTheta*sinTheta;


		w = 1;
		//
		y = sqrt3 * cosTheta * sinPhi;
		z = sqrt3 * sinTheta;
		x = sqrt3 * cosTheta * cosPhi;
		//
		v = sqrt5 * sqrt3 / 2 * sin2Phi * cosTheta2;
		t = sqrt5 * sqrt3 / 2 * sinPhi * sin2Theta;
		r = sqrt5 * ((3 * sinTheta2) - 1) / 2;
		s = sqrt5 * sqrt3 / 2 * cosPhi * sin2Theta;
		u = sqrt5 * sqrt3 / 2 * cos2Phi * cosTheta2;

		^[w, y, z, x, v, t, r, s, u];

	}
}

// third order

AmbEnc3 : AmbEnc {
	classvar <order = 3;

	/* 	Class method: *kr
	   	Control rate method providing the Ambisonics coeffs
	   	Parameter
			phi: Azimuth angle of source
			theta: Elevation angle of source
		Return
			coeffs: Array[16] of Ambisonics coeffs
	*/
	*kr { |phi, theta, norm|

		var cosTheta, cosPhi, sinTheta, sinPhi;
		var sin2Phi, cos2Phi, sin2Theta, cosTheta2, sinTheta2;
		var sin3Phi, cos3Phi, cosTheta3;
		var w,x,y,z,v,t,r,s,u,q,o,m,k,l,n,p;
		// var f1,f2;
		var sqrt3=sqrt(3);
		var sqrt5=sqrt(5);
		var sqrt7=sqrt(7);
		var sqrt5_8=sqrt(5/8);
		var sqrt15=sqrt(15);
		var sqrt3_8=sqrt(3/8);

/*		switch (norm)
		{'N3D'} {f1=sqrt(3); f2=sqrt(5)};*/

		cosTheta=cos(theta);
		cosPhi=cos(phi);
		sinTheta=sin(theta);
		sinPhi=sin(phi);

		sin2Phi=sin(2*phi);
		cos2Phi=cos(2*phi);
		sin2Theta=sin(2*theta);
		cosTheta2=cosTheta*cosTheta;
		sinTheta2=sinTheta*sinTheta;

		sin3Phi=sin(3*phi);
		cos3Phi=cos(3*phi);
		cosTheta3=cosTheta2*cosTheta;



		w = 1;
		//
		y = sqrt3 * cosTheta * sinPhi;
		z = sqrt3 * sinTheta;
		x = sqrt3 * cosTheta * cosPhi;
		//
		v = sqrt5 * sqrt3 / 2 * sin2Phi * cosTheta2;
		t = sqrt5 * sqrt3 / 2 * sinPhi * sin2Theta;
		r = sqrt5 * ((3 * sinTheta2) - 1) / 2;
		s = sqrt5 * sqrt3 / 2 * cosPhi * sin2Theta;
		u = sqrt5 * sqrt3 / 2 * cos2Phi * cosTheta2;
		//
		q = sqrt7 * sqrt5_8 * sin3Phi * cosTheta3;
		o = sqrt7 * sqrt15 / 2 * sin2Phi * sinTheta * cosTheta2;
		m = sqrt7 * sqrt3_8 * sinPhi * cosTheta * ((5 * sinTheta2) - 1);
		k = sqrt7 * sinTheta * ((5 * sinTheta2) - 3) / 2;
		l = sqrt7 * sqrt3_8 * cosPhi * cosTheta * ((5 * (sinTheta2)) - 1);
		n = sqrt7 * sqrt15 / 2 * cos2Phi * sinTheta * cosTheta2;
		p = sqrt7 * sqrt5_8 * cos3Phi * cosTheta3;

		^[w, y, z, x, v, t, r, s, u, q, o, m, k, l, n, p];
	}
}
