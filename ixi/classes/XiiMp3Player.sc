XiiMp3Player{
	// using mpg123
	// http://www.mpg123.de/

	*new {
		^super.new.initXiiMp3Player;
		}
		
	initXiiMp3Player {
		
		var win, listview, files, jpg, mp3files, blackjpg;
		var displayFiles, filepaths, activepid, playFlag, playlists;
		
		playFlag = false;
		files = [];
		blackjpg = "/Users/thor/Library/Application Support/SuperCollider/mp3black.jpg";
		
		playlists = Object.readArchive("mp3playlists.ixi");
		if(playlists.isNil, { playlists = () });
		
		win = Window.new("ixi mp3 player", Rect(0, 600, 320, 270), resizable: true);
		win.view.background = Color.white;
		
		listview = ListView.new(win, Rect(10,10, 300, 240))
			.items_(files)
			.background_(Color.black)
			.hiliteColor_(Color.white) //Color.new255(155, 205, 155)
			.selectedStringColor_(Color.black)
			.stringColor_(Color.white)
			.focus(true)
			.focusColor_(Color.black.alpha_(0.0))	
			.resize_(5)
			.keyDownAction_({arg view, char, mod, uni, key;  
				var viewer, string, command;
				[\mod, mod].postln;
				if(key==51, { // back arrow - stop playing
					("kill"+activepid.asString).unixCmd;
					playFlag = false;
				});
				if(key==36, { // Return button - play selected file
					string = "";
					if(playFlag, {
						("kill"+activepid.asString).unixCmd;
					});
					if(view.items.size > 0, {
						files[view.value..files.size-1].do({arg filepath;
							string = string ++ "'" ++ filepath ++ "' ";
						});
						command = "/usr/local/bin/mpg123 "++string;
						playFlag = true;
						activepid = (command).unixCmd;
					});
				});
				if(key==125, { // down
					view.value_(view.value+1);
				});
				if(key==126, { // up
					view.value_(view.value-1);
				});
				if(mod == 1048840, { // Apple key
					"storing playlist".postln;
					playlists[char.asSymbol] = files;
				});
				if(mod == 256, { // if no modifier
					 "choosing playlist nr: ".post; char.postln;
					 if(playlists[char.asSymbol].isNil.not, {
					 	files = playlists[char.asSymbol];
						jpg = nil;
						mp3files = files.reject({arg file; file.splitext[1] != "mp3" }); // get rid of other files than mp3
						files.do({arg file; if((file.splitext[1] == "jpg")||(file.splitext[1] == "bmp"), { jpg = file }) });
						if(jpg.isNil, { jpg = blackjpg });
						view.backgroundImage_(SCImage.new(jpg),10,0.3);
						view.items_(mp3files.collect({arg file; file.basename }));
					 });
				});
				if(key==24, { // the = sign
					files = [];
					view.items_(files);
					view.backgroundImage_(SCImage.new(blackjpg),10,0.3);
				});
			})
			.canReceiveDragHandler_(true)
			.receiveDragHandler_({ arg view;
				var draggedfiles, mp3files;
				draggedfiles = SCView.currentDrag;
				draggedfiles.do({ arg file;
					if(file.isFolder, {
						files = files ++ (file++"/*").pathMatch;
					},{
						files = files.add(file);
					})
				});
				
				mp3files = files.reject({arg file; file.splitext[1] != "mp3" }); // get rid of all other files than mp3
				files.do({arg file; if((file.splitext[1] == "jpg")||(file.splitext[1] == "bmp"), { jpg = file }) });
				[\jpg, jpg].postln;
				view.backgroundImage_(SCImage.new(jpg),10,0.3);
				view.items_(mp3files.collect({arg file; file.basename }));
			});
			
		win.front;
		win.onClose_({
			if(playFlag, { ("kill"+activepid.asString).unixCmd });
			playlists.writeArchive( "mp3playlists.ixi" );
		});
	}	
}