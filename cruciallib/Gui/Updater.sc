
Updater {

	var <>model,<>updateFunc;

	*new { arg model,updateFunc;
		^super.new.model_(model).updateFunc_(updateFunc).init
	}
	init {
		model.addDependant(this);
	}

	update { arg ... args;
		updateFunc.valueArray(args)
	}
	remove {
		model.removeDependant(this);
	}
	removeOnClose { arg layout;
		NotificationCenter.registerOneShot(this.findWindow(layout),\didClose,this,{
			this.remove;
		})
	}
	findWindow { arg layout;
		var windowClass;
		windowClass = Window.implClass;
		loop {
			if(layout.class === windowClass,{
				^layout
			});
			if(layout.respondsTo('findWindow'),{ // SCTopView and children
				^layout.findWindow
			});
			if(layout.respondsTo('window'),{ // PageLayout
				^layout.window
			});
			layout = layout.parent
		}		
	}
}


