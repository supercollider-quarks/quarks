

MxFrameRateDevice {

    var <>func, <>forUnit, <lastValue;

    *new { arg func, forUnit;
        ^super.newCopyArgs(func, forUnit)
    }

    tick { arg time;
        lastValue = func.value(time);
        this.changed(lastValue);
        ^lastValue
    }
}
