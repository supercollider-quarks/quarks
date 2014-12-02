
/*
	with a Patch you don't need to supply the fftbuff,
	it will be created by default based on the argname.
	
	so simply :
	
	Patch([\pvfft,\PV_MagAbove],[ input, 10.0 ]).play
	
*/
	
var build1;
build1 = { arg func,audio,buffs;
	if(audio.numChannels == 2,{
		Array.fill(2,{ arg i;
			func.value(buffs.at(i),audio.at(i))
		})
	},{
		func.value(buffs.at(0),audio)
	})
};



Instr("pvfft.noop", { arg audio;
	var chain;
	chain = FFT(LocalBuf(2048,1), audio);
	IFFT(chain);
},[
	\audio
],
	\audio
);



Instr("pvfft.PV_MagAbove", { arg audio,threshold=32.0;
	var chain;
	chain = FFT(LocalBuf(2048,1), audio);
	chain = PV_MagAbove(chain, threshold ); 
	IFFT(chain);
},[
	\audio,
	[0.0,128.0]
],
	\audio
);



Instr("pvfft.PV_MagBelow", { arg audio,threshold=1.0;
	var chain;
	chain = FFT(LocalBuf(2048,1), audio);
	chain = PV_MagBelow(chain, threshold ); 
	IFFT(chain);
},[
	\audio,
	[0.0,10.0]
],
	\audio
);



Instr("pvfft.PV_MagClip", { arg audio,threshold=1.0,gain=1.5;
	var chain;
	chain = FFT(LocalBuf(2048,1), audio);
	chain = PV_MagBelow(chain, threshold ); 
	IFFT(chain) * gain;
},[
	\audio,
	[0.0,10.0]
],\audio);


Instr("pvfft.PV_MagSmear", { arg audio,threshold=1.0,gain=1.5;
	var chain;
	chain = FFT(LocalBuf(2048,1), audio);
	chain = PV_MagSmear(chain, threshold ); 
	IFFT(chain) * gain;
},[
	\audio,
	[0.0,10.0]
],
	\audio
);

Instr("pvfft.PV_BinShift", { arg audio,stretch=1.0,shift=0.0;
	var chain;
	chain = FFT(LocalBuf(2048,1), audio);
	chain = PV_BinShift(chain, stretch,shift ); 
	IFFT(chain);
},[
	\audio,
	[0.0,10.0],
	[0.0,1024.0]
],
	\audio
);




Instr("pvfft.PV_MagShift", { arg audio,stretch=1.0,shift=0.0;
	var chain;
	chain = FFT(LocalBuf(2048,1), audio);
	chain = PV_MagShift(chain, stretch,shift ); 
	IFFT(chain);
},[
	\audio,
	[0.0,10.0],
	[0.0,1024.0]
],
	\audio
);



Instr("pvfft.PV_MagSquared", { arg audio;
	var chain;
	chain = FFT(LocalBuf(2048,1), audio);
	chain = PV_MagSquared(chain); 
	IFFT(chain);
},[
	\audio
],
	\audio
);




Instr("pvfft.PV_MagNoise", { arg audio;
	var chain;
	chain = FFT(LocalBuf(2048,1), audio);
	chain = PV_MagNoise(chain); 
	IFFT(chain);
},nil,
	\audio
);



//Instr([\pvfft,\PV_PhaseShift90], { arg audio;
//	var chain;
//	chain = FFT(LocalBuf(2048,1), audio);
//	chain = PV_PhaseShift90(chain); 
//	IFFT(chain);
//},[
//	\audio
//]);
//
//
//Instr([\pvfft,\PV_PhaseShift270], { arg audio;
//	var chain;
//	chain = FFT(LocalBuf(2048,1), audio);
//	chain = PV_PhaseShift270(chain); 
//	IFFT(chain);
//},[
//	\audio
//]);
//
//



Instr("pvfft.PV_BrickWall", { arg audio,wipe=0.0,fftbuffl,fftbuffr;

	build1.value({ arg buff,audio;
		var chain;
		chain = FFT(buff.bufnumIr, audio);
		chain = PV_BrickWall(chain, wipe ); 
		IFFT(chain);
	},audio,[fftbuffl,fftbuffr]);
			
},[
	\audio,
	[0.001,1.0,\exp],
	\fftbuff,
	\fftbuff
],
	\audio
);






Instr("pvfft.PV_BinWipe", { arg audio,audio2,wipe=0.0;
	var chain,chain2;
	var maxc;
	maxc = max(audio.numChannels,audio2.numChannels);
	audio2 = NumChannels.ar(audio2,maxc);
	audio = NumChannels.ar(audio,maxc);
	
	
	chain = FFT(LocalBuf(2048,1), audio);
	chain2 = FFT(LocalBuf(2048,1), audio2 );
	chain = PV_BinWipe(chain,chain2, wipe );
	IFFT(chain);
	
},[
	\audio,
	\audio,
	\linear,
	\fftbuff,
	\fftbuff
],
	\audio
);



Instr("pvfft.PV_MagMul", { arg audio,audio2;
	var chain,chain2;
	var maxc;
	maxc = max(audio.numChannels,audio2.numChannels);
	audio2 = NumChannels.ar(audio2,maxc);
	audio = NumChannels.ar(audio,maxc);

	chain = FFT(LocalBuf(2048,1), audio);
	chain2 = FFT(LocalBuf(2048,1), audio2 );
	chain = PV_MagMul(chain,chain2 );
	IFFT(chain);
},[
	\audio,
	\audio,
	\fftbuff,
	\fftbuff
],
	\audio
);


