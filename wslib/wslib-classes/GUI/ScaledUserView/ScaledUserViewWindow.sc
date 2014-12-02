// wslib 2006
// scaled SCUserView

// wslib 2009
// scaled SCUserViewContainer
// replacement for ScaledUserViewWindow
// can both be a separate window and a compositeview in an existing window

ScaledUserViewContainer {
	
	classvar <>defaultBounds, <>defaultFromBounds;
	
	var <composite, <userView, <scaleSliders, <moveSliders;
	var <viewOffset;
	var <>maxZoom = 8, <>minZoom = 1;
	var <>zoomCurve = 0;
	var resize = 1;
	var window;
	var currentBounds;
	var <sliderKnobColor, <sliderBackColor;

	var <sliderWidth = 12, <sliderSpacing = 2, <scaleSliderLength = 52; // defaults
	
	// these defaults should not be changed until after creation
	var <scaleHEnabled = true, <scaleVEnabled = true;
	var <moveHEnabled = true, <moveVEnabled = true;
	
	*initClass { 
		defaultBounds = Rect( 0, 0, 400, 400 );
		defaultFromBounds = Rect(0,0,1,1); // different from ScaledUserView
		}
		
	doesNotUnderstand { arg selector ... args;
		var res;
		res = userView.perform(selector, *args);
		if (res.class == ScaledUserView )
			{ ^this }
			{ ^res }
		}
	
	*new { |parent, bounds, fromBounds, viewOffset|
		var resize;
		
		viewOffset = viewOffset ? [1,1];
		case { viewOffset.class == Point }
			{ viewOffset = [ viewOffset.x, viewOffset.y ]; }
			{ viewOffset.size == 0 }
			{ viewOffset = [ viewOffset, viewOffset ]; }
			{ viewOffset.size == 1 }
			{ viewOffset = viewOffset ++ viewOffset; };
		
		if( parent.isNil or: {parent.isString} )
		 	{ 
			parent = Window(parent ? "ScaledUserView", bounds).front;
			resize = 5;
			bounds = (bounds ? defaultBounds).asRect;
			bounds !? { bounds = bounds
					.moveTo(*viewOffset)
					.resizeBy(*viewOffset.neg)
					.resizeBy(-2,-2) };
			};
			
		bounds = (bounds ? defaultBounds).asRect;
		fromBounds = (fromBounds ? defaultFromBounds).asRect;
		^super.new.init( parent, bounds, fromBounds, resize);
		}
		
	front { composite.front }
	
	init { | inParent, inBounds, inFromBounds, inResize|
		resize = inResize ? resize;
		
		sliderKnobColor = sliderKnobColor ?? { Color.gray(0.2); };
		sliderBackColor = sliderBackColor ?? { Color.black.alpha_(0.33); };
		composite = CompositeView( inParent, inBounds );
		composite.resize_( resize );
		// composite.background = Color.gray(0.8);
		// composite.onClose = { onClose.value( this, composite ) };
		
		userView = ScaledUserView( composite, Rect(0,0, 
			composite.bounds.width - (sliderWidth + sliderSpacing),
			composite.bounds.height - (sliderWidth + sliderSpacing)), inFromBounds );
			
		userView.view.resize_(5);
		userView.view.focusColor = Color.clear;
		userView.background = Color.white.alpha_(0.5);
		
		scaleSliders = [ 
			SmoothSlider( composite, Rect( 
					composite.bounds.width - 
						(scaleSliderLength + sliderWidth + sliderSpacing),  
		 			composite.bounds.height - sliderWidth, 
		 			scaleSliderLength, sliderWidth )  )
				.value_(0).action_({ |v| 
					userView.scaleH = 
						v.value.lincurve(0,1,minZoom.asPoint.x, maxZoom.asPoint.x, zoomCurve);
						//1 + (v.value * maxZoom.asPoint.x);
					this.setMoveSliderWidths( composite.bounds );
					})
				.knobColor_( sliderKnobColor )
		 		.hilightColor_( nil )
		 		.background_(sliderBackColor)
		 		.knobSize_( 1 )
		 		.canFocus_( false )
				.resize_(9),
			SmoothSlider( composite,  Rect( 
		 			composite.bounds.width - sliderWidth,  
		 			composite.bounds.height - 
		 				(scaleSliderLength + sliderWidth + sliderSpacing), 
		 				sliderWidth, scaleSliderLength ) )
				.value_(1).action_({ |v| 
					userView.scaleV = 
						(1-v.value).lincurve(0,1,minZoom.asPoint.y, maxZoom.asPoint.y, zoomCurve);
						//1 + ((1 - v.value) * maxZoom.asPoint.y);
					this.setMoveSliderWidths( composite.bounds );
					})
				.knobColor_( sliderKnobColor )
				.hilightColor_( nil )
				.background_(sliderBackColor)
				.knobSize_( 1 )
		 		.canFocus_( false )
				.resize_(9) ];
		 
		moveSliders = [ 
			SmoothSlider( composite,  Rect( 0, 
					composite.bounds.height - sliderWidth, 
					composite.bounds.width - 
						(scaleSliderLength + sliderWidth + (2 * sliderSpacing)), 
					sliderWidth ) )
				 .value_(0.5).action_({ |v| userView.moveH = v.value; })
				 .knobColor_( sliderKnobColor)
				 .hilightColor_( nil )
				 .background_(sliderBackColor)
				 .resize_(8)
		 		.canFocus_( false ),
			SmoothSlider( composite,  Rect( 
					composite.bounds.width - sliderWidth,  
					0, sliderWidth, 
					(composite.bounds.height - 
						(scaleSliderLength + sliderWidth + (2 * sliderSpacing))) ) )
				 .value_(0.5).action_({ |v| userView.moveV = 1 - (v.value); })
				 .knobColor_( sliderKnobColor )
				 .hilightColor_( nil )
				 .background_(sliderBackColor)
				 .resize_(6)
		 		.canFocus_( false ) ];
		
		this.setMoveSliderWidths;
		
		currentBounds = userView.bounds;
		
		userView.refreshAction = { |vw|
			if( currentBounds != vw.bounds )
				{ this.setMoveSliderWidths; currentBounds = vw.bounds; }
			};
		}
		
	updateSliders { |scaleFlag = true, moveFlag = true|
		if( scaleFlag )
			{ scaleSliders[0].value = 
				userView.scaleH.curvelin(minZoom.asPoint.x, maxZoom.asPoint.x, 0, 1, zoomCurve );
				//(userView.scaleH - 1) / maxZoom.asPoint.x;
			scaleSliders[1].value = 1 - 
				userView.scaleV.linlin(minZoom.asPoint.y, maxZoom.asPoint.y, 0, 1, zoomCurve );
				//((userView.scaleV - 1) / maxZoom.asPoint.y );
			this.setMoveSliderWidths;
			};
		if( moveFlag )
			{
			moveSliders[0].value = userView.moveH;
			moveSliders[1].value = 1 - userView.moveV;
			};
		}
		
	updateSliderBounds {
	
		var hasH, hasV;
		var scaleVReallyEnabled;
		
		scaleVReallyEnabled = scaleVEnabled and: (userView.keepRatio.not);
		
		// show/hide sliders
		[scaleHEnabled, scaleVReallyEnabled, moveHEnabled, moveVEnabled].do({ |enabled, i|
			[ scaleSliders[0], scaleSliders[1], moveSliders[0], moveSliders[1] ][i]
				.visible = enabled;
			});
					
		hasH =  (moveHEnabled or: scaleHEnabled).binaryValue;
		hasV =  (moveVEnabled or: scaleVReallyEnabled).binaryValue;
		
		#hasH, hasV = [ hasH, hasV ] * (sliderWidth + sliderSpacing);
				
		// set bounds		
		if( moveHEnabled )
			{ moveSliders[0].bounds = Rect( 
				0, 
				composite.bounds.height - sliderWidth, 
				composite.bounds.width - ( hasV +
					((scaleSliderLength + sliderSpacing) * scaleHEnabled.binaryValue )
					), 
				sliderWidth );
			};
				
		if( moveVEnabled )
			{ moveSliders[1].bounds = Rect( 
				composite.bounds.width - sliderWidth,  
				0, 
				sliderWidth, 
				composite.bounds.height - (hasH +
					((scaleSliderLength + sliderSpacing) * scaleVReallyEnabled.binaryValue ) 
					)
					
				);
			};
			
		if( scaleHEnabled )
			{ scaleSliders[0].bounds = Rect(
				composite.bounds.width - (scaleSliderLength + hasV),  
		 		composite.bounds.height - sliderWidth, 
		 		scaleSliderLength, 
		 		sliderWidth );
			};
			
		if( scaleVReallyEnabled )
			{ scaleSliders[1].bounds = Rect( 
				composite.bounds.width - sliderWidth,  
	 			composite.bounds.height - (scaleSliderLength + hasH), 
	 			sliderWidth, 
	 			scaleSliderLength );
			};
			
		userView.bounds = Rect(0,0, 
			composite.bounds.width - hasV,
			composite.bounds.height - hasH
			);
		}
		
	bounds { ^composite.bounds }
	bounds_ { |newBounds| composite.bounds = newBounds; }
		
	scaleHEnabled_ { |bool| scaleHEnabled = bool; this.updateSliderBounds; }
	scaleVEnabled_ { |bool| scaleVEnabled = bool; this.updateSliderBounds; }
	
	moveHEnabled_ { |bool| moveHEnabled = bool; this.updateSliderBounds; }
	moveVEnabled_ { |bool| moveVEnabled = bool; this.updateSliderBounds; }
	
	sliderWidth_ { |width = 12| sliderWidth = width; this.updateSliderBounds; }
	sliderSpacing_ { |spacing = 2| sliderSpacing = spacing; this.updateSliderBounds; }
	scaleSliderLength_ { |length = 52| scaleSliderLength = length;  this.updateSliderBounds; }
		
	sliderKnobColor_ { |color|
		(scaleSliders ++ moveSliders).do(_.knobColor_(color));
		}
		
	sliderBackColor_ { |color|
		(scaleSliders ++ moveSliders).do(_.background_(color));
		}
		
	setMoveSliderWidths { // |rect| // not used anymore
		moveSliders[0].relThumbSize = (1 / userView.scaleH).min( userView.scaleH );
		moveSliders[1].relThumbSize = (1 / userView.scaleV).min( userView.scaleV );
			}


	scaleH_ { |newScaleH, refreshFlag, updateFlag|
		updateFlag = updateFlag ? true;
		userView.scaleH_( newScaleH, refreshFlag );
		this.updateSliders( updateFlag, false );
		 }
		 
	scaleV_ { |newScaleV, refreshFlag, updateFlag|
		updateFlag = updateFlag ? true;
		userView.scaleV_( newScaleV, refreshFlag );
		this.updateSliders( updateFlag, false );
		 }
		 
	scale_ { |newScaleArray, refreshFlag, updateFlag| // can be single value, array or point
		updateFlag = updateFlag ? true;
		userView.scale_( newScaleArray, refreshFlag );
		this.updateSliders( updateFlag, false );
		}
		
		
	moveH_ { |newMoveH, refreshFlag, updateFlag|
		updateFlag = updateFlag ? true;
		userView.moveH_( newMoveH, refreshFlag );
		this.updateSliders( false, updateFlag );
		 }
		 
	moveV_ { |newMoveV, refreshFlag, updateFlag|
		updateFlag = updateFlag ? true;
		userView.moveV_( newMoveV, refreshFlag );
		this.updateSliders( false, updateFlag );
		 }
		 
	move_ { |newMoveArray, refreshFlag, updateFlag| // can be single value, array or point
		updateFlag = updateFlag ? true;
		userView.move_( newMoveArray, refreshFlag );
		this.updateSliders( false, updateFlag );
		}
		
	movePixels_ {  |newPixelsArray, limit, refreshFlag, updateFlag| 
		updateFlag = updateFlag ? true;
		userView.movePixels_( newPixelsArray, limit, refreshFlag );
		this.updateSliders( false, updateFlag );
		}
		
	viewRect_ { |rect, refreshFlag, updateFlag| // can be single value, array or point
		updateFlag = updateFlag ? true;
		userView.viewRect_( rect, refreshFlag );
		this.updateSliders( updateFlag, updateFlag );
		}
	
		
	keepRatio_ { |bool = false|
		userView.keepRatio = bool;
		this.updateSliderBounds;
		/*
		scaleSliders[1].visible = bool.not;
		
		moveSliders[1].bounds = moveSliders[1].bounds
			.height_( composite.bounds.height -
				[ scaleSliderLength + sliderWidth + (2 * sliderSpacing),
				  sliderWidth + sliderSpacing][ bool.binaryValue ] );
		*/
		this.scaleH = this.scaleH; 
		 }
		
	refresh { //this.setMoveSliderWidths;
		 ^composite.refresh }
		 
	resize { ^resize }
	resize_ { |val| resize = val; composite.resize_( val ) }
	
	window { ^window ?? { window = composite.getParents.last.findWindow; } }
	
	drawHook { ^this.window.drawHook }
	drawHook_ { |func| this.window.drawHook = func; }
	
	onClose { ^this.window.onClose }
	onClose_ { |func| this.window.onClose = func; }
	
	remove { composite.remove; }
		
	} 
	
