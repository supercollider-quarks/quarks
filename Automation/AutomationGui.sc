/*
   Automation is a SuperCollider Quark.
   (c) 2009 Neels J. Hofmeyr <neels@hofmeyr.de>, GNU GPL v3 or later.

   Automation allows to record and playback live changes made to
   other GUI elements in particular, or anything else you wish to
   setup. 

   This is AutomationGui, the class of the Automation Quark that provides
   the transport control GUI to interface with the Automation class.

   This can be setup automatically by calling Automation.gui.
   See: Automation
*/

AutomationGui {
    var <>automation,
        <win, bounds,
        playBtn, rewindBtn, recordBtn, snapshotBtn, saveBtn, loadBtn,
        timeSlider, timeNumberBox,
        doUpdateTimeSlider = true,
        doUpdateTimeNumber = true;

    /*
     Description of Parameters
     
     * automation
         The Automation instance this is the GUI of.

     * win
         The window that the transport control GUI was put in.
         This is either passed into or automatically created
         by the constructor.
         
     */


    /* automation: The Automation instance this is the GUI of.
     * win: If nil, opens a new window. Otherwise the
     *      given GUI.window is used to show the transport GUI.
     * bounds: A Rect(x,y,w,h) for size and position of the transport
     *      GUI.
     *      If bounds==nil, it will take up the whole window.
     *      If win==nil, these bounds give the size of the new window.
     *      If both win and bounds are nil, defaults are used.
     */
    *new { |automation, win=nil, bounds=nil|
        ^super.new.constructor(automation, win, bounds);
    }



    // administers saving of all clients, first opening a directory
    // dialog to choose a location.
    saveDialog {|preset=nil|
        if (preset == nil){
            preset = automation.presetDir;
        };

        automation.defer{
            this.chooseDirectoryDialog(
                title:"SAVE: select a directory name",
                preset:preset,
                onSuccess:{|dir|
                    automation.save(dir);
                });
        };
    }



    // administers loading of all clients, first opening a directory
    // dialog to choose a location.
    loadDialog {|preset=nil|
        if (preset == nil){
            preset = automation.presetDir;
        };

        automation.defer{
            this.chooseDirectoryDialog(
                title:"LOAD: enter a directory name",
                preset:preset,
                onSuccess:{|dir|
                    automation.load(dir);
                });
        };
    }


    setPlaying {|val=true|
        if (val){
            playBtn.value = 1;
        }{
            playBtn.value = 0;
        };
    }

    /* val == 0 : recording is off (grey)
     * val == 1 : recording is ready (orange)
     * val == 2 : recording is really happening (red)
     */
    setRecording {|val=1|
        if ((val == 1) || (val == 2)){
            recordBtn.value = val;
        }{
            recordBtn.value = 0;
        };
    }


    // internal constructor function
    constructor { |iautomation, iwin, ibounds|
        // `bfoo' means "bounds for the GUI element foo":
        var bplay, brew, brec, bsnap, bsave, bload, bslider, bnr;

        // evaluate input args

        automation = iautomation;
        automation.gui = this;

        win = iwin;
        bounds = ibounds;

        // setting up the GUI elements of the transport control.

        // did the caller supply a window?
        if (win == nil){
            // no. make one that suits me.
            // if no bounds supplied, invent some:
            if (bounds == nil){
                bounds = Rect(0, 0, 450, 25);
            };

            win = GUI.window.new("Automation Control", bounds);
            win.front;

            // if the window is created by me, take all space
            // in the window.
            bounds = nil;
        };

        // simple hack. If the window class' name starts with a 
        // J as in JSCWindow, we don't need to defer GUI signals.
        // see automation.defer().
        if (("" ++ win.class)[0] == $J){
            automation.doDefer = false;
        }{
            automation.doDefer = true;
        };

        // did the caller supply a bounding rectangle?
        if (bounds == nil){
            // no. take up the whole window.
            bounds = Rect(0, 0, win.bounds.width, win.bounds.height);
        };

        // now the buttons and sliders...
        // NOTE: If you add new control elements, be sure to also
        //       add them in the dock() function that checks for
        //       own GUI elements.

        bplay = bounds.copy;
        if (bounds.width > bounds.height) {
            bplay.width = min(bplay.height, 0.1 * bounds.width);
        }{
            bplay.height = min(bplay.width, 0.1 * bounds.height);
        };
        playBtn = GUI.button.new( win, bplay );
        playBtn.states = [[ ">", Color.blue, Color.grey ],
                        [ "||", Color.white, Color.blue ]];
        playBtn.action = {
           if (playBtn.value == 1) {
               automation.play;
           }{
               automation.stop;
           };
        };

        brew = bounds.copy;
        if (bounds.width > bounds.height) {
            brew.left = bplay.left + bplay.width;
            brew.width = min(brew.height, 0.1 * bounds.width);
        }{
            brew.top = bplay.top + bplay.height;
            brew.height = min(brew.width, 0.1 * bounds.height);
        };
        rewindBtn = GUI.button.new( win, brew );
        rewindBtn.states = [[ "<<", Color.blue, Color.grey ]];
        rewindBtn.action = {
            automation.seek;
        };

        brec = bounds.copy;
        if (bounds.width > bounds.height) {
            brec.left = brew.left + brew.width;
            brec.width = min(brec.height, 0.1 * bounds.width);
        }{
            brec.top = brew.top + brew.height;
            brec.height = min(brec.width, 0.1 * bounds.height);
        };
        recordBtn = GUI.button.new( win, brec );
        recordBtn.states = [["O", Color.red, Color.grey]
                           ,["O", Color.grey, Color.new255(255, 165, 0)]
                           ,["O", Color.white, Color.red]];

        recordBtn.action = {
           if (recordBtn.value == 1) {
               automation.enableRecording;
           }{
               automation.stopRecording;
           };
        };

        bsnap = bounds.copy;
        if (bounds.width > bounds.height) {
            bsnap.left = brec.left + brec.width;
            bsnap.width = min(bsnap.height, 0.1 * bounds.width);
        }{
            bsnap.top = brec.top + brec.height;
            bsnap.height = min(bsnap.width, 0.1 * bounds.height);
        };
        snapshotBtn = GUI.button.new( win, bsnap );
        snapshotBtn.states = [["[ô]", Color.black, Color.grey],
                              ["[ô]", Color.white, Color.red]];

        snapshotBtn.action = {
            automation.snapshot;
            {
                snapshotBtn.value = 0;
            }.defer(0.2);
        };

        bsave = bounds.copy;
        if (bounds.width > bounds.height) {
            bsave.left = bsnap.left + bsnap.width;
            bsave.width = min(bsave.height, 0.1 * bounds.width);
        }{
            bsave.top = bsnap.top + bsnap.height;
            bsave.height = min(bsave.width, 0.1 * bounds.height);
        };
        saveBtn = GUI.button.new( win, bsave );
        saveBtn.states = [[ "^", Color.white, Color.grey ]];
        saveBtn.action = {
            this.saveDialog;
        };

        bload = bounds.copy;
        if (bounds.width > bounds.height) {
            bload.left = bsave.left + bsave.width;
            bload.width = min(bload.height, 0.1 * bounds.width);
        }{
            bload.top = bsave.top + bsave.height;
            bload.height = min(bload.width, 0.1 * bounds.height);
        };
        loadBtn = GUI.button.new( win, bload );
        loadBtn.states = [[ "...", Color.white, Color.grey ]];
        loadBtn.action = {
            this.loadDialog;
        };

        // number field goes to the end
        bnr = bounds.copy;
        if (bounds.width > bounds.height) {
            bnr.width = min(2 * bnr.height, 0.2 * bounds.width);
            bnr.left = bounds.left + bounds.width - bnr.width;
        }{
            bnr.height = min(bnr.width, 0.2 * bounds.height);
            bnr.top = bounds.top + bounds.height - bnr.height;
        };
        timeNumberBox = GUI.numberBox.new(win, bnr);
        timeNumberBox.value = 0.0;
        timeNumberBox.action = {
            automation.seek(max(0, timeNumberBox.value));
        };


        // slider goes in-between
        bslider = bounds.copy;
        if (bounds.width > bounds.height) {
            bslider.left = bload.left + bload.width;
            bslider.width = bnr.left - bslider.left;
        }{
            bslider.top = bload.top + bload.height;
            bslider.height = bnr.top - bslider.top;
        };
        timeSlider = GUI.slider.new(win, bslider);
        timeSlider.action = {
            automation.seek(timeSlider.value * automation.length);
        };


        // Some signals that prevent automatic updating of
        // controls while the user is busy modifying them.

        timeSlider.addAction({
            doUpdateTimeSlider = false;
        }, \mouseDownAction);

        timeSlider.addAction({
            doUpdateTimeSlider = true;
            automation.defer{this.updateTimeGUI(automation.now)};
        }, \mouseUpAction);
        
        timeNumberBox.addAction({|view, char, modifiers, unicode, keycode|
            if (keycode == 27) {
                doUpdateTimeNumber = true;
                automation.defer{ this.updateTimeGUI(automation.now) };
            }{
                doUpdateTimeNumber = false;
            };
        }, \keyDownAction);

        timeNumberBox.addAction({
            doUpdateTimeNumber = false;
        }, \mouseDownAction);


    } // constructor()

    // private function that ensures waking up from blocking
    // changes on the time slider or time numberbox.
    unblock {|number=true, slider=false|
        if (number) { doUpdateTimeNumber = true; };
        if (slider) { doUpdateTimeSlider = true; };
    }

    // update both time slider and the time number box.
    updateTimeGUI{|time|
        if (doUpdateTimeSlider){
            timeSlider.value = (time / automation.length);
        };
        if (doUpdateTimeNumber){
            timeNumberBox.value = floor(time);
        };
    }

    // opens a really simple dialog window to enter a string.
    chooseDirectoryDialog {|title="Select Directory",
                            onSuccess, onFailure=nil,
                            preset=nil, bounds=nil|
        automation.defer{
            var dwin, textField, success=false;
            if (bounds == nil) { bounds = Rect(100,300,300,30); };
            if (preset == nil) { preset = "HOME".getenv ++ "/automation/"; };
            dwin = GUI.window.new(title, bounds);
            dwin.onClose = {
                if (success.not){
                    onFailure.value(textField.value);
                };
            };
            textField = GUI.textField.new(dwin, Rect(0,0,bounds.width,bounds.height));
            textField.value = preset;
            textField.action = {
                success = true;
                onSuccess.value(textField.value);
                dwin.close;
            };
            dwin.front;
        };
    }
    

    isMyGuiElement {|guiElement|
        ^ (
             (guiElement == playBtn) ||
             (guiElement == rewindBtn) ||
             (guiElement == recordBtn) ||
             (guiElement == snapshotBtn) ||
             (guiElement == saveBtn) ||
             (guiElement == loadBtn) ||
             (guiElement == timeSlider) ||
             (guiElement == timeNumberBox)
          );
    }


}


