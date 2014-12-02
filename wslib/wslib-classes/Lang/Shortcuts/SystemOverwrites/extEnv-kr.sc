// part of wslib 2005
//
// shortcut:
//
// EnvGen.kr(Env([0,1,0],[1,1]), doneAction:2) == Env([0,1,0],[1,1]).kr(2)

+ Env { 
		
	kr { |doneAction = 0, gate = 1.0, timeScale = 1.0, mul = 1, add = 0 |
		^EnvGen.kr(this, gate, mul, add, timeScale, doneAction); }
	
	ar { |doneAction = 0, gate = 1.0, timeScale = 1.0, mul = 1, add = 0 |
		^EnvGen.ar(this, gate, mul, add, timeScale, doneAction); }
		
	arrayFill { |size = 10, keepSize = true|
		var envLength = times.sum;
		if(keepSize.not) {size = (size * envLength).ceil; };
		^Array.fill(size, { |i| this.at((i / (size-1)) * envLength); })
		}
		
	krAt { |index, div = 10|
		index = index * (div - 1);
		^SelectL.kr(index, this.arrayFill(div, false))
		}
	arAt { |index, div = 10|
		index = index * (div - 1);
		^SelectL.ar(index, this.arrayFill(div, false))
		}
}
