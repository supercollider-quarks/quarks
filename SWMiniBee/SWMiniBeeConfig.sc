SWMiniBeeID{

	var <>serial;
	var <>configLabel;
	var <>nodeID;
	var <>configured = 2;
	var <>status;
	var <>libversion;
	var <>libcaps;
	var <>revision;

	*new{ |ser,cl,nid,conf=2,st,libv,rev|
		^super.newCopyArgs( ser, cl, nid, conf, st, libv, rev );
	}

	storeOn { arg stream;
		stream << this.class.name << ".new(" << 
		serial.asCompileString << "," << configLabel.asCompileString << "," 
		<< nodeID << "," << configured << "," << status << "," 
		<< libversion << "," << revision.asCompileString << "," << libcaps << ")"
	}

}

SWMiniBeeConfig{

	classvar <pinTypes;

	var <label;
	var <>msgInterval;
	var <>samplesPerMsg;
	var <>pinConfig;

	var <>hive;

	var <noInputs,<noOutputs;
	var <inputSize;
	//	var <noCustomOut;
	var <customSizes;

	var <numberOfPins = 19;

	*initClass{
		pinTypes = [
			\unconfigured,
			\digitalIn, \digitalOut, 
			\analogIn, \analogOut, \analogIn10bit,
			\SHTClock, \SHTData,
			\TWIClock, \TWIData,
			\Ping,
			\CustomIn, \CustomOut
		];
	}

	*getPinCID{ |name|
		^pinTypes.indexOf( name );
	}

	*getPinCaps{ |label|
		var caps;
		caps = switch( label,
			\SDA_A4, { this.filterPinTypes( [\TWIClock, \analogOut ]); },
			\SCL_A5, { this.filterPinTypes( [\TWIData, \analogOut ]); },
			\A0, { this.filterPinTypes( [\TWIData, \TWIClock, \analogOut ]); },
			\A1, { this.filterPinTypes( [\TWIData, \TWIClock, \analogOut ]); },
			\A2, { this.filterPinTypes( [\TWIData, \TWIClock, \analogOut ]); },
			\A3, { this.filterPinTypes( [\TWIData, \TWIClock, \analogOut ]); },
			\A6, { this.filterPinTypes( [\TWIData, \TWIClock, \analogOut ]); },
			\A7, { this.filterPinTypes( [\TWIData, \TWIClock, \analogOut ]); },
			\D3, { this.filterPinTypes( [\TWIData, \TWIClock, \analogIn, \analogIn10bit ]); },
			\D4, { this.filterPinTypes( [\TWIData, \TWIClock, \analogIn, \analogIn10bit, \analogOut ]); },
			\D5, { this.filterPinTypes( [\TWIData, \TWIClock, \analogIn, \analogIn10bit ]); },
			\D6, { this.filterPinTypes( [\TWIData, \TWIClock, \analogIn, \analogIn10bit ]); },
			\D7, { this.filterPinTypes( [\TWIData, \TWIClock, \analogIn, \analogIn10bit, \analogOut ]); },
			\D8, { this.filterPinTypes( [\TWIData, \TWIClock, \analogIn, \analogIn10bit, \analogOut ]); },
			\D9, { this.filterPinTypes( [\TWIData, \TWIClock, \analogIn, \analogIn10bit ]); },
			\D10, { this.filterPinTypes( [\TWIData, \TWIClock, \analogIn, \analogIn10bit ]); },
			\D11, { this.filterPinTypes( [\TWIData, \TWIClock, \analogIn, \analogIn10bit ]); },
			\D12, { this.filterPinTypes( [\TWIData, \TWIClock, \analogIn, \analogIn10bit, \analogOut ]); },
			\D13, { this.filterPinTypes( [\TWIData, \TWIClock, \analogIn, \analogIn10bit, \analogOut ]); }
			);
		^caps;
	}

	*filterPinTypes{ |filters|
		var caps = pinTypes;
		filters.do{ |it|
			caps = caps.reject{ |jt| jt == it };
		};
		//	caps.postln;
		^caps;
	}
	
	*new{
		^super.new.init;
	}

	*newFrom{ |label,msgInt, smp, pinC|
		^super.newCopyArgs( label, msgInt, smp, pinC );
	}

	from{ |conf|
		label = conf.label;
		msgInterval = conf.msgInterval;
		samplesPerMsg = conf.samplesPerMsg;
		pinConfig = conf.pinConfig;

		//		noInputs = conf.noInputs;
		//		noOutputs = conf.noOutputs;
		//		inputSize = conf.inputSize;
		this.parseConfig;
	}

	addCustom{ |custom|
		var customPins;
		//	customSizes = [];
		if ( custom[0] > 0 ){
			customSizes = Array.fill( custom[0], custom[1]/custom[0] );
		}{
			customSizes = [];
		};
		customPins = custom.clump(2).copyToEnd(1);
		customPins.do{ |it,i|
			if ( it[1] > 0 ){
				pinConfig[ it[0] ] = \CustomIn;
				noInputs = noInputs + 1;
				customSizes = customSizes ++ it[1];
			}{
				pinConfig[ it[0] ] = \CustomOut;
			};
		}
	}

	storeOn { arg stream;
		stream << this.class.name << ".newFrom(" << label.asCompileString << "," << msgInterval << "," << samplesPerMsg << "," << pinConfig.asCompileString << ")"
	}

	init{
		pinConfig = Array.fill( numberOfPins, { \unconfigured } );
		customSizes = [];
	}

	label_{ |lb|
		label = lb.asSymbol;
	}

	getDataFunc{
		var analog,digital;
		var dataFunc;
		// order of data is: custom, analog in (8 or 10 bit), digital, twi, sht, ping
		var sizes = customSizes;
		var scales = Array.fill( customSizes.size, 1 );

		analog = pinConfig.select{ |it| (it == \analogIn) or: (it == \analogIn10bit) };
		analog.do{ |it|
			if ( it == \analogIn ){
				sizes = sizes.add( 1 );
				scales = scales.add( 255 );
			}{
				sizes = sizes.add( 2 );
				scales = scales.add( 1023 );
			}
		};

		digital = pinConfig.select{ |it| (it == \digitalIn) };
		digital.do{ |it|
				sizes = sizes.add( 1 );
				scales = scales.add( 1 );
		};

		if ( pinConfig.includes( \TWIData ) ){
			sizes = sizes ++ [1,1,1];
			scales = scales ++ [255,255,255];
		};

		if ( pinConfig.includes( \SHTData ) ){
			sizes = sizes ++ [2,2];
			scales = scales ++ [1,1];
		};

		if ( pinConfig.includes( \Ping ) ){
			sizes = sizes.add( 2 );
			scales = scales ++ [20000];
		};

		dataFunc = { |indata|
			indata = indata.clumps( sizes );
			indata.collect{ |it,i|
				if ( it.size == 2 ){
					(it * [256,1]).sum / scales[i]; // 10 bit; may not be correct for ping
				}{
					it/ scales[i];
				};
			}.flatten;
		}
		^dataFunc;
	}

	parseConfig{
		noInputs = 0;
		noOutputs = 0;
		inputSize = 0;
		pinConfig.do{ |it|
			switch( it,
				\analogIn, { noInputs = noInputs + 1; inputSize = inputSize + 1; },
				\analogIn10bit, { noInputs = noInputs + 1; inputSize = inputSize + 2; },
				\digitalIn, { noInputs = noInputs + 1; inputSize = inputSize + 1; },
				\analogOut, { noOutputs = noOutputs + 1; },
				\digitalOut, { noOutputs = noOutputs + 1; },
				\SHTData, { noInputs = noInputs + 2; inputSize = inputSize + 4; },
				\TWIData, { noInputs = noInputs + 3; inputSize = inputSize + 3; },
				\Ping, { noInputs = noInputs + 1; }
			)
		}
	}

	getConfigMsg{
		var pins,mint;
		// config has things like:
		// noInputs
		// samplesPerMsg
		// msgInterval
		// scale
		// pins
		pins = pinConfig.collect{ |it| SWMiniBeeConfig.getPinCID( it ) }.replace( 0, 200 );
		mint = [ (msgInterval / 256).floor.asInteger, (msgInterval%256).asInteger ];
		^( mint ++ samplesPerMsg ++ pins );
	}

	checkConfig{
		// check if SHTData is matched by SHTClock, and vice versa
		// check if TWIData is matched by TWIClock, and vice versa
		var hasSHTData, hasSHTClock, shtOk = true;
		var hasTWIData, hasTWIClock, twiOk = true;
		var configStatus = "";

		hasSHTClock = pinConfig.select{ |it| it == \SHTClock };
		if ( hasSHTClock.size == 1 ){
			shtOk = false;
			hasSHTData = pinConfig.select{ |it| it == \SHTData };
			if ( hasSHTData.size == 1 ){
				shtOk = true;
			}{
				if ( hasSHTData.size > 1 ){
					configStatus = configStatus ++ "Err: More than one SHTData pin defined!"

				}{
					configStatus = configStatus ++ "Err: No SHTData pin defined!"
				}
			}
		}{
			if ( hasSHTClock.size == 0 ){
				// check for data pin
				hasSHTData = pinConfig.select{ |it| it == \SHTData };
				if ( hasSHTData.size > 0 ){
					shtOk = false;
					configStatus = configStatus ++ "Err: No SHTClock pin defined!"
				}
			}{
				// more than one!
				shtOk = false;
				configStatus = configStatus ++ "Err: more than one SHTClock pin defined!"
			};
		};


		hasTWIClock = pinConfig.select{ |it| it == \TWIClock };
		if ( hasTWIClock.size == 1 ){
			twiOk = false;
			hasTWIData = pinConfig.select{ |it| it == \TWIData };
			if ( hasTWIData.size == 1 ){
				twiOk = true;
			}{
				if ( hasTWIData.size > 1 ){
					configStatus = configStatus ++ "Err: More than one TWIData pin defined!"

				}{
					configStatus = configStatus ++ "Err: No TWIData pin defined!"
				}
			}
		}{
			if ( hasTWIClock.size == 0 ){
				// check for data pin
				hasTWIData = pinConfig.select{ |it| it == \TWIData };
				if ( hasTWIData.size > 0 ){
					twiOk = false;
					configStatus = configStatus ++ "Err: No TWIClock pin defined!"
				}
			}{
				// more than one!
				twiOk = false;
				configStatus = configStatus ++ "Err: more than one TWIClock pin defined!"
			};
		};
		configStatus.postln;
		^[ twiOk, shtOk, configStatus ];
	}

	store{
		var newconfig;
		if ( hive.notNil ){
			newconfig = SWMiniBeeConfig.newFrom( label, msgInterval, samplesPerMsg, pinConfig.copy );
			hive.addConfig( newconfig );
			//	hive.replaceConfigID( newconfig );
		};
	}

	makeGui{
		^SWMiniBeeConfigGui.new( this );
	}

}

