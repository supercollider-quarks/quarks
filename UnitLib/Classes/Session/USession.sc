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

USession : UArchivable{

    /*
    *   objects -> Array[UChain,UChainGroup,UScore or UScoreList]
    */
    var <objects, <name = "untitled";

    *new{ |...objects|
        ^super.new.init(objects);
    }

    init { |inObjects|
        objects = if(inObjects.size > 0){ inObjects }{ [] };
    }

    *current { ^USessionGUI.current !? _.session }

    *acceptedClasses{
        ^[UChain,UChainGroup,UScore,UScoreList]
    }

    at { |...path| 
		 var out;
		 out = objects;
		 path.do({ |item|
			 out = out[ item ];
		 });
		 ^out
	}
    copySeries { |first, second, last| ^objects.copySeries( first, second, last ) }
    collect { |func|  ^objects.collect( func );  }
    do { |func| objects.do( func ); }
    first { ^objects.first }
    last { ^objects.last }
    indexOf { |obj|
		var index;
		index = objects.indexOf( obj );
		if( index.isNil ) {
			objects.do({ |item, i|
				index = [ i, item.indexOf( obj ) ];
				if( index[1].notNil ) { ^index; }
			});
			^nil;
		} {
			^index
		};
	}


    add { |items|
        var oldSize = objects.size;
        objects = objects ++ items
            .asCollection
            .select{ |x|USession.acceptedClasses.includes(x.class) };
        if(objects.size != oldSize) {
            this.changed(\objectsChanged)
        }
    }

    remove { |item|
        objects = objects.select( _ != item);
        this.changed(\objectsChanged)
    }

    insert { |index,item|
        objects = objects.insert(index,item);
        this.changed(\objectsChanged)
    }

    insertCollection { |index,newObjects|
        objects = objects[..(index-1)]++newObjects++objects[index..];
        this.changed( \objectsChanged )
    }

    prepareAndStart { |targets|
		objects.do( _.prepareAndStart(targets) );
    }

    startChains { |targets|
        var chains = objects.select(_.isUChainLike);
        chains.do(_.prepareAndStart(targets))
        ^chains
    }
    
    startScores { |targets|
        var scores = objects.select(_.isUScoreLike);
        scores.do(_.prepareAndStart(targets))
        ^scores
    }

    release {
        objects.do(_.release)
    }

    gui { ^USessionGUI(this) }

    getInitArgs {
		^objects;
	}

	storeArgs { ^this.getInitArgs }
	
	textArchiveFileExtension { ^"usession" }

	onSaveAction { this.name = filePath.basename.removeExtension }
	
	readTextArchiveAction{ this.name = filePath.basename.removeExtension }

	name_ { |x| name = x; this.changed(\name) }

}