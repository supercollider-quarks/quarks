// ©2009 Miguel Negr‹o
// GPLv3 - http://www.gnu.org/licenses/gpl-3.0.html

//func of type { |vector,t|  a scalar value }	
//note: it's up to the user to make sure that they are passing a vector with the correct size

ScalarField {
	var <>func, <dim;
	
	*new{ |func,dim| 
		if(func.isNil || dim.isNil){
			Error("ScalarField: either func or dim is nil").throw
		};
		^super.newCopyArgs(func,dim) 
	}

	value{ |vector,t| ^func.(vector,t) }
	
	prGradElem{ |i,vector,t,delta=0.01|
		var deltaV = RealVector.canonB(i,vector.size)*delta;
		^this.value(vector+deltaV,t)-this.value(vector-deltaV,t)
	}
	
	grad{ |vector,t=0,delta = 0.01|
		^vector.size.collect{ |i| 
			this.prGradElem(i,vector,t,delta)
			}.as(RealVector)
	}
	
	surf3{ |t=0,rect,grid = 20,hidden3d = true, pm3d = true,extracmds|
		var xyz, gnuplot, grain, specX, specY;
		
		rect = rect ? Rect(0,0,1,1);
		specX = [rect.left,rect.left+rect.width,\linear].asSpec;
		specY = [rect.top,rect.top+rect.width,\linear].asSpec;
			
		gnuplot = GNUPlot.new;
		grain = 1/grid;  
		xyz =  (0,grain .. 1).collect{|x|  (0,grain .. 1).collect{|y| 
			 [specX.map(x),specY.map(y),this.value(RealVector2D[specX.map(x),specY.map(y)],t)]
		 } };
		gnuplot.surf3(xyz, "a Scalar Field", hidden3d, pm3d,extracmds:extracmds);
	}
	
	//as a VectorFieldF
	gradFieldF{ |delta = 0.01|
		 ^VectFieldF({ |vector,t=0| this.prGrad(vector,t,delta) })
	}
		
	asSCImageUni{ |rect, hsize=100, res = 0.1, contrast = 1, color= #[247,145,30], t=0|
		
		var image;
		
		rect = rect ? Rect(0,0,1,1);
		
		image = this.prUnipolar(rect,hsize*res,contrast,color,t);
		
		image.scalesWhenResized_(true);
		
		image.setSize(hsize.asInteger,(hsize*rect.height/rect.width).asInteger)
		
		^image
	}
	
	plotUni{ |rect, hsize = 300, res = 0.3, contrast = 1, color= #[247,145,30], t=0|
		rect = rect ?? { Rect(0.0,0.0,1.0,1.0) };
		this.asSCImageUni(rect, hsize, res, contrast,color).plot
	}
	
	prUnipolar{ |rect, hsize, contrast, color, t=0| 

		var min, max, image, rgbs, valArray, xmap, ymap, vsize;
		
		min = 100;
		max = 0;
		rect = rect ? Rect(0,0,1,1);
		vsize = (hsize*rect.height/rect.width).asInteger;
		
		hsize = hsize.asInteger;
		vsize = vsize.asInteger;
		
		image = SCImage.new(hsize,vsize);
		//image.accelerated_(true);

		xmap = { |x| x.linlin(0,hsize-1,rect.left,rect.left + rect.width)Ê};
		ymap = { |y| y.linlin(0,vsize-1,rect.top + rect.height,rect.top )Ê};

		
		valArray = Array.fill(hsize*vsize, { |i|
			var x = i%hsize, y = i.div(hsize);
			this.func.(RealVector2D[xmap.(x),ymap.(y)],t).abs;
		});
		
		min = valArray.minItem;
		max = valArray.maxItem;
		
		rgbs = Int32Array.fill(hsize*vsize, { |i|
			var val;
			val = valArray[i].linlin(min,max,0,1)*contrast; //- ((1-contrast)/2);
			val = (color*val).floor.asInteger.clip(0,255);
			Integer.fromRGBA(val[0],val[1],val[2],255);

		});
			
		image.pixels_(rgbs)
		
		^image
		
	}
	
	asSCImageBi{ |rect, hsize=100, res = 0.1, contrast = 1, color1 = #[247,145,30],  color2=#[45,235,234], t=0|
		
		var image;
		
		rect = rect ? Rect(0,0,1,1);
		
		image = this.prBipolar(rect,hsize*res,contrast,color1,color2,t);
		
		image.scalesWhenResized_(true);
		
		image.setSize(hsize.asInteger,(hsize*rect.height/rect.width).asInteger)
		
		^image
	}
	
	plotBi{ |rect, hsize = 100, res = 0.4, contrast = 1, color1= #[247,145,30], color2=#[45,235,234], t=0|
		rect = rect ?? { Rect(0.0,0.0,1.0,1.0) };
		this.asSCImage(rect, hsize, res,contrast,color1,color2,t).plot
	}
	
	prBipolar{  |rect, hsize, contrast, color1=#[255,142,123], color2=#[45,235,234],t=0|
	
		var min, max, image, rgbs, valArray, xmap, ymap, vsize;
		
		min = 100;
		max = 0;
		rect = rect ? Rect(0,0,1,1);
		vsize = (hsize*rect.height/rect.width).asInteger;
		
		hsize = hsize.asInteger;
		vsize = vsize.asInteger;
		
		image = SCImage.new(hsize,vsize);
		//image.accelerated_(true);

		xmap = { |x| x.linlin(0,hsize-1,rect.left,rect.left + rect.width)Ê};
		ymap = { |x| x.linlin(0,vsize-1,rect.top + rect.height,rect.top)Ê};

		
		valArray = Array.fill(hsize*vsize, { |i|
			var x = i%hsize, y = i.div(hsize);
			this.func.(RealVector2D[xmap.(x),ymap.(y)],t).abs;
		});
		
		min = valArray.minItem;
		max = valArray.maxItem;


		rgbs = Int32Array.fill(hsize*vsize, { |i|
			var val;
			val = valArray[i]*contrast;
			if(val >=0 )
			{	val = (color1*val).floor.asInteger.clip(0,255);
				Integer.fromRGBA(val[0],val[1],val[2],255)
			}
			{
				val = (color2*(val.abs)).floor.asInteger.clip(0,255);
				Integer.fromRGBA(val[0],val[1],val[2],255)
			};
		});
			
		image.pixels_(rgbs); 
		^image
	
	}
	
	checkDim{ |field|
		if(this.dim != field.dim){
			Error("Dimensions mismatch: "++this.dim++" vs "++field.dim).throw
		}	
	}

	doesNotUnderstand{ arg selector, other;
		
		this.checkDim(other);	
		if(['+','-','*','/',\mod,\div,\pow, \min, \max].includes(selector)){
			^ScalarField(func.perform(selector,other.func),dim)
		}
	}
	
	prGetGridValuesArray{ |n = 20|
		var spec = [0,n-1].asSpec;
		^n.collect{ |i|
			n.collect{ |j|
				func.value(RealVector2D[spec.unmap(i),spec.unmap(j)])
			}
		}.flat			
	
	}
	
	getMax{ |n = 20|
		^this.prGetGridValuesArray(n).maxItem	
	}
	
	getMin{ |n = 20|
		^this.prGetGridValuesArray(n).minItem	
	}
	
	getRange{ |n = 20|
		^[this.getMin(n),this.getMax(n)]	
	}

	//3D interpolation based on formula for intersection of a line with a plane.
	//http://www.softsurfer.com/Archive/algorithm_0104/algorithm_0104B.htm#Line-Plane Intersection	//at the moment only sampling the rect Rect(0,0,1,1)
	//implemnent custom Rect in the future.
	
	sampled{ |n,rect|
		var values, spec;
		rect = rect ?? { Rect(0.0,0.0,1.0,1.0) };
		//this.checkD(2);
		spec = [0,n-1,\linear].asSpec;
		values = Array2D.new2D(n+1,n+1,{ |i,j|  this.value(RealVector2D[spec.unmap(i),spec.unmap(j)],0) });
		
		^ScalarField({ |vector|
			var grain, indexX,indexY,xdiv,ydiv,points,xmod,ymod,xmodbool,ymodbool,normal,x,y;
			#x,y = vector;
			grain = 1/(n-1);
			xmod = x.mod(grain); 
			ymod = y.mod(grain);
			indexX = spec.map(x).asInteger;
			indexY = spec.map(y).asInteger;
			xdiv = x.div(grain)*grain;
			ydiv = y.div(grain)*grain;
			
			points = if(ymod>xmod){
				[RealVector3D[xdiv,ydiv,values.at(indexX,indexY)],
				RealVector3D[xdiv,ydiv+grain,values.at(indexX,indexY+1)],
				RealVector3D[xdiv+grain,ydiv+grain,values.at(indexX+1,indexY+1)]]
			}{
				[RealVector3D[xdiv,ydiv,values.at(indexX,indexY)],
				RealVector3D[xdiv+grain,ydiv+grain,values.at(indexX+1,indexY+1)],
				RealVector3D[xdiv+grain,ydiv,values.at(indexX+1,indexY)]]
			};
			normal = (points[1]-points[0]).cross(points[2]-points[0]);
			
			(normal<|>(points[0]-RealVector3D[x,y,0]))/normal.z
		
	},2)		
	
	}
}

