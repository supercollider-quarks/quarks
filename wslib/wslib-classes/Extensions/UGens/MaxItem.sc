// wslib 2006
// min/max item for UGens and other objects
// example:  MaxItem( [ SinOsc.ar( 220 ), SinOsc.ar( 225 ) ] );

MaxItem {
	*new { |array, force = false|  
		var out;
		if( array.any({ |item| item.isKindOf( UGen ) }) )
			{ out = array[0];
				array[1..].do({ |item,i|
					out = if( item > out, item, out );
				});
				^out;
			} {
			^array.maxItem;
			};
		}
		
	*abs { |array|
		var out;
		if( array.any({ |item| item.isKindOf( UGen ) }) )
			{ out = array[0];
				array[1..].do({ |item,i|
					out = if( item.abs > out.abs, item, out );
				});
				^out;
			} {
			^array[ array.abs.indexOf( array.abs.maxItem ) ];
			};
		}
	
	*switch { |array, switchArray|
		var out;
		if( array.any({ |item| item.isKindOf( UGen ) }) )
			{ out = [ array[0], switchArray[0] ];
				array[1..].do({ |item,i|
					out = if( item > out[0], [ item, switchArray.wrapAt(i+1) ], out );
				});
				^out[1];
			} {
			^switchArray[ array.indexOf( array.maxItem ) ];
			};
		}
	}
	
MinItem {
	*new { |array, force = false|
		var out;
		if( array.any({ |item| item.isKindOf( UGen ) }) )
			{ out = array[0];
				array[1..].do({ |item,i|
					out = if( item < out, item, out );
				});
				^out;
			} {
			^array.minItem;
			};
		}
		
	*abs { |array|
		var out;
		if( array.any({ |item| item.isKindOf( UGen ) }) )
			{ out = array[0];
				array[1..].do({ |item,i|
					out = if( item.abs < out.abs, item, out );
				});
				^out;
			} {
			^array[ array.abs.indexOf( array.abs.minItem ) ];
			};
		}
		
	*switch { |array, switchArray|
		var out;
		if( array.any({ |item| item.isKindOf( UGen ) }) )
			{ out = [ array[0], switchArray[0] ];
				array[1..].do({ |item,i|
					out = if( item[0] < out[0], [ item, switchArray.wrapAt(i+1) ], out );
				});
				^out[1];
			} {
			^switchArray[ array.indexOf( array.minItem ) ];
			};
		}
	}