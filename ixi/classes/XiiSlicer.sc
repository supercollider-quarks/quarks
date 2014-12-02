/* Slicer to do :

 + urgent :
exponential amp?
error when reposition, if set one layer then go to 8 and 7 no volume at all
when there is only one state if you try to go back to it it does not work
#####

pitch range areas draw 

multiple drag selection object

automovement, flocking, wind, jump
lock nodes X/Y buttons and/or shortcuts

be able to choose if clicks o not when loop crossing.

try broadcasting buffer IDs from bufferpool via OSC to integrate non Supercollider (OF?) applications
*/


XiiSlicer {
	classvar slicerindex; // global slicer instances count

	var instanceindex; // local value of this instance in global count
	
	var <>xiigui, <>win, params;
	var ldSndsGBufferList, selbPool;
	var stateDict, stateNum;

	var pitchRange, pitchRangeLimits, amp, outbus, rate, shift, start, length;
	var numOfLayers, bufferGlobalLimits, lowGraphicsMode, buffer, mute;
	var ampSpec;

	var gBufferPoolNum, poolName;

	var canvas, boxes, nodes, displays, selection;
	var players;
	var hcanvas, channels, server;
	var style, sndView, selected;
	var playheadsResponder;
	var sndNameList, gBufferPoolNum, bufferList;
	//var sendTrigIndex; // to avoid sendTrig index collisions
	//var rate, shift, start, length;
	var startLabel, lengthLabel, shiftLabel, pitchLabel; // LABELS for global values
	var rateRangeSl, hiRateLabel, lowRateLabel;
	var lowGraphicsButt, numOfLayersButton;
	var randNodesButt, randBoxesButt, randButt;
	var bufferPop, globalVolSl, outbusPop, startButt;
	var statesPop, clearButt, storeButt;


	*new { arg server=Server.default, channels=2, setting = nil;
		^super.new.initXiiSlicer(server, channels, setting);
	}


	updatePoolMenu 
	{
		var poolname, poolindex;
		poolname = selbPool.items.at(selbPool.value); // get the pool name (string)
		selbPool.items_(XQ.globalBufferDict.keys.asArray.sort); // put new list of pools
		poolindex = selbPool.items.indexOf(poolname); // find the index of old pool in new array
		if(poolindex != nil, {
			selbPool.valueAction_(poolindex); // nothing changed, but new poolarray or sound 
			ldSndsGBufferList.value(poolname);
		}, {
			selbPool.valueAction_(0); // loading a pool for the first time (index nil) 
			ldSndsGBufferList.value(XQ.globalBufferDict.keys.asArray[0]); // load first pool
		});
	}

	initAll { // CREATE SYNTHDEFS //
		if ( buffer == nil ) { buffer = XQ.globalBufferDict.at(poolName)[0][bufferPop.value] };		

		boxes.size.do ({ arg i;
			try { var srate = 44100; var synthdef;
				if ( server.actualSampleRate != nil ) { srate = server.actualSampleRate } ;
				if ( buffer.numChannels == 1, { synthdef = \xiiSlicerMonoPlayer }, 
											  { synthdef = \xiiSlicerStereoPlayer } ); 				
				// BEWARE that the synthdef index for the SendTrig is i+sendTrigIndex to avoid index collision with other triggers in ixiQuarks
				players.put(i, Synth(synthdef, [\buffer , buffer, \rate, rate, \start, start, 
						\length, length, \index, i + instanceindex ]));
				boxes[i].updateSynths(amp/numOfLayers); // update pan and vol
				if ( i > numOfLayers, { players[i].set(\rate, 0) }); // now stop the ones that are not visible
			}; //{ "snd server not init?".postln };		
				
		});
		this.updateLimits; // finally calculate each layer start and length
	}
	
	
	closeAll 
	{
		players.do({ arg i; i.free });
	}
	


	updateCanvas {
		canvas.clearDrawing; 
		canvas.refresh;
	}
	

	setRateRange { arg a, b;
		pitchRange = [ a, b, ((b - a).abs / canvas.bounds.height ).round(0.0001) ]; 	
	}




	setRate { arg pos; 
		rate = pitchRange[0] + ((canvas.bounds.height - pos) * pitchRange[2]).round(0.001);
		try { pitchLabel.string = "rate : " + rate };

		//("rate " + rate).postln;
			/*
			case 
				{ pos < hcanvas } // top area
					{
						if ( pos == 0, 
							{ rate = pitchRange[0] }, // fix float rounding problem
							{ rate = pitchRange[1] + (pitchRange[3] * ((hcanvas - pos).abs)) } // scale to mid to max
						);
					}
				{ pos == hcanvas } // center
					{ rate = pitchRange[1] }
				{ pos > hcanvas } // bottom area
					{ 
						if ( pos == 0, 
							{ rate = pitchRange[0] }, // fix float rounding proble
							{ rate = pitchRange[2] + ( pitchRange[4] * ((hcanvas - pos).abs) ) }
						);
					};
			*/
			//rate.postln;
		/*
			def setPitch(self, i) :
				if i < self.app.height*0.5 : # top. mouse range 0 to middle 300
				    if i == 0 : # very top'
				        self.app.pitch = self.app.pitchLimits[0] # fix float rounding problem
+
				    else:
				        self.app.pitch = self.app.pitchLimits[1] + ( self.app.pitchLimits[3] * abs(self.rev - i) ) # scale to mid to max
				elif i == self.app.height*0.5 :# middle
				    self.app.pitch = self.app.pitchLimits[1]
				else : # bottom. the difference is that mouse range is 300 to 600 here
				   self.app.pitch = self.app.pitchLimits[2] + ( self.app.pitchLimits[4] * abs(self.app.height - i) )
		*/
	}



	updateLimits {
		// update global values
		this.setRate(nodes[0].loc.y); // pitch, length
		length = nodes[0].loc.x; // in pixels
		start = nodes[1].loc.x; // in pixels
		shift = nodes[1].loc.y - hcanvas; // in pixels

		try { 
			lengthLabel.string = "length : " + length;
			startLabel.string = "start : " + start;
			shiftLabel.string = "shift : " + shift;
			pitchLabel.string = "rate : " + rate;
		};

		numOfLayers.do({ arg i; var mysttime, mylength, width;
		
			width = displays[0].bounds.width; // displays width. 620px

			mysttime = start + (i * shift); // "i" counter needed here
			mylength = length;
		
			if (mysttime > (width + displays[0].bounds.left), { // wrap on right
				mysttime = mysttime % width;
			    mylength = length;
				if (mylength > width, { // cut on right
			       	mylength = width;
				});
			});
		
			if (mysttime + mylength < displays[0].bounds.left, { // wrap on left
				mysttime = width - ((mysttime*(-1)) % width); // positive modulo from right
				mylength = length;
				//if (mysttime + mylength > width, { // cut on right
				//	mylength = width - mysttime + displays[0].bounds.left;
				//});
			});

			if (mysttime + mylength - displays[0].bounds.left > width, { // cut on right
				mylength = width - mysttime + displays[0].bounds.left;
			});
		
			if (mysttime < displays[0].bounds.left, { //cut on left
				mylength = mylength + mysttime - displays[0].bounds.left; // -- = +
			    mysttime = displays[0].bounds.left;
			});

			if ( (mysttime + mylength) <= mysttime, { mylength = 0.5 }); //minimum
		
			// update graphical representation
			displays[i].drawbounds.left = mysttime;
			displays[i].drawbounds.width = mylength;
			
			// update players
			try { var pstart, plength, relativeWidthFactor;
				pstart = bufferGlobalLimits[0] + (mysttime * (bufferGlobalLimits[1]/width));
				plength = ( mylength ) * (bufferGlobalLimits[1]/width);// selection 

				if ((pstart+plength) > 1) { plength = 1 - pstart }; // never overflow 1 limit

				players[i].set(\start, pstart); //normalise to 0/1 range		
				players[i].set(\length, plength);
				players[i].set(\rate, rate); // general;
			}//{"snd server not init?".postln}
		}); // end loop

		{ canvas.clearDrawing; canvas.refresh; }.defer(0.001); // hack**
	}



	
		
	mouseDown // handles mouse down on canvas
		{ arg  view, x, y, mod, button; 
		  var l, selectionflag;

			selectionflag = true;

			if ( (selected != nil) && (selected.class == XiiSlicerBox), { 
				try { selected.deselect }; // only works with boxes
				selected = nil;
			});
			

			l = [nodes, boxes, displays]; // mouseable items lists by cathegory
			block { arg break;
				l.size.do( { arg n;
					l[n].size.do( { arg i;
						if (l[n][i].bounds.contains(Point(x, y)) == true) {
							selected = l[n][i]; // this is clicked		
							case { button == 1 } 
								 	{  
										l[n][i].mouseDown(x,y);
										if (l[n][i].class == XiiSlicerDisplay, { selectionflag = false });
									}
								 { button == 3 } // MUTE
									{ 
										selectionflag = 0; //never with right click
										l[n][i].rightMouseDown(x,y); 
										canvas.clearDrawing;
										canvas.refresh; 
										if ( n > 0, // only boxes and displays control audio layers
											{ try{ players[i].set(\mute, l[n][i].color.alpha.round) } }
										); 
									}; // end case
							break.value(l.size-1); // now BREAK. job done
						};
					}); 
				});
			};

			//selected.postln;
			
			// mouse on background?
			if ( selectionflag == 1, { selection.start(x,y) });
	}


	initInstances // graphical elements of GUI
	{
		// set classvars
		XiiSlicerRect.canvasBounds = canvas.bounds;
		XiiSlicerRect.players = Ref(players); // as ref!
		XiiSlicerDisplay.win = win; //set classvar
		XiiSlicerDisplay.canvasBounds = canvas.bounds;



		numOfLayers.do({ arg i; var color, ypos;
			color = Color.rand(0.3, 0.8); // each layer has a rand color
			// DISPLAYS
			displays.put( i, XiiSlicerDisplay.new( Rect(10, 10, 620, 40), color ) );
			displays[i].label =
				StaticText(win, Rect(10,10, 100, 15))
					.font_(style.normal)
					.string_(i+1);
			// BOXES
			boxes.put( i, XiiSlicerBox.new( Rect(10,10, 12, 12), color ) );
			boxes[i].setIndex(i); //**
			boxes[i].label = 
				StaticText( win, Rect(10,10, 0, 0) )
					.font_(style.normal)
					.string_(i+1);
		});

		// NODES
		nodes.size.do({ arg i; var x;
			x = (canvas.bounds.width/2)-20; // both aligned in center of screen
			nodes.put( i, XiiSlicerRect.new( Rect(x+(i*20), canvas.bounds.height/2, 14, 14), 
											[Color(0.1,0.1,0.1), Color.white][i] ) );
		});


		selection  = XiiSelection.new(boxes, canvas.bounds);
	}


	reposition { var dw, dh, inbetween, dx, dy;

		dy = 10; // y loc for first one
		inbetween = ( dy * 3 ) / numOfLayers; 
		dh = ( canvas.bounds.height - (dy * 5)) / numOfLayers; // 65 # height.

		displays.size.do({ arg i; var color, ypos;
			if (i < numOfLayers,
				{
					displays[i].bounds = Rect(10.5, dy + 0.5, 620.5, dh + 0.5);
					displays[i].drawbounds = displays[i].bounds.copy; // this as well					
					dy = dy + dh + inbetween; // next Y position
					boxes[i].bounds = Rect(10.5+((canvas.bounds.width-20).rand), 10.5+((canvas.bounds.height-20).rand), 12.5, 12.5);
					try { players[i].set(\rate, rate) } ; // do i need this? 				
				} , {	//OFF the rest
					displays[i].bounds.top = -1000; // go offscreen
					displays[i].drawbounds.top = -1000;
					boxes[i].bounds.top = 1000; // low position to mute
					try { players[i].set(\rate, 0) } ; // STOP**;
				}
			);
			// all of them
			displays[i].updateLabel; 
			boxes[i].updateLoc;
			boxes[i].updateLabel;
			boxes[i].updateSynths(amp/numOfLayers); // update pan and vol
		});
		this.updateLimits;
	}


	randAll
	{
		this.randNodes;
		this.randBoxes;
	}

	randNodes
	{
		nodes.collect(_.randLoc);
		this.updateLimits; // updates global values of synths as well
	}

	randBoxes
	{
		boxes.size.do({ arg i;
			if (i < numOfLayers,
				{ boxes[i].randLoc },
				{ boxes[i].bounds.top = 1000 }
			);
			boxes[i].updateLoc;
			boxes[i].updateLabel;
			boxes[i].updateSynths(amp/numOfLayers);// synths
		});

		{ canvas.clearDrawing; canvas.refresh; }.defer(0.001); // hack**
	}
	



	// called from XiiSoundFileView when selected loop is moved
	setLoopRange {arg chnl, startPos, numSelectedFrames;
		var lo, hi, selfile;

//		[bufferGlobalLimits[0], "loop range"].postln;
		
		bufferGlobalLimits[0] = startPos/buffer.numFrames;
		bufferGlobalLimits[1] = bufferGlobalLimits[0]  + (numSelectedFrames/buffer.numFrames);

		XiiSlicerDisplay.selectionDelta = bufferGlobalLimits[1]-bufferGlobalLimits[0];

		this.updateLimits;
	}


	changeNumOfLayers { arg n; //Num of layers
		numOfLayers = n;
		//XiiSlicerBox.gNumLayers = numOfLayers;
		this.reposition; 
		numOfLayersButton.value = n - 1; //must be one less. check button code for details
	}

	getPositions {
		var positions;

		positions = Array.fill(10, {0}); // empty array right now. max 8 layers + 2 control nodes
		boxes.size.do({ arg i; // 0 to 7th position
			positions[i] = boxes[i].bounds; //.getPos;
		});
		nodes.size.do({ arg i; // 8th and 9th positions
			positions[i+8] = nodes[i].bounds; //.getPos;
		});
		^positions;
	}


	initialise { arg setting, state = "state 1";
		var point, positions, colors;

		//setting.postln;

		if (setting.isNil, {
			point = Point(100, 100);
			params = [ nil, nil, pitchRangeLimits, pitchRange, numOfLayers, rate, start, 
						length, amp, shift, lowGraphicsMode, buffer, outbus ];
		}, { // coming from a preset setting
			// ok - set state 1 as default state, and load vars - GUI views take care of themselves
			try {
				point = setting[1];
				
				stateDict = setting[2];
				stateNum = stateDict.size;
				params = setting[2].at(state.asSymbol);
			} {
				point = Point(win.bounds.top, win.bounds.left); //no point in this case
				
				params = setting.at(state.asSymbol).deepCopy;
				stateDict = setting;
				stateNum = stateDict.size;
			};
			//params.postln;

			// restore params
			positions = params[0].deepCopy;
			colors = params[1].deepCopy;
			pitchRangeLimits = params[2].copy;
			pitchRange = params[3].copy;
			numOfLayers = params[4].copy; 
			rate = params[5].copy; 
			start = params[6].copy; 
			length = params[7].copy;
			shift = params[8].copy;
			amp = params[9].copy;
			
			lowGraphicsMode = params[10].copy;
			buffer = params[11].copy;
			outbus = params[12].copy;
			
			statesPop.items_(stateDict.keys.asArray.sort); //update pull down menu
			//stateDict.keys.asArray.postln;
		});

		lowGraphicsButt.valueAction_(lowGraphicsMode);

		//XiiSlicerBox.gNumLayers = numOfLayers;
		//XiiSlicerBox.gAmp = amp;
		globalVolSl.value_(amp); //sets but doesnt trigger action //.valueAction_(amp);

		this.reposition; // NOT sure why I need to do this when not restoring a state
		numOfLayersButton.valueAction_(numOfLayers-1); //MUST be one less than variable. Check button code for details
			
		// set initial state from setting or random situation //
		if(setting.isNil, { 
			this.randAll 
		}, { // Restote positions from params[0] array of positions
			numOfLayers.do({ arg i; // 0 to max 7th position
				boxes[i].bounds = params[0][i];
				boxes[i].color = params[1][i];
				displays[i].color = boxes[i].color;
			});
			nodes.size.do({ arg i; // 8th and 9th positions
				nodes[i].bounds = params[0][i+8]; //.getPos;
			});
		}); 
		// ************** remove this following line later ??!!!!	
		this.setRateRange(pitchRangeLimits[0], pitchRangeLimits[0] + pitchRangeLimits[1].abs ); 		
		this.setRate(nodes[0].loc.y); // pitch, length

		outbusPop.value = outbus/2;
		try{ players.do({ arg i; i.set(\outbus, outbus) }) };

		 
		rateRangeSl.lo_(0.5 + (params[3][0].copy/pitchRangeLimits[1])); // normalise to range 0 to 1
		rateRangeSl.hi_(0.5 + (params[3][1].copy/pitchRangeLimits[1]));
		lowRateLabel.string = params[3][0].copy; 
		hiRateLabel.string = params[3][1].copy; 

		nodes.collect(_.updateLoc);
		boxes.collect(_.updateLoc);
		boxes.collect(_.updateLabel);		
		//boxes.collect(_.updateSynths); // update pan and vol
		boxes.do({ arg b; b.updateSynths(amp/numOfLayers) });
		("amp is"+amp).postln;

		win.bounds.left = point.x;
		win.bounds.top = point.y;

		this.updateLimits;
	}

	getColors { 
		var colors;
		colors = Array.fill(8, {0});
		boxes.size.do({ arg i; // 0 to max 7th position
			colors[i] = boxes[i].color.copy;
		});
		^colors;
	}

 	getState { // for save settings
		var point;
		point = Point(win.bounds.left, win.bounds.top); 

		params = [ this.getPositions, this.getColors, pitchRangeLimits, pitchRange, numOfLayers, 
					rate, start, length, shift, amp, lowGraphicsMode, buffer, outbus ];

		if(stateDict.size == 0, {
			stateDict.add("state 1".asSymbol -> params.copy); // we create a state
		});
		//stateDict.postln;
		
		^[2, point, stateDict];
	}




	/* INIT */

	initXiiSlicer { arg server_, channels_, setting;
		var point, params;
		var updatePoolMenu; 
		var setplayheads; 

		if ( slicerindex == nil, // global slicer instances count
			{ slicerindex = 600 }, // first instance opened
			{ 
				//XiiAlert("A single Slicer can be run. sorry.");
				//^nil;
				[\slicerindex, slicerindex].postln;
				//slicerindex = 600;
				slicerindex = slicerindex + 8;
				//("opening a new copy of Slicer with instanceindex " + slicerindex).postln;
			} // new instance being opened. 8 layers max for each
		);
		instanceindex = slicerindex; // each instance knows its own index in the global count
		
		channels = channels_;
		server = server_;
		
		selected = nil;
		style = XiiStyles(channels);

		players = Array.fill(8, {0}); // 8 is max num of layers
		displays = Array.fill(8, {0});
		boxes = Array.fill(8, {0});
		nodes = Array.fill(2, {0});

		bufferGlobalLimits = Array.fill(2, {0}); // global limits of the selected buffer

		stateDict = ();
		stateNum = 0;

		// set params
		numOfLayers = 8;
		rate = 1;
		start = 0;
		length = 1;
		amp = 0.5;
		shift = 1;
		outbus = 0;

		mute = false;
	
		//pitchRange = [-2, 0, 2, 0, 0]; // seetop, middle of screen and bottom
		pitchRangeLimits = [-5, 10]; // mix and max//range
		pitchRange = [-5, 5, 4]; // low, top, range/canvas.bounds.width 

		if ( GUI.id == \swing, 
			{ lowGraphicsMode = 1 },
			{ lowGraphicsMode = 0 }
		);

		/*
				n = self.size[1]*0.5 # half the height of the window
				self.__pitchLimits = l # items 0,1,2 (top, midd, bottom)
				self.__pitchLimits.append( (l[0] - l[1]) / n) # sub 3
				self.__pitchLimits.append( (l[1] - l[2]) / n) # sub 4
		*/
		setplayheads = { arg time, resp, msg;  // red playheads
			var pos;
			pos = msg[2] - instanceindex; 
			if ( (pos >= 0) && (pos <= 7) , { // within range for this particular instance of Slicer
				displays[pos].setplayhead(msg[3]); // sendTrigIndex is to avoid duplicated indexes between different ixiQuarks SendTrigs
			});
		};

		playheadsResponder = OSCresponderNode( server.addr, '/ixi/slicer/playheads'+instanceindex, setplayheads ).add;


		/// GUI //////

		// WIN
		win = Window("Slicer", Rect(100, 100, 772, 494), resizable: false);

		win.view.keyDownAction = 
		{ arg view, char, modifiers, unicode, keycode; 
			var delta;	 
			//["keyDownAction", modifiers, unicode, keycode].postln;

			if ( modifiers == 8519680, { delta = 5 }, { delta = 1 }); //SHIFT

			case 
				{ keycode == 32 } { // Space key MUTE & UNMUTE
									if ( mute == true, {
										globalVolSl.valueAction_(amp); 
										mute = false;
									}, {
										globalVolSl.valueAction_(0); 
										mute = true;
									});
									
								} 
				{ keycode == 27 } { // ESC quits 
									this.closeAll;//.value;
									win.close;
								}
				{ (keycode >= 49) && (keycode <= 56) } { // keys 1 to 8
									this.changeNumOfLayers(keycode-48);  // set num of layers
								} 
				{ keycode == 38 } { // up
									selected.bounds.top = selected.bounds.top - delta;
									selected.updateLoc;
									try { selected.updateLabel };	
								} 
				{ keycode == 40 } { // down
									selected.bounds.top = selected.bounds.top + delta;
									selected.updateLoc;
									try { selected.updateLabel };					
								} 
				{ keycode==39 } { // right								
									selected.bounds.left = selected.bounds.left + delta;
									selected.updateLoc;
									try { selected.updateLabel };
								} 
				{ keycode==37 } { // left				
									selected.bounds.left = selected.bounds.left - delta;
									selected.updateLoc;
									try { selected.updateLabel };
								};
		};



		// Instantiate objects
		canvas = UserView(win, Rect(120, 2, 650, 482))
			.canFocus_(false)
			.drawFunc_({ arg view;
				// smoothing off ONLY if button is activated
				if ( lowGraphicsMode == 1, { Pen.smoothing_(false) } ); // faster rendering in java
		
				Pen.color = Color(0.5, 0.5, 0.5, 1);
				Pen.strokeRect( Rect(0.5, 0.5, 640+0.5, 480+0.5 ) );

				displays.collect(_.draw); // on background
				
				nodes.do({ arg n; // lines between boxes and nodes//
					Pen.color = n.color; // two different colors
					numOfLayers.do({ arg i; Pen.line( boxes[i].loc, n.loc ) });
					Pen.stroke;
				});
				
				Pen.color = Color(0.9, 0, 0, 1);
				displays.collect(_.drawplayhead);
				Pen.stroke;

				if ( GUI.id == \cocoa,
					{ win.refresh }, 
					{ this.updateCanvas }
				);
		
				boxes.collect(_.draw);
				nodes.collect(_.draw);
				selection.draw;
			})
			//.relativeOrigin_(true) // use this for the refresh
			.clearOnRefresh_(true) // no refresh when window is refreshed
			.mouseMoveAction_({ arg view, x, y;
				if ( selected != nil ) { // if there is a selection
					selected.mouseMoved(x,y);
					//  in case it is a control node, update globals and its display
					if ( nodes.find([selected]) != nil  , { this.updateLimits });
				}
			})
			.mouseDownAction_({ arg  view, x, y, mod, button; 
				this.mouseDown(view, x, y, mod, button)
			})
			.mouseUpAction_({ arg  view, x, y;
				if ( selected == nil, {
					selection.doSelection;	
				},{ 
					selected.mouseUp;
				});
			}); //end canvas


		//***** ///
		hcanvas = canvas.bounds.height*0.5;
		//*****///

		this.initInstances; 



		// WIDGETS //

		// buffer pools //
		selbPool = PopUpMenu.new(win, Rect(10, 5, 100, 16))
			.font_(style.normal)
			.items_(if(XQ.globalBufferDict.keys.asArray == [], {["no pool"]}, {XQ.globalBufferDict.keys.asArray}))
			.value_(0)
			.canFocus_(false)
			.background_(Color.white)
			.action_({ arg item;
				gBufferPoolNum = item.value;
				ldSndsGBufferList.value(selbPool.items[item.value]);
			});

		
		bufferPop = PopUpMenu.new(win, Rect(10, 27, 100, 16)) // 550
				.font_(style.normal)
				.items_(["no buffer 1", "no buffer 2"])
				.background_(Color.new255(255, 255, 255))
				.canFocus_(false)	
				.action_({ arg popup;
					//this.closeAll;
					buffer = XQ.globalBufferDict.at(poolName)[0][popup.value];
					//if ( startButt.value == 1, { this.initAll }); //restart to update						
					bufferGlobalLimits[0] = XQ.globalBufferDict.at(poolName)[1][popup.value][0] / buffer.numFrames;
					bufferGlobalLimits[1] = XQ.globalBufferDict.at(poolName)[1][popup.value][1] / buffer.numFrames;
					XiiSlicerDisplay.selectionDelta = bufferGlobalLimits[1]-bufferGlobalLimits[0];		
					
					try{ players.do({ arg i; i.set(\buffer, buffer) }) };	
					this.updateLimits; // finally calculate each layer start and length			
				})
				.addAction({ bufferPop.action.value( bufferPop.value )}, \mouseDownAction);
		
		
		ldSndsGBufferList = { arg argPoolName, firstpool=false;	
			poolName = argPoolName.asSymbol;
			if ( try { XQ.globalBufferDict.at(poolName)[0] } != nil, {
				sndNameList = [];
				bufferList = List.new;
				XQ.globalBufferDict.at(poolName)[0].do({arg buf, i;
					sndNameList = sndNameList.add(buf.path.basename);
					bufferList.add(buf.bufnum);
				 });
				 bufferPop.items_(sndNameList); // get all global sounds and put them into the list
				 // put the first file into the view and load buffer (if first time)
				 if(firstpool, { { bufferPop.action.value(0) }.defer(0.5) }); // load BUFFER with delay!!!!!!!!

			}, {
				"got no files".postln;
				sndNameList = [];
			});
		};

		ldSndsGBufferList.value(selbPool.items[0].asSymbol, true); // GET BUFFERPOOL??
		
		/*
		ldSndsGBufferList = {arg argPoolName;
				poolName = argPoolName.asSymbol;
					if(try {XQ.globalBufferDict.at(poolName)[0] } != nil, {
						sndNameList = [];
						bufferList = List.new;
						XQ.globalBufferDict.at(poolName)[0].do({arg buff;
							sndNameList = sndNameList.add(buff.path.basename);
							bufferList.add(buff.bufnum);
						// assign random buffer to each drop
							//soundFuncArray.do({|dict| dict.buffer = sndNameList.size.rand });
						});
						 bufferPop.items_(sndNameList);
						 bufferPop.action.value(0); // put the first file into the view and load buffer
				}, {
					sndNameList = [];
				});
			};

		ldSndsGBufferList.value(selbPool.items[0].asSymbol);
		*/
		
		// num of layers //
		numOfLayersButton = PopUpMenu.new(win, Rect(70, 54, 40, 16))
			.font_(style.normal)
			.items_(["1", "2", "3", "4", "5", "6", "7", "8"])
			.value_(7)
			.canFocus_(false)
			.background_(Color.white)
			.action_({ arg item;
				numOfLayers = item.value + 1; // the position in array + 1
				//XiiSlicerBox.gNumLayers = numOfLayers;
				this.reposition; //set new situation
				("new num of layers is" + numOfLayers).postln;
				//this.changeNumOfLayers;
			});


			
		StaticText( win, Rect( 10, numOfLayersButton.bounds.top-7, 50, 30) )
			.font_(style.normal)
			.string_("layers"); //

		// random situations //
		randButt = Button(win, Rect(10, 80, 100, 16))
			.states_([["random state", Color.black, Color.clear]])
			.font_(style.normal)
			.canFocus_(false)
			.action_({ arg butt; this.randAll });

		// random nodes
		randNodesButt = Button(win, Rect(10, 100, 100, 16))
			.states_([["random nodes", Color.black, Color.clear]])
			.font_(style.normal)
			.canFocus_(false)
			.action_({ arg butt; this.randNodes });

		// random boxes
		randBoxesButt = Button(win, Rect(10, 120, 100, 16))
			.states_([["random slices", Color.black, Color.clear]])
			.font_(style.normal)
			.canFocus_(false)
			.action_({ arg butt; this.randBoxes });






		// states
		statesPop = GUI.popUpMenu.new(win, Rect(10, 150, 100, 16))
			.font_(style.normal)
			.items_( if(stateDict.size > 0, { stateDict.keys.asArray.sort }, { ["states"] }) )
			.value_(0)
			.background_(Color.white)
			.action_({ arg item; var newdict; // var chosenstate;
				stateNum.postln;
				if(stateNum > 0, { // if there are any states : RESTORE
					//stateDict.keys.postln;
					/*stateDict.keys.do({ arg key;
						("is"+item.items[item.value]+"equal to"+key).postln;
						(item.items[item.value].asSymbol == key).postln;
					});*/
					//stateDict.at(item.items[item.value].asSymbol).postln;
	
					this.initialise(stateDict.deepCopy, item.items[item.value].asSymbol);
				});
			});
			
		clearButt = GUI.button.new(win, Rect(10, 170, 47, 18))
			.canFocus_(false)
			.font_(style.normal)
			.states_([["clear", Color.black, Color.clear]])
			.action_({arg butt;
				stateNum = 0;
				stateDict = ();
				statesPop.items_(["states"]);
			});

		storeButt = GUI.button.new(win, Rect(63, 170, 47, 18))
			.canFocus_(false)
			.font_(style.normal)
			.states_([["store", Color.black, Color.clear]])
			.action_({arg butt;// var statesarray;
				stateNum = stateNum + 1;
				statesPop.items_(Array.fill(stateNum, {|i| "state "++(i+1).asString}));
				statesPop.value_(stateNum-1);

				params = [ 
					this.getPositions.deepCopy, 
					this.getColors.deepCopy,
					pitchRangeLimits.deepCopy, 
					pitchRange.deepCopy, 
					numOfLayers.copy, 
					rate.copy, 
					start.copy, 
					length.copy,
					shift.copy, 
					amp.copy, 
					lowGraphicsMode.copy, 
					buffer.copy, 
					outbus.copy 
				];

				//("stored amp is"+params[9]).postln;
				
				stateDict.add(("state "++stateNum.asString).asSymbol -> params.copy);
				//soundFuncArray = soundFuncArray.deepCopy; // make a new stamp of sndfarray
			});







		
		// RATE range
		StaticText(win, Rect(10, 212, 100, 16))
					.font_(style.normal)
					.string_("Rate range");
	
		rateRangeSl = RangeSlider(win, Rect(10, 230, 100, 12))
 			.mouseUpAction_({ arg view, x, y;
					this.setRate(nodes[0].loc.y); // update pitch 
					players.do({ arg p; p.set(\rate, rate) });	// update all
				})
			.action_({ arg sl;
				this.setRateRange(
					pitchRangeLimits[0] + (sl.lo.round(0.001) * pitchRangeLimits[1]),  
					pitchRangeLimits[0] + (sl.hi.round(0.001) * pitchRangeLimits[1])
				);
				lowRateLabel.string = pitchRange[0];
				hiRateLabel.string = pitchRange[1];
			});
		
		lowRateLabel = StaticText(win, Rect(10, 240, 100, 16))
					.font_(style.normal)
					.string_(pitchRange[0]);
				
		hiRateLabel = StaticText(win, Rect(10, 240, 100, 16))
					.font_(style.normal)
					.string_(pitchRange[1])
					.align_(\right); // RIGHT ALIGN

		ampSpec = ControlSpec(0, 1, \linear, 0, 0); // min, max, mapping, step
		// AMPLITUDE 
		globalVolSl = OSCIISlider(win, Rect(10, 290, 100, 12), "- vol", 0, 1, 1, 0.01, \amp)
				.font_(style.normal)
				.canFocus_(false)
				.action_({ arg sl; 
					amp = sl.value;
					//amp =  ampSpec.map(sl.value);
					//XiiSlicerBox.gAmp = amp; // inform them
					//boxes.collect(_.updateSynths); //they know how to calculate each's amp
					boxes.do({ arg b; b.updateSynths(amp/numOfLayers) });
					//("global amp is"+amp).postln;
				});

		outbusPop = PopUpMenu.new(win, Rect(10, 325, 50, 16))			
				.font_(style.normal)
				.items_(XiiACDropDownChannels.getStereoChnList)
				.value_(0)//params[8]/2)
				.canFocus_(false)
				.background_(Color.white)
				.action_({ arg ch;
					outbus = ch.value * channels;
					try{ players.do({ arg i; i.set(\outbus, outbus) }) };
				});
		
		startButt = Button(win, Rect(65, 325, 45, 16))
				.states_([["start", Color.black, Color.clear],
						["stop", Color.black, XiiColors.onbutton]])
				.font_(style.normal)
				.canFocus_(false)
				.action_({ arg butt; 
					if ( butt.value == 1, { this.initAll }, { this.closeAll } )
				});

		




		startLabel = StaticText(win, Rect(10, 360, 100, 16))
					.font_(style.normal)
					.string_("start :");

		lengthLabel = StaticText(win, Rect(10, 375, 100, 16))
					.font_(style.normal)
					.string_("length :");		

		shiftLabel = StaticText(win, Rect(10, 390, 100, 16))
					.font_(style.normal)
					.string_("shift :");
		
		pitchLabel = StaticText(win, Rect(10, 405, 100, 16))
					.font_(style.normal)
					.string_("rate :");


		lowGraphicsButt = Button(win, Rect(10, 430, 100, 16))
				.states_([["set low-fi graphics", Color.black, Color.clear],
						["set hi-fi graphics", Color.black, XiiColors.onbutton]])
				.font_(style.normal)
				.canFocus_(false)
				.action_({ arg butt; 
					lowGraphicsMode = butt.value;
					displays.do({ arg i; i.lowGraphicsMode = lowGraphicsMode });
					boxes.do({ arg i; i.lowGraphicsMode = lowGraphicsMode });
					selection.lowGraphicsMode = lowGraphicsMode; 

					if ( lowGraphicsMode == 1, { Pen.smoothing_(false) }, {Pen.smoothing_(true)} ); 
				});
		//  end GUI description



		// INITAL STATE //
		this.initialise(setting);

		// FINALLY
		win.front;
		win.view.focus(true);
		win.onClose_({ // must be passed within brackets
			var t;
			("closing Slicer" + instanceindex).postln;
			slicerindex = nil; // only if single Slicer copy mode
			this.closeAll; 
			XQ.globalWidgetList.do({arg widget, i; if(widget == this, { t = i })}); //get position in array
			try{ XQ.globalWidgetList.removeAt(t) }; 
		});
	}
}


    
   
