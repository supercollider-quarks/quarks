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

UdefListView {
	
	classvar <>current;
	
	var <view, <views;
	var <collapsed;
	
	*new { |parent, bounds, makeCurrent = true|
		if( parent.isNil && { current.notNil && { current.view.isClosed.not } } ) {
			^current.rebuild.front;
		} {
			^super.new.init( parent, bounds ).makeCurrent( makeCurrent );
		};
	}
	
	makeCurrent { |bool| if( bool == true ) { current = this } }
	
	rebuild {
		var parent, cx = false;
		parent = view.parent;
		if( current == this ) { 
			cx = true;
		};
		parent.children.do(_.remove);
		this.init( parent );
		this.makeCurrent( cx );
	}
	
	*front {
		var parent;
		if( current.notNil && { current.view.isClosed.not } ) {
			current.front;
		} {
			^UdefListView( );
		};
	}
	
	collapseAll {
		views.do({ |item|
			if( item.class == ExpandView ) {
				item.collapse;
			};
		});
	}
	
	expandAll {
		views.do({ |item|
			if( item.class == ExpandView ) {
				item.expand;
			};
		});
	}
		
	
	front { view.findWindow.front }
	
	init { |parent, bounds|
		
		var categories, names, rackCategories, g;
		var scrollerMargin = 12;
		var refreshFunc;
		var controller;
		
		refreshFunc = ActionFunc( \delay, { { this.rebuild }.defer( 0.01 ); }, 0.1 );
		
		controller = SimpleController( Udef.all );
		
		controller.put( \added, { refreshFunc.value });
		
		if( GUI.id == \qt ) { scrollerMargin = 20 };
			
		collapsed = collapsed ?? { () };
		
		if( parent.notNil ) {
			bounds = bounds ?? { parent.bounds.moveTo(0,0).insetBy(4,4) };
		} {
			bounds = bounds ? 220@400;
		};
		
		view = EZCompositeView( parent, bounds ).resize_(5);
		bounds = view.bounds;
		view.onClose_({ 
			if( current == this ) { current = nil };
			controller.remove;
		});
		views = ();
		
		views[ \scrollview ] = ScrollView( view, view.bounds.moveTo(0,0) ).resize_(5);
		views[ \scrollview ].addFlowLayout;
		views[ \scrollview ].hasBorder = false;
		
		categories = [];
		
		Udef.all !? { |all| all.keys.asArray.sort.do({ |key|
                var category, index, udef;
                udef = all[ key ];
                category = udef.category;
                index = categories.indexOf( category );
                if( index.isNil ) {
                    categories = categories ++ [ category, [ udef ] ];
                } {
                    categories[ index + 1 ] = categories[ index + 1 ].add( udef );
                };
            })
		};

        rackCategories = [];

		UnitRack.all !? { |all| all.keys.asArray.sort.do({ |key|
                var category, index, rack;
                rack = all[ key ];
                category = rack.category;
                index = rackCategories.indexOf( category );
                if( index.isNil ) {
                    rackCategories = rackCategories ++ [ category, [ rack ] ];
                } {
                    rackCategories[ index + 1 ] = rackCategories[ index + 1 ].add( rack );
                };
            });
        };

		g = { |cat, udefs|
            if( cat !== \private ) {
			
            views[ cat ] = ExpandView( views[ \scrollview ],
                (bounds.width - (scrollerMargin+6))@( (udefs.size + 1) * 22 ),
                (bounds.width - (scrollerMargin+6))@18,
                collapsed[ cat ] ? true
            );
            
            views[ cat ].button.background = nil;

            views[ cat ].addFlowLayout( 0@0, 4@4 );

            StaticText( views[ cat ], (bounds.width - (18 + scrollerMargin + 6))@18 )
                .string_( " " ++ cat.asString )
                .applySkin( RoundView.skin ? () );

            udefs.do({ |udef|
	            var hasFile = false, wd = 36;
	            if( GUI.id == \qt ) { wd = 44 };
	            if( udef.isKindOf( GenericDef ) ) {
		           hasFile = udef.filePath.notNil;
		            if( hasFile ) { wd = 36 + 4 + scrollerMargin + 6 };
	            };
                DragSource( views[ cat ], (bounds.width - wd)@18 )
                    .object_( udef )
                    .string_( " " ++ udef.name.asString )
                    .applySkin( RoundView.skin ? () );
                  
                if( hasFile ) {
	               SmoothButton( views[ cat ], 18@18 )
	                	.label_( \document )
	                	.radius_(2)
	                	.border_(1)
	                	.resize_(3)
	                	.canFocus_( false )
	                	.action_({
		                	this.setEndFrontAction;
		                	udef.openDefFile 
		                });
                };
            });
            
            collapsed[ cat ] = views[ cat ].collapsed;
            
            views[ cat ]
            	.expandAction_({ collapsed[ cat ] = false })
            	.collapseAction_({ collapsed[ cat ] = true })
            	.hideOutside;
            
            };
        };

		RoundView.useWithSkin( UChainGUI.skin ++ (RoundView.skin ? ()), {
			var comp;
			
			comp = CompositeView( views[ \scrollview ], (bounds.width - 18)@14 );
			comp.addFlowLayout( 0@0, 4@0 );
			
			SmoothButton(comp, 50@14 )
				.label_([ "show all", "hide all" ])
				.hiliteColor_( Color.clear )
				.border_(1)
				.radius_(2)
				.canFocus_(false)
				.action_({ |bt|
					switch( bt.value, 
						1, { this.expandAll },
						0, { this.collapseAll }
					);
				});
				
			SmoothButton(comp, 50@14 )
				.label_( "refresh" )
				.border_(1)
				.radius_(2)
				.canFocus_(false)
				.action_({ { this.rebuild }.defer( 0.01 ) });
				
			SmoothButton(comp, 50@14 )
				.label_([ "load all" ])
				.hiliteColor_( Color.clear )
				.border_(1)
				.radius_(2)
				.canFocus_(false)
				.action_({ |bt|
					var defs;
					Udef.loadOnInit = false;
					defs = Udef.loadAllFromDefaultDirectory.collect(_.synthDef).flat.select(_.notNil);
					Udef.loadOnInit = true;
					ULib.servers.do({ |srv| 
						if( srv.class == LoadBalancer ) {
							if( srv.servers[0].isLocal ) {
								defs.do(_.justWriteDefFile); 
								srv.servers.do({ |sx|
									sx.loadDirectory( SynthDef.synthDefDir );
								});
							} {
								srv.servers.do{ |s|
									defs.do(_.send(s))
								};
							};
						} {
							if( srv.isLocal ) { 
								defs.do(_.justWriteDefFile); 
								srv.loadDirectory( SynthDef.synthDefDir ); 
							} {
								defs.do(_.send(srv)); 
							};
						};
					});
					UnitRack.loadAllFromDefaultDirectory;
				});
				
			StaticText(views[ \scrollview],100@25).string_("Udefs");
			views[ \scrollview].decorator.nextLine;
			categories = categories.clump(2).sort({ |a,b| a[0] <= b[0] }).flatten(1);
			categories.pairsDo(g);
			StaticText(views[ \scrollview],100@25).string_("UnitRacks");
            views[ \scrollview].decorator.nextLine;
            rackCategories = rackCategories.clump(2).sort({ |a,b| a[0] <= b[0] }).flatten(1);
            rackCategories.pairsDo(g);
		});
	}
	
	setEndFrontAction {
		 if( GUI.id == \cocoa ) {
			 view.findWindow.endFrontAction = nil;
		 };
	}	
}