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

FreeUdef : Udef {
	
	// a freeUDef can hold any function for any type of functionality
	// fully customizable
	
	// please note that the createSynthFunc must return a single synth
	// if there are more synths started please either add them to the env
	// or to the unit.synths in the func. This synth will be tracked to
	// let the unit know if it stopped or not. It may also be a Group,
	// but in that case any UEnv will not work (unless applied to a synth
	// outside the group).

	var <>createSynthFunc, <>setSynthFunc;
	var <>env; // environment for variables
	
	var <>createSynthDefFunc; // optional, called at load or send
	var <>synthsForUMapFunc;
	
	*new { |name, args, canFreeSynth = false, category|
		^super.basicNew( name, args ? [], category ).initFree( canFreeSynth ); 
	}
	
	initFree { | canFreeSynth |
		env = ();
		if( canFreeSynth ) { this.addUEnv };
		this.initArgs;
		if( loadOnInit ) { this.loadSynthDef };
	}
	
	synthDef_ { |def|
		synthDef = def;
		if( loadOnInit ) { this.loadSynthDef };
	}
	
	addSynthDefControls { |def, inArgSpecs|
		def = def ? synthDef;
		if( def.notNil ) {
			this.class.buildUdef = this;
			this.class.buildArgSpecs = [];
			SynthDef( "tmp", def.func );
			this.class.buildUdef = nil;
		};
		this.prAddSynthDefControls( def, inArgSpecs ++ this.class.buildArgSpecs ); 
	}
	
	prAddSynthDefControls { |def, inArgSpecs|
		def = def ? synthDef;
		ArgSpec.fromSynthDef( def, inArgSpecs ).do({ |argSpec| this.addArgSpec( argSpec ); });
		this.initArgs; // make private if needed
	}
	
	removeSynthDefControls { |def, inArgSpecs|
		def = def ? synthDef;
		ArgSpec.fromSynthDef( def, inArgSpecs ).do({ |argSpec| this.removeArgSpec( argSpec ); });
	}
	
	addUIO { |class, selector ...args| 
		// create a temp synthdef to get the correct args 
		// this assumes you have a UEnv in at least one of 
		// the synths you are running, or you use the UEnv controls in 
		// some other way to release the synths, set its duration etc
		var def;
		this.class.buildUdef = this;
		this.class.buildArgSpecs = [];
		def = SynthDef( "tmp", { class.perform( selector, *args) } );
		this.class.buildUdef = nil;
		this.prAddSynthDefControls( def, this.class.buildArgSpecs ); 
	}
	
	removeUIO { |class, selector ...args|
		var def;
		this.class.buildUdef = this;
		this.class.buildArgSpecs = [];
		def = SynthDef( "tmp", { class.perform( selector, *args) } );
		this.class.buildUdef = nil;
		this.removeSynthDefControls( def, this.class.buildArgSpecs ); 	}
	
	addUEnv { this.addUIO( UEnv, \kr ); }
	removeUEnv {  this.removeUIO( UEnv, \kr ); }
	
	addUGlobalEQ { this.addUIO( UGlobalEQ, \ar, { Silent.ar } ); }
	removeUGlobalEQ {  this.removeUIO( UGlobalEQ, \ar, { Silent.ar } ); }
		
	envPut { |key, value|
		env.put( key, value );
	}
	
	canFreeSynth_ { |bool| 
		if( bool ) { this.addUEnv } { this.removeEnv };
	}

	
	createSynthDef { synthDef = createSynthDefFunc.value( this ) ? synthDef; }
		
	loadSynthDef { |server|
		this.createSynthDef;
		if( synthDef.notNil ) {
			server = server ? ULib.servers ? Server.default;
			server.asCollection.do{ |s|
				if( s.class == LoadBalancer ) {
					s.servers.do({ |s|
						synthDef.asCollection.do(_.send(s));
					});
				} {
					synthDef.asCollection.do(_.send(s));
				};
			}
		}
	}
	
	sendSynthDef { |server|
		this.createSynthDef;
		if( synthDef.notNil ) {	
			server = server ? ULib.servers ? Server.default;
			server.asCollection.do{ |s|
				if( s.class == LoadBalancer ) {
					s.servers.do({ |s|
						synthDef.asCollection.do(_.send(s));
					});
				} {
					synthDef.asCollection.do(_.send(s));
				};
			}
		}
	}
	
	synthDefName { 
		if( synthDef.notNil ) { 
			if( synthDef.isArray ) {
				^synthDef.collect(_.name) 
			} {
				^synthDef.name;
			};
		} { 
			^nil 
		} 
	}
	
	createSynth { |unit, server, startPos = 0| // create a single synth based on server
		if( createSynthFunc.notNil ) {
			server = server ? Server.default;
			^createSynthFunc.value( unit, server, startPos );
		} {
			^super.createSynth( unit, server, startPos );
		};
	} 
	
	setSynth { |unit ...keyValuePairs|
		if( setSynthFunc.notNil ) {
			setSynthFunc.value( unit, *keyValuePairs );
		} { 
			super.setSynth( unit, *keyValuePairs );
		};
	}
	
	synthsForUMap { |unit|
		if( synthsForUMapFunc.notNil ) {
			^synthsForUMapFunc.value( unit );
		} {
			^super.synthsForUMap( unit );
		};
	}
	
	printOn { arg stream;
		stream << "a " << this.class.name << "(" <<* [this.name]  <<")"
	}

	storeOn { arg stream;
		stream << this.class.name << "(" <<* [
			this.name.asCompileString, 
			argSpecs.asCompileString,
			createSynthFunc.asCompileString,
			setSynthFunc.asCompileString,
			this.canFreeSynth,
			category.asCompileString
		]  <<")"
	}

}