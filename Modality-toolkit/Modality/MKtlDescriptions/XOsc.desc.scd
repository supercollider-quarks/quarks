(
device: "XOsc",
protocol: \osc,
description: (
	// input elements:

// 16 integer arguments represent the state of the 16 digital inputs (0 or 1). Sent only when the state of any digital input pin changes
	inDig: (msgName: '/inputs/digital', numIns: 16, ioType: \in),

// 16 floating-point arguments indicate the analogue readings for all 16 inputs. Sent at the analogue input message rate
	inAna: (msgName: '/inputs/analogue', numIns: 16, ioType: \in),




// 16 integer arguments represent the current compare state of the 16 analogue inputs (0 if below or 1 if above the threshold). Sent only when a threshold on any channel is crossed
	inComp: (msgName: '/inputs/analogue/comparator', numIns: 16, ioType: \in),

// 1 blob argument containing the raw serial data received on input <channel> (1 – 4)
	inS1: (msgName: '/inputs/serial/1', inType: \blob, ioType: \in),
	inS2: (msgName: '/inputs/serial/2', inType: \blob, ioType: \in),
	inS3: (msgName: '/inputs/serial/3', inType: \blob, ioType: \in),
	inS4: (msgName: '/inputs/serial/4', inType: \blob, ioType: \in),

// 10 floating-point arguments indicating the current readings of the on board sensors. Arguments 1 – 3 gyroscope (°/s) x, y, z; arguments 4 – 6 accelerometer (g), x, y ,z; arguments 7 – 9 magnetometer (Ga) x, y ,z; argument 10 gyroscope temperature (°C)
	imu: (msgName: \imu, numIns: 10),

	// 1 float indicating the current battery voltage
	battery: (msgName: \battery, numIns: 1, ioType: \in),

	// /ping: 3 string arguments are the x-OSC local IP address (1st string), MAC address (2nd string) and firmware version (3rd string)
	ping: (msgName: \ping, numArgs: 3, ioType: \in),


	// output elements going to XOsc:
// If x-OSC receives a message that does not match any message in the following table the LED will flash red.

	// send new IP for XOsc as string, e.g. “169.254.1.2”
	setIP: (msgName: "/osc/remote/ip", ioType: \out),
	// send new remote port number for XOsc as int16 (0 – 65535)
	setRemotePort: (msgName: "/osc/remote/port", ioType: \out),
	// send new local port number for XOsc as int16 (0 – 65535)
	setLocalPort: (msgName: "/osc/local/port", ioType: \out),
	// int 1 enables using bundles, 0 disables
	useBundles: (msgName: "/osc/bundles", ioType: \out),
	// int 1 enables using prefix (e.g. “/xosc”), 0 disables
	usePrefix: (msgName: "/osc/prefix/enabled", ioType: \out),
	// set osc address prefix string, e.g. (e.g. “/xosc”)
	prefix: (msgName: "/osc/prefix/address", ioType: \out)
);

// /inputs/mode/analogue/<channel>
// sets the input <channel> (1 – 16) to analogue mode
// /inputs/mode/digital/<channel>
// sets the input <channel> (1 – 16) to digital mode
// /inputs/mode/serial/<channel>
// sets the input <channel> (1 – 16) to serial mode
// /inputs/analogue/read
// prompts x-OSC to respond with an /inputs/analogue
//
// /inputs/analogue/rate
// floating-point argument sets the message rate for the analogue input readings (0.0 – 400.0)
// /inputs/analogue/comparator/read
// prompts x-OSC to respond with an /inputs/analogue/compare message
// /inputs/analogue/comparator /threshold/<channel>
// floating-point argument sets the comparator threshold (0.0 – 1.0) for the specified input <channel> (1 – 16)
// /inputs/digital/read
// prompts x-OSC to respond with an /inputs/digital message
// /inputs/digital/up/<channel>
// enables the pull up resistor for the specified digital input <channel> (1 – 16)
// /inputs/digital/down/<channel>
// enables the pull down resistor for the specified digital input <channel> (1 – 16)
// /outputs/mode/digital/<channel>
// the specified output <channel> (1 – 16) is set to digital mode
// /outputs/mode/pulse/<channel>
// the specified output <channel> (1 – 16) is set to pulse mode
// /outputs/mode/pwm/<channel>
// the specified output <channel> (1 – 16) is set to PWM mode
// /outputs/mode/serial/<channel>
// the specified output <channel> (1 – 16) is set to serial mode
// /outputs/digital/<channel>
// integer argument (0 or 1) sets the state of the specified output <channel>
// /outputs/digital/pattern
// the lower 16-bits of the integer argument sets the state of all 16 digital outputs
// /outputs/pulse/<channel>
// triggers a pulse on the specified output <channel> (1 – 16) is set to pulse mode. Uses the last width and invert values
// /outputs/pulse/width/<channel>
// integer argument sets the pulse duration in milliseconds (0 – 60000) for the specified <channel>. Also triggers a pulse
// /outputs/pulse/invert/<channel>
// integer argument (0 or 1) sets the inversion state of the pulse (1 is inverted). Also triggers a pulse
// /outputs/pwm/frequency/<channel>
// floating-point argument sets the PWM frequency (5 – 250 000) for the specified output <channel> (1 – 16). Also sets the pin to PWM mode if not already
// /outputs/pwm/duty/<channel>
// floating-point argument sets the PWM duty cycle (0 – 100 %) for the specified output <channel> (1 – 16). Also sets the pin to PWM mode if not already
// /outputs/rgb/<channel>
// blob argument will set the RGB values of LEDs connected to <channel> (1 – 16). The blob should contain bytes in the format: R1, G1, B1, R2, G2, B2 ... Rn, Gn, Bn.
// /outputs/serial/<channel>
// blob argument will be transmitted on the specified serial
// ￼
// <channel> (1 – 4)
// /led/rgb
// three integer arguments (range 0 – 255) set the RGB vales for the on board LED
// /led/default
// resets the LED to the network mode colour
// /ping
// causes x-OSC to broadcast its local IP address
// /serial/baud/<channel>
// integer argument (0 – 2000000) sets the baud rate for the specified serial <channel> (1 – 4)
// /serial/buffer/<channel>
// integer argument (1 – 2048) sets the input buffer size for the specified serial <channel> (1 – 4)
// /serial/timeout/<channel>
// the timeout of the input buffer for the specified serial <channel> (1 – 4) is set to the value of i (0 – 1000 ms)
// /serial/framing/<channel>
// integer argument (-1 – 255) sets the framing character for the specified serial <channel> (1 – 4)
)
