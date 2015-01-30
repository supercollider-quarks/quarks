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



CollidingGUI{
        var serverBar, <>scopeBar, stethoscope;
        var volSlider,bootButton,killButton;
        var <win,<palette;
	    var <>tabView,initialTab,initialPatch;
	    var <tabs;
	    var bottomTabView, scopeTab, postTab;
	    var buttonPanel, newTabButton, helpButton, loadButton, saveButton, panicButton;
        var <>buf, bus, <>synth;
        var <>app;
        var <>lines;
        var <>kaput;
        var <>initDone = false;
        var <>controller;
	    var <texts,<sliders,<soundButtons;

	newTab{|id|
		tabs=tabs.add(CollidingTab.new(tabView,controller,id));
		^tabs.last;
	}

	removeFirst{
		tabs=tabs.drop(1);
		tabView.removeAt(0);
	}

    init{|anApp|
		app = anApp;
        GUI.qt;
        palette =  QPalette.dark;
        QtGUI.palette = palette;
		Font.default = Colliding.guiFont;

		buf = Buffer.alloc(app.server,1024,2);
		bus = Bus(\audio, 0, 2, app.server);
		synth = BusScopeSynth(app.server);

		controller = CollidingControl.new(app);
        win = Window.new("Colliding");
		win.bounds = Rect(0,0,1024,730);

		tabView = TabbedView2.newTall(win,Rect(10,10,900,530));
		initialTab = CollidingTab.new(tabView,controller,app.tabId);
		tabs = [initialTab];

		newTabButton = Button(win,Rect(740,10,50,25))
		    .action_({
			    if(tabView.tabViews.size<Colliding.max_tabs){
				this.newTab(app.tabId);
			    }
		     })
		     .states_([["＋"]]
		);

		helpButton = Button(win,Rect(795,10,50,25))
		.states_([["?"]])
		   .action_({
			    controller.helpButton(
				    tabs[tabView.activeTab.index]);
			}
		);

		loadButton = Button(win,Rect(850,10,50,25))
		.states_([["⇈"]])
		   .action_({
			    controller.loadProject;
			}
		);

		saveButton = Button(win,Rect(895,10,50,25))
		.states_([["⇊"]])
		   .action_({
			    controller.saveProject;
			}
		);

		panicButton = Button(win,Rect(950,10,50,25))
		.states_([["✕",Color.white,Color.red]])
		   .action_({
			  tabs.do({|t| t.track.stop;t.state=0});
			  app.server.freeAll;
			  this.updateScope;
			}
		);

		buttonPanel = CompositeView(win, Rect(940, 40, 80, 500));
		soundButtons = Array.fill(8,{|i|
			[
				Button(buttonPanel,Rect(0,i*63,60,60))
				.states_([[i]])
				.action_({this.openBufferView(i)})
			]

		});

		scopeBar = QScope2(win,Rect(10,560,990,150));
		scopeBar.bufnum = buf.bufnum;
        scopeBar.server = app.server;
        scopeBar.canFocus = true;
		scopeBar.focus;
		win.front;
        CmdPeriod.add({this.free});
    }

	makeScopeDef{
        SynthDef(\collidingscope, { |in, bufnum|
                    ScopeOut2.ar(In.ar(in, 2), bufnum);
		}).send(app.server);
    }

    openBufferView{|bufnum|
		var bufferView = CollidingBufferGUI.new(bufnum,app);
	}

    free{
        if(this.synth.isPlaying) {  synth.free };
        synth = nil;
    }

    updateScope{
            if(synth.isPlaying.not,{this.runScope});
    }

	runScope {
         synth=Synth(\collidingscope,[\bufnum, buf.bufnum, \in,0],RootNode(app.server),\addToTail);
		 scopeBar.start;
         synth.isPlaying = true;
    }
}
