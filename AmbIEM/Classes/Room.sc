/*
	Filename: Room.sc 
	created: March 2005 

	Copyright (C) IEM 2005, Alberto de Campo [decampo@iem.at] 

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

/* 	NOTE: 
	the coordinate system is given according to the listener's head:
	x-axis (nose), y-axis (left-ear) and z-axis (vertex)
	
	* left-deep-corner			  *
		       x				  |
		       |	       		  |
		      / \		  		  depth
		y<---| z |			  |
		     ----				  |
							  |
	*---------- width ------------* (origin) 

	Rooms are defined by the origin and width/depth/height
*/


/* 	Class: Room2D
	helper class to calc the early reflections in a 2D shoe-box room
*/
Room2D { 
	
	// The room as [x, y, depth, width]
	var <>room; 
	
	// class method new with initialisation of the room variable
	*new { arg room;
		^super.new.room_(Array.with(0, 0, 10, 5));
	}
	
	/* 	method: refs8
		Calculates the first 8 relfections of a given source position in the room
		Prameters: 
			px The source position (x)
			py The source position (y)
		Result:
			Array with 16 values as (x,y) for each of the 8 reflections
	*/
	refs8 { arg px, py;
		var y1, y2, x1, x2; 
		
		x1 = room[0] + (2 * room[2]) - px; 
		x2 = room[0] - px; 
		y1 = room[1] + (2 * room[3]) - py;
		y2 = room[1] - py;	

		^[	x1, py,  		// walls
			x2, py, 
			px, y1, 
			px, y2, 
			x1, y1, 	// 2d corners
			x1, y2, 
			x2, y1, 
			x2, y2
		]			
	}
	/* 	method: refs8polar
		Calculates the polar coordinates of the mirror sources 
		(phi, distance) with the listener position as reference
		Parameters: 
			px, py The source position 
			lx, ly The listener position 
		Result: 
			Array with 16 values as (phi, distance) for the 8 relflections 
	*/
	refs8polar { arg px, py, lx, ly;
		
		^this.refs8(px, py).clump(2).collect( { | ref | 
			[ atan2(ref[1]-ly, ref[0]-lx), hypot(ref[1]-ly, ref[0]-lx) ];
		} ).flatten(1);
	}	
}	

/* 	Class: Room3D
	helper class to calc the early reflections in a 3D shoe-box room
*/
Room3D : Room2D {
  	
  	// NOTE: room is here [x, y, z, depth, width, height]
  	
  	// class method new with initialisation of the room variable
	*new { arg room;
		^super.new.room_(Array.with(0, 0, 0, 10, 5, 5));
	}
  	
	/* 	method: refs10
		Calculates the first 10 relfections of a given source position in the room
		(4 x wall, 4 x corners and the first reflection at the floor and ceiling)
		Prameters: 
			px The source position (x)
			py The source position (y)
			pz The source position (z)
		Result:
			Array with 30 values as (x,y,z) for each of the 10 reflections
	*/
	refs10 { arg px, py, pz;

		var y1, y2, x1, x2; 
		
		x1 = room[0] + (2 * room[3]) - px; 
		x2 = room[0] - px; 
		y1 = room[1] + (2 * room[4]) - py;
		y2 = room[1] - py;	

		^[	x1, py, pz, 		// walls
			x2, py, pz, 
			px, y1, pz, 
			px, y2, pz, 
			x1, y1, pz, 	// 2d corners
			x1, y2, pz, 
			x2, y1, pz, 
			x2, y2, pz,
			px, py, (2*room[5]) - pz,
			px, py, pz.neg
		]			
	}	
	/* 	method: refs10polar
		Calculates the polar coordinates of the mirror sources 
		(phi, theta, distance) with the listener position as reference
		Parameters: 
			px, py, pz The source position 
			lx, ly, lz The listener position 
		Result: 
			Array with 30 values as (phi, theta, distance) for the 10 relflections 
	*/
	refs10polar{ arg px, py, pz, lx, ly, lz;
		
		^this.refs10(px, py, pz).clump(3).collect( { | ref | 
			var planeDist, phi, theta; 
			planeDist = hypot(ref[1]-ly, ref[0]-lx);
			phi = atan2(ly-ref[1], ref[0]-lx); 
			theta = atan2(ref[2]-lz, planeDist);
			[phi, theta, hypot(planeDist, ref[2]-lz)];
		}).flatten(1);
	}

}