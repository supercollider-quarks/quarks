MKtl.addSpec(\compassShanWan, [0, 7]);

(
device: "USB WirelessGamepad _ShanWan",
protocol: 'hid',
description: (


// --------- input elements ----------
\triangle: ('hidUsage': 1, 'hidUsagePage': 9, 'type': 'button', 'ioType': 'in', spec: \hidBut, mode: \push ),
\circle: ('hidUsage': 2, 'hidUsagePage': 9, 'type': 'button', 'ioType': 'in', spec: \hidBut, mode: \push ),
'cross': ('hidUsage': 3, 'hidUsagePage': 9, 'type': 'button', 'ioType': 'in', spec: \hidBut, mode: \push ),
'square': ('hidUsage': 4, 'hidUsagePage': 9, 'type': 'button', 'ioType': 'in', spec: \hidBut, mode: \push ),
	'sh': (
		'l': (
			1: ('hidUsage': 5, 'hidUsagePage': 9, 'type': 'button', 'ioType': 'in', spec: \hidBut, mode: \push ),
			2: ('hidUsage': 7, 'hidUsagePage': 9, 'type': 'button', 'ioType': 'in', spec: \hidBut, mode: \push ),
		),
		'r': (
			1: ('hidUsage': 6, 'hidUsagePage': 9, 'type': 'button', 'ioType': 'in', spec: \hidBut, mode: \push ),
			2: ('hidUsage': 8, 'hidUsagePage': 9, 'type': 'button', 'ioType': 'in', spec: \hidBut, mode: \push ),
		),
	),
'select': ('hidUsage': 9, 'hidUsagePage': 9, 'type': 'button', 'ioType': 'in', spec: \hidBut, mode: \push ),
'start': ('hidUsage': 10, 'hidUsagePage': 9, 'type': 'button', 'ioType': 'in', spec: \hidBut, mode: \push ),


'mode': ('hidUsage': 1, 'hidUsagePage': 65280, 'type': 'button', 'ioType': 'in', spec: \hidBut, mode: \push ),

	'joy': (
		'l':
		(
			'x': ('hidUsage': 48, 'hidUsagePage': 1, 'type': 'joyAxis', 'ioType': 'in', spec: \cent1, mode: \center  ),
			'y': ('hidUsage': 49, 'hidUsagePage': 1, 'type': 'joyAxis', 'ioType': 'in', spec: \cent1, mode: \center  ),
			'hat': ('hidUsage': 11, 'hidUsagePage': 9, 'type': 'button', 'ioType': 'in', spec: \hidBut, mode: \push ),
		),
		'r':
		(
			'x': ('hidUsage': 50, 'hidUsagePage': 1, 'type': 'joyAxis', 'ioType': 'in', spec: \cent1, mode: \center  ),
			'y': ('hidUsage': 53, 'hidUsagePage': 1, 'type': 'joyAxis', 'ioType': 'in', spec: \cent1, mode: \center  ),
			'hat': ('hidUsage': 12, 'hidUsagePage': 9, 'type': 'button', 'ioType': 'in', spec: \hidBut, mode: \push ),
		)
	),

	'compass': ('hidUsage': 57, 'hidUsagePage': 1, 'type': 'compass', 'ioType': 'in', spec: \compassShanWan, mode: \center),

// --------- output elements ----------
'modeOut': ('hidUsage': 2, 'hidUsagePage': 65280, 'type': 'option', 'ioType': 'out', spec: \lin255, mode: \minimum ),
'rumble1': ('hidElementID': 19, 'type': 'rumble', 'ioType': 'out', spec: \lin255, mode: \minimum ),
'rumble2': ('hidElementID': 20, 'type': 'rumble', 'ioType': 'out', spec: \lin255, mode: \minimum ),
'rumble3': ('hidElementID': 21, 'type': 'rumble', 'ioType': 'out', spec: \lin255, mode: \minimum ),
'rumble4': ('hidElementID': 22, 'type': 'rumble', 'ioType': 'out', spec: \lin255, mode: \minimum )
)
);