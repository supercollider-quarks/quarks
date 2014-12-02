//redFrik

RedInstrumentModuleGUI {
	var <win;
	*new {
		^super.new.initRedInstrumentGUI;
	}
	initRedInstrumentGUI {
		
	}
	defaults {
		RedInstrumentModule.all.do{|x|
			x.cvs.do{|cv| cv.value= cv.spec.default};
		};
	}
	randomize {
		RedInstrumentModule.all.do{|x|
			x.cvs.do{|cv| cv.input= 1.0.rand};
		};
	}
}
//this could be a global interface for instruments.  randomizeAll, randomizeSome, randomizeAllInstruments, randomizeSomeInstruments, varyAll, varySome, varyAllInstruments, varySomeInstruments, defaults, savePreset, loadPreset, #1, #2, #3
//now-later slider?  no, use some kind on setting point/preset that i can go back to or interpolate back to