( // GENERAL SETUP
  //~decoder = GameLoopDecoder.newStereo;
  ~decoder = GameLoopDecoder.binauralAmbIEM;
  ~gameloop = GameLoop(100, 100, 1).play(0.05);
  ~gameloop.makeEdgeWalls;
  ~gameloop.gui;

  //some more walls for tests
  a = Wall(RealVector2D[5,10], RealVector2D[13, 29]);
  b = Wall(RealVector2D[20,20], RealVector2D[40, 35]);
  ~gameloop.entManager.addWall(a);
  ~gameloop.entManager.addWall(b);
)
( // CAMERA SETUP
  ~camera = Camera2D(
    ~gameloop.world,
    position: ~gameloop.world.center,
    radius: 0.8
  );
  ~camera.collisionType_(\mobile);
  ~camrep =Camera2DRepresentation(
    ~gameloop.repManager,
    color: Color.white
  ).shape_(1);

  ~camera.attach(~camrep);
);
( // FORCEMANAGER TEST
  3.do{
    { arg wander, forceManager;

      ~entity = Vehicle(
        ~gameloop.world,
        position: RealVector2D[rrand(10.0, 70), rrand(10.0, 70)],
        velocity: RealVector2D[rrand(0.0, 1.2), rrand(0.0, 1.2)],
        radius: 0.7,
        maxSpeed: 2.6
      );

      ~entity.collisionType_(\mobile);

      //a sound rep
      ~rep = SoundRepresentation(
        ~gameloop.repManager,
        /* input:{BPF.ar(in:WhiteNoise.ar(0.2), freq: rrand(100, 19000.0), rq:0.3)} */
      );
      ~entity.attach(~rep);

      //and a visual representation
      ~visrep = VisualRepresentation(~gameloop.repManager, color: Color.green);
      /* ~visrep.collisionFunc = {arg rep, ent;}; */
      ~entity.attach(~visrep);


      ~entity.collisionFunc_({arg entity, entList;
        /* forceManager.addTemp(entity.velocity * (-40)); */
        /* NonPenetrationConstrain(entity,entList); */
      });

      forceManager = ForceManager.new;

      /* ~entity.add; */

      wander = Wander(~entity,
        wanderRadius:   3.2,
        wanderDistance: 4,
        wanderJitter:  2
      );

      forceManager.add({wander.value});

      ~entity.force_({ arg entity;
        //forceManager.list.postln;
        forceManager.sum;
      });
    }.value;
  }
)

( // INTERFACE CLARIFICATION
  GameLoopDecoder('AmbIEM', 'binaural');
  ~gameloop = GameLoop(20, 20, 1).play(0.05);
  ~gameloop.makeEdgeWalls;
  ~gameloop.gui;
)
(
  ~camera = CameraUnit(
    ~gameloop,
    position: RealVector2D[10, 10]
  );
)
(
  ~unit = SVUnit(
    ~gameloop,
    input: nil,
    position: RealVector2D[10,10],
    velocity: RealVector2D[0.5, 0.7],
  );
)

// -----------------------\
// ----------------------- \
// --                  ---  \
( //PROCESS CONTROL
  ~gameloop.clearEntities;
  ~gameloop.clearWalls;
  ~camera.remove(true)

  ~camera.goto(RealVector2D[20, 30])
  ~camera.reset;
  //clear and close everything
  ~gameloop.clear;

  ~gameloop.entManager.entList

  //clear the Decoder
  GameLoopDecoder.instance.clear

  //detach representations
  ~entity.detachAll;
)
// --                  ---  /
// ----------------------- /
// -----------------------/

