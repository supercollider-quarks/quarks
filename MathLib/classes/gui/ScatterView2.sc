// 2008 by till bovermann
//   bielefeld university

ScatterView2 {
	var <plot, <>background;

	var <>colorFunc;
	var <>itemSize = 2, <>drawMethod = \lineTo;

	//var <>highlightColor, <highlightItem, <>highlightSize, <>isHighlight, <>drawAxis, <>drawValues;
	//var <>xAxisName, <>yAxisName;

	var  normedData, specX, specY, numItems;

	// selection
	var <>selectModes;
	var <>selectionMode = \nextNeighbour; // \surrounding
	var <>selectRegion = 0.1; // [0..1]
	var selected;
	var selectionRect;
	var <>action;

	var <>mouseDownAction, <>mouseUpAction, <>mouseMoveAction, <mouseOverAction;	

	*new {|parent, bounds, data, specX, specY|
		^super.new.initPlot(
			parent, bounds, data, specX, specY
		)
	}

	mouseOverAction_{|func|
		plot.mouseOverAction = func;
	}
	refresh {
		plot.refresh;
	}
	data_{|data|
		normedData = data.collect {|item|
			[specX.unmap(item[0]), specY.unmap(item[1])];
		};
		numItems = normedData.size;
	}
	canFocus_ { arg state = false;
		plot.canFocus_(state);
	}
	canFocus {
		^plot.canFocus;
	}

	visible_ { arg bool;
		plot.visible_(bool)
	}
	visible {
		^plot.visible
	}
	select{|indices|
		this.prSelect(indices);
		plot.refresh;
	}
	selection{
		^selected.selectIndex{|elem| elem};
		//{|elem|elem}
	}
	prSelect{|indices|
		selected = false!numItems;
		indices.asArray.do{|idx|
			selected[idx] = true;
		};
	}
	initPlot {|parent, bounds, data, argSpecX, argSpecY|
		
		specX = argSpecX ? [0,1].asSpec;
		specY = argSpecY ? specX.copy;
		this.data_(data);

		colorFunc = {|idx, selected| selected.if({Color.red}, {Color.red(0.5, 0.5)})};
		background = Color.white;

		selectModes = IdentityDictionary[
				\nextNeighbour -> {|view, data, pos, itemSize|
					var threshold, index, selected;

					threshold = itemSize*2;
					index = data.detectIndex{|item|
						(item - pos).abs.sum <= threshold;
					};
					index
				},
				\surrounding -> {|view, data, pos, itemSize, region|
					var threshold, selected;
					threshold = region;
					selected = data.selectIndex{|item|
						(item - pos).squared.sum.sqrt <= threshold;
					};
					selected
				}
		];

		selected = false!numItems;

		plot = GUI.userView.new(parent,bounds).relativeOrigin_(true);

		plot.drawFunc = {|view|
			var mult = view.bounds.extent.asArray;

			// clipping into the boundingbox
			GUI.pen.addRect( view.bounds.moveTo( 0, 0 ));
			GUI.pen.clip;
			// draw Background
			GUI.pen.color = background;
			GUI.pen.addRect( view.bounds.moveTo( 0, 0 ));
			GUI.pen.fill;


			normedData.do{|pos, i|
				GUI.pen.color = colorFunc.value(i, selected[i]);
				GUI.pen.fillRect(((pos * mult  - (itemSize*0.5)) ++ (itemSize!2)).asRect)
			};

			// draw selection
			selectionRect.notNil.if{
				GUI.pen.color = Color.gray(1, 0.3);
				GUI.pen.fillRect(selectionRect);
				GUI.pen.color = Color.blue(0.3, 0.6);
				GUI.pen.strokeRect(selectionRect);
			}
		};
		plot.mouseDownAction = {|view, x, y, modifiers, buttonNumber, clickCount|
			var rMult, pos, normedPos, normedItemSize, threshold, index;
	
			(clickCount == 1).if{
				pos = [x, y]-view.bounds.leftTop.asArray;
				
				rMult = view.bounds.extent.asArray.reciprocal;
				normedPos = pos * rMult;
				normedItemSize = itemSize * rMult;
				normedItemSize = min(normedItemSize[0], normedItemSize[1]);
	
				this.prSelect(selectModes[selectionMode].value(
					view, 
					normedData, 
					normedPos, 
					normedItemSize,
					selectRegion
				));
			};			
			mouseDownAction.value(this, x, y, modifiers, buttonNumber, clickCount);		};
		// draw a rect
		plot.mouseMoveAction = {|view, x, y, modifiers|
			var pos;
			
			pos = [x, y]-view.bounds.leftTop.asArray;
	
			selectionRect.isNil.if({
				selected = false!numItems;
				selectionRect = (pos++[0, 0]).asRect;
			},{
				selectionRect = Rect.newSides(
					selectionRect.left,
					selectionRect.top,
					pos[0],
					pos[1]
				);
				plot.refresh;
			});
			mouseMoveAction.value(this, x, y, modifiers);
		};
		plot.mouseUpAction = {|view, x, y, modifiers|
			var mult = view.bounds.extent.asArray;
	
			selectionRect.notNil.if{
				if(selectionRect.width<0){
					selectionRect.left = selectionRect.left + selectionRect.width;
					selectionRect.width = selectionRect.width.neg;
				};
				if(selectionRect.height<0){
					selectionRect.top = selectionRect.top + selectionRect.height;
					selectionRect.height = selectionRect.height.neg;
				};
	
				selected = normedData.collect{|dat|
					selectionRect.containsPoint((dat*mult).asPoint)
				};
			};
			selectionRect = nil;
			plot.refresh;
			action.value(
				selected.selectIndex{|item, i|
					item
				}
			);
			mouseUpAction.value(this, x, y, modifiers)
		};
	}
}
