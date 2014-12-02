
GameLoopVisualiser{

       classvar <instance;
       var <>entManager, <>repManager, >bounds;
       var <mainView, infoString;
       var width = 400, height = 400;
       var <>meterInPixels;
       var leftRotationRoutine, rightRotationRoutine, fwdRotationRoutine, backRotationRoutine;
       var sceneWidth, sceneHeight;

  *new{ arg entManager, repManager, bounds;
      if(instance.isNil,
        {
        ^super.newCopyArgs(entManager, repManager, bounds).init;
        },
        {"There is already an active instance of GameLoopVisualiser".error;}
      );
  }

  init{

    if (bounds.isNil,
      { bounds = Rect(GUI.window.screenBounds.width*0.5, GUI.window.screenBounds.height*0.5, width, height); }
    );


    instance = this;
    CmdPeriod.add({this.clear});
    this.initCameraRoutines;
    this.calculateMeterUnit;

  }

  /* Public */

  gui{
    this.createMainView;
    this.setViewOptions;
    this.setDrawFunction;
    this.setWindowKeyActions;
  }

  render {
    if(mainView.notNil, {{ mainView.refresh }.defer});
  }

  close {
    if(mainView.notNil, {mainView.close}, {"There is no view open for GameLoopVisualiser".error});
  }

  clear{
    if(mainView.notNil, {mainView.close});
    instance = nil;
  }

  /* Private */

  calculateMeterUnit{
    sceneWidth = entManager.sceneWidth;
    sceneHeight = entManager.sceneHeight;
    if(sceneWidth >= sceneHeight,
      { meterInPixels = width/sceneWidth},
      { meterInPixels = height/sceneHeight }
    );
  }

  camera{
    ^Camera2D.instance;
  }

  createMainView{
    mainView ?? { var text;
      mainView = Window("Visualiser", bounds, scroll: false);
      infoString= StaticText(mainView, Rect(3, 3, 200, 20)).stringColor_(Color.grey);
    }
  }

  setViewOptions{
    mainView.view.background = Color.black;
    mainView.onClose = {mainView = nil; };
    mainView.front;
    /* mainView.alwaysOnTop = true; */
  }

  setDrawFunction{
    mainView.drawFunc = {
      infoString.string = this.getInfoString;
      this.drawEntities(repManager.repList);
      this.drawWalls;
      Pen.stroke;
    };
  }

  drawEntities{ arg repList; var obstacle;
    repList.size.do { arg index;
      obstacle = repList[index];
      if(obstacle.type == 'visual')
        {this.drawEntity(obstacle)};
    }
  }

  drawEntity{arg obstacle;
             var radiusInPixels, widthInPixels, obstacPos;
             var left, top;
             /* var h = 400, w = 400; */

    obstacPos = obstacle.position;
    radiusInPixels = obstacle.radius * meterInPixels;
    widthInPixels = radiusInPixels + radiusInPixels;

    left = (obstacPos[0]*meterInPixels)-radiusInPixels;
    top  = ((obstacPos[1]*meterInPixels).linlin(0, height, width, 0)) - radiusInPixels;

    if (top > -4 and:{top < 396} and:{left > -4} and:{left<396})
    {
      Pen.width = obstacle.penWidth;
      Pen.color = obstacle.color.alpha_(0.7);
      Pen.beginPath;

      obstacle.draw(Rect(left, top, widthInPixels, widthInPixels));
    };
  }

  drawWalls{
    entManager.wallList.do{arg i; var pointFrom, pointTo, from, to,  halfSceneDimensions;
      /* var sceneWidth, sceneHeight, */
      /* sceneWidth = entManager.sceneWidth; */
      /* sceneHeight = entManager.sceneHeight; */
      halfSceneDimensions = [sceneWidth, sceneHeight] * 0.5;
      i = i[0];
      from = i.from;
      to = i.to;
      if (Camera2D.active,
        {
        from = this.camera.translatePosition(from);
        to = this.camera.translatePosition(to);
        from = from + halfSceneDimensions;
        to = to + halfSceneDimensions;
        }
      );
      pointFrom = RealVector2D[from[0], sceneHeight - from[1]];
      pointTo = RealVector2D[to[0], sceneHeight - to[1]];
      pointFrom = pointFrom * meterInPixels;
      pointTo = pointTo * meterInPixels;
      Pen.color = Color.gray;
      Pen.line(pointFrom.asPoint, pointTo.asPoint);
    }
  }

  getInfoString{ var string;
    string =   "Ents: " + entManager.numberOfActiveEntities.asString +
             "- Reps: " + repManager.numberOfActiveReps.asString;
    ^string;
  }

  initCameraRoutines{
    leftRotationRoutine = Routine{
      loop{
      this.camera.rotateLeft;
      0.05.wait;
      };
    };

    rightRotationRoutine = Routine{
      loop{
      this.camera.rotateRight;
      0.05.wait;
      };
    };

    fwdRotationRoutine = Routine{
      loop{
      this.camera.forceFwd;
      0.05.wait;
      };
    };

    backRotationRoutine = Routine{
      loop{
      this.camera.forceBack;
      0.05.wait;
      };
    };
  }

  setWindowKeyActions{
      //Specific mainview setting and keyboard controls
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

}

