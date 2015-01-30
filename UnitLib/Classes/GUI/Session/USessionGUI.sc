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

USessionGUI : UAbstractWindow {

	var <session;
    var <sessionView, bounds;
    var <sessionController, objGuis;
    var <selectedObject;

    *new { |session, bounds|
        ^super.new.init( session)
			.addToAll
			.makeGui(bounds)
	}

	init { |inSession|
	    session = inSession;
	    sessionController = SimpleController(session);
	    sessionController.put(\objectsChanged,{
	        { this.makeSessionView }.defer(0.05);
	    });
	    sessionController.put(\name,{
            window.name = this.windowTitle
        });

	}

    windowTitle {
        ^("Session Editor : "++this.session.name)
    }

    remove {
        sessionController.remove;
        objGuis.do(_.remove)
    }

	makeGui { |bounds|
        var topBarView;
		var font = Font( Font.defaultSansFace, 11 );

        var topBarHeigth = 40;
        var size = 16;
        var margin = 4;
        var gap = 2;
        bounds = bounds ? Rect(100,100,700,400);
        this.newWindow(bounds, "USession - "++session.name, {

            if(USessionGUI.current == this) {
                USessionGUI.current = nil
            };
            this.remove;
            {
                if( (this.session.objects.size != 0) && (this.session.isDirty) ) {
                    SCAlert( "Do you want to save your session? (" ++ this.session.name ++ ")" ,
                        [ [ "Don't save" ], [ "Cancel" ], [ "Save" ],[ "Save as"] ],
                        [ 	nil,
                            { USessionGUI(session) },
                            { this.session.save(nil, {USessionGUI(session)} ) },
                            { this.session.saveAs(nil,nil, {USessionGUI(session)} ) }
                        ] );
                };
            }.defer(0.1)
        }, margin:0, gap:0);
        topBarView =  CompositeView(view, Rect(0,0,bounds.width,topBarHeigth)).resize_(2);
        topBarView.addFlowLayout;

        SmoothButton( topBarView, 40@size  )
			.states_( [
			    [ \play, Color.black, Color.clear ]] )
			.canFocus_(false)
			.font_( font )
			.border_(1).background_(Color.grey(0.8))
			.action_({
			    session.prepareAndStart
			});

		SmoothButton( topBarView, 40@size  )
			.states_( [
			    [ \stop, Color.black, Color.clear ]] )
			.canFocus_(false)
			.font_( font )
			.border_(1).background_(Color.grey(0.8))
			.action_({
			    session.release
			});

		topBarView.decorator.shift(10);

        SmoothButton( topBarView, size@size )
            .states_( [[ '-' ]] )
            .canFocus_(false)
            .border_(1).background_(Color.grey(0.8))
            .action_({
                selectedObject !? this.removeObject(_)
            });

		topBarView.decorator.nextLine;

		CompositeView( topBarView, Rect( 0, 14, (topBarView.bounds.width - (margin * 2)), 2 ) )
        	.background_( Color.black.alpha_(0.25) )
        	.resize_(2);

        this.makeSessionView;

    }

    removeObject { |object|
        if(object.class != UScore){
            session.remove(object);
        } {
            if( (object.events.size != 0) && (object.isDirty) ) {
                SCAlert( "Do you want to save your score? (" ++ object.name ++ ")" ,
                [ [ "Don't save" ], [ "Cancel" ], [ "Save" ],[ "Save as"] ],
                [ 	{session.remove(object)},
                    nil,
                    { object.save({session.remove(object)}) },
                    { object.saveAs(nil,{session.remove(object)}) }
                ] );

            }
        }
    }

    makeSessionView {
        var addLast;
        var topBarHeigth = 40;
        var margin = 4;
        var gap = 2;
        var sessionViewsHeight = 16;
        var font = Font( Font.defaultSansFace, 11 );
        var bounds = view.bounds.moveTo(0,0);

        //first remove old view and controllers;
        if(sessionView.notNil) {
            sessionView.remove;
        };
        objGuis.do(_.remove);

        sessionView = CompositeView(view, Rect(0,topBarHeigth,bounds.width,bounds.height - topBarHeigth)).resize_(5);
        sessionView.addFlowLayout(margin@margin,margin@margin);

        objGuis = session.objects.collect { |object,i|
            var releaseTask, but, ctl, comp, gui;

            comp = ActiveCompositeView( sessionView, (sessionView.bounds.width - (margin*2))@(sessionViewsHeight + (margin*2)) )
            		.resize_(2)
            		.background_(Color.grey(0.9));
            comp.mouseDownAction_({ selectedObject = object });
            comp.uview.beginDragAction_({ object });
            comp.canReceiveDragHandler_({ |sink|
                [ UChain, UScore, Array ].includes(View.currentDrag.class)
            })
            .receiveDragHandler_({ |sink, x, y|
                session.insertCollection(session.objects.indexOf(object)+1 ,View.currentDrag.asCollection.collect(_.deepCopy))
            });

            comp.addFlowLayout;

            StaticText(comp,150@16)
                .string_(object.class.asString++" "++object.name);

            SmoothButton(comp,25@16)
                .states_([[\up,Color.black,Color.clear]])
                .font_( font )
			    .border_(1).background_(Color.grey(0.8))
			    .radius_(5)
			    .canFocus_(false)
			    .action_({
                    object.gui
			    });

			comp.decorator.shift(22,0);
			gui = object.sessionGUI(comp);
			sessionView.decorator.nextLine;
			gui
        };

        addLast = UserView( sessionView, (sessionView.bounds.width - (margin*2))@14 )
            .resize_(2)
            .canFocus_(false)
            .canReceiveDragHandler_({ |sink|
                var drg;
                drg = View.currentDrag;
                USession.acceptedClasses.includes(drg.class)
            })
            .receiveDragHandler_({ |sink, x, y|
                session.add(View.currentDrag.deepCopy)
            });
        window.refresh;
    }

}

