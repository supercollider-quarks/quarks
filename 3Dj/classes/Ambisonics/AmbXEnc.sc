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
// AmbXEnc Class
//
// implements Ambisonic encoders up to 3rd order
// extended source shape
//
////////////////////////////////////////////////////////////////////////////

AmbXEnc {

	classvar <>delta = 0.001;

	*ar { |in, a=0, da=0, e=0, de=0, preserveArea=0, norm='N3D'|
		^ in * this.kr(a, da, e, de, preserveArea);
	}

	*kr { |a, da, e, de, preserveArea|

	}

	*nonZeroLengths { |da,de|
		// with values of 0 the equations get crazy
		da = BinaryOpUGen('==',da,0).if(delta,da);
		de = BinaryOpUGen('==',de,0).if(delta,de);

		^[da,de];
	}

	*limitElevation { |e,de|
		// e+de/2 <= pi/2 AND: e-de/2 >= -pi/2
		e = min(e,(pi/2)-(de/2));
		e = max(e,(-pi/2)+(de/2));
		^e;
	}

	*preserveArea { |preserveArea,da,e,de|
		/*
		if preserve_area :
		|  da = da / cos(e)
		|  if da > 2*pi :
		||    excess_area_factor = da / (2*pi)
		||    da = 2*pi
		||    de = de * excess_area_factor
		||    e = limit_e (e, de)
		*/
		var condition1, condition2;
		var excessAreaFactor;

		condition1 = preserveArea.if(1,0);
		da  = condition1.if(da/cos(e),da);

		condition2 = (condition1*(da>2pi)).if(1,0);
		excessAreaFactor = condition2.if(da/2pi,1);
		da = condition2.if(2pi,da);
		de = condition2.if(de*excessAreaFactor,de);
		e = condition2.if(this.limitElevation(e,de),e);

		^[da,e,de];
	}
}

AmbXEnc1 : AmbXEnc {
	classvar <order = 1;

	*kr { |a = 0, da = 0.001, e = 0, de = 0.001, preserveArea = 0, norm|

		// output channels
		var w,x,y,z;

		// define frequently used operations
		var sqrt3,csc_de_2,sec_e,sin_a,sin_da_2,da2,cos_2e,sin_de,sin_e,cos_a;


		// pre-process parameters
		#da,de = this.nonZeroLengths(da,de);
		e = this.limitElevation(e,de);
		#da,e,de = this.preserveArea(preserveArea,da,e,de);


		// pre-calculate frequently used operations
		sqrt3 = sqrt(3);
		csc_de_2 = 1/sin(de/2);
		sec_e = 1/cos(e);
		sin_a = sin(a);
		sin_da_2 = sin(da/2);
		da2 = da*2;
		cos_2e = cos(2*e);
		sin_de = sin(de);
		sin_e = sin(e);
		cos_a = cos(a);

		// get channel coefficients

		w = 1;

		//

		y =  ( (sqrt3 * de * csc_de_2 * sec_e * sin_a * sin_da_2) / da2 );
		y = y + ( (sqrt3 * cos_2e * csc_de_2 * sec_e * sin_a * sin_da_2 * sin_de) / da2 );

		z = (sqrt3 * csc_de_2 * sin_de * sin_e) / 2;

		x = ( (sqrt3 * de * cos_a * csc_de_2 * sec_e * sin_da_2) / da2 );
		x = x + ( (sqrt3 * cos_a * cos_2e * csc_de_2 * sec_e * sin_da_2 * sin_de) / da2 );

		// output coefficients

		^[w, y, z, x];
	}
}

