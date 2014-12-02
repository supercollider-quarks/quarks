SVUnit{ var gameloop, input, position, radius, mass, velocity, collisionType, maxSpeed;
        var <entity, <soundRep, visualRep;

  *new{ arg gameloop, input, position = RealVector2D[15, 15], radius = 0.8, mass = 1.0, velocity, collisionType = \free, maxSpeed = 100;
    ^super.newCopyArgs(gameloop, input, position, radius, mass, velocity, collisionType, maxSpeed).init;
  }

  init{
    this.makeEntity;
    this.makeSoundRep;
    this.makeVisualRep;
  }

  makeEntity{
    entity = Vehicle(
      gameloop.world,
      position: position,
      radius: radius,
      mass: mass,
      velocity: velocity,
      maxSpeed: maxSpeed
    );
    entity.collisionType_(collisionType);
  }

  makeSoundRep{ var rep;
    rep = SoundRepresentation(
      gameloop.repManager,
      input: input
    );
    entity.attach(rep);
  }

  makeVisualRep{ var rep;
    rep = VisualRepresentation(gameloop.repManager, color: Color.white);
    entity.attach(rep);
  }

  force_{ arg force;
    entity.force_(force)
  }

  remove{
    entity.remove;
  }

}
