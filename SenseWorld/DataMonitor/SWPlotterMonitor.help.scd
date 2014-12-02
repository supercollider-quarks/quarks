// A very simple example use, plotting some fake data:

(
// some fake data
e = 0;
f = 0;
r = Routine{
    loop {
        e = 0.0.rrand(1);
        f = 0.0.rrand(1).cos;
        0.5.wait;
    }
}.play;


// look at the data
m = SWPlotterMonitor.new( {
    //data to plot
    [
    [e, f], // view the two streams superimposed
    [e - f], // view their differene
    [(e-f).sign] // view sign of difference
    ]},
    200, //nr of points to plot
    2, // channels
    0.25, // plot dtime
    1 // write every n data points
    ).start;

)
// stop and restart the data
r.stop
r.reset.play

// stop and restart the plotter
m.stop;
m.start;

// you can access Plotter and send it messages:
// e.g. change window appearence
m.plotter.parent.close;
m.plotter.name_("plot some fake data");
m.plotter.bounds_( Rect(100, 100, 700, 700))
m.plotter.makeWindow;

// also, all the keyboard shortcuts from Plotter work (found in the Plotter helpfile)