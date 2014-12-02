/// class to work with (derived) sensordata and derive data from it
/// created for Schwelle, www.schwelle.org
/// (c) 2006, Marije Baalman

SensorData { var <data, <>ltlen, <>stlen, <>thon, <>thoff, <>thdiv, <>maFactor1,<>maFactor2;
	var <crossOn, <crossIDon, <crossIDoff, <lastPeak, <peakUp, <movingAverage1, <movingAverage2, <peaks;

	*new{ |ltl,stl,tn,tf,td,maf|
		^super.new.init(ltl,stl,tn,tf,td,maf);
	}

	init{ |ltl,stl,tn,tf,td,maf|
		ltlen = ltl ? 50;
		stlen = stl ? 10;
		thon = tn ? 0.1;
		thoff = tf ? 0.05;
		thdiv = td ? 0.05;
		maFactor1 = maf ? 0.95;
		maFactor2 = maf ? 0.95;
		data = Signal.new( ltlen );
		crossOn = false;
		crossIDon = ltlen + 1;
		crossIDoff = ltlen + 1;
		lastPeak = [0,0];
		movingAverage1 = 0;
		movingAverage2 = 0;
		peakUp = false;
		peaks = [];
		^this;
	}

	addValue{ |newval|
		if ( crossOn,
			{
				if ( newval < thoff, { crossOn = false; crossIDoff = -1; });
			},
			{
				if ( newval > thon, { crossOn = true; crossIDon = -1; });
			});
	
		if ( newval > lastPeak[0],
			{
				lastPeak[0] = newval;
				lastPeak[1] = -1;
				peakUp = true;
			},
			{
				if ( newval < (lastPeak[0]-thdiv),
					{ 
						peakUp = false;
						peaks = peaks.addFirst( lastPeak );
						lastPeak[0] = newval;
						lastPeak[1] = -1;
					} );
			});

		movingAverage1 = (maFactor1*movingAverage1) + ( (1-maFactor1)*newval );
		movingAverage2 = (maFactor2*movingAverage2) + ( (1-maFactor2)*newval );
		data = data.addFirst(newval);
		if ( data.size > ltlen,
			{
				data.pop( data.size - ltlen );
			});
		crossIDon = crossIDon + 1;
		crossIDoff = crossIDoff + 1;
		lastPeak[1] = lastPeak[1] + 1;
		if ( peaks.size > 0,
			{ 
				peaks.removeAllSuchThat( {|it,i| it[1] >= data.size} );
				peaks.do{ |it,i|
					peaks[i] = [ it[0], it[1] + 1 ];
				};
			});
	}

	fluctuation{
		^( this.shortStdDev / this.longStdDev );
	}

	longStdDev{
		var ldata;
		ldata = data.copyRange( 0, ltlen - 1 );
		^ldata.stdDev;
	}

	longMean{
		var ldata;
		ldata = data.copyRange( 0, ltlen - 1 );
		^ldata.meanF;
	}

	shortStdDev{
		var ldata;
		ldata = data.copyRange( 0, stlen - 1 );
		^ldata.stdDev;
	}

	shortMean{
		var ldata;
		ldata = data.copyRange( 0, stlen - 1 );
		^ldata.meanF;
	}
	
	betweenPeakRiseDecay{
		var ldata, result;
		result = Array.fill( 4, 0);
		if ( crossOn, { crossIDoff = 0 } );
		if ( (crossIDon < data.size) and: (crossIDoff < data.size ),
			{
				ldata = data.copyRange( crossIDoff, crossIDon );
				result[0] = ldata.maxItem;
				result[1] = ldata.indexOf( result[0] );
				result[2] = result[1]; // rise time;
				result[3] = ldata.size - result[1]; // decay time
				result[1] = result[1] + crossIDoff;
			});
		^result;
	}

	betweenIntegral{
		var ldata,result;
		result = 0;
		if ( crossOn, { crossIDoff = 0 } );
		if ( (crossIDon < data.size) and: (crossIDoff < data.size ),
			{
				ldata = data.copyRange( crossIDoff, crossIDon );
				result = ldata.integral;
			});
		^result;
	}

	longIntegral{
		var ldata = data.copyRange( 0, ltlen -1 );
		^ldata.integral;
	}

	shortIntegral{
		var ldata = data.copyRange( 0, stlen -1 );
		^ldata.integral;
	}

	makeGui{ |w|
		^SensorDataGUI.new( this, w );
	}
}


SensorDataGUI{
	classvar <counter = 0;
	classvar <xposScreen=0, <yposScreen=20;
	var <>w, <>data, <>labelColor, <watcher;
	var <shortStd,<longStd;
	
	*new { |data, w| 
		^super.new.w_(w).data_(data).init;
	}
	
	init {
		var xsize,ysize;
		xsize = 2 * 44 + 2;
		ysize = 30;
		counter = counter + 1;

		w = w ?? { 
			w = GUI.window.new("Sensor Data", Rect(xposScreen, yposScreen, xsize + 20, 700)).front; 
			w.view.decorator = FlowLayout(Rect(10, 10, w.bounds.width, w.bounds.height), 2@2, 2@2);
		};
		labelColor = Color.white.alpha_(0.4);

		shortStd = GUI.numberBox.new( w, Rect( 10, 10, 40, 30 ) );
		longStd = GUI.numberBox.new( w, Rect( 10, 10, 40, 30 ) );		
		if ( GUI.scheme.id == \swingosc, // why did I put \schwelle here??
			{ 
				shortStd.maxDecimals_( 3 ).minDecimals_( 3 );
				longStd.maxDecimals_( 3 ).minDecimals_( 3 );
			});

	}

	updateVals { 
		{ 
			shortStd.value = data.shortStdDev.round( 0.001 );
			longStd.value = data.longStdDev.round( 0.001 );
		}.defer;
	}

}