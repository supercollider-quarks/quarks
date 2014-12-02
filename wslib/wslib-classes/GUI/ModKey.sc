ModKey {
	classvar <numberDict;
	classvar <dict;
	
	var <>list; 
	var <>extra; // extras bit indexes for 24bits modifier number	
	// extra args contain left/right shift and cmd and some unknown bits
	
	// no setting yet, only testing (except directly with list argument)
	
	*initClass {
		numberDict = (
		
			// bit = 1 indexes
			
			0: 'fn', // powerbook
			1: 'help',// help key
			2: 'num', // num & fn = arrow key (or fn on a num key with no numlock)
			3: 'cmd',
			4: 'alt',
			5: 'ctrl',
			6: 'shift',
			7: 'capslock'
			
			);
				
		}
		
	*new { |... args|
		if( args[0].isNumber && { args.size == 1 } )
			{ ^this.fromNumber( args.first ) }
			{ ^this.fromList( *args ) };
		}
	
	*fromList { |... args| ^super.new.init( args.flat ); } // [\shift, \ctrl] etc. -- no Strings
	
	*fromNumber { |number|  // as provided by mouseActions and keyActions 
		^this.fromList( *number.asBinaryString(24).indicesOfEqual( $1 ) ); 
		}
	
	asNumber {
		var indices, string;
		indices = list
			.collect({ |item| numberDict.findKeyForValue( item ) })
			.select( _.notNil ) ++ extra;
		string = String.fill(24, { |i| if( indices.includes( i ) ) { $1 } { $0 } } );
		^( "2r" ++ string ).interpret.asInt;
		}
	
	init { |args|
		var tempList;
		tempList = args.collect({ |item| numberDict[ item ] ? item; });
		list = tempList.select({ |item| item.class == Symbol }).sort;
		extra = tempList.select({ |item| (item.class != Symbol) && (item.notNil) }).sort; 
		}
		
	
	includes { |modNames, mode = \all|
	
		switch( mode,
			\all, { ^this.includesAll( *modNames ); },
			\any, { ^this.includesAny( *modNames ) },
			\only, { ^this.includesOnly( *modNames ) },
			\onlyAny, { ^this.includesOnlyAny( *modNames ) },
			\one, { ^this.includesOne( *modNames ) },
			\onlyOne, { ^this.includesOnlyOne( *modNames ) } );
		
		 ^this.includesAll( *modNames );
			
		}
			
	includesAll { |... modNames| ^modNames.every({ |item| list.includes( item ) }); }
	includesAny { |... modNames| ^modNames.any({ |item| list.includes( item ) }); } 
	includesOnly { |... modNames| // optimized
		var tempList;
		tempList = list.copy;
		// only true when all items are included
		modNames.do({ |item| 
			if( tempList.remove( item ).isNil )
				{ ^false }; 
			});
			
		if( tempList.size == 0 )
			{ ^true }
			{ ^false };
		}
		
	includesOnlyAny { |... modNames| // optimized
		var tempList, tempTests;
		tempList = list.copy;
		
		// only true when all items are included
		
		tempTests = modNames.collect({ |item| 
			tempList.remove( item ).isNil });
		
		if( tempTests.every( _.value ) )
			{ ^false };
		
		if( tempList.size == 0 )
			{ ^true }
			{ ^false };
		}
	
	includesOne { |... modNames| // optimized
		if( this.find( *modNames ).size == 1)
			{ ^true }
			{ ^false };
		}
		
	includesOnlyOne { |... modNames| // optimized
		var tempList;
		tempList = this.find( *modNames );
		if( tempList.size == 1)
			{ ^this.includesOnly( *tempList ) }
			{ ^false };
		}
		
	find { |... args| // return items when found
		var tempList;
		tempList = list.copy;
		^args.collect({ |item| tempList.remove( item ) }).select( _.notNil );
		}
		
	add { |item| // ModKey[ ... ] syntax support
		if( list.includes( item ).not )
			{ list = list.add( item ) };
		}
		
	remove { |item| ^list.remove( item );
		// returns item  (or nil when not found)
		}
	
	// common tests
	shift { |mode = \all| ^this.includes( \shift, mode ); }
	capslock { |mode = \all| ^this.includes( \capslock, mode ); }
	ctrl { |mode = \all| ^this.includes( \ctrl, mode ); }
	cmd { |mode = \all| ^this.includes( \cmd, mode ); }
	alt { |mode = \all| ^this.includes( \alt, mode ); }
	fn { |mode = \all| ^this.includes( \fn, mode ); }
	
	//alternate names
	cl { |mode = \all| ^this.capslock(mode); }
	option { |mode = \all| ^this.alt(mode); }
	apple { |mode = \all| ^this.cmd(mode); }
	command { |mode = \all| ^this.cmd(mode); }
	function { |mode = \all| ^this.fn(mode); }
	
	shiftCapslock { |mode = \xor, mode2 = \any|
		// by default capslock is defeated by shift (\xor)
		var items;
		case { ( mode == \xor ) && { mode2 == \any } }
			{ ^this.includesOne( \shift, \capslock )  }
			{ ( mode == \xor ) && { mode2 == \only } }
			{ ^this.includesOnlyOne( \shift, \capslock )  }
			{ ( mode == \or ) && { mode2 == \any } }
			{ ^this.includesAny( \shift, \capslock )  }
			{ ( mode == \or ) && { mode2 == \only } }
			{ ^this.includesOnlyAny( \shift, \capslock )  }
			{ true }
			{ "bad arguments for ModKey-shiftCapslock".postln;
				^false }
		}
	
	shiftCl { |mode = \xor, mode2 = \any| ^this.shiftCapslock( mode, mode2 ); }
				
	onlyShift { ^this.shift( \only ); }
	onlyCapslock { ^this.capslock( \only ); }
	
	onlyShiftCl { |mode = \xor| 
		// \xor: shift or capslock, not both
		// \or: shift, capslock or both
		^this.shiftCapslock( mode, \only );
		 }
		
	printOn { |stream|
		stream << "ModKey( ";
		list.do({ |item, i|
			stream << item;
			if( i < (list.size-1) )
				{ stream << ", " };
			});
		stream << " )";
		}
		
	}
	

             ÊÊÊÊ          