BlobField : ScalarField{
	var <>low, <>high;	
	
	*basicNew{ |afunc,adim,alow,ahigh|
		^super.new(afunc,adim).basicInit(alow,ahigh);
	}
	
	basicInit{ |alow,ahigh|
		low = alow;
		high = ahigh;	
	}
		
	*new{ |low=0.0,high=1.0,fieldDesc, normalize = true| 
		
		var blob,range,func;
		
		fieldDesc = fieldDesc ?? 
			{ 3.collect{ [RealVector.rand(2),rrand(0.4,1.0),rrand(0.2,0.4)] } };
		
		blob = { |x0,y0,sigma=0.2| var e = 2.71828;
			{ |x,y| e**((((x-x0)**2) + ((y-y0)**2))/(2*(sigma**2))).neg }
		};
			
		func = { |p,t| 
			fieldDesc.collect{ |array|
				var vector;
			
				vector = array[0];					
					
				blob.(vector.x,vector.y,array[2] ? 0.2).value(p.x,p.y)*array[1]			}.sum;
		};			
		
		if(normalize){
			range = ScalarField(func,2).getRange;
			func = func.linlin(range[0],range[1],0.0,1.0)
		};
	
		^super.new(func,2).init(low,high)
	}
	
	init{ |alow,ahigh| 
		
		low = Ref(alow);
		high = Ref(ahigh);
	}
	
	value { |point|
		^func.(point).range(low.value,high.value)
	}
	
	valueUnmapped{ |point|
		^func.(point)
	}
	
	prGradElemUnmapped{ |i,vector,t,delta=0.01|
		var deltaV = RealVector.canonB(i,vector.size)*delta;
		^this.valueUnmapped(vector+deltaV,t)-this.valueUnmapped(vector-deltaV,t)	
	}
	
	sampled{ |n,rect|
		^BlobField.basicNew(super.sampled(n,rect).func,dim,low.copy,high.copy)
	}	
}

