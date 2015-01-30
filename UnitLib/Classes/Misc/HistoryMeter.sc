HistoryMeter : UserViewHolder {

	var <>history, <>pos = 0;
	var value = 0, latestValue = 0, <>updated = false;
	var <>routine;
	var <color;
	var <>active = false;
	var <>interval = 1;
	
	init { |parent, bounds|
		super.init( parent, bounds );
		history = Array.fill( this.bounds.asRect.width, 0);
	}
	
	draw {
		var height, width;
		height = this.bounds.height;
		width = this.bounds.width.asInt;
		Pen.moveTo( 0 @ history[pos].linlin(0,1,height,0) );
		(history.size).do({ |item, i|
			Pen.lineTo( (i+1) @ history.wrapAt(pos+i).linlin(0,1,height,0) );
		});
		Pen.lineTo( history.size @ height );
		Pen.lineTo( 0 @ height );
		if( history.any(_ >= 1) ) { Pen.color = Color.red };
		if( color.notNil ) { Pen.color = color; };
		Pen.fill;
	}
	
	clear { history = Array.fill( this.bounds.asRect.width, 0); pos = 0; this.refresh; }
	
	color_ { |aColor| color = aColor; this.refresh }
	
	value { history[ pos ] }
	value_ { |val = 0| 
		if( this.isClosed.not ) {	
			latestValue = val;
			if( active ) {
				if( updated ) {
					value = val;
					updated = false;
				} {
					value = val.max( value );
				};
				if( routine.isPlaying.not ) {
					this.start; // resume after cmd-period
				};
			} {
				value = val;
				history[ pos ] = val; 
				pos = (pos + 1).wrap(0, history.size -1); 
				this.refresh;
			};
		}
	}
	
	start {
		routine.stop;
		routine = Task({
			while { active && this.isClosed.not } {
				history[ pos ] = value;
				pos = (pos + 1).wrap(0, history.size -1); 
				updated = true;
				value = latestValue;
				this.refresh;
				interval.max(0.05).wait;
			};
		}, AppClock ).start;
		active = true;
	}
	
	stop { active = false }
}