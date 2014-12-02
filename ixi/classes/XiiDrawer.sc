// used in teaching to draw stuff such as waves 

XiiDrawer {	

	*new {
		^super.new.initDrawer;
		}
		
	initDrawer {
		var w, txt, tmppoints, all, userview;
		tmppoints = [];
		
		w = Window("ixi drawer", Rect(128, 64, 540, 460));
	
		userview = UserView(w,w.view.bounds)
			.mouseMoveAction_({|v,x,y|
				tmppoints = tmppoints.add(Point(x,y));
				v.refresh;
		})
			.mouseUpAction_({|v,x,y|
				all = all.add(tmppoints.copy);
				tmppoints = [];
				v.refresh;
		})
			.drawFunc_{|me|
				Pen.use {	
					//Color.white.set;
					Pen.color = Color.white;
					Pen.fillRect(me.bounds.moveTo(0,0));			
					Pen.width = 1;
					Pen.color = Color.black;					
					//Color.black.set;
		
					Pen.beginPath;
					
					tmppoints.do{	|p, i|
						if(i == 0){
						Pen.moveTo(p);
						}{
						Pen.lineTo(p);
						}
					};
					all.do{|points|
						points.do{|p, i|
							if(i == 0){
								Pen.moveTo(p);
							}{
								Pen.lineTo(p);
							}
						};
					};
					Pen.stroke;
				};
			};	
		//userview.relativeOrigin = true;
			
		w.front;
	}
	
}