//// Soon to be replaced by above

ScaledUserViewWindow {
	
	classvar <>defaultBounds, <>defaultFromBounds;
	
	var <window, <userView, <scaleSliders, <moveSliders, <>drawHook; 
	var <viewOffset;
	var <>maxZoom = 8;
	var <>onClose;
	
	*initClass { 
		defaultBounds = Rect( 350, 128, 400, 400 );
		defaultFromBounds = Rect(0,0,1,1); // different from ScaledUserView
		}
		
	doesNotUnderstand { arg selector ... args;
		var res;
		res = userView.perform(selector, *args);
		if (res.class == ScaledUserView )
			{ ^this }
			{ ^res }
		}
		
	*new { |name, bounds, fromBounds, viewOffset|
		name = name ? "ScaledUserViewWindow";
		bounds = (bounds ? defaultBounds).asRect;
		fromBounds = (fromBounds ? defaultFromBounds).asRect;
		viewOffset = viewOffset ? [1,1];
		^super.new.init( name, bounds, fromBounds, viewOffset);
		}
		
	front { window.front }
	
	init { | inName, inBounds, inFromBounds, inViewOffset|
		var sliderKnobColor, sliderBackColor;
		sliderKnobColor = Color.gray(0.2);
		sliderBackColor = Color.black.alpha_(0.33);
		window = Window( inName, inBounds );
		//window.view.background = Color.gray(0.8);
		case { inViewOffset.class == Point }
			{ viewOffset = [ inViewOffset.x, inViewOffset.y ]; }
			{ inViewOffset.size == 0 }
			{ viewOffset = [ inViewOffset, inViewOffset ]; }
			{ inViewOffset.size == 1 }
			{ viewOffset = inViewOffset ++ inViewOffset; }
			{ true }
			{ viewOffset = inViewOffset; };
		
		window.onClose = { onClose.value( this, window ) };
			
		userView = ScaledUserView( window, (window.asView.bounds + 
			Rect(0,0,-19,-19)).insetAll( *(viewOffset ++ [0,0]) ), inFromBounds );
			
		userView.view.resize_(5);
		userView.background = Color.white.alpha_(0.5);
		
		scaleSliders = [ 
			SmoothSlider( window,  Rect( 
		 			window.asView.bounds.width - 75,  
		 			window.asView.bounds.height - 16, 48, 13 )  )
				.value_(0).action_({ |v| 
					userView.scaleH = 1 + (v.value * maxZoom.asPoint.x);
					this.setMoveSliderWidths( window.asView.bounds );
					/*
					if( userView.keepRatio )
						{ scaleSliders[1].valueAction = (1 - v.value); };
					*/
					})
				.knobColor_( sliderKnobColor )
		 		.hilightColor_( nil )
		 		.background_(sliderBackColor)
		 		.knobSize_( 1 )
		 		.canFocus_( false )
				.resize_(9),
			SmoothSlider( window,  Rect( 
		 			window.asView.bounds.width - 16,  
		 			window.asView.bounds.height - 75, 13, 48 ) )
				.value_(1).action_({ |v| 
					userView.scaleV = 1 + ((1 - v.value) * maxZoom.asPoint.y);
					this.setMoveSliderWidths( window.asView.bounds );
					/*
					if( userView.keepRatio )
						{ scaleSliders[0].valueAction = (1 - v.value); };
					*/
					})
				.knobColor_( sliderKnobColor )
				.hilightColor_( nil )
				.background_(sliderBackColor)
				.knobSize_( 1 )
		 		.canFocus_( false )
				.resize_(9) ];
		 
		moveSliders = [ 
			SmoothSlider( window,  Rect( viewOffset[0],  
				 	window.asView.bounds.height - 16, 
				 	(window.asView.bounds.width - 78) - viewOffset[0], 13 )  )
				 .value_(0.5).action_({ |v| userView.moveH = v.value; })
				 .knobColor_( sliderKnobColor)
				 .hilightColor_( nil )
				 .background_(sliderBackColor)
				 .resize_(8)
		 		.canFocus_( false ),
			SmoothSlider( window,  Rect( 
					window.asView.bounds.width - 16,  viewOffset[1], 13, 
					(window.asView.bounds.height - 78) - viewOffset[1] )  )
				 .value_(0.5).action_({ |v| userView.moveV = 1 - (v.value); })
				 .knobColor_( sliderKnobColor )
				 .hilightColor_( nil )
				 .background_(sliderBackColor)
				 .resize_(6)
		 		.canFocus_( false ) ];
		
		this.setMoveSliderWidths;
		
		
		
		
		window.drawHook = { |w|
			this.setMoveSliderWidths;
			drawHook.value( window );
			};
			
		window.front;
		}
		
	updateSliders { |scaleFlag = true, moveFlag = true|
		if( scaleFlag )
			{ scaleSliders[0].value = (userView.scaleH - 1) / maxZoom.asPoint.x;
			scaleSliders[1].value = 1 - ((userView.scaleV - 1) / maxZoom.asPoint.y );
			this.setMoveSliderWidths;
			};
		if( moveFlag )
			{
			moveSliders[0].value = userView.moveH;
			moveSliders[1].value = 1 - userView.moveV;
			};
		}
		
	setMoveSliderWidths { // |rect| // not used anymore
		moveSliders[0].relThumbSize = (1 / userView.scaleH).min(1);
		moveSliders[1].relThumbSize = (1 / userView.scaleV).min(1);
			}


	scaleH_ { |newScaleH, refreshFlag, updateFlag|
		updateFlag = updateFlag ? true;
		userView.scaleH_( newScaleH, refreshFlag );
		this.updateSliders( updateFlag, false );
		 }
		 
	scaleV_ { |newScaleV, refreshFlag, updateFlag|
		updateFlag = updateFlag ? true;
		userView.scaleV_( newScaleV, refreshFlag );
		this.updateSliders( updateFlag, false );
		 }
		 
	scale_ { |newScaleArray, refreshFlag, updateFlag| // can be single value, array or point
		updateFlag = updateFlag ? true;
		userView.scale_( newScaleArray, refreshFlag );
		this.updateSliders( updateFlag, false );
		}
		
		
	moveH_ { |newMoveH, refreshFlag, updateFlag|
		updateFlag = updateFlag ? true;
		userView.moveH_( newMoveH, refreshFlag );
		this.updateSliders( false, updateFlag );
		 }
		 
	moveV_ { |newMoveV, refreshFlag, updateFlag|
		updateFlag = updateFlag ? true;
		userView.moveV_( newMoveV, refreshFlag );
		this.updateSliders( false, updateFlag );
		 }
		 
	move_ { |newMoveArray, refreshFlag, updateFlag| // can be single value, array or point
		updateFlag = updateFlag ? true;
		userView.move_( newMoveArray, refreshFlag );
		this.updateSliders( false, updateFlag );
		}
		
	movePixels_ {  |newPixelsArray, limit, refreshFlag, updateFlag| 
		updateFlag = updateFlag ? true;
		userView.movePixels_( newPixelsArray, limit, refreshFlag );
		this.updateSliders( false, updateFlag );
		}
		
	keepRatio_ { |bool = false|
		userView.keepRatio = bool;
		scaleSliders[1].visible = bool.not;
		this.scaleH = this.scaleH; 
		 }
		
	refresh { ^window.refresh }
		
	} 