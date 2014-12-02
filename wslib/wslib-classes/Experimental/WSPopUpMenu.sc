// temporary hack (?) to re-enable lines and disabled items in latest git version

WSPopUpMenu {
	
	var <popup, <lastValue = 0, <items, <>action, <>mouseDownAction, wasClicked = false;
	
	*new { |parent, bounds|
		^super.new.init( parent, bounds );
	}
	
	init { |parent, bounds|
		popup = PopUpMenu( parent, bounds );
		
		popup.mouseDownAction = { |...args| wasClicked = true; mouseDownAction.value( *args ) };
		popup.action = { |pu|
			var item;
			if( wasClicked ) { 	
				item = items[pu.value].asString;
				if( (item[0] == $-) or: { item.includes( $( ) /*)*/ } ) { 
					pu.value = lastValue; 
				} {
					action.value( this );
					lastValue = pu.value;
				};
			} {	
				action.value( this );
				lastValue = pu.value;
			};
			
			wasClicked = false;
		};
	}
	
	
	doesNotUnderstand { arg selector ... args;
		var res;
		res = popup.perform( selector, *args );
		if( res != popup ) { ^res } { ^this };
	}
	
	items_ { |list| 
		items = list; 
		popup.items = items.collect({ |item|
			item = item.asString;
			
			if( (item[0] == $-) ) {
				item = " ";
			};
			
			if( item.includes( $( ) /*)*/ ) { 
				item = item.select({ |it| it != $(; /*)*/ })
			};
			
			item;
		});
	}
	
	value { ^popup.value }
	
	addUniqueMethod { |...args|
		popup.addUniqueMethod( *args );
	}
	
	
}