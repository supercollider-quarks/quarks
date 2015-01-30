/*
    Unit Library
    The Game Of Life Foundation. http://gameoflife.nl
    Copyright 2006-2011 Miguel Negrao, Wouter Snoei.

    GameOfLife Unit Library: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GameOfLife Unit Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GameOfLife Unit Library.  If not, see <http://www.gnu.org/licenses/>.
*/

UScoreEditorGui_TopBar {

    var <>scoreView;
    var <header, <>views, <scoreEditorController, <scoreController;

    *new{ |parent, bounds, scoreView|
        ^super.newCopyArgs(scoreView).init(parent, bounds)
    }

    init{ |parent, bounds|
        this.makeGui(parent, bounds);
        this.addScoreEditorController;
    }

    remove{
        if(scoreEditorController.notNil) {
                scoreEditorController.remove;
        };
        if( scoreController.notNil ) {
	        scoreController.remove;
	    };
        header.remove;
    }

    scoreEditor{
         ^scoreView.currentEditor
    }

    addScoreEditorController{
	    
        if(scoreEditorController.notNil) {
                scoreEditorController.remove;
        };
        scoreEditorController = SimpleController( scoreView.currentEditor );
        scoreEditorController.put(\undo, { this.resetUndoRedoButtons });
        scoreEditorController.put(\redo, { this.resetUndoRedoButtons });

        if( scoreController.notNil ) {
	        scoreController.remove;
	    };
        scoreController = SimpleController( scoreView.currentScore );
        scoreController.put(\something, { this.resetUndoRedoButtons });

    }

    resetUndoRedoButtons{
        views[\redo]
            	.enabled_(this.scoreEditor.redoSize != 0)
            	.stringColor_( Color.gray( [0.5,0][ UScoreEditor.enableUndo.binaryValue ] ) );
        views[\undo]
            	.enabled_(this.scoreEditor.undoSize != 0)
            	.stringColor_( Color.gray( [0.5,0][ UScoreEditor.enableUndo.binaryValue ] ) );
    }

    selectedEvents{
        ^scoreView.selectedEvents;
    }

    selectedEventsOrAll{
        ^scoreView.selectedEventsOrAll
    }

    makeGui{ |parent, bounds|
        var font = Font( Font.defaultSansFace, 11 ), size, marginH, marginV;
		views = ();
		
	    marginH = 2;
	    marginV = 2;
		size = bounds.height - (2*marginV);
		
        header = CompositeView( parent, bounds );
        
        RoundView.pushSkin( UChainGUI.skin );
        
		header.addFlowLayout(marginH@marginV);
		header.resize_(2);

		SmoothButton( header, size@size )
			.states_( [[ \i, Color.black, Color.blue.alpha_(0.125) ]] )
			.canFocus_(false)
			.action_({ |b|
				scoreView.editSelected;
			});

		header.decorator.shift(10);

		SmoothButton( header, size@size )
			.states_( [[ '-' ]] )
			.canFocus_(false)
			.action_({
				scoreView.deleteSelected;
			});

		SmoothButton( header, size@size )
			.states_( [[ '+' ]] )
			.canFocus_(false)
			.action_({
			    if(scoreView.selectedEvents.notNil) {
				    scoreView.duplicateSelected;
				} {
				    scoreView.currentEditor.addEvent
				}
			});
			
		SmoothButton( header, size@size )
			.states_( [[ { |bt, rect|
				var square;
				square = Rect.aboutPoint( rect.center, 
						rect.width.min( rect.height ) / 5, 
						rect.width.min( rect.height ) / 4 );
						
				Pen.line( square.leftBottom, square.leftTop );
				Pen.lineTo( square.rightTop );
				Pen.lineTo( square.right @ (square.top + (square.height / 2) ) );
				Pen.lineTo( square.left @ (square.top + (square.height / 2) ) );
				Pen.lineTo( square.leftBottom );
				Pen.fillStroke;
				
			 } ]] )
			.canFocus_(false)
			.action_({
			     scoreView.currentEditor.addMarker
			});

		header.decorator.shift(10);

		SmoothButton( header, size@size  )
 			.states_( [[ "[", Color.black, Color.clear ]] )
 			.canFocus_(false)
			.radius_( 0 )
			.font_( Font( font.name, 10 ).boldVariant )
			.radius_([9,0,0,9])
			.action_({
				this.selectedEventsOrAll !? { |x| this.scoreEditor.trimEventsStartAtPos( x ) }
			});
		
		header.decorator.shift(-1);

		SmoothButton( header, size@size  )
			.states_( [[ "|", Color.black, Color.clear ]] )
			.canFocus_(false)
			.radius_(0)
			.font_( Font( font.name, 12 ).boldVariant )
			.action_({
				this.selectedEventsOrAll !? { |x| this.scoreEditor.splitEventsAtPos( x ) }
			});
			
		header.decorator.shift(-1);

		SmoothButton( header, size@size  )
			.states_( [[ "]", Color.black, Color.clear ]] )
			.canFocus_(false)
			.radius_([0,9,9,0])
			.font_( Font( font.name, 10 ).boldVariant )
			.action_({
			    this.selectedEventsOrAll !? { |x| this.scoreEditor.trimEventsEndAtPos( x ) }
		    });

		header.decorator.shift(10);

		views[\undo] = SmoothButton( header, (size * 0.8)@size )
			.states_( [[ 'arrow_pi' ]] )
			.radius_( [1,0,0,1] * (size/2) )
			.canFocus_(false)
			.enabled_(false)
			.stringColor_( Color.gray( [0.5,0][ UScoreEditor.enableUndo.binaryValue ] ) )
			.action_({
				this.scoreEditor.undo
			});
			
		header.decorator.shift(-1);

		views[\redo] = SmoothButton( header, (size * 0.8)@size )
			.states_( [[ 'arrow' ]] )
			.radius_( [0,1,1,0] * (size/2) )
			.canFocus_(false)
			.enabled_(false)
			.stringColor_( Color.gray( [0.5,0][ UScoreEditor.enableUndo.binaryValue ] ) )
			.action_({
				this.scoreEditor.redo
			});

		header.decorator.shift(-5);
		
		views[ \disableUndo ] = SmoothButton( header, 9@9 )
			.states_( [ ['+'], ['-'] ] )
			.hiliteColor_( nil )
			.value_( UScoreEditor.enableUndo.binaryValue )
			.canFocus_(false)
			.action_({ |bt|
				switch( bt.value,
					1, { 
						UScoreEditor.enableUndo = true; 
						this.scoreEditor.changed( \undo );
					},
					0, { 
						UScoreEditor.enableUndo = false;
						this.scoreEditor.clearUndo;
						this.scoreEditor.changed( \undo );
					}
				);  
			});
		
		header.decorator.shift(10);

		SmoothButton( header, size@size  )
			.states_( [[ \speaker, Color.black, Color.clear ]] )
			.canFocus_(false)
			.action_({ |b|
				this.selectedEvents !? { |x|  this.scoreEditor.toggleDisableEvents( x ) }
			});
			
		SmoothButton( header, size@size  )
			.states_( [[ \lock, Color.black, Color.clear ]] )
			.canFocus_(false)
			.action_({ |b|
				this.selectedEvents !? { |x|  this.scoreEditor.toggleLockEvents( x ) }
			});

		SmoothButton( header, size@size  )
			.states_( [[ \folder, Color.black, Color.clear ]] )
			.canFocus_(false)
			.action_({
			    this.selectedEvents !? { |x|
                        	this.scoreEditor.folderFromEvents(x);
				}
			});
			
		SmoothButton( header, 40@size  )
			.states_( [[ "unfold" , Color.black, Color.clear ]] )
			.canFocus_(false)
			.action_({
			    this.selectedEvents !? { |x|
                        	this.scoreEditor.unpackSelectedFolders(x)
			     }
			});

		header.decorator.shift(10);

		SmoothButton( header, 40@size  )
			.states_( [[ "mixer", Color.black, Color.clear ]] )
			.canFocus_(false)
			.font_( font )
			.action_({ |b|
				UMixer(scoreView.currentScore);
			});


		header.decorator.shift( header.decorator.indentedRemaining.width - (189 + size), 0 );

		StaticText( header, 30@size ).string_( "snap:" ).font_( font ).align_( \right )
			.resize_(3);

		PopUpMenu( header, 55@size )
			.items_( [ 
				"off", "cf",
				"0.001", "0.01", "0.1", 
				"1/32", "1/16", "1/12", "1/8", "1/6", "1/5", "1/4", "1/3", "1/2", "1" 
			] )
			.canFocus_(false)
			.font_( font )
			.resize_(3)
			.value_(11)
			.action_({ |v|
				if (v.value == 0)
					{ scoreView.snapActive = false; }
					{ scoreView.snapActive = true; };

				scoreView.snapH = (1/[inf, 
					ULib.servers[0].options.sampleRate ? 44100 / 
					ULib.servers[0].options.blockSize,
				1000,100,10,32,16,12,8,6,5,4,3,2,1])[ v.value ];
				});

		SmoothButton( header, size@size )
			.label_( "Q" )
			.resize_(3)
			.canFocus_(false)
			.action_({
				this.selectedEvents !? { |x| 
					this.scoreEditor.quantizeEvents(
						x, scoreView.snapH, scoreView.showTempoMap 
					)
				} ?? {
					this.scoreEditor.quantizePos( scoreView.snapH, scoreView.showTempoMap );
				};
			});

		StaticText( header, 30@size ).string_( "mode:" ).font_( font )
			.resize_(3)
			.align_('right');

		PopUpMenu( header, 50@size )
			.items_( [ "all","move","resize","fades"] )
			.canFocus_(false)
			.font_( font )
			.resize_(3)
			.value_(0)
			.action_({ |v|
				scoreView.usessionMouseEventsManager.mode = v.items[v.value].asSymbol;
			});
			
		RoundView.popSkin;

    }

}