AmbXEnc2 : AmbXEnc {
	classvar <order = 2;

	*kr { |a = 0, da = 0.001, e = 0, de = 0.001, preserveArea = 0, norm|

		// output channels
		var w,x,y,z,v,t,r,s,u;

		// define frequently used operations
		var sqrt3,csc_de_2,sec_e,sin_a,sin_da_2,da2,cos_2e,sin_de,sin_e,cos_a;
		var	sqrt15,sin_da,da4,sqrt5_3,cos_3e,cos_de,sqrt5,cos2_e,cos_2a,da8;


		// pre-process parameters
		#da,de = this.nonZeroLengths(da,de);
		e = this.limitElevation(e,de);
		#da,e,de = this.preserveArea(preserveArea,da,e,de);


		// pre-calculate frequently used operations
		sqrt3 = sqrt(3);
		csc_de_2 = 1/sin(de/2);
		sec_e = 1/cos(e);
		sin_a = sin(a);
		sin_da_2 = sin(da/2);
		da2 = da*2;
		cos_2e = cos(2*e);
		sin_de = sin(de);
		sin_e = sin(e);
		cos_a = cos(a);
		//
		sqrt15 = sqrt(15);
		sin_da = sin(da);
		da4 = da*4;
		sqrt5_3 = sqrt(5/3);
		cos_3e = cos(3*e);
		cos_de = cos(de);
		sqrt5 = sqrt(5);
		cos2_e = cos(e)**2;
		cos_2a = cos(2*a);
		da8 = da*8;

		// get channel coefficients

		w = 1;

		//

		y =  ( (sqrt3 * de * csc_de_2 * sec_e * sin_a * sin_da_2) / da2 );
		y = y + ( (sqrt3 * cos_2e * csc_de_2 * sec_e * sin_a * sin_da_2 * sin_de) / da2 );

		z = (sqrt3 * csc_de_2 * sin_de * sin_e) / 2;

		x = ( (sqrt3 * de * cos_a * csc_de_2 * sec_e * sin_da_2) / da2 );
		x = x + ( (sqrt3 * cos_a * cos_2e * csc_de_2 * sec_e * sin_da_2 * sin_de) / da2 );

		//

		v = ( (3 * sqrt15 * cos_a * sin_a * sin_da ) / da4 );
		v = v + ( (sqrt5_3 * cos_a * cos_3e * sec_e * sin_a * sin_da ) / da4 );
		v = v + ( (sqrt5_3 * cos_a * cos_de * cos_3e * sec_e * sin_a * sin_da) / da2 );

		t = ( (sqrt5_3 * ((cos((de-(2*e))/2)).pow(3)) * csc_de_2 * sec_e * sin_a * sin_da_2) / da );
		t = t - ( (sqrt5_3 * ((cos((de/2)+e)).pow(3)) * csc_de_2 * sec_e * sin_a * sin_da_2) / da );

		r = ( sqrt5/4 ) - ( (sqrt5 * cos2_e) / 2 ) - ( (sqrt5 * cos_de * cos_3e * sec_e) / 4 );

		s =  ( (sqrt5_3 * cos_a * ((cos((de-(2*e))/2)).pow(3)) * csc_de_2 * sec_e * sin_da_2 ) / da );
		s = s - ( (sqrt5_3 * cos_a * ((cos((de/2)+e)).pow(3)) * csc_de_2 * sec_e * sin_da_2 ) / da );

		u = ( ( 3 * sqrt15 * cos_2a * sin_da ) / da8 );
		u = u + ( (sqrt5_3 * cos_2a * cos_3e * sec_e * sin_da ) / da8 );
		u = u + ( (sqrt5_3 * cos_2a * cos_de * cos_3e * sec_e * sin_da ) / da4 );

		// output coefficients

		^[w, y, z, x, v, t, r, s, u];
	}
}

