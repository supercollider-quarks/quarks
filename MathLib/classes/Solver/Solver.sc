// Miguel Negro (c) 2009
// www.friendlyvirus.org/artists/zlb/
// GPLv2 -http://www.gnu.org/licenses/old-licenses/gpl-2.0.html.

 
//-----------------------------------------------------------------------------------------------//
//    Solver - Abstract Class
//-----------------------------------------------------------------------------------------------//
//  Solves
//  y'=f(t,y)
//-----------------------------------------------------------------------------------------------//
// for systems of equations f should be an NFunc
//
// with:
// x = [x1,...,xn]
// f = NFunc[f1,...,fn] with fi = fi(t,x1,...,xn)
//-----------------------------------------------------------------------------------------------//
// for higher order ODE's
//
// transforms y^(n) = f(t,y,y',...,y^(n-1))
// into this system of equations
// with X=(t,x0,...,xn-1)
//
//  x_0' = x1 = { |t,x0,...,xn-1| x1}.(X)
//  x_1' = x2
//  ...
//  x_n-2' = x_n-1
//  x_n-1' = f(t,x_0,...,x_n-1)
//
//  initial y = [y0,y'0,...,y^(n-1)0]
// 
// the substitution is
// y -> x0
// y' -> x1
// y^(n-1) -> x_n-1
//-----------------------------------------------------------------------------------------------//

Solver : Object {
	var <>f,<>dt,<>t=0,<>y=0,<>order = 1;

	*new { arg f,dt, t=0,y=0; ^super.newCopyArgs(f,dt,t,y) }
	
	*newHO { |f,dt,t,y| ^super.new.initHO(f,dt,t,y) }

	initHO { |af,adt,at,ay|
		dt = adt;
		t = at;
		y = ay;
		if(af.size == 0)
		{
			order = (af.def.numArgs-1);
			f = ((order-1).collect{ |i| { arg... args; args[i+2]} })
				++ [{ arg... args; af.(*args) }];
			f = f.as(NFunc);
		}
		{	//order is order of the equation with higher order
			//"system of equations mode".postln;
			//af.postln;
			order = ((af[0].def.numArgs-1)/af.size).asInteger.postln;
			f = af.collect{ |func,k| 
				//postln("function: "++ k);
				(((order-1).collect{ |i| { arg... args; args[i+2+(k*order)]} })
				++ [{ arg... args; func.(*args) }]) 
			}.as(SystemNFunc);
		}		
			
	}

	next { 
		//postln("calculating t = "++t);
		y = y + (this.dydt*dt.value);
		t = t + dt.value;
		^y
		
	}	

}

RK : Solver {

	evaluate { |initial_y,t,dt,dydt|
		var newy = initial_y + (dydt*dt.value);
		/*postln("newy for time "++t++" : "++newy);
		postln("newy size: "++newy.size);
		postln("args: "++[initial_y,t,dt,dydt]);
		*/
		if( f.size ==0 )
			{ ^f.(*([t+dt.value]++newy.asArray)) }
			{ ^f.(*([t+dt.value]++newy.flatten)) }

	}

}

RK4 : RK {

	dydt {
	
		var k1,k2,k3,k4;
		k1 = this.evaluate(y,t,0,0);
		k2 = this.evaluate(y,t,dt.value*0.5,k1);
		k3 = this.evaluate(y,t,dt.value*0.5,k2);
		k4 = this.evaluate(y,t,dt.value,k3);
		//postln("t: "++t++"k1: "++k1++" k2: "++k2++"k3: "++k3++"k4: "++k4);

		^(k1+(2*k2)+(2*k3)+k4)/6		
	
	}
	
}

RK3 : RK {

	dydt {
	
		var k1,k2,k3;
		k1 = this.evaluate(y,t,0,0);
		k2 = this.evaluate(y,t,dt.value*0.5,k1);
		k3 = this.evaluate(y,t,dt.value,k1.neg+(2*k2));
		//postln("t: "++t++"k1: "++k1++" k2: "++k2++"k3: "++k3);

		^(k1+(4*k2)+k3)/6	
	}
}

RK2 : RK {

	dydt {
	
		var k1,k2;
		k1 = this.evaluate(y,t,0,0);
		k2 = this.evaluate(y,t,dt.value*0.5,k1);
		//postln("t: "++t++"k1: "++k1++" k2: "++k2);
		
		^k2
	
	}
	
}
	
Euler : Solver {

	dydt {
		//^f.(*([t+dt.value]++y.asArray))
		if( f.size ==0 )
			{ ^f.(*([t+dt.value]++y.asArray)) }
			{ ^f.(*([t+dt.value]++y.flatten)) }
		
	}

}	

// NFunc f:R^n -> R^n
NFunc[slot] : Array {
	
	value { arg... args;
		^this.collect{ |func| func.(*args) } 
	}
}

// SystemNFunc f:(R^n)^m -> (R^n)^m 
SystemNFunc[slot] : Array {
	
	value { arg... args;
		^this.collect{ |arrayfunc,i| arrayfunc.collect {|func| func.(*args) } } 
	}
}


