MoltenMaster
{
	var 	<pieces, <>lockAction, <>unlockAction, <isLocked, <doneCondition,
		<currentlyPlaying, intCondition, <isPlaying;
	
	*new
	{|argNumPieces|
		
		argNumPieces = argNumPieces ? 0;
		if(argNumPieces < 1, { "You should provide an argument bigger than 0.".error; ^this.halt; });
		^super.new.init(argNumPieces);
	}
	
	init
	{|argNumPieces|
		
		pieces = argNumPieces.collect({ MoltenPiece.new(this); });
		doneCondition = Condition.new.test_(false);
		intCondition = Condition.new.test_(false);
		currentlyPlaying = -1;
		lockAction = unlockAction = {};
		isLocked = false;
		isPlaying = false;
		
	}
	
	playPiece
	{|argIndex|
	
		if(isLocked.not and: { currentlyPlaying == -1 },
		{
			lockAction.value;
			isLocked = true;
			
			{
				pieces[argIndex].initAction.value;
				doneCondition.wait;
				doneCondition.test_(false);
				pieces[argIndex].playAction.value;
				doneCondition.wait;
				doneCondition.test_(false);
				currentlyPlaying = argIndex;
				unlockAction.value;
				isLocked = false;
				isPlaying = true;
			}.fork(AppClock);
		},
		{
			if(isLocked.not and: { currentlyPlaying != -1; },
			{
				this.prStopPlay(argIndex);
			});
		});		
	}
	
	stopPlayback
	{
		if(isLocked.not and: { currentlyPlaying != -1; },
		{
			lockAction.value;
			isLocked = true;
			
			{
				pieces[currentlyPlaying].finishAction.value;
				doneCondition.wait;
				doneCondition.test_(false);
				currentlyPlaying = -1;
				unlockAction.value;
				isLocked = false;
				isPlaying = false;
			}.fork(AppClock);
		});
	}
	
	prStopPlay
	{|argIndex|
	
		//dupe code from stopPlayback
		if(isLocked.not and: { currentlyPlaying != -1; },
		{
			lockAction.value;
			isLocked = true;
			
			{
				pieces[currentlyPlaying].finishAction.value;
				doneCondition.wait;
				doneCondition.test_(false);
				currentlyPlaying = -1;
				unlockAction.value;
				isLocked = false;
				isPlaying = false;
				this.playPiece(argIndex);
			}.fork(AppClock);
		});
	}
	
	signal
	{
		doneCondition.test_(true).signal;
		^this;
	}
}

MoltenPiece
{
	var parent, <>initAction, <>playAction, <>finishAction, <>nameString, <>infoString, 
		<>durationString;
	
	*new
	{|argParent|
		
		^super.new.init(argParent);
	}
	
	init
	{|argParent|
	
		parent = argParent;
		initAction = playAction = finishAction = { parent.doneCondition.test_(true).signal; };
		nameString = "Untitled";
		durationString = "inf";	
		infoString = "";
	}
}

MoltenPlayer
{
	var master, win, lockables, playList;
	
	*new
	{|argMaster|
	
		^super.new.init(argMaster);
	}
	
	init
	{|argMaster|
	
		master = argMaster;
		lockables = List.new;
		win = Window.new("MoltenPlayer", Rect(100, 100, 300, 200), false)
			.onClose_({ master.stopPlayback; });
		lockables.add
		(
			Button(win, Rect(10, 10, 40, 20))
				.states_([["<-"]])
				.action_
				({|btn|
					
					if(master.isPlaying,
					{
						master.playPiece((playList.value - 1).wrap(0, master.pieces.size - 1));
					});
					
					playList.value_((playList.value - 1).wrap(0, master.pieces.size - 1));
				});
		);
		
		lockables.add
		(
			Button(win, Rect(60, 10, 40, 20))
				.states_([["->"]])
				.action_
				({|btn|
					
					if(master.isPlaying,
					{
						master.playPiece((playList.value + 1).wrap(0, master.pieces.size - 1));
						playList.value_(master.currentlyPlaying);
					});
					
					playList.value_((playList.value + 1).wrap(0, master.pieces.size - 1));
					
				});
		);
		
		lockables.add
		(
			Button(win, Rect(110, 10, 40, 20))
				.states_([[">>"]])
				.action_
				({|btn|
				
					master.playPiece(playList.value);
				});
		);
		
		lockables.add
		(
			Button(win, Rect(160, 10, 40, 20))
				.states_([["[ ]"]])
				.action_
				({|btn|
				
					master.stopPlayback;
				});
		);
		
		lockables.add
		(
			playList = 
				ListView(win, Rect(10, 40, 280, 150))
					.items_(master.pieces.collect({|item, cnt| "%: % -- %".format((cnt + 1).asString, item.nameString, item.durationString); }))
					.mouseDownAction_
					({|...args|
					
						
						if(args[5] == 2,
						{
							master.playPiece(args[0].value);
						});
					});
		);
		
		master.lockAction_({ if(win.isClosed.not, { lockables.do({|item| item.enabled_(false); }); }); });
		master.unlockAction_({ if(win.isClosed.not, { lockables.do({|item| item.enabled_(true); }); }); });
		
		win.front;
	}
}