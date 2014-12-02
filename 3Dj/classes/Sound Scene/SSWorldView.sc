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
// SSWorldView.sc
// Based on RedUniverse quark (by redFrik)
//
// This class implements the drawing functionality that allows to visualize a SSWorld
//
////////////////////////////////////////////////////////////////////////////

SSWorldView {

	var <world;
	var drawFunc;
	var <>textScale=10;
	var <>writeText=true;
	var <>f=0.75; //focal point for perspective drawing

	*new{ |ssWorld|
		^super.new.init(ssWorld);
	}

	init { |ssWorld|
		world=ssWorld;
		// drawFunc
	}


	draw { // return a function to be evaluated inside window.userView.drawFunc

		^{
			/////////////////////////////////////////////////////////////////
			// draw static elements

			Pen.strokeColor=Color.blue;
			Pen.alpha=1;


			// draw world bounds
			// Rect.aboutPoint(center,half x distance, half y distance)
			// remember that coordinates are changed respect to draw view

			// draw sweet spot (on the floor)
			Pen.addArc(0@0,f*world.sweetSpotSize,0,2pi);
			Pen.stroke;

			//ceiling
			Pen.strokeColor=Color.black;
			Pen.addRect(Rect.aboutPoint(0@0,world.dim.y/2,world.dim.x/2));
			//floor
			Pen.addRect(Rect.aboutPoint(0@0,f*world.dim.y/2,f*world.dim.x/2));
			//wall lines
			Pen.line(Point(world.dim.y/2,world.dim.x/2),Point(f*world.dim.y/2,f*world.dim.x/2));
			Pen.line(Point(world.dim.y/2.neg,world.dim.x/2),Point(f*world.dim.y/2.neg,f*world.dim.x/2));
			Pen.line(Point(world.dim.y/2,world.dim.x/2.neg),Point(f*world.dim.y/2,f*world.dim.x/2.neg));
			Pen.line(Point(world.dim.y/2.neg,world.dim.x/2.neg),Point(f*world.dim.y/2.neg,f*world.dim.x/2.neg));
			Pen.stroke;




			/////////////////////////////////////////////////////////////////
			// draw dynamic elements

			//TODO: not create a new instance of SSObject for each draw! make a visual object instead

			if (world.objects.size > 0 ) {
				world.objects.do { |obj|

					if (obj.present)  {

						// mirror dimensions!!
						var x=(obj.loc.y).neg;
						var y=(obj.loc.x).neg;
						// 0 is the floor, and world.dim.z/2 is the ceiling
						var z=obj.loc.z.linlin(0,world.dim.z,world.dim.z,0); //invert min and max in the view

						var a,b;

						var newObj,newObj2;
						newObj=obj.copy.loc_(Cartesian(x,y,z)); //take care not to overwrite the actual object!!

						Pen.alpha=1;

						/////////////////////////////////////////////////////////////////
						// write names
						// we need extra (re)scaling because fond cannot be smaller than 1

						if (writeText==true) {

							newObj2=obj.copy.loc_(Cartesian(x*textScale,y*textScale,z*textScale)); //take care not to overwrite the actual object!!

							Pen.scale(1/textScale,1/textScale);
							Pen.stringAtPoint(newObj.name,Rect.aboutSSObject(newObj2,f:f).rightBottom,Font("Helvetica", 2.5),Color.red);
							Pen.scale(textScale,textScale);

						};


						/////////////////////////////////////////////////////////////////
						// draw objects and shadows

						switch (obj.shape)
						{\point} {
							Pen.strokeColor= Color.black;
							Pen.strokeOval(Rect.aboutSSObject(newObj,f:f));
							//draw shadow
							Pen.fillColor= Color.gray;
							Pen.alpha=0.5;
							Pen.fillOval(Rect.aboutSSObject(newObj.loc_(Cartesian(x,y,world.dim.z)),f:f));
						}

						{\ring} {
							var newZ= (world.dim.z-obj.loc.z/(world.dim.z)).linlin(0, 1, f, 1);
							var rho = Cartesian(x*newZ,y*newZ).rho;

							Pen.strokeColor= Color.black;
							Pen.addArc(0@0,rho*f,0,2pi);
							Pen.stroke;
							//draw shadow
							Pen.fillColor= Color.gray;
							Pen.alpha=0.5;
							Pen.addArc(0@0,f*obj.locSph.rho,0,2pi);
							Pen.stroke;
						}

						{\extended} {
							var newZ= (world.dim.z-obj.loc.z/(world.dim.z)).linlin(0, 1, f, 1);
							var rho = Cartesian(x*newZ,y*newZ).rho;
							var azi = Cartesian(x,y).asPolar.angle; // since we change x and y for the window, we make this shortcut
							var o1= obj.copy.locSph_(ele:obj.locSph.elevation+(obj.dElevation/2));
							var newZ1= (world.dim.z-o1.loc.z/(world.dim.z)).linlin(0, 1, f, 1);
							var rho1 = Cartesian(o1.loc.x*newZ1,o1.loc.y*newZ1).rho;

							//don't show negative elevations --> clip
							var o2= obj.copy.locSph_(ele:(obj.locSph.elevation-(obj.dElevation/2)).clip(0,pi/2));
							var newZ2= (world.dim.z-o2.loc.z/(world.dim.z)).linlin(0, 1, f, 1);
							var rho2 = Cartesian(o2.loc.x*newZ1,o2.loc.y*newZ1).rho;

							Pen.fillColor = Color.grey;
							Pen.strokeColor = Color.black;
							Pen.alpha=0.6;
							// Pen.addAnnularWedge(0@0, rho1*f, rho*f,azi-(obj.dAzimuth/2),obj.dAzimuth);
							Pen.addAnnularWedge(0@0, rho1*f, rho2*f,azi-(obj.dAzimuth/2),obj.dAzimuth);
							Pen.fillStroke;

							//draw shadow
							Pen.strokeColor= Color.gray;
							Pen.alpha=0.5;
							Pen.addArc(0@0,f*obj.locSph.rho,azi-(obj.dAzimuth/2),obj.dAzimuth);
							Pen.stroke;

						}

						{\meridian} {
							Pen.strokeColor= Color.black;
							Pen.alpha=1;
							Pen.line(0@0,f*(x@y));
							Pen.stroke;
						}

						/*
						Pen.strokeColor= Color.black;
						Pen.addArc(0@0,f*obj.locSph.rho,azi-(obj.dAzimuth/2),obj.dAzimuth);
						Pen.stroke;*/
						// }

						{ //default
						};

						/////////////////////////////////////////////////////////////////
						// draw line between the object and its shadow
						//TODO: change names!
						a=Rect.aboutSSObject(newObj.loc_(Cartesian(x,y,z)),f:f).center;
						b=Rect.aboutSSObject(newObj.loc_(Cartesian(x,y,world.dim.z)),f:f).center;
						Pen.line(a,b);
						Pen.alpha=0.1;
						Pen.stroke;
					}
				}
			};
		};

	}
}
