+ ScaledUserView {
	
	drawTimeGrid { // assumes that 1px (unscaled) = 1s
		var viewRect, left, width, left60, round, leftRounded, bnds, scaleAmt;
		var top, bottom;
		
		viewRect = this.viewRect;
		top = viewRect.top;
		bottom = viewRect.bottom;
		left = viewRect.left.ceil;
		width = viewRect.width.ceil;
		round = (width / 5).max(1).nearestInList([1,5,10,30,60,300,600]);
		leftRounded = left.round(round);
		left60 = left.round(60);
		bnds = "00:00".bounds( Font( Font.defaultSansFace, 9 ) );
		bnds.width = bnds.width + 4;
		scaleAmt = 1/this.scaleAmt.asArray;
		
		Pen.width = this.pixelScale.x / 2;
		Pen.color = Color.gray.alpha_(0.25);
		
		if( viewRect.width < (this.view.bounds.width/4) ) {			width.do({ |i|
				Pen.line( (i + left) @ top, (i + left) @ bottom );
			});
			Pen.stroke;
		} {
			if( viewRect.width < (this.view.bounds.width/0.4) ) {
				(width/10).ceil.do({ |i|
					i = i*10;
					Pen.line( (i + left) @ top, (i + left) @ bottom );
				});
				Pen.stroke;
			};
		};
		
		Pen.color = Color.white.alpha_(0.75);
		(width / 60).ceil.do({ |i|
			i = (i * 60) + left60;
			Pen.line( i @ top, i @ bottom );
		});
		Pen.stroke;
		
		(width/round).ceil.do({ |i|
			Pen.use({
				i = i * round;
				Pen.translate( (i + leftRounded), bottom );
				Pen.scale( *scaleAmt );
				Pen.font = Font( Font.defaultSansFace, 9 );
				Pen.color = Color.gray.alpha_(0.25);
				Pen.addRect( bnds.moveBy( 0, bnds.height.neg - 1 ) ).fill;
				Pen.color = Color.white.alpha_(0.5);
				Pen.stringAtPoint(
					SMPTE.global.initSeconds( i+leftRounded ).asMinSec
						.collect({ |item| item.asInt.asStringToBase(10,2); })
						.join($:),
					2@(bnds.height.neg - 1) 
				);
			});
		});
	}
}
