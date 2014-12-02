//redFrik 111115

RedOverdub {
	
	*ar {|buffer, in, gate= 0, fb= 1, atk= 0.01, rel= 0.01, rate= 1|
		var env= EnvGen.ar(Env.asr(atk, 1, rel), gate);
		RecordBuf.ar(in*env, buffer, 0, 1, fb, gate);
		^PlayBuf.ar(buffer.numChannels, buffer, rate*BufRateScale.kr(buffer), 1, 0, 1);
	}
	
	*kr {|buffer, in, gate= 0, fb= 1, atk= 0.01, rel= 0.01, rate= 1|
		var env= EnvGen.kr(Env.asr(atk, 1, rel), gate);
		RecordBuf.kr(in*env, buffer, 0, 1, fb, gate);
		^PlayBuf.kr(buffer.numChannels, buffer, rate*BufRateScale.kr(buffer), 1, 0, 1);
	}
}