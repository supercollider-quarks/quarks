
XixiPredator {	
	var <>point, rect, destpoint, stageRect, preyArray, predatorArray;
	var prey;
	var fillcolor, strokecolor;
	
	var posNegX, posNegY;
	var xDistance, yDistance;
	var <>friction, restlessness, <>restlessSeed;
	var xAway, yAway;
	var chooseNewTargetCounter;
	var energy, biteFlag, aggr;	
	
	*new { | point, stageRect, preyArray | 
		^super.new.initXixiPredator(point, stageRect, preyArray);
	}
	
	initXixiPredator { |argpoint, argstageRect, argpreyArray|
		if(argstageRect == nil, {
			stageRect = argpoint;
			point = Point(stageRect.width.rand, stageRect.height.rand);
		},{
			stageRect = argstageRect;
			point = argpoint;
		});
		fillcolor = Color.white;
		strokecolor = Color.black;
		destpoint = Point(50.rand2, 50.rand2);
		preyArray = argpreyArray;
		xAway = 0; yAway = 0;
		friction = 17.9;
		chooseNewTargetCounter = 300+(300.rand);

		prey = preyArray.choose;
		restlessSeed = 20;
		restlessness = restlessSeed + (restlessSeed.rand);
		energy = 100; // 0 to 100
		biteFlag = false;
		aggr = 4;
	}
	
	supplyPredatorArray {|argpredatorArray| // from above level when all preds are initialized
		predatorArray = argpredatorArray;
	}

	supplyPreyArray {|argpreyArray| // when a new prey is created
		preyArray = argpreyArray;
		prey = preyArray.choose;
	}
	
	update {
		destpoint.x = prey.point.x + xAway;		destpoint.y = prey.point.y + yAway;		// move
    		if(point.x < destpoint.x, { 
    			posNegX = 1; 
    			xDistance = destpoint.x - point.x;
    		}, {
    			posNegX = -1; 
    			xDistance = point.x - destpoint.x;
    		});		if(point.y < destpoint.y, {
			posNegY = 1; 
			yDistance = destpoint.y - point.y;
		}, {
			posNegY = -1;
			yDistance = point.y - destpoint.y;
		});		point.x = point.x + (posNegX * (xDistance/friction));
		point.y = point.y + (posNegY * (yDistance/friction));

		// avoid each other		predatorArray.do({ |predator|			if(predator != this, {
        			if(this.distanceFrom(predator) < 16.0, {          			xAway = 1.rand2 * (30 + (30.rand));          			yAway = 1.rand2 * (30 + (30.rand));          			restlessness = restlessSeed + (restlessSeed.rand);
          			energy = energy - 10; // they loose energy by fighting
          		});
          	});
		});

		// attack the prey    				restlessness = restlessness + 1;
		energy = energy - 0.1; // loose energy by waiting
		if(restlessness > 100, {
			xAway=0; yAway=0; // we put the locus on the prey
          	restlessness = restlessSeed + (restlessSeed.rand);
		});
    		// chooses a new prey to attack		chooseNewTargetCounter = chooseNewTargetCounter - 1;		if(chooseNewTargetCounter < 0, {			chooseNewTargetCounter = 100+(200.rand);			prey = preyArray.choose;		});
		
		fillcolor = Color.new255(230-energy, 230, 230-energy);
	}
	
	draw {
		if(point.y-10 < stageRect.height, {
	    	     ^{ var rot;
				GUI.pen.use({
				
					if ( GUI.id == \cocoa, { Pen.setShadow(1@2, 10, Color.black) });
					GUI.pen.color = Color.black;
					GUI.pen.width = 1;
					GUI.pen.translate(point.x, point.y);
					GUI.pen.moveTo(0@0);
					
					rot = atan2(prey.point.y - point.y, prey.point.x - point.x);
					GUI.pen.rotate(rot);
	
			     	GUI.pen.line(0@0, 15@5);
			     	GUI.pen.line(0@0, 15@ -5);
			     	GUI.pen.stroke;
		
			     	GUI.pen.color = fillcolor;
			    		GUI.pen.fillOval(Rect(-10, -5, 20, 10));
			     	GUI.pen.color = Color.black;
			     	GUI.pen.strokeOval(Rect(-10, -5, 20, 10));
	     	
				});
		     }
	     });
	}

	moveAway {
		energy = 100;
         	xAway = 1.rand2 * (60 + (60.rand));
         	yAway = 1.rand2 * (60 + (60.rand));
          restlessness = restlessSeed + (restlessSeed.rand);
        	this.iAteFromPrey;
	}
	
	iAteFromPrey {
		if(biteFlag == false, {
			biteFlag = true;
			prey.predatorAteMe;
			AppClock.sched(aggr, { biteFlag = false; nil;});
		});
	}
	
	setAggression_{arg argaggr;
		aggr = argaggr;	
	}
	
	setLoc_ {|x, y|
		point = Point(x,y);
	}
	
	getLoc {
		^point;
	}
	
	mouseDown { |x, y, func|
		if(Rect(point.x-10, point.y-5, 20, 10).intersects(Rect(x, y, 1, 1)), {
			^true;
		}, {
			^false;
		});
	}
		
	mouseTrack { |x, y, func|
	}
		
	mouseUp { |x, y, func|
	}
	
	mouseOver { |x, y, func|
	}	
	
	distanceFrom { |other|
		^sqrt(([this.point.x, this.point.y] - [other.point.x, other.point.y]).squared.sum);
	}
}
