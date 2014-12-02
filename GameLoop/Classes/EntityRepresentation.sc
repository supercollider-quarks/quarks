EntityRepresentation { var repManager, <>collisionFunc;
  var <position, <radius, <speed, <entity, <attached = false;
  var <>type = 'sound';

  *new { arg repManager, collisionFunc;
    ^super.newCopyArgs(repManager, collisionFunc)
  }

  init{ var latency;
    /* initialize data */
    /* this.getData; */
    position = entity.position;
    radius = entity.radius;
    speed = entity.velocity.norm;

    collisionFunc = collisionFunc ?? {{}};

    //Do transformation in case there is a camera
    position = this.cameraTransform(entity) + entity.worldCenter;
  }

/* public  */

  update { arg entity, message;
    switch (message[0])
    {\preUpdate}
    { var transPosition;
      transPosition = this.cameraTransform(entity);
      this.preUpdate(entity, transPosition);
    }
    {\update}
    {this.getData}
    {\attach}
    {this.attach(entity) }
    {\remove}
    {this.remove;}
    {\detach}
    {this.detach(message)}
    {\collision}
    {this.collision(message)};
  }

  attach{ arg entity;
    if (attached.isNil or:{attached.not},
    { attached = true;
      this.storeEntity(entity);
      this.init; }
    );
  }

  add{
    this.addAll;
  }

  remove{
    repManager.remove(this);
    attached = false;
  }

/* private */

  detach{ arg message;
      if (attached and:{message[1] == this}, {attached = false; this.remove;});
  }

  cameraTransform{arg entity;
    if (Camera2D.active,
      {^(Camera2D.instance.applyTransformation(entity))},
      {^position}
    );
  }

  preUpdate{ arg entity, transposition;
    position = transposition + entity.worldCenter;
  }

  getData{
    position = entity.position - entity.worldCenter;
    radius = entity.radius;
    speed = entity.velocity.norm;
  }

  addAll{ arg delay = 0;
    if(entity.queuedForAddition,
      {this.addRepresentation},
      {this.addEntityAndRepresentation(delay)}
    );
  }

  addEntityAndRepresentation{ arg delay;
    entity.queuedForAddition_(true);
    Routine{
      if(delay.notNil) {delay.wait};
      if (entity.active.not){entity.add};
      this.addRepresentation;
    }.play;
  }

  addRepresentation{
      repManager.add(this);
  }

  storeEntity{ arg item;
    entity = item;
  }

  colliding{
    ^entity.colliding;
  }

  dt{
    ^entity.dt;
  }

  collision{ arg message;
    /* message should have a list at [1] with the colliding with entities*/
    collisionFunc.value(this, entity, message[1]);
  }

}
