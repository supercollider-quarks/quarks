ChordtrisScoreManager {
	
	var <score = 0;
	var <lines = 0;
	var <level = 0;
	
	incrementLines {
		lines = lines + 1;
	}
	
	nextLevel {
		if(level < 10) { level = level + 1 };
	}
	
	linesCompleted { |numOfCompletedLines|
		var multiplier;
		
		if(numOfCompletedLines <= 0) { ^0 };
		
		lines = lines + numOfCompletedLines;
		
		if((lines % 10) == 0) { this.nextLevel };
		
		//(numOfCompletedLines + "lines completed").postln;
		
		switch(numOfCompletedLines,
			1, { multiplier = 40 },
			2, { multiplier = 100 },
			3, { multiplier = 300 },
			4, { multiplier = 1200 },
			{ multiplier = 0 }
		);
		
		score = score + (multiplier * (level+1));
		^score;
	}
	
	reset {
		score = 0;
		lines = 0;
		level = 0;
	}
	
	
}