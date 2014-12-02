// this file is part of redUniverse /redFrik

//--todo:
//drawing normalization

RedLTurtle {
	var	<>lsystem, <>length, <>theta, <>scale, <>noise,
		<>commands, <>preCommandAction,
		n, index;
	*new {|lsystem, length= 40, theta= 20, scale= 1, noise= 0|
		^super.newCopyArgs(lsystem, length, theta, scale, noise).initRedLTurtle;
	}
	initRedLTurtle {
		commands= this.defaultCommands;
	}
	draw {
		n= 0;
		index= 0;
		if(lsystem.isString, {
			this.prDrawStr(lsystem, 0);
		}, {
			this.prDrawSys(lsystem.production, 0);
		});
		Pen.stroke;
	}
	defaultCommands {
		^(
			$F: {|depth, depthLength, index|
				Pen.line(Point(0, 0), Point(0, depthLength.neg));
				Pen.translate(0, depthLength.neg);
			},
			$G: {|depth, depthLength, index|
				Pen.translate(0, depthLength.neg);
			},
			$+: {
				Pen.rotate(theta/360*2pi+noise.rand2);
			},
			$-: {
				Pen.rotate(theta/360* -2pi+noise.rand2)
			},
			$[: {
				Pen.push;
			},
			$]: {
				Pen.pop;
			},
			$|: {|depth, depthLength, index|
				Pen.line(Point(0, 0), Point(0, depthLength.neg));
				Pen.translate(0, depthLength.neg);
			}
		);
	}
	addCommand {|chr, func|
		commands.put(chr, func);
	}
	makeWindow {|bounds, initAngle= 0, initTranslate|
		var b= bounds ?? {Rect(100, 200, 700, 700)};
		var win= Window(this.class.name, b, false);
		var usr= UserView(win, Rect(0, 0, b.width, b.height));
		initTranslate= initTranslate ? Point(0.5, 0.5);
		usr.drawFunc= {
			Pen.rotate(initAngle, b.width*0.5, b.height*0.5);
			Pen.translate(b.width*initTranslate.x, b.height*(1-initTranslate.y));
			this.draw;
		};
		win.front;
		^win;
	}
	
	//--private
	prDrawStr {|x, depth|
		var depthLength;
		x.do{|chr, i|
			if(chr.isDecDigit, {
				n= n*10+x.digit;
			}, {
				depthLength= scale*length;			//depth cannot be calculated for strings
				preCommandAction.value(0, depthLength, i);
				n.max(1).do{commands[chr].value(0, depthLength, i)};
				n= 0;
			});
		};
	}
	prDrawSys {|x, depth|
		var depthLength;
		if(x.size==0, {
			if(x.isArray, {^nil});
			if(x.isDecDigit, {
				n= n*10+x.digit;
			}, {
				depthLength= scale**depth*length;
				preCommandAction.value(depth, depthLength, index);
				n.max(1).do{commands[x].value(depth, depthLength, index)};
				n= 0;
				index= index+1;
			});
		}, {
			^x.do{|y| this.prDrawSys(y, depth+1)};
		});
	}
}
