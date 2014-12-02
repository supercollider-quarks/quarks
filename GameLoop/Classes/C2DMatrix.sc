/**
 * Desc:   2D Matrix class
 *
 * @author Petr (http://www.sallyx.org/)
 * translated to SC by Dionysis Athinaios
 */

C2DMatrix { var <matrix;

   *new{
     ^super.new.init;
   }

   init{
     /* matrix = Matrix.newClear(3,3); */
     matrix = Matrix.newIdentity(3);
   }
   //applies a 2D transformation matrix to a single Vector2D
   transformVector2Ds{arg vPoint; var tempX, tempY;

     tempX = (matrix[0,0] * vPoint.x) + (matrix[1,0] * vPoint.y) + (matrix[2,0]);
     tempY = (matrix[0,1] * vPoint.x) + (matrix[1,1] * vPoint.y) + (matrix[2,1]);

    ^RealVector2D[tempX, tempY]
   }
   //create a transformation matrix
   translate { arg x,y; var mat;
     mat = Matrix.newClear(3, 3);

     mat[0,0] = 1; mat[0,1] = 0; mat[0,2] = 0;

     mat[1,0] = 0; mat[1,1] = 1; mat[1,2] = 0;

     mat[2,0] = x; mat[2,1] = y; mat[2,2] = 1;

     /* matrix.debug("matrix in translate"); */
     /* mat.debug("mat in translate"); */
     //and multiply
     matrix = matrix * mat;
   }
   //create a scale matrix
   scale{arg xScale, yScale; var mat;
     mat = Matrix.newClear(3, 3);

     mat[0,0] = xScale; mat[0,1] = 0; mat[0,2] = 0;

     mat[1,0] = 0; mat[1,1] = yScale; mat[1,2] = 0;

     mat[2,0] = 0; mat[2,1] = 0; mat[2,2] = 1;

     //and multiply
     matrix = matrix * mat;
   }
   //create a rotation matrix
   rotateAngle{ arg rot; var sin, cos, mat;
     mat = Matrix.newClear(3, 3);

     sin  = rot.sin;
     cos = rot.cos;

     mat[0,0] = cos;  mat[0,1] = sin ; mat[0,2] = 0;
     mat[1,0] = (-1) * sin ; mat[1,1] = cos; mat[1,2] = 0;
     mat[2,0] = 0;    mat[2,1] = 0;   mat[2,2] = 1;

     //and multiply
     matrix = matrix * mat;
   }
   //create a rotation matrix from a 2D vector
   rotate{ arg fwd, side; var mat;
     mat = Matrix.newClear(3, 3);

     mat[0,0] = fwd[0];  mat[0,1] = fwd[1];  mat[0,2] = 0;
     mat[1,0] = side[0]; mat[1,1] = side[1]; mat[1,2] = 0;
     mat[2,0] = 0;       mat[2,1] = 0;       mat[2,2] = 1;

     //and multiply
     /* matrix.debug("matrix in rotate"); */
     /* mat.debug("mat in rotate"); */
     matrix = matrix * mat;
   }

}

PointToWorldSpace{ var point, agentHeading, agentSide, agentPosition;

  *new{ arg point, agentHeading, agentSide, agentPosition;
    ^super.newCopyArgs(point, agentHeading, agentSide, agentPosition).calculate;
  }

  calculate{ var matTransform;

      /* point.debug("point"); */
      /* agentHeading.debug("agentheading"); */
      /* agentSide.debug("agen side"); */
      /* agentPosition.debug("agent position"); */

      //create a transformation matrix
      matTransform = C2DMatrix.new;
      //rotate
      matTransform.rotate(agentHeading, agentSide);
      //and translate
      matTransform.translate(agentPosition[0], agentPosition[1]);
      //now transform the vertices
      ^matTransform.transformVector2Ds(point);
  }

}


//applies a 2D transformation matrix to a std::vector of Vector2Ds
/*
public void TransformVector2Ds(List<Vector2D> vPoint) {
  ListIterator<Vector2D> it = vPoint.listIterator();
  while (it.hasNext()) {
    Vector2D i = it.next();
    double tempX = (matrix[0,0] * i.x) + (matrix[1,0] * i.y) + (matrix[2,0]);
    double tempY = (matrix[0,1] * i.x) + (matrix[1,1] * i.y) + (matrix[2,1]);
    i.x = tempX;
    i.y = tempY;
  }
}
*/
