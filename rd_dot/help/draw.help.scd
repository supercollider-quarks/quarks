// Draw the famous Analog Bubbles example.

(
{
	f = LFSaw.kr(0.4, 0, 24, LFSaw.kr([8,7.23], 0, 3, 80)).midicps;
	CombN.ar(SinOsc.ar(f, 0, 0.04), 0.2, 0.2, 4)
}.draw;
)

// There is an option to draw input names.

DotViewer.drawInputName = true;

// Draw a graph that includes a multiple output UGen.

{Pan2.ar(SinOsc.ar(440,0,0.1),SinOsc.kr(1),1);}.draw;

// The location where the Dot file is written to, and the viewer that
// is invoked.

[DotViewer.directory, DotViewer.viewer];
