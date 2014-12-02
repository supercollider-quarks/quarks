// A simple window for controlling the MIDIClient
// will init client upon creation and give control
// over which device input to connect to a predefined port
//
// wslib 2005

MIDIWindow {
	
	classvar <currentDevice = 0;
	classvar <inIsOn = nil;
	classvar <>toPort = 0;
	classvar <window = nil;
	classvar <sourceNames = nil;
	
	
	*new { arg connect = false, current; // connect can be array or single
		var clientConnectInButton, portPopUp, clientPopUp;
		var nSources, noMidi = false;
		var getSourceNames;
		var font;
		var connectAllButton;
		// only one port so far
				
		/* if(MIDIClient.initialized.not)
			{ MIDIClient.init; }; */
			
				
	if(window.isNil) {
		MIDIClient.init;
		
		font = Font("Helvetica", 10);
		
		nSources = MIDIClient.sources.size;
		if(nSources == 0) { noMidi=true };
		if(noMidi)
			{currentDevice = 0;
			if(connect.size == 0) {connect = connect.dup(1); };
			inIsOn = inIsOn ? [false]; } {
			currentDevice = current ? currentDevice;
			if(connect.size == 0) {connect = connect.dup(nSources); };
			inIsOn = { |i|( connect[i] ? false) or: ((inIsOn ? [])[i] ? false); }!nSources;
			};
		
		getSourceNames = {
			if(noMidi) { sourceNames = ["(no sources available" /*)*/]; }
				{ sourceNames =  MIDIClient.sources.collect({|item, i| 
				if(inIsOn[i])
					{(item.device + item.name)
						.replaceItems( "()/", "[]:" ) + ":" + toPort }
					{(item.device + item.name).replaceItems( "()/", "[]:" ) }
				}); };
			sourceNames;
		};
		
		getSourceNames.value;
		
		//sourceNames.postln;
		
		inIsOn.do({ |item, i|
			if(item) {MIDIIn.connect(toPort, i) } });

		window = Window("MIDIClient", Rect(299, 55, 200, 48), false);
		window.addFlowLayout;
		
		
		clientPopUp = PopUpMenu(window, 192@18)
			.items_( sourceNames )
			.font_( font )
			.value_(currentDevice)
			.action_({ |popup|
				currentDevice = popup.value;
				clientConnectInButton.value_(inIsOn[currentDevice].binaryValue); 
				popup.items_( getSourceNames.value );
				
				[	{popup.background_(Color.clear)},
					{popup.background_(Color.green.alpha_(0.2))}]
					[inIsOn[currentDevice].binaryValue].value; 
			})
			.stringColor_(Color.black)
			.background_(Color.clear);
			
		/*
		StaticText(window, Rect(95, 30, 55, 18))
				.string_( "MIDIIn port: " )
				.font_( font );
		*/
			
		clientConnectInButton = Button(window, 61@18)
			.states_([[ "Connect" ],
				[ "Connect", Color.black, Color.green.alpha_(0.2) ]])
			.font_( font )
			.value_(inIsOn[currentDevice].binaryValue)
			.action_({ |button|
				case {button.value == 1}
					{MIDIIn.connect(toPort,currentDevice);
						inIsOn[currentDevice] = true;
						clientPopUp.action.value(clientPopUp);
						
					}
					{button.value == 0}
					{MIDIIn.disconnect(toPort,currentDevice);
						inIsOn[currentDevice] = false;
						clientPopUp.action.value(clientPopUp);}
				});
				
		connectAllButton = Button(window, 61@18)
			.states_([[ "All" ],
				[ "All", Color.black, Color.green.alpha_(0.2) ]])
			.font_( font )
			.value_(inIsOn[currentDevice].binaryValue)
			.action_({ |button|
				switch( button.value,
					1, { MIDIClient.sources({ |item, i|
							if( inIsOn[i] != true )
								{ MIDIIn.connect( toPort, item );
								  inIsOn[i] = true }; 
							});
 					  clientPopUp.action.value(clientPopUp);
					  },
					0, { MIDIClient.sources({ |item, i|
							if( inIsOn[i] != false )
								{ MIDIIn.disconnect( toPort, item );
								  inIsOn[i] = false
								}; 
							});
						clientPopUp.action.value(clientPopUp);
					});
				});
				
		/*
		StaticText(window, Rect(95, 30, 61, 18))
				.string_("")
				//.background_( Color.green )
				.font_( font );
		*/
				
		if(noMidi) {clientConnectInButton.enabled=false; clientPopUp.enabled=false; }
			{ clientPopUp.action.value(clientPopUp);};
			
		Button(window, 61@18).states_([["restart"]])
			.font_( font )
			.action_({ window.close; window = nil; { MIDIWindow.new }.defer(0.2) });
			
		};
		
		window.onClose_({ window = nil });
		window.front;
		
		
		}
	
}