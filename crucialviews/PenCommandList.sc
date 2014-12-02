

PenCommandList {

	var <list;

	add { arg selector ... args;
		list = list.add( [selector] ++ args );
	}
	clear {
		list = nil;
	}
	value {
		var pen;
		pen = GUI.pen;
		pen.use {
			list.do { arg args; pen.perform(*args) }
		}
	}
}


