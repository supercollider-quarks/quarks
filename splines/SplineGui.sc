

AbstractSplineGui : ObjectGui {

	var uv,pen,<>alpha=1.0;
	var <>color,<>crossHairsColor,<>textColor;
	var <>pointSize=3,<>font,<>showGrid=true;

	gui { arg parent,bounds,userView;
		if(userView.notNil,{
			parent = userView.parent;
			bounds = bounds ?? {userView.bounds};
		},{
			if(parent.isNil,{
				parent = Window(model.asString,bounds).front;
				bounds = parent.bounds.moveTo(0,0);
			},{
	 			bounds = bounds ?? {
	 				parent.tryPerform('indentedRemaining') ?? {
	 					Rect(0,0,400,300)
	 				}
	 			}
			});
		});
		uv = this.makeView(parent,bounds,userView);
		this.initColors;	
		this.guiBody(parent,uv.bounds.moveTo(0,0));
	}
	initColors {
		// Du gamla, Du fria
		color = Color.blue;
		crossHairsColor = Color(0.92537313432836, 1.0, 0.0, 0.41791044776119);
		textColor = Color.black.alpha_(0.5);
		font = GUI.font.sansSerif(9);
	}
	makeView { arg parent,bounds,userView;
		pen = GUI.pen;
		^userView ?? {
			UserView(parent, bounds)
				.background_(GUI.skin.background)
				.resize_(5);
		};
	}
}


