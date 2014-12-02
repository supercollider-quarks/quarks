
/*
	trails are efx with delay lines and pitch shifts or filtrations tapering off into the old cosmic void
*/



Instr([\trails,\pelog],{ arg audio=0.0,degree=1,pchDispersion=0.0;

	var scale;
	scale = FloatArray[0, 1, 3, 7, 8];
	
	PitchShift.ar( audio, 0.1,
		DegreeToKey.kr(scale.asBufnum,degree.round(1.0)).midiratio,
		pchDispersion,0)

},#[
	\audio,
	[-9,14,\linear,1]
],\audio);

Instr([\trails,\scriabin],{ arg audio=0.0,degree=1,pchDispersion=0.0;

	var scale;
	scale = FloatArray[0, 1, 4, 7, 9];
	
	PitchShift.ar( audio, 0.1,
		DegreeToKey.kr(scale.asBufnum,degree.round(1.0)).midiratio,
		pchDispersion,0)

},#[
	\audio,
	[-9,14,\linear,1]
],\audio);


Instr([\trails,\penta],{ arg audio=0.0,degree=1,pchDispersion=0.0;

	var scale;
	scale = FloatArray[0, 1, 5, 7, 11];
	
	PitchShift.ar( audio, 0.1,
		DegreeToKey.kr(scale.asBufnum,degree.round(1.0)).midiratio,
		pchDispersion,0)

},#[
	nil,
	[-9,14,\linear,1]
],
	\audio
);




// boring
Instr([\trails,\beatDelay],{ arg audio=0.0,beat=1,tempo=1.0;
	DelayC.ar(audio,1 / (40 / 60) , beat * (tempo ?? {Tempo.kr}).reciprocal)
	
},#[
	\audio,
	[0.001,1.0,\linear,0.125]
],
	\audio
);



//decent feed
Instr([\trails,\feed],{ arg audio=0.0,beat=0.375,xfade=0,feedback=0.5,tempo=1.0,bufferProxy;

	var dly,func;
	audio = NumChannels.ar(audio,bufferProxy.numChannels);
	dly = 	PingPongT.ar(bufferProxy.bufnumIr, audio,beat,feedback,0.05,1, (tempo ?? {Tempo.kr}));
	
	LinXFade2.ar(audio,dly,xfade);

},#[
	\audio,
	[0.001,1.0,\linear,0.125],
	[-1,1],
	[0,1]
],
	\audio
);




Instr([\trails,\pelogFeed],{ arg audio=0.0,beat=0.375,degree=2,xfade=0,
					feedback=0.5,drive=1,tempo=1.0,bufferProxy;

	var dly;
	dly =  PingPongTP.ar(audio,{arg in;
	 	Instr.ar([\trails,\pelog],[in,degree])
	},1,beat,feedback,0.05,tempo ?? {Tempo.kr},bufferProxy.bufnumIr);
	LinXFade2.ar(audio,dly,xfade);

},#[
	\audio,
	[0.001,1.0,\linear,0.125],
	[-9,14,\linear,1],
	[-1,1],
	\feedback,
	[0,1.5]
],
	\audio
);

	
	
Instr([\trails,\rlpfPing],{ arg audio=0.0,ffreq=3000,rq=0.5,beats=0.375,feedback=0.5,xfade=0.0
	,tempo=1.0,bufferProxy;

	LinXFade2.ar(
			audio,
			PingPongTP.ar(HPF.ar(audio,150),{arg in; 
				RLPF.ar(in,ffreq,rq)},0.5,beats,
					feedback,0.0,tempo ?? {Tempo.kr},bufferProxy.bufnumIr),
			xfade
		)

},[
	\audio,
	\ffreq,
	\rq,
	[0.125,0.5,\linear,0.125],
	\unipolar,
	\bipolar
],
	\audio
);


