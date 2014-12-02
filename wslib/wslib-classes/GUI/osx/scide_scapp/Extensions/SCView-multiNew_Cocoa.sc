+ SCView {
	*multiNew { |parent, boundsArray|
		^boundsArray.collect({ |bounds| this.new(parent, bounds) });
		}
	*arrayNew { |parent ... argsArray|
		^this.multiNew(parent, Rect.multiNew(*argsArray) );
		}
	*series { |parent, bounds, size = 4,  step|
		bounds = bounds ? Rect(10,10,80,20);
		step = step ? (0@30);
		case { step.class == Point}
				{ step = Rect(step.x, step.y, 0, 0); }
			{ step.isNumber}
				{ step = Rect(step, 0, 0, 0); }
			{ step.isArray }
				{ step = Rect(*step) };
		^this.multiNew(parent, Array.fill(size,
				{ |i| Rect( *( bounds.storeArgs + (step.storeArgs * i) ) );
					}) );
			}
	}
