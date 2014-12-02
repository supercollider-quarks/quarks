
code::
// simple random keyboard player
(
s.boot;
s.latency = nil;    // make sure we respond quickly
thisThread.randSeed = 2014;
k = KeyPlayer(\rand);
"qwertzuiopasdfghjklyxcvbnm".do { |char|
    var note = (midinote: rrand(36, 96), dur: rrand(0.1, 0.3));
   k.putDown(char, { |char| char; note.play });
};

     // make k.rec a KeyLoop:
k.makeLoop;
g = KeyPlayerGui(k);
)

// second player shows up as well;
KeyPlayer(\xs);

// the keys that have associated actions are shown colored in KeyPlayerGui.

// Meta control is available from alt-key shortcuts :

alt-r toggles RECORDING
alt-p toggles KeyLoop PLAYBACK
alt-l toggles LOOPED on/off
alt-f FORKS a (non-looped) task - playOnce

alt-g GUI opens for the KeyLoop
alt-t TOP focus goes to top control that sets current KeyPlayer

// not done yet
alt-1..9,0 could go to one of top ten KeyPlayers on buttons.

g.focusToPlayerName(\xs);

k.rec.list.print
k.rec.startRec;
k.rec.stopRec;

k.rec.loop = true;
k.rec.play;
k.rec.tempo = 1;
k.rec.range = 0.25;
k.rec.lpStart = 0.25;
k.rec.lpStart = 0.5;

k.rec.jitter = 0.2;
k.rec.quantizeTo(1/6);
k.rec.quantizeTo(1/4);

k.rec.listInfo;
k.rec.printLists;

k.rec.play;

KeyLoop
