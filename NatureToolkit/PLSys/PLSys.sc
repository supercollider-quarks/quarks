/// Context sensitive parametric L-systems
/// implementation by nescivi
/// Copyright 2009-10, Marije Baalman

/// this version still needs to be extended for contexts larger than 1
/// and branching of the pattern

/// single Parametric L-system segment with a name and parameters
PLSeg{
	var <>name;
	var <>pars;

	*new{ |name,pars|
		^super.newCopyArgs(name.asSymbol,pars);
	}

	*fromString{ |string|
		var pars,c,name;
		pars = string.replace( "(", "[").replace(")","]");
		c = pars.find( "[" );
		name = pars.copyFromStart( c-1 );
		pars = pars.copyToEnd( c ).interpret;
		^this.new(name,pars);
	}
	
	printOn { arg stream;
		stream 
		//	<< this.class.name << "(" 
		<< name << pars.round(0.01).asString.replace("[","(").replace("]",")").replace( " ", "" ) 
		//		<< ")";
	}

	storeArgs { 
		^[name,pars]
	}

	asEvent{
		^( plName: name, plPars: pars );
	}

	asPatternPairs{ |parNames|
		//	var outEvent = ();
		parNames = parNames ?? Array.fill( pars.size, {|i| ("plPar"++i).asSymbol; });
		^( [\plName, name ] ++ [ parNames, pars].flop).flatten
	}

}

PLBranch : List {
	
	printOn { arg stream;
		stream 
		//	<< this.class.name << "(" 
		<< "PLB" << array
		//		<< ")";
	}
	
}

/// parametric L-system rule, returns nil, if the rule does not apply
PLRule {

	var <>function; 
	/// function is passed:
	/// arg 1: parameters from current segment
	/// arg 2: parameters from previous segment
	/// arg 3: parameters from next segment
	/// the function should return a PLSeg or an Array of them
	var <>segment;
	var <>predecessor,<>successor;

	var <>unique = true;
	var <>name;

	var <>active = true;

	*new{ |seg,pre,suc,func,unique=true,name,active=true|
		^super.newCopyArgs(func,seg,pre,suc,unique,name,active)
	}

	storeArgs { 
		^[segment,predecessor,successor,function,unique,name,active] 
	}

	/// get the left context size for this rule
	leftSize{
		^predecessor.size;
	}

	/// get the right context size for this rule
	rightSize{
		^successor.size;
	}

	apply{ |seg,pre,suc|
		var funcArgs;

		if ( active.not ){
			^nil
		};

		//		[seg,pre,suc].postln;
		if ( seg.name != segment ){
			//			"segment wrong name".postln;
			^nil;
		};
		//		"applies to this segment".postln;
		if ( predecessor.notNil ){
			//			"\t predecessor required".postln;
			if ( pre.notNil ){
				//				"\t\t there is a predecessor".postln;
				if ( pre.name != predecessor ){
					//					"\t\t\t wrong name".postln;
					^nil;
				}
			}{ // no predecessor in range, but is required for rule
				^nil;
			}
		};
		if ( successor.notNil ){
			//			"\t successor required".postln;
			if ( suc.notNil ){
				//				"\t\t there is a successor".postln;
				if ( suc.name != successor ){
					//					"\t\t\t wrong name".postln;
					^nil;
				}
			}{ // no successor in range, but is required for rule
				^nil;
			}
		};

		funcArgs = [seg.pars];
		if ( pre.notNil ){ funcArgs = funcArgs ++ [pre.pars] }{ funcArgs = funcArgs ++ [nil] };
		if ( suc.notNil ){ funcArgs = funcArgs ++ [suc.pars] }{ funcArgs = funcArgs ++ [nil] };
		funcArgs = funcArgs ++ seg;
		//		funcArgs.postln;
		^function.value( *funcArgs );
	}
	
}


/// context sensitivity only up to 1 level
/// branching not taken into account yet

