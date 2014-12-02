

LinearSpline  { // : AbstractFunction

	var <>points,<>isClosed;

	*new { |points,isClosed=false|
		^super.newCopyArgs(points.collect(_.asArray),isClosed)
	}
	storeArgs { ^[points,isClosed] }
	simplifyStoreArgs { |args| ^args }

	value { arg u;
		var args,i,ii;
		i = u.floor;
		ii = u.frac;
		^if(isClosed,{
			points.wrapAt(i).blend( points.wrapAt(i+1), ii )
		},{
			points.clipAt(i).blend( points.clipAt(i+1), ii )
		});
	}
	interpolate { arg divisions=128;
		// along the spline path
		var step;
		step = points.size.asFloat / divisions.asFloat;
		^Array.fill(divisions,{ arg i; this.value(i * step) })
	}
	interpolateValues { arg domainValues, domain=0;
		var intpd = this.interpolate(this.points.size * 100);
		^domainValues.collect({ arg t, i;
			var xi, pa, pb, xfrac;
			xi = intpd.lastIndexForWhich({ arg p; p[domain] <= t });
			if(xi.isNil, {
				nil
			},{
				pa = intpd[xi];
				pb = intpd[xi + 1];
				if(pa[domain] == t,{
					pa
				},{
					if(pb.isNil, {
						nil
					},{
						xfrac = (t - pb[domain]) / (pa[domain] - pb[domain]);
						// domain value of this point should equal t
						blend(pa, pb, xfrac);
					});
				});
			});
		});			
	}
	numDimensions {
		^points.first.size
	}

	bilinearInterpolate { arg divisions,domain=0,fillEnds=true;
		// return y values for evenly spaced intervals along x (eg. steady time increments)
		// interpolate returns a series of points evenly spaced along the spline path
		// this linear interpolates between those known points to find y

		// domain : dimension along which evenly spaced divisions occur
		// value : the next dimension up for which value is sought
		// ie. domain = 0 [x], value = 1 [y]
		// any higher dimensions are ignored
		var ps,step,feed,t=0.0;
		var value,totalTime;

		value = 1 - domain;

		ps = this.interpolate( (divisions / points.size * 4.0).asInteger ); // oversampled

		totalTime = ps.last[domain];
		// TODO
		// interpolates from time 0
		// mathematically obliged to do the pre-0 span
		//if(ps.first[domain] < 0,{
		//	totalTime = totalTime + ps.first[domain].abs
		//});

		step = totalTime / divisions.asFloat;
		feed = Routine({ // arg t;
				var xfrac,after;
				ps.do({ arg p,i;
					if(t == p[domain], {
						p[value].yield
					});
					while({ p[domain] > t },{
						if(i > 0,{
							xfrac = (t - ps[i-1][domain]) / (p[domain] - ps[i-1][domain]);
							blend(ps[i-1][value],p[value],xfrac).yield
						},{
							// first point is already past t
							// nil or fill
							if(fillEnds,{
								p[value].yield
							},{
								nil.yield
							})
						})
					})
				});
				inf.do({
					if(fillEnds,{
						ps.last[value].yield
					},{
						nil.yield
					})
				})
			});

		^Array.fill(divisions,{ |i|
			t = i * step;
			feed.next
		})
	}

	xypoints {
		^points.collect({ |p| Point(p[0],p[1]) })
	}
	add { arg p;
		points = points.add(p.asArray)
	}
	createPoint { arg p,i;
		points = points.insert(i,p.asArray);
	}
	deletePoint { arg i;
		if(points.size > 1,{
			points.removeAt(i)
		});
	}
	setPoint { arg i,di,val;
		points[i][di] = val;
	}

	minMaxVal { arg dim;
		var maxes,mins,numd;
		numd = this.numDimensions;
		maxes = -inf;
		mins = inf;
		points.do { arg p;
			if(p[dim] < mins,{
				mins = p[dim]
			});
			if(p[dim] > maxes,{
				maxes = p[dim]
			});
		};
		^[mins,maxes]
	}
	normalizeDim { arg dim,min=0.0,max=1.0;
		// normalize the points
		// not the resulting line
		// which may go beyond the min/max
		// because this would depends on curvature
		var maxes,mins,numd;
		# mins, maxes = this.minMaxVal(dim);
		points.do { arg p;
			p[dim] = p[dim].linlin(mins,maxes,min,max)
		};
	}

	//	plot
	//	skew
	//	rotate
	//	moveBy
	//	resizeBy
	++ { arg thou;
		^this.class.new(points ++ thou.points,isClosed)
	}
	sliceDimensions { arg dims;
		^this.class.new(points.slice(nil,dims),isClosed)
	}
	spliceDimensions { arg dims,other;
		other.points.do { arg p,i;
			dims.do { arg di,ii;
				points[i][di] = p[ii]
			}
		}
	}

	guiClass {
		// 2D only
		^SplineGui
	}
	// see MathLib/ScatterView3d for viewing 3D into a 2D plane
}


