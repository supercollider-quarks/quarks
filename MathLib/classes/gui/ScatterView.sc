// 2005 by till bovermann
//   bielefeld university

ScatterView {
	var <plot, <background;
	var <>highlightColor, <highlightItem, <>highlightSize, <>isHighlight, <>drawAxis, <>drawValues;
	var <>xAxisName, <>yAxisName;
	var <symbolSize, <>symbolColor, <>drawMethod = \lineTo;
	var adjustedData, specX, specY;

	*new {|parent, bounds, data, specX, specY|
		^super.new.initPlot(
			parent, bounds, data, specX, specY
		)
	}
	background_ {|color|
		background = color;
	}
	highlightItem_{|item|
		highlightItem = item.isKindOf(Collection).if({item}, {[item]});
		//this.plot.refresh;
	}
	highlightItemRel_{|val|
		this.highlightItem = (val * (adjustedData.size-1)).asInteger;
	}
	highlightRange{|start, end|
		this.highlightItem = (start .. end);
	}
	highlightRangeRel{|start, end|
		var startIndex, endIndex;
		startIndex = (start * (adjustedData.size-1)).asInteger;
		endIndex = (end * (adjustedData.size-1)).asInteger;
		this.highlightItem = (startIndex .. endIndex);
	}
	refresh {
		plot.refresh;
	}
	data_{|data|
		adjustedData = data.collect {|item|
			[specX.unmap(item[0]), specY.unmap(item[1])];
		};
		highlightItem = min(highlightItem, (adjustedData.size-1));
	}
	symbolSize_{|point|
		if(point.isKindOf(Point), {symbolSize = point}, {symbolSize = (point@point)});
	}
	resize{
		^plot.resize;
	}
	resize_{arg resize;
		plot.resize_(resize)
	}
	initPlot {
		arg parent, bounds, data, argSpecX, argSpecY;
		
		var widthSpec, heightSpec, cross;
		var possibleMethods;
		
		specX = argSpecX ? [0,1].asSpec;
		specY = argSpecY ? specX.copy;
		possibleMethods = [\fillRect, \fillOval, \strokeOval, \strokeRect];
		
		this.symbolSize = 1;
		symbolColor = symbolColor ? Color.black;
		
		background = Color.white;
		highlightColor = Color.red;
		highlightItem = 0;
		highlightSize = (2@2);
		isHighlight = false;
		drawAxis = false;
		drawValues = false;
		xAxisName= "X";
		yAxisName= "Y";
		
		this.data_(data);
		
		cross = {|rect, width, color|
			var dx, dy, extX, extY;
			dx = rect.left;
			dy = rect.top;
			extX = rect.width;
			extY = rect.height;
			GUI.pen.use{
				GUI.pen.color = color;
				GUI.pen.translate(dx, dy);
				GUI.pen.moveTo((0)@(extY*0.5));
				GUI.pen.lineTo(extX@(extY*0.5));
				GUI.pen.moveTo((extX*0.5)@(0));
				GUI.pen.lineTo((extX*0.5)@(extY));
				GUI.pen.stroke;
			};
		};
		
		plot = GUI.userView.new(parent, bounds)
			.relativeOrigin_(false)
			.drawFunc_({|w|
				var width, height, rect, pad = 10;
				if (drawAxis) { pad = 60 };

				width =  w.bounds.width  - pad;
				height = w.bounds.height - pad;

				GUI.pen.use{
					// clipping into the boundingbox
					GUI.pen.moveTo((w.bounds.left)@(w.bounds.top));
					GUI.pen.lineTo(((w.bounds.left)@(w.bounds.top)) 
							+ (w.bounds.width@0));
					GUI.pen.lineTo(((w.bounds.left)@(w.bounds.top)) 
							+ (w.bounds.width@w.bounds.height));
					GUI.pen.lineTo(((w.bounds.left)@(w.bounds.top)) 
							+ (0@w.bounds.height));
					GUI.pen.lineTo((w.bounds.left)@(w.bounds.top));
					GUI.pen.clip;
					
					// draw Background
					GUI.pen.color = background;
					GUI.pen.addRect(w.bounds);
					GUI.pen.fill;
					
					// draw data
					GUI.pen.color = symbolColor;
					if (possibleMethods.includes(drawMethod), {
						GUI.pen.beginPath;
						adjustedData.do{|item, i|
							rect = Rect(
								(   item[0]  * width)  - (symbolSize.x/2) + (pad/2),
								((1-item[1]) * height) - (symbolSize.y/2) + (pad/2), 
								symbolSize.x, symbolSize.y
							);
							GUI.pen.perform(
								drawMethod,
								(Rect(w.bounds.left, w.bounds.top, 0, 0) + rect)
							);
						}
					},{ // else draw lines
						GUI.pen.width = symbolSize.x;
						// move to first position;
						if (adjustedData.notNil) {
							GUI.pen.moveTo(
								((adjustedData[0][0]  * width) + (pad/2))
								 @
								 (((1-adjustedData[0][1]) * height) + (pad/2))
							   	  + 
								  (w.bounds.left@w.bounds.top)
							);
						};
						// draw lines
						adjustedData.do{|item, i|
							GUI.pen.lineTo(
								((item[0]  * width) + (pad/2))
								 @
								 (((1-item[1]) * height) + (pad/2)) 
							 	  + 
							 	  (w.bounds.left@w.bounds.top)
							)
						};
						if(drawMethod == \fill, {GUI.pen.fill},{GUI.pen.stroke});
					});
					
					// highlight datapoint
					if(isHighlight) {
						highlightItem.do{|item|
							cross.value((Rect(
								(adjustedData[item][0]  * width) 
								 - 
								 (highlightSize.x/2) + (pad/2),
								((1-adjustedData[item][1]) * height) 
								 - 
								 (highlightSize.y/2) 
								  + 
								  (pad/2), 
								highlightSize.x, highlightSize.y) 
								  + 
								  Rect(w.bounds.left, w.bounds.top, 0, 0)),
								symbolSize,
								highlightColor
							);
						}; // od
					};
						// draw axis
					if (drawAxis) {
						GUI.pen.moveTo((w.bounds.left+(pad/2))@(w.bounds.top+(pad/2)));
						GUI.pen.lineTo((w.bounds.left+(pad/2))@(w.bounds.top+w.bounds.height-(pad/2)));
						GUI.pen.lineTo(
							(w.bounds.left-(pad/2)+w.bounds.width)@(w.bounds.top+w.bounds.height-(pad/2)));
						specX.minval.round(0.001).asString
							.drawAtPoint(
								(w.bounds.left+(pad/2)+10)@
								(w.bounds.height-(pad/2)+10));
						xAxisName
							.drawAtPoint(
								(w.bounds.left+(w.bounds.width/2))@
								(w.bounds.height-(pad/2)+10));
						specX.maxval.round(0.001).asString
							.drawAtPoint(
								(w.bounds.left+10+w.bounds.width-20-(pad/2))@
								(w.bounds.height-(pad/2)+10));



						GUI.pen.rotate(-pi/2); 
						GUI.pen.translate(w.bounds.height.neg, 0);
						specY.minval.round(0.001).asString
							.drawAtPoint((pad/2)@(w.bounds.left+(pad/2) -20));
						yAxisName.
							drawAtPoint((w.bounds.height/2)@(w.bounds.left+(pad/2) -20));
						specY.maxval.round(0.001).asString
							.drawAtPoint((w.bounds.height - (pad/2))@(w.bounds.left+(pad/2) -20));
						GUI.pen.translate(w.bounds.height, 0);
						GUI.pen.rotate(pi/2);
						GUI.pen.stroke;
					};
						// draw values
					if (drawValues) {
						adjustedData.do{|item, i|
							data.at(i).round(0.001).asString.drawAtPoint(
								((item[0]  * width) + (pad/2))
								@
								(((1-item[1]) * height) + (pad/2) + 10)
							)
						}
					}
				}; // end GUI.pen.use
			});
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
}

ScatterView3d {
	var scatterView;
	var rotX, rotY, rotZ;
	var specX, specY, specZ;
	var data3d;
	
	*new {|parent, bounds, data, specX, specY, specZ, rotX = 0, rotY = 0, rotZ = 0|
		^super.new.init3d(parent, bounds, data, specX, specY, specZ, rotX, rotY, rotZ);
	}

	init3d {|parent, bounds, data, argspecX, argspecY, argspecZ, argrotX, argrotY, argrotZ|
		specX = argspecX ? [0, 1].asSpec;
		specY = argspecY ? specX.copy;
		specZ = argspecY ? specX.copy;

		rotX = argrotX;
		rotY = argrotY;
		rotZ = argrotZ;

		// [rotX, rotY, rotZ].postln;


		// [-2.sqrt, 2.sqrt].asSpec approx. [-1.42, 1.42].asSpec
		scatterView = ScatterView(parent, bounds, [[1, 1, 1]], [-1.42, 1.42].asSpec);
		this.data = data;
		this.refresh
	}
	data_{|data|
		data3d = data.collect {|item|
			Matrix.withFlatArray(3, 1, [
				specX.unmap(item[0]), 
				specY.unmap(item[1]), 
				specZ.unmap(item[2])
			] * 2 - 1);
		};
		scatterView.data = this.pr_project;

//		highlightItem = min(highlightItem, (adjustedData.size-1));
	}
	refresh {
		scatterView.refresh;
	}
	rotX_{|val|
		this.rot(val, rotY, rotZ);
	}
	rotY_{|val|
		this.rot(rotX, val, rotZ);
	}
	rotZ_{|val|
		this.rot(rotX, rotY, val);
	}
	rot {|rX, rY, rZ|
		rotX = rX;
		rotY = rY;
		rotZ = rZ;
		scatterView.data = this.pr_project;
	}
	drawMethod_{|method|
		scatterView.drawMethod = method;		
	}
	symbolSize_{|val|
		scatterView.symbolSize = val;		
	}
	symbolColor_{|val|
		scatterView.symbolColor = val;		
	}
	isHighlight_{|val|
		scatterView.isHighlight_(val);
	}
	highlightItem_{|item|
		scatterView.highlightItem_(item);
	}
	highlightRange{|start, end|
		scatterView.highlightRange(start, end);
	}
	highlightColor_{|color|
		scatterView.highlightColor_(color);
	}
	highlightSize_{|size|
		scatterView.highlightSize_(size);
	}
	background_{|val|
		scatterView.background = val;		
	}
	resize{
		^scatterView.resize
	}
	resize_{arg resize;
		scatterView.resize_(resize)
	}
	pr_project {
		var cx, cy, sx, sy, sz, cz, projectionMatrix;
		sx = sin(rotX);
		sy = sin(rotY);
		sz = sin(rotZ);

		cx = cos(rotX);
		cy = cos(rotY);
		cz = cos(rotZ);
			
		projectionMatrix = Matrix.with([
			[( (cy * cz) - (sx * sy * sz)), (sz.neg * cx), ((cz * sy) + (sz * sx * cy))],
			[( (cy * sz) + (cz * sx * sy)), ( cz * cx),    ((sz * sy) - (sx * cy * cz))]
		]);
	
	
		^data3d.collect{|row|
			(projectionMatrix * row).getCol(0)
		};
	}
}
