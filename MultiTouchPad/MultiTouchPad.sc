MultiTouchPad
{
	classvar <responder, <fingersDict, <activeBlobs, <>setAction, <>touchAction, <>untouchAction,
		<guiOn, <guiWin, <isRunning, <pid, <stopFunc, <device;
	
	
	*initClass
	{
		responder = OSCresponderNode(nil, "/tuio/2Dobj", {|...args| this.processOSC(*args); });
		fingersDict = Dictionary.new;
		activeBlobs = List.new;
		guiOn = false;
		isRunning = false;
		stopFunc = { this.stop; };
		device = 0;
	}
	
	*setDevice
	{|argDevice|
		
		argDevice.switch
		(
			\internal, { device = 0; },
			\external, { device = 1; },
			{ "argDevice must be \\internal for internal trackpad and \\external for external trackpad.".error; }
		);
	}
	
	*start
	{|argForce|
	
		if(isRunning == false or: { argForce == \force; },
		{
			if(argForce == \force, { responder.remove; }); //in case...
			
			"killall tongsengmod".unixCmd
				({|res|
					
					if(res == 0,
					{
						"A dangling tongsengmod process was found and terminated.".postln;
					});
					
					pid = ("tongsengmod localhost" + NetAddr.langPort.asString + device.asString).unixCmd
					({|res|
						
						if(res == 127,
						{
							"tongsengmod executable not found. See help.".error;
						});
					});
				});
			
			responder.add;
			isRunning = true;
		},
		{
			"MultiTouchPad is already active and running. Try MultiTouchPad.start(\\force)".error;
		});
		ShutDown.add(stopFunc);
	}
	
	*stop
	{
		if(isRunning,
		{
			responder.remove;
			("kill -1" + pid.asString).unixCmd;
			isRunning = false;
		},
		{
			responder.remove; //in case
			"killall tongsengmod".unixCmd;
			"MultiTouchPad isn't running.".error;
		});
	}
	
	*processOSC
	{|time, responder, msg|
	
		//msg.postln;
		var toRemove = List.new;
		var curID = msg[2];
		var xys = msg[4..6];
		
		if(msg[1] == 'alive',
		{
			
			activeBlobs = msg[2..];
			fingersDict.keys.do
			({|item|
				
				if(activeBlobs.includes(item).not,
				{
					toRemove.add(item);
				});
			});
			
			toRemove.do
			({|item| 
				
				fingersDict.removeAt(item); 
				untouchAction.value(item);
				if(guiOn, { { guiWin.refresh; }.defer; });
			});
			
			activeBlobs.do
			({|item|
			
				if(fingersDict.at(item).isNil,
				{
					fingersDict.put(item, -1); //-1 means xy not initialized
				});
			});
			
			^this;
		});
		
		if(msg[1] == 'set',
		{
			if(fingersDict.at(curID).isNil, { "MultiTouchPad: bug? this should never happen.".postln; });
			if(fingersDict.at(curID) == -1, { touchAction.value(curID, xys); });
			fingersDict.put(curID, xys);
			setAction.value(curID, xys);
			if(guiOn, { { guiWin.refresh; }.defer; });
			^this;
		});
	}
	
	*gui
	{
		var view;
		guiWin = Window("MultiTouchPad", Rect(100, 100, 525, 375)).onClose_({ guiOn = false; });
		view = UserView(guiWin, guiWin.view.bounds).background_(Color.white).resize_(5);
		view.drawFunc_
			({
				var fItem;
				
				fingersDict.keys.do
				({|key|
					
					fItem = fingersDict.at(key);
					Pen.color = Color.red;
					Pen.fillOval
					(
						Rect
						(
							guiWin.view.bounds.width * fItem[0], 
							guiWin.view.bounds.height * fItem[1],
							20 * fItem[2],
							20 * fItem[2]
						)
					);
				});
			});
		guiOn = true;
		guiWin.front;
	}
	
	*resetActions
	{
		touchAction = {};
		untouchAction = {};
		setAction = {};
	}
}