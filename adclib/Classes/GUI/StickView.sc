/*

design by Dominik Hildebrand,
conversion to class by adc

	// tests
w = Window("Stick").front;
z = StickView(w, Rect(200,100,100,100));

z.x = -1; z.refresh;
z.x =  1; z.refresh;
z.x =  0; z.refresh;
z.y = -1; z.refresh;
z.y =  1; z.refresh;
z.y =  0; z.refresh;
z.hatval =  1; z.refresh;
z.hatval =  0; z.refresh;

z.rho;
z.point;
z.x;
z.y;
z.hatval;

*/

StickView {
	var <uview, <point, <>hatval = 0;

	*new { |parent, bounds|
		^super.new.init(parent, bounds);
	}

	point_ { |pt| point = pt; }
	x 		{ ^point.x }
	x_ 		{ |val| point.x = val }

	y 		{ ^point.y }
	y_		{ |val| point.y = val }

	polar 	{ ^point.asPolar }
	rho 		{ ^point.rho }
	angle 	{ ^point.angle }

		// interface ... hmm, point is [-1, 1]!
	value 	{ ^[point.x, point.y, hatval] }
	value_ 	{ |val|
		point.x = val[0];
		point.y = val[1];
		hatval = val[2];
	}

	init { |parent, bounds|

		var midGrey = Color.grey(0.5,0.5);
		var black = Color.grey(0);
		var blackB = Color.grey(0, 0.6);
		var onColor = Color.grey(1.0, 0.6);

		var fieldSize = bounds.width;
		var halfWidth = fieldSize * 0.5;
		var headRadius = fieldSize * 0.3;

		point = Point(0, 0);

		uview = UserView.new(parent, bounds);

		uview.mouseUpAction = { point.x = 0; point.y = 0; };

		uview.mouseMoveAction = { |view, x, y, mod|
			point.x = x / halfWidth - 1;
			point.y = y / halfWidth - 1;
			hatval = if (mod.isAlt, 1, 0);
			uview.refresh;

		};

		uview.drawFunc = {
			var angle = point.angle;
			var rho = point.rho;

			var stAngle = angle + 0.5pi;
			var bentRho = ((1-(0.1 ** rho.min(1))) * 1.1); //curve
			var rhoToLength = bentRho * ((headRadius * 0.6) - halfWidth);

			Pen.push;

			Pen.translate(halfWidth, halfWidth);

			// background circle
			Pen.strokeColor = midGrey;
			Pen.width = fieldSize * 0.15;
			Pen.addOval(Rect.aboutPoint(Point(0,0), rhoToLength*1.3, rhoToLength*1.3));
			Pen.stroke;

			//stickhead, stick and frame
			Pen.width = halfWidth * 0.025;
			Pen.push;
			Pen.fillColor = black;
			Pen.strokeColor = blackB;
			Pen.addRect(Rect.aboutPoint(Point(0,0),fieldSize*0.5,fieldSize*0.5));
			Pen.stroke;

			Pen.addOval(Rect.aboutPoint(Point(0,0),fieldSize*0.1,fieldSize*0.1));
			Pen.rotate(stAngle);
			Pen.addRect(Rect.aboutPoint([0,rhoToLength/2].asPoint, fieldSize* 0.06, fieldSize * 0.1));
			Pen.translate(0,rhoToLength);

			Pen.fillColor = [blackB, onColor][hatval];
				// this is the head
			Pen.addOval(
				Rect.aboutPoint(Point(0,0),
					headRadius-(bentRho*headRadius*0.05),
					headRadius-(rho.min(1)*headRadius*0.4))
			);
			Pen.fill;
			Pen.pop;
			Pen.pop;
		};

		uview.refresh;
	}

	refresh { uview.refresh; }
}
