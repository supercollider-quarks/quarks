Arduino
{
	classvar <>crtscts=true;
	var <port, <parser, inputThread;
	var <>action;

	*parserClass {
		^this.subclassResponsibility(thisMethod)		
	}
	*new { | portName, baudrate |
		^super.newCopyArgs(
			SerialPort(
				portName,
				baudrate,
				// without flow control data written only seems to
				// appear at the device after closing the connection
				// aug.2008: made this into a class variable so it can be turned off in certain subclass environments (nescivi)
				crtscts: crtscts
			)
		).init
	}
	init {
		parser = this.class.parserClass.new(this);
		inputThread = fork { parser.parse };
	}

	close {
		inputThread.stop;
		port.close;
	}
	send { | ... args |
		^this.subclassResponsibility(thisMethod)
	}

	// PRIVATE
	prDispatchMessage { | msg |
		action.value(*msg);
	}
}

ArduinoParser
{
	var <arduino, <port;

	*new { | arduino |
		^super.newCopyArgs(arduino, arduino.port).init
	}
	init {
	}
	parse {
		^this.subclassResponsibility(thisMethod)
	}
	dispatch { | msg |
		arduino.prDispatchMessage(msg);
	}
}

// EOF