+ String{

	/// returns a dictionary with stats of the named process
	getStats{
		var stats;
		stats = ("ps -C \""++this++"\" u").unixCmdGetStdOut;
		//		stats.postcs;
		// some reformatting:
		stats = stats.split($\n).collect{ |it| it.split($ ) }.do{ |it| it.removeAllSuchThat( { |it2| it2==""}); };
		stats[0].do{ |it,i| stats[0][i] = it.replace( "%", "" ).asSymbol; };
		stats[1] = stats[1].copyFromStart( stats[0].size-1 );
		stats = stats.copyFromStart( 1 );
		stats = stats.flop.flatten;
		stats = Dictionary.new.putPairs( stats );
		[\CPU,\MEM].do{ |jt|
			stats[jt] = stats[jt].asFloat / 100;
		};
		[\RSS,\VSZ,\PID].do{ |jt|
			stats[jt] = stats[jt].asInteger;
		};
		^stats;
	}

}