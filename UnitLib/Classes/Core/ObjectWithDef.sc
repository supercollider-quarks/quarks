/*
    Unit Library
    The Game Of Life Foundation. http://gameoflife.nl
    Copyright 2006-2012 Miguel Negrao, Wouter Snoei.

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

ObjectWithDef : ObjectWithArgs {
	
	var def, defName;
	
	*new { |def, args|
		^super.new.init( def, args );
	}
	
	*defClass { ^GenericDef }
	
	// subclass responsibility
	*asDefMethod { ^\value }
	
	init { |inDef, inArgs|
		if( inDef.isKindOf( this.class.defClass ) ) {
			def = inDef;
			defName = inDef.name;
			if( defName.notNil && { defName.perform( this.class.asDefMethod ) == def } ) {
				def = nil;
			};
		} {
			defName = inDef.asSymbol;
			def = nil;
		};
		if( this.def.notNil ) {
			args = this.def.asArgsArray( inArgs ? [] )
		} {
			args = inArgs;
			"def '%' not found".format(inDef).warn;
		};
	}
	
	def {
        ^def ?? { defName.asUdef }
    }

    defName {
        ^defName ?? { def.name }
    }

    def_ { |newDef, keepArgs = true|
        this.init( newDef, if( keepArgs ) { args } { [] });
    }

    defName_ { |newDefName, keepArgs = true|
        this.init( newDefName, if( keepArgs ) { args } { [] });
    }
	
}