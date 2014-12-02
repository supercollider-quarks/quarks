

/*
	vectorizing data is a common machine learning technique
	where many parameters are stored as dimensions of a vector.

	a vector-spline is my own Neologism meaning a (time) series of such vectors where the first dimension is interpreted as X and the other dimensions are the data. 

	most commonly X is time, but it can also be an ordered series of states or presets.

	the spline can be used to interpolate between those states or to sequence changes between states.  

	so this gui shows each dimension overlaid and allows switching the focused one and editing it.

	it uses an array of 2D splines
	each one a pair of X (global time) and that dimension as Y

*/

VectorSplineGui : AbstractSplineGui {

	var <splineGuis,<focused=0;
	var fade=0.25,<>specs;
	var domainSpec,fromX,toX;

	guiBody { arg parent,bounds;
		domainSpec = [0.0,model.points.maxValue(_.at(0))].asSpec;
		fromX = domainSpec.minval;
		toX = domainSpec.maxval;
		this.makeSplineGuis;
		this.focused = focused;
	}
	focused_ { arg di;
		var sg;
		if(di.inclusivelyBetween(0,splineGuis.size - 1).not,{
			("Focus index out of range:" + di).error;
			^this
		});
		sg = splineGuis[focused];
		sg.alpha = fade;
		sg.showGrid = false;
		focused = di;
		sg = splineGuis[focused];
		sg.alpha = 1.0;
		sg.showGrid = true;
		sg.makeMouseActions;
		this.refresh;
	}
	refresh {
		splineGuis.do { arg sg,sgi;
			if(sgi != focused, {sg.refresh})
		};
		splineGuis[focused].refresh
	}
	update { arg spline,wot,i;
		//[spline,wot,i].debug("CHANGED");
		splineGuis.do { arg sg,sgi;
			if(sg.model === spline,{
				if(#[\didCreatePoint,
				\didCreateControlPoint,
				\didDeletePoint,
				\didDeleteControlPoint,
				\didMovePoint].includes(wot),{
					this.perform(wot,sg.model,sgi+1,i);
					^this
				},{
					//[spline,wot,i].debug("not handled");
					^nil
				})
			});
		};
		// rebuild all
		splineGuis.do { arg sg;
			sg.model.removeDependant(this);
			sg.remove;
		};
		{
			this.makeSplineGuis;
			uv.refresh;
			this.focused = focused;
		}.defer
	}
	didMovePoint { arg xySpline,dim,i;
		model.setPoint(i,0,xySpline.points[i][0]);
		model.setPoint(i,dim,xySpline.points[i][1]);
		this.updateSplineGuis;
		this.refresh;
	}
	didCreatePoint { arg xySpline,dim,i; 
		var p,interpd,a,b,x,d,xypoint;
		
		// for now, create linear interpolated point
		a = model.points.clipAt(i-1);
		b = model.points.clipAt(i+1);
		xypoint = xySpline.points[i];
		x = xypoint[0];
		d = b[0] - a[0];
		if(d == 0,{
			interpd = a;
		},{
			if(d > 0,{
				// where is xySpline x in space of a[0] b[0] ?
				interpd = blend(a,b,x.linlin(a[0],b[0],0.0,1.0))
			},{
				interpd = blend(b,a,x.linlin(b[0],a[0],0.0,1.0))
			})
		});
		
		p = Array.fill(model.numDimensions,{ arg di;
				if(di == 0,{
					xypoint[0]
				},{
					if(di == dim,{
						xypoint[1]
					},{
						// interpolated value at this x
						// needs a pre-calculated spline interpolator
						interpd[di]
					})
				})
			});
		model.createPoint(p,i);
		this.updateSplineGuis;
		this.refresh;
	}
	didCreateControlPoint { arg xySpline,dim,i; 
		thisMethod.notYetImplemented
	}
	didDeletePoint { arg xySpline,dim,i; 
		model.deletePoint(i);
		this.updateSplineGuis;
		this.refresh;
	}
	didDeleteControlPoint { arg xySpline,dim,i; 
		model.deleteControlPoint(i);
		this.updateSplineGuis;
		this.refresh;
	}

	updateSplineGuis {
		(model.numDimensions - 1).do { arg di;
			var spline,sg;
			// not efficient
			spline = model.sliceDimensions([0,di+1]);
			sg = splineGuis[di];
			sg.model.removeDependant(this);
			sg.model = spline;
			sg.setDomainSpec(domainSpec);
			sg.setZoom(fromX,toX);
			spline.addDependant(this)
		};
	}
	makeSplineGuis {
		uv.drawFunc = nil;
		splineGuis = { arg di;
			var spline,sg;
			spline = model.sliceDimensions([0,di+1]);
			sg = spline.guiClass.new(spline).gui(nil,nil,(specs?[]).clipAt(di),userView:uv);
			sg.setDomainSpec(domainSpec);
			sg.setZoom(fromX,toX);
			sg.color = Color.hsv(di * (model.numDimensions.reciprocal),1,0.5);
			sg.showGrid = false;
			sg.alpha = fade;
			spline.addDependant(this);
			sg
		} ! (model.numDimensions - 1);
	}
	setDomainSpec { arg dsp,setGridLines=true;
		domainSpec = dsp;
		splineGuis.do { arg sg;
			sg.setDomainSpec(domainSpec);
		};
	}
	setZoom { arg argFromX,argToX;
		var toXpixels;
		fromX = argFromX.asFloat;
		toX = argToX.asFloat;
		domainSpec = ControlSpec(domainSpec.minval,max(toX,domainSpec.maxval));
		splineGuis.do { arg sg;
			sg.setZoom(fromX,toX);
		};
	}	
	viewDidClose {
		super.viewDidClose;
		splineGuis.do { arg sg;
			sg.model.removeDependant(this)
		}
	}
}



