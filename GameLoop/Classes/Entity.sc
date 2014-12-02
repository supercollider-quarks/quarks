
Entity {
    var <>world, <>position, <>radius, <>mass, <>collisionType;
    var <>colliding, <active, <>queuedForAddition,  >collisionFunc;

  *new{ arg world, position = RealVector2D[15,15], radius = 1.0, mass = 1.0, collisionType = \free;
     ^super.newCopyArgs(world,
                        position,
                        radius,
                        mass,
                        collisionType
      ).init;
  }

  /* The init method is called in the subclass by using super.init. Using super.init
  in all the init methods assures that everything will be called. Of course remember
  to call init in the subclass new method to start the domino effect */

  init{
      position = position ?? {world.center};
      radius = radius ?? {1.0};
      mass = mass ?? {1.0};
      collisionType  = collisionType ?? {\free};
      colliding = false;
      active = false;
      queuedForAddition = false;
      collisionFunc = {};
  }

  /* public */

  attach{arg rep;
    this.addDependant(rep);
    this.changed([\attach])
  }

  detach{arg rep;
    this.changed([\detach, rep]);
    this.removeDependant(rep);
  }

  detachAll{ var list;
    list = this.dependants.asList;
    list.do{arg i; this.detach(i)};
  }

  add{
    world.add(this);
    active = true;
    queuedForAddition = false;
    this.changed([\add]);
  }

  remove {
    world.remove(this);
    active = false;
    this.changed([\remove]);
    this.releaseDependants;
  }

  collision { arg entitiesArray, msg;
          colliding = true;
          collisionFunc.value(this, entitiesArray, msg);
          this.changed([\collision, entitiesArray, msg]);
  }

  worldCenter{
    ^world.center;
  }

  /* private */

  dt{
    ^world.dt;
  }

}

MobileEntity : Entity {
     var <>velocity;
     var <>force = 0;

  *new{ arg world, position = RealVector2D[15,15],
            radius = 1.0, mass = 1.0, collisionType = \free,
            velocity = RealVector2D[0,0];
      ^super.new(world,
           position,
           radius,
           mass,
           collisionType
      ).velocity_(velocity).init;
  }

  init{
    super.init;
    velocity = velocity ?? {RealVector2D[0,0]};
  }

  /* public */

  update {
    /* calling update on the dependants ensure that we always get set
    by the integration of the last cycle */
    this.changed([\update]);
    this.integrateEuler(force.value(this));
    /* and here we update with the future value in case we want to
    use it for prediction as in the case of interpolation (lag) of sound
    units */
    this.changed([\preUpdate]);
  }

  /* private */

  integrateEuler{ arg force = 0;
    this.integrateVelocity(force);
    this.integratePosition(force);
  }

  integrateVelocity{ arg force = 0;
    velocity = velocity + ((force/mass) * this.dt);
  }

  integratePosition{ arg force = 0;
    position = position + (velocity *this.dt);
  }


}

Vehicle : MobileEntity {
     var <>heading, <>side, <>maxSpeed, <>maxForce, <>maxTurnRate;

  *new{ arg world, position= RealVector2D[15,15], radius = 1.0, mass = 1.0, collisionType = \free,
            velocity = RealVector2D[0, 0],  maxSpeed = 100, maxForce = 40, heading = RealVector2D[0, 0],
            side = RealVector2D[0,0], maxTurnRate = 2;
      ^super.new(world,
           position,
           radius,
           mass,
           collisionType
      ).velocity_(velocity)
       .maxSpeed_(maxSpeed)
       .maxForce_(maxForce)
       .heading_(heading)
       .side_(side)
       .maxTurnRate_(maxTurnRate).init;
  }

  init{
    super.init;
    maxSpeed = maxSpeed ?? {100};
    maxForce = maxForce ?? {40};
    maxTurnRate = maxTurnRate ?? {2};
  }

  integrateEuler{ arg force = 0;
    this.integrateVelocity(force);
    velocity = velocity.limit(maxSpeed);
    this.integratePosition(force);
    // update the heading and side (only if velocity is greater than *from AI by example book*)
    if (velocity.magSq > 0.00000001)
      {
      heading = velocity.normalize;
      side = heading.perp;
      };
  }

}

