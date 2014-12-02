
SpatialHashing{

  var <sceneWidth, <sceneHeight, <cellSize;
  var <buckets, cols, rows;

  *new { arg sceneWidth = 10, sceneHeight = 10, cellSize = 0.5;
    ^super.newCopyArgs(sceneWidth, sceneHeight, cellSize).init
  }

  init { var numCells;

    cols= sceneWidth / cellSize;
    rows= sceneHeight / cellSize;
    numCells = cols * rows;
    buckets = List.newClear(numCells);

    for (0,numCells-1,
      { arg i; var list;
        //("pol"++i).postln;
        buckets.put(i, List.new);
      }
    )
  }

  register{arg object; //registers the object in the relevant cells
    var set; //use a set to discard duplicates
    set = this.findCells(object); //returns a set of cell id's
    set.do{arg item; buckets[item].add(object)};
  }

  unregister{ arg object;
    var bucketIDs;
    bucketIDs = this.findCells(object);
    bucketIDs.do{arg i;
      buckets[i].remove(object)
    };

  }

  getObjectsFromCellSet{ arg set, objectSet;
    objectSet = IdentitySet.new;
    set.do{arg cellID;
      this.getCell(cellID).do{arg i;
        objectSet.add(i)
      }
    };
    ^objectSet;
  }

  findCells{ arg object;
    var pos, rad, boundingBoxCorners;
    pos = object.position;
    rad = object.radius;
    boundingBoxCorners = this.findBoundingBoxCorners(pos,rad);
    ^this.getSetFromCorners(boundingBoxCorners);
  }

  getSetFromCorners{ arg boundingBoxCorners; var set;
    set = IdentitySet.new;
    boundingBoxCorners.do{ arg item, index;
      set.add(this.findBacket(boundingBoxCorners[index]));
    };
    ^set;
  }

  findBoundingBoxCorners{ arg position, radius;
    var minPoint, maxPoint;
    minPoint = [position[0] - radius, position[1] - radius];
    maxPoint = [position[0] + radius, position[1] + radius];
    ^[minPoint, [maxPoint[0], minPoint[1]], maxPoint, [minPoint[0], maxPoint[1]]];
  }

  findBacket{ arg pos;
    /* can  be optimised to remove the divisions! See hash optimisation paper */
    ^((pos[0]/cellSize).floor + ((pos[1]/cellSize).floor*cols)).asInteger;
  }

  getNearest{ arg object;
    var set = IdentitySet.new, bucketIDs;
    bucketIDs = this.findCells(object);
    bucketIDs.do{arg i;
      buckets[i].do{
        arg i; set.add(i)
      }
    };
    //return the set minus the main object
    ^set.remove(object);
  }

  getCell{arg index;
    ^buckets[index];
  }

  clearBuckets{
    buckets.do{arg i; i.clear}
  }

  /* my surely inefficient algorithm for assigning cells to a line. */
  /* It involves creating and projecting spherical objects along its length. */
  /* Hopefully not too costly since it is going to be called only once. */

  getCellsForLine{ arg line;
    var halfCellSize, objectDistance, unitsInLine, allCorners = List.new;

    halfCellSize = cellSize * 0.5;
    objectDistance = line.to - line.from;

    unitsInLine = objectDistance.norm/halfCellSize;

    /* gather helper objects every half a cell along the line */
    unitsInLine.do{
      arg i;
      var currentPlace, xStep, yStep, x, y, pointToObject;
      var destinationX, destinationY, boundingBoxCorners;

      destinationX = line.to[0];
      destinationY = line.to[1];

      currentPlace = halfCellSize*i;
      xStep = objectDistance[0]/unitsInLine;
      yStep =  objectDistance[1]/unitsInLine;

      x = destinationX - (  (destinationX - line.from[0] ) - ( xStep * i ) );
      y = destinationY - (  (destinationY - line.from[1] ) - ( yStep * i ) );
      boundingBoxCorners = this.findBoundingBoxCorners(RealVector2D[x,y], cellSize);
      allCorners.add(boundingBoxCorners);
    };
    ^this.getSetFromCorners(allCorners.flatten);
  }

 }
