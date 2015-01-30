
/*
desc file for Thrustmater ferrari gamepad.

two details:
 bt 11 and 12 are actually joy_l_hat and joy_r_hat,
so moved them there.

in \compass, id 57, the reported range is wrong:
instead of [0, 7], it it is [0, 15] when including
the value 15, which is sent when compass but is released.
the directions are:

     0
  7      1
6    15    2
  5      3
     4

// example for fixing this and using compass directions:

HIDMKtl.find;
f = HIDMKtl('rnnd0');
f.printElementNames;

c = f.elementsDict[\compass];
c.action = { |el|
// calc integer value for indexing:
var intVal = (el.value * 15).round.asInteger;
var dir;
[el.value, intVal, el.rawValue].round(0.001).postln;
if (intVal == 15) {
"rnd compass released.".postln
} {
dir = [\N, \NE, \E, \SE, \S, \SW, \W, \NW][intVal];
"rnd compass direction: %\n".postf(dir);
};
};

// test that joy hat works:
f.elementsDict[\joy_l_hat].action = { |el| [el.name, el.value].postln };
f.elementAt(\joy, \r, \hat).action = { |el| [el.name, el.value].postln };

*/

var dict;
// a spec to fix the falsely reported spec:
MKtl.addSpec(\compassRND, [0, 15/7]);

dict = (
	device: "Run'N' Drive_Thrustmaster",
	protocol: \hid,
	description: (
		'bt': (
			// --------- input elements ----------
			1: ('hidUsage': 1, 'hidUsagePage': 9, 'type': 'button', 'ioType': 'in', spec: \hidBut, mode: \push ),
			2: ('hidUsage': 2, 'hidUsagePage': 9, 'type': 'button', 'ioType': 'in', spec: \hidBut, mode: \push ),
			3: ('hidUsage': 3, 'hidUsagePage': 9, 'type': 'button', 'ioType': 'in', spec: \hidBut, mode: \push ),
			4: ('hidUsage': 4, 'hidUsagePage': 9, 'type': 'button', 'ioType': 'in', spec: \hidBut, mode: \push ),
			5: ('hidUsage': 5, 'hidUsagePage': 9, 'type': 'button', 'ioType': 'in', spec: \hidBut, mode: \push ),
			6: ('hidUsage': 6, 'hidUsagePage': 9, 'type': 'button', 'ioType': 'in', spec: \hidBut, mode: \push ),
			7: ('hidUsage': 7, 'hidUsagePage': 9, 'type': 'button', 'ioType': 'in', spec: \hidBut, mode: \push ),
			8: ('hidUsage': 8, 'hidUsagePage': 9, 'type': 'button', 'ioType': 'in', spec: \hidBut, mode: \push ),
			9: ('hidUsage': 9, 'hidUsagePage': 9, 'type': 'button', 'ioType': 'in', spec: \hidBut, mode: \push ),
			10: ('hidUsage': 10, 'hidUsagePage': 9, 'type': 'button', 'ioType': 'in', spec: \hidBut, mode: \push )
			// moved 11 and 12 to joy_l_hat, joy_r_hat.
			// 11: ('hidUsage': 12, 'hidUsagePage': 9, 'type': 'button', 'ioType': 'in', spec: \hidBut, mode: \push ),
			// 12: ('hidUsage': 12, 'hidUsagePage': 9, 'type': 'button', 'ioType': 'in', spec: \hidBut, mode: \push ),
		),

		'compass': ('hidUsage': 57, 'hidUsagePage': 1, 'type': 'compass', 'ioType': 'in', spec: \compassRND, mode: \center),

		'joy': (
			\l:(
				\x: ('hidUsage': 48, 'hidUsagePage': 1, 'type': 'joyAxis', 'ioType': 'in', spec: \cent1, mode: \center ),
				\y: ('hidUsage': 49, 'hidUsagePage': 1, 'type': 'joyAxis', 'ioType': 'in', spec: \cent1, mode: \center ),
				\hat: ('hidUsage': 11, 'hidUsagePage': 9, 'type': 'button', 'ioType': 'in', spec: \hidBut, mode: \push )
			),
			\r:(
				\x: ('hidUsage': 50, 'hidUsagePage': 1, 'type': 'joyAxis', 'ioType': 'in', spec: \cent1, mode: \center ),
				\y: ('hidUsage': 53, 'hidUsagePage': 1, 'type': 'joyAxis', 'ioType': 'in', spec: \cent1, mode: 'center' ),
				\hat: ('hidUsage': 12, 'hidUsagePage': 9, 'type': 'button', 'ioType': 'in', spec: \hidBut, mode: \push )
			)
		),

		'throt': (
			\l: ('hidUsage': 51, 'hidUsagePage': 1, 'type': 'springFader', 'ioType': 'in', mode: \minimum, spec: \lin1inv ),
			\r: ('hidUsage': 52, 'hidUsagePage': 1, 'type': 'springFader', 'ioType': 'in', mode: \minimum, spec: \lin1inv )
		),
		'wheel': ('hidUsage': 54, 'hidUsagePage': 1, 'type': 'springFader', 'ioType': 'in', spec: \cent1inv, mode: \center ),

		// --------- output elements ----------
		'rumble':(
			\l: ('hidUsage': 187, 'hidUsagePage': 1, 'type': 'rumble', 'ioType': 'out', spec: \lin255, mode: \minimum ),
			\r: ('hidUsage': 0, 'hidUsagePage': 1, 'type': 'rumble', 'ioType': 'out', spec: \lin255, mode: \minimum ) // this one is different in OSX due to a bug in the Apple API, but does not work yet anyways
		)
	)
);
