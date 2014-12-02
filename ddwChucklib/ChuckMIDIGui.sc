
MTGui : HJHObjectGui {
	classvar	<>dragWidth = 80, <>dragHeight = 15, <>rows = 12,
			<>gap = 2,
			<>fontSpecs = #["Helvetica", 10];
	classvar	>font, <backColors, blackKeys;

	var	<mainView,
		<chanText,	// which channel?
		<dragViews,	// one per key
		minNoteFloor, maxNoteCeil;	// to position views correctly

	*initClass {
		StartUp.add({
//			font = GUI.font.new("Helvetica", 10);	// nice and small
			backColors = (
				ready: [Color.new255(181, 196, 255), Color.new255(136, 147, 191)],  // baby blue
				playing: [Color.new255(69, 255, 23), Color.new255(52, 191, 17)],  // green
				late: [Color.yellow, Color.yellow(0.75)],
				driven: [Color.new255(214, 191, 255), Color.new255(161, 143, 191)],  // lavender
				idle: [Color.gray(0.75), Color.gray(0.5)]
			);
			blackKeys = #[1, 3, 6, 8, 10];
		});
	}
	
		// you might have switched to a different gui scheme since initializing the class library
	*font {
		(GUI.font === font.class).if({ ^font }, {
			^(font = GUI.font.new(*fontSpecs))
		});
	}

	guiBody { |lay|
		var	size, cols, myRows, width, height, temp;
		layout = lay;

			// compute bounds
		minNoteFloor = model.minNote.trunc(rows);
		((maxNoteCeil = model.maxNote.trunc(rows)) != model.maxNote).if({
			maxNoteCeil = maxNoteCeil + rows;
		});
		size = maxNoteCeil - minNoteFloor + 1;
		myRows = rows;
		cols = (size / rows).floor;	// one column per octave
			// octave + 1 should not create a new column, but it needs an extra row
			// if > octave+1, need an extra column
		temp = (size - (cols*rows + 1)).sign;
			// case here ignores temp == -1
		case	{ temp == 0 } { myRows = myRows + 1 }
			{ temp == 1 } { cols = cols + 1 };
		width = dragWidth * cols + gap + (gap*cols);
			// include an extra row for the header
		height = dragHeight * (myRows+1) + (gap*2) + (gap*myRows);

		mainView.isNil.if({	// if this gui is already open, don't recreate
			{	mainView = GUI.compositeView.new(layout, argBounds ?? { Rect(0, 0, width, height) })
					.onClose_({ this.remove });
//			try { mainView.relativeOrigin_(layout.relativeOrigin) };

			chanText = GUI.staticText.new(mainView,
				Rect(gap+mainView.bounds.left, gap+mainView.bounds.top, width-gap, dragHeight))
				.string_("MT(" ++ model.collIndex.asShortString ++ ")")  // midi channel index
				.font_(this.class.font).align_(\center);

				// like mixingboard -- create them and index them, then place them
			dragViews = Array.fill(model.maxNote - model.minNote + 1, { |i|
				temp = this.makeDragView;
				this.emptyView(temp, i);
				temp
			});

			this.positionViews(myRows, cols)
				.populateViews;
			}.defer;
		});
	}

	makeDragView {
		^GUI.dragBoth.new(mainView, Rect(0, 0, dragWidth, dragHeight))
			.font_(this.class.font)
			.beginDragAction_({ |drag| drag.object })
			.action_({ |sink|
				var index;
					// dragging an object from a dragboth into itself is like clicking on it
					// this should trigger the model
				index = dragViews.indexOf(sink) + model.minNote;
				(model.v[index].notNil and: { model.v[index].bp === sink.object }).if({
					model.v[index].ready = 1;
					model.noteOn(index);  // trigger
				}, {
						// else reassign
					(sink.object.tryPerform(\draggedIntoMTGui, this, index) != true).if({
							// if inappropriate class, reset view
						model.v[index].isNil.if({
							this.emptyView(sink, index - model.minNote);
						}, {
							this.populateView(model.v[index])
						});
					});
				});
			})
	}
	
	positionViews { |r, c|
		var	x, y, i, j, top, left;
		top = mainView.bounds.top;
		left = mainView.bounds.left;
		i = 0;
		j = model.minNote - minNoteFloor;
		x = gap;
			// if minNote is not a multiple of 12, first y position needs to be adjusted
		y = gap*2 + dragHeight + ((dragHeight + gap) * j);
		{
		dragViews.do({ |view|
			view.bounds_(view.bounds.moveTo(x + left, y + top));
			((y = y + dragHeight + gap; j = j+1) >= rows).if({
				((i = i+1) < c).if({
						// advance to next col, if we haven't hit the end of the cols
						// otherwise, leave position as is (oct+1)
					y = gap*2 + dragHeight; x = x + dragWidth + gap; j = 0;
				});
			});
		});
		}.defer;
	}
	
	populateViews {
		model.value.keysValuesDo({ |k, v|
			this.populateView(v);
		});
	}
	
	populateView { |entry|
		{ dragViews[entry.noteNum - model.minNote].object_(entry.bp)
			.string_(" " ++ entry.asString)
			.background_(backColors[entry.playState]
				[blackKeys.includes(entry.noteNum % 12).binaryValue])
		}.defer;
	}
	
	emptyView { |view, index|
		{ view.string_(" " ++ (index + model.minNote).asMIDINote)
			.background_(backColors[\idle]
				[blackKeys.includes(index % 12).binaryValue])
		}.defer;
	}
	
	update { |mt, mtEntry|
		case { mtEntry == \free } { this.remove; }
			{ mtEntry.isNumber }
				{ this.emptyView(dragViews[mtEntry-model.minNote], mtEntry-model.minNote) }
			{ this.populateView(mtEntry); };
	}
	
	writeName {}	// from cruxxial: don't want it, don't need it

	remove {
		mainView.notNil.if({
			mainView.notClosed.if({		// check for window closed
				{ mainView.remove; }.defer;
			});
			mainView = chanText = dragViews = nil;
			model.removeDependant(this);
		});
	}
		
}
