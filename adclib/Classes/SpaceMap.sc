/*
(
m = SpaceMap();
m.put(\adc);
m.put(\ha, 200@200);
m.envir;
g = SpaceMapGui(m);
)
*/

SpaceMap { 
	var <envir, <dim;
	*new { 
		^super.new.init;
	}	
	init { 
		envir = ();
		dim = 400@400;
	}
	
	put { |name, loc|
		envir[name] = envir[name] ?? { () };
		envir[name][\loc] = loc ?? { rand(dim.x) @  rand(dim.y) }
	}
	
	at { |name| ^envir[name]; }
	
	addTo { |name, key, thing|
		envir[name].put(key, thing);
	}	
}

SpaceMapGui : JITGui { 
	var <usr, <scaleX, <scaleY;
	var <locColor, <selColor;
	var <isDrawing = false, <drawStart, <lines;
	var <>drawLines = true;
	var <drawSettings;
	var <selectedLoc;

		// these methods should be overridden in subclasses: 
	setDefaults { |options|
		if (parent.isNil) { 
			defPos = 10@10
		} { 
			defPos = skin.margin;
		};
		minSize = 410 @ (410 + (numItems * skin.buttonHeight + skin.headHeight));
	//	"minSize: %\n".postf(minSize);
	}
	
	makeViews { 
		drawSettings = (); 
		
		lines = List[];
		locColor = Color.white(0.9);
		selColor = Color.yellow(0.9);
		
		zone.resize_(5);
		usr = UserView(zone, zone.bounds.moveTo(0,0).insetBy(2,2)).resize_(5);
		usr.background = Color.grey(0.5);
		usr.animate_(true);
		
		usr.drawFunc = { |usr|
			if (object.notNil) { 

				Pen.fillColor = Color.green;
				Pen.stringAtPoint("Click a  name to move it to its location;", 10@10);
				Pen.stringAtPoint("alt-click to start drawing a line;", 10@30);
				Pen.stringAtPoint("type backspace to clear last line.\n", 10@50);
				
				Pen.font = Font("Verdana", 12);
				scaleX = usr.bounds.width / object.dim.x;
				scaleY = usr.bounds.height / object.dim.y;
				Pen.scale(scaleX, scaleY);
		
				object.envir.keysValuesDo { |key, dict| 
					var locrect = Rect.aboutPoint(dict.loc, 25, 25);
					Pen.addOval(locrect);
	
					Pen.font = Font("Verdana", 16);
					Pen.stringCenteredIn(key.asString, locrect);
					Pen.stroke;
				};
				
				if (drawLines) { 
					Pen.width = 1;
					Pen.fillColor = locColor;
					lines.do { |pair|
						Pen.line(pair[0], pair[1]);
						Pen.stroke;
					}
				};
			};
		};
		
		usr.mouseDownAction = { |view, mx, my, mod| 
			mx = mx / scaleX; my = my / scaleY;
			isDrawing = mod.isAlt;
			if (isDrawing) { 
				"start isDrawing".postln;
				drawStart = (mx@my).round(1@1).postln;
					// if close enough to an existing point, 
					// move that point;
					// if final point tooo close to line start, 
					// remove line.
				lines.add([drawStart, drawStart.copy]);
				selectedLoc = lines.last[1];
			} {
				selectedLoc =  object.envir.detect { |dict| dict.loc.dist(mx@my) < 25 };
				if (selectedLoc.isNil) { 
					selectedLoc = lines.flat.detect { |loc| loc.dist(mx@my) < 10 };
				};
			};
		};
		
		usr.mouseMoveAction = { |view, mx, my, mod|
			mx = mx / scaleX; my = my / scaleY;
			mx = mx.round(1); my = my.round(1); 
			if (isDrawing) { 
				lines.last.put(1, mx@my);
			};
			if (selectedLoc.notNil) { 
				if (selectedLoc.isKindOf(Point)) { 
					selectedLoc.x_(mx).y_(my);
				} { 
					selectedLoc.loc.x_(mx).y_(my);
				};
			}
		};
		
		usr.keyDownAction = { |view, key| 
			key.postcs;
			// 8.asAscii in Qt?? 
			if (key == 127.asAscii) { "delete last line.".postln; lines.pop } 
		}

	} 
	
	checkUpdate {
		
	}
}