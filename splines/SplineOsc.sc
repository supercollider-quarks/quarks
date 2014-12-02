


SplineGen {
	
	var <>spline,<>dimension,<>loop;
	
	*new { arg spline,dimension=0,loop=false;
		^super.newCopyArgs(spline,dimension,loop)
	}
	storeArgs {
		^[spline,dimension,loop]
	}
	duration {
		// or total loop time
		^spline.points.last[dimension]
	}
	asLocalBuf { arg divisions=512;
		var levels;
		levels = spline.bilinearInterpolate(divisions,dimension,true);
		^LocalBuf.newFrom(levels);	
	}
	kr { arg timeScale=1.0,doneAction=0,divisions=512;
	    // spline x values are seconds
	    // if timeScale is tempo then x values are beats
		var b,index;
		b = this.asLocalBuf(divisions);
		if(loop,{	
			index = VarSaw.kr(this.duration.reciprocal * timeScale,width:1).range(0,divisions-1)
		},{
			index = Line.kr(0.0,divisions-1,this.duration * timeScale,doneAction:doneAction);
		});
		^BufRd.kr(1,b,index,loop.binaryValue,4)
	}
	
	readKr { arg position,timeScale=1.0,divisions=512;
	    // position is in X units (seconds)
	    // if timeScale is tempo then x values are beats
		var b,index;
		b = this.asLocalBuf(divisions);
		if(timeScale != 1.0,{
		    position = position * timeScale;
		});
		index = (position / this.duration) * divisions;
		^BufRd.kr(1,b,index,0,4)
	}
	//trigKr
	
	xyKr { arg speed,divisions=512,rate='kr';
		// returns an array of kr following each dimension
		// returns as many dims as you have, not just xy
		// time is the path along the spline
		// the total tangent length along spline can be summed
		// but only if 2 dimensions
		// so just crank the speed up until you like it
		
		// should be able to do it in a single buffer/BufRd
		var ps;
		ps = spline.interpolate(divisions);
		ps.flop.plot2;
		^Array.fill(spline.numDimensions,{ arg di;
			var b,index;
			b = LocalBuf.newFrom(ps.collect(_[di]));
			if(loop,{	
				index = VarSaw.perform(rate,speed,0,1).range(0,divisions-1)
				
			},{
			index = Line.perform(rate,0.0,divisions-1,speed,1.0,0.0);
			});
			BufRd.perform(rate,1,b,index,loop.binaryValue,4)
		});
	}

	/*
	loopInterpolate { arg divisionsPerSegment=128;
		// odd: last point is far right edge
		// if first point is at far left edge
		// then they are at same time
		// onscreen you expect to be viewing one cycle
		// not have it varying in wavelength by where last point lies.
		// maybe that is fine. no other way to vary the wavelength without zooming
		var first,ps,li,one,two;
		first = Point(spline.points.last.x + spline.points.first.x,spline.points.first.y);
		ps = (spline.points ++ [first]).collect(_.asArray)
				.interpolate(divisionsPerSegment,
							spline.interpolationKey,
							false,spline.order)
				.collect(_.asPoint);
		// chop off after last point
		// move to pre-first
		li = ps.lastIndexForWhich(_.x < spline.last.x);
		if(li.notNil,{
			one = ps.copyToEnd(li).collect({ |p| Point(p.x-spline.points.last.x,p.y) });
			two = ps.copyRange(0,li-1);
			ps = one ++ two
		});
		^ps
	}
	*/
	
}	

	// a more accurate, efficient and complicated technique
	// would be to use a time to buffer index mapper
	// where more points are allocated when the error/difference between true value and
	// interpolated value is highest
	// so fewer total points, more of them where they are needed.

	// but its easier to just write a UGen to calculate the splines on the server in real time

	// would be unable to do it for other more complex continuous functions
	// but those are probably best written with straight ugen math



SplineOsc : SplineGen {
	
	// plays a spline in a loop with X as time
	// ignoring any time reversals
	// doesnt sound as expected
		
	ar { arg freq=440,phase=0,divisions=1024,rate=\ar;
	    // plays in cycles (full spline) per second
		var b,index,totalTime=0,levels;
		totalTime = this.duration;
		b = this.asLocalBuf;
		index = VarSaw.perform(rate,(totalTime).reciprocal * freq,phase,1).range(0,levels.size-1)
		^BufRd.perform(rate,1,b,index,1,4)
	}
	
	kr { arg freq=440,phase=0,divisions=1024;
		^this.ar(freq,phase,divisions,\kr)
	}
}


SplineMapper {
	
	// use 2D spline as a mapping function
	// spline lies within bounds of inSpec (x) and outSpec (y)
	// but those specs are required so we know what full width/height is
	
	var <>spline,<>dimension,<>inSpec,<>outSpec;
	
	*new { arg spline,dimension=0,inSpec,outSpec;
		^super.newCopyArgs(spline,dimension,inSpec.asSpec,outSpec.asSpec)
	}
	
	kr { arg x,divisions=128,rate=\kr;
		var b,index,ispec;
		b = this.makeBuf(divisions,dimension);
		if(inSpec.warp.isKindOf(LinearWarp).not,{
			ispec = [0,b.numFrames-1].asSpec;
			index = ispec.map(inSpec.unmap(x));
		},{
			index = x.linlin(inSpec.minval,inSpec.maxval,0,b.numFrames-1)
		});
		^BufRd.perform(rate,1,b,index,0,4)
	}
	ar { arg x,divisions=128;
		// Shaper better ?
		^this.kr(x,divisions,\ar)
	}
	
	makeBuf { arg divisions=512,dimension=0;
		var levels;
		levels = spline.bilinearInterpolate(divisions,dimension,true).clip(outSpec.minval,outSpec.maxval);
		^LocalBuf.newFrom(levels);
	}
}



