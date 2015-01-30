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



CollidingBufferGUI{
	var window, sfv, sound, bufNum, app;
	var soundButton, wtButton, fsButton, playButton,stopButton,allocNum,allocButton;
	var infoView;
	var playEvt;
	*new{|buf,app|
		^super.new.init(buf,app);
	}
	init{|aBuf,anApp|
		app = anApp;
		bufNum = aBuf;
		this.makeWindow;
		if (app.sounds[bufNum].notNil){this.loadSFView};
	}
	makeWindow{
		var y = Window.screenBounds.height - 120;
		window = Window("", Rect(200, y, 840, 300));
		sfv = SoundFileView(window, Rect(20,20, 800, 220)).gridOn_(false).waveColors_([Color.white,Color.white]);
		soundButton = Button(window,Rect(20,250,90,40))
		   .states_([["load sound"]]).action_({this.openFileDialog;});

		wtButton = Button(window,Rect(120,250,90,40))
		   .states_([["to wavetable"]]).action_({this.toWavetable;});

		fsButton = Button(window,Rect(220,250,90,40))
		   .states_([["freesound"]]).action_({this.fromFreesound});

		playButton = Button(window,Rect(320,250,90,40))
		   .states_([["play"]])
		   .action_({|but|
			if(playEvt.notNil){playEvt.stop};
			    playEvt=app.sounds[bufNum].cue((server:Server.internal),playNow:true);
		    });
		stopButton = Button(window,Rect(420,250,90,40))
		   .states_([["stop"]])
		   .action_({|but|
			if(playEvt.notNil){
				playEvt.stop;
			};
		    });

		allocNum = NumberBox(window, Rect(520, 250, 100, 40)).value_(44100*2);
		allocButton = Button(window,Rect(620,250,90,40))
		   .states_([["alloc"]])
		   .action_({|but|
			app.buffers[bufNum] =
			Buffer.alloc(app.server,allocNum.value,1,bufnum:bufNum);

		    });
		window.front;
	}

	loadSFView{
		sfv.soundfile = app.sounds[bufNum];
		sfv.read(0, app.sounds[bufNum].numFrames);
		sfv.refresh;
	}
	toWavetable{
	    var sig = Signal.newClear(1024);
		app.sounds[bufNum].readData(sig);
		sig=sig.resamp1(sig.size.nextPowerOfTwo).as(Signal);
		app.buffers[bufNum]=Buffer.alloc(app.server,
			sig.asWavetable.size,bufnum:bufNum);
		app.buffers[bufNum].loadCollection(sig.asWavetable);
		sfv.setData(sig);
	}
	openFileDialog{
		Dialog.openPanel({|path|
			app.loadBuffer(bufNum,path, {defer{this.loadSFView;}});
		});
	}
	fromFreesound{
		var soundList,results=[],fileNames=[];
		var y = Window.screenBounds.height - 120;
		var w = Window("", Rect(300, y, 400, 600));
		var searchBox = TextField(w, Rect(10, 10, 250, 30));
		var searchButton =  Button(w,Rect(260, 10, 50, 30)).states_([["search"]]);
		var loadButton =  Button(w,Rect(10, 550, 300, 30)).states_([["load"]]);

		searchButton.action_({
			soundList.clear;
			results=[];
			fileNames=[];
			FS2Sound.search(q:searchBox.string,f:"type:wav",action:{|p|
				p.do({|snd|
					fileNames=fileNames.add(snd.original_filename);
					results = results.add(snd);
				});
				soundList.items_(fileNames);
			});
			});

			soundList = ListView(w,Rect(10, 50, 300, 500)).action_({|list|
				//results[list.value].preview; TODO

			});

		   loadButton.action_({|b|
				var i = soundList.value;
			    var path = Platform.defaultTempDir++fileNames[i];
				results[i].retrieve(Platform.defaultTempDir,
				{
					app.buffers[bufNum] =
				         Buffer.read(app.server,path,bufnum:bufNum,action:{
						  defer{
						  app.sounds[bufNum] = SoundFile.openRead(path);
						  this.loadSFView;}
					});
				});
			});

		w.front;
	}
}
