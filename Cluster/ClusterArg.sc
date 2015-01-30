ClusterArg : ClusterBasic{

	oclass{ ^ClusterArg }

/*	doesNotUnderstand{ arg selector...args;
		("ClusterArg is dumb, it doesn't understand "++selector)
		^this.prExpandCollect(selector,args)

	}*/

    storeArgs {
        ^[items]
    }

}