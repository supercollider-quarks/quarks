/*
    Unit Library
    The Game Of Life Foundation. http://gameoflife.nl
    Copyright 2006-2011 Miguel Negrao, Wouter Snoei.

    GameOfLife Unit Library: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GameOfLife Unit Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GameOfLife Unit Library.  If not, see <http://www.gnu.org/licenses/>.
*/

/* 
this is here as long as there isn't any incoming pattern matching functionality in sc itself.
It is used for UOSCsetter
*/

OSCMethodPatternDispatcher : OSCMessageDispatcher { 
	
	classvar <>compatibilityMode = false;
	
	*initClass { 
		compatibilityMode = OSCMessageDispatcher.findMethod( \value ).argNames[1] == \time;
	}
	
	value {|msg, time, addr, recvPort| 
		var pattern;
		if( compatibilityMode ) {
			#time, addr, recvPort, msg = [ msg, time, addr, recvPort ];
		};
		pattern = msg[0];
		active.keysValuesDo({|key, func|
			if(pattern.matchOSCAddressPattern(key), {func.value(msg, time, addr, recvPort);});
		})
	}
	
	typeKey { ^('OSC patternmatched').asSymbol }
	
}