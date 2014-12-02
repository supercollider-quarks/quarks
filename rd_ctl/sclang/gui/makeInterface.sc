/*  makeInterface.sc - (c) rohan drape, 2004-2007 */

+ Controller {
	
	makeInterface {
		arg rows = 4, columns = 8, offset = 0,
		windowName = "c.Interface", colorChooser = nil;
		var n, width, height, rect, window, flow;
		n = rows * columns;
		width = columns * CtlView.width;
		height = rows * CtlView.height;
		rect = Rect(128, 128, width, height);
		window = GUI.window.new(windowName, rect, resizable:false);
		flow = FlowLayout.new(window.view.bounds,
			Point.new(0,0),
			Point.new(0,0););
		window.view.decorator = flow;
		rows.do({
			arg i;
			columns.do({
				arg j;
				var index;
				index = (i * columns) + j + offset;
				CtlView.new(ctl.at(index),
					window,
					if(colorChooser.isNil,
						{Color.white;},
						{colorChooser.value(index);}););
			});
			flow.nextLine;
		});
		window.front;
		^window;
	}
}
