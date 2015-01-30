//////////// MKtl - a model for a (hardware or gui-based) controller //////////

	// One can find all currently available devices on MIDI, HID;
	//  ... others later: hardware  (Serial, OSC, Wii, Keyboard)
	//  ... virtual control devices (GUI, SETO, DataNode)
MKtl.find;

	// post known devices that have descriptions:
MKtl.postAllDescriptions;



// There are two ways of making MKtls:
	
// 1	// FOR CURRENTLY PRESENT DEVICES: 
	// based on an existing uid, 
	// make a MIDIMKtl or HIDMKtl:
	// this gets the description for the device on that USB port ID;
	// replace ??? with your preferred name for that device.
MIDIMKtl('?', -616253900); 


// 2	// based on a description name, make a blank MKtl with those specs; 
	// this should work without the device's physical presence.
	// this gets the description for the device on that USB port ID;
MKtl.make(\nk1, 'nanoKONTROL');
MKtl(\nk1).dump;
MKtl(\nk1).deviceDescription;

MKtl(\nk1).elements.choose.elementDescription


// 2b // the same procedure for HIDKtl: 

MKtl.all.clear;
MKtl.make(\ferr1, 'Run_N_Drive');
MKtl(\ferr1).dump;
MKtl(\ferr1).elements[\joyLX].deviceDescription
MKtl(\ferr1).elements.do {|el| el.deviceDescription.postln };


// look only for HIDMKtl devices present:
HIDMKtl.find;

HIDMKtl(\fer1, 102760448);	// osx
HIDMKtl('fer1', "usb-0000:00:1a.0-1.1/input0" );  // Thrustmaster Run'N' Drive on linux



	///////// Some Tests //////////

	// there is no uid like that, so this fails
MIDIMKtl(\nk1, 12345);			// 



// it auto-loads descriptions of all its elements from files: 
	MIDIMKtl(\nk1).deviceDescription;
	
// which can be posted conveniently
	MIDIMKtl(\ferr1).postDeviceDescription;

	MIDIMKtl(\nk1).ktlNames.postcs; "";

(
	MIDIMKtl(\nk1).addFunc(\sl1_1, \yubel, { |who, what, howmuch| 
		"YAYAYAY: ".post; [who, what, howmuch].postln;
	});
)		// 

// turn verbose mode on; this adds a "verbose" function to all functionChains for all elements.
	MIDIMKtl(\nk1).verbose = true;

// turn it off again
	MIDIMKtl(\nk1).verbose = false;

	MIDIMKtl(\nk1).openTester;


MIDIMKtl('launchpad1', 593081660);  // Launchpad
MIDIMKtl('launchpad1').postDeviceDescription

MIDIMKtl('launchpad1').defaultValueFor(\bu13)

// maybe "debug" (as)?
MIDIMKtl('launchpad1').verbose = true
MIDIMKtl('launchpad1').verbose = false

MIDIMKtl(\launchpad1).verbose = true

(
	MIDIMKtl(\launchpad1).addFunc(\bu0, \yubel, { |who, what, howmuch| 
		"YAYAYAY: ".post; [who, what, howmuch].postln;
	});
)		// 
MIDIMKtl(\launchpad1).verbose = false


MIDIMKtl(\launchpad1).removeFunc(\bu0, \yubel)
