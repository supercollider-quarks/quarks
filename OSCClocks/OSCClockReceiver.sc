/**
  OSCClockReceiver objects can be used to receive a clock signal sent 
  by an OSCClockSender object.

  // create a new OSCClockReceiver object:
  ~clockReceiver = OSCClockReceiver.new
 
  // create a new OSCClockReceiver object with filter coefficient 0.1,
  // OSC base path "/foobar" and a message timeout of 1 sec.
  ~clockReceiver = OSCClockReceiver.new(w: 0.1, path: "/foobar", timout: 1)

  // Make the receiver a bit more verbose:
  ~clockReceiver.verbose = true
*/
OSCClockReceiver
{
  var
  internalClock,
  timeResponder, 
  state,
  path,

  // The state needed for our locked loop
  currentRemoteTime,
  currentSystemTime,
  nextSystemTime,
  firstTime,
  z,
  <>w,

  accumTime,
  <>verbose = false,
  <>verboseTime = 5,

  <>timeout,
  timeoutTime = 0;
  
  *new
  {
	arg
	/**
	  The filter coefficient for the locked loop
	*/
	argW = 0.01,
	/**
	  The OSC path were to listen for time messages
	*/
	argPath = "/OSCClocks",
	/**
	  The timeout when to stop scheduling after the last
	  message has been sent
	*/
	argTimeout = 5;

	^super.new.init(argW, argPath, argTimeout);
  }
  
  init
  {
	arg
	argW,
	argPath,
	argTimeout;

	// "[ClockReceiver]: Init...".postln;

	w = argW;
	path = argPath;
	timeout = argTimeout;

	internalClock = SystemClock;

	this.reset;

	timeResponder = OSCresponderNode.new(
	  nil,
	  path ++ "/time", 
	  {
		arg
		time, 
		resp, 
		msg, 
		addr;
 
		//"[OSCClockReceiver]: action...".postln;
		this.timeCallback(time, resp, msg, addr)
	  }
	).add;

	internalClock.schedAbs(internalClock.seconds + 0.5,
	  {
		if (state == \running,
		  {
			timeoutTime = timeoutTime + 0.5;
			if (timeoutTime > timeout,
			  {
				if (verbose == true, {"[OSCClockReceiver]: Timed out waiting for messages. Resetting...".postln; });
				this.reset;
			  }
			);
		  }
		);
		0.5;
	  }
	);
  }

  reset
  {
	if (verbose == true, {"[OSCClockReceiver]: resetting...".postln; });

	accumTime = 0;

	firstTime = true;

	z = 0.0;
	currentRemoteTime = 0;
	currentSystemTime = 0;
	nextSystemTime = 0;

	timeoutTime = 0;

	state = \stopped;
  }

  timeCallback {
	arg 
	time, 
	theResponder, 
	message, 
	addr; 

	var 
	remoteTime, 
	periodTime,
	d;


	//"[OSCClockReceiver]: timeCallback".postln;

	remoteTime = message[1].asFloat;
	periodTime = 1.0/message[2];

	// reset watchdog
	timeoutTime = 0;

	if (firstTime,
	  {
		if (verbose == true, {"[OSCClockReceiver]: Receiving time messages...".postln; });

		state = \running;
		accumTime = 0;
		firstTime = false;
		currentSystemTime = internalClock.seconds;
		nextSystemTime = currentSystemTime + periodTime;
		z = 0;
		currentRemoteTime = remoteTime;
		^nil;
	  },
	  {
		//"[OSCClockReceiver]: firstTime == false".postln;
		d = (internalClock.seconds) - nextSystemTime;
		z = z + (w * (d - z));
		currentSystemTime = nextSystemTime;
		nextSystemTime = nextSystemTime + z + periodTime;

		accumTime = accumTime + periodTime;
		if ((accumTime > verboseTime).and(verbose == true),
		  {
			("[OSCClockReceiver]: z: " ++ z ++ " d: " ++ d ++ " message: " ++ message).postln;
			accumTime = 0;
		  }
		);
	  }
	);
	
	
	if (currentRemoteTime > remoteTime,
	  {
		if (verbose == true, {"[OSCClockReceiver]: Sync messages out of order. Resyncing...".postln; });

		this.reset;
	  },
	  {
		currentRemoteTime = remoteTime;
	  }
	);
  }

  sched {
	arg delta, function;

	schedAbs([(this.seconds + delta), function]);
  }

  schedAbs {
	arg time, function;

	if (state != \running, {
	  "[OSCClockReceiver]: not RUNNING. Start clock sender on remote end..".postln;
	  ^nil;
	});
	internalClock.sched(time - this.seconds, {
	  arg system_time;
	  var ret;

	  ret = function.value(system_time);
	  if (ret.isKindOf(Number),

		{
		  this.schedAbs(time + ret, function);
		});

	  nil;
	});
  }

  clear {
	internalClock.clear;
  }
	
  seconds {
	if (state == \running,
	  {
		^(currentRemoteTime + (internalClock.seconds - currentSystemTime));
	  },
	  {
		0;
	  }
	);
  }
}

