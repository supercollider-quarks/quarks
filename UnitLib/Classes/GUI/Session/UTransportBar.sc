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

/*UTransportWindow {
    var <transporView;

    *new{ |score, parent, bounds|
        ^super.init(score, parent, bounds)
    }

    init{ |parent, bounds|
        this.makeGui(parent, bounds);
        this.addControllers;
        
		        view = parent ?? { Window.new("UTranportBar",bounds).front };
                        view = CompositeView( parent, bounds.moveTo(0,0) );
                        		view.addFlowLayout(marginH@marginV);
		//view.background_( Color.white );
		view.resize_(5);
		        height = bounds.height - (2*marginV);
		
		        
        
    }
*/

UTransportView {
    var <score;
    var <>views, <>scoreController, <scoreViewController;

    *new{ |score, parent, height|
        ^super.newCopyArgs(score).init(parent, height)
    }

    init{ |parent, height|
        this.makeGui(parent, height);
        this.addControllers;
    }

    addControllers{
        if(scoreController.notNil) {
            scoreController.remove;
        };
        scoreController = SimpleController( score );

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
		        { views[\prepare].start }.defer;
                views[\pause].value = 2;
		    };
		    //resuming
		    if( (newState == \playing) && (oldState == \paused) ) {
		        views[\pause].value = 0;
		    };
		    if( newState == \prepared ) {

                { views[\prepare].stop }.defer;
                views[\play].value = 2;
		    };
		    if( newState == \paused ) {
                views[\pause].value = 1;
		    };

		});

		scoreController.put(\paused, {
            views[\pause].value = 1;
		});

		scoreController.put(\start, {
            views[\play].value = 1;
		});

		scoreController.put(\pos, { |who,what,pos|
            views[\counter].value = score.pos;
		});

		views[\play].value = score.isPlaying.binaryValue;
		views[\pause].value = score.isPaused.binaryValue;
		if(score.isPreparing) {
		    { views[\prepare].start }.defer
		} {
		    { views[\prepare].stop }.defer
		}


    }

    remove {
        scoreController.remove
    }

    makeGui{ |parent, height|

        var font = Font( Font.defaultSansFace, 11 );
		views = ();

		views[\play] = SmoothSimpleButton( parent, 40@height  )
			.states_( [
			    [ \play, Color.black, Color.clear ],
			    [ \stop, Color.black, Color.green.alpha_(0.5) ],
			    [ \play, Color.blue, Color.red.alpha_(0.5) ]] )
			.canFocus_(false)
			.font_( font )
			.border_(1).background_(Color.grey(0.8))
			//.changeStateWhenPressed_(false)
			.action_({  |v,c,d,e|

			    var startedPlaying;
			    switch( v.value )
			        {0} {
                        score.prepareAndStart( ULib.servers, score.pos, true);
			        }{1} {
                        score.stop;
                    }{2} {
                        score.start( ULib.servers, score.pos, true);
                    }

			});
			
		views[\pause] = SmoothSimpleButton( parent, 50@height  )
			.states_( [
			    [ \pause, Color.black, Color.clear ],
			    [ \pause, Color.red,Color.green.alpha_(0.5) ],
			    [ \pause, Color.blue,Color.red.alpha_(0.5) ]] )
			.canFocus_(false)
			.font_( font )
			.border_(1)
			.background_(Color.grey(0.8))
			.action_({ |v|
			    switch( v.value)
			    {0}{
			        if(score.isPlaying) {
			            score.pause;
			       } {
			            score.prepare;
			       }
			    }{1} {
			        score.resume(ULib.servers);
			    }{2}{
			        score.stop;
			    }
			});

		views[\return] = SmoothButton( parent, 50@height  )
			.states_( [[\return, Color.black, Color.clear ]])
			.canFocus_(false)
			.font_( font )
			.border_(1).background_(Color.grey(0.8))
			.action_({
			    score.pos = 0;
			});

        views[\loop] = SmoothButton( parent, 50@height  )
        			.states_( [[\roundArrow, Color.black, Color.clear ],
        			[\roundArrow, Color.black, Color.green.alpha_(0.5) ]])
        			.canFocus_(false)
        			.font_( font )
        			.border_(1)
        			.background_(Color.grey(0.8))
        			.action_({ |v| score.loop = v.value.booleanValue;  });

        views[\prepare] = WaitView( parent, height@height )
					.alphaWhenStopped_( 0 )
					.canFocus_(false);

        parent.decorator.shift(20,0);

	    views[\counter] = SMPTEBox( parent, 150@height )
			.value_( score.pos )
			.radius_( 12 )
			.align_( \center )
			.clipLo_(0)
			.background_( Color.clear )
			.charSelectColor_( Color.white.alpha_(0.5) )
			.autoScale_( true )
            .action_({ |v|
                if(score.isStopped) {
                    score.pos = v.value
                }
            });

    }

}