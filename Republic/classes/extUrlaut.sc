Urlaut { 
	*initClass {
		Class.initClassTree(Event);
			// support event type urlaut: 
		Event.addEventType(\urlaut, { |server| 
			var formA, formZ, freqsA, freqsZ;
			// server.postln;
			~freq = ~freq * 2;
			~type = \note;
			~instrument = \ur2;
			~voiceA = ~voiceA ? \tenor; 
			~voiceZ = ~voiceZ ?  ~voiceA ? \tenor; 
			
			~vowelA = ~vowelA ? \a; 
			~vowelZ = ~vowelZ ?  ~vowelA ? \a; 
		
			~fundA = ~fundA ?? { ~freq.value * 0.25 };
			~fundZ = ~fundZ ? ~fundA ?? { ~freq.value * 0.25 };
			
			formA = FormantLib.at(~voiceA.asSymbol, ~vowelA.asSymbol);
			formZ = FormantLib.at(~voiceZ.asSymbol, ~vowelZ.asSymbol);
			
			freqsA = formA[\freq]; 
			freqsZ = formZ[\freq]; 
			
			~f1a = freqsA[0]; ~f2a = freqsA[1]; 
			~f1z = freqsZ[0]; ~f2z = freqsZ[1]; 
			
			currentEnvironment.play;
		});
	}
}