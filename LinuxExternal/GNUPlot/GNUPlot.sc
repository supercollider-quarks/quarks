GNUPlot { 

	// original class by Ben Moran (2006)
	// mods from John Yates (2006)
	// seriously expanded by Marije Baalman (2006-9)
	// additions by Oswald Berthold (2009)
	// additions and some refactoring by Dan Stowell (2009)

	classvar id, <>folder = "SC_to_GNUPlot/", <>initCode = "set data style lines\n", <>gnuplotpath="gnuplot";
	classvar <>default;

	var pipe,gid,<hisdata,monrout,updateFunc,<lastdata;

	// histogram:
	var <>histoMin, <>histoMax, <>histoSteps = 100;

        // This function takes an arbitrary array and plots the values
	*initClass{
		Platform.case(\osx, {
			// on OSX, common "x11" term doesn't work by default (since SC not under X11)
			initCode = initCode ++ "set term aqua\n";
			// and you can't always rely on gnuplot being in the path
			gnuplotpath = ["/opt/local/bin/gnuplot", "/sw/bin/gnuplot", "/usr/local/bin/gnuplot", "/usr/bin/gnuplot"].detect{|p| File.exists(p)};
		});
		StartUp.add{
			id = 0;
			this.folder = PathName.tmp +/+ "SC_to_GNUPlot/";
			this.makeBackupFolder;
		}
	}

	*makeBackupFolder { 
		var testfile, testname = "zzz_gnuplot_test_delete_me.txt"; 
		testfile = File(folder ++ testname, "w");
		if (testfile.isOpen.not) 
			{ unixCmd("mkdir" + folder) }
			{ testfile.close;  unixCmd("rm" + folder ++ "*.*"); }
	}

    *plot {
		|data|
		var fh, tmpname;
		tmpname = this.pr_tmpname;
		this.pr_writeTempData1(data, tmpname: tmpname);
		fh = Pipe.new(gnuplotpath + "-persist", "w");
		fh.putString(initCode);
		fh.putString("plot \"" ++ tmpname ++ "\" title \"\" \n");
		fh.close;
	}

	*bootDefault{
		if ( default.isNil ){
			default = GNUPlot.new;
			ShutDown.add( { default.stop; })
		};
	}

	*plotdHisto{ |data,ns=1,label="",verb=true|
		this.bootDefault;
		default.plotdHisto( data, ns, label, verb );
	}

	*plotd{ |data,ns=1,label=""|
		this.bootDefault;
		default.plotd( data, ns, label );
	}
	
	// Helper function to convert an envelope into a suitable array
    *envarray { |env|
		var times, step, slices;
		times = env.times;
		step = times.minItem;
		slices = Array.fill(times.sum/step,
			{|ind| env.asArray.envAt(step * ind); });
		^ slices
        }
	
	*plotenv {
		|env|
		this.plot(this.envarray(env));
		//plot(envarray(env));
	}

	*new { 
		^super.new.init;
	}
	
	init{
		gid = id;
		id = id+1;
		this.start;
	}

	start{
		pipe = Pipe.new(gnuplotpath + "-persist", "w");
		pipe.putString(this.class.initCode);
	}
	
	/*
	On Mac you need to use a script if you want to launch something in X11 rather than aqua:
	*/
	*pathToXLauncher {
		^ this.filenameSymbol.asString.dirname +/+ "mac-open-x11-direct"
	}
	
	createTempFile{ |data,ns=1|
		var fh, tmpname,unlaced;
		tmpname = this.pr_tmpname;
		if ( ns == 1,
			{
				this.class.pr_writeTempData1(data, tmpname: tmpname);
			},
			{
				this.class.pr_writeTempData2(data, ["\n", "\n\n"], tmpname: tmpname);
			});
		^tmpname;
	}

	putCommand{ |command,tmpname,label=""|
		pipe.putString(command + tmpname.quote + "title" + label.asString.quote ++ Char.nl);
		pipe.flush;
	}

	putTestCommand{ |command|
		this.deprecated(thisMethod, this.class.findMethod(\sendCmd));
		^this.sendCmd(command);
	}

	plotd{ |data,ns=1,label=""|
		defer{
			pipe.putString("plot ");
			(ns-1).do{ |i|
				pipe.putString("'-' title \""++label++(i+1)++"\",");
			};
			pipe.putString("'-' title \""++label++ns++"\"\n");
			if ( ns > 1,
				{
					ns.do{ |id|
						data.at(id).do{ |it,i| pipe.putString( "%\n".format(it) ); };
						pipe.putString("e\n");
					};
				},
				{
					data.do{ |it,i| pipe.putString( "%\n".format(it) ); };
					pipe.putString("e\n");
				});
			pipe.flush;
		};
	}

	plot{ |data,ns=1,label="", title|
		var tmpname = this.createTempFile( data, ns );
		title !? {pipe.putString("set title %\n".format(title.asString.quote))};
		if ( ns == 1,
			{
				pipe.putString("plot % title %\n".format(tmpname.asString.quote, label.asString.quote));
				lastdata = [ data ];
			},
			{
				pipe.putString("plot % index 0 title \"% 0\"".format(tmpname.asString.quote, label));
				(ns-1).do{ |i|
					pipe.putString(", % index % title \"% %\"".format(tmpname.asString.quote, i+1, label, i+1)); };
				pipe.putString( "\n" );
				lastdata = data;
			});
		pipe.flush;
		//		this.putCommand( "plot", tmpname, label );
		
	}

	replot{ |data,ns=1,label=""|
		var lastns;
		if ( ns == 1,
			{
				lastdata = lastdata.add( data );
			},
			{
				ns.do{ |i| lastdata = lastdata.add( data[i] ) };
			});
		lastns = lastdata.size;
		this.plot( lastdata, lastns, label );
	}

	autoscaleX{
		pipe.putString( "set autoscale x\n" );
		pipe.flush;
	}

	autoscaleY{
		pipe.putString( "set autoscale y\n" );
		pipe.flush;
	}
	
	autoscaleZ{
		pipe.putString( "set autoscale z\n" );
		pipe.flush;
	}

	setXrange{ |min,max|
		pipe.putString( "unset autoscale x\n" );
		pipe.putString( "set xrange [%:%]\n".format(min, max) );
		pipe.flush;
	}

	setYrange{ |min,max|
		pipe.putString( "unset autoscale y\n" );
		pipe.putString( "set yrange [%:%]\n".format(min, max) );
		pipe.flush;
	}
	
	setZrange{ |min,max|
		pipe.putString( "unset autoscale z\n" );
		pipe.putString( "set zrange [%:%]\n".format(min, max) );
		pipe.flush;
	}

	stop {
		pipe.putString( "quit\n" );
		pipe.flush;
		pipe.close;
	}

	monitorReset{
		var length = hisdata.size;
		hisdata = Array.fill( length, 0 );
	}

	monitor{ |updateF,dt,length,ns=1,skip=1| // id: id of data to monitor, dt: time step, skip: stepsize
		updateFunc = updateF;
		hisdata = Array.fill( length, 0 );
		monrout = Task{ 
			var cnt = 0;
			inf.do{ 
				hisdata.pop;
				hisdata = hisdata.addFirst( updateFunc.value );
				cnt = cnt + 1;
				if ( cnt == skip,
					{
						if ( ns > 1, {
							this.plotd( hisdata.flop, ns );
						},{
							this.plotd( hisdata.flatten, ns );
						});
						cnt = 0;
					});
				dt.wait;
			}
		};
	}

	startMonitor{
		monrout.stop.play;
	}

	stopMonitor{ 
		monrout.stop;
	}


	plotdHisto{ |data,ns=1,label="",verb=true|
		var histodata, xtics;
		var step,start,end,xticsArr;
		defer{
			if ( ns > 1, {
				histodata = data.collect{ |it| it.histo(histoSteps,histoMin,histoMax, verb) };
				xticsArr = data.first.histoBands(histoSteps,histoMin,histoMax);
				//	histodata = histodata.flop;
			},{
				histodata = data.histo(histoSteps,histoMin,histoMax, verb);
				xticsArr = data.histoBands(histoSteps,histoMin,histoMax);
				//	histodata = histodata.first;
			});


			// create the tics axis:
			step = xticsArr.size / 10;
			start = (step / 2 ).floor;
			end = xticsArr.size - start;
			
			xticsArr = xticsArr.at( ( start,(step+start)..end) );
			if ( xticsArr.last.isNil, { xticsArr = xticsArr.drop(-1); });
			
			xtics = "set xtics ( ";
			xticsArr.do{ |it,i| 
				xtics = xtics ++ "\"" ++ it.round(0.1) ++ "\"" + ( i*step+(step/2) );
				if ( i < (xticsArr.size - 1) ){ xtics = xtics ++ ", " };
			};
			xtics = xtics ++ ")";
			
			pipe.putString( xtics ++ "\n" );
			
			this.plotd( histodata, ns, label );
		};
	}

	// as Monitor, but plots a Histogram of the data
	monitorHisto{ |updateF,dt,length,ns=1,skip=1| // id: id of data to monitor, dt: time step, skip: stepsize
		updateFunc = updateF;
		hisdata = Array.fill( length, 0 );
		monrout = Task{ 
			var cnt = 0;
			var histodata;
			inf.do{ 
				hisdata.pop;
				hisdata = hisdata.addFirst( updateFunc.value );
				cnt = cnt + 1;
				if ( cnt == skip,
					{
						if ( ns > 1, {
							this.plotdHisto( hisdata.flop, ns );
						},{
							this.plotdHisto( hisdata.flatten, ns );
						});
						cnt = 0;
					});
				dt.wait;
			}
		};
	}


	// Oswalds' additions:
	plot3 {|data, label="", title, style="lines"|
		var fh, tmpname; // = this.createTempFile3( data, ns );
		defer {
			tmpname = this.pr_tmpname;
			this.class.pr_writeTempData2(data, tmpname: tmpname);
			
			["GNUPlot.plot3 data size: ", data.size].postln;
			title !? {pipe.putString("set title %\n".format(title.asString.quote))};
			pipe.putString("splot % with % title %\n".format(tmpname.asString.quote, style, label.asString.quote));
			lastdata = [ data ];
			pipe.flush;
		}
	}
	
	// the data for this should be an array-of-arrays-of-arrays, eg:
	// [[p01, p02, p03], [p10, p11, p12], [p20, p21, p22]] where each "pXX" is an array of 3D co-ords.
	/*
	g = GNUPlot.new;
	// a nice deckchair:
	g.surf3([[[0,0.5,0], [ 0.5,0,0], [ 1,-0.5,0]],   [[ 0.5,1,0], [ 1,0.5,0], [ 1.5,0,0]], [[ 1,1.5,0.5], [ 1.5,1,0.5], [ 2,0.5,0.5]]])	*/
	surf3 {|data, label="", hidden3d=true, pm3d=false, title, extracmds|
		var fh, tmpname; // = this.createTempFile3( data, ns );
		defer {
			tmpname = this.pr_tmpname;
			this.class.pr_writeTempData3(data, tmpname: tmpname);
			
			["GNUPlot.plot3 data size: ", data.size].postln;
			pipe.putString("%set hidden3d\n".format(if(hidden3d, "", "un")));
			pipe.putString("%set pm3d\n".format(if(pm3d, "", "un")));
			title !? {pipe.putString("set title %\n".format(title.asString.quote))};
			extracmds !? {pipe.putString(extracmds ++ "\n")};
			pipe.putString("set dummy u,v\n"); // This dummy tells gnuplot it's doing a surface not a curve
			pipe.putString("splot % with lines title %\n".format(tmpname.asString.quote, label.asString.quote));
			lastdata = [ data ];
			pipe.flush;
		}
	}

	multiplot{ |onoff=true|
		if ( onoff ){
			this.sendCmd("set multiplot");
		}{
			this.sendCmd("unset multiplot");
		}
	}

	scatter {|data, label="", title|
		var tmpname, dim, strs;
		defer {
			if(data[0][0].isArray){ // we have an array of multiple datasets, not just one
				dim = data.size;
				tmpname = dim.collect{|d| this.pr_tmpname("_"++d)};
				dim.do{|d| this.class.pr_writeTempData2(data[d], tmpname: tmpname[d]) };
				
				title !? {pipe.putString("set title %\n".format(title.asString.quote))};
				label = if(label.isArray and: {label.isString.not}){ label.collect(_.asString) }{[label.asString]};
				strs = dim.collect{|d|
					" % with points title % ".format(tmpname[d].asString.quote, label.wrapAt(d).quote)
					};
				pipe.putString(if(data[0][0].size==3, "splot", "plot") 
						++ strs.join(", ") ++ Char.nl);
				lastdata = [ data ];
				pipe.flush;
			}{
				tmpname = this.pr_tmpname;
				this.class.pr_writeTempData2(data, tmpname: tmpname);
				
				title !? {pipe.putString("set title %\n".format(title.asString.quote))};
				pipe.putString(  if(data[0].size==3, "splot", "plot") 
						++ "% with points title %\n".format(tmpname.asString.quote, label.asString.quote));
				lastdata = [ data ];
				pipe.flush;
			}
		}
	}
	
	/*
	boxplot API is modelled after matlab's function - send it data and it'll find the 5/25/50/75/95 percentiles,
	and then it'll boxplot them.
	GNUPlot.new.boxplot([{99.0.rand}.dup(100), {10.0.rand.squared}.dup(80), {5.0.rand.squared + 20}.dup(80)], "test", title:"box-and-whisker plot adding median value as bar")
	"summaryfunc" is by default a function that turns the data into 5/25/50/75/95 percentiles, but you can supply your own function.
	   The function would need to take a single array of numerical data and return 5 (ordered) statistics which will be plotted.
	*/
	boxplot {|data, label="", title, grplabels, summaryfunc, extracmds|
		var fh, tmpname, pciles;
		pciles = data.collect{|d, index| [index+1] ++ 
			(summaryfunc ? {|sth| sth.percentile([0.05, 0.25, 0.5, 0.75, 0.95])}).value(d, index)
		};
		defer {
			tmpname = this.pr_tmpname;
			this.class.pr_writeTempData2(pciles, tmpname: tmpname);
			
			title !? {pipe.putString("set title %\n".format(title.asString.quote))};
			grplabels !? {pipe.putString("set xtics (" ++ grplabels.collect{|alabel, index| alabel.quote + (index+1) }.join(", ") ++ ")\n")};
			extracmds !? {pipe.putString(extracmds ++ "\n")};
			pipe.putString("set boxwidth 0.2 absolute\n");
			pipe.putString("set xrange [ 0.00000 : %.0000 ] noreverse nowriteback\n".format(data.size+1));
			pipe.putString("set style fill empty\n");
			pipe.putString(("plot % using 1:3:2:6:5 with candlesticks lt 3 lw 2 title %, "
			                 ++ "'' using 1:4:4:4:4 with candlesticks lt -1 lw 2 notitle\n").format(tmpname.asString.quote, label.asString.quote));
			lastdata = [ data ];
			pipe.flush;
		}
	}

	/*
	GNUPlot.new.errorbar([7,6,2,4,3], [4, 2, 1, 0.5, 0.25]);
	GNUPlot.new.errorbar([[7,6,2,4,3], [2, 6, 8, 6, 4]], [[4, 2, 1, 0.5, 0.25], [1, 1, 0.1, 0.5, 0.5]], ["miscreantjuice", "soresal"]);
	*/
	errorbar {|data, errors, label="", title, grplabels, drawlines=true, extracmds|
		var fh, tmpname, tmpdata, command;
		
		// Ensure we're working with 2D data (promote 1D as needed)
		data.shape.size.switch(
			1, {
				// Single dataset is fine, turn it into same layout as multi
				data   = [data];
				errors = [errors];
				label  = [label];
			},
			2, {},
			{Error("data must be a 1D or 2D array").throw}
		);
		// Here we check that errors and label are 2D (they may have been auto-promoted)
		if(errors.shape.size != 2){Error("'errors' dimensionality must match that of the data").throw};
		if(label .shape.size != 2){Error("'label'  dimensionality must match that of the data").throw};
		
		// Marshal the data into a 2D table: [index, A, Aerr, B, Berr, C, Cerr ...]
		tmpdata = [data, errors].flop.flatten.flop.collect{|row, index| [index+1] ++ row};
		
		defer {
			tmpname = this.pr_tmpname;
			this.class.pr_writeTempData2(tmpdata, tmpname: tmpname);
			
			title !? {pipe.putString("set title %\n".format(title.asString.quote))};
			grplabels !? {pipe.putString("set xtics (" ++ grplabels.collect{|alabel, index| alabel.quote + (index+1) }.join(", ") ++ ")\n")};
			extracmds !? {pipe.putString(extracmds ++ "\n")};
			pipe.putString("set xrange [ 0.00000 : %.0000 ] noreverse nowriteback\n".format(data[0].size+1));

			// The string needs to have a separate command for each dataset plotted, in fact two commands each if adding lines.
			command = "plot %".format(tmpname.asString.quote) +
				data.size.collect{|i|
					"using 1:%:% title '' with errorbars ls %".format(2*i+2, 2*i+3, i+1)
					++ if(drawlines){", '' using 1:% title '%' with lines ls %".format(2*i+2, label[i], i+1) }{""}
				}.join(", '' ");
			pipe.putString(command ++ Char.nl);
			lastdata = [ data ];
			pipe.flush;
		}
	}

	// http://gnuplot.info/docs/node281.html
	setView{ |...vals|
		pipe.putString("set view %\n".format(vals.join($,)));
		pipe.putString("replot\n");
		pipe.flush;
	}
	
	sendCmd { |str|
		pipe.putString(str ++ "\n");
		pipe.flush;
	}
	
	write{ |path, term="pdf", termopts="fsize 10"|
		path ?? {
			path = PathName.tmp +/+ "gnuplot_sc.%".format(term);
			"GNUPlot:write - path %".format(path).postln;
		};
		^this.sendCmd(
			"set term % %
set output '%'
replot
set term pop".format(term, termopts, path.escapeChar($')))
	}
	
	//////////////////////////////////////////////////////////////////////
	// Helper functions to write temp data files
	*pr_writeTempData1 { |data, delims(["\n"]), tmpname|
		// And add exception handling.
		var fh = File.new(tmpname,"w");
		data.do{|val|
			fh.putString(val.asString ++ delims[0]);
		};
		fh.close;
	}
	*pr_writeTempData2 { |data, delims([" ", "\n"]), tmpname|
		// And add exception handling.
		var fh = File.new(tmpname,"w");
		data.do{|sub|
			sub.do {|val|
				fh.putString(val.asString ++ delims[0]);
			};
			fh.putString(delims[1]);
		};
		fh.close;
	}
	*pr_writeTempData3 { |data, delims([" ", "\n", "\n"]), tmpname|
		// And add exception handling.
		var fh = File.new(tmpname,"w");
		data.do{	|col|
			col.do{|sub|
				sub.do {|val|
					fh.putString(val.asString ++ delims[0]);
				};
				fh.putString(delims[1]);
			};
			fh.putString(delims[2]);
		};
		fh.close;
	}
	
	// Generate temporary filenames for class method calls, and for instance method calls
	*pr_tmpname { |postfix=""|
		var tmpname = folder +/+ "scdata_meta%%.tmp".format(id, postfix); // Todo: proper temp name!
		id = id + 1;
		^ tmpname
	}
	pr_tmpname { |postfix=""|
		^ folder +/+ "scdata%%.tmp".format(gid, postfix); // Todo: proper temp name!
	}
} // End GNUPlot class
