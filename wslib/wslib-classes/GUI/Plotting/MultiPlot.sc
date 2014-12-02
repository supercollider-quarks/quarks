// wslib 2007
// plot multiple arrays on top of eachother

// example:
/*
MultiPlot( [ (-10,-9..10), (-20,-18..20).fold(-10,10), { 3.0.rand2 } ! 20 ] )
*/

MultiPlot {

	*new { |arrays, width = 600, height = 400, colors, background, hLinesAt, vLinesAt, 
			linesAlpha = 0.5, discrete = false, min, max|
		var win;
		
		win = Window.new("MultiPlot", Rect(100,100,width,height));
		
		win.drawHook = { |w|
			var rect;
			rect = w.view.bounds.insetBy(10,10);
			if( background.notNil )
				{  background.set;
					Pen.fillRect( rect ); };
			Color.black.alpha_(0.5).set;
			Pen.strokeRect( rect );
			this.inRect( arrays, rect, colors,  hLinesAt, vLinesAt, linesAlpha, discrete, min, max );
			};
		win.refresh;
		win.front;
		}
	
	*inRect { |arrays, rect, colors, hLinesAt, vLinesAt, linesAlpha =0.5, discrete = false, min, max|
		var minVal, maxVal, maxArrSize;
		
		hLinesAt = hLinesAt ?? { [0] };
		vLinesAt = vLinesAt ?? { [] };
		
		minVal = min ?? { arrays.collect( _.minItem ).minItem; };
		maxVal = max ?? { arrays.collect( _.maxItem ).maxItem; };
		maxArrSize = arrays.collect( _.size ).maxItem;
	
		colors = colors ?? { 
			arrays.collect({ |item, i|
			[ Color.blue, Color.red, Color.green, Color.black, 
				Color.blue(0.5), 
				Color.red(0.5), 
				Color.green(0.5),
				Color.gray(0.25) ]
				.wrapAt( i ) })
			  };
			  
		Pen.use({
		
		Color.black.alpha_(linesAlpha).set;
		hLinesAt.do({ |item|
			item = item.linlin(minVal, maxVal, rect.bottom, rect.top, \none );
			Pen.line( rect.left@item, rect.right@item ).stroke; 
			  });
			  
		vLinesAt.do({ |item|
			item = item.linlin(0, maxArrSize - 1, rect.left, rect.right, \none );
			Pen.line(item@rect.top,item@rect.bottom ).stroke; 
			  });
				
		arrays.do({ |array, i|
			this.plotOne( array, rect, colors.wrapAt(i), minVal, maxVal, maxArrSize, discrete )
			});
			
		});
		
		}
		
	*plotOne { |array, rect, color, min, max, arrSize, discrete = false|
		var discRadius, zerolineY;
		arrSize = arrSize ?? {array.size};
		min = min ?? { array.minItem };
		max = max ?? { array.maxItem };
		discRadius = ( (rect.width / arrSize) / 3).min( 4 );
		zerolineY = 0.linlin(min ,max, rect.bottom, rect.top, \none );
		if( discrete )
			{ Pen.use({
				(color ? Color.black).set;
				array.do({ |item, i|
					var posX, posY;
					posX = rect.left + ((i/ (arrSize-1)) * rect.width);
					posY = (item.linlin(min ,max, rect.bottom, rect.top, \none ) );
					
					Pen.line( posX@zerolineY, posX@posY ).stroke;
					Pen.addArc( 
						posX@posY,
						discRadius, 0, 2pi ).fill;
					
					});
				}); 
			} { Pen.use({
				(color ? Color.black).set;
				Pen.moveTo( rect.left@(array[0].linlin(min, max, rect.bottom, rect.top, \none ) ) );
				array[1..].do({ |item, i|
					Pen.lineTo( 
						(rect.left + (((i + 1)/ (arrSize-1)) * rect.width ) )
							@
						(item.linlin(min ,max, rect.bottom, rect.top, \none ) ) )
					});
				Pen.stroke;
				}); 
			};
		}
	
	}