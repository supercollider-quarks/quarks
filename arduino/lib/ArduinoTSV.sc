// =====================================================================
// Read in tab-separated values, on lines
// e.g. in Arduino code:
/*
	Serial.print(millis() - start);        // check on performance in milliseconds
    Serial.print("\t");                    // tab character for debug windown spacing
    Serial.print(total[0]);                  // print sensor output 1
    Serial.print("\t");
    Serial.print(total[1]);                  // print sensor output 2
    Serial.print("\t");
    Serial.print(total[2]);                // print sensor output 3
    Serial.print("\t");
    Serial.println(total[3]); 
*/

ArduinoTSV : Arduino
{
	*parserClass {
		^ArduinoParserTSV
	}
	/*
	send { | ... args |
		var lastIndex = args.lastIndex;
		args.do { |obj,i|
			port.putAll(obj.asString);
			if (i !== lastIndex) {
				port.put(Char.space)
			};
		};
		port.put(13);
	}
	*/
}

ArduinoParserTSV : ArduinoParser
{
	var msg, msgArgStream, state;

	parse {
		msg = Array[];
		msgArgStream = CollStream();
		state = nil;
		loop { this.parseByte(port.read) };
	}

	finishArg {
		var msgArg = msgArgStream.contents; msgArgStream.reset;
		if (msgArg.notEmpty) {
			msg = msg.add(msgArg);
		}
	}
	parseByte { | byte |
		if ( byte == 9 ){ // end of this value
			this.finishArg;
		}{
			if ( byte == 10 || (byte == 13) ){ // end of line
				this.finishArg;
				if (msg.notEmpty) {
					this.dispatch(msg);
					msg = Array[];
				};
			}{ // anything else
				// add to current arg
				msgArgStream << byte.asAscii;
			};
		};
	}
}

// EOF