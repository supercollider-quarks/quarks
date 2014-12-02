
EntityManager {
       var <spatialIndex;
       var <freeList, <mobList, <staticList, <wallList;
       var <>dt;
       var <sceneWidth, <sceneHeight;
       var <currentCollisionList;

  *new { arg spatialIndex = SpatialHashing(20, 20, 0.5);
    ^super.newCopyArgs(spatialIndex).init
  }

  init{
    dt = 0.05; //20 FPS
    freeList = List.new;
    mobList = List.new;
    staticList = List.new;
    wallList = List.new;
    currentCollisionList = List.new;
    this.getDimensionsFromIndex;
  }

  /* public */

  update{
    this.collisionResolution;
    this.unregisterIndex;
    this.updateEntities;
    this.registerIndex;
    this.collisionCheck;
  }

  clearEntities{
    [freeList.copy, mobList.copy, staticList.copy].flat.do{arg i; i.remove};
  }

  clear {
    this.clearEntities;
    this.clearWalls;
  }

  add{ arg entity;
    switch (entity.collisionType)
    {\free} {freeList.add(entity)}
    {\mobile} {mobList.add(entity); spatialIndex.register(entity)}
    {\static} {staticList.add(entity); spatialIndex.register(entity)};
  }

  remove{ arg entity;
    switch (entity.collisionType)
    {\free} {freeList.remove(entity)}
    {\mobile} {mobList.remove(entity); spatialIndex.unregister(entity)}
    {\static} {staticList.remove(entity); spatialIndex.unregister(entity)};
  }

  addWall{ arg wall; var surroundingCells;
    surroundingCells = spatialIndex.getCellsForLine(wall);
    wallList.add([ wall, surroundingCells ]);
  }

  removeWall{ arg wall; var index;
    index = wallList.detectIndex({arg i; i[0] == wall});
    wallList.removeAt(index);
  }

  clearWalls{ wallList.clear;
  }

  numberOfActiveEntities{
    ^(freeList.size + mobList.size + staticList.size)
  }

  center{
    ^RealVector2D[sceneWidth * 0.5, sceneHeight*0.5];
  }

  entList{
    ^[freeList.copy, mobList.copy, staticList.copy].flat;
  }

  /* private */

  newIndex{ arg newIndex;
    spatialIndex = newIndex;
    this.getDimensionsFromIndex;
  }

  getDimensionsFromIndex{
    sceneWidth = spatialIndex.sceneWidth;
    sceneHeight = spatialIndex.sceneHeight;
  }

  updateEntities{
      freeList.do{arg i; i.update};
      mobList.do{arg i; i.update};
      staticList.do{arg i; i.update};
  }

  unregisterIndex {
        mobList.do{arg i;
          spatialIndex.unregister(i);
        };
  }

  registerIndex {
        mobList.do{arg i;
          spatialIndex.register(i);
        };
  }

  collisionCheck{
    this.collisionCheckForMobile;
    this.collisionCheckForStatic;
    this.collisionCheckForWalls;
  }

  collisionCheckForMobile{
    mobList.do{ arg entity; var nearest;
      nearest = spatialIndex.getNearest(entity);
      this.doForPotentialCollisions(entity, nearest);
    };
  }

  collisionCheckForStatic{
    staticList.do{ arg entity; var nearest;
        nearest = spatialIndex.getNearest(entity);
        nearest = this.removeStaticEntitiesFromSet(nearest);
        this.doForPotentialCollisions(entity, nearest);
    };
  }

  doForPotentialCollisions{ arg entity, nearest;
    if(nearest.size>0,
      { this.checkForCollisionsWithObjects(entity, nearest)}
    );
  }

  checkForCollisionsWithObjects{ arg entity, potentiallyCollidingObjects; var collidingWith;
    collidingWith = this.collectCollidingObjects(entity, potentiallyCollidingObjects);
    if(collidingWith.size != 0,
      {currentCollisionList.add([entity, collidingWith])} //form = [entity, [ListofCollidingWithEntities]]
    );
  }

  collectCollidingObjects{ arg entity, potentiallyCollidingObjects; var collidingWith;
    collidingWith = List.new;
    potentiallyCollidingObjects.do{arg object;
      if(this.circlesCollide(entity, object)) {collidingWith = collidingWith.add( object)};
    };
    ^collidingWith;
  }

  collisionCheckForWalls{
    wallList.do{arg item; var wall, cells, potentialCollidingEntities;
      wall = item[0];
      cells = item[1];
      potentialCollidingEntities = spatialIndex.getObjectsFromCellSet(cells);
      potentialCollidingEntities = this.removeStaticEntitiesFromSet(potentialCollidingEntities);
      potentialCollidingEntities.do{arg entity; var offset;
        offset = this.checkEntityWallCollision(entity, wall);
        if(offset != 0,
          {currentCollisionList.add([entity, wall, offset])}
        );
      }
    }
  }

  removeStaticEntitiesFromSet{ arg set;
    set.removeAllSuchThat({arg item; item.collisionType == \static});
    ^set;
  }

  circlesCollide{ arg cA, cB; //circleA circleB
          var r1, r2, dx, dy;
          var a;
      r1 = cA.radius;
      r2 = cB.radius;
      a = (r1+r2) * (r1+r2);
      dx = cA.position[0] - cB.position[0];//distance of x points
      dy = cA.position[1] - cB.position[1];//distance of y points

      if (a > ((dx*dx) + (dy*dy)),
        {^true} ,
        {^false}
      );

  }

  checkEntityWallCollision{ arg entity, wall;
    var closest, distv, distvNorm, circpos, circrad, offset;

    circpos = entity.position;
    circrad = entity.radius;

    closest = wall.closestPointOnWall(circpos);
    distv = circpos - closest;
    distvNorm = distv.norm;

    if ( distvNorm > circrad) {
      /* ^RealVector2D[0, 0] */
      ^0
    };

    if ( distvNorm <= 0) {
      "Circle's center is exactly on segment".error;
    };

    offset = distv / distvNorm * (circrad - distvNorm);
    ^offset;
  }

  clearCollisionStateForAll{
    mobList.do{arg i; i.colliding_(false)};
    staticList.do{arg i; i.colliding_(false)};
  }

  collisionResolution{
    this.clearCollisionStateForAll;
    currentCollisionList.do{arg i;
      i[0].collision(i[1], i[2]); //entityColliding.collision(collidingWith, additionalInfo)
    };
    currentCollisionList.clear;
  }

}

