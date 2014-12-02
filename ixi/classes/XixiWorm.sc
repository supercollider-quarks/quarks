XixiWorm {

	var <>point, destpoint, size, rect, stageRect;
	var taillength;
	var <>tailArray;
	var move, oldpoint;
	var <>auto, <>boundaries;
	var wanderCircleDistance, wanderCircleRadius, wanderCircleAngle;
	var xOffset, yOffset;
	var <>tailcolor;
	var wormArray, <>playThreshholdFlag;
	var curverange, speed;
	var predatorArray;
	
  //	var spriteNum, boxArray;
	//	var <>r, <>g, <>b, fillcolor;

 // color white = color(255, 255, 255);
  //color dark = color(75, 75, 75);
  //color norm = color(r, g, b);
	
	
	*new { | point, size, taillength, stageRect| 
		^super.new.initXixiWorm(point, size, taillength, stageRect);
	}
	
	initXixiWorm { |argpoint, argsize, argtaillength, argstageRect |
		
		point = argpoint;
		size = argsize;
		taillength = argtaillength;
		stageRect = argstageRect;
		tailArray = Array.fill(taillength, {arg i; Point(point.x+(i*10), point.y)});
		move = false;
		oldpoint = point;
		
		wanderCircleDistance = 4; // I DONt THINK I'm USING THIS
		wanderCircleRadius = 4;
		wanderCircleAngle = 4;
		destpoint = Point(point.x+(20.rand2), point.y+(20.rand2));
		auto = false;
		boundaries = false;
		tailcolor = Color.new255(100+(50.rand),100+(80.rand),100+(50.rand));
		wormArray = [];
		playThreshholdFlag = true; // if true, it plays
		curverange = 4;
		speed = 2;
	}
	
	update {
		
			point.x = destpoint.x; // make the new point
			point.y = destpoint.y;

			if(point.x > stageRect.width, {point.x = 0});
			if(point.x < 0, {point.x = stageRect.width});
			if(point.y > stageRect.height, {point.y = 0});
			if(point.y < 0, {point.y = stageRect.height});
			
			xOffset = (wanderCircleRadius * cos(wanderCircleAngle * pi/90) ) * speed;
			yOffset = (wanderCircleRadius * sin(wanderCircleAngle * pi/90) ) * speed;
			
			destpoint.x = point.x + xOffset;
			destpoint.y = point.y + yOffset;
		
		if(0.5.coin, {
			wanderCircleAngle = wanderCircleAngle + (curverange.rand2);
		}, {
//			wormArray.do({arg worm;
//				
//				if(destpoint.distanceFrom(worm.point) < 10, {
//					wanderCircleAngle = wanderCircleAngle + (20.rand2);
//				});
//			});
		});
		//if((abs(point.x - oldpoint.x)> (size*2)) || (abs(point.y - oldpoint.y) > (size*2)), {
			tailArray = tailArray.addFirst(Point(point.x, point.y));
			tailArray.pop;
			//tailArray.postln;
			//oldpoint = point;
		//});
		
		/*
		// INTERSECTION AND PLAY STUFF
		if(playThreshholdFlag == true, {
			block{arg break;
				wormArray.do({arg worm, i;	
					if(worm != this, {
						tailArray.do({arg point;
							worm.tailArray.do({arg tailpoint;
								if(Rect(point.x, point.y, size, size).intersects(
									Rect(tailpoint.x, tailpoint.y, size, size)), {
										//"intersect".postln;
									//worm.setThreshholdFlag;
									worm.playThreshholdFlag = false;
									playThreshholdFlag = false;
									Synth(\sine, [\freq, 344+tailpoint.y, \amp, 0.2]);
									AppClock.sched(1, {
											//"setting TRUE".postln;
										playThreshholdFlag = true; nil;});
									break.value;
								});
							});
						});
					});
				});		
			}
		});
		*/	
	}
	
	draw{
		^{
			var point;
			point = tailArray[0];
//			Color.red.set;
//			Pen.fillOval(Rect(
//						(point.x).round(1)+0.5-(size/2), 
//						(point.y).round(1)+0.5-(size/2), size, size));
			Color.black.set;
//			Pen.strokeOval(Rect(
//						(point.x).round(1)+0.5-(size/2), 
//						(point.y).round(1)+0.5-(size/2), size, size));
			//(tailArray.size-1).do({arg i;
			tailArray.do({arg point;
			
				
				XiiColors.lightgreen.set;
				Pen.fillOval(Rect(
							(point.x).round(1)+0.5-(size/2), 
							(point.y).round(1)+0.5-(size/2), size, size));
				Color.black.set;
				
				Pen.strokeOval(Rect(
							(point.x).round(1)+0.5-(size/2), 
							(point.y).round(1)+0.5-(size/2), size, size));
			});
		}
	}
	
	supplyOtherWorms {arg worms;
		wormArray = worms;
	}
	
	supplyPredatorArray {|argpredatorArray| 
		predatorArray = argpredatorArray;
	}

	
	setCurves_{arg curve;
		curverange = curve;	
	}

	
	setSpeed_{arg aspeed;
		speed = aspeed;	
	}

	
	setLength_{arg arglength;
		if(arglength < tailArray.size, {
			tailArray = tailArray[0..(arglength-1).asInteger];
		},{
			tailArray = tailArray.add(tailArray.last);
		});
	}

	setThreshholdFlag {
		playThreshholdFlag = false;
		AppClock.sched(1, {playThreshholdFlag = true; nil;});
	}
	
	mouseDown { |x, y, func|
		[\px, point.x, \py, point.y, \size, size].postln;
		if(Rect(point.x, point.y, size, size).intersects(Rect(x, y, 1, 1)), {
			"move is true".postln;
			move = true;
			^true;
		},{
			^false;			
		});
	}
		
	mouseTrack { |x, y, func|
	
		if(move, { 
			point = Point(x, y);
			if((abs(point.x - oldpoint.x)> (size*2)) || (abs(point.y - oldpoint.y) > (size*2)), {
				tailArray = tailArray.addFirst(point);
				tailArray.pop;
				oldpoint = point;
			});
		});
	}
		
	mouseUp { |x, y, func|
		if(Rect(point.x, point.y, size, size).intersects(Rect(x,y,1, 1)), {
			"move is false".postln;
			move = false;
		});
	}
	
	mouseOver { |x, y, func|
		if(Rect(point.x, point.y, size, size).intersects(Rect(x,y,1, 1)), {
			"mouseover".postln;
		});
	}	

}
