

Instr("toneknob.hiloBalance",{ arg audio=0.0,xover=200,logain=1.0,higain=1.0;
						
	var lo,hi;
		
	lo = LPF.ar( audio, xover);
	hi = audio - lo;
	
	lo = if(logain == 1.0,{lo},{lo * logain});
	hi = if(higain == 1.0,{hi},{hi * higain});
	
	Mix.new([ lo , hi])

},#[
	\audio,
	\freq,
	\gain,
	\gain
],
	\audio
);

Instr("toneknob.himidloBalance",{ arg audio=0.0,xover=200,xover2=3000,logain=1.0,midgain=1.0,higain=1.0;
						
	var lo,hi,rest,mid;
		
	lo = LPF.ar( audio, xover);
	rest = audio - lo;
	hi = HPF.ar(rest,xover2);
	mid = rest - hi;
	
	
	lo = if(logain == 1.0,{lo},{lo * logain});
	mid = if(midgain == 1.0,{mid},{mid * midgain});
	hi = if(higain == 1.0,{hi},{hi * higain});
	
	Mix.new([ lo , mid, hi])

},#[
	\audio,
	\freq,
	\freq,
	\gain,
	\gain,
	\gain
],
	\audio
);



Instr("toneknob.bpfGain",{ arg audio=0.0,freq=200,rq8=1.0,gain=1.0;
						
	var chan,rest;
		
	chan = BPF.ar( audio, freq,rq8);
	rest = audio - chan;
	
	Mix.new([chan * gain,rest])

},#[
	\audio,
	[60,20000],
	nil,
	[0,20]
],
	\audio
);



// awesome !
Instr("toneknob.bpfHardShaper",{ arg audio=0.0,freq=200,rq8=1.0,drive=1.0;
						
	var chan,rest;
		
	chan = BPF.ar( audio, freq,rq8);
	chan = HardShaper.ar(chan,drive);
	rest = BRF.ar( audio, freq, rq8);
	
	Mix.new([chan,rest])

},#[
	\audio,
	[60,10000],
	[0.1,4.0],
	\drive
],
	\audio
);




