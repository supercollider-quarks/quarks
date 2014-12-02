//--
work in progress.  happy for any comments or contributions.


note! some of the examples require additional classes like RedGA etc.  these are available online at my homepage http://www.fredrikolofsson.com under code->sc


//--
140108 - RedFingerprint optimized pen drawing, removed RedWindow and corrected examples
	removed old html helpfiles
130225 - RedVector2D and RedVector3D optimizations
121130 - moved RedQWindow out of scide_scapp folder
121128 - added qt (RedQWindow) and bumped up required sc version to 3.5
	added helpfile for RedQWindow
	added new example 210-patterns_and_particles.scd
	bugfix for RedUniverse interpolate class method
121126 - added update2 to RedSpring - thanks a. bartetzki
120820 - bugfix memory leak in RedParticleSystem - thanks d. kolokol
111004 - added examples 013, 017, 046, 161
	changed envelope in ex 004 from kr to ar
	changed example 160 to animate instead of play
110927 - all helpfiles converted to scdoc format
110914 - added 191-kmeans2 example
110323 - moved RedWindow cocoa into scide_scapp folder
101214 - support for discrete worlds with surroundings and neighbours
	one new discrete world example added
101210 - cleaned up and added helpfiles for RedHiddenObject, RedParticle, RedBoid, RedRock, RedFood, RedAgent
	added 2 boids examples and cleaned up a few others
	wrote addForceWander1D and addForceWander3D methods for RedBoid
	wrote addForceAngular3D, pendulumOffset3D, pendulumLoc3D
101208 - added manhattanDistance to RedVector
	added RedKMeans class and example 190
	added animate, frame and frameRate for RedJWindow to fake primitive
100602 - removed bugfixes for Collection and FloatArray and moved the species {^this.class} fix into the RedVector class itself
090629 - helpfiles and examples now uses view redirect instead of GUI
	bugfix for RedWindow and RedJWindow resize
090624 - optimised collision detection.  added distance line example
090617 - added RedObject:spring and RedSpring helper class.  also added spring examples and RedObject:containsLoc
090616 - added example 102.  added RedWorld1 class.  RedWindow and RedJWindow removed relativeOrigin
090523 - moved some classes to folder 'additional'.  added RedPerlin
090522 - added RedMRCM and RedIFS
090521 - RedWindow and RedJWindow now draws in the UserView - thanks Thor
090514 - growth example and asPoint added to RedVector
090510 - added RedLSystem and RedLTurtle classes
	updated example 070-lsystem.scd
	edited links in all helpfiles and in the overview
	added s.sync to a few examples
	updated and moved redFingerprint quark into redUniverse
081111 - moved RedWindow and RedJWindow into separate folders (osx, linux, windows) so that cocoa gui code is ignored on linux+windows
080929 - some minor additions
	added extPoint asRedVector2D helper methods
	added RedHiddenObject.  useful when using attractors
	added <userView for RedWindow and RedJWindow
080219 - updated for sc3.2 and swingosc0.59
	fixed all pendulum examples to draw line on swingosc (added a GUI.pen.stroke)
	changed from mouseover to mousemove so now it's required to click&drag to update mouse position.
	fixed 150-track_synth.scd and took away RedTrack.  now using only standard ugens for audio tracking.
080116 - changed from 25 to 40 fps for RedWindow and RedJWindow .play
	some small corrections to match Pen changes to width_
071205 - converted all help.rtf to .html and the examples to .scd
	added the example overview file.
	changed so that RedJWindow is disabled by default.
071031 - update for 3.1
	removed penExt.sc with its strokeColor_ and fillColor_ extensions.
	tiny fix for 150-track_synth.rtf.
071029 - bugfix
	now RedWindow shouldn't crash sc anymore.  it was due to scott's window scoll implemented earlier this autumn.

