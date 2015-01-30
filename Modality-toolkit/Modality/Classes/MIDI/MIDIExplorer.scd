MIDIIn.connectAll;
MIDIExplorer.start;


// name

MIDIExplorer.findNoteGroup(name: \keys, type: \noteOn, spec: \midiNote);
MIDIExplorer.findNoteGroup(name: \keys, type: \noteOff, spec: \midiNote);
MIDIExplorer.findNoteGroup(name: \keys, type: \noteOnOff, spec: \midiNote);

MIDIExplorer.findCCGroup(name: \kn, type: \knob, spec: \midiCC);
MIDIExplorer.findCCGroup(name: \bt, type: \button, spec: \midiCC);

MIDIExplorer.shutUp;

MIDIExplorer.compile;
MIDIExplorer.compile(true);

MIDIExplorer.openDoc;

MIDIExplorer.verbose = true;
// (compress sequential things later, Miguel ;-)

