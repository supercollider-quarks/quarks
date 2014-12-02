TestKMeansRT : UnitTest {

	dataFromLanguageTest { |d, numclust, senddatafunc, analysebufferfunc|
		// Simple factory method to poke a KMeansRT with values from the language, and check what the buffer looks like afterwards.
		var s=Server.default, thebuf, thesynth, c, subtots;
		thebuf = Buffer.alloc(s, numclust, d+1);
		0.1.wait;
		s.sync;
		thesynth = Synth("testmcldugens_kmeansrt%d".format(d), ['buf', thebuf, 'k', numclust]);
		0.1.wait;
		s.sync;
		senddatafunc.value(thesynth);
		0.1.wait;
		s.sync;
		thebuf.loadToFloatArray(action: { |data|
			data = data.clump(d+1);
			// equiv to "c = k.centroids":
			c = data.collect{|frame| frame[..d-1]};
			subtots = data.collect{|frame| frame.last};

			analysebufferfunc.value(c, subtots);
	
			thebuf.free;
			thesynth.free;
		});
	}

	test_kmeansrt {
		var k, p, c, range, rescaled, testsIncomplete=0;
		this.bootServer;
		SynthDef('testmcldugens_kmeansrt1d', { |buf=0, input=0,          k=5, t_gate=0, t_reset=0, learn=1|
			KMeansRT.kr(buf, input, k, t_gate, t_reset)
		}).add;
		SynthDef('testmcldugens_kmeansrt2d', { |buf=0, input=#[0,0],     k=5, t_gate=0, t_reset=0, learn=1|
			KMeansRT.kr(buf, input, k, t_gate, t_reset)
		}).add;
		SynthDef('testmcldugens_kmeansrt3d', { |buf=0, input=#[0,0,0],   k=5, t_gate=0, t_reset=0, learn=1|
			KMeansRT.kr(buf, input, k, t_gate, t_reset)
		}).add;
		SynthDef('testmcldugens_kmeansrt4d', { |buf=0, input=#[0,0,0,0], k=5, t_gate=0, t_reset=0, learn=1|
			KMeansRT.kr(buf, input, k, t_gate, t_reset)
		}).add;

		[1,2,3,4].do{|d|
			var thebuf, thesynth;

			testsIncomplete = testsIncomplete + 1;
			this.dataFromLanguageTest(d, 2, { |thesynth|
				p = [{-2}.dup(d), {2}.dup(d)];
				"Sending fuzzybinary data to KMeansRT (takes about 10 secs)...".postln;
				1000.do{
					thesynth.set('t_gate', 1, 'input', p.choose + {0.5.sum3rand}.dup(d));
					0.01.wait;
				};
			}, { |c, subtots|
				c.sortBy(0);
				c = (p-c).flat.abs;
				this.assertArrayFloatEquals(c, 0.0, 
					"clustering %D fuzzybinary data should approximately recover the true centroids".format(d), 0.1);
				testsIncomplete = testsIncomplete - 1;
			});

			[1,2,3,4].do{|numclust|
				testsIncomplete = testsIncomplete + 1;
				this.dataFromLanguageTest(d, numclust, { |thesynth|
					p = {100.0.rand}.dup(d);
					100.do{
						thesynth.set('t_gate', 1, 'input', p);
						0.01.wait;
					};
				}, { |c, subtots|
					this.assertArrayFloatEquals(c.flat, p.dup(c.size).flat, 
						"clustering constant data should give centroids at the constant value  (%D, k=%)".format(d, numclust));
					testsIncomplete = testsIncomplete - 1;
				});
				//////
				testsIncomplete = testsIncomplete + 1;
				this.dataFromLanguageTest(d, numclust, { |thesynth|
					range = {{100.0.rand}.dup.integrate}.dup(d).flop;
					100.do{
						thesynth.set('t_gate', 1, 'input', d.collect{|whichd| 1.0.rand.linlin(0,1,range[0][whichd], range[1][whichd])});
						0.01.wait;
					};
				}, { |c, subtots|
					rescaled = c.collect{ |acent| (acent - range[0]) / (range[1] - range[0]) }.flat;
					this.assert(rescaled.every{|val| (val>=0) and: {val<=1}},
						"clustering range-limited data should give centroids within that range (%D, k=%)".format(d, numclust));
					testsIncomplete = testsIncomplete - 1;
				});
			};
		};
		
		// Wait for async tests
		this.wait{testsIncomplete==0};
	} // end test_kmeansrt

	test_kmeansrt_nolearn {
		var d=5, numclust=7, origdata, buf, s=Server.default, thesynth, testsIncomplete=1;
		this.bootServer;

		origdata = {100.0.rand}.dup(numclust * (d+1));
		buf = Buffer.loadCollection(s, origdata, d+1);
		0.2.wait;
		s.sync;

		thesynth = {
			Line.kr(0,0,1,doneAction:2);
			KMeansRT.kr(buf, {PinkNoise.kr}.dup(d), numclust, gate:1, reset:0, learn:0);
		}.play(s);
		0.2.wait;
		s.sync;
		1.0.wait;
		
		buf.loadToFloatArray(action: {|data|
			this.assertArrayFloatEquals(data, origdata,
				"data buffer should be unaffected by use of KMeansRT.kr(learn=0)");
			testsIncomplete = testsIncomplete - 1;
		});
		
		// Wait for async tests
		this.wait{testsIncomplete==0};
	} // end test_kmeansrt_nolearn

} // end class TestKMeansRT

