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

LocalUdef : Udef {

	classvar <tempDefCount;
	classvar <>maxTempDefNames = 512;
	classvar <>synthDefDict;

	var <>name;

	*initClass{
	    tempDefCount = Dictionary.new;
	    synthDefDict = IdentityDictionary( );
	}

    *new { |name, func, args, category|
        ^super.basicNew( name, args, category, false ).name_(name).init(func)
    }

    *fromUdef { |def|
        var udef;
        switch(def.class)
            {Udef}{ udef = def }
            {Symbol}{ udef = Udef.all.at(def) };
        ^this.new(udef.name, udef.func, udef.argSpecs, udef.category).shouldPlayOnFunc_(def.shouldPlayOnFunc)
    }

    *prefix { ^"u_temp_" }

    *callByName { ^false }

    prGenerateSynthDefName {
        var defName = this.class.prefix ++ this.name.asString;
        var name = this.name.asString;
        var x = tempDefCount.at(name);
        var y;
        if( x.isNil ) {
            tempDefCount.put(name,1);
            ^defName++"-"++1;
        } {
            y = x+1 % maxTempDefNames;
            tempDefCount.put(name, y);
            ^defName++"-"++y;
        }
    }

    synthDef {
        ^synthDefDict.at(this) ?? {
            var def = SynthDef( this.prGenerateSynthDefName, func );
            this.synthDef_(def);
            def
        }
    }

    synthDef_ { |def|
        synthDefDict.put(this, def)
    }

    func_ { |inFunc|
        func = inFunc;
        this.rebuild;
    }

    prepare { |servers, unit, action|
        action = MultiActionFunc( action );
        servers.do({ |server|
            var innerAction = action.getAction;
            this.synthDef.send(server);
            if( UEvent.nrtMode != true ) {
           	 OSCresponderNode( server.addr, '/done', { |time, resp, msg, addr|
	                if( msg == [ '/done', '/d_recv' ]  ) {
	                    resp.remove;
	                    innerAction.value;
	                };
	           }).add;
            } {
	            innerAction.value;
            };
        })
    }
    
    needsPrepare { ^true }

    asUdef { }

    asOriginalUdef {
        ^name.asUdef
    }

    saveAsUdef { |action|

        Dialog.savePanel( { |path|
            var name = path.basename.removeExtension;
            path = path.dirname+/+name++".scd";
            Udef(name,func,argSpecs,category).write(path, successAction: { action.value(name) } );
        } );

    }

}