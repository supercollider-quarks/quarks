MKtlGui		- a gui that can draw views 
			for all the elements  of an MKtl.

(	// make two MKtls for tests
MKtl.make(\ferr1, 'Run_N_Drive');
MKtl.make(\nk1, 'nanoKONTROL');
MKtlGui.init;

MKtlAllGui(12);

m = MKtlGui(MKtl(\ferr1));
)

(	// make the zones for each element by hand:

var zoneDict = (
	'bt1r': Rect(250 + 40, 200 - 12, 40, 24),
	'bt2r': Rect(275 + 40, 225 - 12, 40, 24),
	'bt3r': Rect(300 + 40, 200 - 12, 40, 24),
	'bt4r': Rect(275 + 40, 175 - 12,  40, 24),

	'lfBot7': 	Rect(110, 25,  40, 20),
	'lfTop5': 	Rect(40, 100,  80, 20),
	
	'midL9': 		Rect(150, 190, 48, 20),
	'midR10': 	Rect(202, 190, 48, 20),
	
	'rfBot8':		Rect(250, 25,  40, 20),
	'rfTop6': 	Rect(280, 100,  80, 20),
	
	\joyL: 		Rect(60, 260, 100, 100),
	\joyR: 		Rect(220, 260, 100, 100),
	
	\compass: 	Rect.aboutPoint(100@200, 45, 45),

	'wheel': 		Rect(5, 125, 40, 150 ),
	'throtL': Rect(30, 55, 120, 40),
	'throtR': Rect(250, 55, 120, 40)
);
	// temp fix for composite elements:
m.object.elements.put(\joyL, (type: \joyStick));
m.object.elements.put(\joyR, (type: \joyStick));

m.makeElemViews(zoneDict);
)



TODO next: connect all the elements;

// make a template to sketch a gui for a new device:

MKtlGui.postZoneTemplate(MKtl(\ferr1));

MKtlGui.postZoneTemplate(MKtl(\nk1));



// incomplete sketch for nanoKontrol gui: 

m = MKtlGui(MKtl(\nk1));

(
var left = 10, width = 42;
 // where should each gui element be? 
var zoneDict = (
	'rew': Rect(10, 10, 40, 20),
	'play': Rect(50, 10, 40, 20),
	'fwd': Rect(90, 10, 40, 20),
	'loop': Rect(10, 35, 40, 20),
	'stop': Rect(50, 35, 40, 20),
	'rec': Rect(90, 35, 40, 20),
	'sl1_1': Rect(30, 145, 20, 120),
	'sl2_1': Rect(70, 145, 20, 120),
	'sl3_1': Rect(110, 145, 20, 120),
	'sl4_1': Rect(150, 145, 20, 120),
	'sl5_1': Rect(190, 145, 20, 120),
	'sl6_1': Rect(230, 145, 20, 120),
	'sl7_1': Rect(270, 145, 20, 120),
	'sl8_1': Rect(310, 145, 20, 120),
	'sl9_1': Rect(350, 145, 20, 120),
	'kn1_1': Rect(10, 60, 40, 80),
	'kn2_1': Rect(50, 60, 40, 80),
	'kn3_1': Rect(90, 60, 40, 80),
	'kn4_1': Rect(130, 60, 40, 80),
	'kn5_1': Rect(170, 60, 40, 80),
	'kn6_1': Rect(210, 60, 40, 80),
	'kn7_1': Rect(250, 60, 40, 80),
	'kn8_1': Rect(290, 60, 40, 80),
	'kn9_1': Rect(330, 60, 40, 80),
	'bu1_1': Rect( 10, 170, 20, 20),
	'bu2_1': Rect( 50, 170, 20, 20),
	'bu3_1': Rect( 90, 170, 20, 20),
	'bu4_1': Rect(130, 170, 20, 20),
	'bu5_1': Rect(170, 170, 20, 20),
	'bu6_1': Rect(210, 170, 20, 20),
	'bu7_1': Rect(250, 170, 20, 20),
	'bu8_1': Rect(290, 170, 20, 20),
	'bu9_1': Rect(330, 170, 20, 20),
	'bd1_1': Rect( 10, 200, 20, 20),
	'bd2_1': Rect( 50, 200, 20, 20),
	'bd3_1': Rect( 90, 200, 20, 20),
	'bd4_1': Rect(130, 200, 20, 20),
	'bd5_1': Rect(170, 200, 20, 20),
	'bd6_1': Rect(210, 200, 20, 20),
	'bd7_1': Rect(250, 200, 20, 20),
	'bd8_1': Rect(290, 200, 20, 20),
	'bd9_1': Rect(330, 200, 20, 20)
);
m.makeElemViews(zoneDict);
)
