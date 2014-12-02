// wslib
// just testing; a bit dangerous to do this..

+ TempoClock {
	
	setBeats { |beats = 0|
		var tempo;
		tempo = this.tempo;
		this.prStop;
		this.prStart( tempo );
		}
	
	ptr { ^ptr }
	
	}