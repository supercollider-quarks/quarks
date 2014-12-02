// =====================================================================
// XBee - MiniBee serial port interface and parser


XBeeSMS : Arduino
{

	var <>timeout = 0.001;
	
	*initClass{
		crtscts = true;
	}

	*parserClass {
		^XBeeParserSMS
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
		).init.myInit( portName )
	}

	myInit{ |portName|
		if ( thisProcess.platform.name == \linux ){
			("stty -F"+portName+"-icrnl").systemCmd;
		};
	}

	start{
		port.put( $r.ascii );
	}

	stop{
		port.put( $x.ascii );
	}

	send{ |data|
		port.putAll( data );
	}

	sendMsg{ |type,id,vals|
		port.putAll( 
			[ 92, type.ascii ] ++
			(([ id ] ++ vals).collect{ |it| it.asInteger }
				.replaceAllSuchThat( { |it| it == 10 }, [92,10])
				.replaceAllSuchThat( { |it| it == 13 }, [92,13])
				.replaceAllSuchThat( { |it| it == 92 }, [92,92])
				.flatten)
			++ [ 10 ], timeout );		
	}

	sendMsgNoID{ |type,vals|
		port.putAll( 
			[ 92, type.ascii ] ++
			(vals.collect{ |it| it.asInteger }
				.replaceAllSuchThat( { |it| it == 10 }, [92,10])
				.replaceAllSuchThat( { |it| it == 13 }, [92,13])
				.replaceAllSuchThat( { |it| it == 92 }, [92,92])
				.flatten)
			++ [ 10 ], timeout );		
	}

	flicker{ |id,vals|
		this.sendMsg( $f, id, vals );
	}

	flickerSettings{ |id,vals|
		this.sendMsg( $s, id, vals );
	}

	lightAll{ |vals|
		this.sendMsgNoID( $a, vals );
	}

	light{ |id,vals|
		this.sendMsg( $l, id, vals );
	}

	lightRate{ |id,vals|
		this.sendMsg( $f, id, vals );
	}

	lightWidth{ |id,vals|
		this.sendMsg( $w, id, vals );
	}

	lightNew{ |id,vals|
		var length = vals.size + 2;
		port.putAll( 
			([ 10, length.asInteger, id.asInteger, $l.ascii ]  ++ (vals.collect{ |it| it.asInteger }) ).postln;
		)
	}

	motor{ |val|
		port.putAll( [ $m.ascii, val.max(0).min(9).asInteger.asDigit ] );
	}

	// PRIVATE
	prDispatchMessage { | msg |
		action.value(msg);
	}

	// PRIVATE
	prDispatchTypedMessage { | type, msg |
		action.value(type, msg);
	}
}

XBeeParserSMS : ArduinoParser
{
	var msg, msgArgStream, state;

	var <>verbose = 0;

	var msgType = $0;

	var <logfile;
	var record = false;

	parse {
		msg = Array[];
		msgArgStream = CollStream();
		state = nil;
		loop { this.parseByte(port.read) };
	}

	finishArg {
		var msgArg = msgArgStream.contents; msgArgStream.reset;
		if (msgArg.notEmpty) {
			if ( verbose > 2, { msgArg.postln; } );
			if (msgArg.first.isDecDigit) {
				msgArg = msgArg.asInteger;
			};
			if ( msgArg.first.isKindOf( Char ) ){
				msgType = msgArg;
			};
			if ( msgType != $0 ){ // only add if we have a msgType set
				msg = msg.add(msgArg);
			};
			if ( verbose > 1, { [msgType, msg].postln; } );
		}
	}

	dispatchTyped { | type, msg |
		arduino.prDispatchTypedMessage(type, msg);
	}


	parseByte { | byte |
		if ( record, { logfile.write( byte.asString; ); logfile.write( " " ); });
		if ( verbose > 2, { [state,byte].postln; } );
		if ( state == \escape ){ // escape is set
			if ( (byte === 10) or: ( byte === 13 ) or: (byte === 92) ){
				if ( verbose > 0, { "escaping 10 or 13 or 92".postln; } );
				msgArgStream << byte;
				this.finishArg;
				state = nil;
			}{// escape to message type
				msgArgStream << byte.asAscii;
				this.finishArg;
				state = nil;
			}
		}{ // no escape set
			switch( byte,
				92, {
					if ( verbose > 2, { "setting escape".postln; } );
					state = \escape;
				},
				10,{
					// end of line
					this.finishArg;
					if (msg.notEmpty) {
						this.dispatchTyped(msgType,msg);
						msgType = $0;
						msg = Array[];
					};
					state = nil;
				},
				// default case
				{
					// any other byte
					msgArgStream << byte;
					this.finishArg;
					state = nil;	
				}
			);
		};
	}

	// logging
	// recording
	startRecord{ |fn|
		var recordnodes;
		fn = fn ? "XBeeParserLog";
		logfile =  File(fn++"_"++Date.localtime.stamp++".txt", "w");
		record = true;
	}

	stopRecord{
		record = false;
		logfile.close;
	}
}

// EOF
