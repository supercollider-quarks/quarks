Automation is a SuperCollider Quark.
(c) 2009 Neels J. Hofmeyr <neels@hofmeyr.de>, GNU GPL v3 or later.

Automation allows to record and playback live changes made to
other GUI elements in particular, or anything else you wish to
setup. 

This is Automation, the main class of the Automation Quark.
This is what you instantiate to use Automation.

Automation has a number of subscribed AutomationClient
instances and delegates them, as well as plays back their actions.

Most GUIs you are already using can be docked to Automation without
changing any of your old code.

Automation provides a control GUI to start/stop/record/save/load the
Automation. How to use the Automation Control GUI:


Quick example:
   0) Install the Automation Quark and run below code.
      This should open a GUI window with buttons and sliders.
   1) Click '[ô]' (record a snapshot of all current slider levels)
   2) Click 'O' (enable live recording, goes orange)
   3) Click '>' (play button), the time slider on the top moves.
   4) Do crazy motion on any sliders (record them, record button goes red).
   5) Click '<<' (rewind button), sliders repeat recorded motion.
   6) Grab a moving slider (overwrite previous recording).
   7) Click 'O' (disable recording), playing continues.
   8) Click '<<' (rewind again), enjoy watching your recording.
      If you grab sliders now, it doesn't affect the recorded values.
   9) Click '||' to stop playing, if you wish.
  10) Click '^', enter a writable directory and hit Return.
      (current slider recordings are saved to that directory)
  11) Restart your computer ;)  (just for illustration)
  12) Relaunch below code, click '...', enter the same path as above and
      hit Return. (previous values are loaded from disk)
  13) Click '>' (play) and enjoy watching your previous slider motion.
  14) Go to the path as above and swap two files' names.
      Load again and observe that the sliders swap their motion. ;)


Details:

 You need at least one Automation class instance, where you register sliders
 (etc.) as desired. Every slider (etc.) is registered with a distinct name
 (for saving its recorded values to a file).

 By "sliders", I also mean buttons and number boxes, and any controllable
 things as defined by AutoClient. It doesn't necessarily have to be GUI
 elements.
 
 
 The Automation Control GUI, explained in detail:


 * Click the play button (marked '>') and the time slider starts moving.
   This starts the automation for all the clients.
   The play button hence becomes the stop (or pause) button (marked '||').


 * The rewind button '<<' seeks the beginning (zero seconds). If the
   rewind button is clicked during play, playing resumes from the start.
   If you were recording any sliders, though, they will stop recording
   and go back to being "ready" for recording (read on...).
   You can set control.doStopOnSeek = true to stop transport on each seek.


 * When you click the record button (marked 'O'), this tells all the sliders
   to be "ready" for recording. Sliders will only actually start recording
   as soon as you first adjust their value.

   If recording is enabled, the recording button becomes orange. As soon
   as any one slider is adjusted, the recording button becomes red, to
   show that something is actually recording.

   Recording, naturally, overwrites any previously recorded values
   within the time span of recording. The last value of the recording
   will remain up until another recorded value comes up, regardless.

   You can also record a slider's momentous value without playing.
   Seek a position, enable recording (orange) and then move a slider (red).
   Disable recording (grey). You have set one single value.

   If you move the time slider or rewind during recording,
   all sliders are reset to only being "ready" for recording (orange).
   Note that play/stop does effectively NOT change recording states,
   so, while recording, you can just pause the timing to grab
   a restless slider and calmly adjust it before resuming play.


 * The snapshot button (marked '[ô]') records an instant value
   of *all* subscribed sliders at the current time. That's useful, for
   example, to set an initial level for time zero for all sliders. Or you
   might have played around with some sliders and don't want to nudge every
   single one to record the current values.
   
   Snapshots work both while stopped, during playback and while recording.
   Taking a snaphot while recording will not start (or stop) prolonged
   recording for all sliders, it will just record one value per client.


 * The save button (marked '^') opens a small window, for you to enter
   the name of a directory. The different sliders' stored values will
   be saved in files within that directory, with corresponding
   names. Any directories will be created if missing
   (TODO: only works on systems with a '/' path separator).

   Files are never overwritten: If files already exist, they are
   renamed to remain as backup.

   Saving also works during play or recording. If you have to.


 * The load button (marked '...') opens a small window for you to
   enter a directory name that you previously saved controls in.
   Each subscribed slider will try to find a file with its name.
   Loading also works while playing or recording. Loading values
   stops any recording and resets to "ready" for recording.


 * Moving the time slider seeks automation control to that position.
   If you were recording any sliders, though, they will stop recording
   and go back to being "ready" for recording (record button goes orange).

   Moving the time slider affects the client GUIs both during play
   and when stopped.


 * There also is a time number box, which always displays the current
   control time in seconds. If you enter a new number and hit enter,
   Automation will seek to that position.



