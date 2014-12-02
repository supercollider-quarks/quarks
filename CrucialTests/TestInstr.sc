
TestInstr : UnitTest {
	
	setUp {
		Instr.clearAll
	}
	test_leaves {
		var l;
		l = Instr.leaves;
		this.assertEquals( l.size, 0,"should initially be nothing in leaves");
	}
	
	test_clearAll {
		var l;
		Instr("test_clearAll",{SinOsc.ar});
		Instr.clearAll;
		l = Instr.leaves;
		this.assertEquals( l.size, 0, "should be nothing in leaves after clearAll");
	}

	/*test_noOverwriteSubnode {
		var q,n;
		Instr.clearAll;
		q = Instr("test_noOverwriteSubnode.inner",{SinOsc.ar});
		// should raise an exception
		n = Instr("test_noOverwriteSubnode",{ SinOsc.ar });

		//because there is already a directory named "test"
		//you cannot overwrite that
	}*/
		
	test_putat_dotnotation {
		Instr("TestInstr.test_putat",{ SinOsc.ar });
		
		this.assert( Instr.at("TestInstr.test_putat").notNil, "dot notation should retrieve instr");
		this.assert( Instr.at( ["TestInstr","test_putat"]).notNil,"array notation should retrieve instr");

		this.assert( Instr("TestInstr.test_putat").notNil, "Instr(name) notation should retrieve instr");

	}
	test_putat_array {
		Instr(["TestInstr","test_putat"],{ SinOsc.ar });
		
		this.assert( Instr.at("TestInstr.test_putat").notNil, "dot notation should retrieve instr");
		this.assert( Instr.at( ["TestInstr","test_putat"]).notNil,"array notation should retrieve instr");

		this.assert( Instr("TestInstr.test_putat").notNil, "Instr(name) notation should retrieve instr");
	}
	test_put_at_symbol {
		Instr(\test_put_at_symbol,{SinOsc.ar});
		this.assert( Instr.at(\test_put_at_symbol).notNil, "symbol should retrieve instr");
		this.assert( Instr.at("test_put_at_symbol").notNil, "dot notation should retrieve instr");
		this.assert( Instr.at( ["test_put_at_symbol"]).notNil,"array notation should retrieve instr");
	
		this.assert( Instr(\test_put_at_symbol).notNil, "dot notation should retrieve instr");
	}
	test_defArgAt {
		var d,i;
		Instr(\test_defArgAt,{ arg freq,amp;
				SinOsc.ar(freq,mul: amp)
		});
		d = Instr(\test_defArgAt).defArgAt(0);
		
		// should be nil
		this.assertEquals( d, nil, "no def arg supplied, should be nil");
		
		i = Instr(\test_defArgAt).initAt(0);
		this.assertEquals( i, \freq.asSpec.default,"no def arg supplied, init should be the spec default");
	}
		
	test_asSynthDef {
		var sd;
		Instr("TestInstr.asSynthDef",{ arg freq,amp;
					SinOsc.ar(freq,mul: amp)
		});
		sd = Instr("TestInstr.asSynthDef").asSynthDef;
		this.assert( sd.isKindOf(InstrSynthDef),"should produce an InstrSynthDef succesfully");
	}
	test_doubleStore {
		var leaves;
		Instr("test_doubleStore",{ SinOsc.ar });
		Instr("test_doubleStore",{ SinOsc.ar });
		leaves = Instr.leaves;
		this.assertEquals( leaves.size, 1,"should still only be one leaf");
	}
	
	// strictly speaking this would be a Spec test, but defaultControl is a crucial extension
	test_defaultControl {
		Spec.specs.keysValuesDo({ |k,v|
			var defcon;
			this.assert(v.isKindOf(Spec),  k.asString + v + "is a spec");
			// just testing that it works
			defcon = v.defaultControl;

//			[k,v,defcon].debug;
//			this.assert(v.canAccept( defcon.poll), "spec "+k+v.asCompileString+"should be able to 'accept' its own defaultControl.poll");

		});
	}
	test_pathWasSet {
		// this makes quark dependant on cxaudio
		var instr;
		instr = Instr([\allBands,'threes-gated']);
		this.assert( instr.path.notNil,"path should be set on instr loaded from disk");
	}
	test_loadAll {
		var instr;
		Instr.loadAll;
		instr = Instr([\allBands,'threes-gated']);
		this.assert( instr.path.notNil,"path should be set on instr loaded via Instr.loadAll");
	}
	test_at { // with loading from disk

		// THIS Is because I have a folder called subfolder which is empty in my own dir
		// without it there I get the behavior below : nil the first time

		var i;
		Instr.clearAll;
		i = Instr.at("subfolder.subinstr.one");
		this.assert(i.notNil,"should load subfolder.subinstr.one");

/*		Instr.clearAll;
		i = Instr.at("subfolder.leaf");
		this.assert(i.isNil,"should not find the improperly named subfolder.leaf");
*/
	}
	test_findFileFor {
		var find;
		find = Instr.findFileFor(Instr.symbolizeName("subfolder.subinstr.one"));
		this.assert(find.notNil,"should find the file for subfolder.subinstr.one");
	}
	test_findFileInDir {
		var find,dir;
		// this was a weird bug.
		dir = (Platform.userExtensionDir ++ "/quarks/CrucialTests/Instr/");
		find = Instr.findFileInDir(Instr.symbolizeName("subfolder.subinstr.one"),dir);
		// its in quarks/CrucialTests/Instr/subfolder/subinstr.scd
		this.assert(find.notNil,"should find the file for subfolder.subinstr.one");
	}
}

