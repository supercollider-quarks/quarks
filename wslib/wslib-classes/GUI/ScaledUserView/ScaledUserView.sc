// wslib 2006
// scaled SCUserView

// 2007: altered for SC3.2 compatibility
// 2009: altered for SwingOsc compatibility

// TODO: correct half pixel problem for mouse feedback

ScaledUserView { 

	classvar <>defaultGridColor;
	var <gridColor;
	
	var <view, <>fromBounds;
	var <scaleH = 1, <scaleV = 1, <moveH = 0.5, <moveV = 0.5;
	var <>gridSpacingH = 0, <>gridSpacingV = 0; // no grid when spacing == 0
	var <>gridMode = \blocks;
	var <>mouseDownAction, <>mouseMoveAction, <>mouseUpAction;
	var <>mouseOutOfBoundsAction;
	var <drawFunc, <unscaledDrawFunc, <beforeDrawFunc;
	var unclippedUnscaledDrawFunc; // depricated
	var <>autoRefresh = true;
	var <>autoRefreshMouseActions = true;
	
	var <>refreshAction;
	
	// clip: no effect anymore since SC3.3
	// still here for backwards compat
	var <clip = true;
	var <>clipScaled = true; // clips only when clip is also true
	var <>clipUnscaled = true; 
	
	var <background;
	var <>keepRatio = false; 
	
	// mouseActions arguments:
	//  |this, scaledX, scaledY, modifier, x, y, isInside|
	
	// unscaledDrawFunc:  0@0 == leftTop of view (not window)
	
	*initClass { 
		defaultGridColor = Color.gray.alpha_(0.25); 
		}
	
	*new { |window, bounds, fromBounds|
		bounds = (bounds ? Rect(0,0,360, 360)).asRect;
		fromBounds = (fromBounds ? bounds).asRect;
		^super.new.fromBounds_( fromBounds ).init( window, bounds);
		}
		
	*window { |name, bounds, fromBounds, viewOffset| 
			// creates a window with sliders for scale/move
		^ScaledUserViewContainer( name, bounds, fromBounds, viewOffset );
		}
		
	*withSliders { |window, bounds, fromBounds|
		^ScaledUserViewContainer( window, bounds, fromBounds );
		}
	
	init { |window, bounds|
		view = UserView( window, bounds );
		//view.relativeOrigin_( false );
		if( background.notNil ) { view.background = background; };
		
		gridColor = defaultGridColor;
		
		if( view.respondsTo( \drawBounds ).not ) // dirty - but it does the trick..
			{ view.addUniqueMethod( \drawBounds, { |vw| 
				if ( vw.relativeOrigin ) // thanks JostM !
					{ vw.bounds.moveTo(0,0) }
					{ vw.absoluteBounds; };
				})
			};
		
		view.mouseDownAction = { |v, x, y, m, buttonNumber, clickCount|
			var scaledX, scaledY, isInside = true;
			x = x - this.drawBounds.left; y = y - this.drawBounds.top;
			#scaledX, scaledY = this.convertBwd( x,y );
			mouseDownAction.value( this, scaledX, scaledY, m, x, y, isInside, 
				buttonNumber, clickCount);
			this.refresh( autoRefreshMouseActions );
			};
			
		view.mouseMoveAction = { |v, x, y, m|
			var scaledX, scaledY, isInside;
			isInside = this.drawBounds.containsPoint( x@y );
			x = x - this.drawBounds.left; y = y - this.drawBounds.top;
			#scaledX, scaledY = this.convertBwd( x,y );
			mouseMoveAction.value( this, scaledX, scaledY, m, x, y, isInside );
			if( isInside.not )
				{ mouseOutOfBoundsAction.value( this, scaledX, scaledY, m, x, y ); };
			this.refresh( autoRefreshMouseActions );
			};
		
		view.mouseUpAction = { |v, x, y, m|
			var scaledX, scaledY, isInside;
			isInside = this.drawBounds.containsPoint( x@y );
			x = x - this.drawBounds.left; y = y - this.drawBounds.top;
			#scaledX, scaledY = this.convertBwd( x,y );
			mouseUpAction.value( this, scaledX, scaledY, m, x, y, isInside );
			if( isInside.not )
				{ mouseOutOfBoundsAction.value( this, scaledX, scaledY, m, x, y ); };
			this.refresh( autoRefreshMouseActions );
			};
		
		
		view.drawFunc = { |v|
			
			var scaledViewBounds;
			var viewRect;
			var scaleAmt;
			
			Pen.use({
				viewRect = this.viewRect;
		
				beforeDrawFunc.value( this );
				
				if( background.class == Color )
					{ Pen.use({ 
						Pen.color = background;
						Pen.fillRect( this.drawBounds );
						}); 
					}; 
					
				// move to views leftTop corner (only when relativeOrigin==false) :
				Pen.translate( this.drawBounds.left, this.drawBounds.top );
				
				
				if( clip ) { // swing will need clip
						Pen.moveTo(0@0);
						Pen.lineTo(this.drawBounds.width@0);
						Pen.lineTo(this.drawBounds.width@this.drawBounds.height);
						Pen.lineTo(0@this.drawBounds.height);
						Pen.lineTo(0@0);
						Pen.clip;
						};
				
				
				Pen.use({
				
					//scaleAmt = this.scaleAmt;
					//Pen.scale( *scaleAmt );
					
					Pen.transformToRect( this.drawBounds, fromBounds, keepRatio, 
						scaleH@scaleV, moveH@moveV );
				
					//if( GUI.scheme.id == 'swing' && {(scaleAmt[0] != scaleAmt[1])} )
					//	{ Pen.translate( 0.5, 0.5 ); }; // temp correction for swingosc half-pixel bug
						
					// Pen.translate( *this.moveAmt );
					
					// grid:
					
					Pen.use({
					
					Pen.translate( fromBounds.left, fromBounds.top );
					
					if( (gridSpacingV != 0) && // kill grid if spacing < 2px
							{ (viewRect.height / this.drawBounds.height) < ( gridSpacingV / 2 ) } )
						{	if( gridMode.asCollection.wrapAt( 0 ) === 'blocks' )
						
								{ 	Pen.color = gridColor.asCollection[0];
									Pen.width = gridSpacingV;
									
									((0, (gridSpacingV * 2) .. fromBounds.height + gridSpacingV) 
											+ (gridSpacingV / 2))
										.abs
										.do({ |item| Pen.line( 0@item, (fromBounds.width)@item ); });
								} 
								{  	Pen.color = gridColor.asCollection[0]; //Color.black.blend( gridColor, 0.5 );
									Pen.width = (fromBounds.width / this.drawBounds.width).abs / scaleV; 
									
									(0, gridSpacingV .. (fromBounds.height + gridSpacingV))
										.abs
										.do({ |item| Pen.line( 0@item, (fromBounds.width)@item ); });
								 };
								
							Pen.stroke;
						};
					
					
					if( ( gridSpacingH != 0 ) &&
						 	{ (viewRect.width / this.drawBounds.width) < (gridSpacingH / 2 ) } )
						{	if( gridMode.asCollection.wrapAt( 1 ) === 'blocks' )
								{	Pen.color = gridColor.asCollection.wrapAt(1);
									Pen.width = gridSpacingH;
									
									((0, (gridSpacingH * 2) .. fromBounds.width + gridSpacingH) 
											+ (gridSpacingH / 2))
										.abs
										.do({ |item| Pen.line( item@0, item@(fromBounds.height) ); });
								} 
								{  	Pen.color =  gridColor.asCollection.wrapAt(1);
									Pen.width = (fromBounds.width / this.drawBounds.width).abs / scaleH; 
									
									(0, gridSpacingH .. (fromBounds.width + gridSpacingH))
										.abs
										.do({ |item| Pen.line( item@0, item@(fromBounds.height) ); });
								};	
							
							Pen.stroke;
						};
					});
						
					// drawFunc:
					
					// line will be 1px at current view width and scale == [1,1] 
					Pen.width = 
						[ (fromBounds.width / this.drawBounds.width).abs,
						  (fromBounds.height / this.drawBounds.height).abs ].mean; 
						 
					Pen.color = Color.black; // back to default
					
					drawFunc.value( this );
					});
					
					
				Pen.use({
					unscaledDrawFunc.value( this );
					unclippedUnscaledDrawFunc.value( this ); // depricated
					});
					
				 refreshAction.value( this );
				});
			};
			
		}
		
	refresh { |flag = true| 
		flag = flag ? autoRefresh;
		if( flag == true ) { 
			view.refresh;
			// refreshAction.value( this ); // won't work since this isn't the actual view
			 }; 
		}
		
	gridColor_ { |color, refreshFlag| 
		gridColor = color; 
		this.refresh( refreshFlag );
	}

	
	scaleH_ { |newScaleH, refreshFlag|
		scaleH = newScaleH ? scaleH; 
		if( keepRatio ) { scaleV = newScaleH ? scaleH; };
		this.refresh( refreshFlag );
		}
		
	scaleV_ { |newScaleV, refreshFlag|
		if( keepRatio.not )
			{ scaleV = newScaleV ? scaleV; this.refresh( refreshFlag ); };
		}
	
	scale { ^[ scaleH, scaleV ] }
	
	scale_ { |newScaleArray, refreshFlag| // can be single value, array or point
		newScaleArray = (newScaleArray ? this.scale).asPoint;
		this.scaleH_( newScaleArray.x, false );
		this.scaleV_( newScaleArray.y, false );
		this.refresh( refreshFlag );
		}
	
	moveH_ { |newMoveH, refreshFlag|
		moveH = newMoveH ? moveH; this.refresh( refreshFlag );
		}
	
	moveV_ { |newMoveV, refreshFlag|
		moveV = newMoveV ? moveV; this.refresh( refreshFlag );
		}
	
	move { ^[ moveH, moveV ] }
	
	move_ { |newMoveArray, refreshFlag|
		newMoveArray = (newMoveArray ? this.move).asPoint;
		moveH = newMoveArray.x;
		moveV = newMoveArray.y;
		this.refresh( refreshFlag );
		}
		
	movePixels { // works - pixel offset from center
		var bnds;
		bnds = this.drawBounds.extent.asArray.neg;
		^this.move.collect({ |item,i|
			item.linlin( 0.5,1.5,0, bnds[i] * (this.scale[i] - 1), \none);
			});
		}
		
	movePixels_ { |newPixelsArray, limit, refreshFlag| 
		var bnds;
		limit = limit ? true;
		newPixelsArray = (newPixelsArray ? [0,0]).asPoint.asArray;
		bnds = this.drawBounds.extent.asArray.neg;
		#moveH, moveV = newPixelsArray.asPoint.asArray.collect({ |item,i|
			if( this.scale[i] != 1 ) // no change if scale == 1 (prevent nan error)
				{ item.linlin( 0, bnds[i] * (this.scale[i] - 1), 0.5, 1.5, \none); }
				{ [moveH,moveV][i] };
			});
		if( limit ) { #moveH, moveV = [ moveH, moveV ].clip(0,1) };
		this.refresh( refreshFlag );	
		}
		
	reset { |refreshFlag| scaleH = scaleV = 1; moveH = moveV = 0.5; this.refresh( refreshFlag ); }
		
	// number of grid lines:
	gridLines { ^[ fromBounds.width / gridSpacingH, fromBounds.height / gridSpacingV ]; }
	
	gridLines_ { |newGridLines, refreshFlag|
		newGridLines = (newGridLines ? this.gridLines).asPoint;
		gridSpacingH = fromBounds.width / newGridLines.x;
		gridSpacingV = fromBounds.height / newGridLines.y;
		if( gridSpacingH == inf ) { gridSpacingH = 0 };
		if( gridSpacingV == inf ) { gridSpacingV = 0 };
		this.refresh( refreshFlag );
		}
		
		
	clip_ { |newBool, refreshFlag|
		newBool = newBool ? clip;
		clip = newBool;
		this.refresh( refreshFlag ); }
	
	drawFunc_ { |newDrawFunc, refreshFlag|
		drawFunc = newDrawFunc;  this.refresh( refreshFlag );
		}
		
	unscaledDrawFunc_ { |newDrawFunc, refreshFlag|
		unscaledDrawFunc = newDrawFunc;  this.refresh( refreshFlag );
		}
		
	unclippedUnscaledDrawFunc_ { |newDrawFunc, refreshFlag|
		this.deprecated(thisMethod);
		unclippedUnscaledDrawFunc = newDrawFunc;  this.refresh( refreshFlag );
		}
	
	unclippedUnscaledDrawFunc { 
		this.deprecated(thisMethod); ^unclippedUnscaledDrawFunc;
		}
		
	beforeDrawFunc_ { |newDrawFunc, refreshFlag|
		beforeDrawFunc = newDrawFunc;  this.refresh( refreshFlag );
		}
		
	clearDrawFuncs { |refreshFlag|
		drawFunc = unscaledDrawFunc = unclippedUnscaledDrawFunc = beforeDrawFunc = nil;
		this.refresh( refreshFlag );
		}
	
	background_ { |color, refreshFlag|
		background = color; 
		//view.background = background;
		this.refresh( refreshFlag ); }
		
	keyDownAction { ^view.keyDownAction }
	keyDownAction_ { |newAction| view.keyDownAction_( newAction ); }
		
	bounds { ^view.bounds }
	
	drawBounds { ^if( view.respondsTo( \drawBounds ) )
			{ view.drawBounds }
			{ view.bounds.moveTo(0,0) };
	}
	
	bounds_ { |newBounds|
		newBounds = (newBounds ? view.bounds).asRect;
		view.bounds = newBounds;
		}
		
	viewRect { |inset = 0| 
		// the currently viewed part of fromBounds
		/*
		var points;
		points = [ inset@inset,  this.drawBounds.extent - (inset@inset) ];
		points = points.collect({ |point| this.convertBwd( point.x, point.y ).asPoint; });
		^Rect( points[0].x, points[0].y, points[1].x - points[0].x, points[1].y - points[0].y );
		*/
		^this.drawBounds.transformFromRect( this.drawBounds, fromBounds, keepRatio, 
				scaleH@scaleV, moveH@moveV ).insetBy( inset, inset );
		}
	
	viewRect_ { |rect, refreshFlag| 
		var scale, move, msc;
		
		rect = rect.asRect;
		
		scale = (fromBounds.extent / rect.extent).asArray;
		move = (rect.leftTop - fromBounds.leftTop);
		msc = (fromBounds.extent - rect.extent).asArray;
		
		move = [ 
			move.x.linlin( 0, msc[0], 0, 1 ), 
			move.y.linlin( 0, msc[1], 0, 1 )
			];
			
		if( keepRatio )
			{ this.scale_( scale.minItem, false ); }
			{ this.scale_( scale, false ); };
		this.move_( move, false );
		
		this.refresh( refreshFlag );
		}
	
	pixelScale { 
		
		// extent of a rect that shows up as 
		// one pixel regardless of the scale settings
		// example: 
		//  Pen.width = vw.pixelScale.asArray.mean;

		^(((1@1)/view.bounds.extent) * fromBounds.extent) / (scaleH@scaleV);

	}
		
	translateScale { |item| 
		
		^item.transformToRect( this.drawBounds, fromBounds, keepRatio, 
			scaleH@scaleV, moveH@moveV );
		
	 	// requires extRect-transformations.sc from wslib
	 	// item should be Points, Rects or array containing Points and Rects
	 	/*
	 	if( item.class == Meta_Pen )
	 		{ ^Pen.scale( *this.scaleAmt ).translate( *this.moveAmt );  }
	 		{
	 		if( item.isArray )
	 			{ ^item.collect({ |subItem| 
	 				subItem.translateScale( this.moveAmt, this.scaleAmt ); }); }
	 			{ ^item.translateScale( this.moveAmt, this.scaleAmt ); }
			};
		*/
		 }
		  
	// scaling methods /// OLD (not compatible with keepRatio == true)
	convertScale { |inRect, outRect, sh = 1, sv = 1|
		if ( keepRatio )
			{	^( (1 / inRect.width.min(  inRect.height ) ) * 
				       outRect.width.min( outRect.height ) * [sh,sv] );  }
			{ 	^[ (1 / inRect.width ) * outRect.width  * sh, 
				   (1 / inRect.height) * outRect.height * sv ]; };
		}
			 
	convertMove { |inRect, mh = 0, mv = 0| 
		if ( keepRatio )
			{  ^( (( [mh.neg, mv.neg] * inRect.width.min( inRect.height )) + 0) 
				* (1 - (1/scaleH)) ); }
			{ ^[
				((mh.neg * inRect.width) + 0) * (1 - (1/scaleH)), 
				((mv.neg * inRect.height) + 0) * (1 - (1/scaleV))
			   ]; 
			 };
		}
		
	// these return input values for .translate and .scale
     /// OLD (not compatible with keepRatio == true)
	scaleAmt { ^this.convertScale( fromBounds, view.bounds, scaleH, scaleV ); }
	moveAmt { ^this.convertMove( fromBounds, moveH, moveV ); }
	
	// you can use these within drawFuncs and mouseFuncs to convert x/y values:
	
	convertFwd { |x = 0, y = 0| // move and scale input
		^(x@y).transformToRect( this.drawBounds, fromBounds, keepRatio,
				scaleH@scaleV, moveH@moveV ).asArray;
		}
	
	convertBwd { |x = 0, y = 0| // scale and move input backwards
		^(x@y).transformFromRect( this.drawBounds, fromBounds, keepRatio, 
				scaleH@scaleV, moveH@moveV ).asArray;
		}
	
	}