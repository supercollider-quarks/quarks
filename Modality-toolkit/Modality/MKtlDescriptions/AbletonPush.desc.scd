(
device: "Ableton Push",
protocol: \midi,
description: (// display still missing because SysEx and different MIDI port...

//------ pads -------------
\pad: (36..99).collect{|num|
	('midiMsgType': 'noteOnOff', 'type': 'pad', 'midiChan': 0, 'midiNum': num,  'spec': 'midiNote', \ioType: \inout)
}.clump(8),
\poly: (36..99).collect{|num|
	('midiMsgType': 'polyTouch', 'type': 'keytouch', 'midiChan': 0, 'midiNum': num,'spec': 'midiCC', \ioType: \in)
}.clump(8),


//------ ribbon -----------
\ribbonTouched: ('midiMsgType': 'noteOnOff', 'type': 'button', 'midiChan': 0, 'midiNum': 12,  'spec': 'midiNote'),
\ribbon: ('midiMsgType': 'bend', 'type': 'ribbon', 'midiChan': 0, 'spec': 'midiBend'),


//------ encoderTopRow
\encHat: (0..8).collect{|num|
	(midiMsgType: \noteOnOff, type: 'button', midiChan: 0, midiNum: num, spec: \midiVel)
},
\enc: (71..79).collect{|num|
	(midiMsgType: \cc, type: \encoder, midiChan: 0, midiNum: num, spec: \midiCC)
},

//------ left encoders (not in a group because very different in experience)
lEncHat: (midiMsgType: \noteOnOff, type: \button, midiChan: 0, midiNum: 10, spec: \midiVel),
lEnc: (midiMsgType: \cc, type: \encoder, midiChan: 0, midiNum: 14, spec: \midiBut),

rEncHatOn: (midiMsgType: \noteOnOff, type: \button, midiChan: 0, midiNum: 9, spec: \midiVel),
rEnc: (midiMsgType: \cc, type: \encoder, midiChan: 0, midiNum: 15, spec: \midiBut),

//------ color button rows
\colorBut: [
	(20..27).collect{|num|
		('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum':    num, 'spec': 'midiCC')
},
	(102..109).collect{|num|
		('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum':    num, 'spec': 'midiCC')
}
],

//------ buttons
\tapTempo:		 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum':    3,'spec': 'midiCC', ioType: \inout),
\metronome:		 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum': 	  9,'spec': 'midiCC', ioType: \inout),
\undo:			 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum': 	119,'spec': 'midiCC', ioType: \inout),
\delete:		 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum': 	118,'spec': 'midiCC', ioType: \inout),
\double:		 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum': 	117,'spec': 'midiCC', ioType: \inout),
\quantize:		 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum': 	116,'spec': 'midiCC', ioType: \inout),
\fixedLength:	 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum': 	 90,'spec': 'midiCC', ioType: \inout),
\automation:	 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum': 	 89,'spec': 'midiCC', ioType: \inout),
\duplicate:		 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum': 	 88,'spec': 'midiCC', ioType: \inout),
\new:			 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum': 	 87,'spec': 'midiCC', ioType: \inout),
\record:		 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum': 	 86,'spec': 'midiCC', ioType: \inout),
\play:			 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum': 	 85,'spec': 'midiCC', ioType: \inout),
\master:		 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum': 	 28,'spec': 'midiCC', ioType: \inout),
\stop:			 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum': 	 29,'spec': 'midiCC', ioType: \inout),
'132t':			 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum':   43,'spec': 'midiCC', ioType: \inout),
'132':			 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum':   42,'spec': 'midiCC', ioType: \inout),
'116t':			 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum':   41,'spec': 'midiCC', ioType: \inout),
'116':			 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum':   40,'spec': 'midiCC', ioType: \inout),
'18t':			 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum':   39,'spec': 'midiCC', ioType: \inout),
'18':			 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum':   38,'spec': 'midiCC', ioType: \inout),
'14t':			 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum':   37,'spec': 'midiCC', ioType: \inout),
'14':			 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum':   36,'spec': 'midiCC', ioType: \inout),
\volume:		 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum': 	114,'spec': 'midiCC', ioType: \inout),
\track:			 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum': 	112,'spec': 'midiCC', ioType: \inout),
\device:		 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum': 	110,'spec': 'midiCC', ioType: \inout),
\in:			 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum': 	 62,'spec': 'midiCC', ioType: \inout),
\mute:			 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum': 	 60,'spec': 'midiCC', ioType: \inout),
\scales:		 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum': 	 58,'spec': 'midiCC', ioType: \inout),
\repeat:		 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum': 	 56,'spec': 'midiCC', ioType: \inout),
\octaveDown:	 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum': 	 54,'spec': 'midiCC', ioType: \inout),
\addEffect:		 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum': 	 52,'spec': 'midiCC', ioType: \inout),
\note:			 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum': 	 50,'spec': 'midiCC', ioType: \inout),
\select:		 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum': 	 48,'spec': 'midiCC', ioType: \inout),
\panSend:		 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum': 	115,'spec': 'midiCC', ioType: \inout),
\clip:			 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum': 	113,'spec': 'midiCC', ioType: \inout),
\browse:		 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum': 	111,'spec': 'midiCC', ioType: \inout),
\out:			 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum': 	 63,'spec': 'midiCC', ioType: \inout),
\solo:			 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum': 	 61,'spec': 'midiCC', ioType: \inout),
\user:			 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum': 	 59,'spec': 'midiCC', ioType: \inout),
\accent:		 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum': 	 57,'spec': 'midiCC', ioType: \inout),
\octaveUp:		 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum': 	 55,'spec': 'midiCC', ioType: \inout),
\addTrack:		 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum': 	 53,'spec': 'midiCC', ioType: \inout),
\session:		 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum': 	 51,'spec': 'midiCC', ioType: \inout),
\shift:			 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum': 	 49,'spec': 'midiCC', ioType: \inout),
\left:			 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum': 	 44,'spec': 'midiCC', ioType: \inout),
\right:			 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum': 	 45,'spec': 'midiCC', ioType: \inout),
\up:			 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum': 	 46,'spec': 'midiCC', ioType: \inout),
\down:			 ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum':	 47,'spec': 'midiCC', ioType: \inout)
)
)