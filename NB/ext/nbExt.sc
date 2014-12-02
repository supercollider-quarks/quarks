+ NB {


   // by Andrea Valle
   dottedLine { arg x1, y1, x2, y2, pointsPerUnit = 0.15, w = 1, h = 1, bpp = 1;
       // if bpp == 1, points are drawn as ovals
       // if bpp == 0, points are drawn as rectangles
       // values in between 0 and 1 are the probability of a point being drawn as a rectangle or an oval
       // ... 0.3 is a 30% chance of an oval ... 0.75 is a 75% chance of an oval
       var length = ((x2-x1).squared + (y2 - y1).squared).sqrt;
       var density = pointsPerUnit * length;
       var xIncr = (x2-x1)/density;
       var yIncr = (y2-y1)/density;
       var dottedLine = "";
       var basicPointPrim;
             (density+1).do({ arg i ;
                 if( bpp.coin, {
               basicPointPrim = "oval"
           },{
               basicPointPrim = "rect"
           });
                     dottedLine =
               dottedLine
               ++
               (
                   this.point(
                       xIncr * i + x1,
                       yIncr * i + y1,
                       w,
                       h
                   )
               )
       })
   ^this.publish(dottedLine)
   }
     // building on dottedLine ...
   dottedLines {
       arg x1, y1, x2, y2, pointsPerUnit = 0.15, w = 1, h = 1, bpp = 1, thickness = 1, scale = 1, dev = 0;
       thickness.do({ arg i;
           this.dottedLine(
               x1 + (i * scale + dev.rand2),
               y1 + (i * scale + dev.rand2),
               x2 + (i * scale + dev.rand2),
               y2 + (i * scale + dev.rand2),
               pointsPerUnit,
               w,
               h,
               bpp
           )
       })
     }

}
