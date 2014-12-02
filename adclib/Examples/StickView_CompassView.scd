/* 	CompassView 	-- a view for GamePad compasses.

	// tests
w = Window("Compass").front;
z = CompassView(w.view, Rect( 100, 100,100,100));
z.action = { |comp| comp.dump };
z.nameView.string = "test";
z.value = 0;
z.value = rrand(1, 8);
z.valueAction = 0;
z.valueAction = rrand(1, 8).postln;

y = StickView(w.view, Rect( 50, 100,100,100));
y.value
y.x_(0.3).refresh;
y.y_(0.5).refresh;
y.value

y.hatval_(1).refresh;

*/