

+ Mx {

    *initClass {
        var busf;
        if(\Insp.asClass.notNil, {
            Class.initClassTree(ObjectInsp);
            ObjectInsp.registerHook(MxCable, { arg cable, layout;

                InspButton.captioned("Out", cable.outlet.unit.source, layout.startRow);
                InspButton(cable.outlet.adapter, layout);

                InspButton.captioned("In", cable.inlet.unit.source, layout.startRow);
                InspButton(cable.inlet.adapter, layout);

                layout.startRow;
                try {
                    var name = MxCable.strategies.findKeyForValue(cable.strategy);
                    InspButton.captioned(name.asString ? "MxCableStrategy", cable.strategy, layout);
                } {
                    SimpleLabel(layout, "NO STRATEGY FOR CABLE");
                }
            });

            ObjectInsp.registerHook(MxCableStrategy, { arg strategy, layout;
                var name = MxCable.strategies.findKeyForValue(strategy);
                SimpleLabel(layout, name.asString ? "Strategy was not registered");
                //connectf sourceGui
                //disconnectf
            });

            busf = { arg pob, layout;

                    var bus, listen;
                    try {
                        bus = pob.value;
                    };
                    if(bus.notNil, {
                        if(bus.rate == 'audio', {
                            listen = Patch({ In.ar( bus.index, bus.numChannels ) });
                            layout.startRow;
                            SimpleLabel( layout, bus.asString );
                            ToggleButton( layout, "listen", {
                                listen.play
                            }, {
                                listen.stop
                            });
                        });
                        layout.startRow.flow({ |f|
                            var ann;
                            SimpleLabel(f, "Annotations:");
                            ann = BusPool.getAnnotations(bus);

                            if(ann.notNil, {
                                ann.keysValuesDo({ |client, name|
                                    f.startRow;
                                    Tile(client, f);
                                    SimpleLabel(f, ":"++name);
                                });
                            });
                        })
                    });
                };
            [MxPlaysOnBus, MxListensToBus, MxHasBus, MxPlaysOnKrBus].do { arg klass;
                ObjectInsp.registerHook(klass, busf);
            };


            /*
            ObjectInsp.registerHook(MxCableStrategy, { arg strategy, layout;

                ObjectInsp.sourceCodeGui
                InspButton.captioned("connectf", cable.outlet.unit.source, layout.startRow);
                InspButton.captioned("adapter", cable.outlet.adapter, layout.startRow);

                InspButton.captioned("In", cable.inlet.unit.source, layout.startRow);
                InspButton.captioned("adapter", cable.inlet.adapter, layout.startRow);

                layout.startRow;
                try {
                    InspButton.captioned("Strategy", cable.strategy, layout);
                } {
                    SimpleLabel(layout, "NO STRATEGY FOR CABLE");
                }
            })
            */

        })
    }
}
