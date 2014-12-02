/*
   Automation is a SuperCollider Quark.
   (c) 2009 Neels J. Hofmeyr <neels@hofmeyr.de>, GNU GPL v3 or later.

   Automation allows to record and playback live changes made to
   other GUI elements in particular, or anything else you wish to
   setup. 

   This is Automation, the main class of the Automation Quark.
   This is what you instantiate to use Automation.

   Automation has a number of subscribed AutomationClient
   instances and delegates them, as well as plays back their actions.

   It can pop up a little transport GUI for user control, or you
   can fire the same events by calling Automation's functions.
*/

Automation {
    var <>length,
        <>presetDir,
        <>onPlay, <>onStop, <>onSeek,
        <>playLatency = 0.1, <>seekLatency = 0.1,
        <>doStopOnSeek = false, 
        <>onEnd,
        <>verbose = true,
        <>server,
        <>doDefer = true,
        <doRecord = false,
        <clients,
        <>gui,
        startTime = -1, startOffset = 0,
        semaphore = nil,
        playRoutine,
        playStatus = false,
        playDoReschedule = true,
        playLastStopped = 0;

    /*
     Description of Parameters
     
     * length (Default is 180, that's 3 minutes.)
         This value *only* affects the time slider, nothing else.
         Recorded values are NOT interfered with based on this number,
         it simply determines the scale of the time slider GUI element.

     * presetDir
         A directory name that the save and load dialogs show by default.
         If nil, $HOME/automation/ is used.

     * onPlay, onStop, onSeek
         Callback functions called when these events are carried out.
         Each get the current transport time in seconds passed in as
         a parameter.
         For example, use these callbacks to launch and stop some
         background action like audio file playback and such.
         Note, when doStopOnSeek is true, both onStop and onSeek will
         be called for each seek operation.

     * playLatency, seekLatency
         These apply to starting playback, and to seeking while playing,
         respectively. They determine how much headstart the onPlay
         and onSeek callbacks are given. See its use in this code.

     * doStopOnSeek
         If true, every seek stops playback. This causes onStop
         to be called upon a seek during playback.

     * onEnd
         This callback function is called when the time slider hits its
         end boundary.
         The default behaviour is to add 20% to the time slider's max
         when reaching the end of it, like this:
           control.onEnd = { control.length = control.length * 1.2; };
         But instead, we could also, for example, stop at the end:
           control.onEnd = { control.stop; };
         Or wrap back to the start:
           control.onEnd = { control.seek(0); };

     * verbose
         If false, all informational posts on the server window
         are omitted.

     * server
         If you were ever to run this on a different server from
         the default server, you have to pass the server arg to the
         Automation constructor. You may then access it here.

     * doDefer
         If true, all signals to GUI elements are wrapped in defer{}.
         This is automatically set false if the window class name
         starts with a `J', as SwingOSC doesn't need deferring.

     * doRecord
         If true, then the recording button is active (either orange
         or red).

     * clients
         A List of AutomationClient instances that are currently docked.

     * gui
         The AutomationGui instance that is connected to this Automation
         instance, if any.         
     */


    /* This is the constructor.
     * length: The initial max value for playing time.
     * server: The server to use.
     *      If server==nil, then Server.default will be used.
     */
    *new { |length=180, server=nil|
        ^super.new.constructor(length, server);
    }


    /* opens a transport control GUI.
     * win: If nil, opens a new window. Otherwise the
     *      given GUI.window is used to show the transport GUI.
     * bounds: A Rect(x,y,w,h) for size and position of the transport
     *      GUI.
     *      If bounds==nil, it will take up the whole window.
     *      If win==nil, these bounds give the size of the new window.
     *      If both win and bounds are nil, defaults are used.
     */
    front { |win=nil, bounds=nil|
        AutomationGui(this, win, bounds);
        ^this;
    }


    // this is what you call to dock another one of your gui elements
    // to Automation. If name==nil, name will be "automatedN", where
    // N is the current number of GUI elements docked.
    dock {|guiElement, name=nil|
        // safeguard the control's own elements
        if ( this.isMyGuiElement(guiElement) ) {
            if (verbose){
                ("Automation: Refusing to dock one of my own control"+
                  "elements as a client.").postln;
            };
        }{
            // nothing wrong here, add it.
            if (name == nil) {
                name = ("automated" ++ (clients.size + 1));
            };
            AutomationClient(guiElement, this, name);
        };
    }

    isMyGuiElement {|guiElement|
        if (gui != nil){
            ^gui.isMyGuiElement(guiElement);
        }{
            ^false;
        };
    }


    // e.g. findAndDock(window.view.children)
    findAndDock {|list|
        var classname;
        list.do{|child|
            classname = "" ++ child.class;
            if (classname.containsi("button") || classname.containsi("slider")
                || classname.containsi("numberbox")){
                this.dock(child);
            // Note: we're not passing a name to dock(), so the control
            // elements will be auto-named by order of appearance. That
            // means, altering a GUI and then loading a previously saved
            // automation will most probably load some or all values into the
            // wrong GUI elements, depending on whether their order changed.
            // (However, if you can always dock newer GUI elements after the
            // older ones, this won't be a problem.)
            // Note that the order of appearance is not necessarily the
            // visual order in the GUI.
            };
        };
    }


    // user callable function to start playback at the current position.
    play {
        var waitTime;
        fork{
            semaphore.wait;
            if (playStatus == false){
                startOffset = this.now;
                startTime = -1;
                playDoReschedule = true;

                // make sure we've got enough time elapsed after
                // the routine was last running.
                waitTime = this.clockTime - playLastStopped;
                if (waitTime < 0.2){
                    waitTime =  min(0.2, max(0.2 - waitTime, 0.01) );
                    waitTime.wait;
                };

                playRoutine.reset;
                playRoutine.play;
            };
            semaphore.signal;
        };
    }

    // user callable function to seek a given position in seconds.
    // The seeking action is instantaneous. Playback, if running,
    // continues at the new position, unless dostop is set true.
    // dostop should be passed either true or false; if you omit
    // dostop from your arguments list, the value from the variable
    // doStopOnSeek will be used, which in turn is false by default.
    seek { |seconds=0.0, dostop=nil|
        fork{
            if (dostop == nil) {
                dostop = doStopOnSeek;
            };

            semaphore.wait;
            this.privateSeek(seconds, dostop);
            semaphore.signal;
        };
    }

    // user callable function that stops playback, staying at the
    // current position.
    stop {
        fork{
            semaphore.wait;
            this.privateStop( this.now );
            semaphore.signal;
        };
    }

    quit {
        fork{
            semaphore.wait;
            if (startTime >= 0){
                startOffset = 0;
                startTime = -1;

                // call user supplied function
                onStop.value(startOffset);
            };
            semaphore.signal;
        };
    }


    // user callable function that records this instant value
    // for each subscribed client. For example, this is useful
    // for setting initial values for all GUI elements.
    snapshot {
        var now;
        now = max(0.0, this.now);
        this.defer{
            clients.do {|client|
                client.snapshot(now);
            };
        };
    }


    // saves all clients' values to disk, using client names as the
    // filenames in a given directory.
    save{|dir|
        presetDir = dir;
        ("mkdir -p" + dir).systemCmd;
        if (verbose){
            ("Automation: Saving controls to" + dir + "...").postln;
        };
        clients.do{|client| client.save(dir); };
        if (verbose){
            "Automation: ...saving done.".postln;
        };
    }



    // tries to load as many clients as possible from disk.
    load {|dir|
        presetDir = dir;
        if (verbose){
            ("Automation: Loading controls from `" ++ dir ++ "'...").postln;
        };
        clients.do{|client| client.load(dir); };
        if (verbose){
            "Automation: ...Loading done.".postln;
        };
    }


    // returns the current position on the time scale.
    now {
        if (startTime < 0){
            // startTime < 0 means we're not playing.
            // in that case the startOffset holds our current position.
            ^startOffset;
        }{
            // we're playing, so we need to use the startTime and
            // startOffset to determine our current position right NOW.
            ^(this.clockTime - startTime + startOffset);
        };
    }


    // internal constructor function
    constructor { |ilength, iserver|
        // evaluate input args

        server = iserver;
        length = 0.0 + ilength; // make sure it is a float

        if (server == nil){
            server = Server.default;
        };

        // set up other variables

        clients = List.new;
        semaphore = Semaphore(1);

        // default action when the time slider knob touches the rightmost
        // end: elongate the range of the slider by 20%, as in "make the
        // song a little longer". The time knob will skip a sixth to
        // the left and continue moving slightly slower than before,
        // but the time number will continue running steadily.
        onEnd = {
            length = length * 1.2; // +20%
        };


        // this is the bigass routine that takes care of
        // launching and scheduling playback.
        playRoutine = Routine{
            var now,
                events,
                nextEventTime,
                nextTimeSlider,
                visitClient, stopPlayback,
                aclient, step,
                req, reqarg,
                nowdisp;

            // the events queue
            events = SortedList(0, {|itemA, itemB| itemA[0] < itemB[0]});

            // the internal function to nudge a client to action and
            // get the next time it wants to be nudged, scheduling it
            // in the events queue.
            visitClient = {|iclient, inow|
                var nextTime;
                nextTime = iclient.bang(inow);
                if ((nextTime >= inow) && (nextTime < inf)){
                    events.add( [nextTime, iclient] );
                };
            };

            // enter/lock semaphore
            semaphore.wait;

            // start playback and set status
            this.privatePlay;
            playStatus = true;

            // run
            block{|break| loop{
                // semaphore is still (or again) locked

                now = this.now;

                // external stop carried out?
                if (startTime < 0) { break.value; };

                // someone changed the current timing positions?
                if (playDoReschedule){
                    playDoReschedule = false;

                    // skip to another time position; redo the events queue.
                    events.clear; 
                    
                    clients.do{|client|
                        visitClient.value(client, now);
                    };
                    
                    nextTimeSlider = -1;
                };

                // do clients need action?
                block{|break2| loop{
                    if (events.size < 1) { break2.value };
                    if (now >= events[0][0]) {
                        aclient = events[0][1];
                        events.removeAt(0);

                        visitClient.value(aclient, now);
                    }{
                        break2.value
                    };
                }};

                // does the timeslider need action?
                if (now >= nextTimeSlider){
                    // we display the time slider in a fixed resolution.
                    nowdisp = now.round(0.25);
                    if (gui != nil){
                        this.defer{ gui.updateTimeGUI(nowdisp); };
                    };
                    
                    // in that resolution, this is the next time that we
                    // need time slider action.
                    nextTimeSlider = nowdisp + 0.25;

                    if ((length - now) > 0){
                        // normal course of action.
                        // ensure range of the next timeslider event does
                        // not exceed the slider range.
                        nextTimeSlider = max(0, min(nextTimeSlider, length));
                    }{
                        // the slider knob reached its end.
                        // call the user supplied function.
                        onEnd.value;

                        // sanity check.
                        // Have we reached the end and no action
                        // has been taken? Panic and stop everything.
                        if (((length - this.now) <= 0) &&
                            (startTime > 0)
                           ) {
                            this.privateStop( this.now );
                            break.value;
                        };
                    };
                };

                // determine the next soonest event
                if (events.size > 0) { 
                    nextEventTime = events[0][0];
                }{  
                    nextEventTime = inf;
                };

                // determine the waiting time from that
                step = min(nextEventTime, nextTimeSlider) - now;

                // exit/unlock semaphore
                semaphore.signal;

                // do the waiting
                if (step > 0.0){
                    step = min( max(0.005, step), 0.25);
                    step.wait;
                };

                // enter/lock semaphore
                semaphore.wait;
            }}; // loop

            // exiting. semaphore is still locked.
            // notify status "stopped", and the time of stopping.
            playStatus = false;
            playLastStopped = this.clockTime;
            
            // exit/unlock semaphore
            semaphore.signal;

            // goodbye.
        }; // playRoutine

    } // constructor()

    // defers a given function only if doDefer is true. Used for
    // sending GUI signals.
    defer { |func|
        if (doDefer){
            func.defer;
        }{
            func.value;
        }
    }

    // what's the timing base we use?
    clockTime {
        ^thisThread.seconds;
    }

    // the core of the play function without the semaphore stuff
    // don't use unless within a semaphore.wait ... semaphore.signal block.
    privatePlay {
        // Let's give the onPlay() function a head start.
        startTime = this.clockTime + playLatency;
        server.makeBundle(playLatency, {
            onPlay.value(startOffset);
        });

        playDoReschedule = true;
      
        playStatus = \playing;
      
        if (gui != nil){
            this.defer{
                gui.unblock;
                gui.setPlaying(true);
                gui.updateTimeGUI(startOffset);
            };
        };
    }

    // the core of the stop function without the semaphore stuff.
    // It is also called from the playRoutine itself.
    // don't use unless within a semaphore.wait ... semaphore.signal block.
    privateStop {|seekPos|
        startOffset = seekPos;
        startTime = -1;

        // call user supplied function
        onStop.value(startOffset);

        clients.do{|client|
            client.bang(startOffset);
        };

        if (gui != nil){
            this.defer{
                gui.unblock;
                gui.setPlaying(false);
                gui.updateTimeGUI(startOffset);
            };
        };
    }

    // the core of the seek function without the semaphore stuff.
    // don't use unless within a semaphore.wait ... semaphore.signal block.
    privateSeek {|seconds, dostop|
        startOffset = seconds;
        playDoReschedule = true;

        if (startTime > 0){
            if (dostop.not){
                // currently playing. Restart playing at different position,
                // giving the onSeek function a headstart.
                startTime = this.clockTime + seekLatency;

                server.makeBundle(seekLatency, {
                    onSeek.value(startOffset);
                });
            }{
                // we're playing but we need to stop (dostop is true)
                startTime = -1;
                if (gui != nil){
                    this.defer{ gui.setPlaying(false); };
                };

                // essentially, we're both seeking and stopping.  but its
                // cumbersome for users to call two callbacks in the same
                // place. Plus, onStop also gets the seeking position.
                onStop.value(startOffset);
            };
        }{
            // not playing. Just call it without timing involved.
            onSeek.value(startOffset);
        };

        clients.do{|client|
            client.seek(startOffset);
        };

        if (gui != nil){
            this.defer{
                gui.unblock;
                gui.updateTimeGUI(startOffset);

                if (doRecord){
                    gui.setRecording(1);
                };
            };
        };
    }


    // the button action that signals readyness for recording.
    // It makes the record button orange.
    enableRecording{
        fork{
            semaphore.wait;
            doRecord = true;
            if (gui != nil){
                this.defer{
                    gui.setRecording(1);
                };
            };
            semaphore.signal;
        };
    }

    // the button action to switch off recording.
    stopRecording{
        fork{
            semaphore.wait;
            doRecord = false;
            clients.do{|client|
                client.stopRecording;
            };
            semaphore.signal;
            if (gui != nil){
                this.defer{
                    gui.setRecording(0);
                };
            };
        };
    }

    // message function called from AutomationClient as soon as it was first
    // modified in recording mode. It makes the record button red.
    clientStartsRecordingMsg {
        if (gui != nil){
            this.defer{
                gui.setRecording(2);
            };
        };
    }


    // private,
    // don't use unless within a semaphore.wait ... semaphore.signal block.
    addClient {|autoClient|
        block{|break|
            clients.do{|client|
                if (client.name == autoClient.name){
                    ("Automation: WARNING! DUPLICATE AutomationClient.name `" ++ client.name ++ "', SAVING WILL FAIL!").postln;
                    break.value;
                };
            };
        };

        clients.add(autoClient);
        if (verbose){
            ("Automation: Added client `" ++ autoClient.name ++ "'").postln;
        };
    }

}


