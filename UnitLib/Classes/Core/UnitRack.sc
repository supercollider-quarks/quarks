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

UnitRack {
    classvar <>all, <>defsFolders;
    var <name, units;
    var <>category;

   	*initClass{
   		defsFolders = [
   			this.filenameSymbol.asString.dirname.dirname.dirname +/+ "UnitRacks"
   		];
   	}

    *new{ |name, units|
        ^super.newCopyArgs(name,units.collect(_.asUnit)).addToAll(name)
    }

    *loadAllFromDefaultDirectory {
        ^this.defsFolders.reverse.collect({ |path|
            (path ++ "/*.scd").pathMatch.collect({ |path| path.load })
        }).flatten(1);
    }

    addToAll { |name|
        this.class.all ?? { this.class.all = IdentityDictionary() };
        this.class.all[ name ] = this;
    }

    units {
        ^units.collect(_.deepCopy)
    }
}