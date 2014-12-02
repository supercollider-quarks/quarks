PowerMate {
	var <>downAction;
	var <>upAction;

	var <>downTurnAction;
	var <>turnAction;

	var velSpec;
	var <isPressed = false;
	var powerMate;
	
	*new {
		^super.new.init
	}
	init {
		var hidInfo;
		var vel;
		
		velSpec = ControlSpec(128, -128, step: 1);
		
		hidInfo = GeneralHID.findBy( 1917, 1040);
		powerMate = GeneralHID.open( hidInfo );
		
		powerMate.slots[3][51].action_( { |v|
			// factor 0.069 assures that sum(turning one cycle) == 2pi
			vel = 0.069*velSpec.map(v.value);
			isPressed.if({
				downTurnAction.value(this, vel);
			}, {
				turnAction.value(this, vel);
			});
		});
		powerMate.slots[1][1].action_( { |v| 
			isPressed = v.value.booleanValue;
			isPressed.if({
				downAction.value(this)
			},{
				upAction.value(this)
			})
		});
	}
}