XiiSlicerMouseable{
	classvar <>win;
	//var <>xiigui, <>win;
	var color, <bounds;
}


// XiiSlicer classes

XiiSlicerBase {
	classvar <>win; //, >canvasBounds;
	var <color, <>bounds;
	//var >label, >lowGraphicsMode;
	//var hwidth, marqueeColor;
	/*
	*new { arg bounds=bounds(10,10,20,20), color = Color(0.5, 0.5, 0.5, 1);
		^super.new.init(bounds, color) 
	}

	init{ arg r, c;
		color = c;
		bounds = r;
		marqueeColor = Color(0.3, 0.3, 0.3, 1);
		hwidth = bounds.width * 0.5; // not to calculate all the time
		lowGraphicsMode = 0;
	}*/
}




XiiSlicerDisplay {
	classvar <>win, >canvasBounds, >canvas, >selectionDelta, <>test;
	//var <>xiigui, <>win;
	var <>color, <>bounds, <>drawbounds, marqueeColor;
	var >label, >playhead, hwidth, >lowGraphicsMode;//, >visible;

	*new { arg bounds = bounds(10,10,14,14), color = Color(0.5, 0.5, 0.5, 1);
		^super.new.init(bounds,color) 
	}

	init { arg r, c;
		drawbounds = r.copy; //init to same value
		bounds = r.copy; // marquee bounds
		color = c;
		marqueeColor = Color(0.5, 0.5, 0.5, 1); 
		hwidth = bounds.width * 0.5;
		playhead = bounds.left;
		selectionDelta = 1; // global limits of the selected buffer
		lowGraphicsMode = 0;
	}
	
	setplayhead { arg pos; // range 0 to 1
		// this needs to be adapted to different lengths of the sample when selection is set
		playhead =  (pos / selectionDelta) * bounds.width; // *bufferGlobalLimits[0]
	}

	drawplayhead {
		Pen.line( Point(playhead, bounds.top+1), Point(playhead, bounds.bottom) );
	}

	updateLabel {
		label.bounds = Rect(canvasBounds.left+2, bounds.bottom-10, 100, 15) ;//bounds + Rect(canvasBounds.left+12, label.bounds.height+20, 0,0) ;
	}

	draw {
		Pen.color = color;
		Pen.fillRect( drawbounds );
		// if not lowgraphics mode
		if (lowGraphicsMode == 0, {
			Pen.color = color.blend(Color.black, 0.5); //same but half darker suggested by thor
			Pen.strokeRect( drawbounds ); // outline marquee
		});		
	}

	mouseDown { arg x,y;

	}

	mouseUp { arg x,y;

	}

	rightMouseDown { 
		if (color.alpha == 1, { color.alpha = 0.3 }, { color.alpha = 1});
	}

	mouseMoved { arg x,y;
	
	}
}






XiiSlicerRect {
	classvar <>win, >canvasBounds, >players;

	var <>color, <>bounds, <hwidth, <>loc; 
	var >label, >visible, >lowGraphicsMode;
	var offset, marqueeColor, index;

	*new { arg bounds=bounds(10,10,20,20), color = Color(0.5, 0.5, 0.5, 1);
		^super.new.init(bounds, color) 
	}

	init{ arg r, c;
		color = c;
		bounds = r;
		marqueeColor = Color(0.3, 0.3, 0.3, 1); 
		offset = [0, 0];
		hwidth = bounds.width * 0.5; // not to calculate all the time
		loc = Point( bounds.left + hwidth, bounds.top + hwidth);
		lowGraphicsMode = 0;
	}

	randLoc // .5 to avoid antialiasing
	{
		bounds = Rect(10.5+((canvasBounds.width-20).rand.round), 10.5+((canvasBounds.height-20).rand.round), bounds.width, bounds.height);
		this.updateLoc;	
	}

	updateLoc {
		loc.x = bounds.left + hwidth;
		loc.y = bounds.top + hwidth;
	}

	draw {
		Pen.color = color;
		Pen.fillRect( bounds );
		if ( (lowGraphicsMode == 0) || (marqueeColor.red > 0.99) , {
			Pen.color = marqueeColor;		
			Pen.strokeRect( Rect(bounds.left+0.5, bounds.top+0.5, bounds.width, bounds.height) );
		});	
	}
	
	mouseDown { arg x,y;
		offset = [ bounds.left - x, bounds.top - y ];
	}

	mouseUp {
		offset = [0,0];	
	}

	mouseMoved { arg x,y;
		if (canvasBounds.contains(Point(x+canvasBounds.left, y+canvasBounds.top)) == true, {
			bounds.left = x + offset[0]; //move
			bounds.top = y + offset[1];	
			this.updateLoc;
		});
	}

	rightMouseDown { arg x, y;

	}

	//getPos {
	//	^bounds;
	//}
}