UChainSessionView {
    var object;
    var ctl;

    *new { |object,view|
        ^super.newCopyArgs(object).init(view)
    }

    remove {
        ctl.remove;
    }

    init { |view|
        var button;
        var font = Font( Font.defaultSansFace, 11 );
        button = SmoothButton( view, 40@16  )
            .label_( ['power','power'] )
            .hiliteColor_( Color.green.alpha_(0.5) )
            .canFocus_(false)
            .font_( font )
            .border_(1).background_(Color.grey(0.8))
            .action_( [ {
                object.prepareAndStart;
            }, {
                object.release
            } ]
            );

        if( object.groups.size > 0 ) {
            button.value = 1;
        };

        ctl = SimpleController(object)
            .put( \start, { button.value = 1 } )
            .put( \end, {
                if( object.units.every({ |unit| unit.synths.size == 0 }) ) {
                    button.value = 0;
                };
            } )
    }

}

/*DragCompositeView : SCCompositeView {
    var <object;

    *new{ |parent,bounds,object|
        ^super.new(parent,bounds).initDrag(object)
    }

    initDrag { |inObject|
        object = inObject
    }

    defaultGetDrag {
        ^object
    }

}*/

UChainGroupSessionView {
    var object;

    *new { |object,view|
        ^super.newCopyArgs(object).init(view)
    }

    init { |view|
        var button;
        var font = Font( Font.defaultSansFace, 11 );
        button = SmoothButton( view, 40@16  )
            .label_( ['power','power'] )
            .hiliteColor_( Color.green.alpha_(0.5) )
            .canFocus_(false)
            .font_( font )
            .border_(1).background_(Color.grey(0.8))
            .action_( [ {
                object.prepareAndStart;
            }, {
                object.release
            } ]
            );

        if( object.groups.size > 0 ) {
            button.value = 1;
        };
    }
    
    remove{ }

}

+ UScore {
    sessionGUI { |view|
        ^UTransportView(this, view, 16)
    }
}

+ UChain {
    sessionGUI { |view|
        ^UChainSessionView(this,view)
    }
}

+ UChainGroup {
    sessionGUI { |view|
        ^UChainGroupSessionView(this,view)
    }
}

+ UScoreList {
    sessionGUI { |view|
        ^UTransportView(this.metaScore, view, 16);
    }
}