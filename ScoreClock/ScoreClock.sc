/*
TODO:

- let TempoClocks defer to ScoreClock if thisThread.nrt == true, or have some other way of forcing the clock.
perhaps only the places we need to interfere with TempoClock is TempoClock.default (set to ScoreClock.default)
and TempoClock.new (return a new ScoreClock).

- make a wrapper method like this?

+ Function {
  makeScore {|maxtime=60|
    ScoreClock.beginScore;
    TempoClock.use_nrt = true;
    this.value;
    TempoClock.use_nrt = false;
    ^ScoreClock.makeScore(maxTime);
  }
}

NOTE: it only works when stuff are sent to Server.default, which is temporarily set to the ScoreDummy server.

*/

ScoreClock : Clock {
    classvar queue;
    classvar <score;
    classvar <default;
    classvar server,savedServer;
    
    var <>beats;
    var <>tempo;
    
    *new {|tmp=1|
        ^super.new.init(tmp);
    }
    
    *initClass {
        default = this.new;
    }
    
    *beginScore {
        server = Server.fromName(\ScoreDummy).serverRunning_(true);
        savedServer = Server.default;
        Server.default = server;
        queue = PriorityQueue.new;
        score = Score.new;
        server.openBundle;
        default.init(1);
    }
    
    init {|tmp|
        beats = 0;
        tempo = tmp;
    }
    
    schedAbs {|t,item|
        queue.put(t,[item,this]);
    }
    
    sched {|dt, item|
        this.schedAbs(beats+(dt/tempo), item);
    }
    
    //FIXME: handle quant
	play {|task, quant = 1|
		this.schedAbs(beats, task);
	}
	
	//FIXME: tempoClock uses mBaseBeats and mBaseSeconds, perhaps something we also need to care about?
	secs2beats {|secs|
	    ^secs * tempo;
	}

	beats2secs {|inBeats|
	    ^inBeats / tempo;
	}
    
    *makeScore {|maxTime=60,padding=0|
        var dt = 0, item, b, end = 0, time = 0, clock;
        while {queue.notEmpty and: {time<maxTime}} {
            time = queue.topPriority;
            #item, clock = queue.pop;
            clock.beats = time;

            server.openBundle;
            thisThread.clock = clock;
            dt = item.awake(clock.beats, time, clock);
            b = server.closeBundle(false);
            if(b.notEmpty) { score.add([time] ++ b) };

            if(dt.isNumber) {
                clock.sched(dt, item);
            };
            if(dt.isNil) {
                end = clock.beats;
            };
        };
        b = server.closeBundle(false);
        if(b.notEmpty) { score.add([0] ++ b) };
        score.add([end+padding,[\c_set,0,0]]);
        server.serverRunning = false; // "stop" the dummy server
        Server.default = savedServer;
        TempoClock.default.tempo = 1; //FIXME: better to fix Score to default to tempo 1 (seconds).
        ^score;
    }
    
   *play { | task, quant | default.play(task, quant)  }
   *sched { | delta, item | default.sched(delta, item)  }
   *schedAbs { | beat, item | default.schedAbs(beat, item)  }
   *tempo_ { | newTempo | default.tempo_(newTempo)  }
   *tempo { ^default.tempo }
   *beats { ^default.beats }
   *secs2beats { | secs | ^default.secs2beats(secs)  }
   *beats2secs { | secs | ^default.beats2secs(secs)  }
}

