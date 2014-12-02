// A listener creating a unique perception of the Soundscape.


GeoListener {


	var <>positionResponder, <>moveposResponder;
	var <>la, <>lb, <>lorient;
	var <>offsetAngle, <>newPi, <>scapeWidth, <>scapeHeight, <>address;
	var <>perceptionArea;
	var <>counter = 0;
	
	*new { arg a = 50, b = 50, scapeWidth = 1, scapeHeight = 1; 		//a,b initial position listener in the virtul space, scapeWidth 
		^super.new.initGeoListener(a, b, scapeWidth, scapeHeight)
	}
	initGeoListener { arg aA, aB, ascapeWidth, ascapeHeight;

		scapeWidth = ascapeWidth; 
		scapeHeight = ascapeHeight;
		la = aA;		
		lb = aB;
		newPi = pi ;
		lorient = (newPi/2);	//pi standard value for p greco in Sc is buggy?! 
		offsetAngle = (newPi/32); 
		perceptionArea = 30; 	//in meter
		
		//spatdif is an under construction proposed standard format to describe spatial audio in a a structured way http://www.spatdif.org/ 
		positionResponder = OSCresponder(nil, '/spatdif/core/listener/1/position', // listen any adress in default port
		{ arg time, resp, msg; 
		if (msg[1] != nil, {la = msg[1]; });
		if (msg[2] != nil, {lb = msg[2]; });
		if (msg[3] != nil, {lorient = msg[3];});
		this.update(this);
		}).add ;
		
		moveposResponder = OSCresponder(nil, '/spatdif/core/listener/1/move',
		{ arg time, resp, msg; 
		if (msg[1] == 'gostraight', {this.gostraight()});
		if (msg[1] == 'goback', {this.goback()});
		if (msg[1] == 'turnleft', {this.turnleft()});
		if (msg[1] == 'turnright', {this.turnright()});
		}).add ;
		
		OSCresponder(nil, '/spatdif/core/listener/1/getposition',
		{ arg time, resp, msg; 
		("x"+this.la+"y"+this.lb+"orient"+this.lorient).postln;
		}).add ;
		
	}

	
	gostraight {
	var x = cos(lorient);
	var y = sin(lorient);
	var xnosign = abs(x);
	var ynosign = abs(y);
	var signx = x/xnosign;
	var signy = y/ynosign;
	if (xnosign > 0.5, {x = 1},{x = 0});
	if (ynosign > 0.5, {y = 1},{y = 0});
	if (la < scapeWidth)		
		{if (la > 0)
			{la = la + (signx*x)}
			{la = 1}
			}
		{la = (scapeWidth - 1)};
	if (lb < scapeHeight)
		{if (lb > 0)
			{lb = lb + neg((signy*y))}	//because in Painter view if y = y-1 the position go up (Rect upper border distance)
			{lb = 1}
			}
		{lb = (scapeHeight - 1)};
	this.update(this);
	}
	

	goback {
	var x = cos(lorient);
	var y = sin(lorient);
	var xnosign = abs(x);
	var ynosign = abs(y);
	var signx = x/xnosign;
	var signy = y/ynosign;
	if (xnosign > 0.5, {x = 1},{x = 0});
	if (ynosign > 0.5, {y = 1},{y = 0});
	if (la < scapeWidth)		
		{if (la > 0)
			{la = la + neg((signx*x))}
			{la = 1}
			}//the gostraight contrary
		{la = (scapeWidth - 1)};
	if (lb < scapeHeight)
		{if (lb > 0)
			{lb = lb + (signy*y)}
			{lb = 1}
			}
		{lb = (scapeHeight - 1)};
	this.update(this);
	}


	turnleft {
	var compare, angleLimit;
	lorient = lorient + offsetAngle;
	compare = lorient;
	angleLimit = 3*(pi/2);
	if (compare > angleLimit, {lorient = (lorient - 2pi)} );
	//lorient.postln;
	this.update(this); //no angle variation visualisation yet developed
	}

	turnright {
	var compare, limit;
	lorient = lorient - offsetAngle;
	compare = lorient;
	limit = neg((pi)/2); //-pi/2
	if (compare < limit, {lorient = (lorient + 2pi);});
	//lorient.postln;
	this.update(this);
	}
	
	setPosition { arg x, y;
	la = x;
	lb = y;
	this.update(this);	
	}	

	getPosition {
	^[la,lb,lorient];
	}


	
	calculatePanning { arg aXv, aYv, aA, aB, aOrient, anAtm;
	var xv = aXv;				
	var yv = scapeHeight - aYv; //because theoretical Cartoon model for panning spatialisation was created with a cartesian space
	var a = aA;	//while here in Sc y go to scapeHeight (up) to 0 (down). so for a correct pan behaviour we need to change y reference system
	var b = scapeHeight - aB;
	var orient = aOrient;
	var atm = anAtm;
	
	var d = sqrt(squared(a-xv)+squared(b-yv));
	var pan;
	
	if (atm != nil,//vertex is an atmosphere type
		{pan = 0; //panning always 0 for atm	
			},
		{pan = ((sin(orient)*((xv-a)/d)) - (cos(orient)*((yv-b)/d)));
						}
					);
	//"sin".postln; sin(orient).postln; "cos".postln; cos(orient).postln; "d".postln; d.postln;
	//"(xv-a)/d".postln; ((xv-a)/d).postln; "(yv-b)/d".postln; ((yv - b)/d).postln; "pan".postln; pan.postln;

	if (d == 0, {pan = 0});
	^pan; 
	
	}
	

	update { arg theChanged, theChanger, more;

		var offset, result, atm;

		if (theChanged.class == GeoListener,
			{
			more = [\tobefilteredbyposition,la,lb,lorient];
			//"thishacambiatogeoListener".postln;

/*
			("Listener (x,y,angle) = (").post;
			la.post;
			(", ").post;
			lb.post;
			(", ").post;
			lorient.post;
			(") ").postln;

*/
			this.changed(\position, more)}
						
			)
		}

} 

