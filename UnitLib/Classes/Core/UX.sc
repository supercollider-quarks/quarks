/*
    Unit Library
    The Game Of Life Foundation. http://gameoflife.nl
    Copyright 2014 Miguel Negrao, Wouter Snoei.

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

/*
UChain( UX(4, \sine), UX(4, \whiteNoise), UX(4, \lowPass), [\output, [\numChannels, 4] ]).gui

UChain( [4, \sine], [4, \whiteNoise], [4, \lowPass], [\output, [\numChannels, 4] ]).gui


Udef(\test,{
	UIn.ar(0,2)
})

UChain( UX(4, \test) )

UChain( [4, \sine], [4, \whiteNoise], [4, \lowPass], [\output, [\numChannels, 4] ]).asCompileString
*/

UX : U {

	var <n;

	//private
	var counter;
	var hasIn;
	var hasOut;

	*new { |n, def, args, mod|
		^super.new().init2(n, def, args ? [], mod )
	}

	init { }

	init2 { |nArg, in, inArgs, inMod|
		if( nArg.isKindOf(SimpleNumber).not ) {
			Error("UX : n must be a number ! got: %".format(nArg)).throw
		};
		n = nArg;
		if( in.isKindOf( this.class.defClass ) ) {
			def = in;
			defName = in.name;
			if( defName.notNil && { defName.asUdef( this.class.defClass ) == def } ) {
				def = nil;
			};
		} {
			defName = in.asSymbol;
			def = nil;
		};
		if( this.def.notNil ) {
			var ldef = this.def;
			var keys = ldef.keys;
			var numIns = keys.select{ |key| "u_i_ar_.*_bus".matchRegexp(key.asString)  }.size;
			var numOuts = keys.select{ |key| "u_o_ar_.*_bus".matchRegexp(key.asString)  }.size;
			if(numIns > 1) {
				Error("UX - must use Udef with no more then one audio input. Udef % has % audio inputs.".format(ldef.name, numIns)).throw
			};
			if(numOuts > 1) {
				Error("UX - must use Udef with no more then one audio output. Udef % has % outputs.".format(ldef.name, numOuts)).throw
			};
			if((numIns==0)&&(numOuts==0)) {
				Error("UX - %: must use Udef with at least one audio input or output.".format(ldef.name)).throw
			};
			if( (numIns==0)&&(numOuts==0) ) {
				Error("UX - must use Udef with at least one input or output").throw
			};
			hasIn = numIns > 0;
			hasOut = numOuts > 0;
			args = this.def.asArgsArray( inArgs ? [], this );
		} {
			Error("UX def '%' not found").format(in).throw;
		};
		preparedServers = [];
		mod = inMod.asUModFor( this );

		this.changed( \init );
	}

	getSynthArgs {
		var nonsynthKeys = this.argSpecs.select({ |item| item.mode == \nonsynth }).collect(_.name);
		var inOut = if(hasIn){
			[ \u_i_ar_0_bus, this.getArg(\u_i_ar_0_bus)+counter ]
		}++if(hasOut){
			[ \u_o_ar_0_bus, this.getArg(\u_o_ar_0_bus)+counter]
		};
		^(this.args.clump(2).select({ |item|
			var key = item[0].asString;
			(nonsynthKeys.includes( item[0] ) ||
			"u_i_ar_.*_bus".matchRegexp(key) ||
			"u_o_ar_.*_bus".matchRegexp(key) ).not
		})++inOut).flatten(1);
	}

	makeSynth { |target, startPos = 0, synthAction|
		var synths = n.collect{ |i|
			counter = i;
			this.def.makeSynth( this, target, startPos, synthAction );
		}.select(_.notNil);
		if( synths.size == n ) {
			this.umapPerform( \makeSynth, synths[0], startPos );
		};
	}

	apxCPU {
		^n*super.apxCPU
	}

	storeArgs {
		^[n]++super.storeArgs
	}

}
