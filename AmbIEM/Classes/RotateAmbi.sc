/*
	Filename: RotateAmbi.sc 
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

/*	Class: RotateAmbi
	base class providing the audio rate method to for derived classes
	derived classes MUST implement the kr method to provide the rotation matrix
*/
RotateAmbi {

	/* 	Class method: *ar
	   	Audio rate method
	   	Parameter
	   		in: Array[] of Ambisonics channels (number depends on order)
			px: rotation along the x axis
			py: rotation along the y axis
			pz: rotation along the z axis
		Return
			out: Array[] of Ambisonics channels (number depends on order)
	*/
	*ar { |in, px=0, py=0, pz=0| 
		var out;
		// return the rotated audio signal
		out = Array.new(in.size);
		this.kr(px, py, pz).do { arg row; 
			out.add((row * in).sum);
		}
	 	^out;	
	}
}


/* 	Class: RotateAmbi1O
	Ambisonics rotation class for Ambisonics 1st order
*/
RotateAmbi1O : RotateAmbi { 

	/* 	Class mmethod: *kr
		Control rate method providing the rotation matrix
		Parameters
			px: rotation along the x axis
			py: rotation along the y axis
			pz: rotation along the z axis
		Return
			r: Rotation matrix
	*/
	*kr { |px=0, py=0, pz=0|
			
		// the rotation matrices per axis
		var rx, ry, rz, r;
		
		var sinx, cosx;
		var siny, cosy;
		var sinz, cosz;
				
		// all sinus / cosinus terms
		sinx = sin(px);
		cosx = cos(px);
		siny = sin(py);
		cosy = cos(py);
		sinz = sin(pz);
		cosz = cos(pz);
		
		// the rotation matrices
		rz = [[1, 0, 0, 0],
			[0, cosz, -1*sinz, 0], 
			[0, sinz, cosz, 0],
			[0, 0, 0, 1]];
		ry = [[1, 0, 0, 0],
			[0, cosy, 0, -1*siny], 
			[0, 0, 1, 0],
			[0, siny, 0, cosy]];
		rx = [[1, 0, 0, 0],
			[0, 1, 0, 0], 
			[0, 0, cosx, -1*sinx],
			[0, 0, sinx, cosx]];
		
		// the complete rotation matrix	
		^r = Matrix.mul(rz, Matrix.mul(ry, rx));
	}
}

/* 	Class: RotateAmbi2O
	Ambisonics rotation class for Ambisonics 2nd order
*/
RotateAmbi2O : RotateAmbi { 

	/* 	Class mmethod: *kr
		Control rate method providing the rotation matrix
		Parameters
			px: rotation along the x axis
			py: rotation along the y axis
			pz: rotation along the z axis
		Return
			r: Rotation matrix
	*/
	*kr { |px=0, py=0, pz=0|
			
		// the rotation matrices per axis
		var rx, ry, rz, r;
		
		var sinx, sin2x, cosx, cos2x;
		var siny, sin2y, cosy, cos2y;
		var sinz, sin2z, cosz, cos2z;
		var s34, s32;
				
		// all sinus / cosinus terms
		sinx = sin(px); sin2x = sin(2*px);
		cosx = cos(px); cos2x = cos(2*px);
		siny = sin(py); sin2y = sin(2*py);
		cosy = cos(py); cos2y = cos(2*py);
		sinz = sin(pz); sin2z = sin(2*pz);
		cosz = cos(pz); cos2z = cos(2*pz);
		
		s34 = sqrt(3)/4;
		s32 = 2*s34;
		
		// the rotation matrices
		rz = [[1, 0, 0, 0, 0, 0, 0, 0, 0],
			[0, cosz, -1*sinz, 0, 0, 0, 0, 0, 0], 
			[0, sinz, cosz, 0, 0, 0, 0, 0, 0],
			[0, 0, 0, 1, 0, 0, 0, 0, 0], 
			[0, 0, 0, 0, cos2z, -1*sin2z, 0, 0, 0],
			[0, 0, 0, 0, sin2z, cosz, 0, 0, 0], 
			[0, 0, 0, 0, 0, 0, cosz, -1*sinz, 0],
			[0, 0, 0, 0, 0, 0, sinz, cosz, 0], 
			[0, 0, 0, 0, 0, 0, 0, 0, 1]];
		ry = [[1, 0, 0, 0, 0, 0, 0, 0, 0],
			[0, cosy, 0, -1*siny, 0, 0, 0, 0, 0], 
			[0, 0, 1, 0, 0, 0, 0, 0, 0],
			[0, siny, 0, cosy, 0, 0, 0, 0, 0], 
			[0, 0, 0, 0, 1/4*(3+cos2y), 0, -1/2*sin2y, 0, s34*(1-cos2y)],
			[0, 0, 0, 0, 0, cosy, 0, -1*siny, 0], 
			[0, 0, 0, 0, 1/2*sin2y, 0, cosy, 0, -1*s32*sin2y],
			[0, 0, 0, 0, 0, siny, 0, cosy, 0], 
			[0, 0, 0, 0, s34*(1-cos2y), 0, s32*sin2y, 0, 1/4*(1+(3*cos2y))]];
		rx = [[1, 0, 0, 0, 0, 0, 0, 0, 0],
			[0, 1, 0, 0, 0, 0, 0, 0, 0], 
			[0, 0, cosx, -1*sinx, 0, 0, 0, 0, 0],
			[0, 0, sinx, cosx, 0, 0, 0, 0, 0], 
			[0, 0, 0, 0, 1/4*(3+cos2x), 0, 0, 1/2*sin2x, -1*s34*(1-cos2x)],
			[0, 0, 0, 0, 0, cosx, -1*sinx, 0, 0], 
			[0, 0, 0, 0, 0, sinx, cosx, 0, 0],
			[0, 0, 0, 0, -1/2*sin2x, 0, 0, cos2x, -1*s32*sin2x], 
			[0, 0, 0, 0, -1*s34*(1-cos2x), 0, 0, s32*sin2x, 1/4*(1+(3*cos2x))]];
		
		// the complete rotation matrix	
		^r = Matrix.mul(rz, Matrix.mul(ry, rx));
	}
}

