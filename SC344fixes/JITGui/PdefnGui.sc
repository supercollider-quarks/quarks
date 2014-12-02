PdefnGui : JITGui {
	
	*observedClass { ^Pdefn }
	
	accepts { |obj| ^obj.isNil or: { obj.isKindOf(this.class.observedClass) } }

	getState { 
		// get all the state I need to know of the object I am watching
		^(object: object, source: try { object.source }) 
	}
	
	checkUpdate { 
		var newState = this.getState; 
		zone.visible_(newState[\object].notNil);
		
		if (newState[\object] != prevState[\object]) { 
		//	zone.visible_(newState[\object].notNil);
			this.name_(this.getName); 
		};
		if (newState[\source] != prevState[\source]) { 
			if (csView.textField.hasFocus.not) { 
				csView.value_(object);
					// ugly
				try { csView.textField.string_(object.asCode) };
			};
		};
	} 
}

PdefnAllGui : TaskProxyAllGui {
	*observedClass { ^Pdefn }
	*tpGuiClass { ^PdefnGui }

	setDefaults { 
		defPos = 540@660;
		minSize = 260 @ (numItems + 1 * 20);
	}
}