Pitfalls:

 * If a slider seems to take a random value at each rewind, it's
   probably because you haven't recorded a starting value for it.
   (The slider keeps its previous level, might seem "random".)
   Each recorded value acts from its time on up to either the next
   recorded value comes up, or to infinity if there is none.
   It doesn't act before its time.
   So, you need to stop transport, rewind, and
   a) put everything in its initial state and take a snapshot of
      everything ("[ô]" button), or
   b) enable recording ("O" button) and set each GUI element separately.
      Each one of them needs to be *changed* for its recording to catch
      on. So even if it's at the proper level already, wobble it.
   This allows to keep some sliders out of Automation control even
   though you automatically docked everything you could find.

 * You need to *first* set up a slider's action, and *then* dock it to
   Automation. Not the other way around.
   If you set a slider's action *after* it was docked, you have half
   un-docked it in a bad way. If you really need to change an action,
   set the respective AutoClient's action instead (an AutoClient takes
   the action off a slider and puts itself inbetween).
   You can find an AutoClient by name in the Automation.clients list.


// If not done already, select your GUI framework. Run this line:
GUI.cocoa;
// or this one:
GUI.swing; 
// (I use swing on GNU/Linux)


(
    // This code launches the simpler test window. It has the control GUI
    // and the controlled sliders in the same window.

    var win, button, slider, numberBox,
        control;

    // Any window will do...
    win = GUI.window.new("Automation Transport Interface", Rect(40, 400, 450, 130));

    slider = GUI.slider.new(win, Rect(25, 35, 25, 90));
    slider.action = { ("My own slider action says:" + slider.value).postln };

    button = GUI.button.new(win, Rect(90, 70, 50, 25));
    button.states = [
        ["off", Color.white, Color.black],
        ["on", Color.black, Color.white],
        ["standby", Color.yellow, Color.gray]
    ];
    button.action = { ("My own button action says:" + button.value).postln };

    numberBox = GUI.numberBox.new(win, Rect(180, 75, 50, 25));
    numberBox.value = 0;
    numberBox.action = { ("My own numberBox action says:" + numberBox.value).postln };

    win.front;

    // ...up to now it's just any old window. You could have written it
    // three years ago, for that matter. Let's add automation to it:

    // Place the control GUI in above window, and set the control's length
    // to 30 seconds (that's simply and only for the time slider).
    control = Automation(30).front(win, Rect(0, 0, 450, 25));

    // Upon reaching the end of the time slider, I'd like to stop and
    // rewind the control. Default is to add 20% to the time slider's max.
    control.onEnd = {
        control.stop;
        "control is stopped".postln;
        control.seek;
    };

    // All that's left is to hook up above slider, button and number box
    // with our Automation. Let's dock them with specific names:
    control.dock(button, "theButton");
    control.dock(slider, "theSlider");
    control.dock(numberBox, "theNumberBox");

    win.onClose = {
        // When the window was closed, stop control, but do not update the
        // (closed) GUI to avoid an error:
        control.quit;
        "control is stopped".postln;
    };

)


