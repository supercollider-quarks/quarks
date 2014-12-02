RepresentationManager{ var <repList;

  *new {
  ^super.new.init;
  }

  init{ repList = List.new;
  }

  /* Public */

  add{arg rep;
    repList.add(rep);
  }

  remove{ arg rep;
    repList.remove(rep);
  }

  clear{repList.clear;
  }

  numberOfActiveReps{
    ^repList.size;
  }

}
