// this file is part of redUniverse /redFrik


RedIFS {
	var <>drawFunc, <>matrices, <>n, <>skip,
		matrices2, weights;
	*new {|drawFunc, matrices, n= 10000, skip= 100|
		^super.newCopyArgs(drawFunc, matrices, n, skip);
	}
	draw {|width= 400, height= 400|
		matrices2= matrices.collect{|x| x*[1, 1, 1, 1, width, height]};
		weights= matrices.collect{|x| ((x[0]*x[3])-(x[1]*x[2])).max(0.01)}.normalizeSum;
		drawFunc= drawFunc ? {|point| Pen.line(point, point+1)};
		this.prDraw(Point(width.rand, height.rand));
	}
	makeWindow {|bounds|
		var b= bounds ?? {Rect(100, 200, 400, 400)};
		var win= Window(this.class.name, b, false);
		var usr= UserView(win, Rect(0, 0, b.width, b.height));
		usr.drawFunc= {
			Pen.translate(b.width*0.5, b.height*0.5);
			Pen.moveTo(Point(0, 0));
			this.draw(b.width, b.height);
			Pen.stroke;
		};
		win.front;
		^win;
	}
	
	//--private
	prDraw {|point|
		skip.do{
			point= this.prTransform(matrices2.wchoose(weights), point);
		};
		n.do{
			point= this.prTransform(matrices2.wchoose(weights), point);
			drawFunc.value(point);
		};
	}
	prTransform {|matrix, point|
		^Point(
			(matrix[0]*point.x)+(matrix[1]*point.y)+matrix[4],
			(matrix[2]*point.x)+(matrix[3]*point.y)+matrix[5]
		)
	}
}