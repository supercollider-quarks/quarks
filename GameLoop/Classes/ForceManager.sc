
ForceManager { var <>forces;

  *new{ arg forces = [];
    forces = Dictionary.newFrom(forces);
    ^super.newCopyArgs(forces);
  }

  /* public */

  add{ arg forceFunction, key;
    key ?? {key = ("Force_" ++ ( forces.size + 1 )).asSymbol};
    forces.add(key -> forceFunction);
  }

  addTemp{ arg force;
    forces.add('tempForce' -> force);
  }

  clearTemp{
    forces.removeAt('tempForce');
  }

  remove{ arg key;
    forces.removeAt(key);
  }

  get{ arg key;
    ^forces.atFail(key, {0});
  }

  list{
    "\n=============================================".postln;

    ( "| Number of forces:" + forces.size.asString).postln;
    forces.keysValuesDo{ arg key, value;
      ( "|     " + key + " = " + value.value.asString).postln;
    };

    "============================================\n".postln;
  }

  sum{ arg entity,  addedForce = 0; var sum;
    sum = addedForce;
    forces.do{arg item;
      sum = sum + item.value(entity);
    };
    this.clearTemp;
    ^sum;
  }

}
