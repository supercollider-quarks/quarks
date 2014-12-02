// wslib 2009

OneShotController : SimpleController {
	
	update { arg theChanger, what ... moreArgs;
		var action;
		if( actions.notNil )
		{ action = actions.at(what);
		  if (action.notNil, {
			action.valueArray(theChanger, what, moreArgs);
			actions.removeAt( what );
			if( actions.size == 0 )
				{ this.remove; };
			});
		};
	}
	
}