BSpline : LinearSpline {

	var <>order;

	*new { |points,order=2.0,isClosed=false|
		^super.newCopyArgs(points.collect(_.asArray),isClosed).order_(order)
	}

	storeArgs { ^[points,order,isClosed] }

	value {	arg u;
		var controls,list;

		if( isClosed, {
			list = points ++ [ points[0] ]
		},{
			list = points
		});
		// implementation adapted from wslib
		controls = this.bSplineIntControls( list );
		^this.splineIntFunctionArray( list,u, *controls )
	}
	interpolate { arg divisions=128;
		// along the spline path
		// actually gives divisions * numSegments
		var step, controls,list,part1Array;

		step = (points.size.asFloat - 1) / divisions.asFloat;

		if( isClosed, {
			list = points ++ [ points[0] ]
		},{
			list = points
		});
		controls = this.bSplineIntControls( list );
		part1Array = list.collect({ |item,ii|
				this.splineIntPart1( [item, list.clipAt(ii+1)], controls[0][ii], controls[1][ii] )
			});

		^Array.fill(divisions,{ arg i;
			var is;
			is = i * step;
			this.splineIntPart2( part1Array[is.floor], is.frac );
		})
	}

	bSplineIntControls { arg list;
		var delta;
		delta = this.bSplineIntDeltaControls( list );
		^[
			list.collect({ |item, i| item + ( delta[i] ? 0 ); }),
			list[1..].collect({ |item, i| item - ( delta[i+1] ? 0 ); })  ++ [ list.first - list.last ]
		];
	}
	bSplineIntDeltaControls { arg list;
		// adapted from http://ibiblio.org/e-notes/Splines/Bint.htm
		var n, b, a, d;

		n = list.size;
		#b, a, d = { ( 0 ! n ) } ! 3;

		b[1] = -1/order;
		a[1] = (list.clipAt(2) - list[0])/order;

		if(n > 2,{
			( 2 .. (n-1) ).do { |i|
				b[i] = -1/(b[i-1] + order);
			   	a[i] = ((list.clipAt(i+1) - list[i-1] - a[i-1]) * -1) * b[i];
			  	};
		});

		( (n-2) .. 0 ).do { |i|
		   if( a[i] != 0 )
		  	{ d[i] = a[i] + (d[i+1]*b[i]); }
		  	{ d[i] = (d[i+1]*b[i]); }
		 };
	   ^d;
	}
	splineIntFunctionArray { |list,i, x1array, x2array|
		var part1Array;
		// x1array and x2array should have the same size as list
		// this can be cached
		part1Array = list.collect({ |item,ii|
				this.splineIntPart1( [item, list.clipAt(ii+1)], x1array[ii], x2array[ii] )
			});
		^this.splineIntPart2( part1Array[i.floor], i.frac );
	}

	splineIntPart1 { |list,x1, x2| // used once per step
		var y1, y2;
		var c3, c2, c1; // c0;
		#y1, y2 = list;
										// c0 = y1; -> use y1 instead
		c1 = (x1 - y1) * 3 ;				// c1 = (3 * x1) - (3 * y1);
		c2 = (x2 - (x1*2) + y1) * 3;		// c2 = (3 * x2) - (6 * x1) + (3 * y1);
		c3 = (y2 - y1) - ((x2 - x1) * 3); 	// c3 = y2 - (3 * x2) + (3 * x1) - y1;
		^[ y1, c1, c2, c3 ];
	}

	splineIntPart2 { |list,i| // used for every index
		^((list[3] * i + list[2]) * i + list[1]) * i + list[0];
	}
	++ { arg thou;
		^BSpline(points ++ thou.points,order,isClosed)
	}
	sliceDimensions { arg dims;
		^this.class.new(points.slice(nil,dims),order,isClosed)
	}

	*defaultOrder { ^2.0 }

	guiClass { ^BSplineGui }
}