SplineGui : AbstractSplineGui {
	
	// 2D spline editor

	var <spec,<domainSpec;
	var <>density=256;
	var <gridLines;
	var selected,<>onSelect;
	var boundsHeight,boundsWidth;
	// zooming
	var fromX,toX,fromXpixels=0,xScale=1.0;
	
	gui { arg parent, bounds, argSpec,argDomainSpec,userView;
		if(argSpec.notNil,{
			spec = argSpec.asSpec
		},{
			spec = spec ?? {this.guessSpec};
		});
		if(argDomainSpec.notNil,{
			domainSpec = argDomainSpec.asSpec
		},{
			domainSpec = domainSpec ?? {this.guessDomainSpec};
		});
		^super.gui(parent,bounds,userView)
	}
	
	guiBody { arg layout,bounds;

		// this can recalc on resize
		boundsHeight = bounds.height.asFloat;
		boundsWidth = bounds.width.asFloat;

		if(\GridLines.asClass.notNil,{
			gridLines = DrawGrid(bounds,GridLines(domainSpec),GridLines(this.spec));
		},{
			// this is the old implementation
			// can disappear when this is 3.5 only
			gridLines = GridLines0(uv,bounds,this.spec,domainSpec);
		});
		this.setZoom(domainSpec.minval,domainSpec.maxval);
		
		// only if you own it
		uv.drawFunc = uv.drawFunc.addFunc({
			if(uv.bounds != bounds,{
				this.didResize;
				bounds = uv.bounds;
			});
			pen.use {
				pen.alpha = alpha;
				pen.font = font;
				if(showGrid,{
					gridLines.opacity = alpha;
					gridLines.draw;
				});

				// can cache an array of pen commands
				model.xypoints.do { |point,i|
					var focPoint;
					focPoint = point;
					point = this.map(point);
					pen.addArc(point,pointSize,0,2pi);
					if(i==selected,{
						pen.color = color;
						pen.fill;
						
						// crosshairs
						pen.color = crossHairsColor;
						pen.moveTo(0@point.y);
						pen.lineTo(Point(bounds.width-1,point.y));
						pen.moveTo(point.x@0);
						pen.lineTo(Point(point.x,bounds.height-1));
						pen.stroke;

						// text
						pen.color = textColor;
						pen.use {
							pen.translate(point.x,point.y);
							pen.rotate(0.5pi);
							pen.stringAtPoint(gridLines.x.grid.formatLabel(focPoint.x,4),Point(-45,0));
						};
						pen.stringAtPoint( gridLines.y.grid.formatLabel(focPoint.y,4), Point(point.x+15,point.y-15) );
					},{
						pen.stroke;
					})
				};
				this.drawControlPoints();
				
				pen.color = color;
				pen.moveTo( this.map( model.points[0]) );

				model.interpolate(density).do { arg point,i;
					pen.lineTo( this.map(point) )
				};
				pen.stroke;
			}
		});
		this.focusColor = GUI.skin.focusColor ?? {GUI.skin.foreground.alpha_(0.4)};
		this.makeMouseActions;
		
		this.refresh;
	}
	map { arg p;
		// map a spline point to pixel point
		// for the upside down userview
		 p = p.asArray;
		^Point(
			domainSpec.unmap(p[0]) * boundsWidth - fromXpixels * xScale,
			boundsHeight - (spec.unmap(p[1]) * boundsHeight)
		)
	}
	rawMapX { arg x;
		// non-zoomed map: spline x to pixel x
		^domainSpec.unmap(x) * boundsWidth
	}
	unmap { arg point;
		// unmap a pixel point to a spline point-array
		var x;
		x = point.x / xScale + fromXpixels;
		^[
			domainSpec.map(x / boundsWidth),
			spec.map((boundsHeight - point.y) / boundsHeight)
		]
	}
	makeMouseActions {
		uv.mouseDownAction = { |uvw, x, y,modifiers, buttonNumber, clickCount|
			var p,newSelect;
			p = x@y;
			newSelect = model.xypoints.detectIndex({ |pt|
				(this.map(pt)).dist(p) <= pointSize
			});
			if(selected.notNil and: newSelect.notNil and: {modifiers.isShift},{
				selected = nil
			},{
				selected = newSelect
			});
			if(selected.notNil,{
				uv.refresh
			},{
				if(clickCount == 2,{
					selected = this.createPoint(this.unmap(p));
				});
				uv.refresh;
			});
			if(selected.notNil,{ onSelect.value(selected,this) });
		};
			
		uv.mouseMoveAction = { |uvw, x,y, modifiers| 
			var spoint;
			if( selected.notNil ) { 
				spoint = this.unmap(x@y);
				if(spec.notNil,{
					spoint[1] = spec.constrain(spoint[1])
				});
				if(domainSpec.notNil,{
					spoint[0] = domainSpec.constrain(spoint[0])
				});
				if(modifiers.isCtrl,{
					if(modifiers.isShift,{
						spoint[0] = model.points[selected][0]
					},{
						spoint[1] = model.points[selected][1]
					})
				});				
				model.points[selected] = spoint;
				model.changed('didMovePoint',selected);
			}; 
		};
		// key down action
		// delete selected
		uv.keyDownAction = { arg view, char, modifiers, unicode, keycode;
			this.keyDownAction(view, char, modifiers, unicode, keycode);
		};
	}
	guessSpec {
		var miny,maxy,sp;
		miny = model.xypoints.minValue(_.y);
		maxy = model.xypoints.maxValue(_.y);
		sp = ControlSpec(miny.floor,maxy.ceil);
		^sp.grid.looseRange(miny,maxy).asSpec
	}
	guessDomainSpec {
		var xs,minx,maxx,sp;
		xs = model.points.collect(_.first);
		minx = xs.minItem ? 0.0;
		maxx = xs.maxItem ? 1.0;
		^ControlSpec(minx.floor,maxx.ceil).grid.looseRange(minx,maxx).asSpec
	}
	spec_ { arg sp;
		spec = sp;
		gridLines.spec = sp;
		this.refresh;
	}
	setDomainSpec { arg dsp,setGridLines=true;
		domainSpec = dsp;
		if(setGridLines,{
			gridLines.x.setZoom(dsp.minval,dsp.maxval);
		});
		this.refresh;
	}
	setZoom { arg argFromX,argToX;
		var toXpixels;
		fromX = argFromX.asFloat;
		toX = argToX.asFloat;
		domainSpec = ControlSpec(domainSpec.minval,max(toX,domainSpec.maxval));
		gridLines.x.setZoom(fromX,toX);
		if(boundsWidth.notNil,{
			fromXpixels = this.rawMapX(fromX);
			toXpixels = this.rawMapX(toX);
			xScale = boundsWidth / (toXpixels - fromXpixels);
			if(xScale == inf,{
				xScale = 1.0
			});
		});
	}
	didResize {
		var b;
		b = uv.bounds.moveTo(0,0);
		gridLines.bounds = b;
		boundsHeight = b.height;
		boundsWidth = b.width;
	}
	select { arg i;
		selected = i;
	}
		
	refresh {
		uv.refresh
	}
	update { // deprec
		this.refresh
	}
	background_ { arg c; uv.background = c }
	focusColor_ { arg c; 
		uv.focusColor = c;
	}
	writeName {}
	
	drawControlPoints {}
	createPoint { arg p;
		var i;
		i = this.detectPointIndex(p);
		model.createPoint(p,i);
		model.changed(\didCreatePoint,i);
		^i
	}
	// deletePoint
	detectPointIndex { arg p;
		// find the most logical index to insert point at
		var heights, closest;
		if(model.points.size == 0,{ ^0 });

		heights = model.points.collect({ arg mp,i;
			var prev,next;
			if(i > 0,{
				prev = model.points[i - 1].asPoint;
				next = model.points[i].asPoint;
				[i,this.prDistanceToLine(p.asPoint,prev,next)]
			},{
				[0,model.points[0].asPoint.dist(p).abs]
			})
		}).add( [model.points.size,model.points.last.asPoint.dist(p).abs] );

		closest = heights.minItem(_[1]);
		^closest[0]
	}
	prDistanceToLine { arg click,prev,next;
		var a,b,c,angle,cosc;
		c = prev.dist(next);
		a = click.dist(prev);
		b = click.dist(next);
		if(a > c or: {b > c},{
			^inf // base is not largest, click is not over the line at all
		});
		cosc = (a.squared + b.squared - c.squared) / (2 * a * b);
		angle = cosc.acos;
		^(a * angle.sin).abs
	}

	keyDownAction { arg view, char, modifiers, unicode, keycode;
		var handled = false;
		if(unicode == 8 or: {unicode==127},{
			if(selected.notNil,{
				model.deletePoint(selected);
				model.changed('didDeletePoint',selected);
				selected = nil;
				handled = true;
			})
		});
		^handled
	}
}


