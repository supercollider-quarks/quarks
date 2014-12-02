
AllGui : JITGui { 
	const <globalNames = #[ 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 
					    'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' ];

	classvar <inited = false, <labels, <countFuncs, <openFuncs;

	var <texts; 
					    
	*new { |numItems = 12, parent, bounds|
		this.init;
		^super.new(nil, numItems, parent, bounds);
	}
	
	*countGlobals { 
		var interp = thisProcess.interpreter;
		^globalNames.count { |glob| interp.perform(glob).notNil };
	}
	
	*findProxySpace { 
		var space = if (currentEnvironment.isKindOf(ProxySpace), currentEnvironment); 
		if (space.notNil) { ^space };
		
		if (thisProcess.interpreter.p.isKindOf(ProxySpace), { space = thisProcess.interpreter.p });
		if (space.notNil) { ^space };
		
		^ProxySpace.all.maxItem(_.size);
	}
	
	*init { |force = false| 
		
		if (inited.not or: { force }) {	 
			inited = true; 

			labels = List[]; 
			countFuncs = (); 
			openFuncs = ();			
			
			[ 
				[ \global, 	{ this.countGlobals }, { |num| GlobalsGui(num).moveTo(600, 5); } ],
				[ \currEnvir, { currentEnvironment.size }, 
							{ |num| EnvirGui(currentEnvironment, num)
								.moveTo(540, 400)
								.parent.name_("currentEnvironment") } ],
				[\servers, 	{ Server.all.size }, { Server.all.do(_.makeWindow) } ],
				
				[ \Tdef,		{ Tdef.all.size }, { |num| TdefAllGui.new(num) } ],
				[ \Pdef, 		{ Pdef.all.size }, { |num|  PdefAllGui.new(num) } ],
				[ \Pdefn,		{ Pdefn.all.size }, { |num| PdefnAllGui.new(num) } ],
	
				[ \Ndef, 		{ Ndef.all.sum(_.size) }, { |num| NdefMixer.new(Ndef.all.choose, num) } ],
				[ \proxyspace, { try { this.findProxySpace.envir.size } { ProxySpace.all.sum { |ps| ps.envir.size } } },
							{ |num| ProxyMixer.new(this.findProxySpace, num) } ]
							
			].do { |triple| this.add(*triple) };
			
			if (\MKtl.asClass.notNil) { 
				this.add(\MKtl, { MKtl.all.size }, { |num| MKtlAllGui(num).moveTo(420, 5); });
			};
		};
		
	}
	
	*add { |name, countFunc, openFunc| 
		this.init;
		if (labels.includes(name).not) { labels.add(name) }; 
		countFuncs.put(name, countFunc);
		openFuncs.put(name, openFunc);
	}
	
	*remove { |name| 
		this.init;
		labels.remove(name); 
		countFuncs.removeAt(name);
		openFuncs.removeAt(name);
	}

	setDefaults { |options|
		minSize = 170 @ (skin.buttonHeight * (numItems + 1));
								// at the top - works in osx
		defPos = if (parent.isNil) { 250@5 } { skin.margin };
	}
	
	winName { ^"AllGui" }
	
	makeViews { 
		zone.resize_(2);
		texts = ();
				
		labels.do { |label|
			var numItemsBox, countView;
			
			Button(zone, Rect(0,0, 40, 20))
				.states_([["open"]])
				.action_({ openFuncs[label].value(numItemsBox.value.asInteger) })
			;
			
			numItemsBox = EZNumber(zone, Rect(0,0, 20, 20), nil, [0, 32, \lin, 1], initVal: numItems);

			countView = EZText(zone, 110@20, label.asString, labelWidth: 75)
				.value_(0).enabled_(false);
		
			countView.view.resize_(2);
			countView.labelView.align_(\center);
			countView.textField.align_(\center);
			countView.labelView.resize_(2);
			countView.textField.resize_(3);
			
			texts.put(label, countView);
		};
	}
	
	getState { 
		^countFuncs.collect(_.value);
	}
	
	checkUpdate { 
		var newState = this.getState;
		newState.keysValuesDo { |key, val| 
			try { texts[key].value_(val) };
		};
	}
}