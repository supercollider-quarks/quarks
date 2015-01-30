// Impact GamePad description

Spec.add(\cent1,  [0, 1, \lin, 0, 0.5].asSpec);

[
	// no idea what this is
// [ Collection, GamePad, 1, 0, 0, 513, 1, 5 ]

	// right hand side four labeled buttons
	\bt_R_1, (type: \button, osx: (cookie: 3), linux: (slot: [1,288]), spec: \hidBut, mode: \push),
	\bt_R_2, (type: \button, osx: (cookie: 4), linux: (slot: [1,289]), spec: \hidBut, mode: \push),
	\bt_R_3, (type: \button, osx: (cookie: 5), linux: (slot: [1,290]), spec: \hidBut, mode: \push),
	\bt_R_4, (type: \button, osx: (cookie: 6), linux: (slot: [1,291]), spec: \hidBut, mode: \push),

		// left shoulder fire button, right shoulder fire button,
		// left bottom fire button,  right bottom fire button,
 	\lfTop_5, (type: \button, osx: (cookie: 7), linux: (slot: [1,292]), spec: \hidBut, mode: \push),
	\lfBot_6, (type: \button, osx: (cookie: 8), linux: (slot: [1,294]), spec: \hidBut, mode: \push),
	\rfTop_7, (type: \button, osx: (cookie: 9), linux: (slot: [1,293]), spec: \hidBut, mode: \push),
	\rfBot_8, (type: \button, osx: (cookie: 10), linux: (slot: [1,295]), spec: \hidBut, mode: \push),

		// middle buttons
 	\mid_L, (type: \button, osx: (cookie: 11), linux: (slot: [1,296]), spec: \hidBut, mode: \push),
	\mid_R, (type: \button, osx: (cookie: 12), linux: (slot: [1,297]), spec: \hidBut, mode: \push),

		// joystick hat switches
	\joyHat_L, (type: \hidHat, osx: (cookie: 13), linux: (slot: [1,298]), spec: \hidHat, mode: \center),
	\joyHat_R, (type: \hidHat, osx: (cookie: 14), linux: (slot: [1,299]), spec: \hidHat, mode: \center),

		// joystick axes switches
	\joy_L_X, (type: \joyAxis, osx: (cookie: 15, spec: \cent255inv), linux: (slot: [3,0], spec: \cent1 ),  mode: \center),
	\joy_L_Y, (type: \joyAxis, osx: (cookie: 16, spec: \cent255 ), linux: (slot: [3,1], spec: \cent1 ),  mode: \center),
	\joy_R_X, (type: \joyAxis, osx: ( cookie: 18, spec: \cent255inv), linux: (slot: [3,3], spec: \cent1), mode: \center),
	\joy_R_Y, (type: \joyAxis, osx: ( cookie: 17, spec: \cent255 ), linux: ( slot: [3,5], spec: \cent1 ), mode: \center),

		// west is 1, south is 3, east is 5, north is 7
	\compass, (type: \compass, osx: (cookie: 19), spec: \compass8, mode: \push),
	\compass_X, (type: \compassX, linux: (slot: [3,16]), spec: \cent1, mode: \push),
	\compass_Y, (type: \compassY, linux: (slot: [3,17]), spec: \cent1, mode: \push),
];