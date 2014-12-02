
/*
 (C) 2013 Dionysis Athinaios
 GPLv2 - http://www.gnu.org/licenses/gpl-2.0.html
*/

GameLoop{

    classvar <instance;
    var <sceneWidth, <sceneHeight, <cellSize;
    var <entManager, <repManager;
    var <mainRoutine;
    var <edgeWalls, <edgeWallsActive = false;

  *new{ arg sceneWidth = 40, sceneHeight = 40, cellSize = 1;
      if(instance.isNil,
        {
        ^super.newCopyArgs(sceneWidth, sceneHeight, cellSize).init;
        },
        {"There is already an active instance of GameLoop".error;}
      );
  }

  init{
    instance   = this;
    entManager = EntityManager(SpatialHashing(sceneWidth, sceneHeight, cellSize));
    repManager = RepresentationManager.new;
    CmdPeriod.add({this.clear});

    edgeWalls = [];
  }

  /* public */

  switchSpace{ arg width = sceneWidth, height = sceneHeight, cell = cellSize;
    this.clearEntities;
    this.clearWalls;
    entManager.newIndex(SpatialHashing(width, height, cell));
    sceneWidth = width;
    sceneHeight = height;
    cellSize = cell;
    this.changed([\switchSpace]);
    this.fastResetCamera;
  }

  gui{
      GameLoopGUI(this);
  }

  guiClose{
      GameLoopGUI.instance.clear;
  }

  world{
    ^entManager
  }

  play{ arg rate;
    if (mainRoutine.isNil,
      {
        if(rate != nil, {entManager.dt = rate});
        mainRoutine = Routine{
          inf.do{
            entManager.update;
            this.changed([\update]);
            entManager.dt.wait;
            }
        }.play(TempoClock.default)
      },
      {
        mainRoutine.reset.play;
      }
    );
  }

  stop{
    mainRoutine.stop;
  }

  center{
    ^RealVector2D[sceneWidth * 0.5, sceneHeight*0.5];
  }

  dt{
    ^entManager.dt;
  }

  clear{
    mainRoutine.stop;
    entManager.clear;
    this.removeCamera;
    this.guiClose;
    instance = nil;
  }

  clearEntities{
    entManager.clearEntities;
  }

  clearWalls{
    entManager.clearWalls;
  }

  currentCamera{
    ^Camera2D.instance;
  }

  resetCamera{
    this.currentCamera.reset;
  }

  fastResetCamera{
    this.currentCamera.fastReset;
  }

  removeCamera{
    this.currentCamera.remove(true);
  }

  makeEdgeWalls{
    if(edgeWallsActive.not){
      edgeWalls = edgeWalls.add(Wall(RealVector2D[0, sceneHeight], RealVector2D[0, 0]));
      edgeWalls = edgeWalls.add(Wall(RealVector2D[0, 0], RealVector2D[sceneWidth, 0]));
      edgeWalls = edgeWalls.add(Wall(RealVector2D[sceneWidth, 0], RealVector2D[sceneWidth, sceneHeight]));
      edgeWalls = edgeWalls.add(Wall(RealVector2D[sceneWidth, sceneHeight], RealVector2D[0, sceneHeight]));
      edgeWalls.do{ arg wall; entManager.addWall(wall)};
      edgeWallsActive = true;
    }
  }

  clearEdgeWalls{
    edgeWalls.do{arg wall; entManager.removeWall(wall)};
    edgeWallsActive = false;
  }

  /* private */

}
