+Conductor {

	*midiMonitor {

		Conductor.make { | con, control = \SV|
			var updateFunc;
			con.gui.use{ 
				~playerGUI = ~simplePlayerGUI;
				~cvGUI = ~numerical; 
				~svGUI = { | w, name, cv, rect |
					StaticText(w, Rect(0, 0, w.bounds.width - 10, 20) )
						.font_(Font("Courier", 12) )
						.string_(" status   channel  b      c"); 
						w.view.decorator.nextLine;
					cv.connect( ListView(w, w.bounds.insetBy(4, 40))
								.resize_(5)
								.font_(Font("Courier", 12) )
								.hiliteColor_(Color.blue(0.2, 0.2) )
					);
				};  
				~listRect = Rect (0, 0, 200, 200);
			};
			con.noSettings;
			con.name_("MIDI Monitor");
		
			control.items = [""];	
			updateFunc = { | selector |
				{
					var ev;
					loop {
						ev = MIDIIn.perform(selector);
						control.items = control.items
						.addFirst(
							ev.status.asString.extend(10, Char.space) 
							+ ev.chan.asString.extend(6, Char.space) 
							+ ev.b.asString.extend(6, Char.space) 
							+ ev.c.asString.extend(6, Char.space))
						[..100];
					}
				}
			};
			con.action_( { MIDIIn.connectAll });
			con.task_( updateFunc.value( \waitControl));
			con.task_( updateFunc.value( \waitNoteOn));
			con.task_( updateFunc.value( \waitNoteOff));
			con.task_( updateFunc.value( \waitPoly));
			con.task_( updateFunc.value( \waitTouch));
			con.task_( updateFunc.value( \waitControl));
			con.task_( updateFunc.value( \waitBend));
			con.task_( updateFunc.value( \waitProgram));
			
		}.show("midi monitor", 30, Window.screenBounds.height - 40, 230, 500 );
	}


	useMIDI { | argKeys |
		var conductor = this;
		if (conductor.valueKeys.includes(\waitControlMappings).not) {
			conductor.valueKeys = conductor.valueKeys ++ \waitControlMappings;
			conductor[\waitControlMappings] = Ref( () );
		};
		if (conductor.valueKeys.includes(\waiNoteMappings).not) {
			conductor.valueKeys = conductor.valueKeys ++ \waitNoteMappings;
			conductor[\waitNoteMappings] = Ref( () );
		};
		conductor.gui.guis.put('map MIDI', 
			 { |win, name, interp|
			 	var w;
				~simpleButton.value(win, Rect(0,0,60, 20))
					.states_([["map MIDI", Color.black, Color.hsv(0, 0.5,1)]])
					.action_({ var cond;
						MIDIIn.connectAll;
						if (w.isNil) {
						cond = Conductor.make({ | con |
							var keys;
//							var ccAssigns, kdAssigns;
//							~ccAssigns = ccAssigns = ();
							con.gui.header = [];
							con.noSettings;
							con.name_("MIDI mapper");
							keys = argKeys ?? { conductor.gui.keys.flat.select{ | k | conductor[k].class === CV } };
							keys = argKeys ?? { conductor.gui.keys.flat.select{ | k | conductor[k].respondsTo(\input_)} };
							keys = #[player] ++ keys;
							keys.do({ | k | con.addCV(k) });
							keys.do({ | k | con[k].sp(0,0,2,1) });
							con.gui.use {
								~cvGUI = ~radiobuttons
							};
							
							
							con.task_( { var ev, packet, activeKeys;
									loop {
									ev = MIDIIn.waitControl;
									packet = ev.chan * 128 + ev.b;
									activeKeys = keys.select { | k | con[k].value == 1 };
									if (activeKeys.size > 0) {
										conductor[\waitControlMappings].value.put(packet, activeKeys.copy);
										defer {
											activeKeys.do { | k | con[k].value = 2 }
										}
									}
								}
							});

							con.task_( { var ev, packet, activeKeys;
									loop {
									ev = MIDIIn.waitNoteOn;
									packet = ev.chan * 128 + ev.b;
									activeKeys = keys.select { | k | con[k].value == 1 };
									if (activeKeys.size > 0) {
										conductor[\waitNoteMappings].value.put(packet, activeKeys.copy);
										defer {
											activeKeys.do { | k | con[k].value = 2 }
										}
									}
								}
							})
												
							
						});
						w = cond.show("MIDImap", win.bounds.left + win.bounds.width, win.bounds.top, 200, 300);
						cond.play;
						defer ({ w.bounds = w.bounds.resizeBy(50, 0)}, 0.02) ;
						topEnvironment[\w] = w;
					} {
						w.close; w = nil;
					};
				});
			}		
		);

		~midi = Conductor.make { | con |
			con.simpleGUI;
			con.name_("midi");
			con.action_({ MIDIIn.connectAll });

			con.task_({ var ev, keys;
				loop {
					ev = MIDIIn.waitControl;
					if ( (keys = conductor[\waitControlMappings].value[ev.chan * 128 + ev.b]).notNil) {
						keys.do { | key |
							conductor[key].input_(ev.c/127);
						}
					}	
				}
			});
			con.task_({ var ev, keys;
				loop {
					ev = MIDIIn.waitNoteOn;
					if ( (keys = conductor[\waitNoteMappings].value[ev.chan * 128 + ev.b]).notNil) {
						keys.do { | key |
							conductor[key].input_(ev.c/127);
						}
					}	
				}
			});
			con.task_({ var ev, keys;
				loop {
					ev = MIDIIn.waitNoteOff;
					if ( (keys = conductor[\waitNoteMappings].value[ev.chan * 128 + ev.b]).notNil) {
						keys.do { | key |
							conductor[key].input_(0);
						}
					}	
				}
			});
		};		


		conductor.gui.header = [ conductor.gui.header[0] ++ \midi ++ 'map MIDI'];
	}
	
	addCursor { | key = \cursor |
	
		currentEnvironment[key] = Conductor.make { | con, cursor = \Conductor, position, lo, hi, rate, increment |
			hi.value = 1;
			con.noSettings;
			rate	.sp(0.01, 0.001, 1);
			increment	.sp(0.01, 0.0001, 1, 0, 'exp');
			con.gui.keys = #[cursor];
			con.gui.guis.put(\cursor, { | win |
				var x, xw, w = CompositeView(win, Rect(0, 0, 860, 48) );
				x = ~labelW + 4;
				xw = ~sliderRect.value.width - ~labelW;
				con[\but] = Button(w, Rect(0, 0, 75, 24))
					.states_([["cursor", Color.black, Color.green(0.9, 0.2)], ["cursor",Color.black, Color.red(0.5, 0.3)]])
					.font_(Font("Helvetica", 10));
				ConductorSync(con[\cursor], con[\but]);		
			
				position.connect(Slider(w, Rect(x,  0, xw, 15)).canFocus_(false) );
				[lo, hi].connect( RangeSlider(w, Rect(x, 17, xw, 15)).focusColor_(Color.blue(0.4, 0.4)).canFocus_(false) );
				Button(w, Rect(0, 26, 30, 20)).states_([["incr"]]).canFocus_(false);
			
				increment.connect( 
					Slider(w, Rect(x,32, xw, 15)).canFocus_(false);
				);
				increment.connect( NumberBox(w, Rect(31, 26, 44, 20)) );
			});
			cursor.task_({loop { position.value = (wrap( position + increment, lo, hi).value); rate.value.wait } });
		};
		currentEnvironment.gui.keys = currentEnvironment.gui.keys ++ [key];
	}
	
	midiKBD_ { | noteOnFunction, midiChan | 
		var keyList = ();
		if (midiChan.isNil) {
			this.task_ { var ev, result, noteNum;
			 	loop {
				 	ev = MIDIIn.waitNoteOn;
				 	noteNum = ev.b;
				 	keyList[noteNum] = noteOnFunction.value(noteNum, ev.c);
				}
			};
			this.task_ { var ev, result, noteNum;
			 	loop {
				 	ev = MIDIIn.waitNoteOff;
				 	noteNum = ev.b;
				 	if ( (result = keyList[noteNum]).notNil) {
				 		result.release;
				 		keyList[noteNum] = nil;
				 	}
				}
			};
		} {
			this.task_ { var ev, result, noteNum;
			 	loop {
				 	ev = MIDIIn.waitNoteOn;
				 	if (ev.chan == midiChan) {
					 	noteNum = ev.b;
					 	keyList[noteNum] = noteOnFunction.value(noteNum, ev.c);
					}
				}
			};
			this.task_ { var ev, result, noteNum;
			 	loop {
				 	ev = MIDIIn.waitNoteOff;
				 	noteNum = ev.b;
				 	if ( (ev.chan = midiChan) && (result = keyList[noteNum]).notNil) {
				 		result.release;
				 		keyList[noteNum] = nil;
				 	}
				}
			};
		};
				
		this.action_({}, { keyList.do { | r | r.release }; keyList = ()  });
			 	
	}


}