HrButton : SCViewHolder
{
	var isLearning, didLearnOn, didLearnOff, learnOnResponder, learnOffResponder, midiOnResponder, midiOffResponder,
	<boundOnMidiArgs, <boundOffMidiArgs, userCloseFunc;
	
	*new
	{|argParent, argBounds|
	
		^super.new.prInit(argParent, argBounds);
	}
	
	prInit
	{|argParent, argBounds|
	
		isLearning = false;
		didLearnOn = false;	
		didLearnOff = false;	
		
		view = Button(argParent, argBounds)
		.keyDownAction_
		({|slView, char, modifiers, unicode, keycode|
			
			this.myKeyDown(slView, char, modifiers, unicode, keycode);
		}).onClose_({ this.doCleanUp; view = nil; });	
		
	}
	
	value
	{
		^this.view.value;
	}
	
	onClose_
	{|argFunc|
	
		if(argFunc.class != FunctionList,
		{
			if(userCloseFunc.notNil,
			{
				this.view.onClose = this.view.onClose.removeFunc(userCloseFunc);
				this.view.onClose = this.view.onClose.addFunc(argFunc);
			},
			{
				this.view.onClose = this.view.onClose.addFunc(argFunc);
			});
		});
		
		userCloseFunc = argFunc;
	}
	
	myKeyDown
	{
		arg slView, char, modifiers, unicode, keycode;
		
		if(char == $l,
		{
			if(isLearning and: { (didLearnOn == false).and(didLearnOff == false) },
			{
				this.cancelLearn;
				^this;
			});
			
			if(isLearning.not,
			{								
				this.armForLearn;
				^this;
			},
			{
				this.approveLearn;					
				^this;
			});
			
		});		
		
		if(char == $0,
		{
			if(isLearning,
			{
				this.resetLearn;
				^this;
			});
			
			^this;
		});
		
		//if reached here, bubble up to default action.
		this.view.defaultKeyDownAction(char, modifiers, unicode); 
	}
	
	armForLearn
	{
		isLearning = true;
		//oldBackground = this.view.background;
		this.view.focusColor = Color.blue; this.parent.refresh;
		"Press or release a key...".postln;
		learnOnResponder = NoteOnResponder
		({
			arg src, chan, num, value;
			boundOnMidiArgs = [src, chan, num];
			{ this.view.valueAction_(this.view.value + 1); }.defer;
			didLearnOn = true;
		});
		
		learnOffResponder = NoteOffResponder
		({
			arg src, chan, num, value;
			boundOffMidiArgs = [src, chan, num];
			{ this.view.valueAction_(this.view.value + 1); }.defer;
			didLearnOff = true;
		});
	}
	
	cancelLearn
	{
		isLearning = false;
		boundOnMidiArgs = nil;
		boundOffMidiArgs = nil;
		//this.view.background = oldBackground;
		this.view.focusColor = Color(0, 0, 0, 0.5); this.parent.refresh;
		learnOnResponder.remove;
		learnOffResponder.remove;
		"Learning cancelled...".postln;
	}
	
	approveLearn
	{
		this.view.focusColor = Color(0, 0, 0, 0.5); this.parent.refresh;
		learnOnResponder.remove;
		learnOffResponder.remove;
		isLearning = false;
		
		if(didLearnOn,
		{
			midiOnResponder.remove;
			midiOnResponder = 
			NoteOnResponder({
				arg src, chan, num, value;
				{ this.view.valueAction_(this.view.value + 1); }.defer;
			}, *boundOnMidiArgs);
		});
		
		if(didLearnOff,
		{
			midiOffResponder.remove;
			midiOffResponder = 
			NoteOffResponder({
				arg src, chan, num, value;
				{ this.view.valueAction_(this.view.value + 1); }.defer;
			}, *boundOffMidiArgs);
		});
	}
	
	resetLearn
	{
		boundOnMidiArgs = nil;
		boundOffMidiArgs = nil;
		midiOnResponder.remove;
		midiOffResponder.remove;
		learnOnResponder.remove;
		learnOffResponder.remove;
		//{ this.view.background = oldBackground; }.defer;
		this.view.focusColor = Color(0, 0, 0, 0.5); this.parent.refresh;
		"Old midi learn info cleared...".postln;
		isLearning = false;
		didLearnOn = false;
		didLearnOff = false;
	}
	
	doCleanUp
	{
		learnOnResponder.remove;
		midiOnResponder.remove;
		learnOffResponder.remove;
		midiOffResponder.remove;
		
	}
	/////
	
	boundOnMidiArgs_
	{|argBM|
		
		if(argBM.notNil,
		{
			boundOnMidiArgs = argBM;
			//oldBackground = this.view.background;
			didLearnOn = true;
			this.approveLearn;
		});
	}
	
	boundOffMidiArgs_
	{|argBM|
		
		if(argBM.notNil,
		{
			boundOffMidiArgs = argBM;
			//oldBackground = this.view.background;
			didLearnOff = true;
			this.approveLearn;
		});
	}
}