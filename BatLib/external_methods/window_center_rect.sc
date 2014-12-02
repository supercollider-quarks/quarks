+ Window
{
	*centerRect
	{|argWidth = 500, argHeight = 500|
	
		^Rect
		(
			(Window.screenBounds.width / 2) - (argWidth / 2),
			(Window.screenBounds.height / 2) - (argHeight / 2),
			argWidth,
			argHeight
		);
	}
}