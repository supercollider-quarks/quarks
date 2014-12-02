// =====================================================================
// SimpleMessageSystem interface

ArduinoSMS : Arduino
{
	*parserClass {
		^ArduinoParserSMS
	}
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
}

ArduinoParserSMS : ArduinoParser
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
			if (msgArg.first.isDecDigit) {
				msgArg = msgArg.asInteger;
			};
			msg = msg.add(msgArg);
		}
	}
	parseByte { | byte |
		if (byte === 13) {
			// wait for LF
			state = 13;
		} {
			if (byte === 10) {
				if (state === 13) {
					// CR/LF encountered
					// dispatch message
					this.finishArg;
					if (msg.notEmpty) {
						this.dispatch(msg);
						msg = Array[];
					};
					state = nil;
				}
			} {
				if (byte === 32) {
					// eat them spaces
					state = 32;
				} {
					// anything else
					if (state == 32) {
						// finish last arg
						this.finishArg;
						state = nil;
					};
					// add to current arg
					msgArgStream << byte.asAscii;
				}
			}
		}
	}
}

// EOF