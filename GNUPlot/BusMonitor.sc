BusMonitor{
	var <bus;
	var <gnuplot;
	var <data;
	var <>dt = 0.05;
	var <>hisSize = 1000;
	var <>skip = 20;
	
	*new{ |bus,hisSize,skip|
		^super.new.init( bus,hisSize,skip );
	}

	init{ |b,hs,sk|
		bus = b;
		hisSize = hs ? hisSize;
		skip = sk ? skip;
		data = Array.fill( bus.numChannels, 0 );
		gnuplot= GNUPlot.new;
		this.initMonitor;
		this.setRange;
	}

	setRange{ |min=0.0,max=1.00|
		gnuplot.setYrange( min,max );
	}

	start{
		gnuplot.startMonitor;
	}

	stop{
		gnuplot.stopMonitor;
	}

	reset{
		gnuplot.monitorReset;
	}

	initMonitor{
		gnuplot.monitor( { 
			bus.getn( bus.numChannels, { |v| v.do{ |it,i| data[i] = it; } } );
			data.collect{ |it| it.value } 
		}, dt, hisSize, bus.numChannels, skip: skip ); 
	}

	cleanUp{
		gnuplot.stop;
	}

	makeGui{ |title|
		var w,min,max,decorator;
		w = w ?? { 
			w = GUI.window.new(title, Rect( 0,0,300,25 )).front; 
			w.view.decorator = FlowLayout(Rect(0, 0, 300, 25), 2@2, 2@2);
			decorator = w.view.decorator;
			w;
		};

		StaticText.new( w, 150@20 ).string_( title );
		
		min = EZNumber.new(w, 60@20, label: "min", controlSpec: [-10000,10000,\lin].asSpec, action: { this.setRange( min.value, max.value ); }, initVal: 0, labelWidth: 25, numberWidth: 30 );
		
		max = EZNumber.new(w, 60@20, label: "max", controlSpec: [-10000,10000,\lin].asSpec, action: { this.setRange( min.value, max.value ); }, initVal: 1, labelWidth: 25, numberWidth: 30 );

		Button.new(w,Rect(0,0,20,20)).states_( [[">", Color.blue],["[]", Color.red]]).action_({ |but| but.value.postln; if ( but.value == 1){ this.start }{ this.stop} } );

	}
}

BusHistoMonitor : BusMonitor{

	var <>hisSize = 500;

	initMonitor{
		gnuplot.monitorHisto( { 
			bus.getn( bus.numChannels, { |v| v.do{ |it,i| data[i] = it; } } );
			data.collect{ |it| it.value } 
		}, dt, hisSize, bus.numChannels, skip: 20 ); 
	}


	setHistoRange{ |min,max|
		gnuplot.histoMin = min;
		gnuplot.histoMax = max;
	}

	setHistoStep{ |step|
		gnuplot.histoStep = step;
	}
	
}