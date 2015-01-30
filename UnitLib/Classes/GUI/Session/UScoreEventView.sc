/*
    Unit Library
    The Game Of Life Foundation. http://gameoflife.nl
    Copyright 2006-2011 Miguel Negrao, Wouter Snoei.

    GameOfLife Unit Library: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GameOfLife Unit Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GameOfLife Unit Library.  If not, see <http://www.gnu.org/licenses/>.
*/

UScoreEventView : UEventView {

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
		this.createRect(px10Scaled, scaledUserView.viewRect.width);
		

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

	draw { |scaledUserView, maxWidth|
		var lineAlpha =  if( event.disabled ) { 0.5  } { 1.0  };
		var scaledRect, innerRect;

		this.createRect(scaledUserView.doReverseScale(Point(10,0)).x, maxWidth);

		scaledRect = scaledUserView.translateScale(rect);
		
		if( scaledUserView.view.drawBounds.intersects( scaledRect.insetBy(-2,-2) ) ) {	
			innerRect = scaledRect.insetBy(0.5,0.5);
	
			//selected outline
			if( selected ) {
				Pen.width = 2;
				Pen.color = Color.grey(0.2);
				this.drawShape(scaledRect);
				Pen.stroke;
			};
			
			//event is playing
			if( event.isPlaying ) {
				Pen.width = 3;
				Pen.color = Color.grey(0.9);
				this.drawShape(scaledRect);
				Pen.stroke;
			};
			
			Pen.use({	
				var textLeft = 2;
				
				this.drawShape(innerRect);
				Pen.clip;
				
				// fill inside
				Pen.addRect( innerRect );
				event.getTypeColor.penFill(innerRect, lineAlpha * 0.75, nil, 10);
				
				//draw name
				if( scaledRect.height > 4 ) {
					Pen.color = Color.black.alpha_( lineAlpha  );
					if( event.lockStartTime ) {
						DrawIcon( \lock, Rect( scaledRect.left + 2, scaledRect.top, 14, 14 ) );
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
