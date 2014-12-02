ParameterSpace{
	var <>rect,<curloc;
	var <points;
	var <parameterDict;
	var <weightDict;
	var <defaultParDict;
	var <parKeys;
	var <currentPars;
	var <weightfactor=2;
	var <>updateFunc;
	var <>moveFunc,<>moveScale=1;
	var <task;
	var <>dt=0.1;
	var <>mindist = 0.01;

	// GUI
	var <w,<slider,<watcher;
	var <keysGui;
	var <guiUpdate;

	*new{ |rect|
		^super.new.rect_(rect).init;
	}

	init{ 
		points = List.new;
		parKeys = List.new;
		parameterDict = MultiLevelIdentityDictionary.new;
		defaultParDict = IdentityDictionary.new;
		weightDict = IdentityDictionary.new;
		curloc = Point.new;
		currentPars = IdentityDictionary.new;
		updateFunc = {};
		moveFunc = { [0,0] };
		guiUpdate = {};
		task = Task.new{
			loop{
				this.move( (moveFunc.value * moveScale).asPoint );
				dt.wait;
			};
		};
	}

	start{
		task.start;
	}

	stop{
		task.stop;
	}

	addParameter{ |key,default,weight|
		weight = weight ? weightfactor;
		parKeys.add( key );
		defaultParDict.put( key, default );
		weightDict.put( key, weight );
		currentPars.put( key, default );
		parameterDict.do{ |dict| dict.put( key, default ) };
	}

	addPoint{ |point|
		if ( rect.contains( point ),
			{ 
				points.add( point );
				parameterDict.putTree( point, defaultParDict.deepCopy );
			},
			{ "point not inside rectangle".warn });
			
	}
	
	set{ |loc|
		if ( curloc.dist( loc ) > mindist,{
			curloc = loc.constrain( rect );
			this.calculate;
			guiUpdate.value;
			updateFunc.value;
		});
	}

	weightfactor_{ |newweight|
		weightDict.keysValuesChange{ |key,value|
			if ( value == weightfactor,
				{ newweight },
				{ value });
		};
	}

	calculate{
		var distances,invdistsum,thispoint;
		thispoint = points.detect( { |it| it.dist( curloc) < mindist } );
		if ( thispoint.isNil, {
			distances = points.collect{ |it,i| it.dist( curloc ) };
			currentPars.keysValuesChange( { |key,value| 
				invdistsum = distances.sum( { |it| 1/pow(it,weightDict.at( key )) } );
				defaultParDict.at( key ) + (points.sum( { |point,i| (parameterDict.at( point, key )-defaultParDict.at( key ))/pow(distances[i],weightDict.at( key ) ) } ) / invdistsum );
			} );
		},{
			currentPars = parameterDict.at( thispoint );
		});
	}

	move{ |vector|
		this.set( curloc + vector );
	}

	makeGui{
		var ysize,vpos,vbounds;

		ysize = parKeys.size*20 + 130;

		// create a window to view the buffer contents:
		w = GUI.window.new( "Parameter Space", Rect( 0, 0, 130, ysize ) );

		vpos = 2;

		keysGui = parKeys.collect({ arg key,i;
			GUI.staticText.new(w, Rect(2, vpos, 60, 18)).string_( key );
			vbounds = Rect(65, vpos, 60, 18);
			vpos = vpos + 20;
			GUI.staticText.new(w, vbounds);			
		});

		slider = GUI.slider2D.new( w, Rect( 2, vpos, 125, 125 ) );
		slider.action = { |sl| this.set( [sl.x, sl.y].asPoint*rect.extent + rect.origin ) };

		w.front;

		guiUpdate = {
			defer{
				parKeys.do{ |key,i|
					keysGui[i].string_( currentPars.at( key ).round(0.001) );
				};
				slider.setXY( (curloc.x - rect.left) / rect.width, (curloc.y - rect.top)/ rect.height );
			}
		};
	}
}
