

ZoomCalc {

    var <>modelRange, <>displayRange, <zoomedRange;
    var displaySpec, zoomedSpec;

    *new { arg modelRange, displayRange;
        ^super.newCopyArgs(modelRange, displayRange).setZoom(*modelRange)
    }

    setZoom { arg from, to;
        zoomedRange = [from, to];
        this.init;
    }
    init {
        displaySpec = displayRange.asSpec;
        zoomedSpec = zoomedRange.asSpec;
    }

    modelToDisplay { arg value;
        var clipped, v, u;
        v = value.clip(*zoomedRange);
        clipped = v != value;
        if(clipped, { ^nil });
        u = zoomedSpec.unmap(v);
        ^displaySpec.map(u)
    }
    displayToModel { arg value;
        var u, v;
        u = displaySpec.unmap(value);
        v = zoomedSpec.map(u);
        ^v
    }
}
