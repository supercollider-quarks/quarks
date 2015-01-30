
//mouse position
(
w = Window().acceptsMouseOver_(true).front;

x = EventNetwork(ENDef({
	var pos = w.view.mousePosEnIn;
	pos.collect{ |p| putStrLn("mouse pos is: "++p) }.enOut;
}));

x.start
)

// is down
(
t = StaticText();
w = Window().layout_(HLayout(t)).front;

x = EventNetwork(ENDef({
	var pos = w.view.mouseIsDownEnIn;
	pos.collect{ |b| IO{ t.string_("mouse is down: "++b) } }.enOut;
}));

x.start
)

//track mouse position
(
w = UserView().minWidth_(500).minHeight_(500).acceptsMouseOver_(true).front;

x = EventNetwork(ENDef({
	var pos = w.mousePosEnIn;

	var cx = w.bounds.width/2;
	var cy = w.bounds.height/2;
	var c = Point(cx,cy);

	var drawing = pos.collect{ |p|
		var points = [Point(-50, 0),Point(0, 25),Point(0,-25)].collect{ |x|
			x.rotate( (c-p).angle ) + c
		};
		var shape1 = PenStepShape.polygon(points);
		var shape2 = PenWedge(p,10,0,2*pi);
		PenDrawing( [ PenDrawedShapes([shape1], \fill, Color.blue, Color.red),
			PenDrawedShapes([shape2], \fill, Color.green, Color.red)
		] )
	};

	drawing.collect(w.setDrawing(_)).enOut
}));

x.start
)

//stamp circles

(
w = UserView().minWidth_(500).minHeight_(500).acceptsMouseOver_(true).front;

x = EventNetwork(ENDef({
	var pos = w.mousePosEnIn;
	var clicks = w.mouseClicksEnInES;

	var accumPos = pos.sampleOn(clicks).inject([], { |state,x| state.addI(T(x,Color.rand)) });

	accumPos.collect{ |positions|
		 var drawings = positions.collect{ |tup|
			PenDrawedShapes([PenWedge(tup.at1,10,0,2*pi)], \fill, tup.at2, Color.red)
		};
		var final = PenDrawing( drawings );
		w.setDrawing(final)
	}.enOut

}));

x.start
)


//keyboard

//keysDown
(
t = StaticText();
w = Window().layout_(HLayout(t)).front;

x = EventNetwork(ENDef({
	var pos = w.view.keysDownEnIn;
	pos.collect{ |b| IO{ t.string_("keys down: "++b) } }.enOut;
}));

x.start
)

//keyDown
(
t = StaticText();
w = Window().layout_(HLayout(t)).front;

x = EventNetwork(ENDef({
	var pos = w.view.keyDownEnInES;
	pos.collect{ |b| IO{ t.string_("key down: "++b) } }.enOut;
}));

x.start
)

