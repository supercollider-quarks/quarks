UMarkerEventView : UEventView {
	
	checkSelectionStatus { |selectionRect,shiftDown, minWidth, maxWidth|
		//this.createRect(minWidth, maxWidth);
		if(selectionRect.intersects(rect)) {
			selected = true
		} {
			if(shiftDown.not) {
				selected = false
			}
		}
	}

	ifIsInsideRect{ |mousePos, yesAction, noAction|

	    if(rect.containsPoint(mousePos)) {
	        yesAction.value;
	    } {
	        noAction.value;
	    }

	}

	mouseDownEvent{ |mousePos,scaledUserView,shiftDown,mode|

		px5Scaled =  scaledUserView.doReverseScale(Point(5,0)).x;
		px10Scaled = scaledUserView.doReverseScale(Point(10,0)).x;
		this.createRect(px10Scaled, scaledUserView.viewRect.width, scaledUserView);
		

        this.ifIsInsideRect( mousePos, {

           //moving
            state = \moving;
            originalTrack = event.track;
            originalStartTime = event.startTime;
            originalEndTime = event.endTime;

        }, {
            if(selected) {
                originalStartTime = event.startTime;
                originalEndTime = event.endTime;
                originalTrack = event.track;
                //event.wfsSynth.checkSoundFile;
            }
        })

	}

	mouseMoveEvent{ |deltaTime, deltaTrack, overallState, snap, moveVert, tempoMap|

        if(overallState == \moving) {
	        if( moveVert.not ) {
				if( tempoMap.notNil ) {
					event.startTime = tempoMap.timeMoveWithSnap( 
						originalStartTime, deltaTime, snap
					).max(0);
				} {
					event.startTime = (originalStartTime + deltaTime.round(snap)).max(0);
				};
			};
			event.track = originalTrack + deltaTrack;
        }

	}
	
	createRect { |minWidth, maxWidth, scaledUserView|
	    var dur = scaledUserView !? { 
		    scaledUserView.pixelScale.x * 30.max( this.getName.bounds.width + 2); 
		} ? 1;
		rect = Rect( event.startTime, event.track, dur.max(minWidth ? 0), 1 );
	}
	
	drawShape { |rectToDraw, height = 1|
		var radius = 5;
		radius = radius.min( rectToDraw.height/2 );
		
		Pen.moveTo( (rectToDraw.left - 1) @ 0 );
		Pen.lineTo( (rectToDraw.left + 1) @ 0 );
		Pen.lineTo( (rectToDraw.left + 1) @ (rectToDraw.top - radius) );
		Pen.arcTo( 
			(rectToDraw.left + 1) @ (rectToDraw.top),
			(rectToDraw.right - radius) @ (rectToDraw.top),
			radius  
		);
		Pen.arcTo( 
			rectToDraw.rightTop,
			rectToDraw.right @ (rectToDraw.top + radius),
			radius  
		);
		Pen.arcTo( 
			rectToDraw.rightBottom,
			(rectToDraw.left + 1 + radius) @ (rectToDraw.bottom),
			radius  
		);
		Pen.arcTo( 
			(rectToDraw.left + 1) @ (rectToDraw.bottom),
			(rectToDraw.left + 1) @ height,
			radius  
		);
		Pen.lineTo( (rectToDraw.left + 1) @ height );
		Pen.lineTo( (rectToDraw.left - 1) @ height );
		Pen.lineTo( (rectToDraw.left - 1) @ 0 );

	}

	draw { |scaledUserView, maxWidth|
		var lineAlpha =  if( event.disabled ) { 0.5  } { 1.0  };
		var scaledRect, innerRect;

		this.createRect(scaledUserView.doReverseScale(Point(10,0)).x, maxWidth, scaledUserView);

		scaledRect = scaledUserView.translateScale(rect);
		
		if( (scaledUserView.view.drawBounds.left <= scaledRect.right) and: {
			(scaledUserView.view.drawBounds.right >= scaledRect.left)
		}
		) {	
			innerRect = scaledRect;
	
			//selected outline
			if( selected ) {
				Pen.width = 2;
				Pen.color = Color.grey(0.2);
				this.drawShape(scaledRect, scaledUserView.view.bounds.height);
				Pen.stroke;
			};
			
			Pen.use({	
				var textLeft = 2;
				
				this.drawShape(innerRect, scaledUserView.view.bounds.height);
				Pen.clip;
				
				// fill inside
				Pen.addRect( Rect( innerRect.left - 1, 0, innerRect.width + 1, 
					scaledUserView.view.bounds.height )
				);
				event.getTypeColor.penFill(innerRect, lineAlpha * 0.75, nil, 10);
				
				//draw name
				if( scaledRect.height > 4 ) {
					Pen.color = Color.black.alpha_( lineAlpha  );
					if( event.lockStartTime ) {
						DrawIcon( \lock, Rect( scaledRect.left + 2, scaledRect.top, 14, 14 ) );
						textLeft = textLeft + 12;
				     };
				     if( event.autoPause == true ) {
						DrawIcon( \pause, Rect( scaledRect.left + textLeft, 
							scaledRect.top, 14, 14 ) );
						textLeft = textLeft + 12;
				     };
					Pen.stringAtPoint(
						" " ++ this.getName,
						scaledRect.leftTop.max( 0 @ -inf ) + (textLeft @ 1)
					);		       
				};
	
			});
			
		};

	}

}