//Instr([\trails,\multiFeedXfade],{ arg audio=0.0, drive=0.4,ffreq=3000,rq=0.5,freqOffset=33,beat=0.25,xfade=0;
//	
//	var dly;
//	dly = Instr.ar([\trails,\multiFeedRLPF],[ audio , drive,ffreq,rq,freqOffset,beat]);
//	LinXFade2.ar( audio,dly,xfade)
//
//},[
//
//
//	nil,
//	\unipolar,
//	nil,
//	nil,
//	[0,60],
//	[StaticSpec(0.125,0.5,\linear,0.125)],
//	\bipolar
//
//]);


//(//autowah
//j=PbindfGui.browse(	Roster(":Music:sets:basses").all );
//
//Instr([\trails,\autowah],{ arg audio=0.0,sensitivity=8,freqspike=1000,rqspike=1.0;
//
//	var d;
//	d=Amplitude.kr(audio,0.2,0.4,sensitivity);
//	RLPF.ar(audio,(freqspike * d).min(6000),rqspike * d);
//},#[
//	nil,
//	[0,16],
//	\freq,
//	[\rq8]
//])
//.test([ {j.ar} ])
//)

//
//(// ok
//j=PbindfGui.browse(	Roster(":Music:sets:basses").all );
//Instr([\trails,\hirabeat],{ arg audio=0.0,beat=0.5,pchRatio=1,xfade=0;
//
//	var dly;
//	dly=Instr.ar([\trails,\beatDelay],[audio,beat]);
//	dly = Instr.ar([\trails,\pitchCrunch],[dly,pchRatio,10]) ;
//	dly = HPF.ar(dly,300);
//	LinXFade2.ar(audio,dly,xfade);
//
//},#[
//	nil,[[0.001,1.0,\linear,0.125],
//	[0,8],
//	[-1,1]

//])
//.test([ {j.ar} ])
//
//)



//(
////j=PbindfGui.browse(	Roster(":Music:sets:basses").all );
////Instr([\trails,\recursPitch],{ arg audio=0.0,beat=0.5,pchRatio=1,xfade=0,feedback=0.5,drive=10;
////
////	var dly,func;
////	
////	func = { arg in;
////		in = Instr.ar([\trails,\pitchCrunch],[in,pchRatio,drive]) ;
////	};
////	
////	dly = DelayProcessSR.ar(func,audio,
////		thisSynth.tempo.reciprocal * 2,beat * GetTempo.kr.reciprocal,
////		feedback);
////	dly = HPF.ar(dly,300);
////	LinXFade2.ar(audio,dly,xfade);
////
////},#[
////	nil,
////	[0.001,1.0,\linear,0.125],
////	[0,8],
////	[-1,1],
////	nil,
////	[0,40]

////])
////.test([ {j.ar} ])
////
//)



