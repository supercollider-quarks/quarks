
/*
	sorry to be completist about this.
	this offers all bin ops as Instr
	with number of channels matching and level protection / balancing

		
*/

// dangerous levels
#[
	'/' ,
	'**' ,
	'difsqr' ,
	'sumsqr' ,
	'sqrsum' ,
	'sqrdif' 
].do({ arg binop;
	
	Instr([\binOps,\ar,binop],{arg audio1=1.0,audio2=1.0;
		var maxc;
		maxc = max(audio1.numChannels,audio2.numChannels);
		audio1 = NumChannels.ar(audio1,maxc);
		audio2 = NumChannels.ar(audio2,maxc);
		Normalizer.ar(
			audio1.perform(binop,audio2)
		)
	},[
		\audio,
		\audio
	],\dualAudioEffect);


	Instr([\binOps,\kr,binop],{ arg a=1.0,b=1.0;
		a.perform(binop,b)
	},[
		\bipolar,
		\bipolar
	],\bipolar);

});


#['+' ,
'-' ,
'*' ,
'%' ,
'min' ,
'max' ,
'ring1', 
'ring2' ,
'ring3' ,
'ring4' ,
'absdif',
'thresh',
'amclip' ,
'scaleneg',
'clip2',
'fold2' ,
'wrap2' ,
'excess' ].do({arg binop;

		Instr([\binOps,\ar,binop],{arg audio1=1.0,audio2=1.0;
			var maxc;
			maxc = max(audio1.numChannels,audio2.numChannels);
			audio1 = NumChannels.ar(audio1,maxc);
			audio2 = NumChannels.ar(audio2,maxc);
			audio1.perform(binop,audio2)
		},[
			\audio,
			\audio
		],\audio);
		
		Instr([\binOps,\kr,binop],{ arg a=1.0,b=1.0;
			a.perform(binop,b)
		},[
			\bipolar,
			\bipolar
		],\bipolar);	
});



Instr("binOps.LinXFade2",{ arg audio1,audio2,mix;
	var maxc;
	maxc = max(audio1.numChannels,audio2.numChannels);
	audio1 = NumChannels.ar(audio1,maxc);
	audio2 = NumChannels.ar(audio2,maxc);
	LinXFade2.ar(audio1,audio2,mix)
},[
	\audio,
	\audio,
	\bipolar
],
	\dualAudioEffect
);



