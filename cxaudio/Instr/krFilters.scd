
Instr.orc(\krFilters,[
	
	\Integrator,{ arg bipolar=0.0,coef,pre=1.0;
		Integrator.kr(bipolar * pre,coef)
	},
	\RLPF,{ arg bipolar=0.0,ffreq,rq=0.5;
		RLPF.kr(bipolar,ffreq ,rq )
	}
	,
	\RHPF,{ arg bipolar=0.0,ffreq=1200,rq=0.3;
		RHPF.kr(bipolar,ffreq,rq)
	},
	
	\LPF,{ arg bipolar=0.0,ffreq=1200;
		LPF.kr(bipolar,ffreq)
	},
	
	\HPF,{ arg bipolar=0.0,ffreq=600;
		HPF.kr(bipolar,ffreq)
	},
	
	\BPF,{ arg bipolar=0.0,ffreq=130,rq8=1.0;
		BPF.kr(bipolar ,ffreq,rq8)
	},

	\BRF,{ arg bipolar=0.0,ffreq=3000,rq8=2;
		BRF.kr(bipolar,ffreq,rq8)
	},
	
	\Slew,{ arg bipolar=0.0,slewRise=1000,slewFall=1000,drive=1.0;
		Slew.kr(bipolar,slewRise,slewFall,drive)
	},
	
	
	\Ringz,{ arg bipolar=0.0,ffreq=130,microDecay=0.006,pre=0.5,drive=0.8,thru=0.0;
		Ringz.kr(bipolar * pre,ffreq,microDecay, drive,thru * bipolar)	
	},	
	
	\Formlet,{ arg bipolar=0.0,ffreq=130,microDecay=0.01,microAttack=0.005,pre=0.5,drive=0.8,thru=0.0;
		Formlet.kr(bipolar * pre,ffreq,microAttack,microDecay, drive,thru * bipolar)	
	},
	 \Resonz,{arg bipolar=0.0,
		ffreq=130,rq8=1.0,drive=0.8,thru=0.0;
		Resonz.kr(bipolar ,ffreq,rq8, drive,thru * bipolar)
	},

	\Median,{ arg bipolar=0.0,medianLength=1;
		Median.kr((medianLength).asInteger * 2 + 1,bipolar)
	},
	\distort,{ arg bipolar=0.0,drive=1.0,post=1.0;
		
		(bipolar * drive ).distort * post;
	},
	\softclip,{ arg bipolar=0.0,drive=2.0,post=1.0;
		
		(bipolar * drive ).softclip * post ;
	},	
	\TwoPole,{ arg bipolar=0.0,ffreq=900,radius=0.5,drive=1.0;
		
		TwoPole.kr(bipolar,ffreq ,radius, drive).softclip
	},
		
	\OnePole,{ arg bipolar=0.0,bicoef=0.0,drive=1.0;
		
		OnePole.kr(bipolar,bicoef,drive)
	},
		
	\OneZero,{ arg bipolar=0.0,coef=0.5,drive=1.0;
		OneZero.kr(bipolar,coef,drive)
	},
		
	\TwoZero,{ arg bipolar=0.0,ffreq,radius,drive=1.0,thru=0;
		TwoZero.kr(bipolar,ffreq ,radius,drive,thru * bipolar)
	},
	
//	\AvgAbsAmp,{ arg bipolar=0.0,coef=0.5,drive=1.0,thru=0.0;
//		AvgAbsAmp.kr(bipolar,coef,drive,thru * bipolar)
//	},


	\intBPF,{ arg bipolar=0.0,
				xfade=0.5,drive=0.8,
				coef=0.6,ffreq=9000,
				pre=1.0,rq8=1.0;
		
		var int,high;
		
		int=Integrator.kr(bipolar * pre,coef);
		
		high=BPF.kr(int
			 ,ffreq,rq8, drive).softclip;
		
		XFader.kr(int,high,xfade)
	},
	
	
	\hardShaper,{ arg bipolar=0.0,drive=3;
		HardShaper.kr(bipolar,drive)
	},
	
	\DelayN,{ arg bipolar=0.0,freq=440,bidecay=0.2;	
		
		DelayN.kr(bipolar,0.03,freq.reciprocal,bidecay)
		
	},
	\DelayL,{ arg bipolar=0.0,freq=440,bidecay=0.2;	
		
		DelayL.kr(bipolar,0.03,freq.reciprocal,bidecay)
		
	},
	\DelayA,{ arg bipolar=0.0,freq=440,bidecay=0.2;	
		
		DelayC.kr(bipolar,0.03,freq.reciprocal,bidecay)
		
	},
	\Decay2,{ arg bipolar=0.0,attack=0.01,decay=0.01;
		Decay2.kr(bipolar,attack,decay)
	},
	\Decay, { arg bipolar=0.0,decay=0.01;
		Decay.kr(bipolar,decay)
	},
	\Lag, { arg bipolar=0.0,lag=0.01;
		Lag.kr(bipolar,lag)
	},
	\LeakDC, { arg bipolar=0.0,coef=0.99;
		LeakDC.kr(bipolar,coef)
	},
	\Slope,{ arg bipolar=0.0;
		Slope.kr(bipolar)
	},
	\LPZ1,{ arg bipolar=0.0;
		LPZ1.kr(bipolar)
	},
	\HPZ1,{ arg bipolar=0.0;
		HPZ1.kr(bipolar)
	},
	\LPZ2,{ arg bipolar=0.0;
		LPZ2.kr(bipolar)
	},
	\HPZ2,{ arg bipolar=0.0;
		HPZ2.kr(bipolar)
	},
	\BPZ2,{ arg bipolar=0.0;
		BPZ2.kr(bipolar)
	},
	\BRZ2,{ arg bipolar=0.0;
		BRZ2.kr(bipolar)
	},
	\amp, { arg bipolar=0.0,amp=1.0;
		bipolar * amp
	}
],
	\bipolar
);




