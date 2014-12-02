+ Env {
	asPolyLine { |width = 1, height = 1|
		var points, lastTime = 0;
		points = [this.timeLine, levels.copy];
		
		points[0] = (points[0] / this.duration) * width;
		points[1] = points[1].normalize(height,0); // upside down		
		^PolyLine( *points.flop );
		}
	}