////////////////////////////////////////////////////////////////////////////
//
// Copyright (C) Fundació Barcelona Media, October 2014 [www.barcelonamedia.org]
// Author: Andrés Pérez López [contact@andresperezlopez.com]
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; withot even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <http://www.gnu.org/licenses/>
//
////////////////////////////////////////////////////////////////////////////
//
// plusRect_SS.sc
// based on plusRect.sc from redUniverse quark
//
// Custom implementation for central focal point
//
////////////////////////////////////////////////////////////////////////////

+Rect {


	//!!!TODO. clean up focal and scaling
	*aboutSSVector {|vec, size, width, height, depth, s= 1, f= 0.75|
		var x, y, z, ox, oy;
		z= (depth-vec.z/(depth*s)).linlin(0, 1, f, 1);
		x= vec.x*z;
		y= vec.y*z;
		/*		ox= 1-z*(width*0.5)+x;
		oy= 1-z*(height*0.5)+y;*/
		^this.aboutPoint(Point(x, y), size*z.linlin(0, 1, 0.01, 1), size*z.linlin(0, 1, 0.01, 1))
	}

	*aboutSSObject {|obj, s= 1, f= 0.75|
		^this.aboutSSVector(
			obj.loc,
			obj.size,
			obj.world.dim.x,
			obj.world.dim.y,
			obj.world.dim.z,
			s,
			f
		)
	}
}
