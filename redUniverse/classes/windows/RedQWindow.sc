// this file is part of redUniverse toolkit /redFrik

//todo:
//mouseDownAction etc.  now mouse is lost if setting userView.mouseDownAction
//needs more work - just a temporary fix - resize broken

//TO USE REDUNIVERSE WITH QT CHANGE EXAMPLE CODE FROM RedWindow TO RedQWindow

RedQWindow : QWindow {
	var <>mouse, <isPlaying= false, <userView;
	*new {|name= "redQWindow", bounds, resizable= false, border= true, server, scroll= false|
		^super.new.initQWindow(name, bounds, resizable, border, scroll);
	}
	initQWindow {|argName, argBounds, resize, border, scroll|
		argBounds= argBounds ?? {Rect(128, 64, 300, 300)};
		if(scroll, {"RedQWindow: can't scroll".warn});
		view= QTopView(this, argName.asString, argBounds.moveTo(0, 0), resize, border);
		resizable= resize == true;
		QWindow.addWindow(this);
		view.connectFunction('destroyed()', {QWindow.removeWindow(this)}, false);
		
		this.background= Color.black;
		mouse= RedVector2D[0, 0];
		userView= UserView(this, Rect(0, 0, argBounds.width, argBounds.height))
			.mouseDownAction_{|view, x, y| mouse= RedVector2D[x, y]}
			.mouseMoveAction_{|view, x, y| mouse= RedVector2D[x, y]};
		QWindow.initAction.value(this);
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
	//background_ {|color| view.background= color}
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
