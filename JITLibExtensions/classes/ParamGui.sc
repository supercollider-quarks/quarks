// sketch for a smarter envirgui:
// can have an ordered List of param names,
// uses Halo to access Tdef's own specs.
// even a little window to reorder.
// could eventually be used in TdefGui/PdefGui.

ParamGui : EnvirGui {

	var <>orderedNames, <>filterNames, <>showNewNames = true;

	reorderWindow { |alpha = 0.75|
		var w, drags, zoneBounds, winBounds, height, width = 160;

		if ( this.orderedNames.isNil) { this.grabOrdered };

		zoneBounds = this.zone.absoluteBounds;
		height = max(
			this.zone.absoluteBounds.height,
			this.orderedNames.size + 0.5 * skin.buttonHeight
		);

		winBounds = Window.flipY(zoneBounds)
		.width_(width).height_(height);

		w = Window("reorder:", winBounds).front;
		w.alpha = alpha;
		w.addFlowLayout(skin.margin, skin.gap);
		drags = orderedNames.collect { |key, i|
			DragBoth(w, Rect(10,10, width - 4, skin.buttonHeight))
			.object_(key).align_(\center)
			.receiveDragHandler = { arg obj;
				this.moveName(View.currentDrag, i);
				this.orderedNames.do { |key, i| drags[i].object_(key) };
			};
		};
		^w
	}

	// support specs from Halo spec envir

	useHalo { |haloObject, myNames = false|

		if (haloObject.isNil) {	^this };

		super.useHalo(haloObject);

		orderedNames = haloObject.getHalo(\orderedNames);

		// only change obj halo if flag was set;
		// as name ordering is implemented here,
		// it makes sense to send the names made here to the halo.
		if (myNames or: orderedNames.isNil) {
			this.grabOrdered;
			haloObject.addHalo(\orderedNames, orderedNames);
		};
	}

	// support for orderedNames:

	// // make it a dragboth for reordering by hand?
	// // simpler to keep it separate in reorderWindow
	// makeNameView { |nameWid, height|
	// 	nameView = DragBoth(zone, Rect(0,0, nameWid, height))
	// 	.font_(font).align_(0);
	// }

	grabOrdered {
		orderedNames = nil;
		orderedNames = this.getState[\editKeys];
	}

	moveName { |name, index|
		orderedNames.remove(name);
		orderedNames.insert(index, name);
	}

	hideName { |name| filterNames = filterNames.add(name); }
	showName { |name| filterNames !? { filterNames.remove(name) }; }

	getState {
		var rawKeys, overflow, newKeys, currSpecs;

		if (object.isNil) { ^(editKeys: [], overflow: 0, keysRotation: 0) };

		rawKeys = object.keys;

		if (orderedNames.isNil) {
			newKeys = rawKeys.asArray.sort;
		} {
			newKeys = [];

			filterNames.do { |name| rawKeys.remove(name) };

			orderedNames.do { |name|
				if (rawKeys.includes(name)) {
					rawKeys.remove(name);
					newKeys = newKeys.add(name);
				};
			};

			if (rawKeys.notEmpty and: showNewNames) {
				newKeys = newKeys ++ rawKeys.asArray.sort;
			};
		};

		overflow = (newKeys.size - numItems).max(0);
		keysRotation = keysRotation.clip(0, overflow);
		newKeys = newKeys.drop(keysRotation).keep(numItems);
		currSpecs = newKeys.collect{ |key| [key, this.getSpec(key, object[key])] };

		^(
			object: object,
			editKeys: newKeys,
			overflow: overflow,
			keysRotation: keysRotation,
			specs: currSpecs
		)
	}
}
