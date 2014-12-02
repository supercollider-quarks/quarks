
XiiToshioNode {
	classvar <>selectednode;
	var point, <>dir, rowcol;
	var <>arrow;
	var rot, <>withagent;
	var selected, auto, autoclock, fillcolor, impregnated;
	 // energy = count | resetAllFlag = resetting energy | eThreshold = times before changing
	var generative, energy, energyThreshold;
	var toshioArray;
	// var resetAllFlag;
	//var buffer;
	
	*new {arg point, x, y ;
		^super.new.initXiiToshio(point, x, y);
	}

	initXiiToshio {arg apoint, x, y;
		point = apoint;
		rowcol = [x, y];
		dir = this.getNewDirection;
		arrow = true;
		rot = (0.25pi * dir)-0.5pi;
		withagent = false;
		generative = false;
		energy = 0;
		energyThreshold = 5;
		//resetAllFlag = false;
		selected = false;
		auto = false;
		impregnated = false; // a variable denoting whether the note has an assigned sound
		fillcolor = Color.white; // the color of impregnation
	}
	
	draw {
		GUI.pen.use({
			GUI.pen.width = 0.8;
			
			GUI.pen.translate(point.x, point.y);
			GUI.pen.moveTo(0@0);

			if(selected, {
				GUI.pen.strokeColor = Color.black;
				GUI.pen.strokeOval(Rect(-15.5, -15.5, 31, 31));
			});
					
			GUI.pen.rotate(rot);
			GUI.pen.strokeColor = Color.black;
			if(arrow, {
				//GUI.pen.line(0@0, Point(40, 0));
				GUI.pen.line(Point(25, 0), Point(7, 6));
				GUI.pen.line(Point(25, 0), Point(7, -6));
			});
			GUI.pen.stroke;
						
			//GUI.pen.color = Color.white;
			//GUI.pen.fillOval(Rect(-10, -10, 20, 20));
			GUI.pen.fillColor = XiiColors.darkgreen.alpha_(energy/20);
			GUI.pen.fillOval(Rect(-10, -10, 20, 20));

			GUI.pen.strokeColor = Color.black;
			GUI.pen.strokeOval(Rect(-10, -10, 20, 20));
			
			if(withagent, { 
				GUI.pen.color = XiiColors.darkgreen;
				GUI.pen.fillOval(Rect(-6, -6, 12, 12)); 
			}, {
				GUI.pen.fillColor = fillcolor;
				GUI.pen.fillOval(Rect(-6, -6, 12, 12));
				GUI.pen.strokeColor = Color.black;
				GUI.pen.strokeOval(Rect(-6, -6, 12, 12));			});
		});
	}
	
	rotateToshio {arg mx, my, direction=1;
		if(arrow, { // if visible
			if(Rect(point.x-10, point.y-10, 20, 20).intersects(Rect(mx, my, 1, 1)), {
				//"mousedown on ".post; rowcol.postln;
				if(selected, {
					rot = rot + (0.25pi * direction);
					//[\direction, direction].postln;
					dir = (dir + direction) % 8;  
					//[\dir, dir].postln;
				}, {	
					selected = true;
					selectednode = rowcol;
				});
				//[\rot, rot].postln;
				^true;
			}, {
				selected = false;
				^false;
			});
		}, {
			^false;
		});
	}
	
	
	arrowHandler {arg mx, my;
	
		if(Rect(point.x-10, point.y-10, 20, 20).intersects(Rect(mx, my, 1, 1)), {
			//"make arrow on ".post; rowcol.postln;
			arrow = arrow.not;
		})
	}
	
	setArrow_ {arg bool;
		arrow = bool;
	}
	
	getDirection {
		^dir;
	}
		
	setWithAgent_ {arg bool;
		withagent = bool;
		if(withagent == true, { // if there is an agent in the node (not setting off)
			if(generative, { // if not deterministic - change of direction 
				energy = energy + 1;
				if(energy > energyThreshold, {
					dir = this.getNewDirection;
					rot = (0.25pi * dir)-0.5pi;
					energy = 0;
					/*
					if(resetAllFlag, {
						toshioArray.do({arg row; 
							row.do({arg toshio; toshio.resetEnergy}); 
						});
					});
					*/
				})
			});
			//"------ rowcol".post; rowcol.postln;
			//"pregnated ===========".post; impregnated.postln;
			/*
			if(impregnated, { // play sound
				Synth(\xiiString, [\freq, 200+(1000.rand)]);
			});
			*/
		});
	}
	
	setSound_ {arg bool;
		impregnated = bool;
		if(impregnated, {fillcolor = XiiColors.ixiorange.alpha_(0.4)}, {fillcolor = Color.white});
		//buffer = buffernum;
	}
	
	updateStatus {
		if(rowcol==selectednode, {selected = true}, {selected=false});
	}
	
	setSelected_ {arg bool;
		selected = bool;
	}
	
	resetEnergy {
		energy = 0;
	}
	
	/*
	setResetAllFlag_ {arg bool;
		resetAllFlag = bool;
	}
	*/
	
	setGenerative_ {arg gen;
		generative = gen.booleanValue;
	}
	
	setEnergyThreshold_ {arg thold;
		energyThreshold = thold;
	}
	
	reOrganise {
		dir = this.getNewDirection;
		rot = (0.25pi * dir)-0.5pi;
		energy = 0;
	}
	
	supplyToshioArray { arg toshioarr;
		toshioArray = toshioarr;
	}
	
	stopAutomation {
		try{ autoclock.stop };
	}
	
	automate { arg view;
		// "----------------".post; rowcol.postln;
		auto = auto.not;
		if(auto, {
			autoclock = TempoClock.new(1, 0, Main.elapsedTime.ceil)
						.schedAbs(0, { arg beat, sec;
							this.rotate(1);
							{view.refresh}.defer;
							1
						});
		},{
			autoclock.stop;
		});
	}

	rotate {arg rotatedir=1;
		rot = rot + (0.25pi * rotatedir);
		dir = (dir + rotatedir) % 8;
	}
	
	setDirection_ {arg direction;
		dir = direction; 
		rot = (0.25pi * dir)-0.5pi;
	}


	getNewDirection {
		var x, y;
		x = rowcol[0];
		y = rowcol[1];
		
		^if(x==0, {
			if(y==0, {
				2+(3.rand);
			}, {
				if(y==9, {
					3.rand;
				}, {
					4.rand;
				});
			});
		}, {
			if(x==9, {
				if(y==0, {
					4+(3.rand);
				}, {
					if(y==9, {
						(6+(3.rand))%8;
					}, {
						4+(4.rand);
					});
				});
			}, {
				if(y==0, {
					2+(4.rand);
				}, {
					if(y==9, {
						(6+(4.rand))%8;
					}, {
						8.rand;
					});
				});
			});
		});
	}
}
