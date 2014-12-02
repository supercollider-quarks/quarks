SignalCharacteristics {

    var <>maxsize;

    var <data, <times, <lastTime;
    var <resdata, <stepdata, <timeresdata;

	var first = true;

    *new{ |maxsize|
        ^super.new.maxsize_( maxsize ).init;
    }

    init{
        data = Array.new;
        times = Array.new;
        lastTime = Process.elapsedTime;
    }

    mean{
        ^data.mean;
    }

    stdDev{
        ^data.stdDev;
    }

    range{
        ^[ data.minItem, data.maxItem ];
    }

    median{
        ^data.median
    }

    addValue{ |newval|
        var thistime = Process.elapsedTime;
		if ( first ){
			lastTime = thistime;
			first = false;
		};
        times = times.add( thistime - lastTime );
        lastTime = thistime;
        if ( times.size > ( maxsize-1 ) ){ times = times.drop(1) };
        data = data.add( newval );
        if ( data.size > maxsize ){ data = data.drop(1); };
    }

	empty{
		^( data.size < 5 );
	}

    totalTime{
        ^times.sum;
    }

    calcResolution{
        var datacopy = data.copy;
        var timecopy = times.copy;
        resdata = datacopy.asSet.asArray.sort;
        stepdata = resdata.differentiate.drop(1);
        timeresdata = timecopy.asSet.asArray.sort;
    }

    makeGui{
        ^SignalCharacteristicsGui.new( this );
    }
}

