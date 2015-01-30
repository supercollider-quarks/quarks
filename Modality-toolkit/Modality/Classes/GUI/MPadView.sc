MPadView : SCViewHolder {

	var <value = 0, <upValue = 0;
	var <pressed = false;
	var <useUpValue = false;
	var <>autoUpTime = inf;
	var <baseColor, <hiliteColor;
	var <label;
	var <font;
	var <vShiftLabel = 0;
	var <>action;

	var <>upAction;
	var upTimeTask, upTask, upValueScaled = 0;

	*new { |parent, bounds|
		^super.new.init( parent, bounds );
	}

	init { |parent, bounds|
		baseColor = Color.white;
		hiliteColor = Color.red(0.75,0.5);
		this.view = UserView( parent, bounds );
		this.view.drawFunc = { |vw|
			var rect, fillRect, fillColor;
			rect = vw.bounds.moveTo(0, 0).insetBy(1,1);
			if( pressed ) {
				Pen.width = 2;
				fillRect = rect.insetAll(0, (1 - value) * rect.height,0,0 );
				fillColor = hiliteColor;
			} {
				if( useUpValue && { upValueScaled > 0 }) {
					fillRect = rect.insetAll(0, (1 - upValueScaled) * rect.height,0,0 );
					fillColor = hiliteColor.copy.alpha_( hiliteColor.alpha * 0.5 );
				};
			};
			Pen.fillColor = baseColor;
			Pen.strokeColor = Color.black;
			Pen.fillRect( rect );
			if( fillRect.notNil ) {
				Pen.fillColor = fillColor;
				Pen.fillRect( fillRect );
			};
			Pen.strokeRect( rect );
			if( label.notNil ) {
				Pen.use({
					Pen.font = font;
					Pen.color = baseColor.complementary;
					Pen.stringCenteredIn( label.asString, rect.insetAll( 0, vShiftLabel * 2, 0, 0 ) );
				});
			};
		};

		this.view.mouseDownAction = { |vw, x, y|
			var rect;
			rect = vw.bounds.moveTo(0, 0).insetBy(1,1);
			this.valueAction = y.linlin( 0, rect.height, 1, 0 );
		};

		this.view.mouseUpAction = { |vw, x, y|
			var rect;
			if( autoUpTime == inf ) {
				rect = vw.bounds.moveTo(0, 0).insetBy(1,1);
				this.upValueAction = y.linlin( 0, rect.height, 1, 0 );
			};
		};

		this.refresh;
	}

	doUpAction { upAction.value( this ) }

	doAction { action.value( this ) }

	stopTasks {
		upTimeTask.stop;
		upTask.stop;
		upValueScaled = 0;
	}


	value_ { |newValue = 0|
		this.stopTasks;
		value = newValue;
		pressed = true;
		this.refresh;
		if( autoUpTime < inf ) {
			upTimeTask = Task({
				autoUpTime.wait;
				this.upValue = value;
			}, AppClock).start;
		};
	}

	valueAction_ { |newValue = 0|
		this.value = newValue;
		this.doAction;
	}

	upValue_ { |newValue = 0|
		this.stopTasks;
		if( useUpValue ) {
			upValue = newValue;
			pressed = false;
			upValueScaled = upValue;
			upTask = Task({
				while { upValueScaled > 0 } {
					0.05.wait;
					upValueScaled = upValueScaled - 0.05;
					this.refresh;
				};
			}, AppClock).start;
		} {
			if( pressed != false ) {
				pressed = false;
			};
		};
		this.refresh;
	}

	upValueAction_ { |newValue = 0|
		this.upValue = newValue;
		this.doUpAction;
	}
	
	label_ { |string| 
		label = string;
		this.refresh;
	}
	
	vShiftLabel_ { |number = 0|
		vShiftLabel = number;
		this.refresh;
	}
	
	font_ { |aFont|
		font = aFont;
		this.refresh;
	}

	pressed_ { |bool = false|
		pressed = bool;
		this.refresh;
	}

	useUpValue_ { |bool = true|
		useUpValue = bool;
		this.refresh;
	}

	baseColor_ { |color|
		baseColor = color;
		this.refresh;
	}

	hiliteColor_ { |color|
		hiliteColor = color;
		this.refresh;
	}
}

MPadUpViewRedirect {
	
	// creates a spoof View object which redirects upValue and upAction to value and action
	var <view, <action;
	
	*new { |view| // must be an MPadView
		^super.newCopyArgs( view );
	}
	
	value { ^view.upValue }
	value_ { |val| view.upValue_( val ) }
	valueAction_ { |val| view.upValueAction_( val ) }
	
	action_ { |func| 
		action = func;
		view.upAction = { action.value( this ) }; 
	}
	
	addAction { |act| this.action = action.addFunc( act ) }
	
	removeAction { |act| this.action = action.removeFunc( act ) }
	
	doesNotUnderstand { |selector ...args|
		var res;
		res = view.perform( selector, *args );
		if( res != view ) {
			^res;
		};
	}
}