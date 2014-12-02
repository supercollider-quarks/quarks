(
var length=1800; // 300 is 5 minutes, 1800 is 30 minutes
TempoClock.default.tempo = 1;
Score.write([
	[0.1, [\s_new, \scpodcast_fdbknois1, 1000, 0, 0, \amp, 0.7]],
	
	[length * 0.34, [\n_set, 1000, \fdbk, 0.5]], // Calm down in middle.
	// (NB fdbk setting is very sensitive between about 0.4 and 0.6, die down or pipe up)
	[length * 0.67, [\n_set, 1000, \fdbk, 1.00]], // Back to normal
	
	[length-50, [\n_set, 1000, \fdbk, 0.42]], // Calm down before end
	[length-10, [\n_set, 1000, \amp, 0]], // Fade out
	[length, [\c_set, 0, 0]] // finish
	], SCPodcast.classdir ++ "/examples/inputdata/scpodcast_fdbknois1.osc");
)
