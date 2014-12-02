

EditorGui : ObjectGui {

	writeName {}
}


NumberEditorGui : EditorGui {

	var numv,slv;

	smallGui { arg layout;
		var l;
		l=this.guify(layout);
		this.box(l,Rect(0,0,40,GUI.skin.buttonHeight));
		if(layout.isNil,{ l.front });
	}
	guiBody { arg layout,bounds,slider=true, box=true;
		var h,w,p,gap;
		bounds = (bounds ?? {layout.indentedRemaining}).asRect;
		gap = GUI.skin.gap;
		// massive space,
			// box, slider horz
		w = bounds.width;
		h = bounds.height;
		if(w >= 140 and: {h >= GUI.skin.buttonHeight},{
			h = min(h, GUI.skin.buttonHeight);
			w = min(w, GUI.skin.buttonHeight * 16);
			if(box, { this.box(layout,Rect(0,0,40,h)); });
			if(slider,{ this.slider(layout,Rect(0,0,w-40-(gap.x*2),h)); });
			^this
		});
		// width < height
			// go vert
		if(w < h,{
			// height > 100
				// box, slider
			if(h > 100 and: {w >= 30},{
				h = h.max(130);
				layout.comp({ |l|
					var y;
					this.box(l,Rect(0,0,min(70,w)-(gap.x),y = GUI.skin.buttonHeight));
					y = y + gap.y;
					this.slider(l,Rect(0,y,min(70,w)-(gap.x),h-y-gap.y));
				},Rect(0,0,w-gap.x,h-gap.y))
				^this
			});
			if(h > 100 ,{
				this.slider(layout,Rect(0,0,w-gap.x,h-gap.y));
				^this
			});
			// height < 100, > 30
				// slider
			if(h >= 30,{
				this.slider(layout,Rect(0,0,min(40,w),h));
				^this
			});

			// height < 30
				// box
			if(h <= 30,{
				this.box(layout,Rect(0,0,min(40,w),h));
				^this
			});

		},{// width > height
			h = min(h, GUI.skin.buttonHeight);

			// width > 100
				// box, slider

			// width < 100, > 30
				// slider
			if(w.inclusivelyBetween(30,100),{
				if(slider,{
					this.slider(layout,Rect(0,0,w,h));
				},{
					this.box(layout,Rect(0,0,w,h));
				});
				^this
			});

			// width < 30
			if(w <= 30,{
				// box
				if(box,{
					this.box(layout,Rect(0,0,w,h));
				},{
					this.slider(layout,Rect(0,0,w,h));
				});
				^this
			});
		});

		// any unmatched
		if(slider,{
			this.slider(layout,Rect(0,0,w,h));
		},{
			this.box(layout,Rect(0,0,w,h));
		});
		^this
	}
	box { arg layout,bounds;
		var r,startValue,range,mod,startPoint;
		numv = NumberBox(layout,bounds)
			.value_(model.poll)
			.focusColor_(Color.yellow(1.0,0.5))
			.font_(Font("Helvetica",10))
			.action_({ arg nb;
				model.activeValue_(model.spec.constrain(nb.value)).changed(numv);
			});
		numv.mouseDownAction = { arg view,x, y, modifiers, buttonNumber, clickCount;
			if(modifiers.isAlt,{
				model.activeValue_(model.spec.default).changed
			},{
				startValue = model.unmappedValue;
				mod = modifiers;
				startPoint = 0@0;
			})
		};
		numv.mouseMoveAction = { arg view,x,y,modifiers;
			var move,val,unimove;
			if(modifiers != mod,{
				mod = modifiers;
				startValue = model.unmappedValue;
				startPoint = x@y;
			});
			if(modifiers.isCtrl,{
			    move = (y - startPoint.y).neg;
				if(modifiers.isShift,{
					range = 4000.0;
				},{
					range = 300.0;
				});
				move = move.clip(range.neg,range);
				unimove = move.abs.linlin(0.0,range,0.0,1.0);
				if(move > 0,{
				    val = (startValue + unimove);
				},{
				    val = (startValue - unimove);
				});
				model.setUnmappedValue( val.clip(0.0,1.0) );
			});
		};
		numv.scroll = false;
		numv.clipLo = model.spec.clipLo;
		numv.clipHi = model.spec.clipHi;
		numv.focusColor = GUI.skin.focusColor ?? {model.spec.color};
		/*numv.keyDownAction = { arg char,modifiers,unicode,keycode;
			if("012356789-.".includes(char),{
				this.defaultKeyDownAction(char, modifiers, unicode, keycode);
			},{
				nil
			})
		};*/
		//if(consumeKeyDowns,{
		//	numv.keyDownAction = {nil};
		//});
	}
	slider { arg layout, bounds;
		var r;
		slv = GUI.slider.new(layout, bounds);
		slv.focusColor = GUI.skin.focusColor ?? {model.spec.color};
		slv.setProperty(\value,model.spec.unmap(model.poll));
		slv.thumbSize = min(bounds.height,bounds.width) / 1.61803399;
		slv.action_({arg th;
			model.activeValue_(model.spec.map(th.value)).changed(slv)
		});
		if(consumeKeyDowns,{ slv.keyDownAction = {nil}; });
		/* but slider doesnt trigger mouseDownAction */
		slv.mouseDownAction = { arg view, x, y, modifiers, buttonNumber, clickCount;
			if(modifiers.isAlt,{
				model.activeValue_(model.spec.default).changed
			})
		}
	}
	update {arg changed,changer; // always has a number box
		{
			if(changer == 'spec',{
				if(numv.notNil,{
					numv.clipLo = model.spec.clipLo;
					numv.clipHi = model.spec.clipHi;
				})
			});					
			if(changer !== numv and: {numv.notNil} and: {numv.isClosed.not},{
				numv.value_(model.poll);
			});
			if(changer !== slv and: {slv.notNil} and: {slv.isClosed.not},{
				slv.value_(model.spec.unmap(model.poll));
			});
			nil
		}.defer;
	}
	background { ^Color(0.0,0.2,0.2,0.2) }
}


