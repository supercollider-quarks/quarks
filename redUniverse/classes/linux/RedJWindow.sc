// this file is part of redUniverse toolkit /redFrik

//this class can also be used with osx-swing.  just copy it to the osx folder

//TO USE REDUNIVERSE WITH SwingOSC, UNCOMMENT THIS CLASS AND CHANGE EXAMPLE CODE FROM RedWindow TO RedJWindow

/*
RedJWindow : JSCWindow {
	var <>mouse, <isPlaying= false, <userView,
		<frame= 0, <frameRate= 0, times;				//emulate SCUserView primitive
	*new {|name= "redWindow", bounds, resizable= false, border= true, server, scroll= false|
		^super.new.initSCWindow(name, bounds, resizable, border, scroll, server)
	}
	*initClass {
		StartUp.add{
			GUI.get(\swing).put(\redWindow, RedJWindow);
		};
		UI.registerForShutdown({ this.closeAll });
	}
	initSCWindow {|argName, argBounds, argResizable, argBorder, scroll, argServer|
		name			= argName.asString;
		border		= argBorder;
		resizable		= argResizable;
		argBounds		= argBounds ?? { Rect.new( 128, 64, 300, 300 )};
		server		= argServer ?? { SwingOSC.default; };
		allWindows	= allWindows.add( this );
		id			= server.nextNodeID;
		dataptr		= this.id;
		this.prInit( name, argBounds, resizable, border, scroll ); // , view );
		
		this.background= Color.black;
		mouse= RedVector2D[0, 0];
		userView= JSCUserView(this, Rect(0, 0, view.bounds.width, view.bounds.height))
			.mouseDownAction_{|view, x, y| mouse= RedVector2D[x, y]}
			.mouseMoveAction_{|view, x, y| mouse= RedVector2D[x, y]};
		times= Array.fill(10, {Main.elapsedTime});
	}
	draw {|func| userView.drawFunc= func}
	play {|fps= 40|
		isPlaying= true;
		{while{this.isOpen&&isPlaying} {
			userView.refresh;
			times[frame%10]= Main.elapsedTime;
			if(frame%10==9, {						//emulate SCUserView primitive
				frameRate= 0;
				9.do{|i|
					frameRate= frameRate+(times[i+1]-times[i]);
				};
				frameRate= 9/frameRate;
			});
			frame= frame+1;						//emulate SCUserView primitive
			fps.reciprocal.wait;
		}}.fork(AppClock);
	}
	stop {isPlaying= false}
	resize {|redVec|
		this.setInnerExtent(redVec[0], redVec[1]);
		userView.bounds= Rect(0, 0, redVec[0], redVec[1]);
	}
	background_ {|color| view.background= color}
	isOpen {^this.isClosed.not}
	animate_ {|bool|								//emulate SCUserView primitive
		if(bool, {
			this.play(60);
		}, {
			this.stop;
		});
	}
}
*/
