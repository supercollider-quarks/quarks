Graph {
	
	// for sake of handling the dict must be:
	// {vID: [x, y, vDur, vLabel, vOpts, [end, eDur, eID, eOpts], ...etc], etc:...}
	var <>graphDict ;	//
	var <>ne, <>nv ; // a counter for unique edges and vertices ID
	var <>auto, <>rounding ; // mulScale and rounding for autoduration
	var <>annotationSize; // the number of annotation parameters you need to be stored in the vertex opts
		
	// constructor: you can start with an existing graphDict
	*new { arg graphDict = IdentityDictionary.new ; 
		^super.new.initGraph(graphDict) 	
	}

	initGraph { arg aGraphDict ;
	// TODO: should be taken into account for ne and nv
		aGraphDict = aGraphDict ? IdentityDictionary.new ;	
		graphDict = aGraphDict ;				
		ne = 1 ; nv = 1 ;
		auto = 1 ;
		annotationSize = 2; //CartoonModel uses offsetListenerArea and dy
		this.changed ;
	}
	
	/* Storing/retrieving in internal format */
	
	// for consistence use .gra extension
	write { arg path ;
		this.writeArchive(path)
	}
	
	// this means you can do:
	// a = Graph.read("/test.gra")	
	*read { arg path ;
		^Object.readArchive(path) ;
		
	}
	/**/

	// oh well, raises an exception if you pass an empty graphDict
	setGraphDict { arg aGraphDict ;
		var eInd ;
		graphDict = aGraphDict ;
		nv = aGraphDict.keys.asArray.sort.reverse[0]+1 ;
		eInd = [] ;
		aGraphDict.do({ arg v ;
			v[5..].do({ arg e ; eInd = eInd.add(e[2]) })
		}) ;
		ne = eInd.sort.reverse[0]+1 ;
		this.changed ;
	}


/*	Basic usage */	
	
	// add an empty vertex
	// so that it exists
	// vertex is an Int	
	
	addVertex { arg x = nil, y = nil, dur = 0, label = "", vopts = Array.newClear(annotationSize), vID = nil ; //create an option array filled with nil based on the number of value you need 
		
		if (vID == nil, { 
			vID = nv;
			graphDict.add(vID -> [x, y, dur, label, vopts]) ;
			nv = nv + 1 },
			{graphDict.add(vID -> [x, y, dur, label, vopts]) ;
			})  ;
		this.changed;
		^vID; //return the vID created so that external application could reuse it to modify vertex info
	}
	
	
	// autoDuration allows to scale edges in relation to vertices positions
	// --> loops are unaffected (dist = 0 not allowed)
	// --> all the edges a->b have the same dur
	setAutoDuration { arg mulScale = 1, aRounding = 0 ;
		var dist ;
		auto = mulScale ;
		rounding = aRounding ;
		graphDict.keys.do({ arg key ;
//			if (graphDict[key].size > 4, { 
			graphDict[key][5..].do({ arg e ;
				dist = ((graphDict[key][0]-graphDict[e[0]][0]).squared + 
					(graphDict[key][1]-graphDict[e[0]][1]).squared).sqrt.round(rounding) ;
				if (dist != 0, { e[1] = dist*mulScale }, { dist*mulScale }) ;
			})
//			})
		}) ;
		this.changed
	}
	
	
	// change the position of a vertex
	changeVertexPosition { arg vID, x, y ;
		graphDict[vID][0] = x ;
		graphDict[vID][1] = y ;
		this.changed ;
	}

	readVertexPosition { arg vID;
		graphDict[vID][0].postln;
		graphDict[vID][1].postln;
	}



	addAutoEdge { arg start, end, options = [] ;
		// options might be useful sooner or later
		var edge, dur ;
		dur = ((graphDict[start][0]-graphDict[end][0]).squared + 
					(graphDict[start][1]-graphDict[end][1]).squared).sqrt ;
		edge = [end, (dur*auto).round(rounding), ne, options] ;
		graphDict[start] = graphDict[start].add(edge) ;
		ne = ne + 1 ;
		this.changed
	}		
			

	// changes all the a<->b edges according to geometrical distance
	setAutoEdge { arg start, end ;
		var edge, dur ;
		dur = ((graphDict[start][0]-graphDict[end][0]).squared + 
					(graphDict[start][1]-graphDict[end][1]).squared).sqrt ;
		graphDict[start][5..].do({ arg e ;
			if ( e[0] == end , {e[1] = (dur*auto).round(rounding) })
		}) ;
		graphDict[end][5..].do({ arg e ;
			if ( e[0] == start , {e[1] = (dur*auto).round(rounding) })
		}) ; 
		this.changed
	}	

	setAutoEdgeFromV { arg start ;
		graphDict.keys.do({ arg key ;
			this.setAutoEdge(start, key)
		 }) ;
	}
			
	// add an edge
	addEdge { arg start, end, dur, options = [] ;
		// options might be useful sooner or later
		var edge = [end, dur, ne, options] ;
		graphDict[start] = graphDict[start].add(edge) ;
		ne = ne + 1 ;
		this.changed
	}


	getvID {arg vertexName; //obtain the vID of a vertex from is name
		var found;
		graphDict.do ({arg dict;
			if (dict[3].asSymbol == vertexName.asSymbol, {found = graphDict.findKeyForValue(dict);})
		});
		
		^found; //the vID
	
	}


	removeVertex { arg vID ;
		// Here we remove key
		graphDict.removeAt(vID) ;
		// Here we remove in every other vertex eventual links to vertex
		graphDict.do({ arg def ;
			var key = graphDict.findKeyForValue(def) ;
			var newDef ;
			// would be more effcient probably to use .reject
			if ( def.size == 5, 
				{ newDef = def[0..4] }, 
				{ newDef = def[0..4] ;
				def[5..].do({ arg e ; 
					if (e[0] != vID, { newDef = newDef.add(e) } )
					}) }) ;
			graphDict[key] = newDef 
		}) ;
		this.changed
	}

	
	// allows changing edge duration passing an ID
	// can't be done in other ways (would be ambiguous)
	setEdgeDuration { arg eId, dur ;
		graphDict.do({ arg def ;
			def[5..].do({ arg e ; 
				if ( e[2] == eId, { e[1] = dur })
			}) ; 
		}) ;
		this.changed
	}
	
	
	removeEdge { arg eID ;
			graphDict.do({ arg vertex ;
				vertex[5..].do({ arg edge ;
					// e[2] is eID
					if (edge[2] == eID, { vertex.remove(edge) }) ; 				})
			}) ;
		this.changed
	}
	
	// remove all the edges from start to end
	removeEdges { arg start, end ;
		var links ;
		if ( graphDict[start].size > 5, {
			links = graphDict[start][5..] ;
			links.removeAllSuchThat({ arg item, i; 
					item[0] == end
					}) ;
			graphDict[start] = graphDict[start][0..4].addAll( links ) ;	
			this.changed })
	}
	
	isolateVertex { arg vID ;
		// remove all the I/O links from a vertex
		graphDict[vID] = graphDict[vID][0..4] ; 
		// Here we remove in every other vertex  links to vertex
		graphDict.do({ arg def ;
			var key = graphDict.findKeyForValue(def) ;
			var newDef ;
			// probably would be more efficient to use .reject
			if ( def.size == 1, 
				{ newDef = def[0..4] }, 
				{ newDef = def[0..4] ;
				def[5..].do({ arg e ; 
					if (e[0] != vID, { newDef = newDef.add(e) } )
					}) }) ;
			graphDict[key] = newDef 
		}) ;
		this.changed

	}

	changeVertexName { arg vID, newLab ;
		graphDict[vID][3] = newLab ;
		this.changed	
	 }

	setvopts {arg vID, annotationOrder, val;

			graphDict[vID][4][annotationOrder] = val;
	}
	
	setAnnotationSize { arg aSize;
		annotationSize = aSize;
	}


/* Generation and processing methods */


	createRandom { arg nameList, eNum = 10, eMin = 1, eMax = 1 ; 
		// a list of symbols, number of  edges connecting the list
		// max and min duration 
		var start, end, dur, label ;
			nameList.do({ arg label ;
			this.addVertex(1200.rand, 800.rand, 0, label:label)
		}) ;
		
		eNum.do({ arg i ;
			start = nameList.size.rand+1 ;
			//label = nameList[start] ;
			end = nameList.size.rand+1 ;
			dur = rrand(eMin.asFloat, eMax.asFloat) ;
			this.addEdge(start, end, dur) ;
		}) ;	
		this.changed ;
	}

	// there are two strategies:
	// 1. add in and out to each vertex if lacking
	// 2. cut a vertex without one in and one out
	makeCyclic { 
	}
	
	// check me please
	createRandomCyclic { arg nameList = [], eNum = 10, eMin = 1, eMax = 1, noLoop = true ;
		// a list of symbols,
		// number of  edges connecting the list beyond I/O 
		// max and min duration 
		var start, end, dur, cleanNameList, label, arr ;
	
		this.createRandom(nameList, eNum, eMin, eMax) ;
		
		nameList.size.do({ arg vertex ;
			arr = Array.series(10)+1 ;
			if (noLoop, { arr.remove(vertex+1) }) ;
			start = arr.choose;
			end = arr.choose;
			dur = rrand(eMin.asFloat, eMax.asFloat) ;
			this.addEdge(start, vertex+1, dur) ;
			dur = rrand(eMin.asFloat, eMax.asFloat) ;
			this.addEdge(vertex+1, end, dur) ;
		}) ;	
		this.changed ;
	}
	
	round { arg step = 0 ;
		graphDict.do({ arg edges ;
			edges[5..].do({ arg e ; e[1] = e[1].round(step)})
		}) ;
		this.changed ;
	}


// plotting
	plot {
		var p = Painter.new(this) ;
	}
		
}
              