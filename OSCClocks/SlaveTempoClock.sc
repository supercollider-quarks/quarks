/**
  A TempoClock-like clock that uses another clock 
  (e.g. OSCClockReceiver) as timing source and provides
  a TempoClock like interface on top of it. This can be used in
  conjunction with the OSCClockSender and OSCClockReceiver classes.
  
  // Boot the server
  s = Server.default.boot;

  // Create an OSCClockReceiver instance
  ~clockReceiver = OSCClockReceiver.new
  
  // Send some clock signal to it [just for testing]:
  OSCClockSender.default.targets = [NetAddr("localhost", 57120)]
  OSCClockSender.default.start
  
  // Create a SlaveTempoClock using the receiver and tempo 5.3.
  // We start on this odd looking startTime so that other SlaveTempoClocks
  // start at "compatible" times. On a beat..:
  ~slaveClock = SlaveTempoClock.new(~clockReceiver, 5.3, ((~clockReceiver.seconds / 5.3).ceil) * 5.3);
  
  // Have some Pbind fun with this clock
  (
  ~bind = Pbind(
  
  \degree, Pseq([1,5,3,2,6,9,11], inf),
  
  \dur, 1,
  
  \octave, 4,
  
  \root, 3,
  \amp,0.1
  
  ).play(~slaveClock)
  )
  
  // Stop the madness!
  ~bind.stop
*/
SlaveTempoClock
{
  var
  clock,
  startingTime,
  <beatsPerBar = 4.0,
  <baseBarBeat = 0.0,
  <baseBar = 0.0,
  <>tempo;
  
  /**
	Create a new instance using the clock argClock as timing
	source, with tempo argTempo (beats per second), and the starting
	time argStartingTime
  */
  *new
  {
	arg
	argClock = SystemClock,
	argTempo = 1,
	argStartingTime = argClock.seconds;
	
	// "[SlaveTempoClock]: new()".postln;
	
	^super.new.init(argClock, argTempo, argStartingTime);
  }
  
  init
  {
	arg
	argClock,
	argTempo,
	argStartingTime;
	
	// "[SlaveTempoClock]: init()".postln;

	tempo = argTempo;
	clock = argClock;
	startingTime = argStartingTime;
  }

  /**
	See TempoClock for more info
  */
  sched
  {
	arg
	delta,
	function;
	
	// "[SlaveTempoClock]: sched()".postln;

	this.schedAbs(this.beats + delta, function);
  }

  /**
	See TempoClock for more info
  */
  schedAbs
  {
	arg
	beat,
	function;

	// "[SlaveTempoClock]: schedAbs()".postln;

	clock.schedAbs
	(
	  this.beats2secs(beat),
	  {
		arg
		clockTime;

		var
		ret;

		ret = function.value(this.secs2beats(clockTime));

		if 
		(
		  ret.isKindOf(Number),
		  {
			this.schedAbs(beat + ret, function)
		  }
		)
	  }
	)
  }
  
  clear
  {
	// "[SlaveTempoClock]: clear()".postln;
	clock.clear
  }

  /**
	See TempoClock for more info
  */
  beats2secs
  {
	arg
	beats;

	// "[SlaveTempoClock]: beats2secs()".postln;

	^((beats / tempo) + startingTime);
  }

  /**
	See TempoClock for more info
  */
  secs2beats
  {
	arg
	secs;

	// "[SlaveTempoClock]: secs2beats()".postln;

	^((secs - startingTime) * tempo);
  }

  /**
	See TempoClock for more info
  */
  beatDur
  {
	// "[SlaveTempoClock]: beatDur()".postln;
	^(1.0 / tempo);
  }
  
  /**
	See TempoClock for more info
  */
  beats
  {
	// "[SlaveTempoClock]: beats()".postln;

	^((clock.seconds - startingTime) * tempo);
  }

  /**
	See TempoClock for more info
  */
  nextTimeOnGrid 
  { 
	arg 
	quant = 1, 
	phase = 0;

	var 
	offset;

	if (quant < 0) { quant = beatsPerBar * quant.neg };
	offset = baseBarBeat + phase;
	^roundUp(this.beats - offset, quant) + offset;
  }

  /**
	See TempoClock for more info
  */
  play 
  { 
	arg 
	task, 
	quant = 1; 
	
	this.schedAbs(quant.nextTimeOnGrid(this), task) 
  }
}