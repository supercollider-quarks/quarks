SWDataMonitor {

	var <>dt;
	var <>length;
	var <>numChan;
	var <>updateFunction;
	var <>plotFunction;
	var <>skip;

	var <data;
	var <watcher;
	var counter;

	*new{ |updater,length,plotter,nc=1,dt=0.1,skip=1|
		^super.newCopyArgs( dt, length, nc, updater, plotter, skip ).init;
	}

	init{
		this.reset;
		watcher = SkipJack.new( {
			data.pop;
			data = data.addFirst( updateFunction.value );
			counter = counter + 1;
			if ( counter == skip ){
				if ( numChan > 1 ){
					plotFunction.value( data.flop, numChan );
				}{
					plotFunction.value( data.flatten, numChan );
				};
				counter = 0;
			};
		}, dt, autostart: false );
	}

	reset{
		counter = 0;
		data = Array.fill( length, 0 );
	}

	start{
		watcher.start;
	}

	isPlaying{
		^watcher.task.isPlaying;
	}

	stop{
		watcher.stop;
	}

}

SWPlotterMonitor{

	var <>plotter;
	var <>monitor;

	*new{ |updater,length,nc=1,dt=0.1,skip=1|
		^super.new.init( updater,length,nc,dt,skip );
	}

	init{ |updater,length,nc=1,dt=0.1,skip=1|
		plotter = Plotter.new( "Plotter Monitor", Rect(600, 30, 800, 250) );
		plotter.value_( updater.value ); // temporary workaround!
		plotter.superpose_( true );
		monitor = SWDataMonitor.new( updater,length,{ |data| plotter.value_( data ) }, nc, dt, skip );
	}

	setRange{ |min=0.0,max=1.00|
		plotter.specs_( [min,max].asSpec );
		//		gnuplot.setYrange( min,max );
	}

	start{
		if( plotter.parent.isNil ) { plotter.makeWindow };
		monitor.start;
	}

	isPlaying{
		^monitor.isPlaying;
	}

	stop{
		monitor.stop;
	}

	cleanUp{
		this.stop;
		plotter.close;
	}
}