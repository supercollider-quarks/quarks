// honouring Jeff's MKeys by keeping the M for prototyping the new Ktl

MKtl { // abstract class
	classvar <defaultDeviceDescriptionFolder; //path of MKtlDescriptions folder
	classvar <allDevDescs; // an identity dictionary of device descriptions
	classvar <all; // holds all instances of MKtl
	classvar <specs; // ( 'specName': ControlSpec, ...) -> all specs


	var <name;
	var <elementsDict; //of type: ('elementName':MKtlElement, ...) -> elements to which stuff is registered
	var <elements;


	// tree structure composed of dictionaries and arrays
	// with a description of all the elements on the device.
	// read from an external file.
	var <deviceDescriptionHierarch;

	// an array of keys and values with a description of all the elements on the device.
	// generated from the hierarchical description read from the file.
	var <deviceDescriptionArray;

	var <deviceDescriptionName;

	var <mktlDevice; // references to the device used

	var <traceRunning = false;

	trace{ |value=true|
		if ( mktlDevice.notNil ){ mktlDevice.trace( value ); };
		traceRunning = value;
	}

	*allAvailable{
		^MKtlDevice.allAvailable;
	}

	*allDescriptions{
		if(MKtl.allDevDescs.isNil){
			MKtl.loadAllDescs
		};
		^this.prShortNamesToDeviceDescriptions;
	}

	*find { |protocols|
		MKtlDevice.find( protocols );
	}

	*openDescriptionFolder {
		this.defaultDeviceDescriptionFolder.openOS;
	}

	openDescriptionFile {
		var descfilepath;
		var mydesc = MKtl.allDevDescs.detect { |desc|
			desc[\device] == this.deviceDescriptionName
		};

		if (mydesc.isNil or: {
			descfilepath = mydesc[\filepath];
			descfilepath.isNil;
		}) {
			"% : could not find a description file to open.".postf(this);
			^this
		};

		descfilepath.openOS;

	}

	*initClass {
		Class.initClassTree(Spec);
		all = ();

		specs = ().parent_(Spec.specs);

		// general
		this.addSpec(\cent255, [0, 255, \lin, 1, 128]);
		this.addSpec(\cent255inv, [255, 0, \lin, 1, 128]);
		this.addSpec(\lin255,  [0, 255, \lin, 1, 0]);
		this.addSpec(\cent1,  [0, 1, \lin, 0, 0.5]);
		this.addSpec(\lin1inv,  [1, 0, \lin, 0, 1]);

		// MIDI
		this.addSpec(\midiNote, [0, 127, \lin, 1, 0]);
		this.addSpec(\midiCC, [0, 127, \lin, 1, 0]);
		this.addSpec(\midiVel, [0, 127, \lin, 1, 0]);
		this.addSpec(\midiBut, [0, 127, \lin, 127, 0]);
		this.addSpec(\midiTouch, [0, 127, \lin, 1, 0]);
		this.addSpec(\midiBend, [0, 16383, \lin, 1, 8192]);

		// HID
		// this.addSpec(\mouseAxis, [-5, 5, \lin, 0, 0]);
		// this.addSpec(\mouseWheel, [-5, 5, \lin, 0, 0]);

		this.addSpec(\mouseAxis, [0.4,0.6, \lin, 0, 0.5]);
		this.addSpec(\mouseWheel, [0.4,0.6, \lin, 0, 0.5]);

		this.addSpec(\hidBut, [0, 1, \lin, 1, 0]);
		this.addSpec(\hidHat, [0, 1, \lin, 1, 0]);
		this.addSpec(\compass8, [0, 8, \lin, 1, 1]); // probably wrong, check again!

		this.addSpec(\cent1,  [0, 1, \lin, 0, 0.5].asSpec);
		this.addSpec(\cent1inv,  [1, 0, \lin, 0, 0.5].asSpec);

		defaultDeviceDescriptionFolder = this.filenameSymbol.asString.dirname.dirname +/+ "MKtlDescriptions";
	}

	*addSpec {|key, spec|
		specs.put(key, spec.asSpec);
	}

	*makeShortName {|deviceID|
		^(deviceID.asString.toLower.select{|c| c.isAlpha && { c.isVowel.not }}.keep(4)
			++ deviceID.asString.select({|c| c.isDecDigit}))
	}

	*getMatchingDescsForShortName{ |shortName|
		var r,t, descName, namesToDescs;
		t = shortName.asString;
		r = t[..(t.size-2)];
		r = r.asSymbol;
		descName = this.allDescriptions.select{ |val,key| key == r; }.collect{ |it| it };
		namesToDescs = IdentityDictionary.new;
		descName.do{ |dname,key| namesToDescs.put( dname , this.allDevDescs.at( dname ) ) };
		^namesToDescs;
	}

	*findDeviceDescFromShortName{ |shortName|
		var devDescFromShortName, deviceDescName, devDesc;
		devDescFromShortName = this.getMatchingDescsForShortName( shortName );
		if ( devDescFromShortName.size == 0 ){
			^[nil,nil];
		};
		if ( devDescFromShortName.size > 1 ){
			// found more than one device description it matches
			"WARNING: multiple devices descriptions found that match %: , using %\n".postf( name, devDescFromShortName.keys.asArray, devDescFromShortName.keys.asArray.first );
		};
		devDescFromShortName = devDescFromShortName.asKeyValuePairs.clump(2).first;
		deviceDescName = devDescFromShortName[0];
		devDesc = devDescFromShortName[1];
		^[devDesc, deviceDescName];
	}

	*findDeviceDescFromDeviceName{ |deviceName|
		^allDevDescs.select{ |desc,key|
			desc.at( \device ) == deviceName;
		}.asArray.first;
	}

	*findDeviceDesc{ |deviceDesc, shortName|
		var devDesc, deviceDescName;

		if ( deviceDesc.isNil ){
			if ( shortName.isNil ){
				^[nil,nil];
			}{
				#devDesc, deviceDescName = this.findDeviceDescFromShortName( shortName );
			}
		}{
			if ( deviceDesc.isKindOf( Dictionary ) ){
				devDesc = deviceDesc;
				deviceDescName = devDesc.at( \device );
			}{
				// look up deviceDescName in allDescriptions
				devDesc = this.getDeviceDescription( deviceDesc );
				deviceDescName = deviceDesc;
			}
		};
		^[devDesc, deviceDescName]
	}

	// new returns existing instances
	// of subclasses that exist in .all,
	// or returns a new empty instance.
	// this is to allow virtual MKtls eventually.
	*new { |name, deviceDesc, lookForNew = false|
		if ( name.notNil ){ // first do a lookup
			if ( all[name].notNil ){
				if ( deviceDesc.notNil ){
					if ( deviceDesc != all[name].deviceDescriptionName ){
						"WARNING: MKtl: if you want to change the device specification, use MKtl(%).rebuildFrom(%)\n".postf( name.asCompileString, deviceDesc.asCompileString );
					};
				};
				^all[name];
			}
		};

		// --- generate overview of attached and available description if necessary ---
		if ( name.notNil ){
			// we want to know what is attached if we look with a name
			MKtlDevice.initHardwareDevices( lookForNew );
		};
		this.loadAllDescs( lookForNew ); // will look for deviceDescs

		^super.new.init( name, deviceDesc );
	}

	*newFromDevice{ |name, deviceInfo, deviceDesc, lookForNew = false|  // autogenerates a shortName if not given

		// --- generate overview of attached and available description if necessary ---
		MKtlDevice.initHardwareDevices( lookForNew );
		this.loadAllDescs( lookForNew ); // will look for deviceDescs

		if ( name.notNil ){ // first do a lookup
			if ( all[name].notNil ){
				if ( deviceDesc.notNil ){
					"WARNING: MKtl: if you want to change the device specification, use MKtl(%).rebuildFrom(%)\n".postf( name, deviceDesc );
				};
				^all[name];
			}
		};
		^super.new.initFromDeviceName( name, deviceInfo, deviceDesc );
	}

	init{ |argName, deviceDesc|
		var devDesc, deviceDescName;
		var deviceName;

		name = argName;
		// --- if name is not given, create one ---
		if ( name.isNil ){
			if ( deviceDesc.isKindOf( Dictionary ) ){
				deviceDescName = deviceDesc.at( \device );
			}{
				deviceDescName = deviceDesc;
			};
			name = this.class.makeShortName( deviceDescName ).asSymbol;
		};

		if ( deviceDesc.isNil ){
			// if device is attached, use the device name from that to find the spec
			deviceName = MKtlDevice.getDeviceNameFromShortName( name );
			if ( deviceName.notNil ){
				deviceDesc = this.class.findDeviceDescFromDeviceName( deviceName );
			}
		};

		#devDesc, deviceDescName = this.class.findDeviceDesc( deviceDesc, name );

		if ( devDesc.isNil ){
			this.warnNoDeviceDescriptionFileFound( name );
		};

		this.prInitFromDeviceDescription( devDesc, deviceDescName );
		this.prTryOpenDevice( name, devDesc );

		all.put(name, this);
	}

	initFromDeviceName{  | name, deviceInfo, deviceDesc |
		var deviceName;
		var shortName = MKtlDevice.findDeviceShortNameFromLongName( deviceInfo );
		if( name.isNil ){ name = shortName; };
		if ( deviceDesc.notNil ){
			this.init( name, deviceDesc );
		}{
			deviceName = MKtlDevice.getDeviceNameFromShortName( shortName );
			deviceDesc = this.class.findDeviceDescFromDeviceName( deviceName );
			this.init( name, deviceDesc );
		}
	}

	prTryOpenDevice{ |devName,devDesc| // is a shortname
		var newMKtlDevice;

		newMKtlDevice = MKtlDevice.tryOpenDevice( devName, this );
		if ( newMKtlDevice.isNil ){
			// maybe I gave a funky name, and can find the device from the spec
			if ( devDesc.notNil ){
				devName = MKtlDevice.findDeviceShortNameFromLongName( devDesc.at( \device ) );
				if ( devName.notNil ){
					newMKtlDevice = MKtlDevice.tryOpenDevice( devName, this );
				};
			};
		};

		if ( newMKtlDevice.notNil ){
			// cross reference:
			mktlDevice = newMKtlDevice;
			"Opened device % for MKtl(%)\n".postf( devName, name );
		}{
			"WARNING: Could not open device for MKtl(%)\n".postf( name );
		};
	}

	rebuildFrom{ |deviceDescriptionNameOrDict| // could be a string/symbol or dictionary
		var devDescName, devDesc;
		if ( deviceDescriptionNameOrDict.isNil ){
			#devDesc, devDescName = this.class.findDeviceDesc( shortName: name );
			if ( devDesc.isNil ){
				this.warnNoDeviceDescriptionFileFound( devDescName );
				^this; // don't change anything, early return
			};
		}{
			if ( deviceDescriptionNameOrDict.isKindOf( Dictionary ) ){
				devDescName = deviceDescriptionNameOrDict.at( \device );
				devDesc = deviceDescriptionNameOrDict;
			}{
				#devDesc, devDescName = this.class.findDeviceDesc( deviceDescriptionNameOrDict );
			};
		};

		if ( mktlDevice.notNil ){
			// check whether new device spec mathces protocol
			if ( devDesc.at( \protocol ) != mktlDevice.class.protocol ){
				"WARNING: MKtl(%): protocol % of device description %, does match protocol % of device %. Keeping the device description as it was.\n".postf( name, devDesc.at( \protocol ), devDescName, mktlDevice.class.protocol, mktlDevice.deviceName );
				^this;
			};
			mktlDevice.cleanupElements;
		};

		this.prInitFromDeviceDescription( devDesc, devDescName );
		if ( mktlDevice.notNil ){
			mktlDevice.initElements( name );
		};

		this.changed( \elements );
	}

	closeDevice{
		if ( mktlDevice.notNil ){
			mktlDevice.closeDevice;
		};
		mktlDevice = nil;
	}

	checkWhetherDeviceIsThere{ |deviceName|
		var shortDeviceName, devDesc;

		if ( deviceName.notNil ){
			if ( deviceName.isKindOf( Symbol ) ){
				shortDeviceName = deviceName;
			}{
				shortDeviceName = MKtlDevice.findDeviceShortNameFromLongName( deviceName );
			}
		}{
			devDesc = MKtl.getDeviceDescription( deviceDescriptionName );
			if ( devDesc.notNil ){
				deviceName = devDesc.at( \device );
				shortDeviceName = MKtlDevice.findDeviceShortNameFromLongName( deviceName );
			};
		};
		^[shortDeviceName, deviceName, devDesc]
	}

	free{
		this.closeDevice;
		all.removeAt( name );
	}

	openDevice{ |deviceName, lookAgain=true|
		var shortName, devDesc, protocol;

		if ( this.mktlDevice.notNil ){
			"WARNING: Already a device opened for MKtl(%). Close it first with MKtl(%).closeDevice;\n".postf(name,name);
			^this;
		};

		#shortName, deviceName, devDesc = this.checkWhetherDeviceIsThere( deviceName );
		if ( shortName.isNil ){
			devDesc = this.class.getDeviceDescription(deviceDescriptionName);
			protocol = devDesc.at( \protocol );
			MKtlDevice.initHardwareDevices( lookAgain, protocol.bubble ); // this may be an issue, only look for appropriate protocol
			#shortName, deviceName, devDesc = this.checkWhetherDeviceIsThere( deviceName );
		};

		// [deviceName, shortName ].postln;
		this.prTryOpenDevice( shortName, devDesc );
	}

	warnNoDeviceDescriptionFileFound { |deviceName|
		warn( "Mktl could not find a device description file for device %. Please follow instructions in \"Tutorials/How_to_create_a_description_file\".openHelpFile;
""\n".format( deviceName.asCompileString) )
	}

	*prShortNamesToDeviceDescriptions {
		if( MKtl.allDevDescs.isNil ){ MKtl.loadAllDescs };
		^Dictionary.with(*
			MKtl.allDevDescs.getPairs.clump(2)
			.collect({ |xs| (MKtl.makeShortName(xs[1][\device]).asSymbol -> xs[0]) }))
	}

	/*
	init procedure:

	- load description from file into deviceDescription var
	- flattenDescription on each element
	- substitute each spec in the element for the real ControlSpec corresponding to it

	*/

	prInitFromDeviceDescription { |devDesc, devDescName|
		var deviceInfo;
		elementsDict = ();
		deviceInfo = devDesc.deepCopy;
		if ( deviceInfo.isNil )	{ // no device file found:
			this.warnNoDeviceDescriptionFileFound( name );
		}{
			this.prLoadDeviceDescription( deviceInfo );
		};
		if ( deviceDescriptionArray.notNil ){
			deviceDescriptionName = devDescName;
			this.makeElements;
			( "Created MKtl:" + name + "using device description" + deviceDescriptionName ).postln;
		};
	}

	storeArgs { ^[name] }
	printOn { |stream| this.storeOn(stream) }

	*loadAllDescs { |reload=false, verbose=false|
		if (reload) { allDevDescs = nil }{
			if ( verbose ){
				"Not reloading the MKtl descriptions; to do so, use MKtl.loadAllDescs( true )".inform;
			}
		};
		if ( allDevDescs.isNil ){
			reload = true;
			this.loadMatching("", verbose );
		};
		if ( reload ){
			"MKtl loaded all device descriptions - see them with MKtl.allDescriptions".inform;
		};
	}

	*loadMatching { |name,verbose = true|
		var paths = (defaultDeviceDescriptionFolder +/+
			("*" ++ name ++ "*.desc.scd")).pathMatch;
		var descNames = paths.collect{ |x| PathName(x).fileName.split($.)[0]; };
		if ( verbose ){
			"MKtl loadMatching - found: %.\n".postf(descNames);
		};
		paths.do (this.loadSingleDesc(_, verbose));
	}

	*loadSingleDesc { |path, verbose = false|
		// path has been tested to exist already
		var descName = path.basename.split($.)[0];
		var descDict = path.load;
		var isDesc = descDict.isKindOf(Dictionary) and: {
			descDict[\description].notNil };
		if (isDesc.not) {
			"% is not a valid description file.".format(path.basename).warn;
		} {
			descDict.put( \filepath, path );
			allDevDescs = allDevDescs ?? { IdentityDictionary.new };
			allDevDescs.put(descName.asSymbol, descDict);
			if ( verbose ){
				"MKtl: loaded %.".format(path.basename).inform;
			}
		};
		^descDict
	}

	*getDeviceDescription { |devName|
		var devDesc;
		this.loadAllDescs;
		devDesc = allDevDescs.at( devName );
		^devDesc ?? {
			// see if we can find it from the device name:
			allDevDescs
			.as(Array)
			.select{ |desc| desc[\type] != \template }
			.collect{ |desc| this.flattenDescription( desc ) }
			.detect{ |desc| desc[ \device ] == devName }
		}
	}

	prLoadDeviceDescription { |deviceInfo|
		var deviceFileName;
		var path;

		//"class: % deviceName: % deviceInfo:%".format(deviceName.class, deviceName, deviceInfo).postln;

		deviceDescriptionHierarch = deviceInfo[\description];
		deviceDescriptionArray = this.makeFlatDeviceDescription( deviceDescriptionHierarch );

		deviceDescriptionArray.pairsDo{ |key,elem|
			this.class.flattenDescription( elem )
		};

		// create specs
		deviceDescriptionArray.pairsDo {|key, elem|
			var foundSpec =  specs[elem[\spec]];
			if (foundSpec.isNil) {
				warn("Mktl - in description %, el %, spec for '%' is missing! please add it via:"
					"\nMktl.addSpec( '%', [min, max, warp, step, default]);\n"
					.format(name, key, elem[\spec], elem[\spec])
				);
			};
			elem[\specName] = elem[\spec];
			elem[\spec] = this.class.specs[elem[\specName]];
		};

		deviceInfo[\infoMessage] !? _.postln;
	}

	//traversal function for combinations of dictionaries and arrays
	prTraverse {
		var isLeaf = { |dict|
			dict.values.any({|x| x.size > 1}).not
		};

		var f = { |x, state, stateFuncOnNodes, leafFunc|

			if(x.isKindOf(Dictionary) ){
				if( isLeaf.(x) ) {
					leafFunc.( state , x )
				}{
					x.sortedKeysValuesCollect{ |val, key|
						f.(val, stateFuncOnNodes.(state, key), stateFuncOnNodes, leafFunc )
					}
				}
			} {
				if(x.isKindOf(Array) ) {
					if( x.first.isKindOf( Association ) ) {
						f.(IdentityDictionary.with( *x ), state, stateFuncOnNodes, leafFunc );
					} {
						x.collect{ |val, i|
							f.(val, stateFuncOnNodes.(state, i),  stateFuncOnNodes, leafFunc )
						}
					}
				} {
					Error("MKtl:prTraverse Illegal data structure in device description.\nGot object % of type %. Only allowed objects are Arrays and Dictionaries".format(x,x.class)).throw
				}
			}

		};
		^f
	}

	prMakeElementsFunc {
		var isLeaf = { |dict|
			dict.values.any({|x| x.size > 1}).not
		};

		var f = { |x, state, stateFuncOnNodes, leafFunc|

			if(x.isKindOf(Dictionary) ){
				if( isLeaf.(x) ) {
					leafFunc.( state , x )
				}{
					MKtlElementGroup(state,
						x.sortedKeysValuesCollect{ |val, key|
							f.(val, stateFuncOnNodes.(state, key), stateFuncOnNodes, leafFunc )
						}
					)
				}
			} {
				if(x.isKindOf(Array) ) {
					MKtlElementGroup(state,
						x.collect{ |val, i|
							if( val.isKindOf( Association ) ) {
								(val.key -> f.(val.value, 
									stateFuncOnNodes.(state, val.key),  stateFuncOnNodes, leafFunc ) 
								)
							} {
								f.(val, stateFuncOnNodes.(state, i),  stateFuncOnNodes, leafFunc )
							};
						}
					);
				} {
					Error("MKtl:prTraverse Illegal data structure in device description.\nGot object % of type %. Only allowed objects are Arrays and Dictionaries".format(x,x.class)).throw
				}
			}

		};
		^f
	}

	exploring{
		if ( mktlDevice.isNil ){ ^false };
		^mktlDevice.exploring;
	}

	explore{ |mode=true|
		if ( mktlDevice.notNil ){
			mktlDevice.explore( mode );
		}{
			"MKtl(%) has no open device, nothing to explore\n".postf( name );
		};
	}

	createDescriptionFile{
		if ( mktlDevice.notNil ){
			mktlDevice.createDescriptionFile;
		}{
			"MKtl(%) has no open device, cannot create description file\n".postf( name );
		};
	}


	prUnderscorify {
		^{ |a,b|
			if(a != "") {
				a++"_"++b.asString
			} {
				b.asString
			}
		};
	}

	makeFlatDeviceDescription { |devDesc|

		var flatDict = ();

		this.prTraverse.(devDesc, "", this.prUnderscorify, { |state, x| flatDict.put(state.asSymbol,  x) } );

		^flatDict.asKeyValuePairs

	}

	*postAllDescriptions {
		(MKtl.defaultDeviceDescriptionFolder +/+ "*").pathMatch
		.collect { |path| path.basename.splitext.first }
		.reject(_.beginsWith("_"))
		.do { |path| ("['" ++ path ++"']").postln }
	}

	*flattenDescription { |devDesc|
		// some descriptions may have os specific entries, we flatten those into the dictionary
		var platformDesc = devDesc[ thisProcess.platform.name ];
		if ( platformDesc.notNil ){
			platformDesc.keysValuesDo{ |key,val|
				devDesc.put( key, val );
			}
		};
		^devDesc;
	}

	*flattenDescriptionForIO { |eleDesc,ioType|
		// some descriptions may have ioType specific entries, we flatten those into the dictionary
		var ioDesc = eleDesc[ thisProcess.platform.name ];
		if ( ioDesc.notNil ){
			ioDesc.keysValuesDo{ |key,val|
				eleDesc.put( key, val );
			}
		};
		^eleDesc;
	}


	elementDescriptionFor { |elname|
		^deviceDescriptionArray[deviceDescriptionArray.indexOf(elname) + 1]
	}

	postDeviceDescription {
		deviceDescriptionArray.pairsDo {|a, b| "% : %\n".postf(a, b); }
	}

	makeElements {
		var leafFunc;
		this.elementNames.do{|key|
			elementsDict[key] = MKtlElement(this, key);
		};
		leafFunc = { |finalKey, x| elementsDict[finalKey.asSymbol] };
		MKtlElement.addGroupsAsParent = true;
		elements = this.prMakeElementsFunc.(deviceDescriptionHierarch, "", this.prUnderscorify, leafFunc );
		MKtlElement.addGroupsAsParent = false;

	}

	// needed for fixing elements that are not present for a specific OS
	replaceElements{ |newelements|
		elementsDict = newelements;
	}

	// convenience methods
	defaultValueFor { |elName|
		^this.elementsDict[elName].defaultValue
	}

	makeElementName { |args|
		var n = args.size;
		^(args[..n-2].collect({ |x| x.asString++"_"}).reduce('++')++args.last).asSymbol
	}

	elementAt { |...args|
		^elements.deepAt1(*args)
	}

	at { |index|
		^elements.at( index );
	}

	// should filter: those for my platform only
	elementNames {
		if ( elementsDict.isEmpty ){
			^(0, 2 .. deviceDescriptionArray.size - 2).collect (deviceDescriptionArray[_])
		}{
			^elementsDict.keys.asArray;
		}
	}

	elementsOfType { |type|
		^elementsDict.select { |elem|
			elem.elementDescription[\type] == type
		}
	}

	elementsNotOfType { |type|
		^elementsDict.select { |elem|
			elem.elementDescription[\type] != type
		}
	}

	inputElements{
		^elementsDict.select { |elem|
			[ \in, \inout ].includes( elem.elementDescription[\ioType] ?? \in);
		}
	}

	outputElements{
		^elementsDict.select { |elem|
			[ \out, \inout ].includes( elem.elementDescription[\ioType] ?? \in);
		}
	}

	allElements {
		^elementsDict.asArray
	}


	// from MAbstractKtl:

	prMatchedElements { |elementKey|
		if ( Main.versionAtLeast( 3.5 ) ){
			^elementsDict.asArray.select{ |elem| elem.name.matchOSCAddressPattern(elementKey) }
		}{
			^elementsDict.asArray.select{ |elem| elementKey.asString.matchRegexp( elem.name.asString ) };
		}
	}

	prMatchDo{ |match, elementKey, func|
		if( match ) {
			//match only works on dev versions at the moment.
			this.prMatchedElements(elementKey).do{ |element|
				func.value(element)
			}
		} {
			func.value(elementsDict[elementKey])
		}
	}

	removeAllFromElems {
		elementsDict.do( _.reset )
	}

	printElementNames{
		(
			"\nElements available for %:\n".format(this.name)
			++ elementsDict.keys.as(Array).sort
			.collect{ |s| s.asString.padRight(14) }
			.clump(4)
			.collect{ |xs| xs.reduce('++') ++ "\n" }
			.reduce('++')
			++ "\n"
		).postln
	}

	/*
	recordRawValue { |key,value|
	//		recordFunc.value( key, value );
	}
	*/

	rawValueAt { |elName|
		if (elName.isKindOf(Collection).not) {
			^elementsDict.at(elName).rawValue;
		};
		^elName.collect { |name| this.rawValueAt(name) }
	}

	valueAt { |elName|
		if (elName.isKindOf(Collection).not) {
			^elementsDict.at(elName).value;
		};
		^elName.collect { |name| this.valueAt(name) }
	}

	setRawValueAt { |elName, val|
		if (elName.isKindOf(Collection).not) {
			^this.at(elName).rawValue_(val);
		};
		[elName, val].flop.do { |pair|
			elementsDict[pair[0].postcs].rawValue_(pair[1].postcs)
		};
	}

	setValueAt { |elName, val|
		if (elName.isKindOf(Collection).not) {
			^this.at(elName).value_(val);
		};
		[elName, val].flop.do { |pair|
			elementsDict[pair[0].postcs].value_(pair[1].postcs)
		};
	}

	reset{
		elementsDict.do( _.reset )
	}

	prArgToElementKey { |argm|
		//argm is either a symbol, a string or an array
		^switch( argm.class)
			{ Symbol }{ argm }
			{ String }{ argm.asSymbol }
			{ (argm[..(argm.size-2)].inject("",{ |a,b| a++b.asString++"_"}).asSymbol ++ argm.last.asString).asSymbol }
	}


	//also can be used to simulate a non present hardware
	// receive { |key, val|
	// 	elementsDict[ key ].update( val )
	// }

	send { |key, val|
		if ( mktlDevice.notNil ){
			mktlDevice.send( key, val );
		}
	}


}
