+ Pen {
	*splineCurve { arg point1, point2, cpoint1, cpoint2, div = 10;
		(0,(1/div)..1).copy.do({ |i|
			this.lineTo(
				([point1.x, point2.x].splineIntFunction( i, cpoint1.x, cpoint2.x ))@
				([point1.y, point2.y].splineIntFunction( i, cpoint1.y, cpoint2.y ))
				);
			})
		}
	}
		