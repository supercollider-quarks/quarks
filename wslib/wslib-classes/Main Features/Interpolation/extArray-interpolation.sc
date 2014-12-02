+ Array {
	
	// Basic interpolation functions 
	// part of wslib
	// v#20/7/05
	
	// 3/5/10 changed for compat with points
	 
	linearInt { arg i, step = 0; // uses array of 2 input values
		i = i.round(step);
		^(this[0] + ((this[1] - this[0]) * i)); // from .blend
		}
			
	sineInt { arg i;
		// sine interpolation doesn't support any other than scalar input
		
		//^Env(this[[0,1]], [1], \sine)[i]; // was already there..
		^(( ((((1.0 - i) * pi).cos + 1) * 0.5) * this[1]) + 
			( (((i * pi).cos + 1) * 0.5) * this[0]));
		}
		
	quadIntFunction { |i,x1|
		var y0,y1;
		#y0,y1 = this;
		^( ( (1-i).squared * y0) + (  2 * i * (1-i) * x1 ) + ( i.squared * y1 ) );
		}
	
	quadIntControl {  // not good
		var y0, y1, y2, y3;
		#y0, y1, y2, y3 = this;
		^( 0.5 * ((y1 - (y0/2)) + (y2 - (y3/2))) );
		}
		
	quadInt { arg i; 
		^[ this[1], this[2] ]
			.quadIntFunction( i, this.quadIntControl );
		}
		
	splineIntControls { |amt|
		var y0, y1, y2, y3;
		#y0, y1, y2, y3 = this;
		if(amt.isNil, {amt = 0.75 / ((1.9)**0.5) }); //circle approx.
		amt = amt/2;
		^[ y1 - ((y0 - y2) * amt ), y2 - ((y3 - y1) * amt) ];
		}
		
	modeAt { |index, mode = 'wrap'|
		^switch( mode,
				'wrap', { this.wrapAt( index ) },
				'clip', { this.clipAt( index ) },
				'fold', { this.foldAt( index ) }
			);
	}

	allSplineIntControls { |amt, clipMode = 'wrap'|
		^this.size.collect({ |i|
			this.modeAt( (-1..2) + i, clipMode ).splineIntControls( amt );
		}).flop;
	}
	
	splineIntFunction { |i, x1, x2|
		^this.splineIntPart1( x1, x2 ).splineIntPart2( i );
		}
	
	splineIntPart1 { |x1, x2| // used once per step
		var y1, y2;
		var c3, c2, c1; // c0;
		#y1, y2 = this;
										// c0 = y1; -> use y1 instead
		c1 = (x1 - y1) * 3 ;				// c1 = (3 * x1) - (3 * y1);
		c2 = (x2 - (x1*2) + y1) * 3;		// c2 = (3 * x2) - (6 * x1) + (3 * y1);  
		c3 = (y2 - y1) - ((x2 - x1) * 3); 	// c3 = y2 - (3 * x2) + (3 * x1) - y1; 
		^[ y1, c1, c2, c3 ]; 
		}
		
	splineIntPart2 { |i| // used for every index
		var c0, c1, c2, c3; 
		#c0, c1, c2, c3 = this;
		//	^[ c3 * i.cubed, c2 * i.squared, c1 * i, c0 ].sum;
		^((c3 * i + c2) * i + c1) * i + c0; 
		}
		
	splineIntFunctionArray { |i, x1array, x2array|  // input is a full array (like in bSplineInt )
		var part1Array, out_i;
		// x1array and x2array should have the same size as this
		part1Array = this.collect({ |item,ii|
			[item, this.clipAt(ii+1)].splineIntPart1( x1array[ii], x2array[ii] )
			});
		if( i.size == 0 )
			{ ^part1Array[i.floor].splineIntPart2( i.frac ); }
			{ ^i.asCollection.collect({ |ii|
				part1Array[ii.floor].splineIntPart2( ii.frac ); })  
			};
		
		}
		
	splineInt { arg i, amt; 
		^[ this[1], this[2] ]
			.splineIntFunction( i, 
				*this.splineIntControls( amt ) );
		}
		
	hermiteInt { arg i;  
		// same as spline with amt = 1/3
		// this is used by many UGens as "cubic"
		var c0, c1, c2, c3;
		var y0, y1, y2, y3;
		#y0, y1, y2, y3 = this;
		c0 = y1;
		c1 = (y2 - y0) * 0.5;
		c2 = y0 - (y1 * 2.5) + (y2 * 2.0) - (y3 * 0.5);
		c3 = ((y3 - y0) * 0.5) + ((y1 - y2) * 1.5);
		^((c3 * i + c2) * i + c1) * i + c0;
	}
	
	bSplineIntDeltaControls { |amt = 4|
		// also known as natural spline or cardinal spline
		// adapted from http://ibiblio.org/e-notes/Splines/Bint.htm
		var n;
		var b, a, d;
		
		n = this.size;
		#b, a, d = { ( 0 ! n ) } ! 3;
		 
		b[1] = -1/amt; // (-0.25);
		a[1] = (this[2] - this[0])/amt;
		
		( 2 .. (n-1) ).do { |i|
			b[i] = -1/(b[i-1] + amt);
		   	a[i] = ((this.clipAt(i+1) - this[i-1] - a[i-1]) * -1) * b[i];
		  	};
		  	
		( (n-2) .. 0 ).do { |i|
		   if( a[i] != 0 )
		  	{ d[i] = a[i] + (d[i+1]*b[i]); }
		  	{ d[i] = (d[i+1]*b[i]); }
		 };
	   ^d;
   	}
   	
   	bSplineIntControls { |amt=4|
   		var delta;
   		delta = this.bSplineIntDeltaControls( amt );
   		^[ this.collect({ |item, i| item + ( delta[i] ? 0 ); }),
   			this[1..].collect({ |item, i| item - ( delta[i+1] ? 0 ); }) 
				++ [ this.first - this.last ] ];
   		}
	
	bSplineInt {	arg i, amt, loop = true;
		// full array as input
		// can be way optimized..
		var controls, iF;
		iF = i.floor;
		amt = amt ? 4;
		if( loop )
			{ controls = (this ++ [ this[0] ]).bSplineIntControls( amt );
				
				^[ this.wrapAt(iF), this.wrapAt(iF+1)]
					.splineIntFunction( i.frac, 
						controls[0].clipAt( iF ), 
						controls[1].clipAt( iF ) ); }
						
			{  controls = this.bSplineIntControls( amt );
				
				^[ this.clipAt(iF), this.clipAt(iF+1)]
					.splineIntFunction( i.frac, 
						controls[0].clipAt( iF ), 
						controls[1].clipAt( iF ) ); };			
		}
		
	bSplineInt2 {	arg i, amt, loop = true;
		// experimental: more optimized?
		var controls, iF;
		iF = i.floor;
		amt = amt ? 4;
		if( loop )
			{ controls = (this ++ [ this[0] ]).bSplineIntControls( amt );
				^(this ++ [ this[0] ]).splineIntFunctionArray( i, *controls ) }
						
			{  controls = this.bSplineIntControls( amt );
				^this.splineIntFunctionArray( i, *controls ) };			
		}

		
	fillEnds { |nStart = 1, nEnd = 2| // fill start and end of array with straight lines
		var func = { |x0, x1, n| { |i| x0 + ((i+1) * (x0 - x1)) }!n };
		^func.(this[0], this.clipAt(1), nStart).reverse ++ this ++ 
			func.(this.last, this.clipAt(this.size-2), nEnd);
	}
	
	ghostAt { arg i;  // slow but nice
		var array, add;
		add = i.min(0).abs;
		array = this.fillEnds(add, (i - (this.size - 1)).max(0));
		^array[i + add];
	}
	
	// interpolating at (wraps too)	
	intAt {  |index, type = 'linear', loop = true, extra| 
		var args, i, ii;
		^if( index.size > 0 ) // multichannel support
			{ index.collect( this.intAt(_, type, loop, extra) ); } 
			{
				i = index.floor;
				ii = index.frac;
				case { loop == true }
					{ args = this.wrapAt(i + (-1,0..2)); }
					{ loop == 'fill' }
					{ args = this.fillEnds(1, 2).clipAt(i + (0,1..3));}
					{ loop == false }
					{ args = this.clipAt(i + (-1,0..2)); };
				(			'spline'  : { args.splineInt(ii, extra) },
							'hermite' : { args.hermiteInt(ii) },
							'bspline' : { this.bSplineInt2(index, extra, loop) },
							'quad'    : { args.quadInt(ii) },
							'sine'    : { args.at([1,2]).sineInt(ii) },
							'linear'  : { args.at([1,2]).linearInt(ii) },
							'step'    : { args.at([1,2]).linearInt(ii, 1)  }
					).at(type).value;
			}
		}
	
	// shortcuts
	atL { |index, loop=true| ^this.intAt(index, 'linear', loop) }
	
	atQ { |index, loop=true| ^this.intAt(index, 'quad', loop) }
	
	atH { |index, loop=true| ^this.intAt(index, 'hermite', loop) }
	
	atS { |index, loop=true, extra|  ^this.intAt(index, 'spline', loop, extra) }
	
	atB { |index, loop=true, extra|  ^this.intAt(index, 'bspline', loop, extra) }
	
	atSin { |index, loop=true|  ^this.intAt(index, 'sine', loop) }
	
	fastAtL { |index|	 // simple and fast - loop always on
		var x, xf;
		x = index.frac; xf = index.floor;
		^((x * this.wrapAt(xf+1)) + ((1.0 - x) * this.wrapAt(xf)));
		}
	
	resize { |newSize = 10, type = 'linear', loop=false, extra|
		if(loop == true )
		 {^Array.fill(newSize, { |i|
			this.intAt( i * (this.size / newSize), type, loop, extra) }); }
		 {^Array.fill(newSize, { |i|
			this.intAt( i * ((this.size - 1) / (newSize-1)), type, loop, extra) }); }
	}
	
	interpolate { |division = 10, type = 'linear', loop = true, extra, close = false|
		^this.resize( 
			//if(loop) { (this.size * division).floor } { (this.size * division).floor }, 
			(this.size * division).floor,
			type, loop, extra);
		}
			
}