// VectorField defined by function { |vector,t| anotherVector }
// can be dimension independent e.g. VectFieldF({ |vector,t| vector*3 })
VectFieldF {
	var <func, <dim;
	
	*new{ |func,dim| 
		if(func.isNil || dim.isNil){
			Error("ScalarField: either func or dim is nil").throw
		};
		^super.newCopyArgs(func,dim) 
	}
		
	value{ |vector,t| ^func.(vector,t) }
	
	asVectField{ |size|
		
		^VectField(*size.collect{ |i| 
			{ |vector,t| this.func.(vector,t).at(i) }
		})
	}	
}

//array of functions o type { |vector,t| Double } 
VectField[slot] : RawArray{	
	
	*new { arg ... args;		
		^super.new(args.size).addAll(args)		 
	}
	
	dim{
		this.size
	}
	
	value { arg... args;
		^this.collect{ |func| func.(*args) }.as(RealVector)
	}
		
	asFunc{
		^{ |vector,t| this.value(vector,t) }
	}
	
	asVectFieldF{
		^VectFieldF(this.asFunc,this.size)
	}
	
	printOn { arg stream;
			stream << "a " << this.class.name << "[" << this.size << "]"
	}
	
	performUnaryOp { arg aSelector;
		^this.class.new(*this.collect({ arg item; item.perform(aSelector) }));
	}

	performBinaryOp { arg aSelector, theOperand, adverb;
Ê		^this.class.new(*theOperand.performBinaryOpOnSeqColl(aSelector, this, adverb));
	}
	
	//usual notation up to 3D vectors
	x{ ^this.at(0) }
	
	y{ this.checkD(2); ^this.at(1) }
	
	z{ this.checkD(3); ^this.at(2) }	
	
	checkD{ |dim|
		if(this.size < dim){
			Error("Field is not "++dim++"D !").throw
		}
	}		
}
