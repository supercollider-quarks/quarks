// this file is part of redUniverse toolkit /redFrik

//todo:
//mouseDownAction etc.  now mouse is lost if setting userView.mouseDownAction

RedWindow : SCWindow {
	var <>mouse, <isPlaying= false, <userView;
	*new {|name= "redWindow", bounds, resizable= false, border= true, server, scroll= false|
		^super.new.initSCWindow(name, bounds, resizable, border, scroll)
	}
	initSCWindow {|argName, argBounds, resizable, border, scroll|
		name= argName.asString;
		argBounds= argBounds ?? {Rect(128, 64, 300, 300)};
		allWindows= allWindows.add(this);
		if(scroll, {"RedWindow: can't scroll".warn});
		view= SCTopView(nil, argBounds.moveTo(0, 0));
		this.prInit(name, argBounds, resizable, border, false, view, false);
		
		this.background= Color.black;
		mouse= RedVector2D[0, 0];
		userView= SCUserView(this, Rect(0, 0, view.bounds.width, view.bounds.height))
			.mouseDownAction_{|view, x, y| mouse= RedVector2D[x, y]}
			.mouseMoveAction_{|view, x, y| mouse= RedVector2D[x, y]};
	}
	draw {|func| userView.drawFunc= func}
	play {|fps= 40|
		isPlaying= true;
		{while{this.isOpen&&isPlaying} {userView.refresh; fps.reciprocal.wait}}.fork(AppClock);
	}
	stop {isPlaying= false}
	resize {|redVec|
		this.setInnerExtent(redVec[0], redVec[1]);
		userView.bounds= Rect(0, 0, redVec[0], redVec[1]);
	}
	background_ {|color| view.background= color}
	isOpen {^this.isClosed.not}
	
	animate_ {|bool|
		userView.animate= bool;
	}
	frame {
		^userView.frame;
	}
	frameRate {
		^userView.frameRate;
	}
}
