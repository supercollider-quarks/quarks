SETOgui {
var tServer;
var <window, <view, <jack;
var <fillColors;

*new {|server|
	^super.new.init(server)
}

init {|server|
	tServer = server; 

	window = Window.new.front;
	view = UserView(window, window.view.bounds).background_(Color.white);
	fillColors = [
		Color(0.367, 0.713, 0.714, 0.5),
		Color(0.854, 0.765, 0.383, 0.5),
		Color(0.849, 0.880, 0.380, 0.5), 	
		Color(0.313, 0.311, 0.712, 0.5),
		Color(0.319, 0.406, 0.804, 0.5), 
		Color(0.710, 0.638, 0.600, 0.5), 
		Color(0.538, 0.018, 0.494, 0.5), 
		Color(0.890, 0.528, 0.314, 0.5), 
		Color(0.680, 0.311, 0.666, 0.5), 
		Color(0.740, 0.571, 0.765, 0.5), 
		Color(0.813, 0.525, 0.336, 0.5), 
		Color(0.815, 0.500, 0.458, 0.5) 
	];

	
	view.drawFunc = {|me|
		var strokeColor = Color.gray(0);
		var textColor = Color.gray(0.4);
		
		var extent = 40;
		Pen.strokeColor = Color.black;
		Pen.addRect(Rect(0, 0, me.bounds.width, me.bounds.height));
		Pen.stroke;
		tServer.visibleObjs.do{|obj|
			Pen.use{
				Pen.translate(
					obj.pos[0] * me.bounds.width, 
					obj.pos[1] * me.bounds.height
				);
				Pen.strokeColor = strokeColor;
				Pen.fillColor = fillColors.wrapAt(obj.classID);
				Pen.translate(extent*0.5, extent*0.5);
				Pen.rotate(obj.rotEuler[0], 0, 0);
				Pen.addRect(Rect(extent* -0.5, extent* -0.5, extent, extent));
				Pen.line(0@0, 0@(extent * 0.75));
				Pen.fillStroke;
			};
		};
	
		// text
		tServer.visibleObjs.do{|obj|
			Pen.use{
				Pen.translate(
					obj.pos[0] * me.bounds.width, 
					obj.pos[1] * me.bounds.height
				);
				Pen.color = textColor;
				Pen.stringAtPoint("% (%)".format(obj.classID, obj.id), (extent * 0.65)@(extent * 0.5));
			}
		};
	};
	jack = SkipJack({window.refresh}, 0.1, {window.isClosed});

}



}