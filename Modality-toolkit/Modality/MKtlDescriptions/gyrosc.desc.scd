// gyrosc iphone/ipad app, skeching how OSC devices should work.
// should create an
/*
OSCMktl.find; // can't detect any active ones ...
OSCMktl('gyrosc', "gyrosc");
newFromDesc
OSCExplorer
OSCMktl.new;

MKtl.loadAllDescs;
MKtl.allDevDescs[\gyrosc];

*/

(
device: "gyrosc",
protocol: \osc,
description: (
	'gyrosc/*/gyro': (\oscMsgType: \, \type: \gyro, \spec: \midiCC)
)
)
