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


UMenuWindow {
	
	classvar <>window, <font;
	classvar <>sessionMenu, <>scoreMenu, <>viewMenu;
	classvar <>sessionDict, <>scoreDict, <>viewDict;
	
	*initClass {
		sessionDict = MultiLevelIdentityDictionary();
		scoreDict = MultiLevelIdentityDictionary();
		viewDict = MultiLevelIdentityDictionary();
		
	}
	
	*new { 
		if( window.notNil && { window.isClosed.not } ) {
			window.front;
		} {
			this.makeWindow;
		};
	}
	
	*makeWindow {
		
		if( font.class != Font.implClass ) { font = nil };
		
		font = font ?? { Font( Font.defaultSansFace, 12 ); };
		
		window = Window( "UMenuWindow", Rect(0, Window.screenBounds.height - 30, 428, 30) ).front;
		window.addFlowLayout;
		
		sessionMenu = PopUpTreeMenu(window, 120@20 )
			.font_( font )
			.tree_(
				OEM(
					'Session', (),
					'New', { USession.new.gui },
					'Open...', { USession.read(nil, USessionGUI(_) ) },
					'Save', { USession.current !? _.save },
					'Save as...', { USession.current !? _.saveAs },
					' ', (),
					'Add', OEM(
						'New', OEM( 
							'UChain', { USession.current !? _.add(UChain()) },
							'UChainGroup', { USession.current !? _.add(UChainGroup()) },
							'UScore', { USession.current !? _.add(UScore()) },
							'UScoreList', { USession.current !? _.add(UScoreList()) }
						),
						'Current score', {
                       			USession.current !? { |session|
	                       			UScore.current !? { |score|
		                       			session.add( score )
		                       		}
		                       	}
			               },
						'Current score duplicated', {
                       			USession.current !? { |session|
	                       			UScore.current !? { |score|
		                       			session.add( score.deepCopy )
		                       		}
		                       	}
						},
						'Selected events', OEM(
							'all', {
            						USession.current !? { |session|
						                UScoreEditorGUI.current !? { |editor|
						                    editor.selectedEvents !? { |events|
						                        session.add( events.collect(_.deepCopy) )
						                    }
						                }
						            }
							},
							'flattened', {
								USession.current !? { |session|
									UScoreEditorGUI.current !? { |editor|
										editor.selectedEvents !? { |events|
											session.add( events.collect{ |x|
												     x.deepCopy.getAllUChains
												}.flat
											)
										}
									}
								}
        						},
							'into a UChainGroup', {
								USession.current !? { |session|
									UScoreEditorGUI.current !? { |editor|
										editor.selectedEvents !? { |events|
											session.add( 
												UChainGroup( 
												    *events.collect{ |x|
												         x.deepCopy.getAllUChains
												    }.flat
												)
											)
										}
									}
								}
							},
							'into a UScore', {
								USession.current !? { |session|
									UScoreEditorGUI.current !? { |editor|
										editor.selectedEvents !? { |events|
											session.add( 
												UScore(
												    *events.collect{ |x|
											 	        x.deepCopy.getAllUChains 
											 	    }.flat
											 	)
											)
										}
									}
								}
							}
						)
					)
				)
			)
			.sortFunc_({true}) // no sorting; OEM is already sorted
			.value_( [ 'Session' ] )
			.action_({ |vw, value|
				vw.value = [ 'Session' ];
				vw.tree.atPath( value ).value;
			});	
		
		scoreMenu = PopUpTreeMenu(window, 150@20 )
			.font_( font )
			.tree_(
				OEM(
					'Score', (),
					'New', { UScore().gui; },
					'Open...', { UScore.open(nil, UScoreEditorGUI(_) ); },
					'Save', { UScore.current !? _.save; },
					'Save as...', { UScore.current !! _.saveAs; },
					' ', (), 
					'Export as audio file...', {
						UScore.current !? { |x| 
							Dialog.savePanel({ |path|
								x.writeAudioFile( path );
							});
						};
					},
					'  ', (),
					'Add Event', { UScoreEditorGUI.current !? { |x| x.editor.addEvent } },
					'Add Marker', { UScoreEditorGUI.current !? { |x| x.editor.addMarker } },
					'Edit', { UScoreEditorGUI.current !? { |x| x.scoreView.editSelected } },
					'Delete', { UScoreEditorGUI.current !? { |x| x.scoreView.deleteSelected } },
					'   ', (),
					'Copy', { UScoreEditorGUI.currentSelectedEvents !? UScoreEditor.copy(_) },
					'Paste', { 
						UScoreEditorGUI.current !? { |x|
							x.scoreView.currentEditor.pasteAtCurrentPos 
						};
					},
					'    ', (),
					'Clean overlaps', { 
						UScoreEditorGUI.current !? { |x| x.score.cleanOverlaps }
					},
					'Sort Events', {
						UScoreEditorGUI.current !! { |x|
							UScore.current.events.sort; 
							UScore.current.changed( \numEventsChanged );
							UScore.current.changed( \something ); 
						};
					},
					'Remove empty tracks', { 
						UScoreEditorGUI.current !? { |x| x.score.removeEmptyTracks }
					},
					'     ', (),
					'Disable selected', { 
						UScoreEditorGUI.current !! { |x| x.scoreView.disableSelected }
					},
					'Enable selected', {
						UScoreEditorGUI.current !! { |x| x.scoreView.soloEnableSelected }
					},
					'      ', (),
					'Add Track', { UScoreEditorGUI.current !! { |x| x.scoreView.addTrack } },
					'Remove Unused Tracks', { 
						UScoreEditorGUI.current !! { |x| x.scoreView.removeUnusedTracks }
					}
				)
			)
			.sortFunc_({true}) 
			.value_( [ 'Score' ] )
			.action_({ |vw, value|
				vw.value = [ 'Score' ];
				vw.tree.atPath( value ).value;
			});	
		
		viewMenu = PopUpTreeMenu(window, 140@20 )
			.font_( font )
			.tree_(
				OEM(
					'View', (),
					'EQ', { UGlobalEQ.gui; },
					'Level', { UGlobalGain.gui; },
					'Udefs', { UdefsGUI(); },
					'Level meters', { ULib.servers.first.meter; }
				)
			)
			.sortFunc_({true}) 
			.value_( [ 'View' ] )
			.action_({ |vw, value|
				vw.value = [ 'View' ];
				vw.tree.atPath( value ).value;
			});	
	}
	
}