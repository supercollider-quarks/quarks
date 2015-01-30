(
protocol: \hid,
device: "Dual Analog Rumble Pad_Jess Tech",
description: (

	// --------- input elements ----------
	'bt': (
		1: ('hidUsage': 5, 'hidUsagePage': 9, 'type': 'button', 'ioType': 'in', spec: \hidBut, mode: \push ),
		2: ('hidUsage': 6, 'hidUsagePage': 9, 'type': 'button', 'ioType': 'in', spec: \hidBut, mode: \push ),
		3: ('hidUsage': 7, 'hidUsagePage': 9, 'type': 'button', 'ioType': 'in', spec: \hidBut, mode: \push ),
		4: ('hidUsage': 8, 'hidUsagePage': 9, 'type': 'button', 'ioType': 'in', spec: \hidBut, mode: \push ),
		5: ('hidUsage': 9, 'hidUsagePage': 9, 'type': 'button', 'ioType': 'in', spec: \hidBut, mode: \push ),
		6: ('hidUsage': 10, 'hidUsagePage': 9, 'type': 'button', 'ioType': 'in', spec: \hidBut, mode: \push ),
		7: ('hidUsage': 11, 'hidUsagePage': 9, 'type': 'button', 'ioType': 'in', spec: \hidBut, mode: \push ),
		8: ('hidUsage': 12, 'hidUsagePage': 9, 'type': 'button', 'ioType': 'in', spec: \hidBut, mode: \push ),
		9: ('hidUsage': 16, 'hidUsagePage': 9, 'type': 'button', 'ioType': 'in', spec: \hidBut, mode: \push ),
	),

	'hat': ('hidUsage': 57, 'hidUsagePage': 1, 'type': 'hatSwitch', 'ioType': 'in', spec: \cent1, mode: \center),

	'joy':(
		l:(
			x: ('hidUsage': 48, 'hidUsagePage': 1, 'type': 'joyAxis', 'ioType': 'in', spec: \cent1, mode: \center ),
			y: ('hidUsage': 49, 'hidUsagePage': 1, 'type': 'joyAxis', 'ioType': 'in', spec: \cent1, mode: \center )
		),
		r:(
			x: ('hidUsage': 50, 'hidUsagePage': 1, 'type': 'joyAxis', 'ioType': 'in', spec: \cent1, mode: \center ),
			y: ('hidUsage': 53, 'hidUsagePage': 1, 'type': 'joyAxis', 'ioType': 'in', spec: \cent1, mode: 'center' )
		)
	)
)
)