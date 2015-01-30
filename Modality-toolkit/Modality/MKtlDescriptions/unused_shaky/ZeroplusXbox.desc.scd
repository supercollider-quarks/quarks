// Impact GamePad description

Spec.add(\cent1,  [0, 1, \lin, 0, 0.5].asSpec);

[

	// right hand side four labeled buttons
	\bt_X, (type: \button, osx: (cookie: 3), linux: (slot: [1,307]), spec: \hidBut, mode: \push),
	\bt_Y, (type: \button, osx: (cookie: 4), linux: (slot: [1,308]), spec: \hidBut, mode: \push),
	\bt_A, (type: \button, osx: (cookie: 5), linux: (slot: [1,304]), spec: \hidBut, mode: \push),
	\bt_B, (type: \button, osx: (cookie: 6), linux: (slot: [1,305]), spec: \hidBut, mode: \push),
	\bt_white, (type: \button, osx: (cookie: 6), linux: (slot: [1,309]), spec: \hidBut, mode: \push),
	\bt_black, (type: \button, osx: (cookie: 6), linux: (slot: [1,306]), spec: \hidBut, mode: \push),

		// left shoulder fire button, right shoulder fire button,
		// left bottom fire button,  right bottom fire button,
 	\lf, (type: \button, osx: (cookie: 7), linux: (slot: [3,2]), spec: \hidBut, mode: \push),
	\rf, (type: \button, osx: (cookie: 9), linux: (slot: [3,5]), spec: \hidBut, mode: \push),

		// middle buttons
	\back, (type: \button, osx: (cookie: 12), linux: (slot: [1,314]), spec: \hidBut, mode: \push),
	\start, (type: \button, osx: (cookie: 12), linux: (slot: [1,315]), spec: \hidBut, mode: \push),


		// joystick axes switches
	\joy_L_X, (type: \joyAxis, osx: (cookie: 15, spec: \cent255inv), linux: (slot: [3,0], spec: \cent1 ),  mode: \center),
	\joy_L_Y, (type: \joyAxis, osx: (cookie: 16, spec: \cent255 ), linux: (slot: [3,1], spec: \cent1 ),  mode: \center),
	\joy_R_X, (type: \joyAxis, osx: ( cookie: 18, spec: \cent255inv), linux: (slot: [3,3], spec: \cent1), mode: \center),
	\joy_R_Y, (type: \joyAxis, osx: ( cookie: 17, spec: \cent255 ), linux: ( slot: [3,4], spec: \cent1 ), mode: \center),

		// west is 1, south is 3, east is 5, north is 7
	\compass, (type: \compass, osx: (cookie: 19), spec: \compass8, mode: \push),
	\compass_X, (type: \compassX, linux: (slot: [3,16]), spec: \cent1, mode: \push),
	\compass_Y, (type: \compassY, linux: (slot: [3,17]), spec: \cent1, mode: \push),
];