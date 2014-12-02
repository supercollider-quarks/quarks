TestSETOServer : UnitTest {
/*
	setUp {
		// will be called before each test
	}
	tearDown {
	 	// will be called after each test
	 }
*/	 
	test_formatParsing {
/*
	  	this.assert( 6 == 6, "6 should equal 6");
	  	this.assertEquals( 9, 9 ,"9 should equal 9");
	  	this.assertFloatEquals( 4.0 , 1.0 * 4.0 / 4.0 * 4.0, "floating point math should be close to equal");
*/	
		var name, format;
		var ids = [
			['2Dobj'  ,"ixyaXYAmr"],
			['3Dobj'  ,"ixyzabcXYZABCmr"],
			['25Dobj' ,"ixyzabcXYZABCmr"],
			['2Dcurs' ,"ixyXYm"],
			['3Dcurs' ,"ixyzXYZm"],
			['25Dcurs',"ixyzabcXYZABCmr"],
			['tDObj'  ,"ixya"],
			['tDobj'  ,"ixya"], 
			['tdObj'  ,"ixya"],
			["tdObj"  ,"ixya"],
			['_ixya'  ,"ixya"],
			["_ixya"  ,"ixya"],
			['_ixyzabcXYZABC'  ,"ixyzabcXYZABC"]
		];

		ids.do{|vals|
		
			#name, format = vals;
			this.assertEquals(SETOServer(name).realFormat, format, "RealFormat")
		}
	}
	test_setAlive {
		var server;
		server = SETOServer("_ixyzuvw", SETObject);
		
		server.set(0, [3, 0.3, 0.7, 0.5, 0.2, 0.4, 0.1]);
		server.alive([0]);
		this.assertEquals(server.visibleObjs.size, 1, "Visibility 1");
		this.assertEquals(server.visibleObjs.first.asArray, [
			0, 3, 
			0.3, 0.7, 0.5, // pos
			0.2, 0.4, 0.1, // rotAxis
			nil, nil, nil, // rotEuler
			nil, nil, nil, nil, nil, nil, // velocity
			nil, nil			 // acceleration
						 // freeSpace
		], "Proof variable assignment.");
		
		
		server.set(1, [1, 0.41, 0.72, 0.5, 0.2, 0.4, 0.1]);
		server.alive([0,1]);
		this.assertEquals(server.visibleObjs.size, 2, "Visibility 2");

		server.set(0, [0, 0.41, 0.72, 0.5, 0.2, 0.4, 0.1]);
		server.allAlive([0,1]);
		this.assertEquals(server.visibleObjs.size, 2, "Visibility 3");
		
		server.alive([]);
		this.assertEquals(server.visibleObjs.size, 0, "Visibility Remove");
	}
	
	test_SETOmeta {
		var server;
		SETOmeta.setoClasses = [SETOdump, SETObject];
		server = SETOServer("_ixyzuvw", SETOmeta);
		
		server.set(0, [0, 0.3, 0.7, 0.5, 0.2, 0.4, 0.1]);
		server.set(1, [1, 0.41, 0.72, 0.5, 0.2, 0.4, 0.1]);
		server.alive([0,1]);
		this.assertEquals(server.visibleObjs.size, 2, "Visibility Basic");
		this.assertEquals(server.visibleObjs.collectAs({|obj| obj.class}, Set), SETOmeta.setoClasses.asSet, "Class Equality");

		server.set(0, [1, 0.41, 0.72, 0.5, 0.2, 0.4, 0.1]);
		server.allAlive([0,1]);
		this.assertEquals(server.visibleObjs.collectAs({|obj| obj.class}, Set), Set[SETObject], "Class Equality Change");
	}
}

/*

// create a TUIObject, and use it to store messages coming from an [OSCReceiver]
t = SETObject('2DObj', 4711);

r = OSCReceiver('/tuio/2Dobj', nil);
//r = OSCReceiver('/tuio/2Dobj', NetAddr("127.0.0.1", 57120));
r.start;
OSCReceiverFunction(r, \set, {|sessionID, i, x, y, a, dX, dY, dA, m, r| 
	[sessionID, i, x, y, a, dX, dY, dA, m, r].postln;
	// t.id 		= sessionID; // not possible; id only settable at instantiation.
	t.classID 	= i;
	t.pos 		= [x,y,a];
	t.velocity 	= [dX, dY, dA];
	t.acceleration = [m,r];
});

// send some update messages
a = NetAddr("127.0.0.1", 57120);
a.sendMsg('/tuio/2Dobj', \set, 4711, 42, 0, 1, 2, 3, 4, 5, 6, 7);

(
(
	("ID:\t"+t.id)			+ "\n" ++
	("Class:\t"+t.classID)		+ "\n" ++
	("Position:\t"+t.pos) 		+ "\n" ++
	("Velocity:\t"+t.velocity) 	+ "\n" ++
	("Acceleration:\t"			+t.acceleration)
).postln;
)

*/