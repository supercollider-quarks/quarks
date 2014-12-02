
Fb : UGen {

	*delayUGen { ^DelayN }

	*new { 
		arg func, maxdelaytime, delaytime=maxdelaytime, numChannels;
		var buf, phase, frames, sig, adddelay;
		if (maxdelaytime.isNil) {
			adddelay = false;
		} {
			adddelay = true;
			maxdelaytime = maxdelaytime - ControlDur.ir;
			delaytime = delaytime - ControlDur.ir;
		};
		
		numChannels = numChannels ?? { func.value([Silent.ar(1)]).asArray.size };
		numChannels = numChannels max: maxdelaytime.asArray.size max: delaytime.asArray.size;
		
		frames = ControlDur.ir*SampleRate.ir;
		buf = LocalBuf(frames, numChannels).clear;
		phase = Phasor.ar(0, 1, 0, frames);

		sig = func.value(BufRd.ar(numChannels, buf, phase));
		if (adddelay) { sig = this.delayUGen.ar(sig, maxdelaytime, delaytime) };
		BufWr.ar(sig, buf, phase);
		^sig;
	}
}

FbL : Fb {*delayUGen{^DelayL}}

FbC : Fb {*delayUGen{^DelayC}}

FbK : UGen {
	
	*new { 
		arg func, initVals;
		var buf, sig, numChannels;
		
		if (initVals.notNil) {
			numChannels = initVals.size;
			buf = LocalBuf(1, numChannels);
			buf.set([initVals]);
		} {
			numChannels =  func.value([Silent.kr(1)]).asArray.size;
			buf = LocalBuf(1, numChannels).clear
		};

		sig = func.value(BufRd.kr(numChannels, buf, 0));
		BufWr.kr(sig, buf, 0);
		^sig;
	}
}


FbNode : UGen {
	classvar currentSynthDef;
	classvar phasors;

	var buf, frames, output, phase;
	var <numChannels;
	var <interpolation;
	var <maxDelayTime;
	var <input;
	
	*new {
		arg numChannels=1, maxdelaytime, interpolation=2;
		^super.new.init(numChannels, maxdelaytime, interpolation);
	}

	init {
		arg in_numChannels, in_maxDelayTime, in_interpolation;
		var blockSize = Server.default.options.blockSize;
		var sampleRate = Server.default.sampleRate;
		numChannels = in_numChannels;
		maxDelayTime = in_maxDelayTime;
		interpolation = in_interpolation;

		if (sampleRate.isNil) {
			Error("FbNode cannot determine sample rate. (Try booting the server.)").throw;
		};

		if (UGen.buildSynthDef.isNil) {
			Error("FbNode declared outside SynthDef").throw;
		};

		if (currentSynthDef !== UGen.buildSynthDef) {
			// so that we only have one Phasor for each delay time, instead of one for every FbNode
			currentSynthDef = UGen.buildSynthDef;
			phasors = IdentityDictionary.new;
		};

		if (maxDelayTime.notNil && {maxDelayTime < (blockSize/sampleRate)}) {
			"FbNode maxDelayTime less than one control period - setting to one block size.".warn;
			maxDelayTime = nil;
		};

		if (maxDelayTime.isNil) {
			maxDelayTime = blockSize/sampleRate;
			interpolation = 0;
			frames = blockSize * 2;
		} {
			frames = maxDelayTime*sampleRate + blockSize;
			// the extra blockSize samples (in both these branches) is so that we're never 
			// reading and writing the same part of the buffer in the same control period, 
			// a bit like double buffering in the graphics world.  This is necessary because 
			// this class has no way to know in which order the reading and writing will 
			// be done.
		};
		if (phasors.includesKey(frames)) {
			phase = phasors[frames];
		} {
			phase = Phasor.ar(0, 1, 0, frames);
			phasors[frames] = phase;
		};
		
		buf = LocalBuf(frames, numChannels).clear;
		output = Thunk({ BufRd.ar(numChannels, buf, phase-blockSize, 1, interpolation) });
		input = nil;
	}

	asUGenInput {
		^output.value;
	}
	
	at {
		arg channels;
		^output.value[channels]
	}
	
	delay {
		arg delaytime = maxDelayTime;
		var blockSize = Server.default.options.blockSize;
		var sampleRate = Server.default.sampleRate;
		var offset;
		
		offset = max( delaytime*sampleRate, blockSize );
		offset = min( offset, frames-blockSize );

		^BufRd.ar(numChannels, buf, phase-offset,
			1, interpolation);
	}

	write {
		arg signal;
		if (input.isNil) {
			input = signal
		} {
			input = input + signal
		};
		BufWr.ar(input, buf, phase);
		^signal;
	}
}



