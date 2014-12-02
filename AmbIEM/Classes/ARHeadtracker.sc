/*
	Filename: ARHeadtraker.sc 
	created: 14.9.2005 

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


/* 	Class: ARHeadtracker
	3DOF Headtracking with the ARHeadtracker commandline tool based on ARToolKit
*/
ARHeadtracker { 

	// the current values for the angles as NodeProxy
	var <>rotation;
	
	// the current values as array
	var <>rotationArray;
	
	// the zero position
	var <>zero;
	
	// the client we are using 
	var <>client;
	var <port = 57120;

	// creating a new instance and calling init for it
	*new { arg port=57120;
		^super.newCopyArgs(port).init;
	}

	// the init function 
	init { 
		var net;
		
		this.rotation = NodeProxy.control(Server.default, 3);
		this.rotation.source = { |e=0, a=0, r=0| [e, a, r] };
		this.zero = [0,0,0];
				
		net = NetAddr("127.0.0.1", this.port);
		this.client = LocalClient(\headtracker, nil);
		ClientFunc(\headtracker, { arg ... data;
			rotationArray = data;
			this.rotation.setn(\e, data-zero);
		});
		this.client.start;
	}

	// the access kr method
	kr {
		^[this.rotation.kr(1,0), this.rotation.kr(1,1), this.rotation.kr(1,2)];
	}
	
	// reset the position and set the zero position
	reset {
		this.zero = this.rotationArray;
	}
}