BSplineGui : SplineGui {

	var order,orderSpec;

	guiBody { arg layout,bounds;
		super.guiBody(layout,bounds);
		// a bit messy, its sitting on top of userview
		this.curveGui(layout);
	}
	curveGui { arg layout;
		orderSpec = [2,8].asSpec;
		order = Slider( layout, 17@200 )
			.value_( orderSpec.unmap( model.order ) )
			.action_({
				model.order = orderSpec.map(order.value);
				model.changed
			});
	}
	focusColor_ { arg c; 
		uv.focusColor = c;
		if(order.notNil,{
			order.focusColor = c
		})	
	}	
}


// LoopSplineEditor

SplineMapperGui : SplineGui {
	
}


BezierSplineGui : SplineGui {
	
	var <>controlPointColor;
	var selectedCP,flatpoints;

	initColors {
		super.initColors;
		controlPointColor = Color(0.55223, 0.361065, 0.1);
	}
	drawControlPoints {
		model.controlPoints.do { |cps,cpi|
			var next;
			pen.color = controlPointColor;
			cps.do { arg point,i;
				pen.addArc(this.map(point),pointSize,0,2pi);
				if(selectedCP == [cpi,i],{
					pen.fill;
				},{
					pen.stroke;
				});
			};
			
			pen.moveTo(this.map(model.points[cpi]));
			cps.do { arg point,i;
				pen.lineTo(this.map(point));
			};
			if(model.isClosed,{
				next = model.points.wrapAt(cpi+1)
			},{
				next = model.points.at(cpi+1)
			});
			if(next.notNil,{
				pen.lineTo(this.map(next))
			});
			pen.stroke;
		};
	}
	curveGui {}
	refresh {
		// only on points changed
		flatpoints = [];
		model.xypoints.do { arg xy,i;
			flatpoints = flatpoints.add( [this.map(xy),\point,i] )
		};
		model.controlPoints.do { arg cps,cpi;
			cps.do { arg xy,i;
				flatpoints = flatpoints.add( [this.map(xy),\cp, [cpi,i] ] )
			}
		};
		{
			uv.refresh;
		}.defer
	}

	makeMouseActions {
		uv.mouseDownAction = { |uvw, x, y,modifiers, buttonNumber, clickCount|
			var p,fp,i;
			p = x@y;
			fp = flatpoints.detect({ arg flatpoint;
				flatpoint[0].dist(p) <= pointSize
			});
			if(fp.notNil,{ 
				if(fp[1] == 'point',{
					if(selected.notNil and: modifiers.isShift,{
						selected = nil
					},{
						selected = fp[2];
					});
					selectedCP = nil;
					onSelect.value(selected,this);
				},{ // control point
					if(selected.notNil,{
						onSelect.value(nil,this)
					});
					selected = nil;
					selectedCP = fp[2];
				});
				uv.refresh;
			},{
				if(clickCount == 2,{
					if(modifiers.isAlt,{ // isCtrl double click not working on Qt
						i = this.createControlPoint(this.unmap(p));
						selected = nil;
						selectedCP = i;
					},{
						i = this.createPoint(this.unmap(p));
						selected = i;
						selectedCP = nil;
					});
					onSelect.value(selected,this);
				});					
			});
		};
			
		uv.mouseMoveAction = { |uvw, x,y,modifiers| 
			var p,spoint;
			p = x@y;
			spoint = this.unmap(p);
			if(spec.notNil,{
				spoint[1] = spec.constrain(spoint[1])
			});
			if(domainSpec.notNil,{
				spoint[0] = domainSpec.constrain(spoint[0])
			});
			if(modifiers.isCtrl,{
				if(modifiers.isShift,{
					spoint[0] = model.points[selected][0]
				},{
					spoint[1] = model.points[selected][1]
				})
			});			
			if(selected.notNil,{					
				model.points[selected] = spoint;
				model.changed;
			},{
				if(selectedCP.notNil,{
					model.controlPoints[selectedCP[0]][selectedCP[1]] = spoint;
					model.changed;
				});
			}); 
		};
		uv.keyDownAction = { arg view, char, modifiers, unicode, keycode;
			this.keyDownAction(view, char, modifiers, unicode, keycode);
		};		
	}
	
	createControlPoint { arg p;
		var i,cps,cpi,return;
		return = { arg i,ci;
			ci = ci ?? {model.controlPoints[i].size};
			model.createControlPoint(p.asArray,i,ci);
			model.changed('didCreateControlPoint',ci);
			^[i,model.controlPoints[i].size-1]
		};
		i = (this.detectPointIndex(p) - 1).clip(0,model.controlPoints.size-1);
		cps = model.controlPoints[i];
		if(cps.size == 0,{
			return.value(i)
		});
		if(p[0] < cps.first[0],{
			return.value(i,0)
		});
		if(p[0] > cps.last[0],{
			return.value(i)
		});
		cps.do { arg cp,ci;
			if(p[0].inclusivelyBetween(cp[0],cps.clipAt(ci+1)[0]),{
				return.value(i,ci + 1);
			})
		};
		return.value(i)
	}	
	keyDownAction { arg view, char, modifiers, unicode, keycode;
		var cpi,handled = super.keyDownAction(view, char, modifiers, unicode, keycode);
		if(handled.not,{
			if(unicode == 8 or: {unicode==127},{
				if(selectedCP.notNil,{
					cpi = selectedCP[1];
					model.deleteControlPoint(selectedCP[0],cpi);					
					selectedCP = nil;
					model.changed('didDeleteControlPoint',cpi);
					handled = true;
				})
			});
		});
		^handled
	}	
}
	

	