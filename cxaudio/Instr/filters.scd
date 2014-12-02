
//written jan 10 2003

/*  
Instr([\filters,\binshift,{
    arg audio=0.0,binshift,binstretch;
    FFTfilter.shift(audio,binshift,binstretch);
    });
*/
    
Instr("filters.Integrator",{ arg audio=0.0,coef=0.1,pre=1.0;
    Integrator.ar(audio * pre,coef)
},
    nil,
    \audio
);




    
Instr("filters.RLPF",{ arg audio=0.0,ffreq,rq=0.5;
    RLPF.ar(audio,ffreq ,rq )
},
    nil,
    \audio
);
    
Instr("filters.RHPF",{ arg audio=0.0,ffreq=1200,rq=0.3;
    RHPF.ar(audio,ffreq,rq)
},
    nil,
    \audio
);
    
Instr("filters.LPF",{ arg audio=0.0,ffreq=1200;
    LPF.ar(audio,ffreq)
},
    nil,
    \audio
);
    
Instr("filters.HPF",{ arg audio=0.0,ffreq=600;
    HPF.ar(audio,ffreq)
},
    nil,
    \audio
);
    
Instr("filters.BPF",{ arg audio=0.0,ffreq=130,rq8=1.0;
    BPF.ar(audio ,ffreq,rq8)
},
    nil,
    \audio
);

Instr("filters.BRF",{ arg audio=0.0,ffreq=3000,rq8=2;
    BRF.ar(audio,ffreq,rq8)
},
    nil,
    \audio
);
    
Instr("filters.Slew",{ arg audio=0.0,slewRise=1000,slewFall=1000,drive=1.0;
    Slew.ar(audio,slewRise,slewFall,drive)
},
    nil,
    \audio
);
    
    
Instr("filters.Ringz",{ arg audio=0.0,ffreq=130,microDecay=0.006,pre=0.5,drive=0.8,thru=0.0;
    Ringz.ar(audio * pre,ffreq,microDecay, drive,thru * audio)  
},
    nil,
    \audio
);
    
Instr("filters.Formlet",{ arg audio=0.0,ffreq=130,microDecay=0.01,microAttack=0.005,pre=0.5,drive=0.8,thru=0.0;
    Formlet.ar(audio * pre,ffreq,microAttack,microDecay, drive,thru * audio)    
},
    nil,
    \audio
);
    
Instr("filters.Resonz",{arg audio=0.0,
    ffreq=130,rq8=1.0,drive=0.8,thru=0.0;
    Resonz.ar(audio ,ffreq,rq8, drive,thru * audio)
},
    nil,
    \audio
);

Instr("filters.Median",{ arg audio=0.0,medianLength=1;
    Median.ar((medianLength).asInteger * 2 + 1,audio)
},
    nil,
    \audio
);
    
Instr("filters.distort",{ arg audio=0.0,drive=1.0,post=1.0;
    (audio * drive ).distort * post;
},
    nil,
    \audio
);
    
Instr("filters.softclip",{ arg audio=0.0,drive=2.0,post=1.0;
    (audio * drive ).softclip * post ;
},
    nil,
    \audio
);
    
Instr("filters.TwoPole",{ arg audio=0.0,ffreq=900,radius=0.5,drive=1.0;
    TwoPole.ar(audio,ffreq ,radius, drive).softclip
},
    nil,
    \audio
);
    
Instr("filters.OnePole",{ arg audio=0.0,bicoef=0.0,drive=1.0;
    OnePole.ar(audio,bicoef,drive)
},
    nil,
    \audio
);
    
Instr("filters.OneZero",{ arg audio=0.0,coef=0.5,drive=1.0;
    OneZero.ar(audio,coef,drive)
},
    nil,
    \audio
);
    
Instr("filters.TwoZero",{ arg audio=0.0,ffreq,radius,drive=1.0,thru=0;
    TwoZero.ar(audio,ffreq ,radius,drive,thru * audio) + 0.0
},
    nil,
    \audio
);
    
/** 
Instr([\filters,\AvgAbsAmp],{ arg audio=0.0,coef=0.5,drive=1.0,thru=0.0;
    AvgAbsAmp.ar(audio,coef,drive,thru * audio)
    });
**/

Instr("filters.intBPF",{ arg audio=0.0,
        xfade=0.5,drive=0.8,
        coef=0.6,ffreq=9000,
        pre=1.0,rq8=1.0;
    
    var int,high;
    
    int=Integrator.ar(audio * pre,coef);
    
    high=BPF.ar(int
         ,ffreq,rq8, drive).softclip;
    
    XFader.ar(int,high,xfade)
},
    nil,
    \audio
);
    
    
Instr("filters.hardShaper",{ arg audio=0.0,drive=3;
    HardShaper.ar(audio,drive)
},
    nil,
    \audio
);
    
Instr("filters.combN",{ arg audio=0.0,freq=440,bidecay=0.2;    
    CombN.ar(audio,0.03,freq.reciprocal,bidecay)    
},
    nil,
    \audio
);
    
Instr("filters.combL",{ arg audio=0.0,freq=440,bidecay=0.2;    
    CombL.ar(audio,0.03,freq.reciprocal,bidecay)
},
    nil,
    \audio
);

Instr("filters.combC",{ arg audio=0.0,freq=440,bidecay=0.2;    
    CombC.ar(audio,0.03,freq.reciprocal,bidecay)
},
    nil,
    \audio
);

Instr("filters.Decay2",{ arg audio=0.0,microAttack=0.01,microDecay=0.01;
    Decay2.ar(audio,microAttack,microDecay)
},
    nil,
    \audio
);

Instr("filters.Decay", { arg audio=0.0,microDecay=0.01;
    Decay.ar(audio,microDecay)
},
    nil,
    \audio
);

Instr("filters.Lag", { arg audio=0.0,lag=0.01;
    Lag.ar(audio,lag)
},
    nil,
    \audio
);

Instr("filters.LeakDC", { arg audio=0.0,coef=0.99;
	
    LeakDC.ar(audio,coef)
},
    nil,
    \audio
);

Instr("filters.Slope",{ arg audio=0.0;
    Slope.ar(audio)
},
    nil,
    \audio
);
    
Instr("filters.LPZ1",{ arg audio=0.0;
    LPZ1.ar(audio)
},
    nil,
    \audio
);
    
Instr("filters.HPZ1",{ arg audio=0.0;
    HPZ1.ar(audio)
},
    nil,
    \audio
);
    
Instr("filters.LPZ2",{ arg audio=0.0;
    LPZ2.ar(audio)
},
    nil,
    \audio
);
    
Instr("filters.HPZ2",{ arg audio=0.0;
    HPZ2.ar(audio)
},
    nil,
    \audio
);
    
Instr("filters.BPZ2",{ arg audio=0.0;
    BPZ2.ar(audio)
},
    nil,
    \audio
);
    
Instr("filters.BRZ2",{ arg audio=0.0;
    BRZ2.ar(audio)
},
    nil,
    \audio
);

Instr("filters.MidEQ",{ arg audio=0.0,ffreq=440.0,rq=1.0,db=0.0;
    MidEQ.ar(audio,ffreq,rq,db)
},
    [\audio,\ffreq,\rq,[-100,24]],
    \audio
);


Instr("filters.amp",{ arg audio=0.0,amp=1.0;
    audio * amp
},
    nil,
    \audio
);

Instr("filters.dbAmp",{ arg audio=0.0,db=0;
    audio * db.dbamp
},
    [
    	\audio,
    	[-100,12]
    ],
    \audio
);




