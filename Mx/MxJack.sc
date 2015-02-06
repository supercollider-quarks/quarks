

MxJack {

    *forSpec { arg spec, defArg;
        if(defArg.isKindOf(MxJack), {
            ^defArg
        });
        if(spec.isKindOf(AudioSpec), {
            ^MxArJack.new.value_(defArg ? 127)
        });
        // EnvSpec
        // SampleSpec
        // BusSpec
        // ScaleSpec
        // ArraySpec
        // StreamSpec
        if(spec.isKindOf(StreamSpec), {
            ^MxStreamJack(defArg ? spec.default, spec)
        });

        if(spec.isKindOf(TrigSpec), {
            ^MxTrJack(defArg ? spec.default, spec)
        });
        if(spec.isKindOf(StaticSpec), {
            ^NumberEditor(defArg ? spec.default, spec)
        });
        if(spec.isKindOf(NamedIntegersSpec), {
            ^spec.defaultControl(defArg)
        });
        if(spec.isKindOf(NoLagControlSpec), {
            ^MxKrJack(defArg ? spec.default, spec, nil)
        });
        if(spec.isKindOf(ControlSpec), {
            ^MxKrJack(defArg ? spec.default, spec)
        });
        ^defArg
    }
}


MxStreamJack : MxJack {

    var stream, <>spec;
    var <source, firstVal, lastVal;
    var <isConnected=false;

    *new { arg initialValue, spec;
        ^super.new.spec_(spec).value_(initialValue ? spec.default)
    }
    storeArgs {
        ^[this.value, spec]
    }
    source_ { arg v;
        source = v;
        if(v.notNil, {
            stream = v.asStream;
            firstVal = stream.next;
            lastVal = nil;
            isConnected = true;
        }, {
            // repeat last val till plugged into something new
            stream = lastVal.asStream;
            firstVal = stream.next;
            isConnected = false;
        });
    }
    value {
        ^lastVal ? firstVal ? spec.default
    }
    value_ { arg v;
        lastVal = v;
    }
    rate { ^\stream }
    asStream { ^this }
    next { arg inval;
        var v;
        if(firstVal.notNil, { // stream is set
            v = firstVal;
            firstVal = nil;
            lastVal = v;
            this.changed;
            ^v
        }, {
            if(stream.isNil, {
                ^this.value
            });
            lastVal = stream.next(inval);
            this.changed;
            ^lastVal
        })
    }
    reset {
        stream.reset;
        firstVal = lastVal = nil;
    }

    synthArg {
        ^this.value
    }
    addToSynthDef {  arg synthDef, name;
        synthDef.addIr(name, this.synthArg);
    }
    instrArgFromControl { arg control;
        ^control
    }
    guiClass { ^MxStreamJackGui }
}


MxControlJack : MxJack { // abstract

    var <value, <>spec;
    var <patchOut, <>isConnected=false;

    storeArgs {
        ^[value, spec]
    }
    value_ { arg v;
        value = v;
        this.changed;
    }
    setValueToBundle { arg v, bundle;
        bundle.addFunction({
            value = v;
        });
        patchOut.connectedTo.do { arg patchIn;
            bundle.add( patchIn.nodeControl.setMsg(v) );
        }
    }

    getNodeControlIndex { arg patchIn;
        ^patchIn.instVarAt('index')
    }
    readFromBusToBundle { arg bus, bundle;
        // does not work in Patch spawn because connectedTo does not happen till didSpawn
        // so this only works after already playing
        patchOut.connectedTo.do { arg patchIn;
            bundle.add( patchIn.nodeControl.node.mapMsg(this.getNodeControlIndex(patchIn.nodeControl), bus) );
        };
        bundle.addFunction({ isConnected = true });
    }
    stopReadFromBusToBundle { arg bundle;
        patchOut.connectedTo.do { arg patchIn;
            bundle.add( patchIn.nodeControl.node.mapMsg(this.getNodeControlIndex(patchIn.nodeControl), -1) );
        };
        bundle.addFunction({ isConnected = false });
    }
    stopToBundle { arg bundle;
        //bundle.addFunction({ patchOut.free; patchOut = nil; })
    }
    freeToBundle { arg bundle;
        bundle.addFunction({ patchOut.free; patchOut = nil; })
    }

    synthArg {
        ^value
    }
    addToSynthDef {  arg synthDef, name;
        synthDef.addKr(name, value);
    }
    instrArgFromControl { arg control;
        ^control
    }
    makePatchOut {
        patchOut = UpdatingScalarPatchOut(this, enabled: false);
    }
    connectToPatchIn { arg patchIn, needsValueSetNow = true;
        patchOut.connectTo(patchIn, needsValueSetNow);
    }
    rate { ^\control }
}


MxKrJack : MxControlJack {

    var <>lag=0.1;

    *new { arg value, spec, lag=0.1;
        ^super.newCopyArgs(value, spec, lag)
    }
    storeArgs {
        ^[value, spec, lag]
    }
    instrArgFromControl { arg control;
        // actually if its patched up to a kr on the server
        // then you don't want Lag
        // this assumes you are sending values from client
        if(lag.notNil and: {spec.isKindOf(NoLagControlSpec).not}, {
            ^Lag.kr(control, lag)
        }, {
            ^control
        })
    }
    guiClass { ^MxKrJackGui }
}


MxArJack : MxControlJack {

    /*
        value is the bus
    */

    var <>numChannels=2;

    *new { arg numChannels=2, bus=126;
        ^super.new.numChannels_(numChannels).value_(bus)
    }
    storeArgs {
        ^[numChannels]
    }

    bus_ { arg v;
        if(v.isNumber, {
            value = v;
        }, {
            value = v.index
        });
        this.changed;
    }
    addToSynthDef {  arg synthDef, name;
        synthDef.addKr(name, value);
    }
    instrArgFromControl { arg control;
        ^In.ar(control, numChannels)
    }

    readFromBusToBundle { arg bus, bundle;
        this.setValueToBundle(bus.index, bundle);
    }
    stopReadFromBusToBundle { arg bundle;
        this.setValueToBundle(126, bundle);
    }

    rate { ^\audio }

    guiClass { ^MxArJackGui }
}


MxIrJack : MxControlJack {

    *new { arg value, spec;
        ^super.newCopyArgs(value, spec)
    }
    addToSynthDef {  arg synthDef, name;
        synthDef.addIr(name, value);
    }
    makePatchOut {
        patchOut = ScalarPatchOut(this);
    }
    connectToPatchIn { } // nothing doing.  we are ir only
}


MxTrJack : MxControlJack {

    *new { arg value, spec;
        ^super.newCopyArgs(value, spec)
    }
    addToSynthDef {  arg synthDef, name;
        synthDef.addTr(name, value);
    }
}

// not yet
/*
MxBufferJack : MxJack {

}


MxFFTJack : MxBufferJack {

}


MxArrayJack : MxJack {

}


MxEnvJack : MxArrayJack {

}

*/
