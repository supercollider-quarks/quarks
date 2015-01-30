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



UMenuBar {
	
	classvar <>sessionMenu, <>scoreMenu, <>viewMenu;
	classvar <>sessionAdd, <>sessionNewAdd, <>sessionSelectedEvents;

    *new { |index = 3|
	    
/* USession */
		sessionMenu = SCMenuGroup.new(nil, "Session", index);
		SCMenuItem.new(sessionMenu,  "New").action_({
			USession.new.gui
		});
		
		SCMenuItem.new(sessionMenu, "Open...").action_({
			USession.read(nil, USessionGUI(_) )
		});
		
		SCMenuItem.new(sessionMenu, "Save").action_({
			USession.current !! _.save
		});	
			
		SCMenuItem.new(sessionMenu, "Save as...").action_({
			USession.current !! _.saveAs
		});
		
		SCMenuSeparator.new(sessionMenu);
/* USession - ADD OBJECTS */
        sessionAdd = SCMenuGroup.new(sessionMenu, "Add");
        sessionNewAdd = SCMenuGroup.new(sessionAdd, "New");
		SCMenuItem.new(sessionNewAdd, "UChain").action_({
			USession.current !! _.add(UChain())
		});
		SCMenuItem.new(sessionNewAdd, "UChainGroup").action_({
        	USession.current !! _.add(UChainGroup())
        });
        SCMenuItem.new(sessionNewAdd, "UScore").action_({
            USession.current !! _.add(UScore())
        });
        SCMenuItem.new(sessionNewAdd, "UScoreList").action_({
            USession.current !! _.add(UScoreList())
        });

        SCMenuItem.new(sessionAdd, "Current score").action_({
                        USession.current !! { |session|
                            UScore.current !! { |score|
                                session.add( score )
                            }
                        }
                    });

        SCMenuItem.new(sessionAdd, "Current score duplicated").action_({
                USession.current !! { |session|
                    UScore.current !! { |score|
                        session.add( score.deepCopy )
                    }
                }
            });
            
        sessionSelectedEvents = SCMenuGroup.new(sessionAdd, "Selected events");
        
        SCMenuItem.new(sessionSelectedEvents, "all").action_({
            USession.current !! { |session|
                UScoreEditorGUI.current !! { |editor|
                    editor.selectedEvents !! { |events|
                        session.add( events.collect(_.deepCopy) )
                    }
                }
            }
        });

        SCMenuItem.new(sessionSelectedEvents, "flattened").action_({
            USession.current !! { |session|
                UScoreEditorGUI.current !! { |editor|
                    editor.selectedEvents !! { |events|
                        session.add( events.collect{ |x| x.deepCopy.getAllUChains }.flat )
                    }
                }
            }
        });

        SCMenuItem.new(sessionSelectedEvents, "into a UChainGroup").action_({
                    USession.current !! { |session|
                        UScoreEditorGUI.current !! { |editor|
                            editor.selectedEvents !! { |events|
                                session.add( UChainGroup(* events.collect{ |x| x.deepCopy.getAllUChains }.flat ) )
                            }
                        }
                    }
                });
                
          SCMenuItem.new(sessionSelectedEvents, "into a UScore").action_({
                    USession.current !! { |session|
                        UScoreEditorGUI.current !! { |editor|
                            editor.selectedEvents !! { |events|
                                session.add( UScore(* events.collect{ |x| x.deepCopy.getAllUChains }.flat ) )
                            }
                        }
                    }
                });


		//events

/* EVENTS */
		scoreMenu = SCMenuGroup.new(nil, "Score", index + 1);

/* USCORE */

		SCMenuItem.new(scoreMenu,  "New").action_({
			UScore.new.gui;
		}).setShortCut("n",true);

		SCMenuItem.new(scoreMenu, "Open...").action_({
			UScore.openMultiple(nil, UScoreEditorGUI(_) )
		}).setShortCut("o",true);

		SCMenuItem.new(scoreMenu, "Save").action_({
			UScore.current !! _.save
		}).setShortCut("s",true);

		SCMenuItem.new(scoreMenu, "Save as...").action_({
			UScore.current !! _.saveAs
		}).setShortCut("S",true);	

		SCMenuSeparator.new(scoreMenu);
		
		SCMenuItem.new(scoreMenu, "Export as audio file..").action_({
			UScore.current !! { |x| 
				Dialog.savePanel({ |path|
					x.writeAudioFile( path );
				});
			};
		});
		
		SCMenuSeparator.new(scoreMenu);

		SCMenuItem.new(scoreMenu, "Add Event").action_({
			UScoreEditorGUI.current !! { |x| x.editor.addEvent }
		}).setShortCut("A",true);
		
		SCMenuItem.new(scoreMenu, "Add Marker").action_({
			UScoreEditorGUI.current !! { |x| x.editor.addMarker }
		});

		SCMenuItem.new(scoreMenu, "Edit").action_({
			UScoreEditorGUI.current !! { |x| x.scoreView.editSelected }
		}).setShortCut("i",true);

		SCMenuItem.new(scoreMenu, "Delete").action_({
			UScoreEditorGUI.current !! { |x| x.scoreView.deleteSelected }
		}).setShortCut("r",true);

		SCMenuSeparator.new(scoreMenu);

	    SCMenuItem.new(scoreMenu, "Copy").action_({
	        UScoreEditorGUI.currentSelectedEvents !! UScoreEditor.copy(_)
		}).setShortCut("C",true);

		SCMenuItem.new(scoreMenu, "Paste").action_({
			UScoreEditorGUI.current !! { |x| x.scoreView.currentEditor.pasteAtCurrentPos }
		}).setShortCut("P",true);
		
		SCMenuSeparator.new(scoreMenu);
				
		SCMenuItem.new(scoreMenu, "Select All").action_({
			UScoreEditorGUI.current !! { |x| x.scoreView.selectAll }

		}).setShortCut("a",true);	
		
		SCMenuItem.new(scoreMenu, "Select Similar").action_({
			UScoreEditorGUI.current !! { |x| x.scoreView.selectSimilar }
		});		
		
		//sort
		SCMenuSeparator.new(scoreMenu);
		
		SCMenuItem.new(scoreMenu, "Sort Events").action_({
			UScoreEditorGUI.current !! { |x|
				UScore.current.events.sort; 
				UScore.current.changed( \numEventsChanged );
				UScore.current.changed( \something ); 
			};
		});
		
		SCMenuItem.new(scoreMenu, "Overlapping events to new tracks").action_({
			UScoreEditorGUI.current !! { |x| x.score.cleanOverlaps }
		});
		
		SCMenuItem.new(scoreMenu, "Remove empty tracks").action_({
			UScoreEditorGUI.current !! { |x| x.score.removeEmptyTracks }
		});
		
		//mute, solo
		SCMenuSeparator.new(scoreMenu);
		
		SCMenuItem.new(scoreMenu, "Disable selected").action_({
			UScoreEditorGUI.current !! { |x| x.scoreView.disableSelected }
		}).setShortCut("m",true);
		
		SCMenuItem.new(scoreMenu, "Enable selected").action_({
			UScoreEditorGUI.current !! { |x| x.scoreView.enableSelected }
		}).setShortCut("u",true);
		
		SCMenuItem.new(scoreMenu, "Enable all").action_({
			UScoreEditorGUI.current !! { |x| x.editor.enableAll }
		});
		
		SCMenuItem.new(scoreMenu, "Enable selected and disable all others").action_({
			UScoreEditorGUI.current !! { |x| x.scoreView.soloEnableSelected }
		}).setShortCut("p",true);

		//tracks
		SCMenuSeparator.new(scoreMenu);
		
		SCMenuItem.new(scoreMenu, "Add Track").action_({
			UScoreEditorGUI.current !! { |x| x.scoreView.addTrack }
		});
		
		SCMenuItem.new(scoreMenu, "Remove Unused Tracks").action_({
			UScoreEditorGUI.current !! { |x| x.scoreView.removeUnusedTracks }
		});
		
		//view
		viewMenu = SCMenuGroup.new(nil, "View", index + 3);
		SCMenuItem.new(viewMenu, "EQ").action_( { UGlobalEQ.gui; });		SCMenuItem.new(viewMenu, "Level").action_( { UGlobalGain.gui; });
		SCMenuItem.new(viewMenu, "Udefs").action_( { UdefsGUI(); });
		SCMenuItem.new(viewMenu, "Level meters").action_({
			ULib.servers.first.meter;
		});
	}
	
	*remove {
		sessionMenu !? _.remove;
		sessionMenu = nil;
		scoreMenu !? _.remove;
		scoreMenu = nil;
		viewMenu !? _.remove;
		viewMenu = nil;
	}
}

