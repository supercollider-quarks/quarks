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


UOSCsetter {
	
	classvar <>all;
	
	var <>uobject;
	var <name;
	var <oscfunc;
	
	*initClass {
		all = Set();
	}
	
	*new { |uobject, name|
		^super.newCopyArgs( uobject, name ).init;
	}
	
	init {
		
		if( name.isNil && { uobject.respondsTo( \name ) } ) {
			name = uobject.name;
		} {
			"%:init - missing name: did not create OSCFunc\n".postf( this.class );
			^this;
		};
		
		oscfunc = OSCFunc({ |msg| 
			this.set( *msg );
		}, this.oscPath, dispatcher: OSCMethodPatternDispatcher.new );
		
		oscfunc.permanent = true;
		oscfunc.enable;
		"started UOSCsetter for %\n - messages should start with '/%/'\n - port: %\n".postf( uobject, name, NetAddr.langPort );
	}
	
	addToAll { all.add( this ) }
	
	oscPath { ^"/" ++ name ++ "/*" }
	
	set { |pth ...inArgs|
		var obj;
		var path, args;
		var setObj, setter;
		path = pth.asString[this.oscPath.size-1..];
		path = path.split($/).collect({ |item|
			item = item.asNumberIfPossible;
			if( item.isNumber ) {
				item;
			} {
				item.asSymbol;
			}
		});
		
		args = path.select(_.isKindOf( Symbol )) ++ inArgs;
		path = path.select(_.isNumber);
		
		if( { 
			if( path.size > 0 ) {
				obj = uobject.at( *path );
			} {
				obj = uobject;
			};
		}.try.notNil ) {
			switch( args.size,
				0, { 
					"%:set - received message points to object:\n\t".postf( this.class );
					obj.asCompileString.postln;
				},
				1, {
					if( obj.respondsTo( args[0].asSymbol ) ) { 
						obj.perform( args[0].asSymbol );
					} {
						"%:set - object does not understand '%':\n\tobject: "
							.postf( this.class, args[0] );
						obj.asCompileString.postln;
						"\tmessage: %\n".postf( [ pth ] ++ args );
					};
				}, 
				2, {
					if( obj.respondsTo( \set ) ) {
						obj.set( *args );
					} {
						obj.perform( *[args[0].asSymbol] ++ args[1..] );
					};
				},
				{
					if( args[1].class == Symbol ) {
						setObj = obj.get(args[0]);
						setter =  args[1].asSetter;
						if( setObj.respondsTo( setter ) ) {
							obj.set( args[0], setObj.perform( setter, *args[2..] ) );
						};
					} {
						if( args[0] == \point ) { // special case
							obj.set( \point, args[[1,2]].asPoint );
						} {
							if( obj.respondsTo( \set ) ) {
								obj.set( args[0], args[1..] ); // array
							} {
								obj.perform( *[args[0].asSymbol] ++ args[1..] );
							};
						};
					};
				}
			);	
		} {
			"%:set - received message points to non-existing object:\n\tmessage: %\n"
				.postf( this.class, [ pth ] ++ args );
		};
	}
	
	enable { oscfunc.enable }
	
	disable { oscfunc.disable }
	
	remove { 
		oscfunc.free;
		all.remove( this );
	}
	
	*disable { 
		all.do(_.disable);
	}
	
	*enable {
		all.do(_.enable);
	}
	
}