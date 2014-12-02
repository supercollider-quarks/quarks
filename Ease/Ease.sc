//--redFrik dec2011
//code ported and adapted to scserver+sclang from the Cinder C++ framework (see Easing.h)


/*
 Copyright (c) 2011, The Cinder Project, All rights reserved.
 This code is intended for use with the Cinder C++ library: http://libcinder.org

 Redistribution and use in source and binary forms, with or without modification, are permitted provided that
 the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and
	the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
	the following disclaimer in the documentation and/or other materials provided with the distribution.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.

Documentation and easeOutIn* algorithms adapted from Qt: http://qt.nokia.com/products/

Disclaimer for Robert Penner's Easing Equations license:
TERMS OF USE - EASING EQUATIONS
Open source under the BSD License.

Copyright © 2001 Robert Penner
All rights reserved.

Disclaimer for Copyright (c) 2011, The Cinder Project, All rights reserved.
Copyright (c) 2011, The Cinder Project, All rights reserved.
 
 
Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

	* Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
	* Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
	* Neither the name of the author nor the names of contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/


//see EaseOverview helpfile


Ease {	//abstract class
	value {^this.subclassResponsibility(thisMethod)}
	*value {|t ...args| ^this.new.value(t, *args)}
	*ar {|t ...args| if(t.rate=='audio', {^this.(t, *args)}, {^this.(K2A.ar(t), *args)})}
	*kr {|t ...args| ^this.(A2K.kr(t), *args)}
	signalRange {^\unipolar}
	//*signalRange {^\unipolar}	//will not work unfortunately
}

EaseNone : Ease {
	value {|t| ^t}
}

//--Quadratic

EaseInQuad : Ease {
	value {|t| ^t*t}
}
EaseOutQuad : Ease {
	value {|t| ^(0-t)*(t-2)}
}
EaseInOutQuad : Ease {
	value {|t| t= t*2; ^if(t<1, 0.5*t*t, -0.5*(t-1*(t-3)-1))}
}
EaseOutInQuad : Ease {
	value {|t| ^if(t<0.5, EaseOutQuad.value(t*2)*0.5, EaseInQuad.value(2*t-1)*0.5+0.5)}
}

//--Cubic

EaseInCubic : Ease {
	value {|t| ^t*t*t}
}
EaseOutCubic : Ease {
	value {|t| t= t-1; ^(t*t*t)+1}
}
EaseInOutCubic : Ease {
	value {|t| t= t*2; ^if(t<1, 0.5*t*t*t, t= t-2; 0.5*(t*t*t+2))}
}
EaseOutInCubic : Ease {
	value {|t| ^if(t<0.5, EaseOutCubic.value(2*t)/2, EaseInCubic.value(2*t-1)/2+0.5)}
}

//--Quartic

EaseInQuart : Ease {
	value {|t| ^t*t*t*t}
}
EaseOutQuart : Ease {
	value {|t| t= t-1; ^0-(t*t*t*t-1)}
}
EaseInOutQuart : Ease {
	value {|t| var t2; t= t*2; t2= t-2; ^if(t<1, 0.5*t*t*t*t, -0.5*(t2*t2*t2*t2-2))}
}
EaseOutInQuart : Ease {
	value {|t| ^if(t<0.5, EaseOutQuart.value(2*t)/2, EaseInQuart.value(2*t-1)/2+0.5)}
}

//--Quintic

EaseInQuint : Ease {
	value {|t| ^t*t*t*t*t}
}
EaseOutQuint : Ease {
	value {|t| t= t-1; ^t*t*t*t*t+1}
}
EaseInOutQuint : Ease {
	value {|t| var t2; t= t*2; t2= t-2; ^if(t<1, 0.5*t*t*t*t*t, 0.5*(t2*t2*t2*t2*t2+2))}
}
EaseOutInQuint : Ease {
	value {|t| ^if(t<0.5, EaseOutQuint.value(2*t)/2, EaseInQuint.value(2*t-1)/2+0.5)}
}

//--Sine

EaseInSine : Ease {
	value {|t| ^0-cos(t*pi/2)+1}
}
EaseOutSine : Ease {
	value {|t| ^sin(t*pi/2)}
}
EaseInOutSine : Ease {
	value {|t| ^-0.5*(cos(pi*t)-1)}
}
EaseOutInSine : Ease {
	value {|t| ^if(t<0.5, EaseOutSine.value(2*t)/2, EaseInSine.value(2*t-1)/2+0.5)}
}

//--Exponential

EaseInExpo : Ease {
	value {|t| ^pow(2, 10*(t-1))}
}
EaseOutExpo : Ease {
	value {|t| ^if(t==1, {1}, {0-pow(2, -10*t)+1})}
	*ar {|t| ^Select.ar(BinaryOpUGen('==', t, 1), [DC.ar(0)-pow(2, -10*t)+1, DC.ar(1)])}
	*kr {|t| ^Select.kr(BinaryOpUGen('==', t, 1), [0-pow(2, -10*t)+1, DC.kr(1)])}
}
EaseInOutExpo : Ease {
	value {|t|
		^if(t==0, {
			0;
		}, {
			if(t==1, {
				1;
			}, {
				t= t*2;
				if(t<1, {
					0.5*pow(2, 10*(t-1));
				}, {
					0.5*(0-pow(2, -10*(t-1))+2);
				});
			});
		});
	}
	*ar {|t|
		var t2= t*2;
		^Select.ar(BinaryOpUGen('==', t, 0), [
			Select.ar(BinaryOpUGen('==', t, 1), [
				if(t2<1, DC.ar(0.5)*pow(2, 10*(t2-1)), DC.ar(0.5)*(0-pow(2, -10*(t2-1))+2)),
				DC.ar(1)
			]),
			DC.ar(0)
		]);
	}
	*kr {|t|
		var t2= t*2;
		^Select.kr(BinaryOpUGen('==', t, 0), [
			Select.kr(BinaryOpUGen('==', t, 1), [
				if(t2<1, 0.5*pow(2, 10*(t2-1)), 0.5*(0-pow(2, -10*(t2-1))+2)),
				DC.kr(1)
			]),
			DC.kr(0)
		]);
	}
}
EaseOutInExpo : Ease {
	value {|t| ^if(t<0.5, EaseOutExpo.value(2*t)/2, EaseInExpo.value(2*t-1)/2+0.5)}
}

//--Circular

EaseInCirc : Ease {
	value {|t| ^0-(sqrt(1-(t*t))-1)}
}
EaseOutCirc : Ease {
	value {|t| t= t-1; ^sqrt(1-(t*t))}
}
EaseInOutCirc : Ease {
	value {|t| var t2; t= t*2; t2= t-2; ^if(t<1, -0.5*(sqrt(1-(t*t))-1), 0.5*(sqrt(1-(t2*t2))+1))}
}
EaseOutInCirc : Ease {
	value {|t| ^if(t<0.5, EaseOutCirc.value(2*t)/2, EaseInCirc.value(2*t-1)/2+0.5)}
}

//--Bounce

EaseBounce : Ease {	//abstract class
	var <>initA;
	*new {|a= 1.70158| ^super.new.initA_(a)}
	prValue {|t, c, a|
		^if(t==1, {
			c;
		}, {
			if(t<(4/11), {
				c*7.5625*t*t;
			}, {
				if(t<(8/11), {
					t= t-(6/11);
					(0-a)*(1-(7.5625*t*t+0.75))+c;
				}, {
					if(t<(10/11), {
						t= t-(9/11);
						(0-a)*(1-(7.5625*t*t+0.9375))+c;
					}, {
						t= t-(21/22);
						(0-a)*(1-(7.5625*t*t+0.984375))+c;
					});
				});
			});
		});
	}
	*prAr {|t, c, a|
		var t2= t-(6/11);
		var t3= t-(9/11);
		var t4= t-(21/22);
		^Select.ar(BinaryOpUGen('==', t, 1), [
			Select.ar(t<(4/11), [
				Select.ar(t<(8/11), [
					Select.ar(t<(10/11), [
						(0-a)*(1-(7.5625*t4*t4+0.984375))+c,
						(0-a)*(1-(7.5625*t3*t3+0.9375))+c
					]),
					(0-a)*(1-(7.5625*t2*t2+0.75))+c
				]),
				c*7.5625*t*t
			]),
			c
		]);
	}
	*prKr {|t, c, a|
		var t2= t-(6/11);
		var t3= t-(9/11);
		var t4= t-(21/22);
		^Select.kr(BinaryOpUGen('==', t, 1), [
			Select.kr(t<(4/11), [
				Select.kr(t<(8/11), [
					Select.kr(t<(10/11), [
						(0-a)*(1-(7.5625*t4*t4+0.984375))+c,
						(0-a)*(1-(7.5625*t3*t3+0.9375))+c
					]),
					(0-a)*(1-(7.5625*t2*t2+0.75))+c
				]),
				c*7.5625*t*t
			]),
			c
		]);
	}
}
EaseInBounce : EaseBounce {		//a= overshoot
	value {|t, a| a= a?initA; ^1-super.prValue(1-t, 1, a)}
	*ar {|t, a= 1.70158|
		if(t.rate=='audio', {
			^1-super.prAr(1-t, DC.ar(1), a);
		}, {
			^1-super.prAr(K2A.ar(1-t), DC.ar(1), a);
		});
	}
	*kr {|t, a= 1.70158| ^1-super.prKr(A2K.kr(1-t), 1, a)}
}
EaseOutBounce : EaseBounce {		//a= overshoot
	value {|t, a| a= a?initA; ^super.prValue(t, 1, a)}
	*ar {|t, a= 1.70158|
		if(t.rate=='audio', {
			^super.prAr(t, DC.ar(1), a);
		}, {
			^super.prAr(K2A.ar(t), DC.ar(1), a);
		});
	}
	*kr {|t, a= 1.70158| ^super.prKr(A2K.kr(t), 1, a)}
}
EaseInOutBounce : EaseBounce {		//a= overshoot
	value {|t, a|
		a= a?initA;
		^if(t<0.5, {
			EaseInBounce.value(2*t, a)/2
		}, {
			if(t==1, {
				1;
			}, {
				EaseOutBounce.value(2*t-1, a)/2+0.5;
			});
		});
	}
	*ar {|t, a= 1.70158|
		^if(t<0.5,
			EaseInBounce.ar(2*t, a)/2,
			Select.ar(BinaryOpUGen('==', t, 1), [
				EaseOutBounce.ar(2*t-1, a)/2+0.5,
				DC.ar(1)
			])
		);
	}
	*kr {|t, a= 1.70158|
		^if(t<0.5,
			EaseInBounce.kr(2*t, a)/2,
			Select.kr(BinaryOpUGen('==', t, 1), [
				EaseOutBounce.kr(2*t-1, a)/2+0.5,
				DC.kr(1)
			])
		);
	}
}
EaseOutInBounce : EaseBounce {		//a= overshoot
	value {|t, a|
		a= a?initA;
		^if(t<0.5, {
			super.prValue(t*2, 0.5, a);
		}, {
			1-super.prValue(2-(2*t), 0.5, a);
		});
	}
	*ar {|t, a= 1.70158|
		^if(t<0.5,
			super.prAr(t*DC.ar(2), DC.ar(0.5), a),
			1-super.prAr(DC.ar(2)-(2*t), DC.ar(0.5), a)
		);
	}
	*kr {|t, a= 1.70158|
		^if(t<0.5,
			super.prKr(t*DC.kr(2), 0.5, a),
			1-super.prKr(DC.kr(2)-(2*t), 0.5, a)
		);
	}
}

//--Back
EaseBack : Ease {			//abstract class
	var <>initA;
	*new {|a= 1.70158| ^super.new.initA_(a)}
}
EaseInBack : EaseBack {		//a= overshoot
	value {|t, a| a= a?initA; ^t*t*((a+1)*t-a)}
}
EaseOutBack : EaseBack {	//a= overshoot
	value {|t, a| a= a?initA; t= t-1; ^t*t*((a+1)*t+a)+1}
}
EaseInOutBack : EaseBack {	//a= overshoot
	value {|t, a|
		var t2, a2;
		a= a?initA;
		t= t*2;
		t2= t-2;
		a2= a*1.525;
		^if(t<1, 0.5*(t*t*((a2+1)*t-a2)), 0.5*(t2*t2*((a2+1)*t2+a2)+2));
	}
}
EaseOutInBack : EaseBack {	//a= overshoot
	value {|t, a|
		a= a?initA;
		^if(t<0.5, EaseOutBack.value(2*t, a)/2, EaseInBack.value(2*t-1, a)/2+0.5);
	}
}

//--Elastic

EaseElastic : Ease {		//abstract class
	var <>initA, <>initP;
	*new {|a= 1, p= 1| ^super.new.initA_(a).initP_(p)}
}
EaseInElastic : EaseElastic {		//a= amplitude, p= period
	prValue {|t, b, c, d, a, p|
		var t_adj, s;
		^if(t==0, {
			b;
		}, {
			t_adj= t/d;
			if(t_adj==1, {
				b+c;
			}, {
				if(a<abs(c), {
					a= c;
					s= p/4;
				}, {
					s= p/2pi*asin(c/a);
				});
				t_adj= t_adj-1;
				0-(a*pow(2, 10*t_adj)*sin((t_adj*d-s)*2pi/p))+b;
			});
		});
	}
	*prAr {|t, b, c, d, a, p|
		var t_adj, t_adj2;
		t_adj= t/d;
		t_adj2= t_adj-1;
		^Select.ar(BinaryOpUGen('==', t, 0), [
			Select.ar(BinaryOpUGen('==', t_adj, 1), [
				if(a<abs(c),
					0-(c*pow(2, 10*t_adj2)*sin((t_adj2*d-(p/4))*2pi/p))+b,
					0-(a*pow(2, 10*t_adj2)*sin((t_adj2*d-(p/2pi*asin(c/a)))*2pi/p))+b
				),
				b+c
			]),
			b
		]);
	}
	*prKr {|t, b, c, d, a, p|
		var t_adj, t_adj2;
		t_adj= t/d;
		t_adj2= t_adj-1;
		^Select.kr(BinaryOpUGen('==', t, 0), [
			Select.kr(BinaryOpUGen('==', t_adj, 1), [
				if(a<abs(c),
					0-(c*pow(2, 10*t_adj2)*sin((t_adj2*d-(p/4))*2pi/p))+b,
					0-(a*pow(2, 10*t_adj2)*sin((t_adj2*d-(p/2pi*asin(c/a)))*2pi/p))+b
				),
				b+c
			]),
			b
		]);
	}
	value {|t, a, p| a= a?initA; p= p?initP; ^this.prValue(t, 0, 1, 1, a, p)}
	*ar {|t, a= 1, p= 1| ^this.prAr(t, DC.ar(0), 1, 1, a, p)}
	*kr {|t, a= 1, p= 1| ^this.prKr(t, 0, 1, 1, a, p)}
}
EaseOutElastic : EaseElastic {		//a= amplitude, p= period
	prValue {|t, b, c, d, a, p|
		var s;
		^if(t==0, {
			0;
		}, {
			if(t==1, {
				c;
			}, {
				if(a<c, {
					a= c;
					s= p/4;
				}, {
					s= p/2pi*asin(c/a);
				});
				a*pow(2, -10*t)*sin((t-s)*2pi/p)+c;
			});
		});
	}
	*prAr {|t, b, c, d, a, p|
		^Select.ar(BinaryOpUGen('==', t, 0), [
			Select.ar(BinaryOpUGen('==', t, 1), [
				if(a<c,
					c*pow(2, -10*t)*sin((t-(p/4))*2pi/p)+c,
					a*pow(2, -10*t)*sin((t-(p/2pi*asin(c/a)))*2pi/p)+c
				),
				c
			]),
			DC.ar(0)
		]);
	}
	*prKr {|t, b, c, d, a, p|
		^Select.kr(BinaryOpUGen('==', t, 0), [
			Select.kr(BinaryOpUGen('==', t, 1), [
				if(a<c,
					c*pow(2, -10*t)*sin((t-(p/4))*2pi/p)+c,
					a*pow(2, -10*t)*sin((t-(p/2pi*asin(c/a)))*2pi/p)+c
				),
				c
			]),
			0
		]);
	}
	value {|t, a, p| a= a?initA; p= p?initP; ^this.prValue(t, 0, 1, 1, a, p)}
	*ar {|t, a= 1, p= 1| ^this.prAr(t, 0, DC.ar(1), 1, a, p)}
	*kr {|t, a= 1, p= 1| ^this.prKr(t, 0, 1, 1, a, p)}
}
EaseInOutElastic : EaseElastic {		//a= amplitude, p= period
	value {|t, a, p|
		var s;
		a= a?initA;
		p= p?initP;
		^if(t==0, {
			0;
		}, {
			t= t*2;
			if(t==2, {
				1;
			}, {
				if(a<1, {
					a= 1;
					s= p/4;
				}, {
					s= p/2pi*asin(1/a);
				});
				if(t<1, {
					-0.5*(a*pow(2, 10*(t-1))*sin((t-1-s)*2pi/p));
				}, {
					a*pow(2, -10*(t-1))*sin((t-1-s)*2pi/p)*0.5+1;
				});
			});
		});
	}
	*ar {|t, a= 1, p= 1|
		var t2= t*DC.ar(2);
		var a2= a.min(1);
		^Select.ar(BinaryOpUGen('==', t, 0), [
			Select.ar(BinaryOpUGen('==', t2, 2), [
				if(t2<1,
					-0.5*(a2*pow(2, 10*(t2-1))*sin((t2-1-(p/2pi*asin(1/a2)))*2pi/p)),
					a2*pow(2, -10*(t2-1))*sin((t2-1-(p/2pi*asin(1/a2)))*2pi/p)*0.5+1
				),
				DC.ar(1)
			]),
			DC.ar(0)
		]);
	}
	*kr {|t, a= 1, p= 1|
		var t2= t*2;
		var a2= a.min(1);
		^Select.kr(BinaryOpUGen('==', t, 0), [
			Select.kr(BinaryOpUGen('==', t2, 2), [
				if(t2<1,
					-0.5*(a2*pow(2, 10*(t2-1))*sin((t2-1-(p/2pi*asin(1/a2)))*2pi/p)),
					a2*pow(2, -10*(t2-1))*sin((t2-1-(p/2pi*asin(1/a2)))*2pi/p)*0.5+1
				),
				1
			]),
			0
		]);
	}
}
EaseOutInElastic : EaseElastic {		//a= amplitude, p= period
	value {|t, a, p|
		a= a?initA;
		p= p?initP;
		^if(t<0.5, {
			EaseOutElastic.new.prValue(t*2, 0, 0.5, 1, a, p);
		}, {
			EaseInElastic.new.prValue(2*t-1, 0.5, 0.5, 1, a, p);
		});
	}
	*ar {|t, a= 1, p= 1|
		^if(t<0.5,
			EaseOutElastic.prAr(t*2, 0, DC.ar(0.5), 1, a, p),
			EaseInElastic.prAr(2*t-1, DC.ar(0.5), DC.ar(0.5), DC.ar(1), a, p)
		);
	}
	*kr {|t, a= 1, p= 1|
		^if(t<0.5,
			EaseOutElastic.prKr(t*2, 0, 0.5, 1, a, p),
			EaseInElastic.prKr(2*t-1, 0.5, 0.5, 1, a, p)
		);
	}
}

//--Atan
EaseAtan : Ease {		//abstract class
	var <>initA;
	*new {|a= 15| ^super.new.initA_(a)}
}
EaseInAtan : EaseAtan {
	value {|t, a| a= a?initA; ^atan((t-1)*a)/atan(a)+1}
}
EaseOutAtan : EaseAtan {
	value {|t, a| a= a?initA; ^atan(t*a)/atan(a)}
}
EaseInOutAtan : EaseAtan {
	value {|t, a| a= a?initA; ^atan((t-0.5)*a)/(2*atan(0.5*a))+0.5}
}
