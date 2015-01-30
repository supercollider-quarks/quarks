

Sheet {

	*new { arg buildDialog,name="",bounds;
		var layout;
		layout = PageLayout(name,bounds,front:false);
		buildDialog.value(layout);
		layout.resizeToFit(center:true);
		layout.front;
		^layout
	}
	*getString { arg prompt,defaultString,callback;
		var b;
		Sheet({ arg l;
			b = 	TextField(l,Rect(0,0,150,30));
			b.string = String.new ++ defaultString;
			b.action = {arg field; callback.value(field.value); l.close; };
		},prompt);
		{ b.focus }.defer;
	}
}


ModalDialog { // hit ok or cancel

	*new { arg buildDialog,okFunc,name="?",cancelFunc;
		var globalKeyDownFunc;
		globalKeyDownFunc = View.globalKeyDownAction;
		View.globalKeyDownAction = nil;

		Sheet({ arg layout;
			var returnObjects;

			returnObjects=buildDialog.value(layout);

			layout.startRow;
			ActionButton(layout,"OK",{
				okFunc.value(returnObjects);
				layout.close;
			});

			ActionButton(layout,"Cancel",{
				cancelFunc.value(returnObjects);
				layout.close;
			});

		},name).onClose_({ View.globalKeyDownAction = globalKeyDownFunc; });
	}
}


