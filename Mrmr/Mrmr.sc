Mrmr {
	/*@
	shortDesc: wrapper for communication with mrmr clients
	longDesc: Mrmr creates an easy to use interface for communicating with Mrmr Clients, such as the one available for the iPhone/iPodTouch.  More information about the mrmr system can be found at mrmr noisepages website.  Mrmr allows supercollider to define an interface, push it to the client, and respond to the messages sent by that interface. 
	seeAlso: NetAddr, Bus, In, OSCresponderNode
	issues: Mrmr does not autodiscover the device, so that aspect of communication must be done manually.
	instDesc: Instance Methods
	longInstDesc: Note the example at the end of the file for proper use
	@*/
	var <>iPhone, <>iPhonePort, <>iPhoneName;
	var <>net;
	var <>interfaceCommands, <>interfaceMessages, <>interfaceLabels, <>interfaceIndex;
	var <>responders, <>busses;
	*new { |host,port,name|
		/*@
		phone: hostname of the device as expected by NetAddr.new
		port: port to send mrmrIB commands to (usually 31337)
		name: name of the device. Found in the preferences of the device.
		ex:
		a = Mrmr("iphone.local",31337,"iphone");
		
		a.free;
		@*/
		^super.new.init(host,port,name)
	}
	init { |host("192.168.1.39"), port(31337), name("alPhone")|
		this.iPhone = host;
		this.iPhonePort = port;
		this.net = NetAddr(this.iPhone, this.iPhonePort);
		this.iPhoneName = name;
		this.interfaceCommands = List();
		this.interfaceMessages = List();
		this.interfaceLabels = List();
		this.interfaceIndex = 0;
		this.responders = List();
		this.busses = List();
	}
	
	performanceMode {
		/*@
		desc: Brings mrmr into performance mode. <br><b>This method should be run before sending interface commands.</b>
		@*/
		this.connect;
		this.net.sendRaw("/mrmrIB mrmr_goPerformanceMode\n");
	}
	bundleStyle { |style(\true)|
		/*@
		desc: Sets the style of messages sent back from mrmr. This class expects bundle messages.
		style: bundle style or alternate style (\true or \false)
		ex:
		//These two commands do the same thing.
		//Run either one to use this class.
		a.bundleStyle;
		a.bundleStyle(\true);
		
		//The alternative style. 
		a.bundleStyle(\false);
		@*/
		this.connect;
		style.switch(
			\false, {
				this.net.sendRaw("/mrmrIB mrmr_setBundleStyle:0\n");
			},
			\true, {
				this.net.sendRaw("/mrmrIB mrmr_setBundleStyle:1\n");
			}
		);
	}
	precision { |type|
		/*@
		desc: Sets the format of the data sent back from mrmr.
		type: \float returns floating point numbers from 0 to 1. \int returns integers from 0 to 1000.
		ex:
		//floating point format
		a.precision(\float);
		
		//integer format
		a.precision(\int);
		@*/
		this.connect;
		type.switch(
			\float, {
				this.net.sendRaw("/mrmrIB mrmr_setPrecision:float\n");
			},
			\int, {
				this.net.sendRaw("/mrmrIB mrmr_setPrecision:int\n");
			}
		);
	}
	accelerometerSmoothing { |amount|
		/*@
		desc: Sets the amount of smoothing applied to the accelerometer data.
		amount: a floating point number between 0 and 1, 1 producing the most smoothing
		@*/
		this.connect;
		amount.clip(0,1);
		this.net.sendRaw("/mrmrIB mrmr_setAccelerometerSmoothingAmount:"++amount++"\n");
	}
	accelerometerRate { |miliseconds|
		/*@
		desc: Sets the update rate of the accelerometer data.
		miliseconds: an integer between 8 and 1000 miliseconds between each accelerometer update
		@*/
		this.connect;
		miliseconds.clip(8,1000);
		this.net.sendRaw("/mrmrIB mrmr_setAccelerometerUpdateRate:"++miliseconds++"\n");
	}
	accelerometerEnabled { |boolean|
		/*@
		desc: Globally enables or disables all accelerometers.
		boolean: wether or not to enable the accelerometers (true or false)
		@*/
		this.connect;
		if(boolean,{this.net.sendRaw("/mrmrIB mrmr_setAccelerometerOutputEnabled\n");},
		           {this.net.sendRaw("/mrmrIB mrmr_setAccelerometerOutputDisabled\n");}
		);
	}
	
	alert { |title, body|
		/*@
		desc: Sends an alert message to the device to be displayed to the user.
		title: the title of the alert message
		body: the contents of the alert message
		ex:
		a.alert("Alert Title","the body of the alert is here.");
		@*/
		this.connect;
		this.net.sendRaw("/mrmrIB mrmr_alert title||"++title++"|| body||"++body++"||\n");
	}
	
	addControl { |type,gridCols,gridRows,posCol,posRow,colSpan(1),rowSpan(1),title("_"),style(1)|
		/*@
		desc: Adds an element (control) to the interface.  For more information on the available controls, go to mrmr noisepages website interface commands page.  A more detailed example can be found at the end of this document.
		type: the kind of control. Can be one of the following: [\pushbutton, \togglebutton, \tactilezone, \slider, \textview, \textinputview, \titleview, \accelerometer, \webview]
		gridCols: number of grid columns for layout
		gridRows: number of grid rows for layout
		posCol: the column position of the element
		posRow: the row position of the element
		colSpan: the number of columns spanned by the element
		rowSpan: the number of rows spanned by the element
		title: the title associated with the element
		style: the style of the element
		@*/
		var messageNames, controlTypes, controlName;
		title = title.replace(" ","_");
		messageNames = List();
		controlTypes = List();
		type.switch(
			\pushbutton, {
				messageNames.add("pushbutton");
				controlTypes.add("PB");
				controlName = "pushbutton";
			},
			\togglebutton, {
				messageNames.add("pushbutton");
				controlTypes.add("PB");
				controlName = "togglebutton";
			},
			\tactilezone, {
				messageNames.addAll(["tactilezoneX","tactilezoneY","tactilezoneTouchDown"]);
				controlTypes.addAll(["TX","TY","TB"]);
				controlName = "tactilezone";
			},
			\slider, {
				messageNames.add("slider/horizontal");
				controlTypes.add("S");
				controlName = "slider";
			},
			\textview, {
				controlName = "textview";
			},
			\textinputview, {
				messageNames.add("textinput");
				controlTypes.add("Txt");
				controlName = "textinputview";
			},
			\titleview, {
				controlName = "titleview";
			},
			\accelerometer, {
				style.switch(
					1, {
						messageNames.addAll(["accelerometerX","accelerometerY","accelerometerZ"]);
						controlTypes.addAll(["AX","AY","AZ"]);
					},
					2, {
						messageNames.addAll(["accelerometer/direction","accelerometer/force"]);
						controlTypes.addAll(["AD","AF"]);
					},
					3, {
						messageNames.addAll(["accelerometer/angle", "accelerometer/force"]);
						controlTypes.addAll(["AA","AF"]);
					},
					{("undefined style for accelerometer:"+style).postln; ^this;}
				);
				controlName = "accelerometer";
			},
			\webview, {
				controlName = "webview";
			},
			{("did not recognize control type:"+type).postln; ^this;}
		);
		messageNames.do( { |item, i|
			this.interfaceMessages.add("/mrmr/"
			                           ++item++"/"
			                           ++this.interfaceIndex++"/"
			                           ++this.iPhoneName);
		});
		controlTypes.do( { |item, i|
			this.interfaceLabels.add((item++"("++this.interfaceIndex++")").asSymbol);
		});
		this.interfaceCommands.add("/mrmrIB"+controlName+"nil 0.2"+gridCols+gridRows
		                                                         +posCol  +posRow
		                                                         +colSpan +rowSpan
		                                                         +title+style
		                                                         +"\n");
		this.interfaceIndex = this.interfaceIndex+1;
	}
	addPageBreak {
		/*@
		desc: Marks the beginning of a new page of controls
		@*/
		this.interfaceCommands.add("/mrmrIB |*|\n");
	}
	
	setupInterface { |debug(false)|
		/*@
		desc: Sends the current form of the interface to the device and begins listening for messages returned by the device.
		debug: wether or not to also post the values returned by the device
		@*/
		this.removeResponders;
		this.clearAll;
		this.connect;
		this.interfaceCommands.do( {|item, i|
			this.net.sendRaw(item);
		});
		this.disconnect;
		this.interfaceMessages.do( {|item, i|
			this.busses.add(Bus.control(Server.default));
			this.responders.add(
				OSCresponderNode(nil,item, {|time, responder, msg|
					if(debug,{[this.interfaceLabels[i],msg[1]].postln;});
					this.busses[i].set(msg[1]);
				}).add
			);
		});
	}
	
	resetInterface {
		/*@
		desc: Resets the internal representation of the interface. <br><b>Be sure to run this before creating a new interface!</b>
		@*/
		this.interfaceCommands = List();
		this.interfaceMessages = List();
		this.interfaceLabels = List();
		this.interfaceIndex = 0;
	}
	
	bus { |label|
		/*@
		desc: Returns the bus associated with the specific control label.  See example at the end of this file.
		label: one of the Symbol labels in mrmr.interfaceLabels
		@*/
		if(this.interfaceLabels.matchItem(label),
			{^this.busses[this.interfaceLabels.indexOf(label)]},
			{("could not find the label:"+label).postln;^this}
		);
	}
	
	
	
	connect {
		this.net.connect({("disconnecting from"+this.iPhone).postln});
	}
	disconnect {
		this.net.disconnect;
	}
	
	clearAll {
		this.connect;
		this.net.sendRaw("/mrmrIB mrmr_clear_all\n");
	}
	removeResponders {
		this.responders.do( { |item, i|
			item.remove;
		});
		this.busses.do( { |item, i|
			item.free;
		});
		this.responders = List();
		this.busses = List();
	}
	printOn { |stream|
		stream << this.class.name << "(" <<* [iPhone,iPhonePort] <<")";
	}
}//endclass

