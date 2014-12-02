+ GeneralHID {
	
	*findBy { |vendorID, productID, locID, versionID|
		if ( locID.isKindOf( String ), { locID = locID.asSymbol } );
		// you could also only search by locID
		if ( [vendorID, productID, locID, versionID].every( _.isNil ) ){
			^nil;
		};
		^this.deviceList.detect { |pair|
			var dev, info; #dev, info = pair;
			vendorID.isNil or: { info.vendor == vendorID }
			and: { productID.isNil or: { info.product == productID } }
			and: { locID.isNil or: { info.physical == locID } }
			and: { versionID.isNil or: { info.version == versionID } }
		};
	}
}

+ GeneralHIDDevice {

	// if we move this to main, it can be stored in a variable
	// cookie -> element/slot list
	elements {
		^IdentityDictionary.new.putPairs( slots.collect{ |type| type.collect{ |it| [it.cookie, it ] }.asArray }.asArray.flat );
	}

	getAllCookies {
		^slots.collect { |type| type.collect{ |it| it.cookie }.asArray }.asArray.flatten;
	}
}

+ GeneralHIDSlot {

	postSpecs {
		//	("" + i + "\t").post; [ele.type, ele.usage, ele.cookie, ele.min, ele.max, ele.ioType, ele.usagePage, ele.usageType].postln;
	}

	cookie {
		^devSlot.getCookie;
	}
}