( // WALL COLLISION TEST

  ~gameloop.clearEntities;

  ~entity = Vehicle(
    ~gameloop.world,
    position: RealVector2D[25, 19],
    velocity: RealVector2D[-1,1],
    radius: 0.4,
    maxSpeed: 3.6
  );

  ~entity.collisionType_(\mobile);
  ~rep = SoundRepresentation(
    ~gameloop.repManager,
    input:{BPF.ar(in:WhiteNoise.ar(0.2), freq: rrand(100, 19000.0), rq:0.3)}
  );

  ~entity.attach(~rep);
  //and a visual representation
  ~visrep = VisualRepresentation(~gameloop.repManager);
  /* ~visrep.collisionFunc = {arg rep, ent;}; */
  ~entity.attach(~visrep);

  ~entity.collisionFunc_({arg entity, entList;
    /* NonPenetrationConstrain(entity,entList); */
  });

)
( // WANDER TEST
    ~entity = Vehicle(
      ~gameloop.world,
      position: RealVector2D[rrand(20.0, 20), rrand(20.0, 20)],
      velocity: RealVector2D[0,0.1],
      radius: 0.4,
      maxSpeed: 2.6
    );

    ~rep = SoundRepresentation(
      ~gameloop.repManager,
      input:{BPF.ar(in:WhiteNoise.ar(0.2), freq: rrand(100, 19000.0), rq:0.3)}
    );

    ~entity.collisionType_(\mobile);
    ~entity.collisionFunc_({arg entity, entList;
      NonPenetrationConstrain(entity,entList);
    });

    ~entity.attach(~rep);
    //and a visual representation
    ~visrep = VisualRepresentation(~gameloop.repManager);
    ~entity.attach(~visrep);

    //SLIDERS TO DEBUG WANDER
    Slider(
      GameLoopGUI.instance.mainView.view,
      Rect(0, 200, 50, 200);
    ).action_({ arg i; ~wander.wanderRadius =  (20 * i.value).debug("Radius") });

    Slider(
      GameLoopGUI.instance.mainView.view,
      Rect(100, 200, 50, 200);
    ).action_({ arg i; ~wander.wanderDistance = (20 * i.value).debug("Distance") });

    Slider(
      GameLoopGUI.instance.mainView.view,
      Rect(200, 200, 50, 200);
    ).action_({ arg i; ~wander.wanderJitter = (100  * i.value).debug("Jitter") });


    ~wander = Wander(~entity,
        wanderRadius:   3.2,
        wanderDistance: 2,
        wanderJitter:  2
      );

    ~entity.force_({ arg entity;
      ~wander.value;
    });
)
( // ENTITY PATH
  { var forceManager;
    ~entity = Vehicle(
      ~gameloop.world,
      position: RealVector2D[rrand(8.0, 32), rrand(8.0, 32)],
      radius: 0.4,
      maxSpeed: 5.6
    );

    ~rep = SoundRepresentation(
      ~gameloop.repManager,
      input:{BPF.ar(in:WhiteNoise.ar(0.2), freq: rrand(100, 19000.0), rq:0.3)}
    );

    ~entity.collisionType_(\mobile);
    ~entity.collisionFunc_({arg entity, entList;
      NonPenetrationConstrain(entity,entList);
    });

    ~entity.attach(~rep);
    ~visrep = VisualRepresentation(~gameloop.repManager);
    ~entity.attach(~visrep);

    ~randForcePath = {var path, pathFollowing;
      path = Path(Array.fill(rrand(2.0, 38.0), {RealVector2D[rrand(5, 35.0), rrand(5.0, 35.0)]}), true);
      /* path = Path([RealVector2D[25, 15], RealVector2D[10, 10]], true); */
      pathFollowing = PathFollowing(~entity, path, 0.5);
      { arg entity; pathFollowing.value};
    };

    ~entity.force_(~randForcePath.value);

  }.value;
)
( // CAMERA TESTS
  ~camera.maxForce = 1000;
  ~camera.mass= 0.05;
  ~camera.maxSpeed = 10;
  ~camera.motionAmount = 30;
  ~camera.rotationAmount = 0.01pi;
  ~camera.friction = 0.9;
  ~camera.remove(true);
  ~camera.moveFwd(2);
  ~camera.moveBack(2);
  ~camera.rotateRight(0.4pi);
  ~camera.rotateLeft(0.4pi);
  ~camera.remove
  ~gameloop.resetCamera;
)
( // SWITCH SPACE TESTS
~gameloop.makeWalls;
~gameloop.switchSpace(20, 20, 0.5);
~gameloop.switchSpace(200, 200, 0.5);
~gameloop.switchSpace(40, 40, 1);
~gameloop.switchSpace(40, 30, 1);
~gameloop.switchSpace(80, 80, 0.5);
)
( // COLLISION TESTS
  ~entity1 = Vehicle(
    ~gameloop.world,
    position: RealVector2D[25, 20.5],
    radius: 0.4,
    maxSpeed: 10.0
  );

  ~entity2 = Vehicle(
    ~gameloop.world,
    position: RealVector2D[15, 20.5],
    radius: 0.4,
    maxSpeed: 10.0
  );

  ~entity1.collisionType_(\mobile);
  ~entity2.collisionType_(\mobile);

  ~visrep1 = VisualRepresentation(~gameloop.repManager);
  ~entity1.attach(~visrep1);
  ~visrep2 = VisualRepresentation(~gameloop.repManager);
  ~entity2.attach(~visrep2);

  ~soundRep1 = SoundRepresentation(
    ~gameloop.repManager,
    //collisionFunc: {arg entity, entList; entity.remove; entList.do{arg i; i.remove}}
  );
  ~entity1.attach(~soundRep1);

  ~soundRep2 = SoundRepresentation(
    ~gameloop.repManager,
    //collisionFunc: {arg entity, entList; entity.remove; entList.do{arg i; i.remove}}
  );
  ~entity2.attach(~soundRep2);

  ~entity1.force_({arg entity; Arrive(entity, RealVector2D[18, 20.5])});
  ~entity2.force_({arg entity; Arrive(entity, RealVector2D[22, 20.5])});

    ~randForcePath = {arg entity; var path;
    /* steering bahavior */
    path = Path(Array.fill(rrand(2.0, 38.0), {RealVector2D[rrand(5, 35.0), rrand(5.0, 35.0)]}), true);
    entity.force_(
      { arg entity;
        PathFollowing(entity,path, 0.5);
      });
    };

  ~randForcePath.value(~entity1);
  ~randForcePath.value(~entity2);
)
( // ASCII LOGO



                GGGGGGGGGGGGG                                                            LLLLLLLLLLL
             GGG.:::::::::::G                                                            L.::::::::L
           GG.:::::::::::::G                                                             L.::::::::L
          G.::::GGGGGGGG.:::G                                                            LL.::::::LL
         G.::::G       GGGGGG  aaaaaaaaaaaaa      mmmmmmm    mmmmmmm       eeeeeeeeeeee    L.::::L                  ooooooooooo      ooooooooooo   ppppp   ppppppppp
        G.::::G                a.:::::::::::a   mm.::::::m  m.::::::mm   ee.:::::::::::ee  L.::::L                oo.::::::::::oo  oo.::::::::::oo p.:::ppp.::::::::p
        G.::::G                aaaaaaaaa.::::a m.:::::::::mm.:::::::::m e.:::::eeeee.::::eeL.::::L               o.::::::::::::::oo.::::::::::::::oP.::::::::::::::::p
        G.::::G    GGGGGGGGGG           a.:::a m.:::::::::::::::::::::me.:::::e     e.::::eL.::::L               o.::::ooooo.::::oo.::::ooooo.::::opp.:::::ppppp.:::::p
        G.::::G    G.:::::::G    aaaaaaa.::::a m.::::mmm.:::::mmm.::::me.::::::eeeee.:::::eL.::::L               o.:::o     o.:::oo.:::o     o.:::o p.::::p     p.::::p
        G.::::G    GGGGG.:::G  aa.:::::::::::a m.:::m   m.:::m   m.:::me.::::::::::::::::e L.::::L               o.:::o     o.:::oo.:::o     o.:::o p.::::p     p.::::p
        G.::::G        G.:::G a.:::aaaa.:::::a m.:::m   m.:::m   m.:::me.:::::eeeeeeeeeee  L.::::L               o.:::o     o.:::oo.:::o     o.:::o p.::::p     p.::::p
         G.::::G       G.:::Ga.:::a    a.::::a m.:::m   m.:::m   m.:::me.::::::e           L.::::L         LLLLLLo.:::o     o.:::oo.:::o     o.:::o p.::::p    p.:::::p
          G.::::GGGGGGGG.:::Ga.:::a    a.::::a m.:::m   m.:::m   m.:::me.:::::::e        LL.::::::LLLLLLLLL.::::Lo.::::ooooo.::::oo.::::ooooo.::::o p.::::ppppp.::::::p
           GG.::::::::::::::Ga.::::aaaa.:::::a m.:::m   m.:::m   m.:::m e.:::::::eeeeeeeeL.:::::::::::::::::::::Lo.::::::::::::::oo.::::::::::::::o p.:::::::::::::::p
             GGG.:::::GGG.::G a.:::::::::aa.::am.:::m   m.:::m   m.:::m  ee.::::::::::::eL.:::::::::::::::::::::L oo.::::::::::oo  oo.::::::::::oo  p.:::::::::::::pp
                GGGGGG   GGGG  aaaaaaaaaa  aaaammmmmm   mmmmmm   mmmmmm    eeeeeeeeeeeeeeLLLLLLLLLLLLLLLLLLLLLLLL   ooooooooooo      ooooooooooo    p.:::::pppppppp
                                                                                                                                                    p.::::p
                                                                              - "Press D for Help" -                                                p.::::p
                                                                                                                                                   p.::::::p
                                                                                                                                                   p.::::::p
                                                                                                                                                   p.::::::p
                                                                                                                                                   ppppppppp
)
( // FORCE MANAGER TEST
  a = ForceManager();
  a.add(\test, { RealVector2D[0, 10]});
  a.add(\test1, { RealVector2D[10, 0]});
  a.sum(RealVector2D[2,0]).postln;
  a.sum;
  a.list;
)
( // WALL TESTS
  a = Wall(RealVector2D[5,10], RealVector2D[13, 29]);
  b = Wall(RealVector2D[20,20], RealVector2D[40, 35]);
  ~gameloop.entManager.addWall(a);
  ~gameloop.entManager.addWall(b);
  /* ~gameloop.entManager.removeWall(b); */
  /* ~gameloop.entManager.clearWalls; */

  /* c = ~gameloop.entManager.spatialIndex.getCellsForLine(b); */

  /* c.do{ arg item; ~gameloop.entManager.spatialIndex.getCell(item).postln } */
)
( // QUARKS
  Quarks.gui;
)
(
  PointToWorldSpace(
    RealVector2D[2, 3],
    RealVector2D[ 0.95020532808649, -0.3116245087859 ],
    RealVector2D[ 0.3116245087859, 0.95020532808649 ],
    RealVector2D[ 20.123526692651, 19.959488813858 ]
  );
)

