
// FileListView - a File browser gui object
// 03 2007 - blackrain at realizedsound dot net

FileListView {
	var  <listbox, <basePath, <path, <filename, <>action, <>dblClickAction, <types, <filtered, 
		<>dirColor, <>typeColors;

	*paletteExample { arg parent, bounds;
		^GUI.fileListView.new(parent,bounds)
	}
	*new { arg parent, bounds, basePath, action, dblClickAction, types, filtered=false;
		^super.new.init(parent, bounds, basePath, action, dblClickAction, types, filtered)
	}
	init { arg argParent, argBounds, argbp, argAction, argDblClickAction, argtypes, argfiltered;
		var f;
		action = argAction;
		dblClickAction = argDblClickAction;
		
		dirColor = Color(blue: 0.2, alpha:0.3);
		// Color(0.49441823959351, 0.5077965259552, 0.68645918369293, 0.75);
		typeColors = [
			Color(0.59729175567627, 0.55308566093445, 0.89100787639618, 1.0),
			Color(0.59729175567627, 0.55308566093445, 0.89100787639618, 0.5),
			Color(0.69688310623169, 0.79153182506561, 0.89808804988861, 1.0),
			Color(0.56976744186047, 0.23255813953488, 0.87209302325581, 0.5)
		];
		types = argtypes ? ["aiff", "aif", "wav", "sd2"];
		filtered = argfiltered;
		
		basePath = argbp ? (String.scDir ++ "/sounds/");
		path = PathName(basePath);
	
		f = {
			var ci;
			filename = listbox.items[listbox.value];
			if (filename.last == $/,  {   // a dir
				if (filename == "../", { // one dir up
					ci = path.colonIndices;
					path = PathName(
							if((path.fullPath.last == $/) && (ci.size > 1), {
								path.fullPath.copyRange(0, ci[ci.size - 2]);
							}, {
								path.fullPath.copyRange(0, path.lastColonIndex)
							})
						);
				}, {
					path = PathName(path.folders[listbox.value-1].fullPath);
				});
				this.refresh;
			}, {
				// a file
				dblClickAction.value(this);
			});
		};
		
		listbox = GUI.listView.new(argParent, argBounds)
			.action_({ arg v; filename = v.items[v.value]; this.action.value(this); })
			.enterKeyAction_({ arg v; f.value; })
			.mouseDownAction_({ arg v, x, y, modifiers, buttonNumber, clickCount;
				if (clickCount == 2, { f.value; });
			})
			.beginDragAction_({ arg view; this.fullname });

		this.refresh;
	}
	bounds {
		^listbox.bounds
	}
	bounds_ { arg argBounds;
		listbox.bounds = argBounds
	}
	path_ { arg newPath;
		if ( File.exists(newPath), {
			path = PathName(newPath);
			this.refresh;
		}, {
			"invalid path.".error
		})
	}
	fullname {
		^(path.pathOnly ++ listbox.items[listbox.value])
	}
	refresh {
		var files, colors=Array();
		files = ["../"];
		colors = colors.add(dirColor);
		path.folders.do({| item |
			files = files.add( format("%/", item.folderName) );
			colors = colors.add(dirColor);
		});
	//	path.folders.do({| item | files = files.add(item.fileName ++ "/") }); // linux bug
		if ( filtered, {
			path.files.do { arg item;
				types.do { arg ext, i;
					var fname = item.fileName;
					if (fname.icontainsStringAt(fname.size - ext.size, ext), {
						files = files.add(fname);
						colors = colors.add( typeColors[i] ? Color(0,0,0,0) );
					})
				}
			}
		},{
			path.files.do({| item |
				files = files.add(item.fileName);
				colors = colors.add( typeColors[0] )
			})
		});
		listbox.items = files;
		listbox.value = 0;
		filename = listbox.items[listbox.value];
		listbox.setProperty(\itemColors, colors)
	}
	value {
		^listbox.value
	}
	value_ { arg val;
		listbox.value = val
	}
	items {
		^listbox.items
	}
	filtered_ { arg state;
		filtered = state;
		this.refresh
	}
	types_ { arg array;
		types = array;
		this.refresh
	}
}

