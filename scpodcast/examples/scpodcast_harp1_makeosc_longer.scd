(
var length=1800; // 300 is 5 minutes, 1800 is 30 minutes
TempoClock.default.tempo = 1;
Score.write([
	[0.1, [\s_new, \scpodcast_harp1, 1000, 0, 0, \amp, 0.7]],
	
	[length * 0.33, [\n_set, 1000, \pluckratefactor, 3]], // More plucking
	[length * 0.67, [\n_set, 1000, \pluckratefactor, 1]], // Less plucking
	
	[length-15, [\n_set, 1000, \pluckratefactor, 0.2]], // Ask the plucking to calm down
	[length-5, [\n_set, 1000, \amp, 0.0]], // Fade out
	
	[length, [\c_set, 0, 0]] // finish
	], SCPodcast.classdir ++ "/examples/inputdata/scpodcast_harp1.osc");
)
