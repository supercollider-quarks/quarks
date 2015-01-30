MKtl.addSpec(\selectNumark, [0, 4]);

(
device: "Numark ORBIT",
protocol: 'midi',
description:
(

'pad':
	4.collect{ arg chan;
		(36..51).collect{ arg num;
			 ('midiMsgType': 'noteOn', 'type': 'key', 'midiChan': chan, 'midiNum':  num, 'spec': 'midiNote')
		}
	}
,

'padOff': (
	4.collect{ arg chan;
		(36..51).collect{ arg num;
			 ('midiMsgType': 'noteOff', 'type': 'key', 'midiChan': chan, 'midiNum':  num, 'spec': 'midiNote')
		}
	}
),

// ------ noteOn -------------


'sh': (
	'l': ('midiMsgType': 'noteOn', 'type': 'key', 'midiChan': 15, 'midiNum':  3, 'spec': 'midiNote'),
	'r': ('midiMsgType': 'noteOn', 'type': 'key', 'midiChan': 15, 'midiNum':  4, 'spec': 'midiNote')
),

// ---------noteOff ----------

'shOff': (
	'l': ('midiMsgType': 'noteOff', 'type': 'key', 'midiChan': 15, 'midiNum':  3, 'spec': 'midiNote'),
	'r': ('midiMsgType': 'noteOff', 'type': 'key', 'midiChan': 15, 'midiNum':  4, 'spec': 'midiNote')
),

// ------ cc -------------

'dial': 4.collect{ arg chan; ('midiMsgType': 'cc', 'type': 'dial', 'midiChan': chan, 'midiNum':  4,'spec': 'midiCC') }
,

'move' :
4.collect{ arg chan;
(
	'l' : ('midiMsgType': 'cc', 'type': 'movement', 'midiChan': chan, 'midiNum':  9,'spec': 'midiCC'),
	'r' : ('midiMsgType': 'cc', 'type': 'movement', 'midiChan': chan, 'midiNum':  10,'spec': 'midiCC')
)
},

'k': ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 15, 'midiNum':  2,'spec': 'selectNumark'),


'padbank': ('midiMsgType': 'cc', 'type': 'slider', 'midiChan': 15, 'midiNum':  1,'spec': 'selectNumark'),

// '_elName_3_002_': ('midiMsgType': 'cc', 'type': 'slider', 'midiChan': 3, 'midiNum':  2,'spec': 'midiCC'),
 )
)