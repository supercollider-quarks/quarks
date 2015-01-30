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

UScoreEditorGuiMouseEventsManager {
	classvar minimumMov = 3;
	var <scoreView;
	var <score, <scoreEditor, <eventViews, <>state = \nothing;
	var <mouseMoved = false, <mouseDownPos, <unscaledMouseDownPos, <clickCount;
	var <selectionRect, theEventView;
	var <xLimit, <yLimit;
	var <isCopying = false, copyed = false;
	var <>mode = \all;
	var scoreController;
	var moveVert = false;
	var lastSelectedEvents;

	//state is \nothing, \moving, \resizingFront, \resizingBack, \selecting, \fadeIn, \fadeOut;
	//mode is \all, \move, \resize, \fades
	//protocol:
	
	//initial click:
	// inside region
	//	- no shift down -> select 
	//	- shift down -> invert selection
	// resize region area
	//   - mouseUp after no movement -> select, no resize
	//   - mouseUp after movement -> don't select, start resize, of all selected
	// outside any region
	//   -> start selecting 
	//     - shiftDown -> add to selection
	//     - no shiftDown -> only set newly selected events
	
	
	*new { |scoreView|
		^super.newCopyArgs(scoreView).init
	}

	init {
		var minWidth = scoreView.scoreView.pixelScale.x * 10;
	    var maxWidth = scoreView.scoreView.fromBounds.width;
        scoreEditor = scoreView.currentEditor;
        score = scoreView.currentScore;
        this.makeEventViews(minWidth, maxWidth);
        scoreController = SimpleController( score );

		scoreController.put(\numEventsChanged, {
		    //"rebuilding views".postln;
		    this.makeEventViews(minWidth, maxWidth)
		});
	}

	remove{
	    scoreController.remove;
	}

	makeEventViews{ |minWidth, maxWidth|
		eventViews = scoreEditor.events.select{ |x| x.hideInGUI.not }.collect{ |event,i|
			event.makeView(i,minWidth, maxWidth)
	    };
	}
	
	isResizing{
		^(state == \resizingFront) || (state == \resizingBack )
	}
	
	isResizingOrFades {
		^(state == \resizingFront) || (state == \resizingBack ) || (state == \fadeIn) || (state == \fadeOut )
	}
	
	selectedEventViews {	
		var events = this.eventViews.select{ |eventView|
			eventView.selected
		};
		^if(events.size > 0){events}{nil}
	}

	selectedEvents {
	    ^this.selectedEventViews.collect( _.event )
	}

	selectedEventsOrAll {
	    var v = this.selectedEventViews;
	    if(v.size > 0){
	        ^v.collect( _.event )
	    } {
	        v = score.events;
	        if(v.size > 0) {
	            ^v
	        } {
	            ^nil
	        }
	    }
	}
		
	mouseDownEvent{ |mousePos,unscaledMousePos,shiftDown,altDown,scaledUserView, inClickCount|
		
		mouseDownPos = mousePos;
		unscaledMouseDownPos = unscaledMousePos;
		clickCount = inClickCount;
		moveVert = true;

		eventViews.do{ |eventView|
			eventView.mouseDownEvent(mousePos,scaledUserView,shiftDown,mode)
		};
		
		theEventView = eventViews.select{ |eventView|
			eventView.state == \resizingFront
		}.at(0);
		
		if(theEventView.notNil){
			state = \resizingFront
		} {
			theEventView = eventViews.select{ |eventView|
				eventView.state == \resizingBack
			}.at(0);
			
			if(theEventView.notNil){
				state = \resizingBack
			} {
				
				theEventView = eventViews.select{ |eventView|
					eventView.state == \fadeIn
				
				}.at(0);
				
				if(theEventView.notNil){
					state = \fadeIn
				} {
					theEventView = 	eventViews.select{ |eventView|
						eventView.state == \fadeOut
					
					}.at(0);
					
					if(theEventView.notNil){
						state = \fadeOut
					} {
						theEventView = 	eventViews.select{ |eventView|
							eventView.state == \moving
						
						}.at(0);
						if(theEventView.notNil) {
							state = \moving;
							if(shiftDown.not) {
								if(theEventView.selected.not) {
									theEventView.selected = true;
									eventViews.do({ |eventView|
										if(eventView != theEventView) {
											eventView.selected = false
										}
									});
								} 
							} {
								theEventView.selected = theEventView.selected.not;
								if( theEventView.selected.not ) {
									state = \nothing;
								};
							};				
							if(altDown){
								isCopying = true;
								"going to copy";
							};					
						} {
							state = \selecting;
							selectionRect = Rect.fromPoints(mousePos,mousePos);
							if(clickCount == 2) {
							    if( scoreView.currentScore.isStopped ) {
                                    score.pos = mouseDownPos.x;
                                } {
	                                score.jumpTo( mouseDownPos.x );
                                };
                            };
						}
					}
				}		
			}
						
		};
		
		//make sure there is only one theEventView being operated on
		if(theEventView.notNil) {
			eventViews.do{ |eventView|
				if(theEventView != eventView) {
					eventView.state = \nothing
				}			
			};
			if(clickCount == 2) {
                if(theEventView.event.isFolder){
                    fork{ 0.2.wait; {
                        scoreView.addtoScoreList(theEventView.event);
                    }.defer }
                } {
                    scoreView.editSelected;
                }
            }
		};

		//for making sure groups of events being moved are not sent off screen
		xLimit = this.selectedEventViews.collect({ |ev| ev.event.startTime }) !? _.minItem;
		yLimit = this.selectedEventViews.collect({ |ev| ev.event.track }) !? _.minItem;

        if( scoreView.currentScore.playState != \stopped) {
            if([\nothing, \selecting].includes(state).not) {
                state = \nothing;
            }
        };

		("Current state is "++state);
	}
	
	mouseXDelta{ |mousePos,scaledUserView|
		^mousePos.x - mouseDownPos.x
	}
	
	mouseYDelta{ |mousePos,scaledUserView|
		^mousePos.y - mouseDownPos.y
	}
	
	mouseMoveEvent{ |mousePos,unscaledMousePos,scaledUserView,snap,shiftDown,maxWidth|
		var deltaX, deltaY, newEvents, selectedEvent;
		var tempoMap;
		
		// only start changing startTime if mouse has moved > 3px horizontally
		if( moveVert == true ) { 
			moveVert = (unscaledMousePos - unscaledMouseDownPos).x.abs <= minimumMov;
		};
		
			//score will change store undo state
			if((mouseMoved == false) && [\nothing, \selecting].includes(state).not){

                scoreEditor.storeUndoState;
                scoreEditor.changed(\preparingToChangeScore);
			};
			mouseMoved = true;

			if( isCopying && copyed.not ) {
				//"copying Events".postln;
				
				newEvents = this.selectedEventViews.collect({ |ev,j|
					ev.event.duplicate;
				});
				selectedEvent = newEvents[0];
				eventViews.do{ |ev| ev.selected_(false).clearState };
				score.events = score.events ++ newEvents;
				score.changed(\numEventsChanged);
				eventViews.do({ |item|
	                	if( newEvents.includes( item.event ) ) { item
		                	.selected_(true)
		                	.state_(\moving)
		                	.originalStartTime_( item.event.startTime )
		                	.originalTrack_( item.event.track )
	                	};
                	});
                	theEventView = eventViews.detect({ |item| item.event === selectedEvent });

				//("scoreEvents "++score.events.size).postln;
				//("selected events"++this.selectedEventViews).postln;
				copyed = true;				
			};
		
			if([\nothing, \selecting].includes(state).not) {

				deltaX = this.mouseXDelta(mousePos,scaledUserView);
				deltaY = this.mouseYDelta(mousePos,scaledUserView).round( scaledUserView.gridSpacingV );
				if(state == \moving) {
					deltaX = deltaX.max(xLimit.neg);
					deltaY = deltaY.max(yLimit.neg);	
				};
				
				//if event is selected apply action all selected, otherwise apply action only to the event
				if( scoreView.showTempoMap ) { tempoMap = score.tempoMap };
				if(theEventView.selected) {
					
					this.selectedEventViews.do{ |eventView|
						("resizing "++eventView);
						eventView.mouseMoveEvent(deltaX,deltaY,state,snap,shiftDown or: moveVert,tempoMap)
					}
				} {
					theEventView.mouseMoveEvent(deltaX,deltaY,state,snap,shiftDown or: moveVert,tempoMap)
				}				

			} {
				
				"selecting now";
				//selecting
				selectionRect = Rect.fromPoints(mouseDownPos,mousePos);
				eventViews.do{ |eventView|
						eventView.checkSelectionStatus(selectionRect,shiftDown,
							scaledUserView.pixelScale.x * 10, 							scaledUserView.viewRect.width
						);
					};
			}

		
	}
	
	mouseUpEvent{ |mousePos,unscaledMousePos,shiftDown,scaledUserView|
		var newSelectedEvents;

		if(this.isResizingOrFades) {
		    //"resizing or fades".postln;
			if(mouseMoved.not) {
				eventViews.do{ |eventView|
					if(eventView.isResizingOrFades.not) {
						eventView.selected = false
					}{
						eventView.selected = true
					}	
				}
			}
				
		} {
			if((state == \moving)) {
				//"finished move".postln;
				if(mouseMoved.not){
				    //"mouse didn't move".postln;
				    state = \nothing;
					/*
					eventViews.do({ |eventView|
						if(shiftDown.not) {
							if(eventView != theEventView) {
								eventView.selected = false
							}
						}
					});
					*/
				};
				
			} {
	
				if(state == \selecting) {
					eventViews.do{ |eventView|
						eventView.checkSelectionStatus(selectionRect,shiftDown,
							scaledUserView.pixelScale.x * 10, 							scaledUserView.viewRect.width
						);
					};

				}
			}
		};
			
		/*if( UEventEditor.current.notNil && { this.selectedEventViews[0].notNil } ) {
			this.selectedEventViews[0].event.edit( parent: scoreEditor );
		};*/

        //score was changed, warn others !
        if( (mouseMoved == true) && [\nothing, \selecting].includes(state).not){
            score.changed(\something);
		};

		//go back to start state
		eventViews.do{ |eventView|
			eventView.clearState
		};
		mouseMoved = false;
		selectionRect = nil;
		state = \nothing;
		isCopying = false;
		copyed = false;

		newSelectedEvents = this.selectedEvents;
		if(newSelectedEvents != lastSelectedEvents) {
			//notify of change of selection
			scoreView.changed(\selection, newSelectedEvents)
		};
		lastSelectedEvents = newSelectedEvents

	}
	
	
}