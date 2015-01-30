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

GenericDef : UArchivable {
	
	classvar <>all; // overwrite in subclass to create class specific lib
	classvar <>defsFolders, <>userDefsFolder;

	var <>argSpecs;
	var filePath;
	var <>prName;

	*new { |name, args, addToAll = true|
		var x = super.new.initArgSpecs( args );
		x.filePath = thisProcess.nowExecutingPath;
		if(addToAll){
		    x.addToAll( name );
		 } {
			x.prName = name
		 };
		 ^x
	}
	
	*fromName { |name|
		var def;
		this.all ?? { this.all = IdentityDictionary() };
		def = this.all[ name ];
		if( def.isNil ) {
            ^this.getFromFile(name);
		}{
		    ^def
		}
	}

	*getFromFile{ arg name;
		var path;
		path = this.findDefFilePath( name );
		^if( path.notNil ) {
			path.load
		} {
			"%:% - no % found for %\n"
			.postf( this, thisMethod.name, this, this.cleanDefName(name), path );
			nil
		}
	}
	
	*findDefFilePath { arg name;
		var path;
		if( name.notNil ) {
			this.defsFolders.do({ |item|
				path = this.createDefFilePath( item, name );
				if(this.existsCaseSensitive( path ) ) { ^path };
			});
			path = this.createUserDefFilePath( name );
			if( this.existsCaseSensitive( path ) ) { ^path };
			
			^this.findRelativeFilePath( name );
		} {
			^nil;
		};
	}
	
	filePath {
		^filePath ?? { this.class.findDefFilePath( this.name ) };
	}
	filePath_ { |fp| filePath = fp; }
	
	*findRelativeFilePath { arg name, levels = 4;
		var path, dir;
		if( thisProcess.nowExecutingPath.notNil ) {
			
			dir = thisProcess.nowExecutingPath.dirname;
			path = this.createDefFilePath( dir, name );
			
			if( this.existsCaseSensitive( path ) ) { ^path };
			
			(1..levels).do({ |i|
				path = this.createDefFilePath( dir +/+ Array.fill( i, $* ).join( $/ ), name )
					.pathMatch[0];
				if( path.notNil && { this.existsCaseSensitive( path ) } ) { ^path };
			});
			
			^nil;
		} {
			^nil;
		};
	}
	
	write { |path, overwrite=false, ask=true, successAction, cancelAction|
		super.write( path, overwrite, ask, 
			{ |pth| this.filePath = pth; successAction.value( pth ) },
			cancelAction
		);
	}
	
	*existsCaseSensitive { |path|
		^(path.dirname+/+"*").pathMatch.detect{|x|x.compare(path)==0}.notNil
	}
	
	*allDefsFolders {
		if( this.userDefsFolder.notNil ) {
			^this.defsFolders ++ [ this.userDefsFolder ];
		} {
			^this.defsFolders;
		};
	}

	*loadAllFromDefaultDirectory {
	    ^this.allDefsFolders.reverse.collect({ |path|
		    (path ++ "/*.scd").pathMatch.collect({ |path| path.load })
	    }).flatten(1);
	}
	
	*getNamesFromDefaultDirectory {
		^this.allDefsFolders.collect({ |path|
		    (path ++ "/*.scd").pathMatch.collect({ |path| path.basename.splitext[0].asSymbol })
	    }).flatten(1);
	}
	
	*loadOnceFromDefaultDirectory {
		^this.getNamesFromDefaultDirectory.collect({ |item|
			this.fromName( item );
		});
	}

	*cleanDefName{ |name|
		^name.asString.collect { |char| if (char.isAlphaNum, char, $_) };
	}
	
	*createDefFilePath { |folder, defName|
		var cleanDefName = this.cleanDefName(defName);
		^folder +/+ cleanDefName ++ ".scd";
	}

	*createUserDefFilePath{ |defName|
		^this.createDefFilePath( this.userDefsFolder, defName );
	}
	
	openDefFile {
		var path;
		path = this.filePath;
		if( path.notNil ) {
			^path.openDocument
		} {
			^nil;
		};
	}

	initArgSpecs { |args|
		argSpecs = ArgSpec.fromArgsArray( args );
	}
	
	addToAll { |name|
		this.class.all ?? { this.class.all = IdentityDictionary() };
		this.class.all[ name.asSymbol ] = this;
		this.class.all.changed( \added, this );
	}
	
	name { ^this.class.all !? { this.class.all.findKeyForValue( this ); } ? prName }
	
	name_ { |name|
		this.class.all ?? { this.class.all = IdentityDictionary() };
		this.class.all[ this.name ] = nil;
		this.addToAll( name );
	}
	
	*allNames { ^this.class.all.keys.as( Array ).sort }
	
	addArgSpec { |argSpec, replaceIfExists = false|
		var index;
		if( argSpec.notNil ) {
			argSpec = argSpec.asArgSpec;
		};
		index = argSpecs.detectIndex({ |item| item.name == argSpec.name });
		if( index.isNil ) {
			argSpecs = argSpecs.add( argSpec );
		} {
			if( replaceIfExists ) {	 // otherwise leave in tact (use setArgSpec to change spec)
				argSpecs[index] = argSpec;
			};
		};
	}
	
	removeArgSpec { |name|
		var index;
		if( name.isKindOf( ArgSpec ) ) { name = name.name };
		index = this.getArgIndex( name );
		if( index.notNil ) {
			^argSpecs.removeAt( index );
		} {
			^nil
		};
	}
	
	
	// getters 
	
	getArgIndex { |name|
		name = name.asSymbol;
		^argSpecs.detectIndex({ |item| item.name == name });
	}
	
	getArgSpec { |name|
		name = name.asSymbol;
		^argSpecs.detect({ |item| item.name == name });
	}
	
	getSpec { |name|
		var asp;
		asp = this.getArgSpec(name);
		if( asp.notNil ) { ^asp.spec } { ^nil };
	}
	
	getDefault { |name|
		var asp;
		asp = this.getArgSpec(name);
		if( asp.notNil ) { ^asp.default } { ^nil };
	}
	
	// setters
	
	setArgSpec { |argSpec|
		var index;
		argSpec = argSpec.asArgSpec;
		index = this.getArgIndex( argSpec.name );
		if( index.notNil ) { 
			argSpecs[index] = argSpec;
		} { 
			"%:setArgSpec - can't set because arg % for % not found"
				.format( this.class, argSpec.name, this.name )
				.warn;
		};
	}
	
	setDefault { |name, default|
		var asp;
		asp = this.getArgSpec(name);
		if( asp.notNil ) { asp.default = default ? asp.default };
	}
	
	setSpec { |name, spec|
		var asp;
		asp = this.getArgSpec(name);
		if( asp.notNil ) { asp.spec = spec.asSpec };
	}

	/**
	* argPairs -> an array with key/value pairs
	* returns -> an array with key/value pairs for all arguments of the synthdef.
	* if the argPairs arrays doesn't have a certain argument, the default value
	* is used.
	*/
	asArgsArray { |argPairs, constrain = true|
		argPairs = argPairs ? #[];
		^argSpecs.collect({ |item| 
			var val;
			val = argPairs.pairsAt(item.name) ?? { item.default.copy };
			if( constrain ) { val = item.constrain( val ) };
			[ item.name,  val ] 
		}).flatten(1);
	}
	
	args { ^argSpecs.collect({ |item| [ item.name, item.default ] }).flatten(1) } 
	
	constrain { |...nameValuePairs|
		^nameValuePairs.clump(2).collect({ |name, value|
			this.prConstrain(name, value);
		}).flatten(1);
	}
	
	prConstrain { |name, value|
		^[ name, this.getArgSpec( name ).constrain( value ) ];
	}
	
	keys { ^argSpecs.collect(_.name) }
	argNames { ^argSpecs.collect(_.name) }
		
	values { ^argSpecs.collect(_.default) }
	
	specs { ^argSpecs.collect(_.spec) }
	
    archiveAsCompileString { ^true }

}

