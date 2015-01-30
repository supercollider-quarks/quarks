(
device: "USB Joystick_MegaWorldectronics",
protocol: \hid,
description: (

	// --------- input elements ----------
	'bt': (
		1: ('hidUsage': 1, 'hidUsagePage': 9, 'type': 'button', 'ioType': 'in', spec: \hidBut, mode: \push ),
		2: ('hidUsage': 2, 'hidUsagePage': 9, 'type': 'button', 'ioType': 'in', spec: \hidBut, mode: \push ),
		3: ('hidUsage': 3, 'hidUsagePage': 9, 'type': 'button', 'ioType': 'in', spec: \hidBut, mode: \push ),
		4: ('hidUsage': 4, 'hidUsagePage': 9, 'type': 'button', 'ioType': 'in', spec: \hidBut, mode: \push )
	),
	'Hat': ('hidUsage': 57, 'hidUsagePage': 1, 'type': 'hatSwitch', 'ioType': 'in', spec: \cent1, mode: \center ),
	joy:(
		x: ('hidUsage': 48, 'hidUsagePage': 1, 'type': 'joyAxis', 'ioType': 'in',  spec: \cent1, mode: \center ),
		y: ('hidUsage': 49, 'hidUsagePage': 1, 'type': 'joyAxis', 'ioType': 'in',  spec: \cent1, mode: \center )
	),
	'Slider': ('hidUsage': 187, 'hidUsagePage': 2, 'type': 'slider', 'ioType': 'in', spec: \cent1, mode: \center )
)
)