
+SequenceableCollection {
	multiplot { |labels,  name = "", bounds,  background, hue = 0, range = 0.75, saturation = 0.7, value = 0.99, drawLines = false |
		var win, view, colors, chans, max, min, valRange;
		bounds = bounds ? Rect(20, 0, 500, 500);
		background = background ? Color(0.1,0,0.1,1);
		max = this.collect(_.maxItem).maxItem;
		min = this.collect(_.minItem).minItem;
		valRange = max - min;
		[min,valRange].postln;
		chans = this.size;
		
		colors =( (0..chans - 1)/chans ).collect { | h | Color.hsv(h + hue * range mod: 1 ,saturation, value) };
		win = GUI.window.new(name ? "warps", Rect(20, 20, 500, 500));
		win.front;
		view = win.view;
		view.background_(background );
		this.do { | v, i  | 
			
			SCMultiSliderView(win, bounds) 
				.resize_(5)
				.readOnly_(true)
				.drawRects_(true)
				.valueThumbSize_(2)
				.indexThumbSize_( 2 ) 
				.elasticMode_(1)
				.background_(Color(0,0,0,0) ) 
				.strokeColor_(colors[i])
				.drawLines_(drawLines)
				.value_((v.linlin( min, max, 0.0, 1.0 );))
		};
		
		labels.do { | t, i |
			GUI.textView.new(win, Rect(4, 25 * i, 80, 20))
				.string_(t.asString)
				.stringColor_(colors[i])
				.background_(background)
				.font_(Font("Helvetica", 18) )
		}
		^win;
	}
	


	windowClump { | windowSize | 
		var result, size;
		size = this.size - windowSize - 1;
		result = (0..size).collect { | i |
			this[i..i+windowSize]
		};
		^result
	}

	rotatingWindowClump { | windowSize |
		var result, size, doubleArray = this ++ this;
		size = this.size - 1;
		result = (0..size).collect { | i |
			doubleArray[i..i+windowSize]
		};
		^result
	}

	slope {
		var list, prev = this.last;
		list = this.class.new(this.size);
		this.do {|item|
			list.add( item - prev );
			prev = item;
		};
		^list
	}

}

+Object {
	postL { | length = 10 |
		var str = this.asString;
		(str ++ String.fill(length - str.size, Char.space))[0..length - 1].post;
	}
	
	postLn { | length = 10 |
		var str = this.asString;		
		(str ++ String.fill(length - str.size, Char.space))[0..length - 1].postln;
	}
}