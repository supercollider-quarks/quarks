// wslib 2009/2010

+ Object {
	
	doOnChange { |what, action, oneShot = true, replace = true|
		var controllerClass, controller;
		
		if( oneShot ) { 
			controllerClass = OneShotController; // from wslib
		} { 
			controllerClass = SimpleController; // from main
		};
			
		if( replace ) {
			controller = this.dependants.detect({ |item| 
				item.class == controllerClass 
			}) ?? { 
				controllerClass.new( this ); 
			};
		} { 
			controller = controllerClass.new( this ); 
		};
		
		^controller.put( what, action ); // return the controller
	}
	
	waitForChange { |what = \n_end, test| // makes a Routine wait for a change
		var condition, controller;
		this.register;
		condition = Condition.new;
		test = test ? true; // extra test to be able to depend on more arguments
		controller = this.doOnChange( what, { |...args|
			if( test.value(*args) ) {
				condition.test = true; 
				condition.signal;
			};
		}, true, false ); // replace = false; there can be many of these for one synth
		CmdPeriod.add( { controller.remove } ); // clean up after cmd-period
		^condition.wait; // return the condition
	}
}


+ Node {

	freeAction_ { |action| // performs action once and then removes it
		this.register;
		this.doOnChange( \n_end, action, true, true);
	}
		
	startAction_ { |action|
		this.register( false );
		this.doOnChange( \n_go, action, true, true );
	}
		
	pauseAction_ { |action, oneShot = false|
		this.register; 
		this.doOnChange( \n_off, action, oneShot, true );
		this.doOnChange( \n_end, { 
			this.pauseAction_( nil, oneShot, true ); 
		}, true, false ); // delete after end
	}
	
	runAction_ { |action, oneShot = false|
		this.register;
		this.doOnChange( \n_on, action, oneShot, true );
		this.doOnChange( \n_end, { 
			this.runAction_( nil, oneShot, true ); 
		}, true, false ); // delete after end
	}
		
	wait { |what = \n_end, test| // makes a Routine wait for a change
		^this.waitForChange( what, test );
	}
	
}