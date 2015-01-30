// template GamePad description

Spec.add(\cent1,  [0, 1, \lin, 0, 0.5].asSpec);

[
	// no idea what this is
// [ Collection, GamePad, 1, 0, 0, 513, 1, 5 ]

	// right hand side four labeled buttons
	\bt_R_1, (type: \button, osx: (cookie: '?'), linux: (slot: [1,'?']), spec: \hidBut, mode: \push),
	\bt_R_2, (type: \button, osx: (cookie: '?'), linux: (slot: [1,'?']), spec: \hidBut, mode: \push),
	\bt_R_3, (type: \button, osx: (cookie: '?'), linux: (slot: [1,'?']), spec: \hidBut, mode: \push),
	\bt_R_4, (type: \button, osx: (cookie: '?'), linux: (slot: [1,'?']), spec: \hidBut, mode: \push),
	
		// left shoulder fire button, right shoulder fire button, 
		// left bottom fire button,  right bottom fire button, 
 	\lfTop_5, (type: \button, osx: (cookie: '?'), linux: (slot: [1,'?']), spec: \hidBut, mode: \push),
	\lfBot_6, (type: \button, osx: (cookie: '?'), linux: (slot: [1,'?']), spec: \hidBut, mode: \push),
	\rfTop_7, (type: \button, osx: (cookie: '?'), linux: (slot: [1,'?']), spec: \hidBut, mode: \push),
	\rfBot_8, (type: \button, osx: (cookie: '?'), linux: (slot: [1,'?']), spec: \hidBut, mode: \push),
		
		// middle buttons
 	\mid_L, (type: \button, osx: (cookie: '?'), linux: (slot: [1,'?']), spec: \hidBut, mode: \push),
	\mid_R, (type: \button, osx: (cookie: '?'), linux: (slot: [1,'?']), spec: \hidBut, mode: \push),
	
		// joystick hat switches
	\joyHat_L, (type: \hidHat, osx: (cookie: '?'), linux: (slot: [1,'?']), spec: \hidHat, mode: \center),
	\joyHat_R, (type: \hidHat, osx: (cookie: '?'), linux: (slot: [1,'?']), spec: \hidHat, mode: \center),

		// joystick axes switches
	\joy_L_X, (type: \joyAxis, osx: (cookie: '?', spec: \cent255inv), linux: (slot: [3,1], spec: \cent1 ),  mode: \center),
	\joy_L_Y, (type: \joyAxis, osx: (cookie: '?', spec: \cent255 ), linux: (slot: [3,0], spec: \cent1 ),  mode: \center),
	\joy_R_X, (type: \joyAxis, osx: ( cookie: '?', spec: \cent255inv), linux: (slot: [3,5], spec: \cent1), mode: \center),
	\joy_R_Y, (type: \joyAxis, osx: ( cookie: '?', spec: \cent255 ), linux: ( slot: [3,2], spec: \cent1 ), mode: \center),
	
		// west is 1, south is 3, east is 5, north is 7
	\compass, (type: \compass, osx: (cookie: '?', spec: \compass8), mode: \push),	
	\compass_X, (type: \compassX, linux: (slot: [3,16], spec: \cent1), mode: \push),
	\compass_Y, (type: \compassY, linux: (slot: [3,17], spec: \cent1), mode: \push),
];