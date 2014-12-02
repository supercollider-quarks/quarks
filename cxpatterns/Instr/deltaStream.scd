



Instr([\deltaStream,\geomHesitate],{ arg min = -4, size = 9,numEvents=0,slideLen=3,slideBy=2;
	Pslide(
			Array.geom(size,2 ** min,2),CXPatterns.inferCycleLength(numEvents), slideLen, slideBy
		)
},[
	StaticIntegerSpec(-9,2),
	StaticIntegerSpec(3,12),
	StaticIntegerSpec(0,1024),
	StaticIntegerSpec(2,11),
	StaticIntegerSpec(3,11)
],\deltaStream);




Instr([\deltaStream,\geomHesitateAndWander],{ arg min = -4, size = 9,numEvents=0,slideLen=3,slideBy=2;
	var list;
	list = Array.geom(size,2 ** min,2);
	Prand([
		Pslide(
				list,CXPatterns.inferCycleLength(numEvents),slideLen,slideBy
			),
		Pswitch1( list, Pbrown(0,size - 1,1) )
	],inf)
		
},[
	StaticIntegerSpec(-9,2),
	StaticIntegerSpec(3,12),
	StaticIntegerSpec(0,1024),
	StaticIntegerSpec(2,11),
	StaticIntegerSpec(3,11)
],\deltaStream);




/* 
	4 bars of random , but add up even
*/
Instr([\deltaStream,\resetingFixRand],{ arg minDiv = -4, maxDiv = -1, cycleLength = 4,cycles=16,offset=0;
	var cyclesDone = 0,sum=0,list,item;
	while({
		cyclesDone < cycles
	},{
		item = 2 ** rrand(minDiv,maxDiv);
		if(sum + item >= cycleLength,{
			//[sum + item, ">",cycleLength,":",sum,cycleLength - sum].debug;
			item = cycleLength - sum;
			cyclesDone = cyclesDone + 1;
			sum = 0;
		},{
			sum = sum + item;
		});
		list = list.add(item);
	});
	
	Pseq(list,inf,offset)
},[
	StaticIntegerSpec(-9,2),
	StaticIntegerSpec(-9,2),
	StaticIntegerSpec(1,16),
	StaticIntegerSpec(1,16),
	StaticIntegerSpec(0,16)
],\deltaStream);

//PlayerBuilder(\deltaStream,[\deltaStream,\resetingFixRand]).gui



Instr([\deltaStream,\resetingFixLooseRand],{ arg minDiv = -4, maxDiv = -1, cycleLength = 4,cycles=16,offset=0;
	var cyclesDone = 0,sum=0,list,item;
	while({
		cyclesDone < cycles
	},{
		item = 2 ** rrand(minDiv.asFloat,maxDiv.asFloat);
		if(sum + item >= cycleLength,{
			//[item,sum + item, ">",cycleLength,":",sum,cycleLength - sum].debug;
			item = cycleLength - sum;
			cyclesDone = cyclesDone + 1;
			sum = 0;
		},{
			sum = sum + item;
		});
		list = list.add(item);
	});
	
	Pseq(list,inf,offset)
},[
	StaticSpec(-9,2,\linear,0),
	StaticSpec(-9,2,\linear,0),
	StaticIntegerSpec(1,16),
	StaticIntegerSpec(1,16),
	StaticIntegerSpec(0,16)
],\deltaStream);


Instr("deltaStream.swung",{ arg swing;
	Pseq([swing,1 - swing] ,inf)
},[
	StaticSpec(0.1,0.9,default: 0.57)
],
	\deltaStream
);


/*


*/



/*

PlayerMixer([

	Patch({ arg trig;
		SinOsc.ar * EnvGen.kr(Env.perc(0.01,0.1),trig)
	},[
	Stream2Trig( 1,
		PlayerBuilder(\deltaStream,[\deltaStream,\RandStutterXOXfuzzy])
	)
]),
	Patch({ arg tempo;
		Impulse.ar( tempo )
	},[
		TempoPlayer.new
	])
]).gui

*/


