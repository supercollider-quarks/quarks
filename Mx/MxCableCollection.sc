/*
want to find what cables are attached to units
may be cheaper to maintain lookup arrays

or a special collection that uses .add and .remove
but also records the units
*/


MxCableCollection : List {

    var toUnits, fromUnits;

    *new {
        ^super.new.init
    }
    init {
        toUnits = IdentityDictionary.new;
        fromUnits = IdentityDictionary.new;
    }
    setCollection { arg aColl;
        array = [];
        aColl.do(this.add(_));
    }

    add { arg cable;
        var h;
		if(cable.inlet.isNil or: {cable.outlet.isNil}, {
			"Broken cable, discarding".warn;
			cable.dump;
			^this
		});
        super.add(cable);
        h = cable.inlet.unit.identityHash;
        toUnits[h] = toUnits[h].add(cable);
        h = cable.outlet.unit.identityHash;
        fromUnits[h] = fromUnits[h].add(cable);
    }
    remove { arg cable;
        toUnits.at(cable.inlet.unit.identityHash).remove(cable);
        fromUnits.at(cable.outlet.unit.identityHash).remove(cable);
        ^super.remove(cable);
    }
    toUnit { arg unit;
        ^toUnits.at(unit.identityHash) ? []
    }
    fromUnit { arg unit;
        ^fromUnits.at(unit.identityHash) ? []
    }
    toInlet { arg inlet;
        ^this.toUnit(inlet.unit).select({ arg cable; cable.inlet === inlet })
    }
    fromOutlet { arg outlet;
        ^this.fromUnit(outlet.unit).select({ arg cable; cable.outlet === outlet })
    }
}