AmbXEnc3 : AmbXEnc {
	classvar <order = 3;

	*kr { |a = 0, da = 0.001, e = 0, de = 0.001, preserveArea = 0, norm|

		// output channels
		var w,x,y,z,v,t,r,s,u,q,o,m,k,l,n,p;

		// define frequently used operations
		var sqrt3,csc_de_2,sec_e,sin_a,sin_da_2,da2,cos_2e,sin_de,sin_e,cos_a;
		var	sqrt15,sin_da,da4,sqrt5_3,cos_3e,cos_de,sqrt5,cos2_e,cos_2a,da8;
		var da16,sqrt35_2,sin_3a,sin_3da_2,da12,cos_4e,sin_2de,da96,sqrt105,sin_2e,sqrt21_2,da32,sqrt7,sin_4e,cos_3a;


		// pre-process parameters
		#da,de = this.nonZeroLengths(da,de);
		e = this.limitElevation(e,de);
		#da,e,de = this.preserveArea(preserveArea,da,e,de);


		// pre-calculate frequently used operations
		sqrt3 = sqrt(3);
		csc_de_2 = 1/sin(de/2);
		sec_e = 1/cos(e);
		sin_a = sin(a);
		sin_da_2 = sin(da/2);
		da2 = da*2;
		cos_2e = cos(2*e);
		sin_de = sin(de);
		sin_e = sin(e);
		cos_a = cos(a);
		//
		sqrt15 = sqrt(15);
		sin_da = sin(da);
		da4 = da*4;
		sqrt5_3 = sqrt(5/3);
		cos_3e = cos(3*e);
		cos_de = cos(de);
		sqrt5 = sqrt(5);
		cos2_e = cos(e)**2;
		cos_2a = cos(2*a);
		da8 = da*8;
		//
		da16 = da*16;
		sqrt35_2 = sqrt(35/2);
		sin_3a = sin(3*a);
		sin_3da_2 = sin(3*da/2);
		da12 = da*12;
		cos_4e = cos(4*e);
		sin_2de = sin(2*de);
		da96 = da*96;
		sqrt105 = sqrt(105);
		sin_2e = sin(2*e);
		sqrt21_2 = sqrt(21/2);
		da32 = da*32;
		sqrt7= sqrt(7);
		sin_4e = sin(4*e);
		cos_3a = cos(3*a);

		// get channel coefficients

		w = 1;

		//

		y =  ( (sqrt3 * de * csc_de_2 * sec_e * sin_a * sin_da_2) / da2 );
		y = y + ( (sqrt3 * cos_2e * csc_de_2 * sec_e * sin_a * sin_da_2 * sin_de) / da2 );

		z = (sqrt3 * csc_de_2 * sin_de * sin_e) / 2;

		x = ( (sqrt3 * de * cos_a * csc_de_2 * sec_e * sin_da_2) / da2 );
		x = x + ( (sqrt3 * cos_a * cos_2e * csc_de_2 * sec_e * sin_da_2 * sin_de) / da2 );

		//

		v = ( (3 * sqrt15 * cos_a * sin_a * sin_da ) / da4 );
		v = v + ( (sqrt5_3 * cos_a * cos_3e * sec_e * sin_a * sin_da ) / da4 );
		v = v + ( (sqrt5_3 * cos_a * cos_de * cos_3e * sec_e * sin_a * sin_da) / da2 );

		t = ( (sqrt5_3 * ((cos((de-(2*e))/2)).pow(3)) * csc_de_2 * sec_e * sin_a * sin_da_2) / da );
		t = t - ( (sqrt5_3 * ((cos((de/2)+e)).pow(3)) * csc_de_2 * sec_e * sin_a * sin_da_2) / da );

		r = ( sqrt5/4 ) - ( (sqrt5 * cos2_e) / 2 ) - ( (sqrt5 * cos_de * cos_3e * sec_e) / 4 );

		s =  ( (sqrt5_3 * cos_a * ((cos((de-(2*e))/2)).pow(3)) * csc_de_2 * sec_e * sin_da_2 ) / da );
		s = s - ( (sqrt5_3 * cos_a * ((cos((de/2)+e)).pow(3)) * csc_de_2 * sec_e * sin_da_2 ) / da );

		u = ( ( 3 * sqrt15 * cos_2a * sin_da ) / da8 );
		u = u + ( (sqrt5_3 * cos_2a * cos_3e * sec_e * sin_da ) / da8 );
		u = u + ( (sqrt5_3 * cos_2a * cos_de * cos_3e * sec_e * sin_da ) / da4 );

		//

		q = ( (sqrt35_2 * de * csc_de_2 * sec_e * sin_3a * sin_3da_2) / da16 );
		q = q + ( (sqrt35_2 * cos_2e * csc_de_2 * sec_e * sin_3a * sin_3da_2 * sin_de) / da12 );
		q = q + ( (sqrt35_2 * cos_4e * csc_de_2 * sec_e * sin_3a * sin_3da_2 * sin_2de ) / da96 );

		o = ( (sqrt105 * cos_a * csc_de_2 * sec_e * sin_a * sin_da * sin_de * sin_2e) / da8 );
		o = o + ( (sqrt105 * cos_a * cos_de * cos_2e * csc_de_2 * sec_e * sin_a * sin_da * sin_de * sin_2e) / da8 );

		m = ( (sqrt21_2 * de * csc_de_2 * sec_e * sin_a * sin_da_2 ) / da16 );
		m = m - ( (sqrt21_2 * cos_2e * csc_de_2 * sec_e * sin_a * sin_da_2 * sin_de) / da4 );
		m = m - ( (5 * sqrt21_2 * cos_4e * csc_de_2 * sec_e * sin_a * sin_da_2 * sin_2de) / da32 );

		k = 0 - ( (sqrt7 * csc_de_2 * sec_e * sin_de * sin_2e ) / 16 );
		k = k - ( (5 * sqrt7 * cos_de * csc_de_2 * sec_e * sin_de * sin_4e) / 32 );

		l = ( (sqrt21_2 * de * cos_a * csc_de_2 * sec_e * sin_da_2 ) / da16 );
		l = l - ( (sqrt21_2 * cos_a * cos_2e * csc_de_2 * sec_e * sin_da_2 * sin_de ) / da4 );
		l = l - ( (5 * sqrt21_2 * cos_a * cos_4e * csc_de_2 * sec_e * sin_da_2 * sin_2de ) / da32 );

		n = ( (sqrt105 * cos_2a * csc_de_2 * sec_e * sin_da * sin_de * sin_2e ) / da16 );
		n = n + ( (sqrt105 * cos_2a * cos_de * cos_2e * csc_de_2 * sec_e * sin_da * sin_de * sin_2e) / da16 );

		p = ( (sqrt35_2 * de * cos_3a * csc_de_2 * sec_e * sin_3da_2 ) / da16 );
		p = p + ( (sqrt35_2 * cos_3a * cos_2e * csc_de_2 * sec_e * sin_3da_2 * sin_de) / da12 );
		p = p + ((sqrt35_2 * cos_3a * cos_4e * csc_de_2 * sec_e * sin_3da_2 * sin_2de) / da96 );

		// output coefficients

		^[w, y, z, x, v, t, r, s, u, q, o, m, k, l, n, p];
	}
}
