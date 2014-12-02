+ Color {
	test {
		var window, tileSize = 50;
		window = Window( this.asString ).front;
		window.drawHook = { |vw|
			var bounds;
			bounds = vw.view.bounds;
			
			if( alpha < 1 )
				{	
				// transparency grid
				(bounds.height/tileSize).ceil.do({ |i|
						(bounds.width/tileSize).ceil.do({ |ii|
							if( ((i + ii) % 2) == 1 )
								{ Pen.addRect( 
									Rect( ii * tileSize, i * tileSize, tileSize, tileSize )
									);
								};
							});
					});
				
				Pen.fillAxialGradient( bounds.leftBottom + (0@(tileSize.neg)), 
						bounds.leftTop + (0@(tileSize)), 
					Color.white, Color.white.alpha_(0));
				
				(bounds.height/tileSize).ceil.do({ |i|
						(bounds.width/tileSize).ceil.do({ |ii|
							if( ((i + ii) % 2) == 0 )
								{ Pen.addRect( 
									Rect( ii * tileSize, i * tileSize, tileSize, tileSize )
									);
								};
							});
					});
				
				Pen.fillAxialGradient( bounds.leftBottom + (0@(tileSize.neg)), 
						bounds.leftTop + (0@(tileSize)), 
					Color.black, Color.black.alpha_(0));
					};
				
			Pen.color_( this ).fillRect( bounds )		
			};
		}
	}