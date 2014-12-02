XiiImageConverter{
	// using sips

	*new {
		^super.new.initXiiImageConverter;
		}
		
	initXiiImageConverter {

		var win, listview, files, format, height, width;
		var widthbox, heightbox, formatpop, convertbutt, dskbut, desktop;
		var htmlstring;
		
		files = [];
		width = 800;
		height = 0;
		desktop = true;
		htmlstring = "<html>\n<body>\n";
		
		win = Window.new("ixi img converter", Rect(0, 600, 320, 290), resizable: false);
		
		listview = ListView.new(win, Rect(10,10, 300, 240))
			.items_(files)
			.hiliteColor_(Color.white) //Color.new255(155, 205, 155)
			.selectedStringColor_(Color.black)
			.stringColor_(Color.white)
			.focus(true)
			.focusColor_(Color.black.alpha_(0.0))	
			.resize_(5)
			.keyDownAction_({arg view, char, mod, uni, key;
				if(key==51, { // back arrow - empty the listview
					files.removeAt(view.value);
					view.items_(files.collect({arg file; file.basename }));
				});
				if(key==36, { // Return button - open selected file
					files[view.value].postln;
					("open"+files[view.value]).unixCmd;
				});
				if(key==125, { // down
					view.value_(view.value+1);
					view.backgroundImage_(SCImage.new(files[view.value]), 10, 0.5);
				});
				if(key==126, { // up
					view.value_(view.value-1);
					view.backgroundImage_(SCImage.new(files[view.value]), 10, 0.5);
				});
			})
			.canReceiveDragHandler_(true)
			.receiveDragHandler_({ arg view;
				var draggedfiles;
				draggedfiles = SCView.currentDrag;
				draggedfiles.do({ arg file;
					if(file.isFolder, {
						files = files ++ (file++"/*").pathMatch;
					},{
						files = files.add(file);
					})
				});
				view.backgroundImage_(SCImage.new(files[0]), 10, 0.5);
				view.items_(files.collect({arg file; file.basename }));
			});
			
		formatpop = PopUpMenu(win, Rect(10, 260, 60, 20))
					.items_(["jpeg", "tiff", "png", "gif", "jp2", "pict", "bmp", "qtif", "psd", "sgi", "tga"])
					.background_(Color.white)
					.resize_(7)
					.action_({ arg menu;
						[menu.value, menu.item].postln;
					});
		
		StaticText(win, Rect(78, 260, 36, 18)).string_("w:").resize_(7);
		
		widthbox = NumberBox(win, Rect(94, 260, 36, 18))
					.value_(width)
					.align_(\center)
					.resize_(7)
					.action_({arg numb; width = numb.value; });

		StaticText(win, Rect(136, 260, 36, 18)).string_("h:").resize_(7);

		heightbox = NumberBox(win, Rect(150, 260, 36, 18))
					.value_(height)
					.align_(\center)
					.resize_(7)
					.action_({arg numb; height = numb.value; });

		convertbutt = Button(win, Rect(194, 260, 60, 20))
				.states_([["convert", Color.black, Color.clear]])
				.resize_(7)
				.action_({ arg butt; var command, htmlfile;
					width = widthbox.value;
					height = heightbox.value;

					if(listview.items.size > 0, {
						if(height < 1, { // width only
							command = "/usr/bin/sips -s format"+formatpop.items[formatpop.value]+"--resampleWidth"+width;
						});
						if(width < 1, { // height only
							command = "/usr/bin/sips -s format"+formatpop.items[formatpop.value]+"--resampleHeight"+height;
						});
						if((height > 1) && (width > 1), { // both height and width
							command = "/usr/bin/sips -s format"+formatpop.items[formatpop.value]+"--resampleHeightWidth"+height+width;
						});
						if(desktop, {
							{
							"__________ AAAAAAAAAA________________".postln;
							"mkdir ~/Desktop/ixi_imgs".unixCmd;
							0.1.wait; // give system time to create the folder - if not File will not work
							htmlfile = File("~/Desktop/ixi_imgs/ixi_imgs.html".standardizePath, "w");
							files.do({arg filepath; var filename;
								filename = filepath.splitext[0].basename++"."++formatpop.items[formatpop.value];
								htmlstring = htmlstring ++ "<img src="++ filename.asString ++">\n";
								(command+filepath+"--out ~/Desktop/ixi_imgs/"++filename).postln;
								(command+filepath+"--out ~/Desktop/ixi_imgs/"++filename).unixCmd;
								0.1.wait;
							});
							htmlstring = htmlstring ++ "</body>\n</html>";
							htmlfile.write(htmlstring);
							htmlfile.close;
							}.fork(AppClock);
						}, {
							"__________ BBBBBBBBBB________________".postln;
							htmlfile = File(files[0].dirname++"/ixi_imgs.html", "w");
							files.do({arg filepath; var filename;
								filename = filepath.dirname++"/_ixi_"++filepath.basename.splitext[0]++"."++formatpop.items[formatpop.value];
								htmlstring = htmlstring ++ "<img src="++ filename.asString ++">\n";
								(command+filepath+"--out "++filename).postln;
								(command+filepath+"--out "++filename).unixCmd;
							});
							htmlstring = htmlstring ++ "</body>\n</html>";
							htmlfile.write(htmlstring);
							htmlfile.close;
						});
					
					
					});
					htmlstring = "<html>\n<body>\n"; // set it back to intial state
				});
				
		dskbut = Button(win, Rect(264, 263, 14, 14))
				.states_([["", Color.clear, Color.clear],
					   ["", Color.clear,  Color.new255(103, 148, 103)]])				.resize_(7)
				.value_(1)
				.canFocus_(false)
				.action_({arg val; desktop = val.value.booleanValue });

		StaticText(win, Rect(284, 261, 40, 18)).string_("dsk").resize_(7);

		win.front;
//		win.onClose_({
//			if(playFlag, { ("kill"+activepid.asString).unixCmd });
//			playlists.writeArchive( "mp3playlists.ixi" );
//		});		
	}	
}