/*	Sandrode - simulates Touch Sandrodes as used in CircuitBending
	requires the MultiTouchPad quark by Batuhan Bozkurt.
	Inspired by Peter Blasser's magical Fyrall, Fourses, et al.



c = Sandrode(\a, 0.5 @ 0.5, 0.2);
c.dump;
w = Window.new.front;
c.drawOn(w.view);
c.sectArea(Rect(0.5, 0.5, 0.1, 0.1)); // 0.25

*/


Sandrode { 
	
	classvar <all, <>verbose = false, <allTouching, <auras;
	classvar <>sameWeight = 3, <>otherWeight = 1; 
	classvar <> keyCmds; 
	
	var <name, <center, <size, <rect, <rectArea, <textView;
	var <touching, <aura; 
	var <weights;
	
	*initClass {
		all = ();
		allTouching = ();
		auras = ();
		keyCmds = (
			$.: { MultiTouchPad.stop },
			$ : { MultiTouchPad.start },
			$m: { |w|
					w.bounds_(Window.screenBounds); 
					Sandrode.drawAll(w.view, 0.02);
				},
			$x: { |w|
				// w.bounds_(Rect(100, 100, 525, 375));
				w.bounds_(Rect(480, 5, 400, 225));
				Sandrode.drawAll(w.view);
			},		
			$d: { |w| Sandrode.drawAll(w.view) }
		);
	}

	*drawAll {|view, dt = 0.02| 
		fork ({ dt.wait; all.do (_.drawOn(view)) }, AppClock);
	}

	*addMtpTouch { |id, xys| 
		all.do(_.addMtpTouch(id, xys)); 
		this.readAuras;
		if (verbose) { this.postTouch;  this.postAuras; }
	}
	
	*removeTouch { |id| 
		all.do(_.removeTouch(id));
		this.readAuras;
		if (verbose) { this.postTouch;  this.postAuras; }
	}
	
	*postTouch { 
		"\n////////// Sandrode.allTouching: ////////// ".postln;
		allTouching.postln;
	}
	*postAuras { 
		"\n////////// Sandrode.auras: ////////// ".postln;
		auras.postln;
	}
	
		// calc how strongly each sandrode influences each other
	*readAuras {	
			// clear untouched ones:
		allTouching.keysValuesDo { |sand1, touch1| 
			if (touch1.isEmpty) { 
				auras.do(_.removeAt(sand1));
				auras[sand1].clear;
			};
		};
				// which fingers touch first sandrode?
		allTouching.keysValuesDo { |sand1, touch1| 
				// which are on second?
			allTouching.keysValuesDo { |sand2, touch2|
					// same sandrode does not count 
					// would be radio pickup-type influence, 
					// but we can do this from touching already
				if (sand1 != sand2) { 
					// store the influences for k1 <-> k2 in both directions
					touch1.keysValuesDo { |fing1, infl1| 
						touch2.keysValuesDo { |fing2, infl2| 
								// more weight if same finger touches both
							var weight = if (fing1 == fing2, sameWeight, otherWeight);
							var infl = (infl1 * infl2).sqrt * weight;
							auras[sand2].put(sand1, infl);
							auras[sand1].put(sand2, infl);	
						}
					}
				}
			}
		}
	}
	
	*new { |name, center, size|
		^super.newCopyArgs(name, center, size.asPoint).init;
	}
	
	init { 
		rect = Rect.aboutPoint(center, size.x * 0.5, size.y * 0.5);
		rectArea = rect.width * rect.height; 
		touching = ();
		aura = ();

		all.put(name, this);
		allTouching.put(name, touching);
		auras.put(name, aura);

			// sketch: how sensitive is this touch to which others?
		weights = ();
	}
		
	drawOn { |view| 
		var width = view.bounds.width;
		var height = view.bounds.height;
		var drawrect = Rect(
			(width * rect.left), 
			height * rect.top, 
			width * rect.width,
			height * rect.height
		);
		try { textView.remove };
		
		textView = StaticText(view, drawrect)
			.font_(Font("Helvetica", 14))
		.string_(name.asString)
		.align_(\center)
		.background_(Color.grey(0.1, 0.2));
	}
	
	sectArea { |rect2|
		var xrect = rect.sect(rect2);
		^xrect.width.clip(0, 1) * xrect.height.clip(0, 1) 
			/ rectArea;
	}
		
	addMtpTouch { |id, xys|
		var x, y, size, fingrect, sectarea; 
		#x,y,size = xys;
		size = size / 50; // (measured: width 0.1 => size 2.5)
		fingrect = Rect.aboutPoint(x@y, size, size);
		sectarea = this.sectArea(fingrect); 
		if (sectarea > 0) { 
			this.addTouch(id, sectarea.round(0.0001));
		} { 
			touching.removeAt(id);
		};
	}
	
	addTouch { |id, area| touching.put(id, area); }
	
	removeTouch { |id| touching.removeAt(id) }

		// utils for MTP 
		
		// would be nice to get the oval major and minor axes and angle from tongsengmod!
	*mtpDrawFunc { 
		^{ |uv|
			var bounds = uv.bounds;
			var width = bounds.width;
			var height = bounds.height;
			var fingerScale = height * 0.025; 
			var winSize = (width @ height);
			
			Pen.color = Color.red(1.0); 
			MultiTouchPad.fingersDict.do ({|finger|
				var fingerSize = finger[2] * fingerScale;
				Pen.width_(fingerSize.sqrt);

				Pen.strokeOval(
					Rect.aboutPoint(
						(width * finger[0]) @ (height * finger[1]),
						fingerSize, 
						fingerSize
					)
				);
			}); 
			// draw connections:
			Pen.color = Color.blue(1.0);
			Pen.capStyle_(1); // round
			Sandrode.auras.keysValuesDo { |sand1, aura|
				aura.keysValuesDo {	 |sand2, stren| 
					Pen.width_(stren * 5);
					Pen.line(Sandrode.all[sand1].center * winSize, Sandrode.all[sand2].center * winSize);
					Pen.stroke;
				}
			};
		}
	}
	
	*keydownFunc { 
		^{ |view, key| 	// for some reason, the view handed in is the userView - WTF???
			var w = MultiTouchPad.guiWin;
			var func = keyCmds[key]; 
			func.value(w); 
		}
	}
}

