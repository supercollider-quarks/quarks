/*
  AutomationClient is part of Automation, a SuperCollider Quark.
  (c) 2009 Neels J. Hofmeyr <neels@hofmeyr.de>, GNU GPL v3 or later.

  AutomationClient acts as a docking node for the Automation class.
  Please first see Automation.

  This class can dock to any GUI element like a slider or a button, or
  anything else that satisfies below criteria, and make it subject to
  Automation's control:
  
  Live changes can be recorded, saved, loaded and replayed.

  These are the criteria for a controllable thing, e.g. a GUI element:
  
      controllableThing.value, controllableThing.value_
        getter and setter of a member variable of any kind.

      controllableThing.action, controllableThing.action_
        getter and setter for a function variable. That function is called
        whenever a new value needs to be propagated. This should be setup
        before docking, the function gets sucked into AutomationClient.

      and that by calling value_, action is not run implicitly.

  Note: by docking a GUI element, the GUI element's action is replaced by
  AutomationClient.internalAction, and its previous action is put in the
  AutomationClient instance. If you want to change the action *after*
  docking, you need to set the action in the AutomationClient instance, not
  in the GUI element.
*/


AutomationClient {
    var <>automation = nil,
        <>name = nil,
        <>action = nil,
        <>minTimeStep = 0.01,
        values = nil,
        playCursor = -1, recordCursor = -1,
        controllableThing = nil;

    *new {|controllableThing, automation, name|
        ^super.new.constructor(controllableThing, automation, name);
    }

    constructor {|icontrollableThing, iautomation, iname|
        controllableThing = icontrollableThing;
        automation = iautomation;
        name = iname;
        
        values = List.new;

        action = controllableThing.action;
        controllableThing.action = {|view| this.internalAction(view); };

        automation.addClient(this);
    }


    stopRecording {
        recordCursor = -1;
    }

    seek { |seconds|
        this.stopRecording;
        
        // optimize a rewind
        if (seconds <= 0){
            playCursor = -1;
        };

        this.bang(seconds);
    }

    value {
        ^controllableThing.value;
    }

    value_ {|val|
        controllableThing.value_(val);
        this.internalAction;
        ^val;
    }

    save {|dir|
        var filename, file, backupname;
        // add a trailing slash.
        // TODO: only works on systems with a '/' file separator.
        filename = dir;
        if (filename.size > 0){
            if (filename.at( filename.size - 1 ) != $/) {
                filename = filename ++ $/;
            };
        };
        // add my name to the dir with the trailing slash, as a filename
        filename = filename ++ name;
        
        // backup existing file?
        if (File.exists(filename)) {
            backupname = filename ++ ".backup_" ++ Date.getDate.stamp;
            while({File.exists(backupname)},{
                backupname = filename ++ "_" ++ 999.rand;
            });
            ("mv" + filename + backupname).systemCmd;
        };

        // now write it out.
        file = File(filename, "wb");
        values.do{|row|
            row.do{|item| file.putDouble(item); }
        };
        file.close;
        if (automation.verbose){
            ("Automation: Saved" + values.size + "values to `" ++ filename ++ "'").postln;
        };
    }

    load {|dir|
        var filename, file;

        // make sure we're not directly overwriting loaded values.
        this.stopRecording;

        // add a trailing slash.
        // TODO: only works on systems with a '/' file separator.
        filename = dir;
        if (filename.size > 0){
            if (filename.at( filename.size - 1 ) != $/) {
                filename = filename ++ "/";
            };
        };
        // add my name to the dir with the trailing slash, as a filename
        filename = filename ++ name;

        // read it in
        file = File(filename, "rb");
        if (file.isOpen.not) {
            ("Automation: FAILED to open `" ++ filename ++ "'").postln;
            ^false;
        };

        values.free;
        values = List.new;

        // a double is 8 bytes, and there's two doubles per value
        // (time and value).
        ((file.length div: 8) div: 2).do{
            values.add([max(0.0,file.getDouble), file.getDouble]);
        };

        file.close;
        
        if (values.size < 1){
            ("Automation: NO VALUES in `" ++ filename ++ "'").postln;
            ^false;
        };

        if (automation.verbose){
            ("Automation: Loaded" + values.size + "values from `" ++ filename ++ "'").postln;
        };
        ^true;
    }




    // Evaluate whether the internal event cursors should move and
    // update the client gui accordingly.
    // Return the absolute time of the next upcoming value.
    bang{|nowtime|
        var val;
        // adjust playCursor position
        if (playCursor > values.size){
            playCursor = values.size - 1;
        }{
            if (playCursor < 0){
                playCursor = -1;
            };
        };

        // move backward?
        while({ if (playCursor > 0) {
                    (nowtime < values[playCursor][0]) 
                }{
                    false
                };
              }, {
            playCursor = playCursor - 1;
        });

        // move forward?
        while({ if ((playCursor + 1) < values.size) {
                    (nowtime >= values[playCursor + 1][0])
                }{
                    false
                }
              }, {
            playCursor = playCursor + 1;
        });

        if (recordCursor < 0) {
            // we're in play mode. Set "slider"'s value.
            if (playCursor >= 0){
                val = values[playCursor][1];
                automation.defer{
                    if (val != controllableThing.value){
                            controllableThing.value_(val);
                            action.value(controllableThing);
                    };
                };
            };
        }{
            // we're in recording mode.
            // remove upcoming saved values that are after the
            // recording cursor and pass "now".
            if (playCursor > recordCursor) {
                (playCursor - recordCursor).do{
                    values.removeAt(playCursor);
                    playCursor = playCursor - 1;
                };
            };
        };

        // return the time of the next value coming up after this one.
        if ((playCursor + 1) >= values.size){
            ^inf;
        }{
            ^values[playCursor+1][0];
        };
    }


    // record a given time and value. You may pass a cursor at which
    // to continue recording (for internal calls).
    // Returns the new cursor index.
    record { |time, val, cursor=(-1)|
        var cursorTime, startedRecording;

        startedRecording = false;

        // if not recording yet, accurately determine the position.
        if (cursor < 0){
            startedRecording = true;
            cursor = playCursor;
            if ((cursor < 0) || (cursor >= values.size)){
                cursor = -1;
            };

            // move backward?
            while({ if (cursor >= 0){
                        (time < values[cursor][0])
                    }{  false  }
                  }, {
                cursor = cursor - 1;
            });
            // move forward?
            while({ if ((cursor + 1) < values.size){
                        (time >= values[cursor + 1][0])
                    }{  false  }
                  }, {
                cursor = cursor + 1;
            });
        };

        // record this value. But where to put it?
        if (cursor < 0) {
            // we're supposed to insert the item at the start.
            cursorTime = -inf; // make the next condition pass
            cursor = -1; // make sure it gets added at index 0
        }{
            // let's see when a new value would count as a separate
            // time step (at least minTimeStep later).
            cursorTime = values[cursor][0] + minTimeStep;
        };

        if (time > cursorTime){
            // the new value is well later than the current one.
            // add after the current one.
            cursor = cursor + 1;
            values.insert(cursor, [time, val]);
            // we've inserted a value, keep the playCursor stationary.
            if (playCursor >= cursor){ playCursor = playCursor + 1; };
        }{
            // the new value's time is very close to the current one. Replace!
            // Do not change the time though to avoid dragging this
            // value along in case of rapidly incoming updates.
            values[cursor][1] = val;
        };

        ^cursor;
    }


    // records the GUI's current value at the given time.
    // You may have to wrap this in a defer{ ... } (Mac).
    snapshot {|now|
        var cursor = recordCursor;
        cursor = this.record(now, controllableThing.value);
        if ((recordCursor >= 0) &&
            (cursor > recordCursor) &&
            (cursor <= playCursor)){
            // oh no, we're interfering with the recording
            // process. Let's fix it.
            if (cursor > (recordCursor + 1)){
                (cursor - (recordCursor + 1)).do{
                    values.removeAt(recordCursor);
                    playCursor = playCursor - 1;
                };
            };
            recordCursor = recordCursor + 1;
        };
    }


    // this is set up to be called upon an action by
    // the GUI element (e.g. slider)
    internalAction {|view|
        var now, val, startedRecording;

        // avoid negative values, these only show up when playLatency
        // results in a negative now.
        now = max(automation.now, 0.0);
        
        val = controllableThing.value;
        
        // call the action set by the user
        action.value(view);

        // now do the automation action
        if (automation.doRecord){
            startedRecording = (recordCursor < 0);
            recordCursor = this.record(now, val, recordCursor);

            if (startedRecording){
                automation.clientStartsRecordingMsg;
            };
        }{
            // Control's recording button is not pressed.
            // Make sure recording is disabled.
            recordCursor = -1;
        };
    }

}