/*

//Examples

//boot and set up mrmr
(
s.boot;
a = Mrmr("iphone.local",31337,"iphoneName"); //or: a = Mrmr("192.168.1.50",31337,"iphoneName");
"sclang port:"+NetAddr.langPort; //make sure mrmr is sending data to this address.
)

//set up and sent interface
//this set shows an example of each of the currently supported controls
(
a.resetInterface; //make sure to call this before resending an interface

a.performanceMode; //put the device into performance mode

//three buttons on the top row: a pushbutton, a small pushbutton (style:2), and a toggle button
a.addControl(\pushbutton,3,6,1,1);
a.addControl(\pushbutton,3,6,2,1,style:2);
a.addControl(\togglebutton,3,6,3,1);

//sliders on the second row: a horintal slider, a vertical slider (style:2)
//also a text input view on the second row, any text typed in sends that text back to supercollider
a.addControl(\slider,3,6,1,2);
a.addControl(\slider,3,6,2,2,style:2);
a.addControl(\textinputview,3,6,3,2,title:"type here");

//a title view on the third row
a.addControl(\titleview,3,6,1,3,3,title:"some more things below...");

//a tactile zone on the fourth row
a.addControl(\tactilezone,3,6,1,4,3);

//three types of accelerometers on the fifth row: an XYZ accelerometer, a cardinal direction/force accelerometer, and an angular direction/force accelerometer
a.addControl(\accelerometer,3,6,1,5);
a.addControl(\accelerometer,3,6,2,5,style:2);
a.addControl(\accelerometer,3,6,3,5,style:3);

//a titleview on the sixth row
a.addControl(\titleview,3,6,1,6,3,title:"see next page for more");

//a page break
a.addPageBreak;

//a web view showing google.com
a.addControl(\webview,1,1,1,1,title:"http://google.com");

// setup the interface showing incoming data.
a.setupInterface(true);
)

//Setup a synthdef to work with the mrmr interface
//note that the inputs are busses which are read using the In UGen.
(
SynthDef(\mrmrTest,
	{ | freq, mul, gate(0) |
		var z;
		mul = (In.kr(mul)-1)*(-1);
		freq = In.kr(freq);
		z = EnvGen.kr(Env.asr,In.kr(gate))*SinOsc.ar(freq*500,0,mul);
		Out.ar([0,1],z);
	}
).send(s);
)

//maps the gate to the toggle button, freqency to the x axis of the touch area, amplitude to the y axis of the touch area.  this format requires the precision to be float.
(
a.precision(\float);
x=Synth.new(\mrmrTest,[\freq,a.bus('TX(7)'),\mul,a.bus('TY(7)'),\gate,a.bus('PB(2)')]);
)

//clean up
(
a.free;
x.free;
)

//this block is useful if you are not seeing what you expect
//this code shows all of the messages coming into sclang
(
(
thisProcess.recvOSCfunc = { |time, addr, msg|Ê
	if(msg[0] != 'status.reply') {
		"time: % sender: %\nmessage: %\n".postf(time, addr, msg);Ê
	} Ê
}
);
)


*/