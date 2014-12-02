TV : CV {

	
	*new { | v | ^super.new(NoSpec, v) }

	draw { |win, name =">"| 
		var tv = TextField(win, ~sliderRect.value)
			.font_(Font("Helvetica", 12) )
			.string_(value)
			; 
		this.connect(tv);
		^tv
	}
	
	connect { | view | ^TVSync(this, view) }
	
}

