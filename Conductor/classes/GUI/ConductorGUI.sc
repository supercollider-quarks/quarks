ConductorGUI : GUIEvent {
	classvar <>osx;
	
	var 
		<>conductor,
		<>header,
		<>keys,
		<>guis,
		<>stopOnClose;
	
	*initClass {
		Class.initClassTree(GUIEvent);
		osx = (parent: GUIEvent.osx);
			osx.use {
			~cvGUI			= 	~nslider;
			~multicvGUI		=	~multislider;
			~svGUI			= 	~popup;
			~settingsGUI = { |win, name =">", settings|
				~simpleButton.value(win)
					.states_([["sv"]])
					.action_( { settings.save;  });
				~simpleButton.value(win)
					.states_([["ld"]])
					.action_( { settings.load; });
			};
			
			~interpolatorGUI =  { |win, name, interp|
					~slider.value(win, name, interp.interpCV, Rect(0,0,400,20));
					~numberBox.value(win, name, interp.targetCV, ~smallNumericalRect.value);
					~simpleButton.value(win)
						.states_([["<"]])
						.action_({
							interp.targetCV.value = 
							interp.targetCV.value - 1 mod: interp.targetCV.spec.maxval;
						});
					~simpleButton.value(win)
						.states_([[">"]])
						.action_({
							interp.targetCV.value = 
							interp.targetCV.value + 1 mod: interp.targetCV.spec.maxval;
						});
		
			};
			~presetGUI = { |win, name =">", preset|
				~simpleButton.value(win)
					.states_([["+"]])
					.action_({preset.addPreset; preset.presetCV.value_(preset.presets.size - 1)});
				~simpleButton.value(win)
					.states_([["-"]])
					.action_({preset.removePreset(preset.presetCV.value); });
		
				~simpleButton.value(win)
					.states_([["<"]])
					.action_({
						preset.presetCV.value = 
						preset.presetCV.value - 1 mod: preset.presets.size
					});
				~simpleButton.value(win)
					.states_([[">"]])
					.action_({
						preset.presetCV.value = 
						preset.presetCV.value + 1 mod: preset.presets.size
					});
				
				~numberBox.value(win, name, preset.presetCV, ~smallNumericalRect.value);
		
			};

			~letterWidth = 10;
			
			~playerGUI = { | win, name, player, rect|
				var bounds, stringWidth;
				var playButton, stopButton, ctl;
				
				rect = rect ?? ~simpleButtonRect;
				name = name.asString;
				stringWidth = (name.size + 1) * ~letterWidth;
				rect.width = stringWidth;
		
				playButton = ~simpleButton.value(win, rect);
				stopButton = ~simpleButton.value(win, Rect(0, 0, 20, rect.height));
		
				playButton
					.action_({ | view | var val, msg;
					
						val = view.value;
						[ {player.stop; },
						  {player.play; },
						  {player.pause},
						  {player.resume; view.value = 1 }
						][val].value;
					})		
					.onClose_({
						ctl.remove;  
						playButton = nil;
						stopButton = nil;
					})		
					.states_([
						[name, Color.new255(100, 200, 100),Color(0.2,0.2,0.2,1)],
						[name, Color.yellow(0.6),Color(0.2,0.2,0.2,1)],
						[name, Color.green(0.5),Color(0.2,0.2,0.2,1)], 
						[name, Color.new255(100, 200, 100),Color(0.2,0.2,0.2,1)]
					]);
		
				stopButton
					.states_( [ [" []", Color.red(0.5),Color(0.2,0.2,0.2,1)] ])
					.action_({ | view | playButton.valueAction_(0)});
					
				ctl = SimpleController(player).put(\synch, { |m,c,v| 
					defer { playButton.value = v }
				});
			};
			
			~simplePlayerGUI = { | win, name, player, rect|
				var bounds, stringWidth;
				var playButton, ctl;
				
				rect = rect ?? ~simpleButtonRect;
				name = name.asString;
				stringWidth = (name.size + 1) * ~letterWidth;
				rect.width = stringWidth;
		
				playButton = ~simpleButton.value(win, rect);
		
				playButton
					.action_({ | view | var val, msg;
					
						val = view.value;
						[ {player.stop; },
						  {player.play; },
						][val].value;
					})		
					.onClose_({
						ctl.remove;  
						playButton = nil;
					})		
					.states_([
						[name, Color.new255(100, 200, 100),Color(0.2,0.2,0.2,1)],
						[name, Color.red(0.6),Color(0.2,0.2,0.2,1)],
					]);
		
				ctl = SimpleController(player).put(\synch, { |m,c,v| 
					defer { playButton.value = v }
				});
			};
		}
	}

	*new { |conductor, keys|
		^super.new
			.conductor_(conductor)
			.keys_(keys)
			.parent_(ConductorGUI.osx)
			.guis_(())
			.stopOnClose_(true)
			.header_(#[[player,settings,preset]])
	}
	
	show { |argName, x = 128, y = 64, w = 900, h = 160|
		var win;
		this.use{
			win = ~window.value(argName ? "", Rect(x,y,w,h) );
			this.draw(win);
			~resizeWindowToContents.value(win);
			~win = win;
		};
		win.front;
		^win;
	}	
	
	reshow {
		var oldWin;
		oldWin = this[\win];
		this.show(oldWin.name, *oldWin.bounds.asArray);
		oldWin.close;
	}

	draw { | win, name, key |
		if (stopOnClose) {
			win.onClose = { conductor.stop }
//			SimpleController(win).put(\close, { conductor.stop });
		};
		this.use { this.drawItems(win) }
	}
	
	drawItems { | win |
		var guiFunction, cv, size, cvs;
		cvs = header ++ keys;
		size = cvs.size;
		
		cvs.do{arg v, i;
			v.asArray.do{ arg v; 
				if ( (guiFunction = guis[v]).isNil) {
					cv = conductor[v];
					if (cv.notNil) { cv.draw(win, v, cv) };
				} {
					if (guiFunction.isKindOf(Array)) {
						#guiFunction...cv = guiFunction;
						guiFunction = this[guiFunction];
					} {
						cv = conductor[v]
					};
					if (guiFunction.isKindOf(Symbol)) { guiFunction = this[guiFunction] };
					guiFunction.value(win, v, cv)
				};
				v.update(conductor,\synch); 
			};						
			if (i < (size - 1)) { win.view.decorator.nextLine } ;
		};
	}
	
	add { | key, gui |
		guis.put(key, gui);
		this.addKeys([key]);
	}
	
	addKeys { | newKeys |
		var v;
		keys = keys ++ newKeys;
		if (this[\win].notNil) {
			this.use {		
				newKeys.collect { | key |
					key.postln;
					v = conductor[key].draw (~win, key);
					if(~noCR.isNil) { ~nextLine.value(~win) };
					~views = ~views.add(v);
				};
			};
		}
	}
	
	resizeToFit {
		var rect;
		this.use {
			~resizeWindowToContents.value(~win);
			~win.resizeToFit;
			rect = ~win.bounds.moveTo(*(~win.parent.bounds.leftTop.asArray) );
			~win.parent.bounds = rect;
		}
	}
	
		
	resize { | pt |
		var win;
		win = this[\win];
		if (win.notNil) { 
			if (pt.isNil) {
				this.use {~resizeWindowToContents.value(win) };
			} {
				win.bounds = win.bounds.extent.resizeTo(pt.x, pt.y)
			}
		}
	}


}