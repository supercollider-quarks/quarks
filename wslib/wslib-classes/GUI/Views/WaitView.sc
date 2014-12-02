// W. Snoei 2006

WaitView : UserViewHolder { 

	// OSX alike wait sign (spinning stripes)

	var <>pos = 0, <>numLines = 16, <>centerSize = 0.5, <>lineWidth = 0.08;
	var <routine, <>speed = 2, <>resolution = 1;
	var <>active = false, <>alphaWhenStopped = 0.5;
	var <>color;
	var <>startAction, <>stopAction;
	
	init { |parent, bounds|
		super.init( parent, bounds );
		color = Color.gray(0.25);
		//this.relativeOrigin = false;
		}
	
	draw {
		var radius, center;		
		radius = this.bounds.width.min( this.bounds.height ) / 2;
		center = this.drawBounds.center;
		if( active or: { alphaWhenStopped != 0 } )
			{ Array.series( numLines + 1, -0.5pi, 2pi/numLines)[1..] 
				.do({ |val, i|
					color.copy.alpha_(
						(( (i/numLines) + (1 - pos) )%1.0) * 
							( if( active ) { 1 } { alphaWhenStopped } ) 
						).set;
					Pen.width_( lineWidth * radius * 2 );
					Pen.moveTo( ((val.cos@val.sin) * (radius * centerSize )) + center );
					Pen.lineTo( ((val.cos@val.sin) * radius) + center );
					Pen.stroke;
				});
			};
		}
		
	setPos { |newPos = 0| pos = newPos; this.refresh; }
	setPosD { |newPos = 0| pos = newPos; { this.refresh; }.defer; }
	
	refresh { if( this.notClosed ) { super.refresh; } } // safe refresh
	
	start { active = true;
		if( routine.isNil && { routine.isPlaying.not } ) 
		{ startAction.value( this );
		 routine = 
			Routine({
				loop { 
					if( this.isClosed ) { nil.alwaysYield }; // stop routine when window closed
					this.setPos( (pos + ( (1/numLines) / resolution) ) % 1.0 );
					(( (1/speed) / numLines ) / resolution).wait;
					};
				}).play( AppClock ); };
		}
	
	stop { routine !? { routine.stop }; 
		stopAction.value( this ); active = false; this.reset; routine = nil; }
	reset { this.setPos( 0 ); }
	
	}