//Batuhan Bozkurt 2009
GAWorkbench
{
	
	//probabilities
	var <>mutationProb, <>coProb;
	
	//feats
	var <>randomChromosomeFunc, <>fitnessFunc, <>mutationFunc, <poolSize, chromosomeSize;
	
	//internal state
	var <>genePool, <fitnessScores;
	
	//pluggable crossover function
	var <>userCrossover, <>externalCrossover;
	
	*new
	{|argPoolSize, argRandomChromosomeFunc, argFitnessFunc, argMutationFunc|
	
		^super.new.init(argPoolSize, argRandomChromosomeFunc, argFitnessFunc, argMutationFunc);
	}
	
	init
	{|argPoolSize, argRandomChromosomeFunc, argFitnessFunc, argMutationFunc|
		
		poolSize = argPoolSize ? 100;
		
		randomChromosomeFunc = argRandomChromosomeFunc ? { { 100.0.rand; } ! 100; };
		fitnessFunc = argFitnessFunc ? {|chromosome| chromosome.sum / 100; };
		mutationFunc = 
			argMutationFunc ? 
				{|chromosome| 
					
					chromosome[(chromosome.size - 1).rand] = 100.0.rand; 
					chromosome; 
				};
		
		mutationProb = 0.08;
		coProb = 0.5;
		
		externalCrossover = false;
		
		genePool = List.new;
		fitnessScores = nil ! poolSize;		
		
		this.initGenePool;
		this.rateFitness;
		
	}
	
	initGenePool
	{
		poolSize.do
		({
			genePool.add(randomChromosomeFunc.value);
		});
		
		chromosomeSize = genePool[0].size;
	}
	
	rateFitness
	{
		var tempOrder;
		
		poolSize.do
		({|cnt|
			
			fitnessScores[cnt] = fitnessFunc.value(genePool[cnt]);
		});
		
		tempOrder = fitnessScores.order;
		fitnessScores = fitnessScores[tempOrder].reverse;
		genePool = genePool[tempOrder].reverse;
		
	}
	
	crossover
	{
		if(externalCrossover,
		{
			genePool = userCrossover.value
				(
					genePool, 
					mutationProb				
				);
		},
		{
			//fitnessScores[0].reciprocal.postln;
			this.internalCrossover;
		});
	}
	
	internalCrossover
	{
		var tempGenePool = List.new;
		var tempChromosome, splitPoint;
		var tp1, tp2, tour, tempParent1, tempParent2;
		var offspring1, offspring2;
		var tourPool = (0 .. (poolSize - 1)).asList;
		
		while({ tempGenePool.size < poolSize; },
		{		
			tp1 = tourPool.choose;
			tourPool.remove(tp1);
			tp2 = tourPool.choose;
			tourPool.remove(tp2);
						
			tour = fitnessScores[[tp1, tp2]];
			
			if(tour[0] > tour[1], { tempParent1 = genePool[tp1]; }, { tempParent1 = genePool[tp2]; });
			
			tp1 = tourPool.choose;
			tourPool.remove(tp1);
			tp2 = tourPool.choose;
			tourPool.remove(tp2);
			
			tour = fitnessScores[[tp1, tp2]];
			
			if(tour[0] > tour[1], { tempParent2 = genePool[tp1]; }, { tempParent2 = genePool[tp2]; });
			
			#offspring1, offspring2 = this.mateParents(tempParent1, tempParent2);
			
			if(tempGenePool.size < poolSize, { tempGenePool.add(offspring1); });
			if(tempGenePool.size < poolSize, { tempGenePool.add(offspring2); });
			
			if(tourPool.size == 0, { tourPool = (0 .. (poolSize - 1)).asList; });
		
		});
		//"co finished".postln;
		//("pool is:"++tourPool.asCompileString).postln;
		genePool = tempGenePool;
		//fitnessScores.reciprocal.asCompileString.postln;
		fitnessScores = nil ! poolSize;
			
	}
	
	mateParents
	{|p1, p2|
	
		var relief = { 2.rand; } ! chromosomeSize;
		var offspring1 = List.new, offspring2 = List.new;
		
		if(coProb.coin,
		{
			relief.do
			({|bump, cnt|
				
				if(bump == 1,
				{
					offspring1.add(p1[cnt]);
					offspring2.add(p2[cnt]);
				},
				{
					offspring1.add(p2[cnt]);
					offspring2.add(p1[cnt]);
				});
			});
			
			if(mutationProb.coin, { offspring1 = mutationFunc.value(offspring1); });
			if(mutationProb.coin, { offspring2 = mutationFunc.value(offspring2); });
			
			^[offspring1.asArray, offspring2.asArray];		},
		{
			if(mutationProb.coin, { p1 = mutationFunc.value(p1); });
			if(mutationProb.coin, { p2 = mutationFunc.value(p2); });
			^[p1, p2];
		});
		
		
		
	}
	
	injectFitness
	{|argFitness|
	
		var tempOrder;
		
		if(argFitness.size != poolSize,
		{
			"poolSize is % but supplied fitness array has a size of %. Can't use these values.".format(poolSize, argFitness.size).error;
		},
		{
			fitnessScores = argFitness;
			tempOrder = fitnessScores.order;
			fitnessScores = fitnessScores[tempOrder].reverse;
			genePool = genePool[tempOrder].reverse;
			
		});
	}

}

