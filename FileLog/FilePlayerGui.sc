FilePlayerGui {

	var <w;
	var <fn,open;
	var nextB,playB,resetB,goto,<line;
	var <slider,<>sliderSpec;
	
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
		
		w = Window.new( "FilePlayer", Rect( 0, 0, 200, 50 ));
		w.view.decorator = FlowLayout.new( Rect(0,0,200,50), 2@2, 2@2);
		
		//	fn = TextField.new(w, Rect( 0, 0, 148, 20 ));
		//	open = Button.new(w, Rect( 150, 0, 45, 20 )).states_( [["open"]]);

		nextB = Button.new( w, Rect( 0, 0, 22, 20)).states_( [["N"]]);
		playB = Button.new(w, Rect( 0, 0, 48, 20 )).states_( [["play"],["stop"]]);
		resetB = Button.new(w, Rect( 0, 0, 22, 20 )).states_( [["R"]]);
		goto = Button.new(w, Rect( 0, 0, 48, 20 )).states_([["go to"]]);
		line = NumberBox.new( w, Rect( 0, 0, 45, 20)).value_( 0 );
		slider = Slider.new( w, Rect( 0, 0, 200, 20));

		w.front;

		sliderSpec = [0,fp.length,\linear,1].asSpec;

		updFunc = { |v| v.postln; };

		playB.action = { |but| if ( but.value == 1 ){ playTask.play; }{ playTask.pause } };
		nextB.action = { updFunc.value( fp.next ); this.updateLoc };
		resetB.action = { fp.reset; playTask.reset; this.updateLoc };
		goto.action = { fp.goToLine( line.value ); this.updateLoc; };
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

		this.setPlayTask;
	}

	estimateLine { 
		var cl,dist; 
		if ( fp.lineMap.size < 2 ){
			// get at least one increase in lines and reset to the old line id:
			cl = fp.currentLine; fp.next; fp.goToLine( cl ); 
		};
		linedist = (fp.lineMap.at(1) - fp.lineMap.at(0) );
	}

	updateLoc { 
		defer{
			line.value_( fp.currentLine );
			slider.value = sliderSpec.unmap( fp.lineMap.at( fp.currentLine ) );
		};
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
