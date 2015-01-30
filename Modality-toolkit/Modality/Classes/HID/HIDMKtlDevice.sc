HIDMKtlDevice : MKtlDevice {
	classvar <initialized = false;
	classvar <sourceDeviceDict;
	classvar <protocol = \hid;

	var <srcID, <srcDevice;

	*getSourceName{ |shortName|
		var srcName;
		var src = this.sourceDeviceDict.at( shortName );
		if ( src.notNil ){
			srcName = this.makeDeviceName( src );
		};
		^srcName;
	}

	*initDevices { |force=false|
		if ( Main.versionAtLeast( 3, 7 ) ){
			(initialized && {force.not}).if{^this};
			HID.findAvailable;

			sourceDeviceDict = IdentityDictionary.new;
			this.prepareDeviceDicts;

			initialized = true;
		}
	}

	*prepareDeviceDicts{
		var prevName = nil, j = 0, order, deviceNames;
		deviceNames = HID.available.collect{|dev,id|
			MKtl.makeShortName( (dev.productName.asString ++ "_" ++ dev.vendorName.asString ).asString)
		}.asSortedArray;
		order = deviceNames.order{ arg a, b; a[1] < b[1] };
		deviceNames[order].do{|name, i|
			(prevName == name[1]).if({
				j = j+1;
			},{
				j = 0;
			});
			prevName = name[1];
			sourceDeviceDict.put((name[1] ++ j).asSymbol, HID.available[ name[0] ])
		};

		// put the available hid devices in MKtlDevice's available devices
		allAvailable.put( \hid, List.new );
		sourceDeviceDict.keysDo({ |key|
			allAvailable[\hid].add( key );
		});
	}

	// open all ports and display them in readable fashion,
	// copy/paste-able directly
	*find { |post=true|
		this.initDevices( true );

		if ( post ){
			this.postPossible;
		};
	}

	*postPossible{
		"\n// Available HIDMKtlDevices:".postln;
		"// MKtl(autoName);  // [ hid product, vendor(, serial number) ]".postln;
		sourceDeviceDict.keysValuesDo{ |key,info|
			var serial = info.serialNumber;
			if( serial.isEmpty ) {
				"    MKtl('%');  // [ %, % ]\n"
				.postf(key, info.productName.asCompileString, info.vendorName.asCompileString )
			} {
				"    MKtl('%');  // [ %, %, % ]\n"
				.postf(key, info.productName.asCompileString, info.vendorName.asCompileString, serial.asCompileString );
			}
		};
		"\n-----------------------------------------------------".postln;
	}

	*findSource{ |rawDeviceName, rawVendorName|
		var devKey;
		if ( initialized.not ){ ^nil };
		this.sourceDeviceDict.keysValuesDo{ |key,hidinfo|
			if ( rawVendorName.notNil ){
				if ( hidinfo.productName == rawDeviceName and: ( hidinfo.vendorName == rawVendorName ) ){
					devKey = key;
				}
			}{
				if ( hidinfo.productName == rawDeviceName ){
					devKey = key;
				};
				if ( (hidinfo.productName ++ "_" ++ hidinfo.vendorName) == rawDeviceName ){
					devKey = key;
				};
			};
		};
		^devKey;
	}

    // create with a uid, or access by name
	*new { |name, path, parentMKtl|
		var foundSource;

		if (path.isNil) {
			foundSource = this.sourceDeviceDict[ name ];
		}{
            //FIXME: uid is this a path?
			foundSource = HID.findBy( path: path ).asArray.first;
		};

		// foundSource.postln;

		// make a new one
		if (foundSource.isNil) {
			warn("HIDMKtl:"
			"	No HID source with USB port ID % exists! please check again.".format(path));
			// ^MKtl.prMakeVirtual(name);
			^nil;
		};

		^super.basicNew( name, this.makeDeviceName( foundSource ), parentMKtl ).initHIDMKtl( foundSource, path );
	}

	initHIDMKtl{ |argSource,argUid|
		srcID = argUid;
        srcDevice = argSource.open;
 		this.initElements;
	}

	closeDevice{
		this.cleanupElements;
		srcID = nil;
		srcDevice.close;
	}

    *makeDeviceName{ |hidinfo|
		^(hidinfo.productName.asString ++ "_" ++ hidinfo.vendorName);
    }

	// postRawSpecs { this.class.postRawSpecsOf(srcDevice) }

	exploring{
		^(HIDExplorer.observedSrcDev == this.srcDevice);
	}

	explore{ |mode=true|
		if ( mode ){
			"Using HIDExplorer. (see its Helpfile for Details)\n\n".post;
			"HIDExplorer started. Wiggle all elements of your controller then".postln;
			"\tMKtl(%).explore(false);\n".postf( name );
			"\tMKtl(%).createDescriptionFile;\n".postf( name );
			HIDExplorer.start( this.srcDevice );
		}{
			HIDExplorer.stop;
			"HIDExplorer stopped.".postln;
		}
	}

	createDescriptionFile {
		if(srcDevice.notNil){
			HIDExplorer.openDocFromDevice(srcDevice)
		} {
			Error("MKtl#createDescriptionFile - srcDevice is nil. HID probably could not open device").throw
		}
	}

	initElements{
		this.setHIDActions;
	}

	cleanupElements{
		this.removeHIDActions;
	}

	removeHIDActions{
		mktl.elementsDict.do{ |el|
			var theseElements;
            var elid = el.elementDescription[\hidElementID];
            var page = el.elementDescription[\hidUsagePage];
            var usage = el.elementDescription[\hidUsage];

			if ( elid.notNil ){ // filter by element id
				srcDevice.elements.at( elid ).action = nil;
			}{
				theseElements = srcDevice.findElementWithUsage( usage, page );
				theseElements.do{ |it|
					it.action = nil;
				}
			}
		};
	}

	setHIDActions{
		var newElements = ();

		mktl.elementsDict.do{ |el|
            var theseElements;

            var elid = el.elementDescription[\hidElementID];
            var page = el.elementDescription[\hidUsagePage];
            var usage = el.elementDescription[\hidUsage];

            // device specs should primarily use usage and usagePage,
            // only in specific instances - where the device has bad firmware use elementid's which will possibly be operating system dependent

            if ( elid.notNil ){ // filter by element id
                // HIDFunc.element( { |v| el.rawValueAction_( v ) }, elid, \devid, devid );
                srcDevice.elements.at( elid ).action = { |v, hidele| // could get raw value hidele.rawValue
					el.rawValueAction_( v );
					if(traceRunning) {
						"% - % > % | type: %, src:%"
						.format(this.name, el.name, el.value.asStringPrec(3), el.type, el.source).postln
					}
				};
            }{  // filter by usage and usagePage
                // HIDFunc.usage( { |v| el.rawValueAction_( v ) }, usage, page, \devid, devid );
                theseElements = srcDevice.findElementWithUsage( usage, page );
                theseElements.do{ |it|
                    it.action = { |v, hidele| // could get raw value hidele.rawValue
						el.rawValueAction_( v );
						if(traceRunning) {
							"% - % > % | type: %, src:%"
							.format(this.name, el.name, el.value.asStringPrec(3), el.type, el.source).postln
						}
					};
                };
            };
            newElements.put( el.name, el );
		};
		mktl.replaceElements( newElements );
	}

	send { |key,val|
		var thisMktlElement, thisHIDElement;
		thisMktlElement = mktl.elementDescriptionFor( key );
		if ( thisMktlElement.notNil ){
			thisHIDElement = srcDevice.findElementWithUsage( thisMktlElement.at( 'hidUsage' ), thisMktlElement.at( 'hidUsagePage' ) ).first;
			if ( thisHIDElement.notNil ){
				thisHIDElement.value = val;
			};
		};
	}
}
