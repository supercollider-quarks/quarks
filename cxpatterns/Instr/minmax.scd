
/*
	numeric patterns that play within min and max
*/
	
Instr([\minmax,\wrapgeom],{ arg grow=1.05,length=0,min=0.0,max=1.0;
	# min, max = [min,max].sort;
	// gets chaotic
	Pwrap(
		Pgeom(if(grow > 1,{min},{max}),grow,length.infer),
		min,
		max
	)
},[
	StaticSpec(0.01,2.0)
],
	\stream
);



//Instr([\minmax,\threshedgeom],{ arg grow=0.9,thresh=0.05,min=0.0,max=1.0;
//
//	var start,end;
//	start = if(grow > 1,{min},{max});
//	end = if(grow > 1,{max},{min});
//	thresh = max - min * thresh;
//	if(start == 0.0,{ start = if(grow > 1, thresh,thresh.neg); });
//	PThreshedGeom(start,grow,end)
//
//},[
//	StaticSpec(0.01,2.0)
//],
//	\stream
//);




// reset every so often
Instr([\minmax,\wrapResetGeom],{ arg grow=1.05,cycleLength=96,length=0,min=0.0,max=1.0;
	# min, max = [min,max].sort;

	Pn(
		Pwrap(
			Pgeom(if(grow > 1,{min},{max}),grow,cycleLength),
			min,
			max
		),
		length.infer
	);
	
},[
	StaticSpec(0.01,2.0),
	StaticIntegerSpec(4,1024)
],
	\stream
);


Instr([\minmax,\Psin],{ arg cycleLength=128,length=0,iPhase=0,min=0.0,max=1.0;
	# min, max = [min,max].sort;
	Psin(min,max,cycleLength,length.infer,iPhase)
},[
	nil,nil,StaticIntegerSpec(0,1024)
],
	\stream
);


Instr([\minmax,\Pwhite],{ arg length=0,min=0.0,max=1.0;
	# min, max = [min,max].sort;

	Pwhite(min,max,length.infer)

},nil,\stream);


Instr([\minmax,\Pbrown],{ arg stepsize=0.1,length=0,min=0.0,max=1.0;
	
	# min, max = [min,max].sort;

	stepsize = (max - min) * stepsize;
	Pbrown(min,max,stepsize,length.infer);
},nil,\stream);


Instr([\minmax,\twosin],{ arg cycleLength=32,cycleLength2=36,min=0.0,max=1.0;
	# min, max = [min,max].sort;
	Pwrap(
		Psin(min,max,cycleLength) + Psin(min,max,cycleLength2),
		min,
		max
	)
},[
	\cycleLength,
	\cycleLength
],\stream);



Instr([\minmax,\twosin2],{ arg cycleLength=32,cycleLength2=36,min=0.0,max=1.0; 
	# min, max = [min,max].sort;
	max = max * 0.5;
	Psin(min,max,cycleLength) + Psin(min,max,cycleLength2)
},[
	\cycleLength,
	\cycleLength
],\stream);


Instr([\minmax,\switchSin],{ arg cycleLength=32,cycleLength2=36,min=0.0,max=1.0; 
	# min, max = [min,max].sort;
	
	Pswitch1(
		[Psin(min,max,cycleLength) ,
		 Psin(min,max,cycleLength2)],
		 Prand([0,1],inf)
	)
},[
	\cycleLength,
	\cycleLength
],\stream);

 // why not ?
Instr([\minmax,\NumberEditor],{ arg min=0.0,max=1.0;
	# min, max = [min,max].sort;
	NumberEditor(rrand(min,max),[min,max])
},nil,\stream);


Instr([\minmax,\PseqLive],{ arg cycleLength=16,min=0.0,max=1.0;
	# min, max = [min,max].sort;
	PseqLive(cycleLength,min,max)
},nil,\stream);


// as a kr device
//Instr([\minmax,\PseqLive],{ arg cycleLength=16,min=0.0,max=1.0;
//	PseqLive(cycleLength,min,max)
//},nil,\player);


Instr([\minmax,\PseqLiveSwitch],{ arg cycleLength=8,numSeqs=4,switchCycleLength=8,min=0.0,max=1.0;
	# min, max = [min,max].sort;

	Pswitch(
		Array.fill(numSeqs,{ 		PseqLive(cycleLength,min,max,0,1) }),
		PseqLive(switchCycleLength,0,numSeqs - 1,1)
	);
},[
	\cycleLength,
	StaticIntegerSpec(2,8),
	\cycleLength
],
	\stream
);


	
	
/*  needs PFork
// switch to change
Instr([\minmax,\PForkInertia],{ arg cycleLength=8,cycleLength2=8,min=0.0,max=1.0;

	PFork(nil,
		PseqLive(cycleLength,min,max),
		PseqLive(cycleLength2,min,max),
		NumberEditor(1.0.rand,\linear)
	)
},[
	\cycleLength,
	\cycleLength
],
	\stream
);

Instr([\minmax,\PForkInertiaStream],{ arg cycleLength=8,cycleLength2=8,inertiaCycleLength=4,min=0.0,max=1.0;

	PFork(nil,
		PseqLive(cycleLength,min,max),
		PseqLive(cycleLength2,min,max),
		PseqLive(inertiaCycleLength,0,1)
	)
},[
	\cycleLength,
	\cycleLength,
	\cycleLength
],
	\stream
);

*/




