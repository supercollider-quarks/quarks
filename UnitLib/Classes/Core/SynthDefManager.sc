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

SynthDefManager {
	classvar <>dict;
	
	*initClass {
		dict = ();
	}
	
	*load { |target, def, completionMsg, dir|
		var server, bytes;
		server = target.asTarget.server;
		if( dict[ server ].isNil or: { dict[ server ].includes( def ).not } ) {
			if( server.isLocal ) {
				def.load( server, completionMsg, dir );
			} {
				this.prSend( server, completionMsg, dir );
			};
			(dict[ server ] ? []).removeAllSuchThat({ |item| item.name == def.name });
			dict[ server ] = dict[ server ].add( def );
		};
	}
	
	*send { |target, def, completionMsg, dir|
		var server, bytes;
		server = target.asTarget.server;
		if( dict[ server ].isNil or: { dict[ server ].includes( def ).not } ) {
			this.prSend( server, def, completionMsg );
		};
		(dict[ server ] ? []).removeAllSuchThat({ |item| item.name == def.name });
		dict[ server ] = dict[ server ].add( def );
	}
	
	*prSend { |server, def, completionMsg|
		var bytes;
		bytes = def.asBytes;
		if(bytes.size > (65535 div: 4)) {
			"synthdef may have been too large to send to remote server".warn;
		};
		server.sendMsg("/d_recv", bytes, completionMsg);
	}
	
	*reset { |target|
		if( target.isNil ) {
			dict = ();
		} {
			dict[ target.asTarget.server ] = nil;
		};
	}
	
	*removeDef { |def, target|
		if( target.isNil ) {
			dict.values.do(_.remove(def));
		} {
			dict[ target.asTarget.server ].remove(def);
		};
	}
	
}