SignalCharacteristicsGui {
    var <>signalc;

    var <window;
    var subwin, subwin2, subwin3;

    var dataLabel;
    var medianNumber, meanNumber, stdNumber;
    var rangerStat, ranger, dataPlot;
    var resolutionLabel, stepsNumber, stepMeanNumber, stepStdNumber, stepRangerStat, stepRanger;
    var histoPlot;

    var timeresolutionLabel, sampleMeanNumber, sampleStdNumber, sampleRangerStat, sampleRanger, sampleHistoPlot, samplePlot;

    var <updater;

	var wasEmpty;

    *new{ |signal|
        ^super.new.signalc_( signal ).init;
    }

    init{
        window = Window.new( "data characteristics", Rect( 0,0, 900, 300 ), scroll: false );
        window.addFlowLayout( 0@0, 0@0 );

        subwin = CompositeView.new( window, Rect( 0,0,300, 300 ) );
        subwin.addFlowLayout( 2@2, 2@2 );

		if ( signalc.empty.not ){
			this.fillWindow;
		}{
			wasEmpty = true;
		};

		window.front;

        updater = SkipJack.new( {
			if ( wasEmpty and: signalc.empty.not ){
				this.fillWindow;
			};
			if ( wasEmpty.not ){
				signalc.calcResolution;
				this.updateVals;
			};
		}, 0.2, { window.isClosed } );
	}

	fillWindow{

        signalc.calcResolution;


        dataLabel = StaticText.new( subwin, Rect( 0,0, 294, 20 ) ).string_( "Data" ).align_( \center ).background_( Color.green );
        medianNumber = EZNumber.new( subwin, Rect( 0,0, 145, 20 ), "median", initVal: signalc.median ).round_( 0.0001 ).numberView.decimals_( 4 );

        subwin.decorator.nextLine;
        meanNumber = EZNumber.new( subwin, Rect( 0,0, 145, 20 ), "mean", initVal: signalc.mean ).round_( 0.0001 ).numberView.decimals_( 4 );
        stdNumber = EZNumber.new( subwin, Rect( 0,0, 145, 20 ), "std-dev", initVal: signalc.stdDev ).round_( 0.0001 ).numberView.decimals_( 4 );


        rangerStat = EZRanger.new( subwin, Rect( 0,0, 294, 20 ), "mean/std", initVal: signalc.mean + ( signalc.stdDev* [-1,1]), labelWidth:60 );
        rangerStat.setColors(sliderColor: Color.yellow, knobColor: Color.black );
        ranger = EZRanger.new( subwin, Rect( 0,0, 294, 20 ), "range", initVal: [ signalc.data.minItem, signalc.data.maxItem], labelWidth:60 );
        ranger.setColors(sliderColor: Color.yellow, knobColor: Color.black );


        dataPlot = Plotter.new( "data", Rect( 0,0, 294, 190 ), subwin );
        dataPlot.value = signalc.data;
        dataPlot.domainSpecs_( [ 0, signalc.totalTime, \linear, signalc.totalTime/signalc.maxsize, 0, " s" ].asSpec ); dataPlot.refresh;


        subwin2 = CompositeView.new( window, Rect( 0,0,300, 300 ) );
        subwin2.addFlowLayout( 2@2, 2@2 );


        resolutionLabel = StaticText.new( subwin2, Rect( 0,0, 294, 20 ) ).string_( "Resolution" ).align_( \center ).background_( Color.green );
        stepsNumber = EZNumber.new( subwin2, Rect( 0,0, 145, 20 ), "steps", [0,1000].asSpec, initVal: signalc.resdata.size, numberWidth: 80 );
        subwin2.decorator.nextLine;
        stepMeanNumber = EZNumber.new( subwin2, Rect( 0,0, 145, 20 ), "mean", initVal: signalc.stepdata.mean, numberWidth: 80  ).round_( 0.0001 ).numberView.decimals_( 4 );
        stepStdNumber = EZNumber.new( subwin2, Rect( 0,0, 145, 20 ), "std-dev", initVal: signalc.stepdata.stdDev, numberWidth: 80  ).round_( 0.0001 ).numberView.decimals_( 4 );

        stepRangerStat = EZRanger.new( subwin2, Rect( 0,0, 294, 20 ), "mean/std", [0,0.1,\linear,0.001].asSpec, initVal: signalc.stepdata.mean + (signalc.stepdata.stdDev* [-1,1]), labelWidth:60 );
        stepRangerStat.setColors(sliderColor: Color.yellow, knobColor: Color.black );
        stepRanger = EZRanger.new( subwin2, Rect( 0,0, 294, 20 ), "range", [0,0.1,\linear,0.001].asSpec, initVal: [signalc.stepdata.minItem, signalc.stepdata.maxItem], labelWidth:60 );
        stepRanger.setColors(sliderColor: Color.yellow, knobColor: Color.black );


        histoPlot = Plotter.new( "histogram", Rect( 0,0, 294, 190 ), subwin2 );
        // plot(discrete: true);
        histoPlot.plotMode_( \steps );
        histoPlot.domainSpecs_( [ signalc.data.minItem, signalc.data.maxItem].asSpec );
        histoPlot.refresh;


        subwin3 = CompositeView.new( window, Rect( 0,0,300, 300 ) );
        subwin3.addFlowLayout( 2@2, 2@2 );

        timeresolutionLabel = StaticText.new( subwin3, Rect( 0,0, 294, 20 ) ).string_( "Sample resolution" ).align_( \center ).background_( Color.green );
        // a.stepsNumber = EZNumber.new( a.subwin2, Rect( 0,0, 145, 20 ), "steps", [0,1000].asSpec, initVal: a.resdata.size, numberWidth: 80 );
        // a.subwin2.decorator.nextLine;
        sampleMeanNumber = EZNumber.new( subwin3, Rect( 0,0, 145, 20 ), "mean", initVal: signalc.times.mean, numberWidth: 80  ).round_( 0.0001 ).numberView.decimals_( 4 );
        sampleStdNumber = EZNumber.new( subwin3, Rect( 0,0, 145, 20 ), "std-dev", initVal: signalc.times.stdDev, numberWidth: 80  ).round_( 0.0001 ).numberView.decimals_( 4 );

        sampleRangerStat = EZRanger.new( subwin3, Rect( 0,0, 294, 20 ), "mean/std", [0,0.5,\linear,0.001].asSpec, initVal: signalc.times.mean + (signalc.times.stdDev* [-1,1]), labelWidth:60 );
        sampleRangerStat.setColors(sliderColor: Color.yellow, knobColor: Color.black );
        sampleRanger = EZRanger.new( subwin3, Rect( 0,0, 294, 20 ), "range", [0,1,\linear,0.001].asSpec, initVal: [signalc.times.minItem, signalc.times.maxItem], labelWidth:60 );
        sampleRanger.setColors(sliderColor: Color.yellow, knobColor: Color.black );

        sampleHistoPlot = Plotter.new( "times histogram", Rect( 0,0, 294, 100 ), subwin3 );
        samplePlot = Plotter.new( "times", Rect( 0,0, 294, 100 ), subwin3 );
        // plot(discrete: true);
        samplePlot.value = signalc.times;
        sampleHistoPlot.plotMode_( \steps );
        sampleHistoPlot.domainSpecs_( [ signalc.times.minItem, signalc.times.maxItem].asSpec );
        sampleHistoPlot.refresh;

		wasEmpty = false;

    }

    updateVals{
        var histodata, sampleHistoData;
        defer{
            medianNumber.value = signalc.median;
            meanNumber.value = signalc.mean;
            stdNumber.value = signalc.stdDev;
            rangerStat.value = signalc.mean + ( signalc.stdDev* [-1,1]);
            ranger.value = [ signalc.data.minItem, signalc.data.maxItem];
            dataPlot.value = signalc.data;
            dataPlot.domainSpecs_( [ 0, signalc.totalTime, \linear, signalc.totalTime/signalc.maxsize, 0, " s" ].asSpec );


            stepsNumber.value = signalc.resdata.size;
            stepMeanNumber.value = signalc.stepdata.mean;
            stepStdNumber.value = signalc.stepdata.stdDev;

            stepRangerStat.value = signalc.stepdata.mean + (signalc.stepdata.stdDev* [-1,1]);
            stepRanger.value = [signalc.stepdata.minItem, signalc.stepdata.maxItem];

            histodata = signalc.data.histo( signalc.resdata.size );
            histoPlot.domainSpecs_( [ signalc.data.minItem, signalc.data.maxItem].asSpec );
            histoPlot.value = histodata;

            sampleMeanNumber.value = signalc.times.mean;
            sampleStdNumber.value = signalc.times.stdDev;

            sampleRangerStat.value = signalc.times.mean + (signalc.times.stdDev* [-1,1]);
            sampleRanger.value = [signalc.times.minItem, signalc.times.maxItem];

            samplePlot.value = signalc.times;
            sampleHistoData = signalc.times.histo( signalc.maxsize/10 );
            sampleHistoPlot.domainSpecs_( [ signalc.timeresdata.minItem, signalc.timeresdata.maxItem].asSpec );
            sampleHistoPlot.value = sampleHistoData;

            signalc.times.mean + (signalc.times.stdDev* [-1,1])
        };
    }
}


/*
(
Tdef( \generateData, { loop{
    a[ \addData ].value( 0.5.gauss(0.1).round(0.01) );
    a[ \getStepData ].value;
    rrand(0.03,0.07 ).wait
    };
} );
);
Tdef( \generateData ).play;
Tdef( \generateData ).stop;
*/