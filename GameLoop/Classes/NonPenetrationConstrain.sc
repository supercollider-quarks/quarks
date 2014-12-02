
/* From AI by Example book p. 125 */

NonPenetrationConstrain{

  *new{ arg entity,entList;
    entList.do{ arg obstacle;
      case
      {obstacle.isKindOf(Wall)} {NonPenetrationConstrainWall(entity, obstacle, 0.05)}
      {obstacle.isKindOf(Entity)} {NonPenetrationConstrainEntity(entity, obstacle, 1)};
    };
  }

  *calculate{

    arg entityPosition, obstaclePosition, entityRadius, obstacleRadius = 1, amountOfSeperation = 1;
    var toEntity, distFromEachOther, amountOfOverlap;

    toEntity = entityPosition - obstaclePosition;
    distFromEachOther = toEntity.norm;
    amountOfOverlap = (obstacleRadius + entityRadius) - distFromEachOther;
    amountOfOverlap = amountOfOverlap * amountOfSeperation;

    if (amountOfOverlap >= 0,
      {
        entityPosition = entityPosition + ( (toEntity/distFromEachOther) * amountOfOverlap );
        ^entityPosition;
      },
      {
        ^entityPosition;
      }
    );
  }

}

NonPenetrationConstrainEntity : NonPenetrationConstrain{

  *new{ arg entity, collidingWith, amountOfSeperation = 1;
    entity.position = this.calculate(
      entity.position,
      collidingWith.position,
      entity.radius,
      collidingWith.radius,
      amountOfSeperation
    );
  }

}

NonPenetrationConstrainWall : NonPenetrationConstrain{

  *new{ arg entity, collidingWith, separationRadius = 1, amountOfSeperation = 1;
    entity.position = this.calculate(
      entity.position,
      collidingWith.closestPointOnWall(entity.position),
      entity.radius,
      separationRadius,
      amountOfSeperation
    );
  }

}
