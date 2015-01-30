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

+ Object {
	
	sublassMethodExistsFor { |selector|
		^this.class.findRespondingMethodFor( selector ) != 
			Object.findRespondingMethodFor( selector )
	}
	
	prettyPrintIndent { |indent = 0|
		^String.fill( indent, $\t );
	}
	
	prettyPrintOn { |stream, indent = 0|
		if( this.sublassMethodExistsFor( \storeOn ) ) {
			stream << this.prettyPrintIndent( indent );
			this.storeOn( stream ); // use subclass method instead
		} {
			stream << this.prettyPrintIndent( indent ) << this.class.name;
			this.prettyPrintParamsOn( stream, indent );
			this.prettyPrintModifiersOn( stream, indent );
		};
	}
	
	prettyPrintParamsOn { arg stream, indent = 0;
		var args = this.storeArgs;
		if(args.notEmpty) {
			args = this.simplifyStoreArgs( args );
			stream << "(";
			args.prettyPrintItemsOn( stream, indent + 1 );
			stream  << ")";
		} {
			stream << ".new"
		}
	}
	
	prettyPrintModifiersOn { arg stream;
		this.storeModifiersOn( stream );
	}
	
	asPrettyString { |indent = 0|
		^String.streamContents({ arg stream; this.prettyPrintOn(stream, indent); });
	}
	
}

+ Collection {
	
	prettyPrintOn { | stream, indent |
		if (stream.atLimit) { ^this };
		stream << this.prettyPrintIndent( indent ) << this.class.name << "[ " ;
		this.prettyPrintItemsOn(stream, indent + 1);
		stream << "]" ;
	}
	
	prettyPrintGetItems { |indent = 0|
		^this.collect { | item |
			item.asPrettyString( indent )[indent..];
		};
	}
	
	prettyPrintItemsOn { | stream, indent = 0 |
		var items, indentString;
		items = this.prettyPrintGetItems( indent );
		if( items.any( _.includes( $\n ) ) or: { 
			items.collect({ |item| item.size + 2 }).sum > 80
		}) {
			indentString = this.prettyPrintIndent( indent );
			stream << "\n" << items.collect({ |item|
				indentString ++ item;
			}).join( ",\n" ) << "\n" << this.prettyPrintIndent( indent - 1 );
		} {
			stream << items.join( ", " ) << " ";
		};
	}
	
}

+ Array {
	prettyPrintOn { | stream, indent |
		if (stream.atLimit) { ^this };
		stream << this.prettyPrintIndent( indent ) << "[ ";
		this.prettyPrintItemsOn(stream, indent + 1);
		stream << "]" ;
	}
}
