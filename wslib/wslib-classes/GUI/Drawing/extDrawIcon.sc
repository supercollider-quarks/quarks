// wslib 2006
//
// additions to DrawIcon

+ Symbol {
	drawIcon { |rect ... args| // args override symbolArgs
		if( args.size == 0 )
			{ ^DrawIcon.symbolArgs( this, rect.asRect ) }
			{ ^DrawIcon( this.asString.split( $_ )[0].asSymbol, rect.asRect, *args ) };
		}
	}
	
+ Pen {
	*drawIcon { |symbol = \play, rect ... args|
		if( args.size == 0 )
			{ ^DrawIcon.symbolArgs( symbol, rect.asRect ) }
			{ ^DrawIcon( symbol, rect.asRect, *args ) };
		}
	}