MultiFilePlayerGui {

	var <w;
	var <fn,open;
	var nextB,playB,resetB,goto,<line;
	var <slider,<>sliderSpec;
	var <label,<prevFB,<nextFB,<gotoF,<file,<stamp;
	
	var <linedist;

	var <fp;
	
	var <>updFunc;
	var <playTask;

	*new{ |fp,parent|
		^super.new.fp_(fp).init(parent);
	}

	fp_{ |filep|
		fp = filep;
		this.estimateLine;
	}

	init{ |parent|
		
		w = Window.new( "MultiFilePlayer", Rect( 0, 0, 200, 92 ));
		w.view.decorator = FlowLayout.new( Rect(0,0,200,92), 2@2, 2@2);
		
		//	fn = TextField.new(w, Rect( 0, 0, 148, 20 ));
		//	open = Button.new(w, Rect( 150, 0, 45, 20 )).states_( [["open"]]);

		nextB = Button.new( w, Rect( 0, 0, 22, 20)).states_( [["N"]]);
		playB = Button.new(w, Rect( 0, 0, 48, 20 )).states_( [["play"],["stop"]]);
		resetB = Button.new(w, Rect( 0, 0, 22, 20 )).states_( [["R"]]);
		goto = Button.new(w, Rect( 0, 0, 48, 20 )).states_([["go to"]]);
		line = NumberBox.new( w, Rect( 0, 0, 45, 20)).value_( 0 );

		label = StaticText.new( w, Rect( 0, 0, 48, 20)).string_( "file");
		prevFB = Button.new( w, Rect( 0, 0, 22, 20)).states_( [["<"]]);
		nextFB = Button.new( w, Rect( 0, 0, 22, 20)).states_( [[">"]]);
		gotoF = Button.new(w, Rect( 0, 0, 48, 20 )).states_([["go to"]]);
		file = NumberBox.new( w, Rect( 0, 0, 45, 20)).value_( 0 );
		stamp = StaticText.new( w, Rect( 0, 0, 195, 20)).background_( Color.white );

		slider = Slider.new( w, Rect( 0, 0, 200, 20));

		w.front;

		sliderSpec = [0,fp.indexFile.length,\linear,1].asSpec;

		updFunc = { |v| v.postln; };

		playB.action = { |but| if ( but.value == 1 ){ playTask.play; }{ playTask.pause } };
		nextB.action = { updFunc.value( fp.next ); this.updateLoc };
		resetB.action = { fp.reset; playTask.reset; this.updateLoc };
		goto.action = { fp.goToLine( line.value ); this.updateLoc; };

		nextFB.action = { fp.openFile( fp.curid + 1 ); this.updateLoc };
		prevFB.action = { fp.openFile( fp.curid - 1 ); this.updateLoc };
		gotoF.action = { fp.openFile( file.value ); this.updateLoc; };

		/*
		slider.action = { |sl|
			var lmval,lval;
			lval = sliderSpec.map( sl.value );
			lmval = fp.lineMap.array.indexInBetween( lval ).floor;
			if (  ( (sl.value) - sliderSpec.unmap( fp.lineMap.at( lmval ) ) ) < 0.1 ){
				fp.goToLine( lmval ); 
			}{
				fp.goToLine( (lval/linedist).floor; );
			};
			this.updateLoc;
		};
		*/

		this.setPlayTask;
	}

	estimateLine { 
		var cl,dist; 
		if ( fp.indexFile.lineMap.size < 2 ){
			// get at least one increase in lines and reset to the old line id:
			cl = fp.indexFile.currentLine; fp.indexFile.next; fp.indexFile.goToLine( cl ); 
		};
		linedist = (fp.indexFile.lineMap.at(1) - fp.indexFile.lineMap.at(0) );
	}

	updateLoc { 
		defer{
			file.value_( fp.curid );
			line.value_( fp.currentLine );
			if ( fp.indexFile.lineMap.at( fp.curid ).notNil ){
				slider.value = sliderSpec.unmap( 
					fp.indexFile.lineMap.at( fp.curid ) );
			};
		};
		this.updateStamp;
	}

	updateStamp {
		defer{ stamp.string_( fp.readIndexLine( fp.curid ).first ); };
	}

	setPlayTask{ |func,dt = 0.1|
		updFunc = func ? updFunc;
		playTask = Task.new( { var val; 
			while ( { (val = fp.next).notNil },{
				updFunc.value( val ); 
				this.updateLoc;
				dt.value.wait;
			});
			playB.value = 0;
		} );
	}

}
