(
device: "USB Optical Mouse_",
protocol: 'hid',
description: (
// --------- input elements ----------
	bt: (
		'left': ('hidUsage': 1, 'hidUsagePage': 9, 'type': 'button', 'ioType': 'in', \spec: \hidBut),
		'right': ('hidUsage': 2, 'hidUsagePage': 9, 'type': 'button', 'ioType': 'in', \spec: \hidBut),
		'middle': ('hidUsage': 3, 'hidUsagePage': 9, 'type': 'button', 'ioType': 'in', \spec: \hidBut)
	),
	x: ('hidUsage': 48, 'hidUsagePage': 1, 'type': 'mouseAxis', 'ioType': 'in', \spec: \mouseAxis ),
	y: ('hidUsage': 49, 'hidUsagePage': 1, 'type': 'mouseAxis', 'ioType': 'in', \spec: \mouseAxis ),
	wheel: ('hidUsage': 56, 'hidUsagePage': 1, 'type': 'mouseWheel', 'ioType': 'in', \spec: \mouseWheel ),
)
);