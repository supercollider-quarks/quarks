// wslib 2010

// a ScrollView wrapped inside SmoothSlider-based scroll bars
// works like a normal scrollview, but looks better (i.m.h.o.)
//  and corresponds with the ScaledUserView.window look

SmoothScrollView {
	
	var <composite, <scrollView, <moveSliders;
	
	/// TODO : solve problem with empty scrollview
	
	var resize = 1;
	var <sliderKnobColor, <sliderBackColor;
	var <sliderWidth = 12, <sliderSpacing = 2, <scaleSliderLength = 52; // defaults
	var window;
	var <refreshFunc;
	
	var <hasHorizontalScroller = true, <hasVerticalScroller = true;

	
	asView { ^scrollView.asView; }
	
	add { |...args| scrollView.add( *args ) }
	
	*new { |parent, bounds|
		var resize;
		
		if( parent.isNil or: {parent.isString} )
		 	{ 
			parent = Window(parent ? "SmoothScrollView", bounds).front;
			resize = 5;
			bounds = (bounds ?? {(parent.asView.bounds)}).asRect.moveTo(0,0);
			bounds !? { bounds = bounds.insetBy(2) };
			};
			
		^super.new.init( parent, bounds, resize);
		}
		
	init { |parent, bounds, inResize|
		var sliderSpace;
		
		sliderKnobColor = sliderKnobColor ?? { Color.gray(0.2); };
		sliderBackColor = sliderBackColor ?? { Color.black.alpha_(0.33); };
		
		resize = 	inResize ? resize;
		composite = CompositeView( parent, bounds ).resize_( resize );
		
		sliderSpace = sliderWidth + (2 * sliderSpacing );
				
		moveSliders = [ // hori
					SmoothSlider( composite,  Rect( 0, 
							composite.bounds.height - (sliderWidth + sliderSpacing), 
							composite.bounds.width - sliderSpace, 
							sliderWidth ) )
						 .value_(0.5)
						 .action_({ |v| 
							 if( v.relThumbSize < 1 ) {								scrollView.visibleOrigin =
							 		scrollView.visibleOrigin.x_(
							 			v.value * 
							 				(scrollView.innerBounds.width - 
							 					scrollView.bounds.width)
							 			);
								};
						 	})
						 .knobColor_( sliderKnobColor)
						 .hilightColor_( nil )
						 .background_(sliderBackColor)
						 .resize_(8)
				 		.canFocus_( false ),
				 		
				 	  // verti
					SmoothSlider( composite,  Rect( 
							composite.bounds.width - ( sliderWidth + sliderSpacing ),  
							0, sliderWidth, 
							composite.bounds.height - sliderSpace ) )
						 .value_(0.5)
						 .action_({ |v| 
							 if( v.relThumbSize < 1 ) {
						 		scrollView.visibleOrigin =
						 			scrollView.visibleOrigin.y_(
						 				(1-v.value) * 
						 					(scrollView.innerBounds.height -
						 						 scrollView.bounds.height)
						 				);
							 };
						 })
						 .knobColor_( sliderKnobColor )
						 .hilightColor_( nil )
						 .background_(sliderBackColor)
						 .resize_(6)
				 		.canFocus_( false ) 
					];
					
		scrollView = ScrollView( composite, Rect( 0, 0, 
				bounds.width - sliderSpace, 
				bounds.height - sliderSpace ) )
			.resize_( 5 );
			
		// scrollView.background_( Color.white );
		scrollView.hasHorizontalScroller_(false).hasVerticalScroller_(false);
					
		refreshFunc = { this.updateSliders };
		
		this.window.drawFunc = this.window.drawFunc.addFunc( refreshFunc );
		
		refreshFunc.value;
		
		}
		
	refresh { refreshFunc.value; composite.refresh; }
	
	background_ { |color| scrollView.background = color }
	
	resize_ { |resize| composite.resize_( resize ) }
			
	updateSliders {	
		var xSize, ySize;
		
		if( hasHorizontalScroller ) {
			xSize = (scrollView.bounds.width / scrollView.innerBounds.width).clip(0,1);
			
			moveSliders[0].relThumbSize_( xSize );
			
			if( xSize == 1 ) { 
				moveSliders[0].knobColor = nil;
			} { 
				moveSliders[0].knobColor = sliderKnobColor;
			};
			
			moveSliders[0].value_(
				(scrollView.visibleOrigin.x / 
					(scrollView.innerBounds.width - scrollView.bounds.width))
			);
		};
		
		if( hasVerticalScroller ) {	
			ySize = (scrollView.bounds.height / scrollView.innerBounds.height).clip(0,1);
			
			moveSliders[1].relThumbSize_( ySize );
			
			if( ySize == 1 ) { 
				moveSliders[1].knobColor = nil;
			} { 
				moveSliders[1].knobColor = sliderKnobColor;
			};
						
			moveSliders[1].value_(
				(1- (scrollView.visibleOrigin.y / 
				 	(scrollView.innerBounds.height - scrollView.bounds.height)))
				);
		}
	}
		
	window { ^window ?? { window = composite.getParents.last.findWindow; } }
	
	remove {
		composite.remove;
		this.window.drawHook = this.window.drawHook.removeFunc( refreshFunc );
		}
		
	sliderWidth_ { |value = 12|
		sliderWidth = value; this.updateViews;
	}
	
	sliderSpacing_ { |value = 2|
		sliderSpacing = value; this.updateViews;
	}
	
	scaleSliderLength_ { |value = 52|
		scaleSliderLength = value; this.updateViews;
	}
	
	hasHorizontalScroller_ { |value = true|
		hasHorizontalScroller = value; this.updateViews;
	}
	
	hasVerticalScroller_ { |value = true|
		hasVerticalScroller = value; this.updateViews;
	}
		
	updateViews {
		var bounds, sliderSpace;
		
		bounds = composite.bounds;
		sliderSpace = sliderWidth + (2 * sliderSpacing );
		
		moveSliders[0].bounds = Rect( 0, 
			bounds.height - (sliderWidth + sliderSpacing), 
			bounds.width - sliderSpace, 
			sliderWidth );
			
		moveSliders[1].bounds = Rect( bounds.width - ( sliderWidth + sliderSpacing ),  
			0, sliderWidth, 
			bounds.height - sliderSpace );
		
		moveSliders[0].visible = hasHorizontalScroller;
		moveSliders[1].visible = hasVerticalScroller;
		
		scrollView.bounds = Rect( 0, 0, 
				bounds.width -  ( sliderSpace * hasVerticalScroller.binaryValue ), 
				bounds.height - ( sliderSpace * hasHorizontalScroller.binaryValue ) 
		);
	}
	
	}