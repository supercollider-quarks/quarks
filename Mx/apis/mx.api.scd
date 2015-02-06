
var get = { arg uuid;
      API('player').call('_get', uuid);
    },
    set = { arg uuid, mx;
      API('player').call('_set', uuid, mx);
    },
    getUnit = { arg uuid, id;
      var mx = get.(uuid);
      id ?? {
        Error("getUnit: unit id is nil").throw
      };
      mx.atID(id.asInteger);
    },
    getInlet = { arg unit, key;
      unit.inlets.at(key) ?? {
        Error("inlet not found:" + key).throw
      };
    };


(
  new: { arg reply, uuid;
    var x = Mx.new.app;
    uuid = set.(uuid, x);
    reply.value(uuid);
  },
  info: { arg reply, uuid;
    var mx = get.(uuid);
    reply.(mx.asJSON);
    // does not include cables yet
  },
  unitInfoAt: { arg reply, uuid, id;
    var unit = getUnit.(uuid, id);
    reply.(unit.asJSON);
  },

  setInletValue: { arg reply, uuid, id, key, value;
    // mapped value
    var unit = getUnit.(uuid, id),
        inlet = getInlet.(unit, key);
    if(inlet.canSet, {
      inlet.set(value);
      reply.value(inlet.get());
    }, {
      Error("Cannot set value on inlet" + inlet).throw;
    });
  },
  setInletValues: { arg reply, uuid, id, keyValuesJSON;
    // JSON, unmapped
    var unit = getUnit.(uuid, id),
        keyValues;
    // keyValuesJSON is a Symbol ?
    // possibly sc's OSC implementation doesn't do strings

    // parseYAML is broken, float values are strings
    keyValues = keyValuesJSON.asString.parseYAML;

    keyValues.keysValuesDo({ arg key, value;
      var inlet = unit.inlets.at(key);
      if(inlet.canSet, {
        inlet.set(inlet.spec.map(value.asFloat));
      }, {
        Error("Cannot set value on inlet" + inlet).throw;
      });
    });
    reply.value();
  },
  getInletValue: { arg reply, uuid, id, key;
    var unit = getUnit.(uuid, id),
        inlet = unit.inlets.at(key);
    if(inlet.canGet, {
      reply.value(inlet.get());
    }, {
      Error("Cannot get value from inlet" + inlet).throw;
    });
  },

  getPointUUID: { arg reply, uuid, x, y;
    // deprec
    var mx = get.(uuid),
        unit = mx.at(x@y);
    reply.(API('player').call('prGetOrSet', unit.source));
  },
  getUnitUUID: { arg reply, uuid, id;
    // find an object in this mx, register it and return that uuid
    var unit = getUnit.(uuid, id);
    reply.(API('player').call('prGetOrSet', unit.source));
  },

  setChannelLevel: { arg reply, uuid, channel, level;
    var mx = get.(uuid),
      db;
    db = \db.asSpec.map(level.max(0.000000001));
    mx.channel(channel.asInteger).db = db;
    reply.(db);
    // mute
    // unmute
  }
);
