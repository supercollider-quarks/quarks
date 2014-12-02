CameraUnit{ var gameloop, position;
            var <entity, <visualRep;

  *new{ arg gameloop, position;
    ^super.newCopyArgs(gameloop, position).init;
  }

  init{
    this.createEntity;
    this.createRepresentation;
  }

  createEntity{
    entity = Camera2D(
      gameloop.world,
      position: gameloop.world.center,
      radius: 0.8,
      collisionType: \mobile
    );
  }

  createRepresentation{
    visualRep =Camera2DRepresentation(
      gameloop.repManager,
      color: Color.white
    ).shape_(1);
    entity.attach(visualRep);
  }

}
