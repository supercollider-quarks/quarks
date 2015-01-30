MKtlDevice{

	classvar <allAvailable; // ( 'midi': List['name1',... ], 'hid': List['name1',... ], ... )

	classvar <allInitialized = false;

	var <name, <deviceName; // short name + full device name
	var <>mktl;

	var <traceRunning = false;

	trace { |mode=true|
		traceRunning = mode;
	}

	*initClass{
			allAvailable = ();
	}

	*find { |protocols|
		if ( Main.versionAtLeast( 3, 7 ) ){
			protocols = protocols ? [\midi,\hid];
		}{
			protocols = protocols ? [\midi];
		};
		protocols.asCollection.do{ |pcol|
			this.matchClass(pcol) !? _.find
		};
		allInitialized = true;
	}

	*matchClass { |symbol|
		^this.allSubclasses.detect({ |x| x.protocol == symbol })
	}

	*initHardwareDevices{ |force=false, protocols|
		if ( protocols.isNil ){
			if ( Main.versionAtLeast( 3, 7 ) ){
				protocols = protocols ? [\midi,\hid];
			}{
				protocols = protocols ? [\midi];
			};
		};
		if ( allInitialized.not or: force ){
			this.allSubclasses.do{ |it|
				if ( protocols.includes( it.protocol ) ){
					it.initDevices( force );
				};
			};
		};
		allInitialized = true;
	}

	*findMatchingProtocols{ |name|
		^allAvailable.select(_.includes(name)).keys.as(Array);
	}

	*getMatchingProtocol{ |name|
		var matchingProtocols = this.findMatchingProtocols( name );
		if ( matchingProtocols.size == 0 ){
			// not attached, just return the virtual one if it was found:
			^nil;
		};
		if ( matchingProtocols.size > 1 ){ // more than one matching protocol:
			"WARNING: multiple protocol devices not implemented yet, using %\n".postf( matchingProtocols.first );
		};
		matchingProtocols = matchingProtocols.first;
		^matchingProtocols;
	}

	*getDeviceNameFromShortName{ |shortName|
		var subClass;
		var matchingProtocol = this.getMatchingProtocol( shortName );
		// "matching protocol : ".post; matchingProtocol.postln;
		if ( matchingProtocol.isNil ){
			^nil;
		};
		subClass = MKtlDevice.matchClass(matchingProtocol);
		if( subClass.isNil ){
			^nil;
		}{
			^subClass.getSourceName( shortName );
		};
	}

	*findDeviceShortNameFromLongName{ |devLongName|
		var devKey, newDevKey;
		if ( devLongName.isKindOf( String ) ){
			this.subclasses.do{ |subClass|
				newDevKey = subClass.findSource( devLongName );
				if ( newDevKey.notNil ){
					devKey = newDevKey;
				};
			};
			^devKey;
		};
		if (devLongName.isKindOf( Array ) ){
			this.subclasses.do{ |subClass|
				newDevKey = subClass.findSource( *devLongName );
				if ( newDevKey.notNil ){
					devKey = newDevKey;
				};
			};
			^devKey;
		};
	}

	*tryOpenDevice{ |name, parentMKtl|
		var matchingProtocol, subClass;
		// then see if it is attached:

		matchingProtocol = this.getMatchingProtocol( name );

		if ( matchingProtocol.isNil ){
			^nil;
		};

		subClass = MKtlDevice.matchClass(matchingProtocol);
		if( subClass.isNil ){
			"WARNING: MKtl: device not found with name %, and no matching device description found\n".postf( name );
			^nil;
		};
		if( subClass.notNil ) {
			^subClass.new( name, parentMKtl: parentMKtl );
		};
	}

	*basicNew { |name, deviceName, parentMKtl |
		^super.new.init(name, deviceName, parentMKtl );
	}


	init{ |initName, argDeviceName, parentMKtl|
		name = initName;
		deviceName = argDeviceName;
		mktl = parentMKtl;
	}


	*protocol{
		this.subclassResponsibility(thisMethod)
	}

	cleanupElements{
		this.subclassResponsibility(thisMethod)
	}

	initElements{
		this.subclassResponsibility(thisMethod)
	}

	closeDevice{
		this.subclassResponsibility(thisMethod)
	}

	// exploration:

	exploring{
		this.subclassResponsibility(thisMethod)
	}

	explore{ |mode=true|
		this.subclassResponsibility(thisMethod)
	}

	createDescriptionFile{
		this.subclassResponsibility(thisMethod)
	}
}