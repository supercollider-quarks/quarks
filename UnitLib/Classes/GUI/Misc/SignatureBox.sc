/*
    Unit Library
    The Game Of Life Foundation. http://gameoflife.nl
    Copyright 2006-2013 Miguel Negrao, Wouter Snoei.

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

SignatureBox : RoundNumberBox {
	
	var <denom = 4;
	
	init { |parent, bounds|
		super.init( parent, bounds );
		clipLo = 1;
		step = 1;
		scroll_step = 1;
		alt_scale = 1; // cents
		value = 4;
		
		formatFunc = { |value| "%/%".format( value, denom ) };
		
		interpretFunc = { |string|
			if( string.includes( $/ ) ) {
				string = string.split( $/ );
				denom = 2**(({ string[1].interpret }.try ? denom).log2.round(1).clip(0,10));
				string = string[0];
				if( string.size == 0 ) {
					string = denom.asString;
				};
			};
			({ string.interpret }.try ? value).asInt;
		};
	}
	
	denom_ { |new| denom = 2**(( new ? denom ).log2.round(1).clip(0,10)); this.refresh; }
	
	num { ^value }
	num_ { |num| value = (num ? denom).asInt.max(1); this.refresh; }
	
	value_ { |val, refresh = true|
		val = val.asCollection;
		value = val[0].asInt.max(1);
		denom = 2**(( val[1] ? denom ).log2.round(1).clip(0,10));
		if( refresh ) { this.refresh; };
	}
	
	valueAction_ { arg val;
		var oldValue;
		oldValue = this.value;
		this.value_( val, false );
		if( actionOnlyOnChange nand: { this.value == oldValue } )
			{ action.value(this, value); };
		this.refresh;
	}
	
	value { ^[ value, denom ] }
	
	decrement {arg mul=1; this.valueAction = this.value - (step*(mul * [1,0.5])) }
	
	getScale { |modifiers| 
		var inc = [1, (2**(denom.abs.log2.round(1))).max(1) ];
		^case
			{ modifiers & 131072 == 131072 } { inc }
			{ modifiers & 262144 == 262144 } { inc * [0,1] }
			{ modifiers & 524288 == 524288 } { inc * [0,1] }
			{ inc * [1,0] };
	}
	
	applySmoothSkin {
		this.applySkin( ( 
			extrude: false,
			border: 0,
			background: Color.white.alpha_(0.5),
			typingColor: Color.red(0.5).alpha_(0.75)
		) );
	}
	
	applyRoundSkin {
		this.applySkin( ( 	
			extrude: true,
			border: 2,
			background: Color.white,
			typingColor: Color.red
		) );
	}

}