/*
MISSING : MultiPitchShift

Instr([\trails,\pitchCrunch],{ arg audio=0.0,pchRatio=1,drive=1,pchDispersion=0.0;

	var scale,out;
	scale = FloatArray[0,1,5,6,10];
	
	out=
	MultiPitchShift.ar(2, audio, 0.1,
		pchRatio,
		pchDispersion,0);
		
	HardShaper.ar(out,drive)

},#[
	nil,
	[0.0,8.0,\linear],
	[0.0,40]

]);

Instr([\trails,\HiraJoshi],{ arg audio=0.0,degree=1,pchDispersion=0.0;

	var scale;
	scale = FloatArray[0,1,5,6,10];
	
	MultiPitchShift.ar( 2, audio, 0.1,
		DegreeToKey.kr(scale,degree.round(1.0)).midiratio,
		pchDispersion,0)

},#[
	nil,
	[-9,14,\linear,1]

]);

*/
/*

// j=PbindfGui.browse(	Roster(":Music:sets:basses").all );
Instr([\trails,\upFeed],{ arg audio=0.0,beat=0.375,pchRatio=2,xfade=0,
			feedback=0.5,drive=1,tempo=1.0,bufferProxy;

	var dly,func;
	
	dly = PitchShift.ar(audio,0.1,pchRatio);
	dly =  PingPongTP.ar(dly,{arg in; 
			HardShaper.ar(in,drive)
		},1,beat,feedback,
				0.05,tempo,bufferProxy.bufnumIr);
	
	LinXFade2.ar(audio,dly,xfade);

},#[
	nil,
	[0.001,1.0,\linear,0.125],
	[0,8],
	[-1,1],
	nil,
	[0,1.5]

]);


// j=PbindfGui.browse(	Roster(":Music:sets:basses").all );
Instr([\trails,\hpfFeed],{ arg audio=0.0,beat=0.375,ffreq=200,xfade=0,feedback=0.5,drive=1,buffer;

	var dly,func;
	
	//dly = HPF.ar(audio,ffreq);
	dly = PitchShift.ar(audio,0.1,2,0.01);
	dly = DelayT.ar( dly,0.5,0.5);
	dly =  PingPongT.ar(buffer.bufnumIr,dly,beat,feedback);
	dly = HardShaper.ar(dly,drive);
	LinXFade2.ar(audio,dly,xfade);

},#[
	nil,
	[0.001, 0.5, \linear, 0.125],
	\ffreq,
	[-1,1],
	nil,
	[0,1.5]

]);


// j=PbindfGui.browse(	Roster(":Music:sets:basses").all );
Instr([\trails,\pitchCascade],{ arg audio=0.0,transpose=0,beats=0.375,feedback=0.5,xfade;

	var dly;

	dly = PitchCascade.ar(audio,transpose,0.5,beats,feedback,0.1);
	LinXFade2.ar(audio,dly,xfade);

},#[
	nil,
	[-24,24,\linear,1],
	[0.001,1.0,\linear,0.125],
	[0,1]

]);


// j=PbindfGui.browse(	Roster(":Music:sets:basses").all );
Instr([\trails,\pitcher],{ arg audio=0.0,transpose=0,beats=0.375,xfade,tempo=1.0;

	var dly,pitch;
	pitch=transpose.midiratio;
	dly = DelayT.ar(
		PitchShift.ar(audio,0.1, pitch),
			0.5,beats,0.0,tempo);
	LinXFade2.ar(audio,dly,xfade);

},#[
	nil,
	[-24,24,\linear,1],
	[0.125,1.0,\linear,0.125],
	[-1,1]

]);

*/


//( // modal then feed
////j=PbindfGui.browse(	Roster(":Music:sets:basses").all );
//j=Roster.at(OrcJoint).choose;
//Instr([\trails,\pelogFeed],{ arg audio=0.0,beat=0.375,degree=2,xfade=0,feedback=0.5,drive=1;
//
//	var dly,func;
//	
//	dly = Instr.ar([\trails,\pelog],[audio,degree]);
//	dly =  PingPongTP.ar(dly,{arg in; HardShaper.ar(in,drive)},1,beat,feedback,0.05);
//	LinXFade2.ar(audio,dly,xfade);
//
//},#[
//	nil,
//	[0.001,1.0,\linear,0.125],
//	[-9,14,\linear,1],
//	[-1,1],
//	nil,
//	[0,1.5]

//])
//.test([ {j.ar} ])
//
//)





//
//
//// beat not hemmed in yet
//Instr([\trails,	\multiFeedRLPF],{arg 
//							audio=0.0, drive=0.4,ffreq=3000,rq=0.5,freqOffset=33,beat=0.25;
//		var qnty=2;
//		var tempor,freqo;
//		tempor = thisSynth.tempo.reciprocal;
//		freqo = LFNoise1.kr(0.3,freqOffset,ffreq);
//		
//		MultiTapDelayProcess.ar(
//			Array.fill(qnty,{arg i; {arg in; RLPF.ar(in,freqo,rq) }}),audio,nil,
//			Array.fill(qnty,{arg i; (i * beat * tempor) }),
//			LFNoise1.kr(0.2,Amplitude.kr(Mono(audio),mul: - 1)),
//			Array.fill(2,{ Signal.newClear(44100 * 1) })
//		)
//
//},[
//	nil,
//	\unipolar,
//	nil,
//	nil,
//	[0,60],
//	[StaticSpec(0.125,0.5,\linear,0.125)]
//]);		
//	
	
	