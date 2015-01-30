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

// a quick hack to get around the NodeWatcher bug in current sc

NodeActionKeeper {
	classvar <>freeDict, <>startDict;
	*initClass { 
		freeDict = IdentityDictionary();
		startDict = IdentityDictionary();
	}
}



+ Node {
	freeAction2_ { |action| // performs action once and then removes it
		if( NodeActionKeeper.freeDict[ this ].notNil ) {
			NodeActionKeeper.freeDict[ this ].remove;
		};
		
		NodeActionKeeper.freeDict[ this ] = OSCresponderNode( this.server.addr,
			'/n_end', { |time, resp, msg|
				if( msg[1] == this.nodeID ) {
					action.value( this );
					resp.remove;
					if( NodeActionKeeper.freeDict[ this ] == resp ) {
						NodeActionKeeper.freeDict[ this ] = nil;
					};
				};
			}
		).add;	
	}
	
	startAction2_ { |action| // performs action once and then removes it
		if( NodeActionKeeper.startDict[ this ].notNil ) {
			NodeActionKeeper.startDict[ this ].remove;
		};
		
		NodeActionKeeper.startDict[ this ] = OSCresponderNode( this.server.addr,
			'/n_go', { |time, resp, msg|
				if( msg[1] == this.nodeID ) {
					action.value( this );
					resp.remove;
					if( NodeActionKeeper.startDict[ this ] == resp ) {
						NodeActionKeeper.startDict[ this ] = nil;
					};
				};
			}
		).add;	
	}
}