/*************************************************************************
From Cephes Math Library Release 2.8:  June, 2000
Copyright by Stephen L. Moshier

Contributors:
    * Sergey Bochkanov (ALGLIB project). Translation from C to
      pseudocode.
    * Charles CŽleste Hutchins. Translation into SuperCollider

See subroutines comments for additional copyrights.

>>> SOURCE LICENSE >>>
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation (www.fsf.org); either version 2 of the 
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

A copy of the GNU General Public License is available at
http://www.fsf.org/licensing/licenses

>>> END OF LICENSE >>>
*************************************************************************/


Bessel
/*@
shortDesc: calculate Bessel functions
longDesc: Calculates Bessel functions of the first kind. This creates a table, so looking up many different n values is efficient.
instDesc: Creation methods
longInstDesc: creating it precalculates j0 and j1
@*/
//
{

	var <x, x_abs, jn_arr;
	
	*new { |x|
	
		^super.new.init(x);
	}
	
	init { |x_val|
	
		x = x_val;
		x_abs = x.abs;
		jn_arr = [this.pr_calc_j0, this.pr_calc_j1];
	}
	
	
	/*************************************************************************
	Bessel function of order n
	
	Does some arithmetic thing I found in a mathbook.  Builds a table.
	Is awesome enough to put copyright stuff on.
	Copyright 2009 Charles C Hutchins	
	*************************************************************************/
	
	jn { |n|
	
		/*@
		desc: Calculate nth order Bessel function of the first kind
		ex: 
		b = Bessel(5); // 5 is the x value
		b.jn(2); // 2 is the n value, so this calculates J[2](5)
		@*/

	
		var current, prev, prev2, flag, j;
	
		(n < jn_arr.size).if({
		
			^jn_arr[n];
		});
		
		current = jn_arr.size;
		prev = current - 1;
		prev2 = current -2;
		flag = false;
		
		{flag.not}.while ({
		
			j = (((2 * prev) / x) * jn_arr[prev]) - jn_arr[prev2];
			jn_arr = jn_arr ++ j;
			
			(current == n).if({
			
				flag = true;
			} , {
			
				current = current +1;
				prev = prev+1;
				prev2 = prev2 + 1;
			})
		});
		
		^j;
	}
	
	
	j0 {
		/*@
		desc: Resturns 0th order Bessel function of the first kind
		ex: 
		b = Bessel(5); // 5 is the x value
		b.j0; // 0 is the n value, so this returns J[0](5)
		@*/

		var result;

		(jn_arr.notNil).if({
		
			(jn_arr.size > 0). if ({
		
				^jn_arr[0];
		}) });
		
		result = this.pr_calc_j0;
		jn_arr = [result];
		^result;
		
	}
	
	
	j1 {
		/*@
		desc: Resturns 1st order Bessel function of the first kind
		ex: 
		b = Bessel(5); // 5 is the x value
		b.j1; // 1 is the n value, so this returns J[1](5)
		@*/

		var result;
		
		(jn_arr.notNil).if({
		
			(jn_arr.size > 1).if ({
			
				^jn_arr[1]
		})} , {
		
			jn_arr = [this.pr_calc_j0];
		});
	
		result = this.pr_calc_j1;
		jn_arr = jn_arr ++ result;
		^result;
	}
		
		
	/*************************************************************************
	Bessel function of order zero

	Returns Bessel function of order zero of the argument.

	The domain is divided into the intervals [0, 5] and
	(5, infinity). In the first interval the following rational
	approximation is used:


	       2         2
	(w - r  ) (w - r  ) P (w) / Q (w)
     	 1         2    3       8

	           2
	where w = x  and the two r's are zeros of the function.

	In the second interval, the Hankel asymptotic expansion
	is employed with two rational functions of degree 6/6
	and 7/7.

	ACCURACY:

	                     Absolute error:
	arithmetic   domain     # trials      peak         rms
	   IEEE      0, 30       60000       4.2e-16     1.1e-16

	Cephes Math Library Release 2.8:  June, 2000
	Copyright 1984, 1987, 1989, 2000 by Stephen L. Moshier
	*************************************************************************/
	
	pr_calc_j0 {	
		// from http://www.alglib.net/specialfunctions/bessel.php
		
		var result, s, xsq, nn, pzero, qzero, p1, q1;
		
		
			//x = x.abs;
			
			(x_abs > 8).if({
			
				result = this.pr_besselasympt0;
				pzero = result[0];
				qzero = result[1];
				nn = x_abs - (pi/4);
				result = (2/pi/x_abs).sqrt * ((pzero * nn.cos) - (qzero * nn.sin));
				jn_arr = [result];
				^result;
			});
			
			xsq = x * x;

			p1 = 26857.86856980014981415848441;
			p1 = -40504123.71833132706360663322+(xsq*p1);
			p1 = 25071582855.36881945555156435+(xsq*p1);
			p1 = -8085222034853.793871199468171+(xsq*p1);
			p1 = 1434354939140344.111664316553+(xsq*p1);
			p1 = -136762035308817138.6865416609+(xsq*p1);
			p1 = 6382059341072356562.289432465+(xsq*p1);
			p1 = -117915762910761053603.8440800+(xsq*p1);
			p1 = 493378725179413356181.6813446+(xsq*p1);
			q1 = 1.0;
			q1 = 1363.063652328970604442810507+(xsq*q1);
			q1 = 1114636.098462985378182402543+(xsq*q1);
			q1 = 669998767.2982239671814028660+(xsq*q1);
			q1 = 312304311494.1213172572469442+(xsq*q1);
			q1 = 112775673967979.8507056031594+(xsq*q1);
			q1 = 30246356167094626.98627330784+(xsq*q1);
			q1 = 5428918384092285160.200195092+(xsq*q1);
			q1 = 493378725179413356211.3278438+(xsq*q1);
			result = p1/q1;
			
			
			^result;
		
	}
				
	
	/*************************************************************************
	Bessel function of order one

	Returns Bessel function of order one of the argument.

	The domain is divided into the intervals [0, 8] and
	(8, infinity). In the first interval a 24 term Chebyshev
	expansion is used. In the second, the asymptotic
	trigonometric representation is employed using two
	rational functions of degree 5/5.

	ACCURACY:

	                     Absolute error:
	arithmetic   domain      # trials      peak         rms
	   IEEE      0, 30       30000       2.6e-16     1.1e-16

	Cephes Math Library Release 2.8:  June, 2000
	Copyright 1984, 1987, 1989, 2000 by Stephen L. Moshier
	*************************************************************************/
	
	pr_calc_j1 {
		// from http://www.alglib.net/specialfunctions/bessel.php
		var result, s, xsq, nn, pzero, qzero, p1, q1, sign;
	
	
		//x = x.abs;
		sign = x.sign;
			
		(x_abs > 8).if({
				
			result = this.pr_besselasympt0;
			pzero = result[0];
			qzero = result[1];
			nn = x - ( 3 * pi / 4);
			result = (2 / pi / x_abs).sqrt * ((pzero * nn.cos) - (qzero * nn.sin));
			result = result * sign;
			
		} , {
		
			xsq = x*x;
			p1 = 2701.122710892323414856790990;
			p1 = -4695753.530642995859767162166+(xsq*p1);
			p1 = 3413234182.301700539091292655+(xsq*p1);
			p1 = -1322983480332.126453125473247+(xsq*p1);
			p1 = 290879526383477.5409737601689+(xsq*p1);
			p1 = -35888175699101060.50743641413+(xsq*p1);
			p1 = 2316433580634002297.931815435+(xsq*p1);
			p1 = -66721065689249162980.20941484+(xsq*p1);
			p1 = 581199354001606143928.050809+(xsq*p1);
			q1 = 1.0;
			q1 = 1606.931573481487801970916749+(xsq*q1);
			q1 = 1501793.594998585505921097578+(xsq*q1);
			q1 = 1013863514.358673989967045588+(xsq*q1);
			q1 = 524371026216.7649715406728642+(xsq*q1);
			q1 = 208166122130760.7351240184229+(xsq*q1);
			q1 = 60920613989175217.46105196863+(xsq*q1);
			q1 = 11857707121903209998.37113348+(xsq*q1);
			q1 = 1162398708003212287858.529400+(xsq*q1);
			result = x*p1/q1;
		});
		
		jn_arr = jn_arr ++ result;
		
		^result;
	
	}
	
	
	
	pr_besselasympt0 {
	
		// from http://www.alglib.net/specialfunctions/bessel.php
		var xsq, p2, q2, p3, q3, pzero, qzero;
		
		xsq = 64.0/(x*x);
		p2 = 0.0;
		p2 = 2485.271928957404011288128951+(xsq*p2);
		p2 = 153982.6532623911470917825993+(xsq*p2);
		p2 = 2016135.283049983642487182349+(xsq*p2);
		p2 = 8413041.456550439208464315611+(xsq*p2);
		p2 = 12332384.76817638145232406055+(xsq*p2);
		p2 = 5393485.083869438325262122897+(xsq*p2);
		q2 = 1.0;
		q2 = 2615.700736920839685159081813+(xsq*q2);
		q2 = 156001.7276940030940592769933+(xsq*q2);
		q2 = 2025066.801570134013891035236+(xsq*q2);
		q2 = 8426449.050629797331554404810+(xsq*q2);
		q2 = 12338310.22786324960844856182+(xsq*q2);
		q2 = 5393485.083869438325560444960+(xsq*q2);
		p3 = -0.0;
		p3 = -4.887199395841261531199129300+(xsq*p3);
		p3 = -226.2630641933704113967255053+(xsq*p3);
		p3 = -2365.956170779108192723612816+(xsq*p3);
		p3 = -8239.066313485606568803548860+(xsq*p3);
		p3 = -10381.41698748464093880530341+(xsq*p3);
		p3 = -3984.617357595222463506790588+(xsq*p3);
		q3 = 1.0;
		q3 = 408.7714673983499223402830260+(xsq*q3);
		q3 = 15704.89191515395519392882766+(xsq*q3);
		q3 = 156021.3206679291652539287109+(xsq*q3);
		q3 = 533291.3634216897168722255057+(xsq*q3);
		q3 = 666745.4239319826986004038103+(xsq*q3);
		q3 = 255015.5108860942382983170882+(xsq*q3);
		pzero = p2/q2;
		qzero = 8*p3/q3/x;

		^[pzero, qzero]
	}
				

	pr_besselasympt1 {
	
		// from http://www.alglib.net/specialfunctions/bessel.php
		var xsq, p2, q2, p3, q3, pzero, qzero;
		
		xsq = 64.0/(x*x);
		p2 = -1611.616644324610116477412898;
		p2 = -109824.0554345934672737413139+(xsq*p2);
		p2 = -1523529.351181137383255105722+(xsq*p2);
		p2 = -6603373.248364939109255245434+(xsq*p2);
		p2 = -9942246.505077641195658377899+(xsq*p2);
		p2 = -4435757.816794127857114720794+(xsq*p2);
		q2 = 1.0;
		q2 = -1455.009440190496182453565068+(xsq*q2);
		q2 = -107263.8599110382011903063867+(xsq*q2);
		q2 = -1511809.506634160881644546358+(xsq*q2);
		q2 = -6585339.479723087072826915069+(xsq*q2);
		q2 = -9934124.389934585658967556309+(xsq*q2);
		q2 = -4435757.816794127856828016962+(xsq*q2);
		p3 = 35.26513384663603218592175580;
		p3 = 1706.375429020768002061283546+(xsq*p3);
		p3 = 18494.26287322386679652009819+(xsq*p3);
		p3 = 66178.83658127083517939992166+(xsq*p3);
		p3 = 85145.16067533570196555001171+(xsq*p3);
		p3 = 33220.91340985722351859704442+(xsq*p3);
		q3 = 1.0;
		q3 = 863.8367769604990967475517183+(xsq*q3);
		q3 = 37890.22974577220264142952256+(xsq*q3);
		q3 = 400294.4358226697511708610813+(xsq*q3);
		q3 = 1419460.669603720892855755253+(xsq*q3);
		q3 = 1819458.042243997298924553839+(xsq*q3);
		q3 = 708712.8194102874357377502472+(xsq*q3);
		pzero = p2/q2;
		qzero = 8*p3/q3/x;

		^[pzero, qzero];

	}
	
}
