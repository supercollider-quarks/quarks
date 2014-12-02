
/*
	 these Instr return Patterns for use in degree inputs of any sort.
	eg. any Pbind that needs an interesting degree input

	*/



Instr([\degreeStream,\leapFrog],{ arg root = 36,numEvents=20,oneSkip=1,twoSkip=2;

	Prand([
			Pseries(root,oneSkip,numEvents),
			Pseries(root - 12,twoSkip,numEvents)
	],inf)

},[
	StaticIntegerSpec(-100,100),
	StaticIntegerSpec(2,100),
	StaticIntegerSpec(-100,100),
	StaticIntegerSpec(-100,100)
],
	\degreeStream
);





Instr([\degreeStream,\endlessLeapFrog],{ arg root = 36,eventCycle=20,oneSkip=1,twoSkip=2;

		Prand([
				Pn( Pseries(root,oneSkip,eventCycle) , inf),
				Pn( Pseries(root - 12,twoSkip,eventCycle) , inf)
			],inf)

},[
	StaticIntegerSpec(-100,100),
	StaticIntegerSpec(2,100),
	StaticIntegerSpec(-100,100),
	StaticIntegerSpec(-100,100)
],
	\degreeStream
);






Instr([\degreeStream,\slidez],{ arg len1=5,step1=2,len2=6,step2=2,numEvents=0,root = 36;

	Pswitch1([
				Pslide([0,1,2,3,4,5,6,7,8,9,10,11] + root,
					inf,len1,step1,0),
				Pslide([1,3,5,7,9,11,13] - 1 + root,
					inf,len2,step2,0)
			],
			Pshuf([0,0,0,1,1,1],CXPatterns.inferCycleLength(numEvents))
		)

},[
	StaticIntegerSpec(2,10),
	StaticIntegerSpec(2,10),
	StaticIntegerSpec(2,10),
	StaticIntegerSpec(2,10),
	StaticIntegerSpec(0,100),
	StaticIntegerSpec(-100,100)
],
	\degreeStream
);



Instr([\degreeStream,\slidezMod],{ arg len1=6,step1=3,len2=7,step2=3,root = 36,stutter=9;

		Pslide([0,1,2,3,4,5,6,7,8,9,10,11] + root,
			inf,len1,step1,0)
		+
		Pstutter(stutter,
			Pslide([1,3,5,7,9,11,13] - 1 + root,
				inf,len2,step2,0)
		)

},[
	StaticIntegerSpec(2,10),
	StaticIntegerSpec(2,10),
	StaticIntegerSpec(2,10),
	StaticIntegerSpec(2,10),
	StaticIntegerSpec(-100,100),
	StaticIntegerSpec(2,16)
],
	\degreeStream
);




Instr([\degreeStream,\soloist],{ arg root = 36,stepsPerMode=2,chordWeight=0.3,largeChordWeight=0.3,downChordWeight=0.3,randIntWeight=0.3,walkWeight=0.3;

	Pwrand([
		Pseq(root + [1,3,5,7,9] - 1,stepsPerMode),
		Pseq(root + [-3,-2,-1,0,1,2,3,4,5,6,7,8,9,10,11] - 1,stepsPerMode),
		Pseq(root + [1,3,5,7,9].reverse - 1,stepsPerMode),
		Pxrand(root + [1,3,5,7,9,11,13] - 1,stepsPerMode),
		Pbrown(root + -12,root + 11,1,stepsPerMode)
	],
		[chordWeight,largeChordWeight,downChordWeight,randIntWeight,walkWeight].normalizeSum,
		inf
	);

},[
	StaticIntegerSpec(-100,100),
	StaticIntegerSpec(2,24),
	StaticSpec(0,1),
	StaticSpec(0,1),
	StaticSpec(0,1),
	StaticSpec(0,1),
	StaticSpec(0,1)
],
	\degreeStream
);

Instr("degreeStream.walker",{
	
	Pswitch1([
		Pseq([1,3,5,7,9] - 1,inf),
		Pseq([-3,-2,-1,0,1,2,3,4,5,6,7,8,9,10,11] - 1,inf),
		Pseq([1,3,5,7,9].reverse - 1,inf),
		Pxrand([1,3,5,7,9,11,13] - 1,inf),
		Pbrown(-12,11,1,inf)
	],
		Pshuf([0,0,0,1,1,1,2,2,2,3,3,3,3,3,3],inf)
	);
},
	nil,
	\degreeStream
);

