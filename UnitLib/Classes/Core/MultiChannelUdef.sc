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

MultiChannelUdef : MultiUdef {
	
	classvar <>channelSpec;
	
	var <>chSpec;
	
	*defNameKey { ^\numChannels }
	
	*initClass {
		channelSpec = ListSpec([1,2,3,4,5,6,7,8,10,12,16,24,32]);
	}
	
	*new { |name, func, args, category, setterIsPrivate = false, channels, addToAll=true| 
		var chSpec = channelSpec;
		if( channels.notNil ) { chSpec = ListSpec(channels); };
		^super.basicNew( name, [ 
			ArgSpec( this.defNameKey, 
				chSpec.default, chSpec, setterIsPrivate, \nonsynth )
		], category, addToAll)
			.chSpec_( chSpec )
			.func_( func )
			.initUdefs( name, args );
	}

	initUdefs { |name, args|
		udefs = chSpec.list.collect({ |numChannels|
			MultiChannelSubUdef( name, numChannels, func, args );
		});
	}
}

MultiChannelSubUdef : Udef {
	
	var <>prDefName;
	
	*prefix { ^"umc_" }
	
	*new { |name, numChannels = 1, func, args|
		^this.basicNew( name, args, \private, false )
			.numChannels_(numChannels)
			.prDefName_( name )
			.init( func ); 
	}
	
	prGenerateSynthDefName {
       ^this.class.prefix ++ (extraPrefix ? "") ++ this.prDefName.asString ++ "_" ++ numChannels;
    }
	
	name { ^numChannels }
}