KrNumberEditorGui : NumberEditorGui {

	background { ^Color(0.0,0.3,0.0,0.2) }
}


PopUpEditorGui : EditorGui {

	var popV;
	    // temp, I don't really have a spec here
        // we arent editing a "pop up", so the class is misnamed
        // just to get this gui representation
        // maybe NumberEditor should use this gui if it has a named integers spec

	guiBody { arg layout;
		var horSize;
		horSize = model.labels.maxValue({arg item; item.size }) * 12;
		popV = PopUpMenu(layout,Rect(0,0,horSize,GUI.skin.buttonHeight))
			.items_(model.labels)
			.action_({ arg nb;
				model.selectByIndex(popV.value).changed(this)
			});
		popV.focusColor = GUI.skin.focusColor ?? {Color.grey(0.5,0.5)};
		popV.background = GUI.skin.background;
		if(consumeKeyDowns,{ popV.keyDownAction = {nil}; });
		popV.value = model.selectedIndex
	}
	update { arg changed,changer;
		if(changer !== this,{
			popV.value = model.selectedIndex;
		});
	}
}


BooleanEditorGui : EditorGui {

	var cb;

	guiBody { arg layout,bounds;
		var bg,b,skin;
		skin = GUI.skin;
		bounds = (bounds ?? { layout.bounds; }).asRect;

		b = Rect(0,0,skin.buttonHeight,skin.buttonHeight);
		if(bounds.notNil,{
			if(b.width > bounds.width,{
				b.width= bounds.width;
				b.height = bounds.width;
			});
			if(b.height > bounds.height,{
				b.width = bounds.height;
				b.height = bounds.height;
			});
		});
		cb = Button( layout,b);
		cb.states = [[" ",Color.black,skin.offColor],[" ",Color.black,skin.onColor]];
		cb.font = Font(*skin.fontSpecs);
		cb.setProperty(\value,model.value.binaryValue);
		cb.focusColor = GUI.skin.focusColor ?? {Color.clear};
		cb.action = { model.activeValue_(cb.value != 0,this) };
		if(consumeKeyDowns,{ cb.keyDownAction = {nil}; });
	}
	update { arg changed,changer;
		if(changer !== this,{
			cb.setProperty(\value,model.value.binaryValue);
		});
	}	
}


DictionaryEditorGui : EditorGui {
	
	var <>onSave;
	
	guiBody { arg layout,bounds,onSave;
		this.onSave = onSave;
		model.editing.keysValuesDo { arg k,v;
			layout.startRow;
			ArgNameLabel(k,layout,minWidth:100);
			v.gui(layout)
		};
		if(this.onSave.notNil,{
			ActionButton(layout.startRow,"SAVE",{
				this.onSave.value(model.value)
			})
		})
	}
}