// more walking, roots
Instr("degreeStream.walker2",{
	Pswitch1([
		Pseq([1,3,5] - 1,inf),
		Pseq([1,3,5].reverse - 1,inf),
		Pxrand([1,3,5,7] - 1,inf),
		Pbrown(-12,11,1,inf)
	],
		Pshuf([0,0,0,1,1,1,2,3,3,3,3,3,3,3],inf)
	);
},
	nil,
	\degreeStream
);



Instr([\degreeStream,\fixArp],{ arg root = 36,stepsPerMode=2,chordWeight=0.3,altChordWeight=0.3,scaleWeight=0.3,downChordWeight=0.3,altPitch=7;

	var list,weights,builder;
	list = [];
	weights = [chordWeight,altChordWeight,scaleWeight,downChordWeight].normalizeSum;
	
	builder = {
		[
			Pseq(root + [1,3,5,7,9] - 1,stepsPerMode),
			Pseq((root + [1,3,5,7,9] - 1 + altPitch).rotate(5.rand),stepsPerMode),
			Pseq((root + [1,2,3,4,5,6,7,8,9,10,11] - 1).rotate(11.rand),stepsPerMode),
			Pseq((root + [1,3,5,7,9].reverse - 1).rotate(5.rand),stepsPerMode)
		].do({ arg seq,i;
			if(weights.at(i).coin,{
				list = list.add(seq);
			});
		});
	};
	while({
		list.size < 2
	},{
		builder.value
	});
	Pseq(list,inf)

},[
	StaticIntegerSpec(-100,100),
	StaticIntegerSpec(2,24),
	StaticSpec(0,1),
	StaticSpec(0,1),
	StaticSpec(0,1),
	StaticSpec(0,1),
	StaticIntegerSpec(-20,20)
],
	\degreeStream
);






Instr([\degreeStream,\fixMel],{ arg root = 36,stepsPerMode=2,
	chordWeight=0.3,altChordWeight=0.3,scaleWeight=0.3,downScaleWeight=0.3,altPitch=7;

	var list,weights,builder,stutter;
	list = [];
	weights = [chordWeight,altChordWeight,scaleWeight,downScaleWeight].normalizeSum;
	
	stutter = { arg list,coin=0.1;
		var nlist;
		nlist = [];
		list.do({ arg l;
			nlist = nlist.add(l);
			if(coin.coin,{nlist = nlist.add(l)});
		});
		list
	};
	builder = {
		[
			[1,3,5,7,9].scramble - 1,
			root + [1,3,5,7,9].scramble - 1 + altPitch,
			(root + [1,2,3,4,5,6,7,8,9,10,11] - 1).rotate(11.rand),
			(root + [1,2,3,4,5,6,7,8,9,10,11].reverse - 1).rotate(11.rand)
		].do({ arg seq,i;
			if(weights.at(i).coin,{
				list = list.add(
						Pseq(stutter.value(seq),stepsPerMode)
					);
			});
		});
	};
	while({
		list.size < 2
	},{
		builder.value
	});
	Pseq(list,inf)

},[
	StaticIntegerSpec(-100,100),
	StaticIntegerSpec(2,24),
	StaticSpec(0,1),
	StaticSpec(0,1),
	StaticSpec(0,1),
	StaticSpec(0,1),
	StaticIntegerSpec(-20,20)
],
	\degreeStream
);





Instr([\degreeStream,\biMelody],{ arg root = 36,eventCycle=20,oneSkip=1,twoSkip=2;

	var seq;
	seq = Pseq(Array.fill(rrand(4,128),{ rrand(0,3) }),inf);
		Pswitch1([
				Pn( Pseries(root,oneSkip,eventCycle) , inf),
				Pn( Pseries(root - 12,twoSkip,eventCycle) , inf),
				Pseq((root + [1,2,3,4,5,6,7,8,9,10,11] - 1).rotate(11.rand),inf),
				Pseq((root + [1,3,5,7,9].reverse - 1).rotate(5.rand),inf)
			],seq)

},[
	StaticIntegerSpec(-100,100),
	StaticIntegerSpec(2,100),
	StaticIntegerSpec(-100,100),
	StaticIntegerSpec(-100,100)
],
	\degreeStream
);