(

    // This code launches the larger demo window.

    var win,
        controlsize, controlwin, control,
        gimmickwin, gimmickbutton;


    // Any window will do...
    // Let's create our own GUI with a couple of sliders (horizontal and
    // vertical), buttons and number boxes.

    win = GUI.window.new("Some sliders and buttons", Rect(40, 40, 450, 320));

    3.do{|i|
        var slider;
        slider = GUI.slider.new(win, Rect(5, 25*i + 5, 440, 20));
        slider.action = { ("My horizontal slider" + (i + 1) + "says:" + slider.value).postln };
    };
    3.do{|i|
        var slider;
        slider = GUI.slider.new(win, Rect(25*i + 5, 90, 20, 220));
        slider.action = { ("My vertical slider" + (i + 1) + "says:" + slider.value).postln };
    };

    3.do{|x|
        5.do{|y|
            var button;
            button = GUI.button.new(win, Rect(55*x + 90, y * 25 + 100, 50, 20));
            button.states = [
                ["off", Color.white, Color.black],
                ["on", Color.black, Color.white],
                ["standby", Color.yellow, Color.gray]
            ];
            button.action = { ("My button" + (3*y + x + 1) + "says:" + button.value).postln };
        };
    };

    5.do{|i|
        var numberBox;
        numberBox = GUI.numberBox.new(win, Rect(270, i*25 + 100, 50, 20));
        numberBox.value = 0;
        numberBox.action = { ("My numberBox" + (i + 1) + "says:" + numberBox.value).postln };
    };

    win.front;

    // ...up to now it's just any old window. You could have written it
    // three years ago, for that matter. Let's add automation to it:

    // This control GUI is created in its own window, with specific
    // position and size. The window is accessible in `control.win'.
    control = Automation().front(bounds: Rect(40, 410, 450, 25));

    // All that's left is to hook up the sliders and buttons with
    // our Automation. Wow, let's do it automatically:

    control.findAndDock(win.view.children);
    // Note: The control elements will be auto-named by order of
    // appearance.
    // That means, altering a GUI and then loading a previously saved
    // automation will most probably load some or all values into the
    // wrong GUI elements, depending on whether their order changed.
    // (However, if you can always dock newer GUI elements after the
    // older ones, this won't be a problem.)
    // Note that the order of appearance is not necessarily the
    // visual order in the GUI.
    // So, for saving and loading, it is safer to explicitly name
    // each single GUI element that you want to control.
    // findAndDock will react to buttons, sliders and number boxes,
    // by parsing the class names. You can easily write your own
    // automatic docking function using win.view.children.
    // Take a look at the source code of findAndDock().


    // that's all that's needed. Start recording stuff and playing
    // it back. The rest is added detail.
    

    // Well, this time I choose to stop playback automatically whenever
    // I seek another position. Note that in the first example playing
    // always continues, e.g. after a rewind, because that is the default.
    // (This could be useful if you want to play a sound file along with
    // the control and you can't get seek to work properly. By setting
    // this to true you are fine with only onPlay and onStop implemented.)
    control.doStopOnSeek = true;

    // Let's set the time slider's max to 30 seconds.
    // (Default is 180, that's 3 minutes.)
    // Note: this value *only* affects the time slider, nothing else.
    // Recorded values are NOT interfered with based on this number,
    // it simply determines the scale of the time slider GUI element.
    control.length = 30;

    // You may schedule an action for when the slider hits the end.
    // The default behaviour is to add 20% to the time slider's max
    // when reaching the end of it, like this:
    //   control.onEnd = { control.length = control.length * 1.2; };
    // But instead, we could also, for example, stop at the end:
    //   control.onEnd = { control.stop; };
    // Or wrap back to the start:
    control.onEnd = { control.seek(0); };



    // Gimmick example: Let's add "our own" transport control button
    // that seeks somewhere random, acting "from outside".
    // I put it in another window to set it apart from the rest.
    gimmickwin = GUI.window.new("Gmk", Rect(510, 410, 150, 20));
    gimmickbutton = GUI.button.new(gimmickwin, Rect(25, 0, 100, 20));
    gimmickbutton.states = [ ["seek", Color.black, Color.green] ];
    gimmickbutton.action = {
        var to = 0.0 + control.length.rand; // make it a float
        ("seeking to" + to).postln;
        control.seek( to );
    };
    gimmickwin.front;

    // close everything if the main window is closed.
    win.onClose = {
        control.gui.win.close;
    };

    control.win.onClose = {
        // When the control window is closed, stop control, but do not
        // update the (closed) GUI to avoid an error:
        control.quit;
        "control is stopped".postln;
        // Also close our gimmick example.
        gimmickwin.close;
    };
)



// By the way, this is what would happen if you tried to subscribe
// an Automation's own transport control GUI elements as clients:
// nothing except some warnings in the server window.
// That means you can add an Automation to an existing window and
// automatically findAndDock all GUI elements in the window, without
// having fear of creating infinite feedback loops in the control GUI.
(
    c = Automation().front(bounds: Rect(40, 410, 450, 25));
    // This created an Automation in its own window, which is
    // accessible as c.gui.win.

    "trying to dock my own elements:".postln;
    // So lets dock all elements in the Automation's window ... not!
    c.findAndDock(c.gui.win.view.children);

    // And if not verbose, then no warnings are posted.
    "trying to dock my own elements again, this time non-verbosely:".postln;
    c.verbose = false;
    c.findAndDock(c.gui.win.view.children);
    "done.".postln;
)

