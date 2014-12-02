# crucial library


Classes for [SuperCollider](http://github.com/supercollider/supercollider)


* __Instr__ —
	load resuable functions from disk, create dynamically variable architecture SynthDefs for use in Synths, Pbind/Patterns and Patches.  Quarks can include Instr/ folders to share with others.
* __InstrBrowser__ —
	peruse and search your installed Instr

* __Patch__ — an Instr player
* __Player__ — class framework for objects that play. manages loading of resources, includes support for sound file recording and saving object state to file.

* __extended Spec system__ — extended datatypes specifications

* __OSCSched__ — schedule OSC bundles to be sent just prior to event time. revoke scheduled bundles, relocate in time, stop, change tempo
* __BeatSched__ — full featured function scheduling with same features as OSCSched

* __Tempo__ - tempo calculations and centralized control
* __TempoBus__ - maintains a control rate bus on scsynth

* __MultiChanRecorder__ — utility for recording multiple Busses to disk

* __Editors__
	* NumberEditor
	* EnvEditor
	* DictionaryEditor


* __SFP__ — SoundFilePlayer

* __Sample__ — easy to load to server, beat matching and slicing tools

* __StreamKrDur__ — play Pseq and friends at \control rate on a Bus
* __Stream2Trig__ - play Pseq and friends as rhythmic \trigs on a Bus

* __InstrSpawner__ — similar to Pbind for Instr
* __InstrGateSpawner__ — similar to Pbind for Instr


