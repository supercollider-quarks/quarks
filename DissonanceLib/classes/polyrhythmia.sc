PolyRhythmia { 
// By JesterN/Alberto Novello and J.S. Lach. (c) 2009.

//class must output how many notes are in each part of the process in each track: begin acc, steady, decell, end!!!

	var <deltaonset, <phase, <tours, <toursBeg, <toursSteady, <toursEnd; //input variables
	var <quantum, <length_acc, <coming_down,<timeToEnd; //timing
	var <durBeg, <durAcc, <durSteady, <durBack, <durEnd;  //durations
	var <startAcc, <startSteady, <startBack, <startEnd; //cues
	var factor, <ndiv, step, elevation, coeff; // for the upwards trip
	var factor2, <ndiv2, step2, elevation2, coeff2; // for the backwards trip
	var <outputArray; //output values

//counts events for each track for each section of the piece (for manipulation)
	var <notesBeg, <notesUp, <notesSteady, <notesDown, <notesEnd;

	*new {|donset, ph, trs, trsBeg, trsSteady, trsEnd| 
		  ^super.new.init(donset, ph, trs, trsBeg, trsSteady, trsEnd).calc }
	
	// use the first deltaonset as the quantum: TOURS ARE COUNTED IN QUANTUMS
	// USE PHASE 0 FOR THE FIRST DELTAONSET
	init {|donset, ph, trs, trsBeg, trsSteady, trsEnd| 
		deltaonset = donset ? [8, 4, 2, 1, 1, 1]; // defaults
	 	phase = ph ? [0, 1, 0, 1/2, 3/2, 3/4];
		tours = trs ? 10; 
		toursBeg =  trsBeg ? 5;
		toursSteady = trsSteady ? 5;
		toursEnd = trsEnd ? 5;
		
		// secondary calculation:
		quantum = deltaonset[0];
		length_acc = tours * quantum; // at this time the steady rhythm starts
		
		durBeg = toursBeg*quantum; 
		durAcc = tours * quantum;
		durSteady = toursSteady*quantum;
		durBack = tours*quantum;
		durEnd = toursEnd*quantum;
		
		startAcc = durBeg;
		startSteady = durBeg + durAcc; 
		startBack = startSteady + durSteady;
		startEnd = startBack + durBack;
		
		// coming_down = (toursBeg * quantum) + length_acc + (toursSteady * quantum ); 
		coming_down = (toursSteady * quantum);
		
		
		// startEnd = (toursBeg * quantum) + (2 * length_acc) + (toursSteady * quantum );  
		
		notesUp={[]}! deltaonset.size;
		notesSteady={[]}! deltaonset.size;
		notesDown={[]}! deltaonset.size;
		notesBeg={[]}! deltaonset.size;
		notesEnd={[]}! deltaonset.size;
		
		factor={[]}! deltaonset.size;
		ndiv={[]}!deltaonset.size; // number of divisions of the time intervals to accelerate
		step={[]}!deltaonset.size; 
		elevation={[]}! deltaonset.size;
		coeff={[]}! deltaonset.size;
		
		//init descending variables
		factor2={[]}! deltaonset.size;
		timeToEnd={[]}! deltaonset.size;
		ndiv2={[]}! deltaonset.size; // number of divisions of the time intervals to decelerate
		step2={[]}! deltaonset.size; 
		elevation2={[]}! deltaonset.size;
		coeff2={[]}! deltaonset.size;
		outputArray = {[]}!ndiv.size;
		
	}

    // main method
	calc { 
		this.calcBeginning;
		this.calcUp; 
		this.calcSteady;
		this.calcDown;
		this.calcEnd; 
	}
	
	calcBeginning { var onset;
		ndiv.size.do{| track| 
			toursBeg.do{
				onset = deltaonset[0];
				outputArray[track] = outputArray[track].add(onset);
				notesBeg[track]=toursBeg;
			}
		}
	}

	calcUp { var f = 0, onset;
		for (0, deltaonset.size - 1) { 
			onset = 2 * quantum;
			factor[f] = 0.9;
			while {onset > quantum} {
				factor[f] = factor[f] + 0.1;
				ndiv[f] = factor[f] * tours;
				step[f] = length_acc / ndiv[f];
				elevation[f] = log(1 - (deltaonset[f] / length_acc)) / 
								log (1 - (ndiv[f].reciprocal));
				coeff[f] = length_acc ** (1 - elevation[f]); // calculation
//				onset = (coeff[f] * (step[f] ** elevation[f])) + (phase[f] / ndiv[f]);
				onset = (coeff[f] * (step[f] ** elevation[f]));
			};
			//factor[f] = factor[f] - 0.1; // uncomment if first delta onset > quantum
			if (factor[f] < 1) {factor[f] = 1}; 
			f = f + 1;
		};
				
		ndiv = factor * tours; 
		step = length_acc / ndiv; // time step

		// acceleration function calculation / time compression for each track
		elevation = log( 1 - (deltaonset / length_acc)) / log(1 - (1 / ndiv));
		coeff = length_acc ** (1 - elevation);
		
		this.calcArraysUp;
	}

	calcArraysUp {var onset;
		ndiv.size.do{|track| 
			ndiv[track].do{|k|
				onset = coeff[track] * (step[track] ** elevation[track]) 
						* ( ((k+1)**elevation[track]) - ( k ** elevation[track]));
				onset = onset + (phase[track] / ndiv[track]) ; 
                   outputArray[track] = outputArray[track].add(onset);     
                   notesUp[track]=outputArray[track].size - notesBeg[track];             
			}
		}
	}

	calcSteady { var onset, nOnsetSteady;
		nOnsetSteady = (toursSteady * quantum) / deltaonset; 
		nOnsetSteady.size.do{|track| 
			nOnsetSteady[track].do{ 
				onset = deltaonset[track];
                   outputArray[track] = outputArray[track].add(onset);
                   notesSteady[track]=outputArray[track].size - notesUp[track] - notesBeg[track];
			}
		}
	}

	calcDown {var track, onset, onset1, onset2,lastonset;
	// first calc factors again:
		track = 0; // track number
		for(0, deltaonset.size - 1, {
			lastonset = 2 * quantum; // just a random value>quantum to start the loop
			factor2[track] = 0.9;
			timeToEnd[track] = (toursBeg * quantum) + (2 * length_acc) + 
							(toursSteady * quantum) - outputArray[track].sum;
			while({lastonset > quantum}, {
				factor2[track] = factor2[track] + 0.1; 
				ndiv2[track] = factor2[track] * tours; 
	     	     step2[track] = timeToEnd[track] / ndiv2[track]; // time step
				elevation2[track] = log(1 - (deltaonset[track] / timeToEnd[track])) /
									 log(1 - (step2[track] / timeToEnd[track]));
				coeff2[track] = 1 / (timeToEnd[track]**( elevation2[track] - 1));
				onset1 = coeff2[track] * ((timeToEnd[track] - (step2[track] * (ndiv2[track] - 1))) 						**elevation2[track]);
				onset2 = 0;
				lastonset = onset1 - onset2;		
			});
			factor2[track] = factor2[track] - 0.0;  
			if(factor2[track] < 1, {factor2[track] = 1}); 
			track = track + 1;
		});			
	// deriving vars
		ndiv2 = factor2 * tours; 
		for (0, deltaonset.size - 1, {arg track;
			timeToEnd[track] = (toursBeg * quantum) + (2 * length_acc) + 
					(toursSteady * quantum) - outputArray[track].sum;
			step2[track] = timeToEnd[track] / ndiv2[track]; // time step
			elevation2[track] = log( 1 - (deltaonset[track] / timeToEnd[track])) / 
					log(1-(step2[track]/timeToEnd[track]));
			coeff2[track] = 1/(timeToEnd[track]**( elevation2[track]-1));
          });	
          
		this.calcArraysDown;
	}
	
	calcArraysDown{ var onset, onset1, onset2;
		ndiv2.size.do{|track| 
			ndiv2[track].do{|k| 
				onset1 = coeff2[track] * ((timeToEnd[track] - (step2[track] * (k))) ** elevation2[track]);
				onset2 = coeff2[track] * ((timeToEnd[track] - (step2[track] * (k + 1))) ** elevation2[track]);
				//onset = onset1 - onset2;
				onset = onset1 - onset2 + phase[track]/ndiv[track];
                   outputArray[track] = outputArray[track].add(onset);
                   notesDown[track]=outputArray[track].size - notesUp[track] - notesBeg[track] - notesSteady[track];
			}
		}
	}
	
	calcEnd {var onset;
		ndiv.size.do{| track| 
			toursEnd.do{ 
				onset = deltaonset[0];
                   outputArray[track] = outputArray[track].add(onset);
                   notesEnd[track]=toursEnd;
			}
		}
	}
	
}