Instr([\degreeStream,\biMelody2],{ arg root = 36,altRoot=12,eventCycle=20,oneSkip=1,twoSkip=2;

	var seq;
	seq = Pseq(Array.fill(rrand(4,128),{ 5.rand }),inf);
		Pswitch1([
				Pn( Pseries(root,oneSkip,eventCycle) , inf),
				Pn( Pseries( altRoot,twoSkip,eventCycle) , inf),
				Pseq((root + [1,2,3,4,5,6,7,8,9,10,11] - 1).rotate(11.rand),inf),
				Pseq((altRoot + [1,2,3,4,5,6,7,8,9,10,11] - 1 ).rotate(11.rand),inf),
				Pseq((root + [1,3,5,7,9].reverse - 1).rotate(5.rand),inf)
			],seq)

},[
	StaticIntegerSpec(-100,100),
	StaticIntegerSpec(-100,100),
	StaticIntegerSpec(2,100),
	StaticIntegerSpec(-100,100),
	StaticIntegerSpec(-100,100)
],
	\degreeStream
);





Instr([\degreeStream,\biMelody3],{ arg root = 36,altRoot=12,seriesCycle=20,oneSkip=1,twoSkip=2,inertia=0.3,numNotes=16;

	var seq,index;
	index = 5.rand;
	seq = Pseq(
			Array.fill(numNotes,{
				if(inertia.coin,{ index = 5.rand });
				index
			})
			,inf);
		
		Pswitch1([
				Pn( Pseries(root,oneSkip,seriesCycle) , inf),
				Pn( Pseries( altRoot,twoSkip,seriesCycle) , inf),
				Pseq((root + [1,2,3,4,5,6,7,8,9,10,11].perform([\scramble,\rotate,\mirror,\pyramid,\mirror1,\mirror2,\reverse].choose) - 1),inf),
				Pseq(((altRoot + [1,2,3,4,5,6,7,8,9,10,11].perform([\scramble,\rotate,\mirror,\pyramid,\mirror1,\mirror2,\reverse].choose) - 1) ).rotate(11.rand),inf),
				Pseq((root + [1,2,3,4,5,6,7,8,9,10,11].perform([\scramble,\rotate,\mirror,\pyramid,\mirror1,\mirror2,\reverse].choose) - 1),inf)
			],seq)

},[
	StaticIntegerSpec(-100,100),
	StaticIntegerSpec(-100,100),
	StaticIntegerSpec(2,100),
	StaticIntegerSpec(-100,100),
	StaticIntegerSpec(-100,100),
	StaticSpec(0,1),
	StaticIntegerSpec(2,1024)
],
	\degreeStream
);




Instr([\degreeStream,\biMelodyDegraded1],{ arg root = 36,altRoot=12,seriesCycle=20,oneSkip=1,twoSkip=2,inertia=0.3,numNotes=16,numPerversions=4;

	var seq,seq2,sequence,index,treatList;
	index = 5.rand;
	seq = 	Array.fill(numNotes,{
				if(inertia.coin,{ index = 5.rand });
				index
			});
	// seq2 has some positions changed
	seq2 = seq.copy;
	if(numPerversions > 0,{
		Array.series(numNotes).scramble.copyRange(0,numPerversions).do({ arg index;
			seq.put(index, 5.rand)
		});
		sequence = Pseq([Pseq(seq,3),Pseq(seq2,1)],inf);
	},{
		sequence = Pseq(seq,inf);
	});
			
	treatList = { arg list;
		list = list.perform([\scramble,\rotate,\mirror,\pyramid,\mirror1,\mirror2,\reverse].choose) - 1;
		list.copyRange(0,seriesCycle);
		list
	};
		Pswitch1([
				Pn( Pseries(root,oneSkip,seriesCycle) , inf),
				Pn( Pseries(altRoot,twoSkip,seriesCycle) , inf),
				Pseq(treatList.value( root + [1,2,3,4,5,6,7,8,9,10,11] ),inf),
				Pseq(treatList.value( altRoot + [1,2,3,4,5,6,7,8,9,10,11] ),inf),
				Pseq(treatList.value( root + [1,2,3,4,5,6,7,8,9,10,11] ),inf)
			],sequence)

},[
	StaticIntegerSpec(-100,100),
	StaticIntegerSpec(-100,100),
	StaticIntegerSpec(2,36),
	StaticIntegerSpec(-12,12),
	StaticIntegerSpec(-12,12),
	StaticSpec(0,1),
	StaticIntegerSpec(2,32),
	StaticIntegerSpec(0,32)
],
	\degreeStream
);



//Instr([\degreeStream,\pseq],{ arg cycleLength = 16;
//
//	PseqLive(cycleLength,-20,20,1,inf)
//
//},[
//	StaticIntegerSpec(2,100)
//],
//	\degreeStream
//);