BezierSpline : LinearSpline {

	var <>controlPoints;

	*new { arg ... things;
		var isClosed = false,points,controlPoints,nu;
		if(things.last.isKindOf(Boolean),{
			isClosed = things.pop
		});
		if(things.size.odd,{ // last controlPoints only needed if isClosed
			things = things.add( [] )
		});
		# points, controlPoints = things.clump(2).flop;
		points = points.collect(_.asArray);
		controlPoints = controlPoints.collect({ arg cps; cps.collect(_.asArray) });
		nu = super.new(points,isClosed);
		nu.controlPoints = controlPoints;
		^nu
	}
	*fromPoints { arg points=[],controlPoints,isClosed=false;
		//p:   [ [x,y],       [x,y],   [x,y], [x,y] ]
		//cps: [  [[x,y],[x,y]],  [[x,y]],  []      ]
		^super.new(points,isClosed).controlPoints_(controlPoints ?? { [] ! points.size-1 })
	}
	storeArgs {
		^[points,controlPoints].flop.flatten(1).add(isClosed)
	}
	// value not yet implemented
	interpolate { arg divisions=128;
		// along the spline path
		// actually gives divisions * numPoints
		var ps,funcs;
		funcs = #['linear','quadratic','cubic'];
		points.do { arg p,i;
			var cp,pnext;
			cp = controlPoints[i];
			if(isClosed, {
				pnext = points.wrapAt(i+1);
			},{
				pnext = points.at(i+1);
			});
			if(pnext.notNil,{
				// iterate t along tangent from p to pnext
				divisions.do { arg di;
					var t,pt;
					t = divisions.reciprocal * di;
					// choose interpolation
					pt = this.perform(funcs[cp.size] ? \ntic,t,p,pnext,cp);
					ps = ps.add(pt)
				};
			});
		};
		^ps
	}
	linear { arg t,p1,p2,cps;
		^p1.blend(p2,t)
	}
	quadratic { arg t,p1,p2,cps;
		^(((1.0-t).squared)*p1) + (2*(1.0-t)*t*cps[0]) + (t.squared*p2)
	}
	cubic {  arg t,p1,p2,cps;
		^(((1.0-t).cubed)*p1) +   (3*((1.0-t).squared)*t*cps[0]) +    (3*(1.0-t)*t.squared*cps[1]) + (t.cubed*p2)
	}
	ntic { arg t,p1,p2,cps;
		// anything greater than cubic .. gazillionic
		var sum,n;
		n = cps.size + 1;
		sum = (1-t).pow(n) * p1;
		(n-1..1).do { arg ni,i;
			var binomialCoef;
			binomialCoef = n.factorial / (ni.factorial * (n - ni).factorial);
			sum = sum + (binomialCoef * t.pow(n-ni) * (1-t).pow(ni) * cps[i] )
		};
		^sum + (t.pow(n) * p2);
	}
	add { arg p,cp;
		controlPoints = controlPoints.add(cp ? []);
		super.add(p);
	}
	createPoint { arg p,i;
		controlPoints = controlPoints.insert(i,[]);
		super.createPoint(p,i);
	}
	createControlPoint { arg p,pointi,cpi;
		var cps;
		cps = controlPoints[pointi];
		controlPoints[pointi] = cps.insert(cpi??{cps.size},p.asArray);
		^[pointi,controlPoints[pointi].size-1]
	}
	deletePoint { arg i;
		if(points.size > 1,{
			super.deletePoint(i);
			controlPoints.removeAt(i);
		});
	}
	deleteControlPoint { arg pointi,i;
		controlPoints[pointi].removeAt(i)
	}
	setControlPoint { arg i,di,val;
		controlPoints[i][di] = val;
	}

	isLinear {
		^(points.size == 2 and: {controlPoints[0].size == 0})
	}
	++ { arg thou;
		^BezierSpline(points ++ thou.points,controlPoints ++ thou.controlPoints,isClosed)
	}
	sliceDimensions { arg dims;
		^this.class.fromPoints(points.slice(nil,dims),
			controlPoints.slice(nil,dims),isClosed)
	}
	spliceDimensions { arg dims,other;
		super.spliceDimensions(dims,other);
		other.controlPoints.do { arg p,i;
			dims.do { arg di,ii;
				controlPoints[i][di] = p[ii]
			}
		}
	}	
	guiClass { ^BezierSplineGui }
}


/*
HermiteSpline : BSpline {

	interpolationKey { ^\hermite }

}
*/

	//Catmull-Rom
	//http://en.wikipedia.org/wiki/Cubic_Hermite_spline#Catmull.E2.80.93Rom_spline
	//http://stackoverflow.com/questions/1251438/catmull-rom-splines-in-python
	/* For example, most camera path animations generated from discrete key-frames are handled using Catmull–Rom splines. They are popular mainly for being relatively easy to compute, guaranteeing that each key frame position will be hit exactly, and also guaranteeing that the tangents of the generated curve are continuous over multiple segments.*/


