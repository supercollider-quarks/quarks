// this file is part of redUniverse /redFrik


RedPerlin {
	var <>spook= 57;
	noise1D {|x, persistence= 0.25, n= 8, interp= 1|
		^{|i|
			var f, a, fx, fracx, floory, v0, v1;
			f= 2.pow(i);
			a= persistence.pow(i);
			fx= f*x;
			fracx= fx.frac;
			floory= fx.floor;
			v0= this.prNoise1(floory);
			v1= this.prNoise1(floory+1);
			if(interp==1, {
				fracx= 1-(fracx*pi).cos*0.5;
				v0*(1-fracx)+(v1*fracx)*a
			}, {
				v0*(1-fracx)+(v1*fracx)*a
			});
		}.sum(n)
	}
	noise2D {|x, y, persistence= 0.25, n= 8, interp= 1|
		^{|i|
			var f, a, fx, fy, fracx, fracy, floorx, floory, v0, v1, v2, v3;
			f= 2.pow(i);
			a= persistence.pow(i);
			fx= f*x;
			fy= f*y;
			fracx= fx.frac;
			fracy= fy.frac;
			floorx= fx.floor;
			floory= fy.floor;
			v0= this.prNoise2(floorx, floory);
			v1= this.prNoise2(floorx+1, floory);
			v2= this.prNoise2(floorx, floory+1);
			v3= this.prNoise2(floorx+1, floory+1);
			if(interp==1, {
				fracx= 1-(fracx*pi).cos*0.5;
				fracy= 1-(fracy*pi).cos*0.5;
				v0= v0*(1-fracx)+(v1*fracx);
				v1= v2*(1-fracx)+(v3*fracx);
				v0*(1-fracy)+(v1*fracy)*a
			}, {
				v0= v0*(1-fracx)+(v1*fracx);
				v2= v2*(1-fracx)+(v3*fracx);
				v0*(1-fracy)+(v2*fracy)*a
			});
		}.sum(n)
	}
	noise3D {|x, y, z, persistence= 0.25, n= 8, interp= 0|
		^{|i|
			var f, a, fx, fy, fz, fracx, fracy, fracz, floorx, floory, floorz,
				v0, v1, v2, v3, v4, v5, v6, v7;
			f= 2.pow(i);
			a= persistence.pow(i);
			fx= f*x;
			fy= f*y;
			fz= f*z;
			fracx= fx.frac;
			fracy= fy.frac;
			fracz= fz.frac;
			floorx= fx.floor;
			floory= fy.floor;
			floorz= fz.floor;
			v0= this.prNoise3(floorx, floory, floorz);
			v1= this.prNoise3(floorx+1, floory, floorz);
			v2= this.prNoise3(floorx, floory+1, floorz);
			v3= this.prNoise3(floorx+1, floory+1, floorz);
			v4= this.prNoise3(floorx, floory, floorz+1);
			v5= this.prNoise3(floorx+1, floory, floorz+1);
			v6= this.prNoise3(floorx, floory+1, floorz+1);
			v7= this.prNoise3(floorx+1, floory+1, floorz+1);
			//if(interp==1, {
				//todo	
			//}, {
				v0= v0*(1-fracx)+(v1*fracx);
				v2= v2*(1-fracx)+(v3*fracx);
				v4= v4*(1-fracx)+(v5*fracx);
				v6= v6*(1-fracx)+(v7*fracx);
				v0= v0*(1-fracy)+(v2*fracy);
				v4= v4*(1-fracy)+(v6*fracy);
				v0*(1-fracz)+(v4*fracz)*a
			//});
		}.sum(n)
	}
	
	//--private
	prNoise1 {|x|
		thisThread.randSeed= x;
		
		^0.25.rand2+0.50.rand2+0.25.rand2
	}
	prNoise2 {|x, y|
		thisThread.randSeed= x+(y*spook);
		
		^0.0625.rand2+0.1250.rand2+0.0625.rand2
		+0.1250.rand2+0.2500.rand2+0.1250.rand2
		+0.0625.rand2+0.1250.rand2+0.0625.rand2
	}
	prNoise3 {|x, y, z|
		thisThread.randSeed= x+(y*spook)+(z*spook);
		
		^0.015625.rand2+0.031250.rand2+0.015625.rand2
		+0.031250.rand2+0.062500.rand2+0.031250.rand2
		+0.015625.rand2+0.031250.rand2+0.015625.rand2
		
		+0.031250.rand2+0.062500.rand2+0.031250.rand2
		+0.062500.rand2+0.125000.rand2+0.062500.rand2
		+0.031250.rand2+0.062500.rand2+0.031250.rand2
		
		+0.015625.rand2+0.031250.rand2+0.015625.rand2
		+0.031250.rand2+0.062500.rand2+0.031250.rand2
		+0.015625.rand2+0.031250.rand2+0.015625.rand2
		
		/*	//generates the ranges above
		[
			[[64, 32, 64], [32, 16, 32], [64, 32, 64]],
			[[32, 16, 32], [16,  8, 16], [32, 16, 32]],
			[[64, 32, 64], [32, 16, 32], [64, 32, 64]]
		].flat.collect{|x| x.reciprocal}
		*/
	}
}
