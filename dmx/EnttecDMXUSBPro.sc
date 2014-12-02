/*
DMX framework for supercollider
(c) 2007-9 Marije Baalman (nescivi)
GNU/GPL v2.0 or later
*/

EntTecDMXUSBPro : DMXDevice {

	*new {
		| port |
		/*/
		  baudrate(57600),
		  databits(8),
		  stopbit(true),
		  parity(nil),
		  crtscts(false),
		  xonxoff(false)
			exclusive(false)	| 
		*/
		^super.new( port, 57600, 8, true, nil, false, false, false );
	}

	/*
	init{
		//	DMX.device = this;
	}
	*/
	createSendHeader{ arg data_size = 512;
		// header consists of: 0x7E, label (in this case 6), datasize low byte, datasize high byte;
		//	ser.write(chr(data_size & 0xFF))
		//	ser.write(chr((data_size >> 8) & 0xFF))
		//		^Int8Array[ 0x7E, 6, data_size.bitAnd( 0xFF ), (data_size >> 8).bitAnd( 0xFF ) ];
		^[ 0x7E, 6, data_size.bitAnd( 0xFF ), (data_size >> 8).bitAnd( 0xFF ) ];
	}

	createFooter{
		//	^Int8Array[ 0xE7 ];
		^[ 0xE7 ];
	}
}