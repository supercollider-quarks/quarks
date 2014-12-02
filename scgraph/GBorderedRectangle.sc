GBorderedRectangle : UGen {
	*gr { 
		arg 
			w = 1, h = 1,
			border_thickness = 0.1,
			fill_color = [0, 0, 0, 0],
			border_color = [1, 1, 1, 1];

		^this.multiNew
		(
			'audio',
			w,
			h,
			border_thickness,
			fill_color[0],
			fill_color[1],
			fill_color[2],
			fill_color[3],
			border_color[0],
			border_color[1],
			border_color[2],
			border_color[3]
		);
	}
}

