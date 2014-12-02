
TestBeatSched : UnitTest {
	
	test_beat {
		var b,t, sched;
		Tempo.bpm_(120);
		sched = BeatSched.new;
		b = sched.beat;
		t = sched.time;
		this.assertFloatEquals( Tempo.secs2beats( t ) ,   b,
		 	"BeatSched .beat should be convertible to .time using Tempo");

	}
	test_setBeat {
		var b,t, sched;
		Tempo.bpm_(120);
		sched = BeatSched.new;
		sched.beat = 10.0;
		// this calculates back through the tempo clock
		b = sched.beat;
		
		this.assertFloatEquals( b , 10.0,
		 	"setting beat should work");

	}
		
	test_deltaTillNext {
				var db,b,ds;
		//Tempo.bpm_(120);
		b = BeatSched.beat;
		db = BeatSched.deltaTillNext(4.0);
		// ds = Tempo.beats2secs(db);
		//[b,db,b + db,b + db % 4.0].postln;
		this.assertFloatEquals( (b + db) % 4.0 , 4.0, 
			"with a fresh BeatSched (at beat 0), delta till next beat with quanitzation of 4.0 should be 4.0 beats");
	}
}