/*
Colliding, a simple SuperCollider environment for learning and live coding
(c) Gerard Roma, 2013-2014

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/


CollidingTab{
	var <tab,<id;
	var <state=0,<prevState=0;
	var <textView,<volumeSlider;
	var <track;
	var compileButton, playButton, stopButton, qwertyButton;
	var msgBox;

	*new{|tabView,control,tabId|
		^super.new.init(tabView,control,tabId);
	}
	state_{|aState|
		prevState = state;
		switch(aState)
		    {0}{tab.label=" "++id++" "} // saved
		    {1}{tab.label="(("++id++"))"} // playing
		    {2}{tab.label="~"++id++"~"}; // unsaved
		state = aState;
		tab.focus;
	}

	save{|path|
		var filePath = path++id++".cld";
		var file;
		filePath.postln;
		file= File(filePath,"w");
		file.write(textView.string);
		file.close;
	}

	init{|tabView,control,tabId|
		id =tabId;
		track = CollidingTrack.new;
		tab = tabView.add(" "++id++" ").closable_(true)
		.onRemove_({|t|
			track.stop;
			control.removeTab(this);
		});
		tab.focus;
		tab.flow({|w|
			textView=TextView(w,Rect(10,10,840,460))
			.font_(Colliding.textFont)
			.keyDownAction_{|v,c,m,u,k|
			      control.tabKeyDown(this,v,c,m,u,k);
			};
			volumeSlider = Slider(w,Rect(800,0,50,460)).action_({|v|
				control.trackVolume(this,v);
			}).value_(1);
		    w.startRow;

			compileButton = Button(w,Rect(905,10,50,30))
			    .states_([["✓"]])
		        .action_({
				     control.compile(this,textView,false);
			      })
			     .font_(Colliding.symbolFont);

			playButton =Button(w,Rect(905,60,50,30))
		        .states_([["▸"]])
		        .action_({
				control.compile(this,textView,true);
			    }).font_(Colliding.symbolFont);
			stopButton =Button(w,Rect(905,110,50,30))
		        .states_([["■"]])
		        .action_({
				    control.stopTrack(this,textView);
			    }).font_(Colliding.symbolFont);
		    qwertyButton = Button(w,Rect(905,160,50,30))
			    .states_([["⌨"]])
		        .action_({
				    control.qwertyButton(this);
			     }).font_(Colliding.symbolFont);

			msgBox = StaticText(w,Rect(905,220,620,40))
			.stringColor_(Color.black)
			.background_(Color.gray);
		});

		this.post("ready");
	}
	post{|str|
		msgBox.string_(str);
	}
}
