/*
    FP Quark
    Copyright 2013 Miguel Negrão.

    FP Quark: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FP Quark is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with FP Quark.  If not, see <http://www.gnu.org/licenses/>.

*/
/*
In Haskell this would be

penShapeStep = PenLineTo Double | PenCurveTo Double cPoint1 cPoint2 | ...
render ::penShapeStep -> IO

*/
PenShapeStep {
    render{ this.subclassResponsibility(thisMethod) }
}

PenLineTo : PenShapeStep {
    var point;
    *new{ |p| ^super.newCopyArgs(p) }
    render { Pen.lineTo(point) }
}

PenCurveTo : PenShapeStep {
    var point, cPoint1, cPoint2;
    *new{ |point, cPoint1, cPoint2| ^super.newCopyArgs(point, cPoint1, cPoint2) }
    render { Pen.curveTo(point, cPoint1, cPoint2) }
}

PenQuadCurveTo : PenShapeStep {
    var point, cPoint;
    *new{ |point, cPoint| ^super.newCopyArgs(point, cPoint) }
    render { Pen.quadCurveTo(point, cPoint) }
}

PenArcTo : PenShapeStep {
    var point1, point2, radius;
    *new{ |point1, point2, radius| ^super.newCopyArgs(point1, point2, radius) }
    render { Pen.arcTo(point1, point2, radius) }
}

PenShape {
    render { this.subclassResponsibility(thisMethod) }
}

PenStepShape : PenShape {
    var startPoint, steps;
    *new{ |startPoint, steps| ^super.newCopyArgs(startPoint, steps) }

    *polygon { |points|
            ^PenStepShape(points[0], (points[1..]++points[0]).collect( PenLineTo(_) ))
    }

    render {
        Pen.moveTo(startPoint);
        steps.do( _.render );
    }
}

PenRect : PenShape {
    var rect;
    *new{ |rect| ^super.newCopyArgs(rect) }

    render { Pen.addRect(rect) }
}

PenOval : PenShape {
    var rect;
    *new{ |rect| ^super.newCopyArgs(rect) }

    render { Pen.addOval(rect) }
}

PenWedge : PenShape {
    var center, radius, startAngle, sweepLength;
    *new{ |center, radius, startAngle, sweepLength|
        ^super.newCopyArgs(center, radius, startAngle, sweepLength)
    }
    render { Pen.addWedge(center, radius, startAngle, sweepLength) }
}

PenAnnularWedge : PenShape {
    var center, innerRadius, outerRadius, startAngle, sweepLength;
    *new{ |center, innerRadius, outerRadius, startAngle, sweepLength|
        ^super.newCopyArgs(center, innerRadius, outerRadius, startAngle, sweepLength)
    }
    render { Pen.addWedge(center, innerRadius, outerRadius, startAngle, sweepLength) }
}


PenDrawedShapes {
    var <penShapes, <style, <fillColor, <strokeColor;
    *new{ |penShapes, style, fillColor, strokeColor| ^super.newCopyArgs(penShapes, style, fillColor, strokeColor) }
}

PenDrawing {
    var <penDrawedShapes; // [PenDrawedShapes]
    *new{ |penDrawedShapes| ^super.newCopyArgs(penDrawedShapes) }

    render {
        penDrawedShapes.do { |x|
            x.penShapes.do( _.render );
            switch( x.style )
                {\fill} {
                    Pen.fillColor = x.fillColor;
                    Pen.fill
                }
                {\stroke} {
                    Pen.strokeColor = x.strokeColor;
                    Pen.stroke
                }
                {\fillStroke } {
                    Pen.fillColor = x.fillColor;
                    Pen.strokeColor = x.strokeColor;
                    Pen.fillStroke;
                }
        };
        Unit
    }

    renderIO {
        IO{ this.render }
    }

    value {
        this.render
    }
}

+ UserView {

    setDrawing { |drawing|
        ^IO{ { this.drawFunc = drawing; this.refresh }.defer }
    }

}

+ Window {

    setDrawing { |drawing|
        ^IO{ { this.drawFunc = drawing; this.refresh }.defer }
    }

}
//etc