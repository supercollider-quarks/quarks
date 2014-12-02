InputRouter {

	classvar <>defaultPath = "~/Documents/SuperCollider";
	classvar <>all;

	var <server;
	var <name, <outputLabels, <inputLabels;
	var <synths;
	var <>path;
	var <inOffset = 0, <outOffset = 0, <private = true;
	var <meter = true, <isRunning = false;
	
	var <settings, <inputLevels;

	
	var <meterLevels, <updateFreq = 10;
	
	var <>surviveCmdPeriod = true;
	var <>surviveServerQuit = true;
	
	var <useFilters = true, <useCompression = true;
	var <filterOrder = 2;
	var <compressionKnee = 10;
	var <compressionRelease = 0.25;
	var <filterSettings, <compressionSettings;
	
	var <testSynths;
	
	*new { |server, name, outputLabels|
		^super.newCopyArgs( server, name, outputLabels ).init.addToAll;
	}
	
	addToAll { all = all.add( this ); }
	
	*unique { |server, name, outputLabels|
		var out;
		server = server ? Server.default ? Server.local;
		name = (name ? server.name).asSymbol;
		out = all.detect({ |item| item.name == name });
		^out ?? { this.new( server, name, outputLabels ) };
	}
	
	*remove { |name|
		var out;
		if( name.isKindOf( Server ) ) { name = name.name };
		name = name ?? { (Server.default ? Server.local).name };
		name = name.asSymbol;
		out = all.detect({ |item| item.name == name });
		if( out.notNil ) { out.stop; all.remove( out ); };
	}
	
	*removeAll { 
		all.do({ |item| item.stop; });
		all = nil;
	}
	
	remove {
		this.class.remove(this.name);
	}
	
	gui { |parent, bounds| ^InputRouterGUI( this, parent, bounds ) }
	
	init { 
		server = server ? Server.default ? Server.local;
		name = (name ? server.name).asSymbol;
		if( outputLabels.isNumber )	{ outputLabels = ("input " ++ _ )!outputLabels; };
		outputLabels = (outputLabels ? ["input 0"]).collect(_.asString);
		inputLabels = server.options.numInputBusChannels.collect({ |i|
			"AudioIn %".format( i+1 );
		});
		
		this.initSettings;
		
		path = path ?? 
			{ thisProcess.nowExecutingPath !? { thisProcess.nowExecutingPath.dirname; }; };
		this.readSettings; // replace with settings from file if available
	}
	
	initSettings {
		settings = [_]!outputLabels.size;
		inputLevels = settings.collect( _.collect( 0 ) );
		filterSettings = settings.collect( _.collect( [ 0, 20, 0, 22050] ) );
		compressionSettings = settings.collect( _.collect( [ -12, 0, 0.05 ] ) );
	}
	
	start { // calls stop internally -- also used for restart
		this.stop;
		this.prStart;	
		this.changed( \start );
		ServerTree.add( this, server );
	}
	
	stop { 
		this.freeSynths; 
		this.prAfterStop;
	}
	
	prStart {
		if( server.serverRunning ) {	
			{
				this.synthDef.send(server);
				server.sync;
				isRunning = true;
				this.startSynths;
				this.startResponders;
				ServerQuit.add( this, server );
			}.fork;
		} { 
			isRunning = true;
			ServerQuit.add( this, server );
		};
	}
	
	prAfterStop {
		synths = nil;
		isRunning = false;
		this.changed( \stop ); 
		ServerTree.remove( this, server ); 
		ServerQuit.remove( this, server );
	}
	
	inputLabels_ { |array|
		inputLabels = array;
		this.changed( \inputLabels );
	}
	
	useFilters_ { |bool|
		bool = bool ? useFilters;
		useFilters = bool;
		if( isRunning ) { this.start; };
		this.changed( \useFilters, bool );
	}
	
	useCompression_ { |bool|
		bool = bool ? useCompression;
		useCompression = bool;
		if( isRunning ) { this.start; };
		this.changed( \useCompression, bool );
	}
	
	filterOrder_ { |bool|
		bool = bool ? 2;
		if( filterOrder != bool ) {
			filterOrder = bool;
			if( isRunning ) { this.start; };
		};
		this.changed( \filterOrder, bool );
	}
	
	compressionKnee_ { |knee|
		knee = knee ? 10;
		compressionKnee = knee;
		if( isRunning ) {
			synths.do(_.do( _.set( \knee, compressionKnee ) ) );
		};
		this.changed( \compressionKnee, compressionKnee );
	}
	
	compressionRelease_ { |time|
		time = time ? 0.25;
		compressionRelease = time;
		if( isRunning ) {
			synths.do(_.do( _.set( \release, compressionRelease ) ) );
		};
		this.changed( \compressionRelease, compressionRelease );
	}
	
	test { |input = 0, dB = -12, type = 'pink'| // 'pink', 'white', float (sine hz)
		
		if( isRunning.not ) {
			"test: not running".warn;
		} {
			this.endTest( input );
			testSynths = testSynths ?? { Order() };
			testSynths[ input ] = 
				switch ( type,
					\pink, { 
						 { Out.ar( 
							 NumOutputBuses.ir + input,
							 PinkNoise.ar( dB.dbamp )
							) 
						}.play( RootNode(server) );
					},
					\white, { 
						 { Out.ar( 
							 NumOutputBuses.ir + input,
							 WhiteNoise.ar( dB.dbamp )
							) 
						}.play( RootNode(server) );
					
					},
					{ 
						 { Out.ar( 
							 NumOutputBuses.ir + input,
							 SinOsc.ar( type, 0, dB.dbamp )
							) 
						}.play( RootNode(server) );
					
					}
				);
					
			} 
	}
	
	endTest { |input|
		if( input.isNil ) {
			testSynths.do(_.free); 
			testSynths = nil;
		} {
			if( testSynths.notNil && { testSynths[ input ].notNil } ) {
				testSynths[ input ].free; 
				testSynths[ input ] = nil;
			};
		};
	}
		
	doOnServerTree { 
		testSynths = nil;
		if( surviveCmdPeriod ) { 
			this.prStart;
		} { 
			this.prAfterStop;
		};
	}
	
	doOnServerQuit {
		if( surviveServerQuit.not ) {
			this.prAfterStop;
		 };
	}
	
	startSynths {
			synths = settings.collect({ |item, i|
				item.collect({ |iitem, ii|
				 	Synth.before( server, this.synthDefName, 
				 		[	// per channel:
				 			\in, iitem,
				 			\out, i,  
				 			\amp, (1/item.size),
				 			\gain, (inputLevels[i][ii] ? 0).dbamp,
				 			
				 			// global:
				 			\meter, meter.binaryValue,
				 			\updateFreq, updateFreq,
				 			\inOffset, inOffset, 
				 			\outOffset, outOffset, 
				 			\private, private.binaryValue,
				 			
				 			// filter
				 			\lowCut, filterSettings[i][ii][0],
				 			\lowFreq, filterSettings[i][ii][1],
				 			\hiCut, filterSettings[i][ii][2],
				 			\hiFreq, filterSettings[i][ii][3],
				 			
				 			// compression
				 			\thresh, compressionSettings[i][ii][0],
				 			\amt, compressionSettings[i][ii][1],
				 			\attack, compressionSettings[i][ii][2],
				 			
				 		 	] ); 
				});
			});
			
			meterLevels = synths.collect({ |item|
					item.collect({ |synth|
						[0,0]; // level, peak
					});
			});
	}
	
	freeSynths { synths !? { synths.flatten(1).do( _.free ); synths = nil; }; }
	
	startResponders { // only after synths have started
		synths.do({ |item, output|
			item.do({ |synth, index|
				synth.onReply_({ |value|
					var val, peak;
					#val, peak = value;
					val = (val.max(0.0) * (updateFreq/server.sampleRate)).sqrt;
					meterLevels[ output ][ index ] = [val, peak].ampdb;
					this.changed( \meterLevel, output, index, 
						meterLevels[ output ][ index ] ); // enable easy mvc
				}, "/%_meterLevel".format( this.synthDefName ).asSymbol );
			});	
		});
	}
	
	setSynth { |output = 0, index = 0 ...pairs|
		synths !? { synths[output][index].set( *pairs ); };
	}
	
	setAllSynths { |key = \private, val = 1|
		synths !? { synths.do({ |sn| sn.do( _.set( key, val ) ) }); };
	}
	
	
	setLevel { |output = 0, index = 0, dB = 0|
		this.setSynth( output, index, \gain, dB.dbamp );
		inputLevels[ output ][ index ] = dB;
		this.changed( \level, output, index, dB );
	}
	
	setFilter { |output = 0, index = 0, lowCut, lowFreq, hiCut, hiFreq|
		var current;
		
		current = filterSettings[ output ][ index ];
		
		lowCut = (lowCut ? current[0]).binaryValue;
		lowFreq = lowFreq ? current[1];
		hiCut = (hiCut ? current[2]).binaryValue;
		hiFreq = hiFreq ? current[3];
		
		this.setSynth( output, index, 
			\lowCut, lowCut,
			\lowFreq, lowFreq,
			\hiCut, hiCut,
			\hiFreq, hiFreq 
		); 
		
		filterSettings[ output ][ index ] = [ lowCut, lowFreq, hiCut, hiFreq ];
		this.changed( \filter, output, index,  lowCut, lowFreq, hiCut, hiFreq );
	}
	
	setCompression { |output = 0, index = 0, thresh, amt, attack|
		var current;
		
		current = compressionSettings[ output ][ index ];
		
		thresh = thresh ? current[0];
		amt = amt ? current[1];
		attack = attack ? current[2];
		
		this.setSynth( output, index, 
			\thresh, thresh,
			\amt, amt,
			\attack, attack
		); 
		
		compressionSettings[ output ][ index ] = [ thresh, amt, attack ];
		this.changed( \compression, output, index, thresh, amt, attack );
	}
	
	setInput { |output = 0, index = 0, in = 0|
		this.setSynth( output, index, \in, in );
		settings[ output ][ index ] = in;
		this.changed( \input, output, index, in );
	}

	meter_ { |bool = true|
		meter = bool.booleanValue;
		this.setAllSynths( \meter, meter.binaryValue );
		this.changed( \meter, meter );
	}
	
	settings_ { |newSettings|
		
		if( newSettings.size < outputLabels.size ) { 
			newSettings = newSettings ++ settings[ newSettings.size-1.. ] 
		};
		
		if( newSettings.size > outputLabels.size ) { 
			newSettings = newSettings[ ..outputLabels.size-1 ]; 
		};
		
		newSettings = newSettings.collect(_.asCollection);
			
		settings = newSettings;
		
		inputLevels = settings.collect({ |item, i|
			item.collect({ |iitem, ii|
				inputLevels[i][ii] ? 0;
			});
		});
		
		filterSettings = settings.collect({ |item, i|
			item.collect({ |iitem, ii|
				filterSettings[i][ii] ? [ 0, 20, 0, 22050 ];
			});
		});
		
		compressionSettings = settings.collect({ |item, i|
			item.collect({ |iitem, ii|
				compressionSettings[i][ii] ? [ -12, 0, 0.05 ];
			});
		});
		
		this.changed( \settings, settings );
		if( isRunning ) { this.start; }
	}
	
	addInput { |output = 0, in, level = 0|
		settings[ output ] = settings[ output ].add( in ? ((settings[ output ].last ? -1) + 1) );
		inputLevels[ output ] = inputLevels[ output ].add( level );
		this.settings = settings;
	}
	
	removeInput { |output = 0, i = 0|
		if( settings[ output ].size >= (i+1) ) { 
			settings[ output ].removeAt( i );
			inputLevels[ output ].removeAt( i );
			filterSettings[ output ].removeAt( i );
			compressionSettings[ output ].removeAt( i );
			this.settings = settings;
		} { 
			"%:removeInput : index out of range".warn;
		};
	}
		
	private_ { |bool = true|
		private = bool.booleanValue;
		this.setAllSynths( \private, private.binaryValue );
		this.changed( \private, private );
	}
	
	inOffset_ { |val = 0|
		inOffset = val;
		this.setAllSynths( \inOffset, inOffset );
		this.changed( \inOffset, inOffset );
	}
	
	outOffset_ { |val = 0|
		outOffset = val;
		this.setAllSynths( \outOffset, outOffset );
		this.changed( \outOffset, outOffset );
	}
	
	synthDefName { ^this.class.name.asString }
	
	synthDef {		 
		^SynthDef( this.synthDefName, { |in = 0, out = 0, amp = 1, gain = 1, 
				private = 1, outOffset = 0, inOffset = 0, updateFreq = 10, meter = 1|
			var input, imp;
			input = SoundIn.ar( in + inOffset );
			
			if( useFilters == true ) {
				
				input = if( \lowCut.kr( 0 ),
					BLowCut.ar( input, \lowFreq.kr( 20 ).lag(1).clip(20,22050), filterOrder ),
					input );
					
				input = if( \hiCut.kr( 0 ), 
					BHiCut.ar( input, \hiFreq.kr( 22050 ).lag(1).clip(20,22050), filterOrder ),
					input );
					
			};
			
			if( useCompression == true ) {
				input = SoftKneeCompressor.ar( input, input, 
					\thresh.kr( -12 ), (1 - \amt.kr(0)).squared, \knee.kr( compressionKnee ), 
					\attack.kr(0), \release.kr( compressionRelease )
				);	
			};
			
			
			input = input * amp * gain;
			
			imp = Impulse.ar(updateFreq) * meter;
			
			SendReply.ar(imp, "/%_meterLevel".format( this.synthDefName ).asSymbol,
				[ 	RunningSum.ar(input.squared, SampleRate.ir / updateFreq ),
					Peak.ar(input, Delay1.ar(imp)).lag(0, 3)
				] );
			
			// to private bus if private == 1
			out = out + ( private * (NumInputBuses.ir + NumOutputBuses.ir) ); 
			Out.ar( out + outOffset, input );
			});
		}
	
	settingsPath { ^( path ?? { 
			(thisProcess.nowExecutingPath !? { thisProcess.nowExecutingPath.dirname; }) ?
			defaultPath } ).standardizePath +/+ 
		"%_%_prefs.txt".format( name, this.class.name );
	}


	// read / write
	
	readSettings {		
		var readPath;
		readPath = this.settingsPath;	
		if( File.exists( readPath ) )
			{ "reading file %\n".postf( readPath );
			File.use( readPath,
				"r",
				{ |f| var evt;
					evt = f.readAllString.interpret;
					settings = evt[ \settings ];
					inputLabels = evt[ \inputLabels ];
					
					inputLevels = evt[ \inputLevels ] ??  
						{ settings.collect( _.collect( 0 ) ) };
						
					compressionSettings = evt[ \compressionSettings ] ??  
						{ settings.collect( _.collect( [ -12, 0, 0.05 ] ) ) };
						
					filterSettings = evt[ \filterSettings ] ??  
						{ settings.collect( _.collect( [ 0, 20, 0, 22050 ] ) ) };
						
					useCompression = evt[ \useCompression ] ? useCompression;
					useFilters = evt[ \useFilters ] ? useFilters;
					
					filterOrder = evt[ \filterOrder ] ? filterOrder;
					compressionKnee =  evt[ \compressionKnee ] ? compressionKnee;
					compressionRelease = evt[ \compressionRelease ] ? compressionRelease;
					
					this.settings = settings;
			});
		};	
	}
	
	writeSettings {	
		this.settingsPath.dirname.makeDir;
		File.use( this.settingsPath,
		"w",
		{ |f|
			f.putString( ( 
					settings: settings, 
					inputLabels: inputLabels,
					inputLevels: inputLevels,
					compressionSettings: compressionSettings,
					filterSettings: filterSettings,
					useCompression: useCompression,
					useFilters: useFilters,
					filterOrder: filterOrder,
					compressionKnee: compressionKnee,
					compressionRelease: compressionRelease
				).asCompileString );
		});
	}
	
}