/* 	Class: RotateAmbi3O
	Ambisonics rotation class for Ambisonics 3rd order
*/
RotateAmbi3O : RotateAmbi { 

	/* 	Class mmethod: *kr
		Control rate method providing the rotation matrix
		Parameters
			px: rotation along the x axis
			py: rotation along the y axis
			pz: rotation along the z axis
		Return
			r: Rotation matrix
	*/
	*kr { |px=0, py=0, pz=0|
			
		// the rotation matrices per axis
		var rx, ry, rz, r;
		
		var sinx, sin2x, sin3x, cosx, cos2x, cos3x;
		var siny, sin2y, sin3y, cosy, cos2y, cos3y;
		var sinz, sin2z, sin3z, cosz, cos2z, cos3z;
		var s3, s6, s10, s15;
				
		// all sinus / cosinus terms
		sinx = sin(px); sin2x = sin(2*px); sin3x = sin(3*px);
		cosx = cos(px); cos2x = cos(2*px); cos3x = cos(3*px);
		siny = sin(py); sin2y = sin(2*py); sin3y = sin(3*py);
		cosy = cos(py); cos2y = cos(2*py); cos3y = cos(3*py);
		sinz = sin(pz); sin2z = sin(2*pz); sin3z = sin(3*pz);
		cosz = cos(pz); cos2z = cos(2*pz); cos3z = cos(3*pz);
		
		s3 = sqrt(3);
		s6 = sqrt(6);
		s10 = sqrt(10);
		s15 = sqrt(15);
		
		// the rotation matrices
		rz = [[1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
			[0, cosz, -1*sinz, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0], 
			[0, sinz, cosz, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
			[0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0], 
			[0, 0, 0, 0, cos2z, -1*sin2z, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
			[0, 0, 0, 0, sin2z, cosz, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0], 
			[0, 0, 0, 0, 0, 0, cosz, -1*sinz, 0, 0, 0, 0, 0, 0, 0, 0],
			[0, 0, 0, 0, 0, 0, sinz, cosz, 0, 0, 0, 0, 0, 0, 0, 0], 
			[0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0],
			[0, 0, 0, 0, 0, 0, 0, 0, 0, cos3z, -1*sin3z, 0, 0, 0, 0, 0],
			[0, 0, 0, 0, 0, 0, 0, 0, 0, sin3z, cos3z, 0, 0, 0, 0, 0],
			[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, cos2z, -1*sin2z, 0, 0 ,0], 
			[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, sin2z, cos2z, 0, 0, 0],
			[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, cosz, -1*sinz, 0],
			[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, sinz, cosz, 0],
			[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1]
			];
		ry = [[1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
			[0, cosy, 0, -1*siny, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0], 
			[0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
			[0, siny, 0, cosy, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
			[0, 0, 0, 0, 1/4*(3+cos2y), 0, -1/2*sin2y, 0, s3/4*(1-cos2y), 0, 0, 0, 0, 0, 0, 0],
			[0, 0, 0, 0, 0, cosy, 0, -1*siny, 0, 0, 0, 0, 0, 0, 0, 0], 
			[0, 0, 0, 0, 1/2*sin2y, 0, cosy, 0, -1*s3/2*sin2y, 0, 0, 0, 0, 0, 0, 0],
			[0, 0, 0, 0, 0, siny, 0, cosy, 0, 0, 0, 0, 0, 0, 0, 0], 
			[0, 0, 0, 0, s3/4*(1-cos2y), 0, s3/2*sin2y, 0, 1/4*(1+(3*cos2y)), 0, 0, 0, 0, 0, 0, 0],
			[0, 0, 0, 0, 0, 0, 0, 0, 0, 1/16*(15*cosy+cos3y), 0, -1*s6/16*(5*siny + sin3y), 0, s15/16*(cosy-cos3y), 0, -1*s10/16*(3*siny-sin3y)],
			[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1/8*(5+(3*sin2y)), 0, -1*s6/4*sin2y, 0, s15/8*(1-cos2y), 0]
			[0, 0, 0, 0, 0, 0, 0, 0, 0, s6/16*(5*siny+sin3y), 0, 1/8*((5*cosy)+(3*cos3y)), 0, -1*s10/16*(-1*siny+(3*sin3y)), 0, s15/8*(cosy-cos3y)],
			[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, s6/4*sin2y, 0, cos2y, 0, -1*s10/4*sin2y, 0],
			[0, 0, 0, 0, 0, 0, 0, 0, 0, s15/16*(cosy-cos3y), 0, s10/16*(-1*siny+(3*sin3y)), 0, 1/16*(cosy+(15*cos3y)), 0, -1*s6/16*(siny+(5*sin3y))],
			[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, s15/8*(1-cos2y), 0, s10/4*sin2y, 0, 1/8*(3+(5*cos2y)), 0],
			[0, 0, 0, 0, 0, 0, 0, 0, 0, s10/16*((3*siny)-sin3y), 0, s15/8*(cosy-cos3y), 0, s6/16*(siny+(5*sin3y)), 0, 1/8*((3*cosy)+(5*cos3y))]
			];
		rx = [[1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
			[0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0], 
			[0, 0, cosx, -1*sinx, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
			[0, 0, sinx, cosx, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0], 
			[0, 0, 0, 0, 1/4*(3+cos2x), 0, 0, 1/2*sin2x, -1*s3/4*(1-cos2x), 0, 0, 0, 0, 0, 0, 0],
			[0, 0, 0, 0, 0, cosx, -1*sinx, 0, 0, 0, 0, 0, 0, 0, 0, 0], 
			[0, 0, 0, 0, 0, sinx, cosx, 0, 0, 0, 0, 0, 0, 0, 0, 0],
			[0, 0, 0, 0, -1/2*sin2x, 0, 0, cos2x, -1*s3/2*sin2x, 0, 0, 0, 0, 0, 0, 0], 
			[0, 0, 0, 0, -1*s3/4*(1-cos2x), 0, 0, s3/2*sin2x, 1/4*(1+(3*cos2x)), 0, 0, 0, 0, 0, 0, 0],
			[0, 0, 0, 0, 0, 0, 0, 0, 0, 1/8*(5+(3*cos2x)), 0, 0, s6/4*sin2x, -1*s15/8*(1-cos2x), 0, 0],
			[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1/16*((15*cosx)+cos3x), -1*s6/16*((5*sinx)+sin3x), 0, 0, -1*s15/16*(cosx-cos3x), s10/16*((3*sinx)-sin3x)],
			[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, s6/16*((5*sinx)+sin3x), 1/8*((5*cosx)+(3*cos3x)), 0, 0, -1*s10/16*(sinx-(3*sin3x)), -1*s15*(cosx-cos3x)],
			[0, 0, 0, 0, 0, 0, 0, 0, 0, -1*s6/4*sin2x, 0, 0, cos2x, -1*s10/4*sin2x, 0, 0],
			[0, 0, 0, 0, 0, 0, 0, 0, 0, -1*s15/8*(1-cos2x), 0, 0, s10/4*sin2x, 1/8*(3+(5*cos2x)), 0, 0],
			[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1*s15/16*(cosx-cos3x), s10/16*(sinx-(3*sin3x)), 0, 0, 1/16*(cosx+(15*cos3x)), -1*s6/16*(sinx+(5*sin3x))],
			[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1*s10/16*((3*sinx)-sin3x), -1*s15/8*(cosx-cos3x), 0, 0, s6/16*(sinx+(5*sin3x)), 1/8*((3*cosx)+(5*cos3x))]
			];
		
		// the complete rotation matrix	
		^r = Matrix.mul(rz, Matrix.mul(ry, rx));
	}
}
