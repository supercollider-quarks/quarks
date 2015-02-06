
/*
    a single output with the msg as an array.

    to get individual components from the array you could use a splitter object
    or a higher level object that represents the messages that the device sends
    and has its own driver
*/

MxUnit.register('OSCFunc',
(
    make: { arg oscFunc;
        var inlets, outlets, out, i=0, action;
        ~source = oscFunc;
        oscFunc.fix;
        action = MxHasAction({ arg callback;
                oscFunc.prFunc = { arg msg, time, addr, recvPort;
                    callback.value(msg.copyToEnd(1))
                }
                });
        out = MxOutlet("out", 0, ArraySpec.new, action);

        MxUnit(oscFunc, [], [out])
    },
    freeToBundle: { arg bundle;
        bundle.addFunction({ ~source.free }.inEnvir)
    }

));
