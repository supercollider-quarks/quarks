
/* For implementation details refer to the book Game AI by example by Mat Buckland */

Seek { var entity, <>targetPos;
  *new{ arg entity, targetPos = RealVector2D[10,13];
    ^super.newCopyArgs(entity, targetPos);
  }

  value{ var desiredVelocity;
    desiredVelocity = targetPos - entity.position;
    desiredVelocity = desiredVelocity.normalize;
    desiredVelocity = desiredVelocity * entity.maxSpeed;
    ^(desiredVelocity - entity.velocity);
  }
}

Arrive { var entity, <>targetPos, <>deceleration, <>tweak;
  //Deceleration{slow = 3, normal = 2, fast = 1};
  *new{ arg entity, targetPos = RealVector2D[10,13], deceleration = 2, tweak = 0.3;
    ^super.newCopyArgs(entity,
      targetPos,
      deceleration,
      tweak
    );
  }

  value{ var desiredVelocity, toTarget, speed, dist;
    toTarget = targetPos - entity.position;
    dist = toTarget.norm;
    if ( dist > 0,
      {
        speed = dist / (deceleration * tweak);
        speed = speed.min(entity.maxSpeed);
        desiredVelocity = (toTarget*speed)/dist;
        ^(desiredVelocity - entity.velocity);
      },
      {
        ^RealVector2D[0,0];
      }
    );
  }
}

Wander {
  var entity, <>wanderRadius, <>wanderDistance, <>wanderJitter;
  var wanderTarget;

  *new { arg entity, wanderRadius, wanderDistance, wanderJitter;

    ^super.newCopyArgs(entity,
      wanderRadius,
      wanderDistance,
      wanderJitter
    ).init
  }

  init { var theta;
    theta = rrand(-1.0, 1.0) * 2pi;
    //create a vector to a target position on the wander circle
    wanderTarget = RealVector2D[wanderRadius * theta.cos, wanderRadius * theta.sin];
  }

  value{ var targetLocal, targetWorld;

    //first, add a small random vector to the target’s position
    wanderTarget = wanderTarget + RealVector2D[rrand(-1.0, 1.0)*wanderJitter, rrand(-1.0, 1.0)*wanderJitter];
    wanderTarget = wanderTarget.normalize;
    wanderTarget = wanderTarget * wanderRadius;

    //move the target into a position wanderDist in front of the agent (x is front)
    targetLocal = wanderTarget + RealVector2D[wanderDistance, 0];

    //project the target into world space
    targetWorld = PointToWorldSpace(targetLocal,￼entity.heading, entity.side, entity.position);
    /* targetWorld.debug("target world"); */

    //and steer toward it
    ^targetWorld - entity.position;
  }

}

PathFollowing{ 
  var entity, <>path, <>seekDistance;
  var arrive, seek;

  *new{ arg entity, path, seekDistance = 0.5;
    ^super.newCopyArgs(
      entity,
      path,
      seekDistance
    ).init;
  }

  init{
    arrive = Arrive(entity);
    seek = Seek(entity);
  }

  value{
             var wayPoint;

      wayPoint = path.wayPoint;
      arrive.targetPos = wayPoint;
      seek.targetPos = wayPoint;

      if (entity.position.distanceSq(wayPoint) < seekDistance) {path.setNextWayPoint};

      if (path.finished,
        {^arrive.value },
        {^seek.value }
      );
  }
}

Path{
  var <wayPoints, <>loop, curWayPoint = 0;

  *new { arg  wayPoints, loop = false;
  ^super.newCopyArgs(wayPoints, loop).init
  }

  init{
    //PathsManager.add(this)
  }

  setNextWayPoint{
    if (wayPoints[curWayPoint] == wayPoints.last,
        {if(loop, {curWayPoint = 0})},
        {curWayPoint = curWayPoint+1}
    );
  }

  finished{
    if(loop,
      {^false},
      {^wayPoints[curWayPoint] == wayPoints.last}
    );
  }

  wayPoint{ ^wayPoints[curWayPoint]
  }

}

PathsManager{ /* the manager is not currently used */
  classvar <paths;

  *initClass{
    paths = List.new;
  }

  *add{ arg path; paths = paths.add(path);
  }

  *clear{ paths.clear;
  }

}