//move the listener using the arrow of you keybord or a,w,x,d:
//a -> turnleft
//w -> gostraight
//d -> turnright
//s -> goback
//TODO test if the controller works with many type of keyboard...in case please change the unicode
// Anyway if you use OSC position control you don't need the 
GeoListenerGUI {
		var <>geoListener, <>address;
		
		*new { arg geoListener, address;
			^super.new.initGeoListenerGUI(geoListener,address)
			
		}

		initGeoListenerGUI { arg aGeoListener, anAddress;
		var w, c, ystep, xstep, g;	
		
		//Geography works with OSCSwing and not Cocoa
		//If you want GeoGraphy GUI you need set OSCSwing and boot it.
		// ATTENTION, you need OSCSwing Installed

		
		geoListener = aGeoListener;
		if (anAddress != nil, {address = NetAddr(anAddress, 57120)},
			{ address = NetAddr("127.0.0.1", 57120)}
		);
		address.postln;
		
		geoListener.addDependant(this);
		w = Window.new("Listener Position Controller",Rect(800, 80, 270, 120)).front;
		//w.view.background_(Color(0.9, 0.9, 0.9)) ;
		ystep = 30;
		xstep = 250*0.5;
		c = StaticText.new( w, Rect( 0, 0, xstep, ystep )).string_("<-- or a = turn left").stringColor_(Color(0, 0, 0.3)).align_(\center);
		c = StaticText.new( w, Rect( xstep, 0, xstep, ystep )).string_("--> or d = turn right").stringColor_(Color(0, 0, 0.3)).align_(\center);
		c = StaticText.new( w, Rect( 0, ystep*1, xstep, ystep )).string_("^ or w= go straight").stringColor_(Color(0, 0, 0.3)).align_(\center);
		c = StaticText.new( w, Rect( xstep, ystep*1, xstep, ystep )).string_("I or s= go back").stringColor_(Color(0, 0, 0.3)).align_(\center);
		c = StaticText.new( w, Rect( 0, ystep*2, 250, 30 )).string_("    CLICK AND FOCUS ON THIS WINDOWS &  ").stringColor_(Color(1, 0, 0.3));
		c = StaticText.new( w, Rect( 0, ystep*3, 250, 30 )).string_("    PRESS POSITION CONTROL COMMAND     ").stringColor_(Color(1, 0, 0.3));
		c = Slider( w, Rect(0, 0, 0, 0));
		w.view.keyDownAction = { arg view,char,modifiers,unicode,keycode;
				//unicode.postln;
				this.setPosition(unicode);	
				};
		
		w.onClose_({ geoListener.removeDependant(this) }) ;		
		}

		setPosition { arg aUnicode; 
		var compare;
		//aUnicode.postln;
		compare = aUnicode;		
			//does it work's universally? (in case you can switch in use a, d, w, s instead of arrows)
 			case //{ (compare == 97) }  //a 
			{ (compare == 63234  or:(compare == 97)) } // left arrow
				{
				address.sendMsg("/spatdif/core/listener/1/move", "turnleft");
				if (geoListener != nil, {geoListener.turnleft;});
				} 

			//{ ( compare == 100) }	//d
			{ ( compare == 63235 or:(compare == 100)) } // right arrow	
				{
				address.sendMsg("/spatdif/core/listener/1/move", "turnright"); 
				if (geoListener != nil, {geoListener.turnright;});
				
				}

			//{ (compare == 119 } //w
			{ (compare == 63232 or:(compare == 119)) } // up arrow

				{
				address.sendMsg("/spatdif/core/listener/1/move", "gostraight"); 
				if (geoListener != nil, {geoListener.gostraight;});
				}

			//{ (compare == 115 ) } //s
			{ (compare == 63233 or:(compare == 115)) } //down arrow

				{
				address.sendMsg("/spatdif/core/listener/1/move", "goback"); 
				if (geoListener != nil, {geoListener.goback;});
				}

		}

		
}


// M. Schirosa Multimedia Engineering Master Thesis
// made @
// CIRMA, Turin
// revised @
// MTG, Barcelona
              