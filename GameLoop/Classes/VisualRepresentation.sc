
VisualRepresentation : EntityRepresentation {

    var  >color, >collisionColor;
    var <penWidth = 1.5, <>shape = 0;

  *new { arg  repManager,  collisionFunc, color, collisionColor;
    ^super.new(repManager, collisionFunc)
          .color_(color)
          .collisionColor_(collisionColor);
  }

  init {

    super.init;
    collisionFunc = collisionFunc ?? {{}};
    type = 'visual';

    color = color ?? {Color.white};
    collisionColor = collisionColor ?? {Color.red};
    this.add;

  }

  /* Public */

  color { if(this.colliding, {^collisionColor },{^color})
  }

  draw{arg rect;
    case
    {shape == 0} {Pen.strokeOval(rect)}
    {shape == 1} {Pen.strokeRect(rect)};
  }
}

