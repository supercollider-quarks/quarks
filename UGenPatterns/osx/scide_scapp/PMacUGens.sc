//redFrik

//only osx, and only sc3.4 or sc3.5 in 32bit mode (i.e. something is broken under 64bit sc3.5)

PMouseX : Pattern {
	classvar screenBounds;
	var <>minval, <>maxval, <>warp, <>lag, <>length;
	*new {|minval= 0, maxval= 1, warp= 0, lag= 0.2, length= inf|
		^super.newCopyArgs(minval, maxval, warp, lag, length);
	}
	*initClass {
		StartUp.add({
			screenBounds= Window.screenBounds;
		});
	}
	storeArgs {^[minval, maxval, warp, lag, length]}
	embedInStream {|inval|
		var minStr= minval.asStream;
		var maxStr= maxval.asStream;
		var warpStr= warp.asStream;
		//var lagStr= lag.asStream;
		var minVal, maxVal, warpVal/*, lagVal*/;
		var mouse;
		length.value(inval).do{
			minVal= minStr.next(inval);
			maxVal= maxStr.next(inval);
			warpVal= warpStr.next(inval);
			//lagVal= lagStr.next(inval);
			if(minVal.isNil or:{maxVal.isNil or:{warpVal.isNil /*or:{lagVal.isNil}*/}}, {^inval});
			mouse= SCNSObject("NSEvent", "mouseLocation").x;
			if(warp==0 or:{warp==\linear}, {
				inval= mouse.linlin(0, screenBounds.width, minVal, maxVal).yield;
			}, {
				if(warp==1 or:{warp==\exponential}, {
					inval= mouse.linexp(0, screenBounds.width, minVal, maxVal).yield;
				}, {
					(this.class.name++": lag argument not recognized.").warn;
					^inval;
				});
			});
		};
		^inval;
	}
}

PMouseY : PMouseX {
	embedInStream {|inval|
		var minStr= minval.asStream;
		var maxStr= maxval.asStream;
		var warpStr= warp.asStream;
		//var lagStr= lag.asStream;
		var minVal, maxVal, warpVal, lagVal;
		var mouse;
		length.value(inval).do{
			minVal= minStr.next(inval);
			maxVal= maxStr.next(inval);
			warpVal= warpStr.next(inval);
			//lagVal= lagStr.next(inval);
			if(minVal.isNil or:{maxVal.isNil or:{warpVal.isNil /*or:{lagVal.isNil}*/}}, {^inval});
			mouse= SCNSObject("NSEvent", "mouseLocation").y;
			if(warp==0 or:{warp==\linear}, {
				inval= mouse.linlin(0, screenBounds.height, minVal, maxVal).yield;
			}, {
				if(warp==1 or:{warp==\exponential}, {
					inval= mouse.linexp(0, screenBounds.height, minVal, maxVal).yield;
				}, {
					(this.class.name++": lag argument not recognized.").warn;
					^inval;
				});
			});
		};
		^inval;
	}
}

PMouseButton : Pattern {
	var <>minval, <>maxval, <>lag, <>length;
	*new {|minval= 0, maxval= 1, lag= 0.2, length= inf|
		^super.newCopyArgs(minval, maxval, lag, length);
	}
	storeArgs {^[minval, maxval, lag, length]}
	embedInStream {|inval|
		var minStr= minval.asStream;
		var maxStr= maxval.asStream;
		//var lagStr= lag.asStream;
		var minVal, maxVal/*, lagVal*/;
		var mouse;
		length.value(inval).do{
			minVal= minStr.next(inval);
			maxVal= maxStr.next(inval);
			//lagVal= lagStr.next(inval);
			if(minVal.isNil or:{maxVal.isNil /*or:{lagVal.isNil}*/}, {^inval});
			mouse= SCNSObject("NSEvent", "pressedMouseButtons");
			inval= mouse.linlin(0, 1, minVal, maxVal).yield;
		};
		^inval;
	}
}
