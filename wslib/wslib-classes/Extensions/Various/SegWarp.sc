SegWarp : Warp {

	/*
	segmented Warp
	creates an env inside if not provided with one
	
	// fold in the center:
	x = SegWarp([0,1,0]);
	x.map( 0.5 ); // -> 1
	x.map( [0,1] ); // -> [0,0]
	x.map( 0.25 ); // -> 0.5
	
	// format of input array:
	// [ [ value, position (0-1), warp to next ], ... ]


	// frequency with fixed center (440)
	x = SegWarp([[20,0.5,\exp], [440,0,\exp], [22050,1]]);  	x.map( 0.5 ); // -> 440
	x.map( [0,1] ); // -> [20,22050]
	x.map( 0.25 ); // -> 0.5
	x.unmap( 220 ); // -> 0.38787808789121
	x.unmap( 880 ); // -> 0.58854052996238
	x.env.plot;
	
	// use an Env
	x = SegWarp( Env([0,1,1,0], [0.3,0.4,0.3]) );
	x.map( 0.5 );
	*/


	var <>env, <array;
	var <last = 0;
	
	classvar <warpConversionTable;
	
	*initClass {
		warpConversionTable = 
			IdentityDictionary[ 
				\lin -> \lin,
				\linear -> \lin,
				\exp -> \exp,
				\exponential -> \exp,
				\sin -> \cos, // strange but true .. 
				\sine -> \cos,
				\wel -> \sin,
				\welch -> \sin,
				
				// TODO
				\sqr -> \lin,
				\squared -> \lin,
				\cub -> \lin,
				\cubed -> \lin
			];
		}

	*new { |array|
		^super.newCopyArgs.init( array );
		}
		
	init { |inArray|
		if( inArray.class == Env )
			{ env = inArray; array = this.class.arrayFromEnv( env ); }
			{ this.makeEnv( inArray ); }
		}
		
	makeEnv { |inArray|
		array = this.class.cleanArray( inArray );	
		env = Env.new( array.flop[0], array.flop[1].differentiate[1..], array.flop[2] );
		}
		
		
	array_ { |inArray| this.makeEnv( inArray ); }
		
		
	*cleanArray { |inArray|
		
		/*
		cleans an array into the form:
		[ [ level, time, curve ], etc.. ]
		input array can also hold single levels, which get times scaled between 0 and 1
		first time always becomes 0, last time is always 1
		times are sorted
		
		SegWarp.cleanArray( [0,1] );
			-> [ [ 0, 0, lin ], [ 1, 1, lin ] ]
			
		SegWarp.cleanArray( [[0.5,0.75],[1,0.25]] );
			-> [ [ 0.5, 0, step ], [ 0.5, 0.75, lin ], [ 1, 0.25, lin ], [ 1, 1, step ] ]
		*/
		
		inArray = inArray ? [0,1];
		
		inArray = inArray.collect({ |item,i|
			if( item.size <= 1 )
				{ item.asCollection ++ [ i / (inArray.size-1), \lin ]; }
				{ item };
			});
			
		inArray = inArray.sort( _[1] <= _[1] );
		
		if( inArray[0][1] != 0 ) { inArray = inArray.addFirst( [ inArray[0][0], 0, \step ] ) };
		if( inArray.last[1] < 1 ) { inArray = inArray.add( [ inArray.last[0], 1, \step ] ) };
		
		^inArray.collect({ |item| [item[0], item[1], item[2] ? \lin ] });
		}
		
	*simplifyArray { |inArray|
		var equalDivisionTimes;
		
		equalDivisionTimes = (0..inArray.size-1)/(inArray.size-1);		
		inArray = inArray.collect({ |item, i| // check for redundant 'lin's
			if( item.size > 0 )
				{ if( [ nil, \lin, \linear ].includes( item[2] ) )
					{ item = item[..1]; };
				  if( (item.size == 2) && { item[1] == equalDivisionTimes[i] } )
				  	{ item = item[0]; };
				};
			item;
		});
	
		^inArray;
			
	}
		
		
	*arrayFromEnv { |env|
		^[env.levels, ([0] ++ env.times).integrate, env.curves ? \lin].flop;
	}

	map { arg value; // can be array
		^if( value.size > 0 ) { value.collect( env.at( _ ) ) } { env.at( value ) };
		}
		
	unmap { arg value; // finds first occurence, returns previous value if not found (default 0)
		var node, nextNode;
		
		node = array.detectIndex({ |item,i| 
				value.inclusivelyBetween( *[item[0], (array[i+1] ? item)[0]].sort ); 
				});
		
		if( node.notNil )
			{ 
			nextNode = array[node + 1];
			node = array[node];
			if( nextNode.isNil or: { node[2] == 'step'} )
				{ ^last = node[1] }
				{ ^last = node[1].blend( nextNode[1], [ node[0], nextNode[0], 
							if( node[2].isNumber ) 
								{ node[2] } 
								{ warpConversionTable[ node[2] ] }
						 ].asSpec.unmap( value ) );
				}
			}
			{ ^last  };
		}
		
	asSpecifier { ^this.class.simplifyArray( this.class.arrayFromEnv( env ) ) }
	
	}

+ Env {
	asWarp { ^SegWarp( this ); }
	asSpec { ^ControlSpec(this.levels.minItem,this.levels.maxItem,this) }
	}

+ SequenceableCollection {
	asWarp { ^SegWarp( this ); }
	}
	
