/*
An SV is a CV that models an index into an array stored in the instance variable 'items'
The method 'item' returns the currently selected array element.

The default GUI presentation in ConductorGUI assumes that items is an array of Symbols.
*/

SV : CV {
	var <items;
	
	*new {arg items, default; 			
		^super.new.items_(items,default);
	}

	items_ { | argItems, default = 0|
		var index = 0;
		items = argItems ? [\nil];
		if (default.isNumber.not) { default = this.getIndex(default) };
		super.sp(default, 0, items.size - 1, 1, 'lin');
		this.changed(\items);
	}
	
	item { ^items[value] }

	item_ { | symbol |
		this.value = this.getIndex(symbol);
	}
	
	getIndex { | symbol |
		items.do { | it, i| if (symbol == it) { ^i } };
		^0
	}
	
	draw { |win, name =">"|
		~svGUI.value(win, name, this);
	}
	
	sp { | default = 0, symbols| this.items_(symbols, default) }
	
	next { ^items[value] }
	
}

