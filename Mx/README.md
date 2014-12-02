
Mx
========

Mx is a patching system for SuperCollider.  It uses an adapter/driver system where object descriptions can be written that specify the inlets and outlets of an object, how it can be connected to, stopped, started and any extra features like guis, timeline aware guis, relocating.

This allows objects of many different types to connect and interact with each other without needing to participate in a common API.


Features
========

Matrix patchbay gui with draggable cables, drawer, large library of Instr functions.
Timeline view - supports Splines and Soundfiles so far
Mixer - basic solid mixer with scope, meters
Scripting API
Connect objects with code while you hear and see the results on the gui
MxQuery for filtering objects and iolets and patching, copying, moving in bulk

Supported objects so far
========================

Bus - jack anything into the mixer
Document - run any code
Instr - patch anything through a large library of instr
SplineFr (frame rate splines)
MultiSplineFr (multi dim splines)
SFP - sound file player [ note that your preferred sound file player can be adapted to work here too]
CCResponder
CCBank - bank of cc
NoteOnBank - bank of midi notes
Sliders

... more to come