TestPappliedInstr : UnitTest {

	test_value {
		var i,p,v;
		i = Instr("_test.papplied",{ arg min = 0,max=10;
					blend(min,max,0.5)
				});
		p = i.papply( (max:4) );
		v = p.value(1);
		this.assertEquals( v , blend(1,4,0.5),"valueing the instr with one arg should work")
	}
	
	test_defArgs {
		var i,p,v;
		i = Instr("_test.papplied",{ arg min = 0,max=10;
					blend(min,max,0.5)
				});
		p = i.papply( (max:4) );
		v = p.value;
		this.assertEquals( v , blend(0,4,0.5),"valueing the instr with no args should use the default min arg of 0")
	}

	test_defArgs2 {
		var i,p,v;
		i = Instr("_test.papplied",{ arg ignore=0,min = 0,max=10;
					blend(min,max,0.5)
				});
		p = i.papply( (max:4) );
		v = p.value(1);
		this.assertEquals( v , blend(0,4,0.5),"valueing the instr with less than the full args should use default args for the rest")
	}
}


TestCompositeInstr : UnitTest {
	
	test_first_input {
		var a,b,c,r;
		a = Instr("_test.tci.a",{ arg freq=440; freq.asString });
		b = Instr("_test.tci.b",{ arg input,plus="x"; input ++ plus });
		c = CompositeInstr(a,b);

		r = c.value;
		this.assertEquals(r , "440x","with no args supplied, the default args 440 and x should be concatenated: 440x");
		
		r = c.value(100,"y");
		this.assertEquals( r , "100y","supplying 100 and y, the result should be the string 100y");
	}
	test_second_input {
		var a,b,c,r;
		a = Instr("_test.tci.a",{ arg freq=440; freq.asString });
		b = Instr("_test.tci.b",{ arg input,plus="x"; input ++ plus });
		c = CompositeInstr(a,b,1);
		r = c.value("first","second");
		this.assertEquals( r , "secondfirst","first instr is passed to 2nd input, so string should be secondfirst");
	}
}


TestInterfaceDef : UnitTest {
	var f;
	setUp {
		InterfaceDef.clearAll;
		f = {
			// an environment is in place here
			~freq = KrNumberEditor(400,[100,1200,\exp]);
			~syncFreq = KrNumberEditor(800,[100,12000,\exp]); 
			~amp = KrNumberEditor(0.1,\amp); 

			Patch({ arg freq,syncFreq,amp=0.3;
				SyncSaw.ar(syncFreq,freq) * amp
			},[
				~freq,
				~syncFreq,
				~amp
			])

		};
		
	}
	test_clearAll {
		var l;
		InterfaceDef("test_clearAll",f);
		InterfaceDef.clearAll;
		l = InterfaceDef.leaves;
		this.assertEquals( l.size, 0, "should be nothing in leaves after clearAll");
	}
	
	test_putat_dotnotation {
		InterfaceDef("TestInterfaceDef.test_putat",f);
		
		this.assert( InterfaceDef.at("TestInterfaceDef.test_putat").notNil, "dot notation should retrieve instr");
		this.assert( InterfaceDef.at( ["TestInterfaceDef","test_putat"]).notNil,"array notation should retrieve instr");

		this.assert( InterfaceDef("TestInterfaceDef.test_putat").notNil, "InterfaceDef(name) notation should retrieve instr");

	}
	test_putat_array {
		InterfaceDef(["TestInterfaceDef","test_putat"],f);
		
		this.assert( InterfaceDef.at("TestInterfaceDef.test_putat").notNil, "dot notation should retrieve instr");
		this.assert( InterfaceDef.at( ["TestInterfaceDef","test_putat"]).notNil,"array notation should retrieve instr");

		this.assert( InterfaceDef("TestInterfaceDef.test_putat").notNil, "InterfaceDef(name) notation should retrieve instr");
	}
	test_put_at_symbol {
		InterfaceDef(\id_test_put_at_symbol,f);
		this.assert( InterfaceDef.at(\id_test_put_at_symbol).notNil, "symbol should retrieve instr");
		this.assert( InterfaceDef.at("id_test_put_at_symbol").notNil, "dot notation should retrieve instr");
		this.assert( InterfaceDef.at( ["id_test_put_at_symbol"]).notNil,"array notation should retrieve instr");
	
		this.assert( InterfaceDef(\id_test_put_at_symbol).notNil, "dot notation should retrieve instr");
	}
	test_leaves {
		var leaves;
		InterfaceDef(\id_test_leaves,f);
		// even though we put an Instr too
		Instr(\id_test_leaves_instr,{ SinOsc.ar });
		
		leaves = InterfaceDef.leaves;
		this.assertEquals( leaves.size , 1 , "should be one leaf in the library");
	}

}

