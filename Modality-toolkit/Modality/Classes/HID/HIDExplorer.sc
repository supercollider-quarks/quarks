HIDExplorer {

    classvar <allMsgTypes = #[ \elid, \usage ];

	classvar <resps;
	classvar <results;
	classvar <observeDict;
	classvar <>verbose = true;
	classvar <observedSrcDev;

    classvar <exploreFunction;

	*shutUp { verbose = false }

	*init {
        exploreFunction = { |devid, thisdevice, elid, page, usage, value, mappedvalue| this.updateRange( elid, page, usage, value ) };
	}

	*start { |srcDev|
		if ( srcDev.notNil ){
			srcDev.debug_( true );
			observedSrcDev = srcDev;
		}{
			HID.debug_( true );
		}
	}

	*stop { |srcDev|
		srcDev = srcDev ? observedSrcDev;
		if ( srcDev.notNil ){
			srcDev.debug_( false );
		}{
			HID.debug_( false );
		}
	}

	*prepareObserve {
		observeDict = ();
		allMsgTypes.do(observeDict.put(_, Dictionary()));
	}

	*openDoc {
		Document("edit and save me", this.compileFromObservation );
	}

    *openDocFromDevice { |dev|
        Document("edit and save me", this.compileFromDevice( dev ) );
	}

    *detectDuplicateElements{ |elements|
        var elementUsageDict = IdentityDictionary.new;
        var duplicates = IdentityDictionary.new;
        var uniques = IdentityDictionary.new;
        var usagePageKey;

        elements.sortedKeysValuesDo{ |elid,ele|
            usagePageKey = ( ele.usage.asString ++ "_" ++ ele.usagePage ).asSymbol;
            if ( elementUsageDict.at( usagePageKey ).notNil ){
                // this one already appeared, it's a double!!
                duplicates.put( elid, ele );
            }{
                uniques.put( elid, ele );
                elementUsageDict.put( usagePageKey, ele );
            }
        };
        ^[uniques, duplicates];
    }

    *compileFromDevice { |dev|
		var str = "(\n";
        var elements = dev.elements;
        var uniques, duplicates;

		str = str ++ "device: \"" ++ dev.info.productName.asString ++ "_" ++ dev.info.vendorName.asString ++ "\",\n";
		str = str ++ "protocol: 'hid',\n";
		str = str ++ "description: (\n";

        /// todo: check the device elements whether any duplicate usages occur, if so, then we need to filter by element id
        /// could infer type from the control
        /// could infer name from the control -> suggest a name

        /// FIXME: ignore constant fields!

        #uniques, duplicates = this.detectDuplicateElements( elements.select{ |v| v.ioType == 1 } );
        if ( uniques.size + duplicates.size > 0 ){
            str = str + "\n\n// --------- input elements ----------";
            uniques.sortedKeysValuesDo{ |key,val|
				str = str + "\n'<element name %>': ('hidUsage': %, 'hidUsagePage': %, 'type': '<type %>', 'ioType': 'in', 'spec': <spec %> ),"
                .format(val.usageName, val.usage, val.usagePage, val.pageName, val.usageName );
            };
            duplicates.sortedKeysValuesDo{ |key,val|
                str = str + "\n'<element name %_%>': ('hidElementID': %, 'type': '<type %>', 'ioType': 'in', 'spec': <spec %> ),"
                .format(val.usageName, key, key, val.pageName, val.usageName );
            };
        };

        #uniques, duplicates = this.detectDuplicateElements( elements.select{ |v| v.ioType == 2 } );
        if ( uniques.size + duplicates.size > 0 ){
            str = str + "\n\n// --------- output elements ----------";
            uniques.sortedKeysValuesDo{ |key,val|
				str = str + "\n'<element name %>': ('hidUsage': %, 'hidUsagePage': %, 'type': '<type %>', 'ioType': 'out', 'spec': <spec %> ),"
                .format(val.usageName, val.usage, val.usagePage, val.pageName, val.usageName );
            };
            duplicates.sortedKeysValuesDo{ |key,val|
				str = str + "\n'<element name %_%>': ('hidElementID': %, 'type': '<type %>', 'ioType': 'out', 'spec': <spec> ),"
                .format(val.usageName, key, key, val.pageName, val.usageName );
            };
        };

        /*
        #uniques, duplicates = this.detectDuplicateElements( elements.select{ |v| v.ioType == 3 } );
        if ( uniques.size + duplicates.size > 0 ){
            str = str + "\n\n// --------- feature report ----------";
            uniques.sortedKeysValuesDo{ |key,val|
                str = str + "\n'<element name %>': ('hidhidUsage': %, 'usagePage': %, , 'type': '<type %>', 'ioType': 'feature' ),"
                .format(val.usageName, val.usage, val.usagePage, val.pageName );
            };
            duplicates.sortedKeysValuesDo{ |key,val|
                str = str + "\n'<element name %_%>': ('hidElementID': %, 'type': '<type %>', 'ioType': 'feature' ),"
                .format(val.usageName, key, key, val.pageName );
            };
        };
        */

		str = str + "\n)\n);";

		^str;
    }

	*compileFromObservation { |includeSpecs = false|

		var num, chan;

		var str = "[";

		if (observeDict[\elid].notEmpty) {
			str = str + "\n// ------ element ids -------------";
			observeDict[\elid].sortedKeysValuesDo { |key, val|
				#num, chan = key.split($_).collect(_.asInteger);
				str = str + "\n'<element name %>': ('hidElementID': %, 'type': '<type>'),"
					.format(key, val);
			};
		};

		if (observeDict[\usage].notEmpty) {
			str = str + "\n\n// --------- usage ids ----------";
			observeDict[\usage].sortedKeysValuesDo { |key, val|
				#num, chan = key.split($_).collect(_.asInteger);
				str = str + "\n'<element name %>': ('hidUsage': %, 'hidUsagePage': %, , 'type': '<type>' ),"
				.format(key, val.usage, val.hidUsagePage ); /// could infer type from the control
			};
		};

		str = str + "\n];";

		^str;
	}

	*updateRange { |elid, page, usage, hidElem|
        var hash, range;
        var msgDict = observeDict[\elid];
		var val = hidElem.value;

        if (verbose) { [elid, page, usage, val].postcs; } { ".".post; };
        if (0.1.coin) { observeDict.collect(_.size).sum.postln };

		/*
		hash = "%_%_%".format(elid, page, usage).postln;
        range = msgDict[hash];
        range.isNil.if{
			msgDict[hash] = [val, val];
		} {
			msgDict[hash] = [min(range[0], val), max(range[1], val)]
		}
		*/
	}
}

/// buttons:
// generally: mode: push , spec: hidBut

// hatswitch: mode: center, spec: hidHat

// x,y : mode: center, spec: cent1
