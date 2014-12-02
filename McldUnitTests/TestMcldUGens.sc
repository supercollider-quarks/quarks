/*
s.boot;
UnitTest.gui;
TestMcldUGens.run
*/
TestMcldUGens : UnitTest {
	test_avgtrig_1noop{
		var testsIncomplete = 0;
	  	this.bootServer;

		// Test sanity of MedianTriggered / MeanTriggered when span is 1 - should be an identity (no-op) operation
		
		[MeanTriggered, MedianTriggered].do{|avgunit|
			[
			{
				var trig = Impulse.kr(100);
				var son = TRand.kr(-1, 1, trig);
				var mash = avgunit.kr(son, trig, 1);
				[son, mash]
			},
			{
				var trig = Impulse.ar(100);
				var son = TRand.ar(-1, 1, trig);
				var mash = avgunit.ar(son, trig, 1);
				[son, mash]
			}
			].do{|synthfunc, whichfunc|
				testsIncomplete = testsIncomplete + 1;
				synthfunc.loadToFloatArray(1, action: { |array|
					// no-op, the two channels should be equal
					array = array.clump(2).flop;
					this.assert( (array[0] - array[1]).abs.every(_<0.0001) ,"%.%(length:1) should not alter the signal".format(avgunit, #[\kr, \ar][whichfunc]), true, {array.flop.flat.plot(numChannels:2)} );
					testsIncomplete = testsIncomplete - 1;
				});
			};
			
		};
		
		// Wait for async tests
		this.wait{testsIncomplete==0};
	}

	test_avgtrig_sameaslangcalc{
		var testsIncomplete = 0;
	  	this.bootServer;

		// Test that MedianTriggered / MeanTriggered give the same results as calculating the same thing lang-side
		
		[[MedianTriggered, \median], [MeanTriggered, \mean]].do{|avgstuff|
			var avgunit = avgstuff[0], avgcalc = avgstuff[1], rnddata, rndbuf;
			testsIncomplete = testsIncomplete + 2;
			
			rnddata = {exprand(0.1, 10.0)}.dup(1000);
//			rnddata = {1.0.rand}.dup(100).postcs;
//			rnddata = 100.collect{|v| (v*0.01).sin};
			rndbuf = Buffer.loadCollection(Server.default, rnddata);
			0.5.wait;
			Server.default.sync;
//			rndbuf.plot;
			[
				[{
					avgunit.kr(PlayBuf.kr(1, rndbuf), 1, rnddata.size);
				}, \kr, Server.default.options.blockSize]
				,
				[{
					avgunit.ar(PlayBuf.ar(1, rndbuf), 1, rnddata.size);
				}, \ar, 1]
			].do{|stuff|
				stuff[0].loadToFloatArray(rnddata.size * stuff[2] / Server.default.sampleRate, action: { |array|
					this.assertFloatEquals(array.last, rnddata.perform(avgcalc), 
						"average from %.% should match val calculated in language".format(avgunit, stuff[1]));
					testsIncomplete = testsIncomplete - 1;
				});
			};
		};
		
		// Wait for async tests
		this.wait{testsIncomplete==0};
	}

	test_crest {
		var testsIncomplete = 1;
		this.bootServer;
                {
			// oscillator getting louder sf DC offset, therefore crest getting bigger
			Crest.kr(SinOsc.ar(SampleRate.ir * 0.25, 0, Line.ar(0,1,1))+1, 100)
		}.loadToFloatArray(1, Server.default, { |data|
			// Note: first item ignored since won't be for a full block yet, so doesn't fit pattern
                        this.assert(data[1..].differentiate.every(_>0.0), 
				"Crest.kr must increase if sig is getting louder cf DC offset");
			testsIncomplete = testsIncomplete - 1;
                });
                rrand(0.12, 0.35).wait;

		// Wait for async tests
		this.wait{testsIncomplete==0};
	} // test_crest

	test_logger_listtrig {
		var testsIncomplete = 1, d, b, s = this.s;
		this.bootServer;

		// We generate a random list of triggers, and see if it's similar to the list we get back:
		d = {1.0.rand}.dup(10).integrate.normalize * 3 + 0.03;
		b = Buffer.alloc(s, 10);
		{ Logger.kr(Line.kr(0,4,4, doneAction: 2), ListTrig.kr(d.as(LocalBuf)), b)}.play(s);
		3.5.wait;
		b.loadToFloatArray(action: {|data| 
	                this.assertArrayFloatEquals(data, d, 
				"Logger result matches ListTrig input", within: 0.0032);
			testsIncomplete = testsIncomplete - 1;
                });
                rrand(0.12, 0.35).wait;

		// Wait for async tests
		this.wait{testsIncomplete==0};
		b.free;
	} // test_logger_listtrig

	test_insideout {
		var tests = Dictionary[
			"InsideOut.ar applied twice is noop" 
				-> {var son = PinkNoise.ar; (son - InsideOut.ar(InsideOut.ar(son)))},
			"InsideOut.kr applied twice is noop" 
				-> {var son = PinkNoise.kr; (son - InsideOut.kr(InsideOut.kr(son)))},
			"InsideOut.ar(_)+_ sums to 1" 
				-> {var son = PinkNoise.ar; (son + InsideOut.ar(son)).abs - 1},
			"InsideOut.kr(_)+_ sums to 1" 
				-> {var son = PinkNoise.kr; (son + InsideOut.kr(son)).abs - 1},
			];
		var testsIncomplete = tests.size;
		this.bootServer;
		tests.keysValuesDo{|text, func|
			func.loadToFloatArray(0.4, Server.default, { |data|
	                        this.assertArrayFloatEquals(data, 0.0, text);
				testsIncomplete = testsIncomplete - 1;
                	});
                	rrand(0.12, 0.35).wait;
		};

		// Wait for async tests
		this.wait{testsIncomplete==0};
	} // test_insideout
	
	test_planetree_2d {
		var testsIncomplete = 1, s = this.s, d, p, c, t, precalc;
		this.bootServer;
		d = #[
		/* xoff,yoff, xvec,yvec, lisl, lidx, risl, ridx */
			   [0.5,  0.5, -0.5, 2.0,    0,    0],
			   [0.25, 0.5,  1.0, 1.0,    1,    1],
			   [0.5,  0.25, -0.5,-0.5,    1,    1]
		   // Note: this uses power-of-two values to avoid numerical precision issues.
		];
		
		precalc = #[
				[ 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7 ],
				[ 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7 ],
				[ 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7 ],
				[ 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7 ],
				[ 5, 5, 6, 6, 7, 7, 7, 7, 7, 7, 7 ],
				[ 5, 5, 5, 4, 4, 7, 7, 7, 7, 7, 7 ],
				[ 5, 5, 4, 4, 4, 4, 4, 4, 4, 4, 7 ],
				[ 5, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4 ],
				[ 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4 ],
				[ 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4 ],
				[ 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4 ]
			]; // the expected results
		
		p = (0, 0.1 .. 1).collect{|y| (0, 0.1 .. 1).collect{|x| [x,y]}};
		
		// language-side classification:
		c = p.collect{|row| row.collect{|datum| PlaneTree.classify(datum, d) }};
		
		// now if we do the server-side classification, it should match:
		t = Buffer.sendCollection(s, d.flat, d[0].size);
		0.3.wait;
		s.sync;
		{
			var x, y, c;
			x = Phasor.kr(end: 11) * 0.1;
			y = Phasor.kr(rate: 1/11, end: 12).floor * 0.1;
			c = PlaneTree.kr(t, [x, y]);
			c
		}.loadToFloatArray(11 * 11 * s.options.blockSize / s.sampleRate, action:{|data| 
			var srvrc = data.asInt.clump(11);
			srvrc.do(_.postln);
     		this.assertArrayFloatEquals(srvrc, c, "PlaneTree server-side and language-side classifications match");
     		this.assertArrayFloatEquals(srvrc, precalc, "PlaneTree server-side and hard-coded classifications match");
     		this.assertArrayFloatEquals(c, precalc, "PlaneTree language-side and hard-coded classifications match");
			testsIncomplete = testsIncomplete - 1;
		});
		// Wait for async tests
		this.wait{testsIncomplete==0};
	} // end test_planetree2d

} // end class
