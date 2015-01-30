CleanDragSink {
	classvar <>all;
	
	var <view, <>color, <>time = 2, <>task;
	var <>canReceiveDragHandler, <>receiveDragHandler;
	var <>onClose;
	
	*initClass {
		all = Set();
	}
	
	*new { |parent, bounds|
		^super.newCopyArgs.init( parent, bounds );
	}
	
	init { |parent, bounds|
		view = UserView( parent, bounds );
		color = Color.blue;
		
		all.add( this );
		view.onClose = { onClose.value; this.task.stop; all.remove( this ) };
		
		view.canReceiveDragHandler = { |vw ...args|
			if( this.canReceiveDragHandler.value( this, *args ) == true ) {
				this.startTask;
				true;
			} {
				false;
			};
		};	
		
		view.receiveDragHandler = { |vw ...args|
			this.stopTask;
			this.receiveDragHandler.value( this, *args );
		};
	}
	
	startTask {
		this.task.stop;
		view.background = this.color.copy.alpha_( 0.33 );
		this.task = Task({
			var n;
			n = (this.time / 0.2).asInt;
			n.do({ |i|
				view.background = this.color.copy.alpha_( i.linlin(0,n-1,0.33,0) );
				0.2.wait;
			});
		}, AppClock).start;
	}
	
	stopTask {
		this.task.stop; this.task = nil;
		view.background = Color.clear;
	}
	
	doesNotUnderstand { |what ...args|
		var res;
		res = view.perform( what, *args );
		if( res != view ) { ^res };
	}
}