/*SCyduino - A python library to interface with the firmata arduino firmware, implemented
for SuperCollider3 by Eirik Blekesaune.
 
Copyright (C) 2009 Eirik Blekesaune <blekesaune@gmail.com>
Copyright (C) 2007 Joe Turner <orphansandoligarchs@gmail.com>

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

//SCPyduino version 0.11

Firmata {
	var <digital_message;
	var <analog_message;	
	var <report_analog_pin;
	var <report_digital_ports;
	var <start_sysex;
	var <set_digital_pin_mode;
	var <end_sysex;
	var <report_version;
	var <system_reset;
	var <pinMode;
	var <pwm_pins;
	
	var <unavailable;
	var <digital_input;
	var <digital_output;
	var <digital_pwm;
	
	*new{
		^super.new.initFirmata();
	}
	
	initFirmata{
		digital_message = 0x90;//Send data for digital pins
		analog_message = 0xE0;// Send data for analog pins (or PWM)
	
		report_analog_pin = 0xC0; // enable report for analog pin
		report_digital_ports = 0xD0; // enable digital input by port pair
		start_sysex = 0xF0; // Start a MIDI SysEx message
		set_digital_pin_mode = 0xF4; // set a digital pin to INPUT or OUTPUT
		end_sysex = 0xF7; // end a MIDI SysEx message
		report_version = 0xF9; // report firmware version
		system_reset = 0xFF; // reset from MIDI
		pwm_pins = #[3, 5, 6, 9, 10, 11];
		
		unavailable = -1;
		digital_input = 0;
		digital_output = 1;
		digital_pwm = 3;
		
		
	}

}
SCPyduino : Firmata {
	
	var <baudrate, <port, <sp;
	var <analog, <digital, <digitalPorts, <firmataVersion;
	
	*new{ |aPort, aBaudrate = 115200|
		^super.new.initSCPyduino(aPort, aBaudrate);
	}
	
	initSCPyduino{ |aPort, aBaudrate|
		port = aPort;
		baudrate = aBaudrate;
		
		
		
		
		Routine.new({
			sp = SerialPort(port, baudrate, crtscts: true);
			2.wait; // Allow 2 secs for Arduino auto-reset to happen
			
			firmataVersion = "None";
			this.sp.put(report_version);
			this.iterate;
			
			analog = { |i| SCPyduinoAnalogPin.new(sp, i) } ! 6;
			digitalPorts = { |i| SCPyduinoDigitalPort(sp, i) } ! 2;
			
			digitalPorts[0].pins[0].mode = unavailable;
			digitalPorts[0].pins[1].mode = unavailable;
			
			digitalPorts[1].pins[6].mode = unavailable;
			digitalPorts[1].pins[7].mode = unavailable;
			
			digital = digitalPorts[0].pins[0..7] ++ digitalPorts[1].pins[0..6];
			
			
		
		}).play;
	}
	
	iterate{
		var data;
		data = this.sp.read;
		data.notNil.if( {this.prProcessInput(data)} );
	}
	
	prProcessInput{ |data|
		var message, portNum, pinNum, lsb, msb, value;
		var minor, major;
		
		if(data < 0xF0,
			{
				message = data & 0xF0;
					if(message == digital_message,
						{
							portNum = data & 0x0F;
							while({lsb.isNil}, {lsb = this.sp.read});
							while({msb.isNil}, {msb = this.sp.read});
							lsb = lsb;
							msb = msb;
							digitalPorts[portNum].value_(msb << 7 | lsb);
						},
						{
						if(message == analog_message)
							{
								pinNum = data & 0x0F;
								while({lsb.isNil}, {lsb = this.sp.read});
								while({msb.isNil}, {msb = this.sp.read});
								lsb = lsb;
								msb = msb;
								analog[pinNum].value_(msb << 7 | lsb);
							}
						});
			},
			{
				if(data == report_version) 
					{
					
					major = this.sp.read;
					minor = this.sp.read;
					firmataVersion = major.asString ++ "." ++ minor.asString;
					};
			});
		
	}
	
	close{
		this.sp.close;
	}

}

SCPyduinoDigitalPort : Firmata{
	var <sp, <portNum, <active, <pins;
	
	*new{ |argSp, argPortNum|
		^super.new.initDigitalPort(argSp, argPortNum);
	}
	
	initDigitalPort{ |argSp, argPortNum|
		sp = argSp;
		portNum = argPortNum;
		active = 0;
		pins = { |i| SCPyduinoDigitalPin.new(sp, this, i) } ! 8
	}
	
	value_{ |mask|
		pins.collect({ |item|
			if(item.mode == digital_input)
				{ item.value_((mask & (1 << item.pinNum)) > 1)};
		});
	}
	
	write{
		var mask, message;
		message = [];
		mask = 0;
		
		pins.collect({|item|
			if(item.mode == digital_output,
				{	
					if(item.value == 1)
						{mask = mask | (1 << item.pinNum)};
				}
			)
		});
		
		this.sp.putAll(Int8Array[
			digital_message + portNum,
			mask % 128,
			mask >> 7
		]);
	}
	
	active_{ |argValue|
		active = argValue;
		this.sp.putAll(Int8Array[
			report_digital_ports + this.portNum,
			active
		]);
	}
	
}

SCPyduinoDigitalPin : Firmata{
	
	var <sp, <port, <pinNum, <>value, <mode;
	
	*new{ |argSp, argPort, argPinNum|
		^super.new.initDigitalPin(argSp, argPort, argPinNum);
	}
	
	initDigitalPin{ |argSp, argPort, argPinNum|
		sp = argSp;
		port = argPort;
		pinNum = argPinNum;
		value = 0;
		mode = digital_input;
	}
	
	mode_{ | argMode | 
		var message;
		message = [];
		
		//Takes the following symbols as arguments: \digital_output
		
		if(argMode == digital_pwm and: pwm_pins.includes(this.boardPinNumber).not)
			{^"Digital pin does not have pwm capabilities".error};
		if(mode == unavailable)
			{^("Cannot set mode for pin " ++ this.boardPinNumber.asString).error};
		mode = argMode;
		this.sp.putAll(Int8Array[
			set_digital_pin_mode,
			this.boardPinNumber,
			mode
		]);
	}
	
	boardPinNumber{
		^((port.portNum * 8) + pinNum);
	}
	
	write{ |argValue|
		var message;
		if(mode == unavailable) {^"Cannot write to pin".error};
		if(mode == digital_input, 
			{^"Pin is not an input".error},
			{
			if(argValue != value) 
				{
					
					value = argValue;
					if(mode == digital_output,
						{
							
							port.write;
						},
						{
							if(mode == digital_pwm)
								{
								value = ((value * 255).round).asInteger;
								this.sp.putAll(Int8Array[
									analog_message + this.boardPinNumber,
									value % 128,
									value >> 7
								]);
								}
						});
				}
			});
		
	}
	
}

SCPyduinoAnalogPin : Firmata{
	var <>value, <pinNum, <active, <sp;
	*new{ | argSp, argPinNum |
		^super.new.initAnalogPin(argSp, argPinNum);
	}
	
	initAnalogPin{ |argSp, argPinNum |
		value = -1;
		pinNum = argPinNum;
		active = false;
		sp = argSp;
	}
	
	active_{ |argValue|
		active = argValue;
		this.sp.putAll(Int8Array[
			report_analog_pin + pinNum,
			active
		]);
	}


}
/*
Revision history
0.11 - (03.10.09) Fixed receiving firmataVersion from microcontroller.


*/
