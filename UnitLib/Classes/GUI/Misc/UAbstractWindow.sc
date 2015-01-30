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

UAbstractWindow {

    classvar <currentDict, <allDict;
    var <window, <view;

    *initClass {
        currentDict = ();
        allDict = ();
    }
    *current { ^currentDict[this] }

    *current_ { |x| currentDict[this] = x }
    
    *all { ^allDict[this] }

    toFront {
        if( window.isClosed.not ) {
         window.front;
        };
    }

	close {
        if( window.isClosed.not ) {
         window.close;
        };
    }

    addToAll {
        allDict[this.class] = allDict[this.class].asCollection.add( this );
    }

    removeFromAll { if( allDict[this.class].notNil ) { allDict[this.class].remove( this ); }; }

    newWindow { |bounds, title, onClose, background, margin, gap|

		var font = Font( Font.defaultSansFace, 11 );
        bounds = bounds ? Rect(230 + 20.rand2, 230 + 20.rand2, 680, 300);

        window = Window(title, bounds).front;
        window.onClose_(onClose);
        window.toFrontAction_({ currentDict[this.class] = this });
        margin = margin ? 4;
        gap = gap ? 2;
        view = window.view;
        view.background_( background ? Color.grey(0.6) );
        view.resize_(5);

        window.onClose = window.onClose.addFunc({ this.removeFromAll });
    }


}