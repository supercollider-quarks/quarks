// Andrea Valle, jan 2010

// a class for autogui creation of synth and synthdefs
SynthDefAutogui {

	var <>name, <>synthDef, <>aSynth, <>rate, <>target, <>args, <>addAction, 
			<>closeOnCmdPeriod, <>freeOnClose, <>window, <>step , <>hOff, <>vOff, <>scopeOn, 
			<>specs, <>onInit ;

	var <>controlArr, <>guiArr, <>synth, <>bus ;	
	var <>stetho ;	
	var <>task, <>dur ; // for  monitor
	
	*new { arg name, aSynth, rate = \audio, target,args,addAction=\addToTail, 
			closeOnCmdPeriod = true, freeOnClose = true, 
			window, step = 50, hOff = 0, vOff = 0, scopeOn = true,
			specs, onInit = true ;
			
		^super.new.initSynthDefAutogui
			([name, aSynth, rate, target,args,addAction, 
			closeOnCmdPeriod, freeOnClose , window, step , hOff , vOff, scopeOn, specs, onInit]) 
	}

	initSynthDefAutogui { arg argArray ;	
		#name, aSynth, rate, target, args, addAction, 
			closeOnCmdPeriod, freeOnClose, window, step , hOff, vOff, scopeOn, specs, onInit  = argArray ;
		
		if(aSynth.notNil){
			target = aSynth
		}{
			if (target.isNil) {
				target = Server.default.asTarget; 
			}
		};
		
		scopeOn = (((GUI.current.name == \CocoaGUI) || (GUI.current.name == \QtGUI)) && (target.server != Server.internal)).not && scopeOn;

		synthDef = SynthDefStorage.synthDefDict[name.asString][0];
		// specs is a dict of controlspecs
		// we need to access it, so better having a void one
		if (specs.isNil) { specs = SynthDescLib.global[name.asSymbol].tryPerform(\metadata).tryPerform(\at,\specs) };
		if (specs.isNil) { specs = Dictionary.new } ;
		// just not to fillup the init method
		this.createMonitorTask ;
		if (onInit) { this.autogui };
	}

	// this is THE method
	// maybe too long but at the end is a on-shot creation
	autogui {
		
		var composite ; // contains stetho
		var h = step*2/5 ;	// vertical ratio fo modules
		var mrg = step/5 ; // margin
		// just ot draw all proportionally
		var wMod = step+mrg ; 
		var hMod = h + mrg ;
		var xOff = wMod*2.5 ;
		var fSize = 12/50*step ; // for font scaling
		var playB, monitorB, rateBox ;
		var labelArr, nameLabel ;
		// a flag storing a property
		var autowindow = false ;
		
		// contains info on defs and gui elements
		controlArr = []; guiArr = [] ;
		// monitor rate
		dur = 2 ;
		// creating a control structure for all control data in def
		// search for user-defined specs
		// if not set a default mapping
		synthDef.allControlNames.do({ arg item, index ;
				var name = item.asString.split($ )[4] ;
				var ctrl ;
				if (specs[name.asSymbol].isNil) {
					ctrl = 
					ControlSpec(0.0, (synthDef.controls[index]*2).max(1), \lin, 0.1, 
						synthDef.controls[index]) ;
					// special treatment for out
					if (name.asSymbol== \out) {
						ctrl = 
						ControlSpec(0, 1, \lin, 1, 
							synthDef.controls[index]) ;
						}
					} 
					{
					ctrl = specs[name.asSymbol].asSpec
					} ;
				controlArr = controlArr.add([name, ctrl])
				}) ;
		// GUI
		// no external window? Create one
		if (window.isNil)
			{
			autowindow = true ; // set the flag 
			window = Window.new(synthDef.name++" Control Panel", 
					Rect(30,30, (controlArr.size+1.5)*wMod+(wMod*2), hMod*6)) ;
			if (closeOnCmdPeriod) 
				{ CmdPeriod.doOnce { window.close } } ;
			if (freeOnClose)
				{ window.onClose_({ synth.free; task.stop}) } ;
			window.front 
		} ;
		// if you want scope, we put it
		// stethoscope bounds not as expected, solved with a trick 
		if (scopeOn)
			{
			composite = CompositeView.new(window, Rect(mrg+hOff, mrg+vOff, step*3, h*5.5));
			composite.decorator = FlowLayout(composite.bounds);
			stetho = Stethoscope.new(target.server, 1, rate:rate, view: composite) ;
			};
			
		// general controllers
		playB = Button.new(window, Rect(mrg+hOff, hMod*4+vOff, hMod*1.25, h))
			.states_([["| |", Color.white, Color.red], ["|>", Color.black, Color.grey]])
			.action_({|v| if(v.value==1) { synth.run(false) } { synth.run(true) } })
			.font_(Font(Font.defaultSansFace, fSize)) ;
		monitorB = Button.new(window, Rect(hMod*2+hOff, hMod*4+vOff, hMod*1.25, h))
			.states_([["m:Off", Color.black, Color.grey],["m:On", Color.white, Color.red]])
			.action_({|v| if(v.value==0) { this.stopMonitor } { this.startMonitor } })
			.font_(Font(Font.defaultSansFace, fSize)) ;
		rateBox = NumberBox.new(window, Rect(hMod*3.5+hOff, hMod*4+vOff, hMod*1.25, h))
			.value_(dur)
			.action_({|v|  dur = 1/v.value ; })
			.font_(Font(Font.defaultSansFace, fSize)) ;
		nameLabel = StaticText.new(window, Rect(mrg+hOff, hMod*5+vOff, wMod*2, h))
			.font_(Font.new(Font.defaultMonoFace, fSize*1.25))
			.string_(synthDef.name).align_( \right);
		// labels
		labelArr = [
		StaticText.new( window, Rect( xOff+hOff, mrg+vOff, step, h ))
						.string_( "min" ).align_( \right)
						.font_(Font(Font.defaultSansFace, fSize)) ,
		StaticText.new( window, Rect( xOff+hOff, hMod+vOff, step, h ))
						.string_( "max" ).align_( \right)
						.font_(Font(Font.defaultSansFace, fSize)) ,
		StaticText.new( window, Rect( xOff+hOff, hMod*2+vOff, step, h))
						.string_( "control" ).align_( \right)
						.font_(Font(Font.defaultSansFace, fSize)) ,
		StaticText.new( window, Rect( xOff+hOff, hMod*4+vOff, step, h ))
						.string_( "value" ).align_( \right)
						.font_(Font(Font.defaultSansFace, fSize)) ,
		StaticText.new( window, Rect( xOff+hOff, hMod*5+vOff, step, h ))
						.string_( "name" ).align_( \right)
						.font_(Font(Font.defaultSansFace, fSize)) 
					] ;
		// no synth provided? Create one
		if(aSynth.isNil) 
			{ 
				{
			synth = synthDef.play(target,args,addAction=addAction) ;
			target.server.sync ;
			// attention to scope
			if (scopeOn)
				{synth.get(\out, { |v| bus = v ; {stetho.index_(bus)}.defer })} ;
				}.fork 
			 }
			{ 
				{
			synth = aSynth  ;
			target.server.sync ;
			if (scopeOn)
				// attention to scope
				{ synth.get(\out, { |v| bus = v ; {stetho.index_(bus)}.defer }) };
				}.fork
			}	; 						 
		// Arg GUI controllers
		controlArr.do({ arg item, ind ;
				var index = ind+1 ;
				var guiElement = [
				NumberBox.new( window, Rect(wMod*(index)+xOff+hOff, mrg+vOff, step, h ))
						.value_(item[1].minval).background_(Color.grey)
						.font_(Font(Font.defaultSansFace, fSize)),
				NumberBox.new( window, Rect( wMod*(index)+xOff+hOff, hMod+vOff, step, h))
						.value_(item[1].maxval).background_(Color.grey)
						.font_(Font(Font.defaultSansFace, fSize)) ,
				Knob.new( window, Rect( wMod*(index)+xOff+hOff, hMod*2+vOff, step, step ))
					.value_(item[1].unmap(item[1].default)),
				NumberBox.new( window, Rect( wMod*(index)+xOff+hOff, hMod*4+vOff, step, h ))
						.value_(item[1].default).align_( \center)
						.font_(Font(Font.defaultSansFace, fSize)) , 
				StaticText.new( window, Rect( wMod*(index)+xOff+hOff, 
					hMod*5+vOff, step, h ))
						.string_( item[0] ).align_( \center)
						.font_(Font(Font.defaultSansFace, fSize)),
				UserView.new( window, Rect( wMod*(index)+xOff+hOff, 
					hMod*5+vOff, step, h ))
				] ;
				guiArr = guiArr.add(guiElement) ;			
		}) ;		
		// if scope is absent, redrawn the layout by moving some stuff 
		if (scopeOn == false) 
			{
				labelArr.do{|i| i.bounds_(i.bounds.moveBy(hMod*1.5+xOff.neg, 0))} ;
				controlArr.do({ arg item, index ;
					var guiElement = guiArr[index] ;
					guiElement.do{|i| i.bounds_(i.bounds.moveBy(hMod*1.5+xOff.neg, 0))}
				}) ;
				monitorB.bounds_(monitorB.bounds.moveTo(mrg+hOff, mrg*2+vOff)) ;
				rateBox.bounds_(rateBox.bounds.moveTo(mrg+hOff, mrg*2+hMod+vOff)) ;
				playB.bounds_(playB.bounds.moveTo(mrg+hOff, hMod*4+mrg+vOff)) ;
				nameLabel.bounds_(nameLabel.bounds.moveTo(mrg+hOff, hMod*3+vOff))
					.align_(\left) ;
				if (autowindow) 	
					{window.bounds_(window.bounds.width_(window.bounds.width-(hMod*3)))}
			} ;		
		// GUI action definition
		// mapping is retrieved from controlArr 
		controlArr.do({ arg item, index ;
			var guiElement = guiArr[index] ;
			guiElement[0].action  = { arg minBox ;
				item[1].minval_(minBox.value) ;						guiElement[2].value_(item[1].unmap(guiElement[3].value))
			} ;
			guiElement[1].action  = { arg maxBox ;
				item[1].maxval_(maxBox.value) ;
				 guiElement[2].value_(item[1].unmap(guiElement[3].value))
			} ;
			guiElement[2].action = { arg me ; 
				var name, val ;
				name = item[0] ;
				val = item[1].map(me.value) ;
				synth.set(name, val) ;
				guiElement[3].value_(val) ; 
				if(item[0].asSymbol == \out && stetho.notNil) {							synth.get(\out, { |v| bus = v ; {stetho.index_(bus)}.defer }) } 
					} ;
			guiElement[3].action = { arg me ; 
				var name = item[0] ;
				guiElement[2].value_(item[1].unmap(me.value)) ;
				synth.set(name, me.value) ;
				if(item[0].asSymbol == \out && stetho.notNil) {
					synth.get(\out, { |v| bus = v ; {stetho.index_(bus)}.defer }) };
			} ;
			guiElement[5].keyDownAction = {
				item[1].makeWindow(action:{|spec| 
					item[1] = spec ;
					guiElement[0].value_(item[1].minval) ;
					guiElement[1].value_(item[1].maxval) ;
					guiElement[2].value_(item[1].unmap(guiElement[3].value))
				})
			}
		}) ;	
	}


	// just to creat the monitoring task
	createMonitorTask { 
		task = Task({
			var val, t, mn, mx ;
			inf.do{
				t = dur/controlArr.size ;
				synth.get(\out, { |v| bus = v ; 
					if (stetho.notNil) {{stetho.index_(bus)}.defer}
					}) ;
				t.wait ;
				controlArr.do({ arg item, index ;
					synth.get(item[0].asSymbol, {|v|
					{
					guiArr[index][2].value_(item[1].unmap(v)) ;
					guiArr[index][3].value_(v) ;
					}.defer
						}) ;
					t.wait ;
				}) ;
			}
		}) ;
	}
	// interface
	startMonitor { 
		task.play ;
	}
	
	stopMonitor {
		task.pause
	}
	
}