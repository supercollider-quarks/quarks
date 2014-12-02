//simple plotting of functions

(
	var width= 100, height= 100, w,
		x, y, dx, dy, widthCartPlane, heightCartPlane, rad, theta, func, cnt= 0,
		scale= 2;
	
	w= RedWindow("func plot", Rect(128, 64, width*scale, height*scale)).front;
	
	widthCartPlane= 16;
	heightCartPlane= 16;
	
	//--loop
	w.draw{
		cnt= cnt+0.05%(width*scale);
		dx= widthCartPlane/(width*scale);
		dy= heightCartPlane/(height*scale);
		x= widthCartPlane.neg/2;
		(width*scale).do{|i|
			y= heightCartPlane.neg/2;
			(height*scale).do{|j|
				rad= x.sumsqr(y).sqrt;
				theta= atan2(y, x);
				
				//func= cos(cnt+rad);
				//func= sin(theta);
				//func= sin(rad);
				//func= cos(theta);
				func= sin(cnt*rad.cos+(5*theta));
				y= y+dy;
				
				//--render
				Pen.strokeColor= Color.grey(func+1);
				Pen.strokeRect(Rect(i, j, 1, 1));
			};
			x= x+dx;
		}
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isClosed.not, {w.close})});
)
