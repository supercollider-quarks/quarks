

MxUnit  {

    classvar registery, <protoHandler;

    var <>source, <inlets, <outlets, <>handlers, <varPooling=false;
    var <>onLoad;
    var <>group, status;

    *make { arg source, class;
        var handlers;
        if(source.isKindOf(MxUnit) or: {source.isNil}, {
            ^source
        });
        handlers = this.handlersFor(class ? source.class);
        ^handlers.use {
            var unit;
            unit = ~make.value(source);
            if(~source.isNil, {
                // set var so you can access it
                ~source = source;
            });
            unit.handlers = handlers;
            ~unit = unit;
            ~this = unit;
            unit
        }
    }
    saveData {
        var data, ids;
        data = this.use { ~save.value(source) };
        ^[source.class.name, data]
    }
    *loadData { arg data;
        var source, class;
        # class, data = data;
        class = class.asClass;
        source = this.handlersFor(class).use { ~load.value(data) };
        ^this.make(source, class)
    }
    *new { arg source, inlets, outlets;
        ^super.newCopyArgs(source, inlets, outlets).init
    }
    init {
        inlets.do(_.unit = this);
        outlets.do(_.unit = this);
    }
    *handlersFor { arg class;
        var classHandlers;
        classHandlers = this.handlersForClass(class);
        if(classHandlers.isNil, {
            Error("No MxUnit driver found for " + class).throw;
        });
        // this is actually a variable space dict, not just handlers
        ^Environment(32, classHandlers, nil, true);
    }
    *handlersForClass { arg class;
        var match, path;
        match = registery[class.name] ?? {
            path = PathName(MxUnit.class.filenameSymbol.asString).parentPath
                            +/+ "drivers" +/+ class.name.asString ++ ".scd";
            if(File.exists(path), {
                path.debug("Loading driver").load;
                match = registery[class.name]
            }, {
                path = Platform.userExtensionDir +/+ "quarks" +/+ "*"    +/+ "mxdrivers" +/+ class.name.asString ++ ".scd";
                path.pathMatch.any { arg p;
                    p.debug("Loading mx driver").load;
                    (match = registery[class.name]).notNil
                };
                if(match.isNil and: {class !== Object}, {
                    ^this.handlersForClass(class.superclass)
                });
                match
            })
        };
        ^match
    }

    getInlet { arg index;
        if(index.isNil, {
            ^inlets.first
        });
        if(index.isInteger, {
            ^inlets[index]
        }, {
            inlets.do { arg in;
                if(in.name == index, {
                    ^in
                })
            }
        });
        Error("Inlet not found:" + index).throw
    }
    getOutlet { arg index;
        if(index.isNil, {
            ^outlets.first
        });
        if(index.isInteger, {
            ^outlets[index]
        }, {
            outlets.do { arg in;
                if(in.name == index, {
                    ^in
                })
            }
        });
        Error("Outlet not found:" + index).throw
    }
    addInlet { arg name, spec, adapter;
        var inlet;
        inlet = MxInlet(name, inlets.size, spec, adapter);
        inlet.unit = this;
        inlets = inlets.add(inlet);
        handlers.at(\mx).register(inlet);
    }
    addOutlet { arg name, spec, adapter;
        var outlet;
        outlet = MxOutlet(name, outlets.size, spec, adapter);

        outlet.unit = this;
        outlets = outlets.add(outlet);
        handlers.at(\mx).register(outlet);
    }

    *register { arg classname, handlers;
        var e, class, superclassHandlers;
        classname = classname.asSymbol;
        e = registery.at(classname);
        if(e.notNil, { // updating
            e.keys.do { arg k;
                e.removeAt(k)
            };
        }, { // new registration
            class = classname.asClass;
            if(class.notNil and: {class !== Object}) {
                class.superclasses.any { arg sup;
                    superclassHandlers = this.handlersForClass(sup);
                    superclassHandlers.notNil
                }
            };
            e = Environment(32, superclassHandlers, nil, true);
        });
        handlers.keysValuesDo { arg k, v;
            e.put(k, v)
        };
        registery.put(classname, e)
    }
    mx_ { arg mx;
        handlers['mx'] = mx;
    }
    varPooling_ { arg boo;
        varPooling = boo;
        handlers['mx'].updateVarPooling;
    }
    parentEnvir_ { arg env;
        handlers.parent = env;
    }
    parentEnvir {
        ^handlers.parent
    }
    isPrepared {
        ^['isPrepared', 'isPlaying', 'isStopped'].includes(status)
    }
    prSetStatus { arg newStatus;
        status = newStatus;
        NotificationCenter.notify(this, \didChangeStatus, newStatus)
    }
    didLoad {
        this.use {
            ~didLoad.value();
            this.onLoad.value();
        }
    }

    // methods delegated to the handlers
    prepareToBundle { arg agroup, bundle, private, bus;
        bundle.addFunction({this.prSetStatus('isPrepared')});
        ^this.delegate('prepareToBundle', agroup, bundle, true, bus);
    }
    spawnToBundle { arg bundle;
        ^this.use {
            ~spawnToBundle.value(bundle);
            bundle.addFunction({ this.prSetStatus('isPlaying') })
        }
    }
    stopToBundle { arg bundle;
        ^this.use {
            ~stopToBundle.value(bundle);
            bundle.addFunction({ this.prSetStatus('isStopped') })
        }
    }
    freeToBundle { arg bundle;
        ^this.use {
            ~freeToBundle.value(bundle);
            bundle.addFunction({ this.prSetStatus('isFreed') })
        }
    }
    respawnToBundle { arg bundle;
        this.stopToBundle(bundle);
        this.spawnToBundle(bundle);
    }
    moveToHead { arg aGroup, bundle;
        ^this.use {
            ~moveToHead.value(aGroup, bundle, group)
        }
    }

    use { arg function, rollback;
        var result, saveEnvir;

        saveEnvir = currentEnvironment;
        currentEnvironment = handlers;
        protect {
            result = function.value(handlers)
        } { arg exception;
            if(exception.notNil) {
                ("MxUnit" + this.source + this.source.class + "ERROR in:\n" + function.def + "\n" + function.def.sourceCode).postln;
                rollback.value;
            };
            currentEnvironment = saveEnvir;
        };
        ^result
    }
    delegate { arg handlerName ... args;
        var result, saveEnvir;

        saveEnvir = currentEnvironment;
        currentEnvironment = handlers;
        protect {
            result = currentEnvironment.at(handlerName).valueArray(args)
        } { arg exception;
            var code;
            if(exception.notNil) {
                code = currentEnvironment.at(handlerName);
                if(code.isKindOf(Function), {
                    code = code.def.sourceCode;
                });
                ("MxUnit:" + this.source + this.source.class + "\nERROR in:\n~" + handlerName + "\n" + code).postln;
            };
            currentEnvironment = saveEnvir;
        };
        ^result
    }
    callHandler { arg method ... args;
        var result, saveEnvir;

        saveEnvir = currentEnvironment;
        currentEnvironment = handlers;
        protect {
            result = handlers[method].valueArray(args)
        } { arg exception;
            if(exception.notNil) {
                ("MxUnit" + this.source + "ERROR in" + method + args).postln;
                // can fetch the handler source code here
            };
            currentEnvironment = saveEnvir;
        };
        ^result
    }

    play { arg group, atTime, bus;
        ^this.use { ~play.value(group, atTime, bus) }
    }
    stop { arg atTime, andFreeResources=true;
        ^this.use { ~stop.value(atTime, andFreeResources) }
    }
    respawn { arg atTime;
        ^this.use { ~respawn.value(atTime) }
    }
    isPlaying {
        ^this.use { ~isPlaying.value }
    }
    numChannels {
        ^this.use { ~numChannels.value }
    }
    spec {
        ^this.use { ~spec.value }
    }
    beatDuration {
        ^this.use { ~beatDuration.value }
    }
    copySource {
        ^this.use { ~copy.value }
    }
    // relocate  toBeat, atTime
    name {
        ^this.use { ~name.value }
    }
    gui { arg parent, bounds;
        ^this.use { ~gui.value(parent, bounds) }
    }
    draw { arg pen, bounds, style;
        ^this.use { ~draw.value(pen, bounds, style) }
    }
    timeGui { arg parent, bounds, maxTime;
        ^this.use { ~timeGui.value(parent, bounds, maxTime) }
    }
    zoomTime { arg fromTime, toTime;
        ^this.use { ~zoomTime.value(fromTime, toTime) }
    }
    gotoBeat { arg beat, atBeat, bundle;
        ^this.use { ~gotoBeat.value(beat, atBeat, bundle) }
    }
    canRecord {
        ^handlers['record'].notNil
    }
    record { arg boo=true, atTime;
        ^this.use { ~record.value(boo, atTime) }
    }
    *initClass {
        registery = IdentityDictionary.new;
    }
}



MxInlet {

    var <>name, <>index, <>spec, <>adapter;
    var <>unit;

    *new { arg name, index, spec, adapter;
        ^super.newCopyArgs(name.asSymbol, index, spec.asSpec, adapter)
    }

    // one-time adhoc get/set of a value, usually a float
    canGet { ^adapter.canGet }
    canSet { ^adapter.canSet }
    set { arg v; adapter.set(v) }
    get { ^adapter.get }

    storeArgs {
        // adapter: AbsMxAdapter subclass
        // which is not really savable
        ^[name, index, spec, adapter]
    }
    printOn { arg stream;
        stream << "in:" << name
    }
}


MxOutlet : MxInlet {

    printOn { arg stream;
        stream << "out:" << name
    }
}
