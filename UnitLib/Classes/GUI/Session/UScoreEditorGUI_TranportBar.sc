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

UScoreEditorGui_TransportBar {
    var <scoreView;
    var <>views, <>scoreController, <scoreViewController;
    
    *initClass {
	    StartUp.defer({			
			DrawIcon.drawFuncs.put( \lock, { |rect|
				var size = rect.width.min( rect.height ) * 0.8;
				var radius = size/6;
				var corners = [ -1 @ 0, -1 @ -2, 1 @ -2, 1 @ 0 ] * radius;
				
				Pen.use({
					
					Pen.translate( *rect.center.asArray );
					
					Pen.fillRect( Rect( size.neg * 0.25,0, size / 2, size / 3 ) );
					
					2.do({
						Pen.moveTo( corners[0] )
							.arcTo( corners[1], corners[2], radius )
							.arcTo( corners[2], corners[3], radius )
							.lineTo( corners[3] );
						corners = corners.reverse * [0.5@0.75];
						radius = radius / 2;
					});
					Pen.fill;
			
					});
				}
			).put( \unlock, { |rect|
				var size = rect.width.min( rect.height ) * 0.8;
				var radius = size/6;
				var corners = [ 0 @ 0, 0 @ -2, 2 @ -2, 2 @ 0 ] * radius;
				
				Pen.use({
					Pen.translate( *rect.center.asArray );
					
					Pen.fillRect( Rect( (size.neg * 0.25) - (radius/2),0, size / 2, size / 3 ) );
					
					2.do({
						Pen.moveTo( corners[0] )
							.arcTo( corners[1], corners[2], radius )
							.arcTo( corners[2], corners[3], radius )
							.lineTo( corners[3] );
						corners = (corners.reverse + [ radius@0 ]) * [0.5@0.75];
						radius = radius / 2;
					});
					Pen.fill;
					
					});
				}
			);
		});
    }

    *new{ |parent, bounds, scoreView|
        ^super.newCopyArgs(scoreView).init(parent, bounds)
    }

    init{ |parent, bounds|
        this.makeGui(parent, bounds);
        scoreViewController = SimpleController( scoreView );
        scoreViewController.put(\activeScoreChanged, {
		    this.addControllers;
		});
        this.addControllers;
    }

    score{
        ^scoreView.currentScore
    }

    addControllers{
        if(scoreController.notNil) {
            scoreController.remove;
        };
        scoreController = SimpleController( this.score );

		scoreController.put(\playState,{ |a,b,newState,oldState|
		    //[newState,oldState].postln;
		    if( newState == \playing )  {
		        views[\play].value = 1;
		        views[\pause].value = 0;
		        { views[\prepare].stop }.defer
		    };
		    if(newState == \stopped ) {
		        { views[\prepare].stop; }.defer;
                views[\pause].value = 0;
                views[\play].value = 0;
		    };
		    if( newState == \preparing  ) {
		        {Êviews[\prepare].start }.defer;
                views[\pause].value = 2;
		    };
		    //resuming
		    if( (newState == \playing) && (oldState == \paused) ) {
		        views[\pause].value = 0;
		    };
		    if( newState == \prepared ) {

                {Êviews[\prepare].stop }.defer;
                views[\play].value = 2;
		    };
		    if( newState == \paused ) {
                views[\pause].value = 1;
		    };

		});

		scoreController.put(\updatePos, { |who,what,updatePos|
		    views[\update].value = updatePos.binaryValue;
		});

		scoreController.put(\pos, { |who,what|
            views[\counter].value = this.score.pos;
            views[\barMap ].value = this.score.pos;
            views[\tempo ].value = this.score.tempoMap.bpmAtTime( this.score.pos ).round(0.001);
            views[\signature ].value = this.score.tempoMap.signatureAtTime( this.score.pos ) 
		});
		
		scoreController.put(\loop, { |who,what|
            views[\loop].value = this.score.loop.binaryValue; 
		});

		views[\play].value = this.score.isPlaying.binaryValue;
		views[\pause].value = this.score.isPaused.binaryValue;
		if(this.score.isPreparing) {
		    { views[\prepare].start }.defer
		} {
		    { views[\prepare].stop }.defer
		}


    }

    remove {
        [scoreController,scoreViewController].do(_.remove)
    }

    makeGui{ |parent, bounds|

        var font = Font( Font.defaultSansFace, 11 ), view, size, marginH, marginV, playAlt;
		views = ();

		marginH = 2;
	    marginV = 2;
		size = bounds.height - (2*marginV);
        view = CompositeView( parent, bounds );
        
        RoundView.pushSkin( UChainGUI.skin );

		view.addFlowLayout(marginH@marginV);
		//view.background_( Color.white );
		view.resize_(8);



        views[\prepare] = WaitView( view, size@size )
					.alphaWhenStopped_( 0 )
					.canFocus_(false);

		views[\play] = SmoothSimpleButton( view, 40@size  )
			.states_( [
			    [ \play, Color.black, Color.clear ],
			    [ \stop, Color.black, Color.green.alpha_(0.5) ],
			    [ \play, Color.blue, Color.red.alpha_(0.5) ]] )
			.canFocus_(false)
			.font_( font )
			//.changeStateWhenPressed_(false)
			.action_({  |v,c,d,e|

			    var startedPlaying;
			    switch( v.value )
			        {0} {
                        this.score.prepareAndStart( ULib.servers, this.score.pos, true);
			        }{1} {
                        this.score.stop;
                    }{2} {
                        this.score.start( ULib.servers, this.score.pos, true);
                    }

			});
			
		views[\pause] = SmoothSimpleButton( view, 50@size  )
			.states_( [
			    [ \pause, Color.black, Color.clear ],
			    [ \pause, Color.red,Color.green.alpha_(0.5) ],
			    [ \pause, Color.blue,Color.red.alpha_(0.5) ]] )
			.canFocus_(false)
			.font_( font )
			.action_({ |v|
			    switch( v.value)
			    {0}{
			        if(this.score.isPlaying) {
			            this.score.pause;
			       } {
			            this.score.prepare( ULib.servers, this.score.pos );
			       }
			    }{1} {
			        this.score.resume(ULib.servers);
			    }{2}{
			        this.score.stop;
			    }
			});

		views[\return] = SmoothButton( view, 30@size  )
			.states_( [[\return, Color.black, Color.clear ]])
			.canFocus_(false)
			.radius_( [ size/2, 0, 0, size/2 ] )
			.font_( font )
			.action_({
			    this.score.toPrevMarker;
			});
			
		views[\next] = SmoothButton( view, 30@size  )
			.states_( [[\skip, Color.black, Color.clear ]])
			.canFocus_(false)
			.radius_( [ 0, size/2, size/2, 0 ] )
			.font_( font )
			.action_({
			    this.score.toNextMarker;
			});

        views[\loop] = SmoothButton( view, 50@size  )
        			.states_( [[\roundArrow, Color.black, Color.clear ],
        			[\roundArrow, Color.black, Color.green.alpha_(0.5) ]])
        			.value_( this.score.loop.binaryValue )
        			.canFocus_(false)
        			.font_( font )
        			.action_({ |v| this.score.loop = v.value.booleanValue;  });

        view.decorator.shift(20,0);

	    views[\counter] = SMPTEBox( view, 110@size )
			.value_( this.score.pos )
			.radius_( 12 )
			.align_( \center )
			.clipLo_(0)
			.applySmoothSkin
			.border_(0.000001)
			.charSelectColor_( Color.white.alpha_(0.5) )
			.autoScale_( true )
			.visible_( scoreView.showTempoMap.not )
            .action_({ |v|
                if(this.score.isStopped) {
                    this.score.pos = v.value
                }
            });
            
        view.decorator.shift( -114, 0 );
        
        views[\barMap] = TempoBarMapView( view, 110@size, this.score.tempoMap )
			.value_( this.score.pos )
			.clipLo_(0)
			.visible_( scoreView.showTempoMap )
			.autoScale_( true )
			.action_({ |v|
				if(this.score.isStopped) {
					this.score.pos = v.value;
				};
			});
            
	   views[\timeMode ] = PopUpMenu( view, 50@size )
			.items_( [ "time", "bar" ] )
			.canFocus_(false)
			.font_( font )
			.value_( scoreView.showTempoMap.binaryValue )
			.action_({ |v|
				scoreView.showTempoMap = v.value.booleanValue;
				views[\signature].visible = scoreView.showTempoMap;
				views[\tempo].visible = scoreView.showTempoMap;
				views[\editTempo].visible = scoreView.showTempoMap;
				views[\lockToTempo].visible = scoreView.showTempoMap;
				views[\barMap].visible = scoreView.showTempoMap;
				views[\counter].visible = scoreView.showTempoMap.not;
			});
			
		views[\signature] = SignatureBox( view,35@size )
			.applySmoothSkin
			.visible_( scoreView.showTempoMap )
			.autoScale_(true)
			.align_( \center )
			.value_( this.score.tempoMap.signatureAtTime( this.score.pos ) )
			.action_({ |vw|
				this.score.tempoMap.setSignatureAtTime( vw.value, this.score.pos );
			});
			
		views[\tempo] = SmoothNumberBox( view,40@size )
			.visible_( scoreView.showTempoMap )
			.clipLo_(1)
			.autoScale_(true)
			.value_( this.score.tempoMap.bpmAtTime( this.score.pos ).round(0.001) )
			.action_({ |vw|
				var beats;
				if(this.score.isStopped && (views[ \lockToTempo ].value == 1) ) {
					beats = this.score.startBeats;
					this.score.tempoMap.setBPMAtBeat( vw.value, 
						this.score.tempoMap.beatAtTime( this.score.pos )
					);
					this.score.startBeats = beats;
				} {
					this.score.tempoMap.setBPMAtBeat( vw.value, 
						this.score.tempoMap.beatAtTime( this.score.pos )
					);
				};
			});
			
		view.decorator.shift( -16, 0 );
			
		views[\lockToTempo ] = SmoothButton( view,12@12 )
			.label_( [ 'unlock', 'lock' ] )
			.states_( [ [ 'unlock' ], [ 'lock', Color.red ] ] )
			.radius_( 4 )
			.hiliteColor_(nil)
			.canFocus_( false )
			.background_( nil )
			.border_( 0 )
			.visible_( scoreView.showTempoMap );
			
		views[\editTempo ] = SmoothButton( view, 28@size )
            	.radius_( bounds.height / 8 )
			.label_( "edit" )
			.canFocus_(false)
			.visible_( scoreView.showTempoMap )
			.action_({ |b|
				views[\tempoEditor ] = UScoreTempoMapGUI( score: scoreView.currentScore );
			});
	    
        view.decorator.shift( view.decorator.indentedRemaining.width - 121, 0 );
        
        views[\follow] = SmoothButton( view, 39@size )
            .label_( [ "follow", "follow" ] )
            .hiliteColor_( Color.green.alpha_(0.5) )
            .value_( scoreView.followPos.binaryValue )
            .radius_( bounds.height / 8 )
            .action_({ |bt| scoreView.followPos = bt.value.booleanValue; })
            .resize_(3)
            .canFocus_(false);
        
        views[\update] = SmoothButton( view, 39@size )
            .label_( [ "update", "update" ] )
            .hiliteColor_( Color.green.alpha_(0.5) )
            .value_(1)
            .radius_( bounds.height / 8 )
            .action_({ |bt| this.score.updatePos = bt.value.booleanValue; })
            .resize_(3)
            .canFocus_(false);
        
        views[\osc] = SmoothButton( view, 28@size )
            .label_( [ "osc", "osc" ] )
            .hiliteColor_( Color.green.alpha_(0.5) )
            .value_(this.score.oscSetter.notNil.binaryValue )
            .radius_( bounds.height / 8 )
            .action_({ |bt| switch( bt.value.asInt,
	            	1, { this.score.enableOSC },
	            	0, {  this.score.disableOSC }
	            )
            })
            .resize_(3)
            .canFocus_(false);
        
        RoundView.popSkin;
        
        view.onClose_({ views[\tempoEditor ] !? _.close })

    }

}