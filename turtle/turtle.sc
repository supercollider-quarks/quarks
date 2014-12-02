Turtle {

	var rad;
	var isDraw;
	var <points;
	
	
	*new {
		^super.new.init;	
	}
	init{
		points = [(0@0)];
		rad = 0;
		isDraw = [false];
	}
	
	to {arg point;
		points = points.add(point);
		isDraw = isDraw.add(isDraw.last)
	}
	turnTo{|argRad|
		rad = argRad;
	}
	turn{|relRad|
	
		rad = (rad+relRad)% 2pi;
	}
	forward{|lenght|
		var actPoint, newPoint;
		
		actPoint = points.last;
		newPoint = (cos(rad)*lenght)@(sin(rad)*lenght) + actPoint;
		
		points = points.add(newPoint);
		isDraw = isDraw.add(isDraw.last);
	}
	penUp{
		points = points.add(points.last);
		isDraw = isDraw.add(false);
	}
	penDown{
		points = points.add(points.last);
		isDraw = isDraw.add(true);
	}
	
	stroke {
		points.do{|point, i|
			if(isDraw[i], {
				Pen.lineTo(point);
			}, {
				Pen.moveTo(point);
			})
		};
		Pen.stroke;
	}
	fill {
		points.do{|point, i|
			if(isDraw[i], {
				Pen.lineTo(point);
			}, {
				Pen.moveTo(point);
			})
		};
		Pen.fill;
	}

}