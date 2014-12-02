// =====================================================================
// PLSys (not a proper helpfile yet, but an example)
// =====================================================================


(
a = PLSys.new( "F(0,0,900)F(4,1,900)F(0,0,900)", 
	[ 
		// rule definition
		// segment, predecessor, successor, function making up the rule
		// arguments to the function are the parameters of the segment,
		// of the predecessor, and of the successor, and finally the segment itself (in case you want to modify the segment before it goes into another rule)
		['F',nil,nil,{ |t,p,s| if ( t[1]==1 and: (t[0] >= 6) ){ 
			[ 
				PLSeg( "F", t*[2/3,0,1]+[0,2,0] ),
				PLSeg( "f", [1] ),
				PLSeg( "F", t*[1/3,0,1]+[0,1,0] )
			]
		}{ nil } 
		}], // end of rule 1
		['F',nil,nil,{ |t,p,s| if ( t[1]==2 and: (t[0] >= 6) ){ 
			[ 
				PLSeg( "F", t*[1/3,0,1]+[0,2,0] ),
				PLSeg( "f", [1] ),
				PLSeg( "F", t*[2/3,0,1]+[0,1,0] )
			]
		}{ nil } 
		}], // end of rule 2
		['F',\F,\F,{ |t,p,s| if ( (t[0] > 3.9) or: (t[2] >0.4) ){ 
			PLSeg( "F", [ t[0]+ 0.1, t[1], t[2] + (0.25*(p[2] + s[2] - (3*t[2]) )) ] )
		}{ nil } 
		}], // end of rule 3
		['F',\F,\F,{ |t,p,s| if ( ((t[0] > 3.9) or: (t[2] >0.4)).not ){ 
			[ PLSeg( "F", [0,0,900] ), PLSeg( "~", 0 ), PLSeg( "H", [1] ) ]
		}{ nil } 
		}], // end of rule 4
		['H',nil,nil,{ |t,p,s| if (t[0] < 3){
			PLSeg( "H", t*1.1 )
		}{ nil } 
		}] // end of rule 5
	],
	[\f,'~','H']   // ignores
);
);

23.do{ a.applyRules }

