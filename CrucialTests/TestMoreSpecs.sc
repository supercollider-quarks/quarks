
TestAudioSpec : UnitTest {
	
	test_specs {
		Instr.clearAll;
		Instr.loadAll;
		Instr.leaves.do({ |instr|
			var f,d,c;
			this.assert( instr.outSpec.notNil,"(" + instr.asCompileString ++ ".path.openDocument ) " + Char.nl + "outSpec is nil.  This is guessable by the patch, but it isn't safe to assume that it is an audio spec" );
		
			if(instr.outSpec.isKindOf(AudioSpec),{
				f = Patch(instr).asSynthDef;
				f.store;
			
				d = SynthDescLib.global.synthDescs.at(f.name.asSymbol);
			
				if(instr.outSpec.isKindOf(AudioEventSpec),{
					//d.hasGate and: 
					//should have a gate and 
					this.assert(d.canFreeSynth,"AudioEventSpec should be able to free the synth in:" + instr);
				},{
					//d.hasGate.not or: 
					//have a gate/not 
					this.assert(d.canFreeSynth.not,"if not specified as an AudioEventSpec the instr should not be  able to free the synth in:" + instr);
				});
				
				//d.outputs
				//d.inputs
			});
			0.001.wait;
		})
	}
	
}
	
TestStaticSpec : UnitTest {
	
	// problems common to StaticSpec, StaticIntegerSpec
	// the constructor is confusing and the spec gets built wrong
	test_specs {
		Spec.specs.keysValuesDo({ |an,spec|
			// mostly default shouldnt be minval, but not always
			//if(spec.respondsTo(\minval),{
			//	this.assert(spec.minval != spec.default,"default should not be set to minval " + an + spec);
			//});
			if(spec.isKindOf(StaticIntegerSpec),{
				this.assert(spec.default.isNumber,"default should be a number" + an + spec);
			});
		});
	}
}


TestStreamSpec : UnitTest{
	
	test_specs {
		Instr.leaves.do({ |instr|
			var f,d,e,c,maxval,minval,mean,did;
			//this.assert( instr.outSpec.notNil,"(" + instr.asCompileString ++ ".path.openDocument ) " + Char.nl + "outSpec is nil.  This is guessable by the patch, but it isn't safe to assume that it is an audio spec" );
		
			if(instr.outSpec.isKindOf(StreamSpec),{
				c = instr.outSpec.itemSpec;
				f = Patch(instr).value;
				
				if(instr.outSpec.isKindOf(EventStreamSpec),{
					this.assert( f.asStream.next(Event.default).isKindOf(Event), instr.asString + "should produce a stream of events" + c + c.findKey)
				},{
					e = f.asStream;
					d = Array.fill(100,{e.next}).reject(IsNil);
					if(c.isKindOf(ControlSpec),{
						minval = d.minItem;
						maxval = d.maxItem;
						mean = d.mean;
						//d.do({ |g| g.postln });
						this.assert( did = d.every({|val| c.canAccept(val) }),format("%: all items in the stream should be acceptable to the itemSpec:% % minval:% maxval:% mean:%",instr,c,c.findKey,minval,maxval,mean));

						// would like to check the mean, stdDev, min, max
						//this.assert( d.mean ,format("%: all items in the stream should be acceptable to the itemSpec:% % minval:% maxval:% mean:%",instr,c,c.findKey,minval,maxval,mean));
						
					},{
						this.assert( did = d.every({|val| c.canAccept(val) }),instr.asString + ": all items in the stream should be acceptable to the itemSpec:" + c + c.findKey);						
					});
				})
			});
			0.001.wait;
		})
	}
	
}