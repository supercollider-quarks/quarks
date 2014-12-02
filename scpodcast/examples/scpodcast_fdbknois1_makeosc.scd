(
var length=300; // 300 is 5 minutes, 1800 is 30 minutes
TempoClock.default.tempo = 1;
Score.write([
	[0.1, [\s_new, \scpodcast_fdbknois1, 1000, 0, 0, \amp, 0.7]],
	[length-50, [\n_set, 1000, \fdbk, 0.42]], // Calm down before end
	[length-10, [\n_set, 1000, \amp, 0]], // Fade out
	[length, [\c_set, 0, 0]] // finish
	], SCPodcast.classdir ++ "/examples/inputdata/scpodcast_fdbknois1.osc");
)
