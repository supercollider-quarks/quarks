XiiPoolManager {
	var <>xiigui, <>win, params;
	
	*new { arg server, channels, setting=nil, rect, pool;
		^super.new.initXiiPoolManager(server, channels, setting, rect, pool);
		}
		
	initXiiPoolManager {arg server, channels, setting, argrect, pool;
		var rect;
		var selPool, txtv, saveButt, delPool, loadPool;
		var bufferDict, name, point;
				
		rect = argrect ? Rect(200, 100, 160, 56);		
		name = "PoolManager";
		
		point = if(setting.isNil, {XiiWindowLocation.new(name)}, {setting[1]});
		xiigui = nil; // not using window server class here
		params = if(setting.isNil, {[0,0,0,0,0]}, {setting[2]});

		bufferDict = if(Object.readArchive("preferences/bufferPools.ixi").isNil,{
						()
					}, {
						Object.readArchive("preferences/bufferPools.ixi")					}); // if no dict, create it
		
		win = GUI.window.new("- poolmanager -", Rect(point.x, point.y, rect.width, rect.height),
			 resizable:false);
		
		selPool = GUI.popUpMenu.new(win, Rect(10, 5, 140, 16))
			.font_(GUI.font.new("Helvetica", 9))
			.items_(bufferDict.keys.asArray)
			.value_(0)
			.background_(Color.white)
			.action_({ arg item;
			});

		delPool = GUI.button.new(win, Rect(10, 27, 67, 16))
			.canFocus_(false)
			.font_(GUI.font.new("Helvetica", 9))
			.states_([["delete pool", Color.black, Color.clear]])
			.action_({
				bufferDict.removeAt(selPool.items[selPool.value].asSymbol);
				selPool.items_(bufferDict.keys.asArray);
				bufferDict.writeArchive("preferences/bufferPools.ixi");
			});

		loadPool = GUI.button.new(win, Rect(82, 27, 67, 16))
			.font_(GUI.font.new("Helvetica", 9))
			.states_([["load pool", Color.black, Color.clear]])
			.action_({
				if(bufferDict.at(selPool.items[selPool.value]) != nil, {
				
					XQ.globalWidgetList.add(
						// here sending bufferpaths and selection array
						XiiBufferPool.new(Server.default, nil, nil, selPool.items[selPool.value].asString)
							.loadBuffers(
								bufferDict.at(selPool.items[selPool.value])[0], // pathnames
								bufferDict.at(selPool.items[selPool.value])[1]  // selections
								);
					);
					
					
				});
			});

		// if the manager is created from a save button in the pool
		if(pool.notNil, {
			txtv = GUI.textView.new(win, Rect(10, 51, 100, 14))
					.hasVerticalScroller_(false)
					.autohidesScrollers_(true)
					.focus(true)
					.font_(GUI.font.new("Helvetica", 9))
					.string_(pool.name.asString)
					.keyUpAction_({arg view, key, mod, unicode; 
						if(unicode ==13, {
							saveButt.focus(true);
						});
					});

	
			saveButt = GUI.button.new(win, Rect(115, 50, 34, 16))
				.states_([["save",Color.black, Color.clear]])
				.font_(GUI.font.new("Helvetica", 9))
				.action_({ arg butt; var str, oldnamelist;
					//"OO in savebutt".postln;
					str = if(txtv.string == "", {Date.getDate.stamp.asString}, {txtv.string});
					// saving filepaths and selection list into file
					bufferDict.add((str).asSymbol -> 
						[pool.getFilePaths, XQ.globalBufferDict.at(pool.name.asSymbol)[1]]);
					selPool.items_(bufferDict.keys.asArray);
					// store the old bufferList
					oldnamelist = XQ.globalBufferDict.at(pool.name.asSymbol);
					// get rid of the old index key in XQ.globalBufferDict
					XQ.globalBufferDict.removeAt(pool.name.asSymbol);
					// put it back as the new
					XQ.globalBufferDict.add(str.asSymbol -> oldnamelist);
					// and rename the window
					pool.setName_(str);
					bufferDict.writeArchive("preferences/bufferPools.ixi");
				});
		});
		
		win.front;
		win.onClose_({
			var t;
			XQ.globalWidgetList.do({arg widget, i; if(widget === this, { t = i})});
			try{XQ.globalWidgetList.removeAt(t)};
			point = Point(win.bounds.left, win.bounds.top);
			XiiWindowLocation.storeLoc(name, point);
		});
	}
	
	getState { // for save settings
		var point;		
		point = Point(win.bounds.left, win.bounds.top);
		^[2, point, params]; // channels, point, params
	}

}