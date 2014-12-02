/*
//	Painter draw the graph putting every vertex in his (x, y) position
// 	Vertices are represented by Buttons, and edge labels by StaticText
// 	The a drawing Pen routine draws edges
//	Substantially it is a reimplementation of level I of Graphista
// 	Should be easy to add trajectory (via MouseXY) and hearing circle
// 	NOT the right solution for topological studies 
// 	This comment: 27/09/07
// 	-- added move with mouse 08/10/07
// 	-- new transparent stuff 11/10/07
*/

Painter {

	var <>graph, <>runner, <>w, <>vStaticDict, <>labelList, <>geoListener, <>buttListener;
	var <>wiring ;			
	// used for visualization flags
	var <>vertices, <>edges, <>vLabels, <>eLabels, <>vDim, <>eDim, <>alpha, <>fontName, <>fontSize  ;	
	var <>activation ; // array: vertex activation
	var <>statsDict ; // counts each vertex occurence
	var <>colFact ; // a multiplier for color scaling
	var <>auto ; // for autoscaling
	var <>lviewdim; //for painting listener area
	var <> windowx, windowy, windowWidth, windowHeight; //for controlling Painter window size and position
	var windowStandard;
	var <>vPosScale; // for controlling the position where a vertex is visualized, in case the visualization has a different scale from the position 

	// constructor: you can start with an existing graphDict
	
	*new { arg graph, runner, geoListener, vertices = true, edges = true, vLabels = true, eLabels = true,			vDim = [75,20], eDim = [50,20], 
			alpha = 0.9, colFact = 0.01, fontName = "Monaco", fontSize = 10, activation = [0.2, 1.0],
			windowx, windowy, windowWidth, windowHeight; 
			
			// colfact--> after 100 times white is reached 
		^super.new.initPainter(graph, runner, geoListener, vertices, edges, vLabels, eLabels, vDim, eDim,
						 alpha, colFact, fontName, fontSize, activation, windowx, windowy, windowWidth, windowHeight) 
	}

	initPainter { arg aGraph, aRunner,aGeoListener, vFlag, eFlag, vlFlag, elFlag, 
			aVDim, anEDim, anAlpha, aColFact, aFontName, aFontSize, anActivation, aWindowx, aWindowy, aWindowWidth, aWindowHeight ;
	// TODO: should be taken into account for ne and nv
		var selected, key;  // for mouse binding
		var a, b, orient; //for GeoListener initview
		graph = aGraph ;
		runner = aRunner ;
		geoListener = aGeoListener;
		runner.addDependant(this) ;
		graph.addDependant(this) ;
		geoListener.addDependant(this);
		windowStandard = [50, 50, 1200, 800];
		if (aWindowx == nil, {windowx = windowStandard[0]} , {windowx = aWindowx});
		if (aWindowy == nil, {windowy = windowStandard[1]} , {windowy = aWindowx});
		if (aWindowWidth == nil, {windowWidth = windowStandard[2]} , {windowWidth = aWindowWidth});
		if (aWindowHeight == nil, {windowHeight = windowStandard[3]} , {windowHeight = aWindowHeight});
		w = GUI.window.new("Grand Verre", Rect(windowx, windowy, windowWidth, windowHeight)).front 
			.onClose_({ runner.removeDependant(this) ; graph.removeDependant(this) }) ;
		
		if ( GUI.current.name == \SwingGUI, 
			{ w.server.sendMsg
				( '/methodr', '[', '/method', w.id, \getPeer, ']', \setAlpha, anAlpha )
		}) ;

		alpha = anAlpha ;
		//w.alpha_(anAlpha) ; // alpha is fucking buggy!
		w.view.background_( Color(0.0, 0.0, 0.0, anAlpha) ); 
		
		vStaticDict = IdentityDictionary.new ;
		// change me to a dict, please
		labelList = [] ;
		edges = eFlag ; eLabels = elFlag ; vertices = vFlag ; vLabels = vlFlag ; 
		eDim = anEDim; vDim = aVDim ;
		statsDict = IdentityDictionary.new ;
		graph.graphDict.keys.do({ arg key ; statsDict[key] = 0 });
		colFact = aColFact ;
		fontName = aFontName ; fontSize = aFontSize ;
		activation = anActivation ;
		auto = false ;
		// mouse binding
		w.view.mouseMoveAction = { arg view, x, y, modif; 
			if ( selected.notNil, 
				{ selected.bounds = selected.bounds.moveTo(x-(vDim[0]*0.5), y-(vDim[1]*0.5)) ;
				// cool but expensive: think about commenting it out
				graph.changeVertexPosition(key, x, y) ;
				if ( auto, { graph.setAutoEdgeFromV(key) }) ;
				 })
		}.defer;
		w.view.mouseDownAction = { arg view, x, y, modif ; 
			vStaticDict.keys.do({ arg aKey ; 
			if ( vStaticDict[aKey].bounds.containsPoint( x @ y), { 				key = aKey ;
				selected = vStaticDict[key] ;
			 })
		}) ;
		}.deferÊ;
		w.view.mouseUpAction = { arg view, x, y, modif; 
			if ( key.notNil, {
			graph.changeVertexPosition(key, x, y) ;
			if ( auto, { graph.setAutoEdgeFromV(key) }) ;
			key = nil ;
			selected = nil })
			 }.defer ;
		
		//GeoListener init view
		if (geoListener != nil,
			{
			a = geoListener.la;
			b = geoListener.lb;
			orient = geoListener.lorient;
			lviewdim = 60;
			buttListener = this.drawListener(a,b,orient)
			}
		);

		this.update ;		
	}
	
	// calculates the total connection between two vertices
	calculateWiring { 
		var start, end ;
		wiring = [] ;

		// create a dict eID -> wiring
		// so that it can be called externally
		graph.graphDict.do ({ arg v ;
			start = graph.graphDict.findKeyForValue(v) ;
			v[5..].do({ arg e ;
				end = e[0] ;
				wiring = wiring.add([start, end])
			});
		}) ;
	}
	
	drawVertices {
		var item, v, x, y, vLab, active ;
		// plot vertex labels
		graph.graphDict.keys.do({ arg v ;
			vStaticDict[v] = this.drawVertex(v) 
				}) ;	
		
	}
	
	drawVertex { arg v ;
		var item,  x, y, vLab, active, butt ;
		item = graph.graphDict[v] ;	
		if (vPosScale == nil, {	
			x = item[0] ; 
			y = item[1] ;
			},{
			x = (item[0] * vPosScale) ; 
			y = (item[1] * vPosScale) ; 
			}
		); 
		if ( vLabels == true, {	vLab = v.asString++": "++item[3] ;
							active = "ON"},
							{ vLab = "" ; active = "" }) ;
		butt = GUI.staticText
			.new(w, Rect.new(x-(vDim[0]*0.5), y-(vDim[1]*0.5), vDim[0], vDim[1]))
			.background_(Color(0, 0, 0, 0.7)) 	
			.string_(vLab)
			.align_(\center)
			.stringColor_(Color.white)
			.font_(GUI.font.new(fontName, fontSize)) ;
		^butt
	}

	
	drawEdgeLabels {	
		var item, v, x, y, vLab, target, tx, ty ;
		var mx, my, xm, ym, caty, catx, catnewy, catnewx, dist1_2, i ;
		var large = eDim[0]/2, tall = eDim[1]/2 ;

		this.calculateWiring ;
		graph.graphDict.keys.asArray.sort.do({ arg index ;
			v = index ; //vestigial
			item = graph.graphDict[index] ;
			x = item[0] ; 
			y = item[1] ; 
			if (item.size > 5, { 				
				item[5..].do({ arg e ;
					target = graph.graphDict[e[0]] ;
					tx = target[0] ;
					ty = target[1] ;
					if ( x < tx, { x = x+large ; tx = tx-large }, {
							x = x-large; tx = tx+large }) ;
					i = wiring.occurrencesOf([v, e[0]]) ; 
					if ( y < ty, { i = i.neg } );
					wiring.removeAt(wiring.indexOfEqual([v, e[0]] ))  ;
					// stolen from graphista
					// loop
					if (v == e[0], {
						i = 10+(i*10) ;
						labelList = labelList.add(
						// edge labels
						GUI	.staticText.new
						// 20 is empirical
							(w, Rect.new(tx-(i*0.5)-large, ty-(i*0.5)-20, large*2, tall*2))
							.string_(e[2].asString++": "++e[1])
							.stringColor_(Color.new(1.0, 0,0, 0.7)) 
							.font_(GUI.font.new(fontName, fontSize)) ;
							) ;					
					}, {
						// no loop
						i = i*25 ;
						dist1_2 = sqrt(((x-tx)**2)+((y-ty)**2)) ;
						xm = (x+tx)*0.5 ;
		    				ym = (y+ty)*0.5 ;
		    				caty = abs(ty-y) ;
		    				catx = tx-x ;
		    				catnewy = (catx*i)/dist1_2 ;
		    				if ( ty < y, { my = ym+catnewy }, {
							my = ym-catnewy }) ; 
		    				catnewx = (caty*i)/dist1_2 ;
		    				mx = xm+catnewx ;
					labelList = labelList.add(
					// edge labels
					GUI	.staticText.new(w, Rect.new(mx, my, large*2, tall*2))
						.string_(e[2].asString++": "++e[1])
						.stringColor_(Color.new(1.0, 0,0, 0.7)) 
						.font_(GUI.font.new(fontName, fontSize)) 
						) ;
						})
						
						}) ;
						
					});
				}) ;
				//w.refresh ;
	}

	drawEdges { 					

		// substantially set the drawHook
		w.drawHook = {
			var v, x, y, vLab, target, tx, ty ;
			var mx, my, xm, ym, caty, catx, catnewy, catnewx, dist1_2, i ;
			var large = vDim[0]/2, tall = vDim[1]/2 ;
			this.calculateWiring ;

			GUI.pen.width = 0.5;	
			GUI.pen.color = Color.new(1.0, 1.0, 1.0, 0.5) ; // Bug: the edges are no visible after setbackground
			// plot  edges
			graph.graphDict.do({ arg item, index ;
				if (item.size > 5, { 
				item[5..].do({ arg e ;
					v = graph.graphDict.findKeyForValue(item) ;
					x = item[0] ; 
					y = item[1] ; 

					target = graph.graphDict[e[0]] ;
					tx = target[0] ;
					ty = target[1] ;
					if ( x < tx, { x = x+large ; tx = tx-large }, {
							x = x-large; tx = tx+large }) ;
					i = wiring.occurrencesOf([v, e[0]]) ;
					if ( y < ty, { i = i.neg } );
					wiring.removeAt(wiring.indexOfEqual([v, e[0]] ))  ;
					// stolen from graphista
					// loop
					if (v == e[0], {
						i = large*2+(i*10) ;
						GUI.pen.strokeOval(Rect(tx-(i*0.5)-large, ty-(i*0.5), i,i))
					
					}, {
						// no loop
						i = i*25 ;
						dist1_2 = sqrt(((x-tx)**2)+((y-ty)**2)) ;
						xm = (x+tx)*0.5 ;
		    				ym = (y+ty)*0.5 ;
		    				caty = abs(ty-y) ;
		    				catx = tx-x ;
		    				catnewy = (catx*i)/dist1_2 ;
		    				if ( ty < y, { my = ym+catnewy }, {
							my = ym-catnewy }) ; 
		    				catnewx = (caty*i)/dist1_2 ;
		    				mx = xm+catnewx ; 
						//------------------------------
						GUI.pen.moveTo(x @ y) ;
						GUI.pen.curveTo(tx @ ty, mx @ my, mx @ my ) ;
							}) ;
							});
					}) ;
					}) ;
					GUI.pen.stroke;
					} ;
		//w.refresh	
	}
		

///// Required if you want to force update, e.g. outside a routine
	
	setEdges { arg bool ; edges = bool ;
		this.update ;
	}

	setVertices { arg bool ; vertices = bool ;
		this.update ;
	}

	setELabels { arg bool ; eLabels = bool ;
		this.update ;
	}

	setVLabels { arg bool ; vLabels = bool ;
		this.update ;
	}
	
	setVDim { arg dimArray ; vDim = dimArray ;
		this.update ;
	}
	
	setEDim { arg dimArray ; eDim = dimArray ;
		this.update ;
	}
	
	setFontSize { arg size ; fontSize = size ;
		this.update ;
	}

	setFontName { arg name ; fontName = name ;
		this.update ;
	}
	
	setAlpha { arg anAlpha ;
		alpha = anAlpha ; 
		if ( GUI.current.name == \SwingGUI, 
			{ w.server.sendMsg
				( '/methodr', '[', '/method', w.id, \getPeer, ']', \setAlpha, anAlpha )
		}) ;
	}
	
	drawListener { arg a,b,orient;	//orient visualization for GUI future implementation
		var butt,vLab;
		vLab = "LISTENER";
		if (vPosScale == nil, {	
			},{
			a = (a * vPosScale) ; 
			b = (b * vPosScale) ; 
			}
		);
		butt = GUI.staticText
			.new(w, Rect.new(a-(lviewdim), b-(lviewdim*0.25), lviewdim*2, lviewdim*0.5)) //a circle will be better...
			.background_(Color(1, 1, 1, 0.7)) 
			.string_(vLab)
			.align_(\center)
			.stringColor_(Color.black)
			.font_(GUI.font.new(fontName, fontSize)) ;
		//w.refresh ;
		^butt
	}
	

	setBackground { arg imagePath ;	//--> Bug: No edges visualisation after graph.setBackground
		var i;
		i = ImageView( w, Rect( 0, 0, windowWidth, windowHeight, 0)) ;
		GUI.schemes.do(_.put(\imageView,ImageView));
		i.path_(imagePath) ;
	
		//setBackground hides the listener.
		if (geoListener != nil,
			{buttListener = this.drawListener(geoListener.la,geoListener.lb,geoListener.lorient)}
		);
	}
	


	// pretty empyrical
	calculateVColor { arg vCount, alpha = 0.7 ;
		var r, g, b ;
		vCount = (vCount*colFact).clip2(1) ; // scaled
		// a mapping, empirical
		r = vCount ; g = vCount ; b = 1-vCount ;
		^Color.new(r, g, b, alpha)
	}
	
	update { arg theChanged, theChanger, more;
		// more is the list being sent 
		var chgClass = theChanged.class ;
		var chger = theChanger;
		var a,b,orient,position;//add by M
		
		{
		case 
		{ (chgClass  == Graph).or(chgClass  == Nil) } 
			{
			labelList.do({ arg label ; label.remove }) ;
			labelList = [] ;
			vStaticDict.do({ arg butt; butt.remove }) ;
			vStaticDict = IdentityDictionary.new ;
			if ( edges == true, { this.drawEdges }, { w.drawHook = nil }) ;
			if ( eLabels == true, { this.drawEdgeLabels }) ;
			if ( vertices == true, { this.drawVertices }) ;
			w.refresh ;
			w.front;	
			} 

		{ ( chgClass == Runner).and(more[0] == \actant) }			
			{
			vStaticDict[more[1]].background_(Color.red) ;
			Routine.new({ more[2][2].clip(activation[0], activation[1]).wait ;
				vStaticDict[more[1]]
				.background_(this.calculateVColor( runner.statsDict[more[1]] )) ;
				}).play ;	
			}

		{ chger == \position }
		
			{
			buttListener.remove;
			a = more[1];
			b = more[2];
			orient = more[3];
			buttListener = this.drawListener(a,b,orient);
			}
		}.defer
	}
	
}                                                                                                  