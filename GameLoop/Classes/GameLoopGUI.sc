
GameLoopGUI{
       classvar <instance;
       var <gameloop, <entManager, <repManager;
       var dimensions, gridSize, cellSize, <mainView, mainBounds, w = 175, h = 335;
       var leftRotationRoutine, rightRotationRoutine, fwdRotationRoutine, backRotationRoutine;
       var visualiser;
       var repsListViewWindow, repsListView, previousSelectionRepresentation, previousSelectionRepColor;

  *new{ arg gameloop;
      if(instance.isNil,
        { ^super.newCopyArgs(gameloop).init },
        {"There is already an active instance of GameLoopGUI".error }
      );
  }

  init{
    mainBounds = Rect(GUI.window.screenBounds.width*0.4, GUI.window.screenBounds.height*0.45, w, h);
    entManager = gameloop.entManager;
    repManager = gameloop.repManager;
    visualiser = GameLoopVisualiser(entManager,repManager);
    instance = this;

    CmdPeriod.add({this.clear});
    this.initCameraRoutines;
    this.gui;
  }

  /* Public */

  update { arg theChanged, message;
    switch (message[0])
    {\update}
    {visualiser.render}
    {\switchSpace}
    {visualiser.calculateMeterUnit};
  }

  gui{ var button;
    gameloop.addDependant(this);
    mainView ?? {
      this.createMainView;
      //||||||||||||||||||-]
      this.createQuitButton;
      this.createVisualiserButton;
      this.createFenceButton;
      this.createClearEntitiesButton;
      this.createShowVisualRepsButton;
      //||||||||||||||||||-]
      this.setWindowKeyActions;
    }
  }

  close {
    if(mainView.notNil, {mainView.close}, {"There is no view open for GameLoopGUI".error});
  }

  clear{
    if(mainView.notNil, {mainView.close});
    instance = nil;
    visualiser.clear;
    gameloop.removeDependant(this);
  }

  /* Private */

  createMainView{
     var run = true;
     mainView = Window("GameLoop", mainBounds, false);
     mainView.addFlowLayout(10@10, 10@10);
     mainView.view.background = Color.black;
     mainView.onClose = { run = false; mainView = nil; }; // stop the thread on close
     mainView.front;
     /* mainView.alwaysOnTop = true; */
     mainView.drawFunc = { };
  }

  createVisualiserButton{var button;
      button = this.createButton;
      this.assignActionToButton(button, {this.setVisualiserBounds; visualiser.gui}, {visualiser.close});
      this.setButtonStates(button, "Visualiser", "Close Visualiser");
      //this.decideStateOfVisualiserButton(button);
      /* button.valueAction = 1; */
      this.setVisualiserBounds(22);
      visualiser.gui;
      button.value = 1;
  }

  setVisualiserBounds{ arg fineTuneHack = 0;
    visualiser.bounds = Rect(mainView.bounds.left + w + 10, mainView.bounds.top - 65 - fineTuneHack, 400, 400);
  }

  createFenceButton{ var button;
      button = this.createButton;
      this.assignActionToButton(button, {gameloop.makeEdgeWalls}, {gameloop.clearEdgeWalls});
      this.setButtonStates(button, "Add Fence", "Remove Fence");
      if(gameloop.edgeWallsActive){button.value = 1};
  }

  createClearEntitiesButton{ var button;
      button = this.createButton;
      this.assignActionToButton(button, {gameloop.clearEntities; button.value = 0});
      this.setButtonStates(button, "Clear Entities", "");
  }

  createShowVisualRepsButton{ var button;
      button = this.createButton;
      this.assignActionToButton(button, {this.showRepresentationList}, {this.hideRepresentationList});
      this.setButtonStates(button, "Representations", "Hide  ===>");
      this.createRepsListView;
  }

  createRepsListView{
      repsListView = ListView(mainView, Rect(100,150,150,100)).canFocus_(false).visible_(false);
      repsListView.background = Color.clear;
      repsListView.stringColor = Color.gray;
      repsListView.selectedStringColor = Color.green;
      repsListView.hiliteColor = Color();
  }

  showRepresentationList{ var repArray;
    repArray = repManager.repList.select({arg rep; rep.type == \visual});
    /* repsListViewWindow = Window("Representations", Rect(-1350, 600, 150, 100), false); */
    repsListView.visible = true;
    repsListView.items_(repArray.collect({arg entity, index; (index + 1) + ": " + entity.class.asString}));
    repsListView.action_({arg view;
      previousSelectionRepresentation.tryPerform(\color_, previousSelectionRepColor);
      previousSelectionRepresentation = repArray[view.value];
      previousSelectionRepColor = previousSelectionRepresentation.color;
      previousSelectionRepresentation.color_(Color.green)
    });
  }

  hideRepresentationList{
    repsListView.visible = false;
    previousSelectionRepresentation.tryPerform(\color_, previousSelectionRepColor);
  }

  createQuitButton{ var button;
      button = this.createButton;
      this.assignActionToButton( button,
        {this.displayQuitButtonConfirmationDialog(button)}
      );
      this.setButtonStates(button, "Quit", "");
  }

  displayQuitButtonConfirmationDialog{ arg button;
    this.popUpWarning(
      "Are you sure you want to quit GameLoop",
      {this.clear; gameloop.clear; },
      {button.value = 0}
    );
  }

  createButton{
      ^Button(mainView, Rect(10,10,150,30)).canFocus_(false);
  }

   setButtonStates{ arg button, offStateString = "off", onStateString = "on";
     button.states_([
           [offStateString, Color.grey, Color.black],
           [onStateString, Color.green, Color.black]
     ]);
   }

  assignActionToButton{ arg button, onAction, offAction;
    button.action_({arg butt;
      switch (butt.value)
      {1}{onAction.value}
      {0}{offAction.value};
    });
  }

  decideStateOfVisualiserButton{ arg button;
    button.value = 1;
     /* if (visualiser.mainView != nil, */
     /*    {button.value = 1}, */
     /*    {button.value = 0} */
     /*  ); */
  }

  decideStateOfWallButton{ arg button;
     if (entManager.edgeWalls.size != 0,
        {button.value = 1},
        {button.value = 0}
      );
  }

  setWindowKeyActions{
      mainView.view.keyDownAction =
        {arg view, char, modifiers, unicode, keycode;
          switch (keycode)
          {126}
          {
            if(fwdRotationRoutine.isPlaying.not)
              {fwdRotationRoutine.reset.play};
          }
          {125}
          {
            if(backRotationRoutine.isPlaying.not)
              {backRotationRoutine.reset.play};
          }
          {123}
          {
            if(leftRotationRoutine.isPlaying.not)
              {leftRotationRoutine.reset.play};
          }
          {124}
          {
            if(rightRotationRoutine.isPlaying.not)
              {rightRotationRoutine.reset.play};
          }
        };

      mainView.view.keyUpAction =
        {arg view, char, modifiers, unicode, keycode;
          switch (keycode)
          {123}{leftRotationRoutine.stop}
          {124}{rightRotationRoutine.stop}
          {125}{backRotationRoutine.stop}
          {126}{fwdRotationRoutine.stop}
        };

  }

  initCameraRoutines{
    leftRotationRoutine = Routine{
      loop{
      Camera2D.instance.rotateLeft;
      0.05.wait;
      };
    };

    rightRotationRoutine = Routine{
      loop{
      Camera2D.instance.rotateRight;
      0.05.wait;
      };
    };

    fwdRotationRoutine = Routine{
      loop{
      Camera2D.instance.forceFwd;
      0.05.wait;
      };
    };

    backRotationRoutine = Routine{
      loop{
      Camera2D.instance.forceBack;
      0.05.wait;
      };
    };
  }

  popUpWarning {
    arg string, action, cancelAction;
    var dialog, buttonColor, buttonTextColor, destructiveButtonColor, destructiveButtonTextColor;

    dialog = Window.new("",Rect((mainView.bounds.left)+200, (mainView.bounds.top)+120, 280, 112), border: false).front;
    destructiveButtonColor = Color.new255(106,106,126);
    destructiveButtonTextColor = Color.white;
    buttonColor = Color.new255(106,106,126);
    buttonTextColor = Color.white;

    if(string.isNil, {string = "Are you sure?"});
    StaticText.new(dialog,Rect(30, 10, 220, 50))
    .string_(string)
    .align_(\left);
    Button.new(dialog,Rect(30, 70, 100, 20))
    .states_([ [ "Do it", destructiveButtonTextColor, destructiveButtonColor] ])
    .action_{|v| action.value; dialog.close};
    Button.new(dialog,Rect(150, 70, 100, 20))
    .states_([ [ "No thanks",buttonTextColor, buttonColor] ])
    .action_{|v| cancelAction.value; dialog.close};
  }

}


