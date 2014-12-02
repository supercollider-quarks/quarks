// SensorData helpfile:

/// typical use of this class: add incoming sensor data in regular time intervals, and use this class to compute several characteristics of the data

// new SensorData
a = SensorData.new;

/// arguments to new are: |ltl,stl,tn,tf,td,maf|
// ltl: longterm length
// stl: shortterm length
// tn:  threshold on
// tf:  threshold off
// td:  threshold difference (used for Peak tracking)
// maf: moving average factor

// values are added with:

a.addValue( 0.3 );

// create some randomlike data for testing:
b = 1.0.rand;
50.do{ b = b + (0.2.rand2); a.addValue( b ); };

a.data.plot;

// properties of the sensordata:
a.longStdDev;  // standard deviation over the long time window
a.longMean;  // mean over the long time window
a.longIntegral; // integral over the long time window
a.shortStdDev;  // standard deviation over the short time window
a.shortMean;  // mean over the short time window
a.shortIntegral; // integral over the short time window

a.fluctuation; // ratio shortStdDev / longStdDev

a.peaks;  // peaks found in the data ( each item is [ value, index ] );
a.lastPeak; // last Peak found

a.betweenPeakRiseDecay; // find the peak between the thresholds and the rise and decay time to get to this peak. Answer is: [ peak value, index of peak, rise time, decay time];
a.betweenIntegral; // integral between thresholds

a.movingAverage;

// these variables can be set:
a.ltlen_( 200 ); // long term window length
a.stlen_( 5 ); // short term window length
a.thon_( 0.1 ); // threshold on
a.thoff_(0.08 ); // threshold off
a.thdiv_(0.02 ); // threshold difference
a.maFactor; // moving average factor: (maFactor*oldval) + ((1-maFactor)*newval);