XiiSlicerBox : XiiSlicerRect {
	//classvar >gNumLayers; //>gAmp, 
	var index, relAmpStored; //, >selected;

	/*
	*new { arg i, bounds=bounds(10,10,20,20), color = Color(0.5, 0.5, 0.5, 1);
		^super.new.init(i, bounds, color) 
	}
	*/
	/*init{ arg i, r, c;
		super.init(i, r,c);
		selected = false
	}*/
	
	/*draw {
		super.draw;
		if (selected = true, {
			Pen.color = Color.red; 		
			Pen.strokeRect( bounds );
		});
	}*/
	mouseDown { arg x,y;
		super.mouseDown(x,y);
		this.select;		
	}

	select {
		marqueeColor = Color(1, 0, 0, 1); 
	}
	
	deselect {
		marqueeColor = Color(0.3, 0.3, 0.3, 1); 
	}

	randLoc	{
		super.randLoc;
		this.updateLabel;
	}

	updateLabel
	{
		label.bounds = bounds + Rect(canvasBounds.left+14, 2+9, 0,0) ;
	}
	
	mouseMoved { arg x,y;
		super.mouseMoved(x,y);
		this.updateLabel;
		this.updateSynths(relAmpStored);
	}

	updateSynths { arg relAmp=0; 
		//var spec, am;
		//spec = ControlSpec(0, 1/gNumLayers, \amp, 0, 0); // min, max, mapping, step
		//am = (((canvasBounds.height-loc.y)/canvasBounds.height) * gAmp);// / gNumLayers;
		relAmpStored = relAmp; // we need it on mousedown
		try {
			//players[index].set(\amp, spec.map(am)); // 0 to 1 max
			players[index].set(\amp, ((canvasBounds.height-loc.y)/canvasBounds.height) * relAmp);			
			players[index].set(\pan, (loc.x/(canvasBounds.width*0.5))-1); // -1 to 1;			
		}; // { "error".postln };
	}

	rightMouseDown { arg x, y;
		if (color.alpha == 1, { color.alpha = 0.3 }, { color.alpha = 1 });
		//players[index].set(\mute, color.alpha.round); // round to 0 or 1
	}

	setIndex { arg i;
		index = i;
	}
}




//XiiSlicerNode : XiiSlicerRect {

	/*init{ arg x, y, c;
		super.init(x,y,c); 
		bounds.width = 15;
		bounds.height = 15; 
	}*/
	/*
	draw { // overwrite superclass
		Pen.color = color;
		Pen.fillRect( bounds );
	}
	*/
	//mouseMoved { arg x,y;
	//	super.mouseMoved(x,y);
	//}
//}




XiiSelection {
	var selectables, selected, winRect;
	var color, bgcolor, visible, bounds, >lowGraphicsMode;

	*new { arg boxes, rect;
		^super.new.init(boxes, rect) 
	}

	init { arg boxes, rect;
		winRect = rect;
		selectables = boxes;
		selected = Array.new; //fill(8, {0}); // 8 is max num of layers
		color = Color(0.9, 0, 0, 1);
		bgcolor = Color(0, 0, 0, 0.6);
		visible = false;
		bounds = Rect(0, 0, 0, 0);
	}
	
	start { arg x,y;
		selected.do({ arg s; try{ s.deselect } });
		selected = Array.fill(8, {0}); //wipe
		bounds.origin = Point(x,y);
		visible = 1;
	}

	select {
		visible = 0;
		//this.doSelection;
		selectables.do({ arg s;
			if ( bounds.contains(s.loc) == 1, {
				selected.add(s);		// pass as ref??	
				s.select;
			});
		});
		bounds = Rect(0, 0, 0, 0); //avoid flashings ??
	}

	draw {
		if ( visible == 1, {
			bounds.right = MouseX.kr(0, winRect.width, 0);
			bounds.bottom = MouseY(0, winRect.height, 0);
			Pen.color = color;
			//Pen.fillRect( bounds );
			/*if (lowGraphicsMode == 0, {
				Pen.color = bgcolor; //marqueeColor;		
				Pen.strokeRect( Rect(bounds.left+0.5, bounds.top+0.5, bounds.width+0.5, bounds.height+0.5) );
			});*/
		});
	}

	doSelection { // Stop
		// selected = Array.fill(8, {0}); //wipe
		// check for intersections between selection and boxes in array
		/*selectables.size.do({ arg i;
			if ( bounds.contains(selectables[i].loc) == 1, {
				selected[i] == selectables[i];		// pass as ref??	
				selected[i].select;
			});
		});*/
		selectables.do({ arg s;
			if ( bounds.contains(s.loc) == 1, {
				selected.add(s);		// pass as ref??	
				s.select;
			});
		});
	}
}