/// parametric L-system with axiom, rule body and ignore list
PLSys {
	var <axiomString;
	var <axiom;

	var <>state;

	//	var <rulesList;
	var <ruleSet;

	var <>ignore;
	var <>remove;

	var <>verbose = 0;

	var <>routine;
	var <currentCalcSegment;

	*new{ |axiom,rules,ignore,remove|
		^super.new.ignore_(ignore).remove_(remove).init( axiom, rules )
	}

	init{ |ax,rules|
		ignore = ignore ? [];
		remove = remove ? [];
		if ( ax.isKindOf( String ) ){
			axiomString = ax;
			axiom = this.parseAxiomString( ax );
		};
		this.parseRulesList( rules );
	}

	/// resets the system to its initial state
	reset{
		state = nil;
		axiom = this.parseAxiomString( axiomString );
		if ( routine.notNil ){ routine.reset };
	}

	getContextLeft{ |index,level|
		var left,tstate;

		/// item is to be ignored with regard to context
		if ( ignore.includes( state[index].name ) ){
			^nil;
		};
		/// copy up to index:
		left = state.copyFromStart( index-1 );

		/// select the context sensitive items
		tstate = left.select{ |it| ignore.includes( it.name ).not };

		^tstate.copyToEnd( -1*level );
	}

	getContextRight{ |index,level|
		var right,tstate;

		/// item is to be ignored with regard to context
		if ( ignore.includes( state[index].name ) ){
			^nil;
		};
		
		/// copy from the index:
		right = state.copyToEnd( index+1 );

		/// select the context sensitive items
		tstate = right.select{ |it| ignore.includes( it.name ).not };

		^tstate.copyFromStart( level );
	}


	applyRules{
		var tstate,res,newstate,nomore;
		var istate;
		newstate = List.new;

		if ( state.isNil ){ state = axiom };

		tstate = this.buildTState;

		tstate.do{ |segs|
			res = nil;
			nomore = false;
			ruleSet.do{ |rule|
				if ( nomore.not ){ /// only one rule may apply
					res = rule.apply( *(segs.at([1,0,2])) );
					if ( res.notNil ){
						newstate = newstate.add( res ).flatten;
						if ( rule.unique ){
							nomore = true;
						};
					};
				};
			};
			if ( res.isNil ){ /// no rule applied, so keep the old segment
				newstate = newstate.add( segs[1] );
			};
		};
		newstate = newstate.reject( { |it| remove.includes( it.name ); });
		if ( verbose > 0 ){	newstate.postln; };
		state = newstate;
	}

	buildTState{
		var tstate;
		var last, prev, prev2;
		
		var segSize;

		last = state.size - 1;
		
		segSize = axiom.first.pars.size;
		
		prev = PLSeg('{', Array.fill( segSize, 0 ) );
		tstate = state.collect{ |it,i|
					if ( i == last ){
						if ( ignore.includes( it.name ) ){
							[ nil, it, nil ];
						}{
							prev2 = prev; prev = it;
							[ prev2, it, PLSeg('}', Array.fill( segSize, 0 ) ) ];
						}				
					}{
						if ( ignore.includes( it.name ) ){
							[ nil, it, nil ];
						}{
							prev2 = prev; prev = it;
							[ prev2, it, state[i+1] ];
						}
					}
				};
		if ( verbose > 2 ){
			tstate.postcs;
		};

		^tstate;

		/*
			/// select the context sensitive items
			tstate = state.select{ |it| ignore.includes( it.name ).not };
			
			/// recall where the ignored items are
			istate = state.selectIndex{ |it| ignore.includes( it.name ) };
			
			/// context sensitive:
			/// make a list that has triples of predecessors, segments and successors
			tstate = tstate.addFirst( nil ).add( nil ).slide(3).clump(3);

			//	tstate.postln;
			//	istate.postln;
			
			/// now we need to add back in the items that had to be ignored for context sensitivity
			/// iterate over the ignored indices of the original list and insert 
			/// the original items, without context
			istate.do{ |index|
			tstate = tstate.insert( index, [ nil, state[index], nil ] );
			};
		*/
	}

	createRoutine{
		routine = Routine{
			
			var tstate,res,newstate,oldstate,nomore;
			var last;

			loop{
				if ( state.isNil ){ state = axiom };
				newstate = List.new;
				oldstate = state.copy;

				if ( verbose > 1 ){
					"--- start of sequence ---".postln;
					oldstate.postcs;
				};

				tstate = this.buildTState;

				tstate.do{ |segs,i|
					last = state.size;
					res = nil;
					nomore = false;
					ruleSet.do{ |rule|
						if ( nomore.not ){ /// only one unique rule may apply
							res = rule.apply( *(segs.at([1,0,2])) );
							if ( res.notNil ){
								if ( verbose > 2 ){
									[segs, rule].postcs;
								};
								newstate = newstate.add( res ).flatten;
								if ( rule.unique ){
									nomore = true;
								};
							};
						};
					};
					if ( res.isNil ){ /// no rule applied, so segment is unchanged
						newstate = newstate.add( segs[1] );
					};
					currentCalcSegment = newstate.last;
					newstate = newstate.reject( { |it| remove.includes( it.name ); });
					oldstate = oldstate.drop(1);
					state = newstate ++ oldstate;
					if ( verbose > 0 and: (state.size > last) ){
						// grown since last update
						"----- growth -----".postln;
						state.postcs;
					};
					if ( verbose > 2 ){	state.postln; };
					state.yield;
				};
			};
		};
	}

	nextApply{
		^routine.next;
	}

	parseRulesList{ |rl|
		var newrule;
		ruleSet = List.new;
		rl.do{ |rule|
			if ( rule.isKindOf( PLRule ).not ){
				newrule = PLRule.new( *rule );
				ruleSet.add( newrule );
			}{
				ruleSet.add( rule );
			}
		}
	}

	parseAxiomString{ |axs|
		var b,c;
		var ax = List.new;
		while( { (c = axs.find( ")" )).notNil },{
			b = axs.copyFromStart( c );
			ax.add( PLSeg.fromString( b ) );
			axs = axs.copyToEnd( c+1 );
		});
		^ax;
	}

	asPattern{ |parNames|
		var evlist;
		if ( state.notNil ){
			parNames = parNames ?? Array.fill( state.first.pars.size, {|i| ("plPar"++i).asSymbol; });
			evlist = this.state.collect{ |it| 
				var ev = it.asEvent; 
				[ev[\plName],ev[\plPars]]
			}.flop;
			evlist[0] = evlist[0].bubble;
			evlist[1] = evlist[1].flop;
			evlist = evlist.flatten(1);
			^Pbind( *([\plName, Pseq( evlist[0], 1) ] ++ ( [parNames, evlist.copyToEnd(1)].flop.collect{ |it| [ it[0], Pseq( it[1], 1 ) ] }.flatten; ); ) );
		};
		^Pbind();
	}
	
	saveRules{ |path|
		ruleSet.writeArchive( path );
	}

	loadRules{ |path|
		ruleSet = Object.readArchive( path );
	}

	saveState{ |path|
		state.writeArchive( path );
	}

	loadState{ |path|
		state = Object.readArchive( path );
	}
}