Instr("pvfft.PV_CopyPhase", { arg audio,audio2;
	var chain,chain2;
	var maxc;
	maxc = max(audio.numChannels,audio2.numChannels);
	audio2 = NumChannels.ar(audio2,maxc);
	audio = NumChannels.ar(audio,maxc);

	chain = FFT(LocalBuf(2048,1), audio);
	chain2 = FFT(LocalBuf(2048,1), audio2 );
	chain = PV_CopyPhase(chain,chain2 );
	IFFT(chain);
},[
	\audio,
	\audio,
	\fftbuff,
	\fftbuff
],
	\audio
);



Instr("pvfft.PV_Max", { arg audio,audio2;
	var chain,chain2;
	var maxc;
	maxc = max(audio.numChannels,audio2.numChannels);
	audio2 = NumChannels.ar(audio2,maxc);
	audio = NumChannels.ar(audio,maxc);

	chain = FFT(LocalBuf(2048,1), audio);
	chain2 = FFT(LocalBuf(2048,1), audio2 );
	chain = PV_Max(chain,chain2 );
	IFFT(chain);
},[
	\audio,
	\audio,
	\fftbuff,
	\fftbuff
],
	\audio
);


Instr("pvfft.PV_Min", { arg audio,audio2;
	var chain,chain2;
	var maxc;
	maxc = max(audio.numChannels,audio2.numChannels);
	audio2 = NumChannels.ar(audio2,maxc);
	audio = NumChannels.ar(audio,maxc);

	chain = FFT(LocalBuf(2048,1), audio);
	chain2 = FFT(LocalBuf(2048,1), audio2 );
	chain = PV_Min(chain,chain2 );
	IFFT(chain);
},[
	\audio,
	\audio,
	\fftbuff,
	\fftbuff
],
	\audio
);



Instr("pvfft.PV_RandComb", { arg audio,wipe=0.0,trig=0.0;
	var chain;

	chain = FFT(LocalBuf(2048,1), audio);
	chain = PV_RandComb(chain, wipe,trig ); 
	IFFT(chain);
},[
	\audio,
	[0.001,1.0,\exp]
],
	\audio
);


Instr("pvfft.PV_RectComb", { arg audio,numTeeth=0.0,phase=0.0,width=0.5;
	var chain;

	chain = FFT(LocalBuf(2048,1), audio);
	chain = PV_RectComb(chain, numTeeth,phase,width ); 
	IFFT(chain);
},[
	\audio,
	[0.0,32.0],
	[0.0,1.0],
	[0.0,1.0]
],
	\audio
);



// left channel issue ??
Instr("pvfft.PV_RectComb2", { arg audio,audio2,numTeeth=0.0,phase=0.0,width=0.5;
	var chain,chain2;
	var maxc;
	maxc = max(audio.numChannels,audio2.numChannels);
	audio2 = NumChannels.ar(audio2,maxc);
	audio = NumChannels.ar(audio,maxc);

	chain = FFT(LocalBuf(2048,1), audio);
	chain2 = FFT(LocalBuf(2048,1), audio2);
	chain = PV_RectComb2(chain, chain2,numTeeth,phase,width ); 
	IFFT(chain);
},[
	\audio,
	\audio,
	[0.0,32.0],
	[0.0,1.0],
	[0.0,1.0],
	\fftbuff,\fftbuff
],
	\audio
);




Instr("pvfft.PV_RandWipe", { arg audio,audio2,wipe=0.0,trig=0.0;
	var chain,chain2;
	var maxc;
	maxc = max(audio.numChannels,audio2.numChannels);
	audio2 = NumChannels.ar(audio2,maxc);
	audio = NumChannels.ar(audio,maxc);

	chain = FFT(LocalBuf(2048,1), audio);
	chain2 = FFT(LocalBuf(2048,1), audio2);
	chain = PV_RandWipe(chain, chain2,wipe,trig); 
	IFFT(chain);
},[
	\audio,
	\audio,
	[0.0,1.0],
	\trig,
	\fftbuff,\fftbuff
],
	\audio
);



Instr("pvfft.PV_Diffuser", { arg audio,trig;
	var chain;
	chain = FFT(LocalBuf(2048,1), audio);
	chain = PV_Diffuser(chain,trig); 
	IFFT(chain);
},[
	\audio
],
	\audio
);



Instr("pvfft.PV_MagFreeze", { arg audio,freeze=0.0,fftbuffl,fftbuffr;

    // this can be done in faster stereo
	build1.value({ arg fftbuff,audio;
		var chain;
		chain = FFT(LocalBuf(2048,1), audio);
		chain = PV_MagFreeze(chain,freeze); 
		IFFT(chain);
	},audio,[fftbuffl,fftbuffr]);
	
},[
	\audio,
	\unipolar,
	\fftbuff,
	\fftbuff
],\audio);


/* ??
Instr("pvfft.PV_MagFreeze2", { arg audio,trig=0.0;
	var chain;
	chain = FFT(LocalBuf(2048,1), audio);
	chain = PV_MagFreeze(chain,Impulse.kr(1,add: -0.5)); 
	IFFT(chain);
},[
	\audio
]);
*/

//Instr("pvfft.PV_BinScramble", { arg audio,wipe=0.0,width=0.2,trig;
//	var chain;
//	chain = FFT(LocalBuf(2048,1), audio);
//	chain = PV_BinScramble(chain,wipe,width,trig); 
//	IFFT(chain);
//},[
//	\audio
//]);


//Instr.leaves([\pvfft]).do({ arg instr;
//	Patch(instr.name,[MonoAudioIn.new]).gui;
//});


