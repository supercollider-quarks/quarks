/*
XiiAlert("ixi alert: you need to do stuff to do stuff");
*/

XiiAlert {
	
	var <>xiigui, <>win;

	*new {arg message;
		^super.new.initXiiAlert(message);
	}

	initXiiAlert {arg message;
		var a, p, b, str;
		xiigui = nil;

		p = [ // the ixi logo
		Point(1,7), Point(8, 1), Point(15,1), Point(15,33),Point(24, 23), Point(15,14), Point(15,1), 
		Point(23,1),Point(34,13), Point(45,1), Point(61,1), Point(66,6), Point(66,37), Point(59,43),
		Point(53,43), Point(53,12), Point(44,22), Point(53,33), Point(53,43), Point(42,43), Point(34,32),
		Point(24,43), Point(7,43), Point(1,36), Point(1,8)
		];
		
		if ( GUI.id == \cocoa,
			{ win = SCModalWindow.new("ixiQuarks alert!", Rect(128, 500, 400,170)) }, 
			{ win = Window.new("ixiQuarks alert!", Rect(128, 500, 400,170), resizable:false).front }
		);

		win.drawHook = {
			// set the Color
			GUI.pen.color = Color.new255(255, 100, 0);
			GUI.pen.width = 3;
			GUI.pen.translate(28,28);
			GUI.pen.scale(1.6, 1.6);
			GUI.pen.moveTo(1@7);
			p.do({arg point;
				GUI.pen.lineTo(point+0.5);
			});
			GUI.pen.stroke
		};
		a = GUI.staticText.new(win, Rect(60, 120, 300, 20));
		a.string_(message);
		win.refresh;
		
		str = ["OK", "oh, ok!", "crap", "oh shit", "all right", "vale", "cool", 
		"again?", "what the..."].wchoose([0.50, 0.10, 0.10,0.5,0.5,0.5,0.5,0.5,0.5]);
		GUI.button.new(win ,Rect(220, 50, 60, 30))
				.states_([[str, Color.black,Color.clear]])
				.action_({ win.close;})
				.focus(true);
	}
}

/*
a = Window.new("test", Rect(10, 10, 500, 300)).front;
b = XiiAlertSheet(a, Rect(20, 20, 280, 120), "The ModalSheet is the Message");

*/
XiiAlertSheet {
	var sheet;

	*new {arg parent, rect, message;
		^super.new.initXiiAlertSheet(parent, rect, message);
	}

	initXiiAlertSheet {arg parent, rect, message;
		var a, p, b, str;

		p = [ // the ixi logo
		Point(1,7), Point(8, 1), Point(15,1), Point(15,33),Point(24, 23), Point(15,14), Point(15,1), 
		Point(23,1),Point(34,13), Point(45,1), Point(61,1), Point(66,6), Point(66,37), Point(59,43),
		Point(53,43), Point(53,12), Point(44,22), Point(53,33), Point(53,43), Point(42,43), Point(34,32),
		Point(24,43), Point(7,43), Point(1,36), Point(1,8)
		];

		sheet = SCModalSheet.new(parent, rect, resizable:false );
		sheet.drawHook = {
			// set the Color
			GUI.pen.color = Color.new255(255, 100, 0);
			GUI.pen.width = 3;
			GUI.pen.translate(28,18);
			GUI.pen.scale(1, 1);
			GUI.pen.moveTo(1@7);
			p.do({arg point;
				GUI.pen.lineTo(point+0.5);
			});
			GUI.pen.stroke;
		};
		a = GUI.staticText.new(sheet, Rect(30, 80, 300, 20))
			.font_(GUI.font.new("Helvetica", 9))
			.string_(message);
		//win.refresh;
		
		str = ["OK", "oh, ok!", "crap", "oh shit", "all right", "vale", "cool", 
		"again?", "what the..."].wchoose([0.50, 0.10, 0.10,0.5,0.5,0.5,0.5,0.5,0.5]);
		GUI.button.new(sheet ,Rect(160, 28, 60, 30))
				.states_([[str, Color.black,Color.clear]])
				.action_({ sheet.close;})
				.focus(true);
	}
}

