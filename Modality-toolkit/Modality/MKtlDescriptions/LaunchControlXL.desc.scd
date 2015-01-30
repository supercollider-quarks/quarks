// user: channel 0
// factory: channel 8

(
device: "Launch Control XL",
protocol: \midi,
description:
(
///////////// Factory ///////////////////
'btFcs': #[0, 8].collect{|chan| ((41..44) ++ (57..60)).collect{|num|
          ('midiMsgType': 'noteOnOff', 'type': 'button', 'midiChan': chan, 'midiNum':  num,'spec': 'midiNote')
}},

'btCtl': #[0, 8].collect{|chan| ((73..76) ++ (89..92)).collect{|num|
          ('midiMsgType': 'noteOnOff', 'type': 'button', 'midiChan': chan, 'midiNum':  num,'spec': 'midiNote')
}},

'device': #[0, 8].collect{|chan|
	      ('midiMsgType': 'noteOnOff', 'type': 'button', 'midiChan': chan, 'midiNum':  105,'spec': 'midiNote')
},
'mute':   #[0, 8].collect{|chan|
	      ('midiMsgType': 'noteOnOff', 'type': 'button', 'midiChan': chan, 'midiNum':  106,'spec': 'midiNote')
},
'solo':   #[0, 8].collect{|chan|
	      ('midiMsgType': 'noteOnOff', 'type': 'button', 'midiChan': chan, 'midiNum':  107,'spec': 'midiNote')
},
'arm':    #[0, 8].collect{|chan|
	      ('midiMsgType': 'noteOnOff', 'type': 'button', 'midiChan': chan, 'midiNum':  108,'spec': 'midiNote')
},


'knSndA': #[0, 8].collect{|chan| (13..20).collect{|num|
          ('midiMsgType': 'cc',        'type': 'knob',   'midiChan': chan, 'midiNum':  num, 'spec': 'midiCC')
}},

'knSndB': #[0, 8].collect{|chan| (29..36).collect{|num|
          ('midiMsgType': 'cc',        'type': 'knob',   'midiChan': chan, 'midiNum':  num, 'spec': 'midiCC')
}},

'knPan':  #[0, 8].collect{|chan| (49..56).collect{|num|
          ('midiMsgType': 'cc',        'type': 'knob',   'midiChan': chan, 'midiNum':  num, 'spec': 'midiCC')
}},

\sl:      #[0, 8].collect{|chan| (77..84).collect{|num|
          ('midiMsgType': 'cc',        'type': 'slider', 'midiChan': chan, 'midiNum':  num, 'spec': 'midiCC')
}},

\up:      #[0, 8].collect{|chan|
	      ('midiMsgType': 'cc',        'type': 'button', 'midiChan': chan, 'midiNum':  104, 'spec': 'midiCC')
},
\down:    #[0, 8].collect{|chan|
	      ('midiMsgType': 'cc',        'type': 'slider', 'midiChan': chan, 'midiNum':  105, 'spec': 'midiCC')
},
\left:    #[0, 8].collect{|chan|
	      ('midiMsgType': 'cc',        'type': 'slider', 'midiChan': chan, 'midiNum':  106, 'spec': 'midiCC')
},
\right:   #[0, 8].collect{|chan|
	      ('midiMsgType': 'cc',        'type': 'slider', 'midiChan': chan, 'midiNum':  107, 'spec': 'midiCC')
},
));
