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

SyncCenterGui {
	var <window, <widgets;
	
	*new {
		^super.new.init;
	}
	
	init {
		var font = Font( Font.defaultSansFace, 11 ), masterText;		
		widgets = List.new;
		
		if( window.notNil ){
			if( window.isClosed.not) {
				widgets.do(_.remove);
				window.close
			};
			window = nil;
		};
		window = Window("Sync Center",Rect(300,300,400,400)).front;
		window.addFlowLayout;
		
		SmoothButton( window, 100@20  )
 			.states_( [[ "Sync", Color.black, Color.clear ]] )
 			.canFocus_(false)
			.radius_( 0 )
			.border_(1)
			.font_( Font( font.name, 10 ).boldVariant )
			.action_({ |bt|
				SyncCenter.remoteSync;
				bt.enabled = false;
				{ bt.enabled = true }.defer(1.1);	
			});
		
		widgets.add(SyncCenterStatusWidget(window,20));

		window.view.decorator.nextLine;
		
		SyncCenter.serverCounts.keys.as(Array).sort({ |a,b| a.name <= b.name }).do{ |server,i|
			var text, uv;
			text = StaticText(window,100@20).string_(server.name++":" );
			widgets.add(SyncCenterServerWidget(window,130@17,server));
						
			window.view.decorator.nextLine;
		};
		
		window.onClose_({ this.remove });
			
	}
	
	remove {
	}
}

SyncCenterStatusWidget{
	var <controller, <view;
	var <>red, <>green;
	
	*new{ |parent, size = 20|
		^super.new.init(parent,size)
	}
	
	init{ |parent,size|
		red = Color.red(0.7);
		green = Color.green(0.7);
		
		view = UserView(parent,size@size);
		this.update;
				
		SyncCenter.ready.addDependant(this);
		
		view.onClose_({ SyncCenter.ready.removeDependant(this); });
	}
	
	update {
		 {
			view.background_( if( SyncCenter.ready.value ) { green } { red } )
		}.defer
	}
	
	remove{
		 SyncCenter.ready.removeDependant(this); 
	}
}

SyncCenterServerWidget{
	var <controller, <view, <difView, <remoteCount;
	var <>red, <>green;
	
	*new{ |parent, bounds, server, small = false|
		^super.new.init(parent,bounds,server, small)
	}
	
	init{ |parent,bounds,server, small = false| 
		var width, height;
		red = Color.red(0.7);
		green = Color.green(0.7);
		
		this.remove;
		remoteCount = SyncCenter.serverCounts.at(server);
		
		width = bounds.asRect.width;
		height = bounds.asRect.height;
		
		if( small ) {
			green = Color.white.alpha_(0.5);
		} {
			view = SmoothNumberBox( parent, (width / 2) @ height );
			width = width / 2;
			view.font = Font( Font.defaultSansFace, 10 );
		};
	
		difView = SmoothNumberBox( parent, width @ height );
		difView.font = Font( Font.defaultSansFace, 10 );
		this.update;
			
		remoteCount.addDependant(this); // becomes a controller
		difView.onClose_( { remoteCount.removeDependant(this); } );
	}
	
	update { 
		if( view.notNil ) {
			view.value_(remoteCount.value);
			view.background_( if( remoteCount.value != -1 ) { green } { red } );
		} {
			difView.background_( if( remoteCount.value != -1 ) { green } { red } );
		};
		if( remoteCount.value != -1 ) { 
			difView.value = remoteCount.value - 
				SyncCenter.serverCounts[ SyncCenter.master ].value;
		};
	}
	
	remove{
		remoteCount.removeDependant(this);
	}
}

	
		