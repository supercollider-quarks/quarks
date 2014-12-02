/*
	Filename: PanAmbi.sc 
	created: 11.4.2005 

	Copyright (C) IEM 2005, Christopher Frauenberger [frauenberger@iem.at] 

	This program is free software; you can redistribute it and/or 
	modify it under the terms of the GNU General Public License 
	as published by the Free Software Foundation; either version 2 
	of the License, or (at your option) any later version. 

	This program is distributed in the hope that it will be useful, 
	but WITHOUT ANY WARRANTY; without even the implied warranty of 
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
	GNU General Public License for more details. 

	You should have received a copy of the GNU General Public License 
	along with this program; if not, write to the Free Software 
	Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA. 

	IEM - Institute of Electronic Music and Acoustics, Graz 
	Inffeldgasse 10/3, 8010 Graz, Austria 
	http://iem.at
*/


/* 	Class: PanAmbi
	Ambisonics encoder base class for 3D and up to 5th order
*/
PanAmbi {
	
	/* 	Class method: *ar
	   	Audio rate method
	   	Parameter
	   		in: Mono audio input
			azi: Azimuth angle of source
			elev: Elevation angle of source
			level: Level/gain 
		Return
			out Array[] of Ambisonics signals (number depends on order)
	*/
	*ar { |in, azi=0, elev=0, level=1| 
		^(in * level) * this.kr(azi, elev);
	}
	
	/* 	Class method: *kr
	   	Control rate method providing the Ambisonics coeffs
	   	MUST be implemented by the derived classes 
	   	Parameter
			azi: Azimuth angle of source
			elev: Elevation angle of source
		Return
			coeffs: Array[] of Ambisonics coeffs (number depends on order)
	*/
	*kr { |azi, elev|
	
	}
}	

/* 	Class: PanAmbi1O
	Ambisonics encoder for 3D, 1st order
*/
PanAmbi1O : PanAmbi {
	classvar <order = 1;

	/* 	Class method: *kr
	   	Control rate method providing the Ambisonics coeffs 
	   	Parameter
			azi: Azimuth angle of source
			elev: Elevation angle of source
		Return
			coeffs: Array[4] of Ambisonics coeffs 
	*/
	*kr { |azi, elev|
		
		var cosElev, sinElev, cosAzi, sinAzi;
		
		cosElev = cos(elev);
		sinElev = sin(elev);
		cosAzi = cos(azi);
		sinAzi = sin(azi);

		// calc and keep only the coeffs needed
		^[1, cosElev*cosAzi, cosElev*sinAzi, sinElev];
	}
}	

/* 	Class: PanAmbi2O
	Ambisonics encoder for 3D, 2nd order
*/
PanAmbi2O : PanAmbi {
	classvar <order = 2;

	/* 	Class method: *kr
	   	Control rate method providing the Ambisonics coeffs 
	   	Parameter
			azi: Azimuth angle of source
			elev: Elevation angle of source
		Return
			coeffs: Array[9] of Ambisonics coeffs 
	*/
	*kr { |azi, elev|
		
		var s3, cosElev, sinElev, cosAzi, sinAzi;
		
		s3 = sqrt(3);
		cosElev = cos(elev);
		sinElev = sin(elev);
		cosAzi = cos(azi);
		sinAzi = sin(azi);

		// calc and keep only the coeffs needed
		^[ 	1 , cosElev*cosAzi, cosElev*sinAzi, sinElev,
			s3/2*(cosElev.squared) * cos(2*azi), s3/2*(cosElev.squared) * sin(2*azi),
			s3*cosElev*sinElev*cosAzi, s3*cosElev*sinElev*sinAzi,
			1/2*(3*(sinElev.squared)-1)
		];
	}
}	

/* 	Class: PanAmbi3O
	Ambisonics encoder for 3D, 3rd order
*/
PanAmbi3O : PanAmbi {
	classvar <order = 3;

	/* 	Class method: *kr
	   	Control rate method providing the Ambisonics coeffs 
	   	Parameter
			azi: Azimuth angle of source
			elev: Elevation angle of source
		Return
			coeffs: Array[16] of Ambisonics coeffs 
	*/
	*kr { |azi, elev|
		
		var s3, s10, s15, s6, cosElev, cosElev2, cosElev3, sinElev, sinElev2, cosAzi, cos2Azi, sinAzi, sin2Azi;
		var s3d2cosElev2, s3cosElevsinElev, s10d4cosElev3, s15d2cosElev2sinElev, s6d4cosElev5sinElev2m1;

		s3 = sqrt(3);
		s10 = sqrt(10);
		s15 = sqrt(15);
		s6 = sqrt(6);
		cosElev = cos(elev); cosElev2 = cosElev.squared; cosElev3 = cosElev2 * cosElev;
		sinElev = sin(elev); sinElev2 = sinElev.squared;
		cosAzi = cos(azi); cos2Azi = cos(2*azi);
		sinAzi = sin(azi); sin2Azi = sin(2*azi);

		// some optimisations
		s3d2cosElev2 = s3/2*cosElev2;
		s3cosElevsinElev = s3*cosElev*sinElev;
		s10d4cosElev3 = s10/4*cosElev3;
		s15d2cosElev2sinElev = s15/2*cosElev2*sinElev;
		s6d4cosElev5sinElev2m1 = s6/4*cosElev*(5*sinElev2-1);
		
		// return the coefficients
		^[ 	1 , cosElev*cosAzi, cosElev*sinAzi, sinElev,
			s3d2cosElev2*cos2Azi, s3d2cosElev2*sin2Azi,
			s3cosElevsinElev*cosAzi, s3cosElevsinElev*sinAzi,
			1/2*(3*sinElev2-1),
			// 3rd order from here 
			s10d4cosElev3*cos(3*azi), s10d4cosElev3*sin(3*azi),
			s15d2cosElev2sinElev*cos2Azi, s15d2cosElev2sinElev*sin2Azi,
			s6d4cosElev5sinElev2m1*cosAzi, s6d4cosElev5sinElev2m1*sinAzi, 
			1/2*sinElev*(5*sinElev2-3)
		];
	}
}

/* 	Class: PanAmbi
	Ambisonics encoder for 3D, 4th order
*/
PanAmbi4O : PanAmbi {
	classvar <order = 4;
}

/* 	Class: PanAmbi
	Ambisonics encoder for 3D, 5th order
*/
PanAmbi5O : PanAmbi {
	classvar <order = 5;
}
