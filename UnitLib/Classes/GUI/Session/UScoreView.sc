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


/*

  View that shows events of a score as rectangles with rounded corners.

*/
UScoreView {

     var <scoreEditorsList;
     var <usessionMouseEventsManager;
     var <>snapActive, <>snapH;
     var <>numTracks = 12;
     var <>minTracks = 12;
     var <scoreView, <scoreListView, <mainComposite, font, <parent, <bounds;
     var <>scoreList;
     var <currentScoreEditorController, <scoreController, <eventControllers = #[];
     var <tempoMapControllers;
     
     var <updateTask, calledUpdate = false, <>updateInterval = 0.2;
     var <showTempoMap = false;
     
     var <followPos = false;

     *new{ |parent, bounds, scoreEditor| ^super.new.init(scoreEditor, parent,bounds) }
     
     *current {
	     ^UScoreEditorGUI.current !? _.scoreView
     }

     init { |scoreEditor, inParent, inBounds|

        scoreEditorsList = [scoreEditor];
        snapActive = true;
		snapH = 0.25;
        font = Font( Font.defaultSansFace, 11 );
		scoreList = [scoreEditor.score];
		parent = inParent;
		bounds = inBounds;
		this.addCurrentScoreControllers;
		this.makeEventControllers;

		if( scoreEditor.score.tempoMap != TempoMap.default ) {
			showTempoMap = true;
		};
     }

     addCurrentScoreControllers {

         if(this.isInnerScore){
            if( currentScoreEditorController.notNil ) {
                currentScoreEditorController.remove;
            };
            currentScoreEditorController = SimpleController( scoreEditorsList.last );
            currentScoreEditorController.put(\preparingToChangeScore, {
                    this.baseEditor.storeUndoState;
            });
        };


        if( scoreController.notNil ) {
	        scoreController.remove;
	    };
        scoreController = SimpleController( this.currentScore );

        scoreController.put(\pos, {
		    { this.update }.defer;
		});

		scoreController.put(\events, {
            this.makeEventControllers;
        });

		scoreController.put(\something, {
		    this.update;
		    if(this.isInnerScore){
		        this.baseEditor.changed(\score);
		    }
		});
		
		tempoMapControllers.do(_.remove);
		tempoMapControllers = [
			SimpleController( this.currentScore.tempoMap )
				.put( \events, { this.currentScore.changed( \pos ) }),
			SimpleController( this.currentScore.tempoMap.barMap )
				.put( \events, { this.currentScore.changed( \pos ) } )
		];
	}

	makeEventControllers {
	    eventControllers.do(_.remove);
	    eventControllers = this.currentScore.collect{ |e|
            var s = SimpleController(e);
            [\startTime,\lockStartTime,\dur,\fadeIn,\fadeOut,\name,\track,\displayColor,\start,\end,\autoPause].do{ |key|
                s.put(key,{ { 
	                if( this.currentScore.updatePos ) {
		                this.currentScore.changed( \something );
	                };
	            }.defer; })
            };
            s
        }
	}

	remove {
        ([currentScoreEditorController, scoreController, usessionMouseEventsManager]++eventControllers++tempoMapControllers).do(_.remove)
    }

	update {
		var zoom;
		if( updateInterval > 0 ) {
			calledUpdate = true;
			if( updateTask.isNil or: { updateTask.isPlaying.not } ) {
				 updateTask = Task({
				    while { calledUpdate } {
					   this.prUpdate;
					   calledUpdate = false;
					   updateInterval.wait;
				    };
			    }, AppClock).start;
			};
		} {
			this.prUpdate;
		};
	}
	
	prUpdate {
		var zoom, score, dur;
		score = this.currentScore;
		dur = score.displayDuration;
		zoom = dur / 4;
		if( scoreView.maxZoom != zoom ) {
			scoreView.maxZoom = zoom;
			if( scoreView.scaleH != 1 ) {
				scoreView.updateSliders;
			};
		};
		if( followPos ) {
			scoreView.moveH = (score.pos / dur).clip(0,1);
		} {
			scoreView.refresh;
		};
	}

    currentEditor{
        ^scoreEditorsList.last
    }

    baseEditor{
        ^scoreEditorsList[0]
    }

    currentScore{
        ^scoreEditorsList.last.score
    }

    isInnerScore{
        ^(scoreEditorsList.size > 1)
    }
    
    followPos_ { |bool = false|
	    if( followPos != bool ) {
	   		followPos = bool;
	   		this.update;
	    };
    }

    selectedEvents{ ^usessionMouseEventsManager.selectedEvents }
    selectedEventsOrAll { ^usessionMouseEventsManager.selectedEventsOrAll }

    editSelected{
        var event, events = this.selectedEvents, gui, currentScore, chains;
        switch(events.size)
            {0}{ 
	            currentScore = this.currentScore;
	            chains = currentScore.getAllUChains;
	            if( chains.size > 0 ) {
	           	 gui = MassEditUChain( chains ).gui;
	                 gui.windowName = "MassEditUChain : % (all % events)".format( 
	                    currentScore.name, 
	                    chains.size
	                 );
	            };
	        }
            {1}{
                event = events[0];
                if(event.isFolder){
                    gui = MassEditUChain(event.getAllUChains).gui( score: event );
                    currentScore = this.currentScore;
                    gui.windowName = "MassEditUChain : % [ % ]".format( 
                    	currentScore.name, 
                    	currentScore.events.indexOf( event )
                    );
                } {
                    gui = event.gui;
                    currentScore = this.currentScore;
                    gui.windowName = "% : % [ % ]".format( 
                    	event.class,
                    	currentScore.name, 
                    	currentScore.events.indexOf( event )
                    );
                }
            }
            { 
	            
	            gui = MassEditUChain(events.collect(_.getAllUChains).flat).gui;
                 gui.windowName = "MassEditUChain : % ( % events )".format( 
                    this.currentScore.name, 
                    gui.chain.uchains.size
                 );
	       }

    }

	deleteSelected{
	    ^this.currentEditor.deleteEvents(this.selectedEvents)
	}

	selectAll {
	    usessionMouseEventsManager.eventViews.do(_.selected = true);
	    this.update;
	}

    selectSimilar {
        var selectedTypes = this.selectedEvents;
        if( selectedTypes.size > 0 ) {
	        if( selectedTypes.every(_.isKindOf( UChain )) ) {
		        selectedTypes = selectedTypes.collect{ |x| x.units.collect(_.defName) };
		        usessionMouseEventsManager.eventViews.do{ |evView|
			        if( evView.event.isKindOf( UChain ) ) {
				        if( selectedTypes.includesEqual(evView.event.units.collect(_.defName)) ) {
					        evView.selected = true
					   };
			        };
				};
	        		this.update;
	    		};
	    		if( selectedTypes.every(_.isKindOf( UMarker ) ) ) {
		    		usessionMouseEventsManager.eventViews.do{ |evView|
			    		if( evView.event.isKindOf( UMarker ) ) {
				    		evView.selected = true
				    	};
	        		};
	        		this.update;
			};
			if( selectedTypes.every(_.isKindOf( UScore ) ) ) {
		    		usessionMouseEventsManager.eventViews.do{ |evView|
			    		if( evView.event.isKindOf( UScore ) ) {
				    		evView.selected = true
				    	};
	        		};
	        		this.update;
			};
			
	    };
    }

    disableSelected {
        this.currentEditor.disableEvents(this.selectedEvents)
    }

    enableSelected {
        this.currentEditor.enableEvents(this.selectedEvents)
    }

    soloEnableSelected {
        this.currentEditor.soloEnableEvents(this.selectedEvents)
    }

    duplicateSelected {
        this.currentEditor.duplicateEvents(this.selectedEvents)
    }
    
    moveSelected { |amt = 0|
	    this.currentEditor.moveEvents(this.selectedEvents, amt)
    }
    
    moveSelectedBeats { |amt = 0|
	    this.currentEditor.moveEventsBeats(this.selectedEvents, amt)
    }
    
    changeTrackSelected { |amt = 0|
	    this.currentEditor.changeEventsTrack(this.selectedEvents, amt)
    }
    
    showTempoMap_ { |bool|
	    showTempoMap = bool.booleanValue;
	    scoreView.refresh;
    }
    
    calcNumTracks { 
	    numTracks = ((this.currentScore.events.collect( _.track ).maxItem ? ( minTracks - 3)) + 3)
			.max( minTracks );
    }

    addTrack {
        numTracks = numTracks + 1;
        minTracks = numTracks;
        this.update;
    }

    removeUnusedTracks {
	    minTracks = 12;
        this.calcNumTracks;
        this.update;
    }

    openSelectedSubScoreInNewWindow{
        this.selectedEvents !? { |x|
            var y = x.at(0);
            if(y.isFolder) {
                UScoreEditorGUI(UScoreEditor(y))
            }
        }

    }


     // call to initialize and draw view. this mean the views are only actually drawn when this method is called after creating this instance.
     // This is needed to be able to pass an instance of this class to the topbar object.
    makeView{
        mainComposite = CompositeView(parent,bounds).resize_(5);
        this.makeScoreView
    }

     remake{

        if(scoreListView.notNil){
            scoreListView.remove;
            scoreListView = nil
        };
        usessionMouseEventsManager.remove;
        if(scoreList.size > 1) {
            this.makeScoreListView;
        };
		this.makeScoreView;
     }

     addtoScoreList{ |score|
        scoreList = scoreList.add(score);
        scoreEditorsList = scoreEditorsList.add(UScoreEditor(score));
        this.addCurrentScoreControllers;
        this.remake;
        this.changed(\activeScoreChanged);
     }

     goToHigherScore{ |i|
        scoreList = scoreList[..i];
        scoreEditorsList = scoreEditorsList[..i];
        this.addCurrentScoreControllers;
        this.changed(\activeScoreChanged);
        fork{ { this.remake; }.defer }
     }

     makeScoreListView{
        var listSize = scoreList.size;
        scoreListView = CompositeView(mainComposite,Rect(0,0,mainComposite.bounds.width,24));
        scoreListView.addFlowLayout;
        scoreList[..(listSize-2)].do{ |score,i|
            SmoothButton(scoreListView,60@16)
                .states_([[(i+1).asString++": "++score.name, Color.black, Color.clear]])
                .font_( font )
			    .border_(1).background_(Color.grey(0.8))
			    .radius_(5)
			    .canFocus_(false)
			    .action_({
                    this.goToHigherScore(i);
			    })
        };
        SmoothButton(scoreListView,16@16)
                .states_([[\up, Color.black, Color.clear]])
                .font_( font )
			    .border_(1).background_(Color.grey(0.8))
			    .radius_(5)
			    .canFocus_(false)
			    .action_({
                    UScoreEditorGUI( UScoreEditor( this.currentScore ) )
			    })

     }

     makeScoreView{
        var scoreEditor = scoreEditorsList.last;
        var score = scoreEditor.score;
        var scoreBounds = if(scoreList.size > 1) {
            mainComposite.bounds.copy.height_(mainComposite.bounds.height - 24).moveTo(0,24);
        }  {
            mainComposite.bounds.copy.moveTo(0,0)
        };

        if(scoreView.notNil) {
            scoreView.view.visible_(false);
            scoreView.view.focus(false);
            scoreView.remove;
        };

       this.calcNumTracks;

        scoreView = ScaledUserViewContainer(mainComposite,
        			scoreBounds,
        			Rect( 0, 0, score.displayDuration, numTracks ),
        			5);

        //CONFIGURE scoreView
        scoreView.background = Color.gray(0.8);
        scoreView.composite.resize = 5;
	    scoreView.gridLines = [ 0, numTracks];
		scoreView.gridMode = ['blocks','lines'];
		scoreView.gridColor = Color.gray(0.5, 0.125);
		scoreView.sliderWidth = 8;
		scoreView.userView.view
		    .canReceiveDragHandler_({
                [ UChain, UScore ].includes(View.currentDrag.class) or: { 
	                View.currentDrag.class == Array && {
	                	View.currentDrag.flat.every({ |item| 
		                	[ UChain, UScore ].includes(item.class) 
	                	})
                	}
                }
            })
            .receiveDragHandler_({ |sink, x, y|
                View.currentDrag.postln;
                if( View.currentDrag.class == Array ) {
	               View.currentDrag.do({ |item|
		               this.currentScore.addEventToEmptyTrack(item.deepCopy);
	               });
                } {
                	this.currentScore.addEventToEmptyTrack(View.currentDrag.deepCopy);
                };
            })
            .beginDragAction_({
                this.selectedEvents;
            });

		scoreView.maxZoom = [64,8];
		scoreView.zoomCurve = 8;

		usessionMouseEventsManager = UScoreEditorGuiMouseEventsManager(this);

		scoreView
			.mouseDownAction_( { |v, x, y,mod,x2,y2, isInside, buttonNumber, clickCount| 	 // only drag when one event is selected for now

				var scaledPoint, shiftDown,altDown;

        		scaledPoint = [ x,y ].asPoint;
				shiftDown = ModKey( mod ).shift( \only );
				altDown = ModKey( mod ).alt( \only );

				usessionMouseEventsManager.mouseDownEvent(scaledPoint,Point(x2,y2),shiftDown,altDown,v,clickCount);

			} )
			.mouseMoveAction_( { |v, x, y, mod, x2, y2, isInside, buttonNumber|
				var snap = if(snapActive){snapH }{0};
				var shiftDown = ModKey( mod ).shift( \only );

				usessionMouseEventsManager.mouseMoveEvent(Point(x,y),Point(x2,y2),v,snap, shiftDown, v.fromBounds.width);

			} )
			.mouseUpAction_( { |v, x, y, mod, x2, y2, isInside, buttonNumber, clickCount|

				var shiftDown = ModKey( mod ).shift( \only );

				usessionMouseEventsManager.mouseUpEvent(Point(x,y),Point(x2,y2),shiftDown,v,isInside);

			} )
			.keyDownAction_( { |v, a,b,c,keycode,key|
				var arrowKey = c.getArrowKey ? key.getArrowKey;
				switch( c.asInt,
					8, { this.deleteSelected }, // backspace QT
					127, { this.deleteSelected }, // backspace Cocoa
					32, { // space bar
						score.togglePlayback( ULib.servers );
					},
					100, { // d
						this.duplicateSelected;
					},
					105, { // i
						this.editSelected;
					},
					108, { // l
						score.loop = score.loop.not;
					},
					112, { // p
						case { score.isPlaying } {
			            		score.pause;
			       		} { score.isPaused } {
				       		score.resume( ULib.servers );
			       		} { score.isPrepared } {
			       			score.stop;
			       		} {
			            		score.prepare( ULib.servers, score.pos );
			       		};
					},
					45, { // -
						score.toPrevMarker;
					},
					43, { // +
						score.toNextMarker;
					},
					46, { // .
						// always stop
						score.stop;
					},
					44, { // , 
						// always play
						case { score.isPaused } {
			            		score.resume( ULib.servers );
			       		} { score.isPrepared } { 
				       		score.start( ULib.servers, score.pos, true);
				       	} { score.isStopped } {
			       			score.prepareAndStart( ULib.servers, score.pos, true, score.loop);
			       		};
					},
					48, { // 0
						score.jumpTo( 0 );
					},
					{ 
						if( c.asInt.inclusivelyBetween( 49, 57 ) ) {
							score.jumpTo( score.markerPositions[ a.asString.interpret - 1 ] 
								? score.finiteDuration );
						};
					}
				);
				switch( arrowKey,
					\up, { // up
						this.changeTrackSelected( -1 );
					},
					\down, { // down
						this.changeTrackSelected( 1 );
					},
					\left, { // left
						if( showTempoMap ) {
							if( this.selectedEvents.size > 0 ) {
								this.moveSelectedBeats( snapH.neg )
							} {
								score.pos = score.tempoMap.useBeat( 
									score.pos, _ - snapH
								).max(0);
							}; 
						} {
							if( this.selectedEvents.size > 0 ) {
								this.moveSelected( snapH.neg );
							} {
								score.pos = (score.pos - snapH).max(0);
							};	
						}
					},
					\right, { // right
						if( showTempoMap ) {
							if( this.selectedEvents.size > 0 ) {
								this.moveSelectedBeats( snapH )
							} {
								score.pos = score.tempoMap.useBeat( 
									score.pos, _ + snapH
								);
							}; 
						} {
							if( this.selectedEvents.size > 0 ) {
								this.moveSelected( snapH );
							} {
								score.pos = score.pos + snapH;
							};	
						}
					}
				);
			})
			.beforeDrawFunc_( {
			    var dur = score.displayDuration;
				this.calcNumTracks;
				scoreView.fromBounds = Rect( 0, 0, dur, numTracks );
				scoreView.gridLines = [0, numTracks];
				} )
			.drawFunc_({ |v|
				var viewRect, pixelScale, l, n, l60, r, lr, bnds, scaleAmt;
				var top, bottom;
				
				// grid lines
				if( showTempoMap ) {
					v.drawTempoGrid( score.tempoMap );
				} {
					v.drawTimeGrid;
				};
			})
			.unscaledDrawFunc_( { |v|
				var scPos, rect;
				rect = v.view.drawBounds.moveTo(0,0);

				Pen.font = Font( Font.defaultSansFace, 10 );
				
				//draw events
				usessionMouseEventsManager.eventViews.do({ |eventView|
					eventView.draw(v, v.fromBounds.width );
				});

				//draw selection rectangle
				if(usessionMouseEventsManager.selectionRect.notNil) {
					Pen.strokeColor = Color.white;
					Pen.fillColor = Color.grey(0.3).alpha_(0.4); 
					Pen.addRect(v.translateScale(usessionMouseEventsManager.selectionRect));
					Pen.fillStroke;
				};

				//draw Transport line
				Pen.color = Color.black.alpha_(0.5);
				Pen.width = 2;
				scPos = v.translateScale( score.pos@0 );
				Pen.line( (scPos.x)@0, (scPos.x)@v.bounds.height);
				Pen.stroke;
				
				if( score.isPlaying ) { // draw ghost line showing latency
					Pen.color = Color.black.alpha_(0.25);
					scPos = v.translateScale( (score.pos - (Server.default.latency))@0 );
					Pen.line( (scPos.x)@0, (scPos.x)@v.bounds.height);
					Pen.stroke;
				};

				Pen.width = 1;
				Pen.color = Color.grey(0.5,1);
				Pen.strokeRect( rect.insetBy(0.5,0.5) );

		})
     }

     refresh{ scoreView.refresh; }
}