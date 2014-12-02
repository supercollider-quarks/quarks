

TestNumberEditor : UnitTest {
	var w;

	setUp {
		w = Window.new;
	}
	tearDown {
		if(w.notNil,{
			w.close
		})
	}
	prDoSizeTest { arg rect;
		var g;
		Environment.use({
			(Quarks.local.path +/+ Quark.find("ServerTools").path +/+ "scripts" +/+ "guiDebugger.scd").loadPath;
		
			g = NumberEditor.new.gui(w,rect);
			this.assert( ~childrenExceedingParents.value(g.view).size == 0, "child should not exceed parent" );
		})
	}
	test_sizing_1 {
		this.prDoSizeTest(160@17);
	}
}

TestKrNumberEditor : UnitTest {

	test_canAccept {
		var k,c,s;
		k = KrNumberEditor(6.0,ControlSpec(0, 11, 'linear', 1, 6, ""));

		c = ControlSpec(0, 11, 'linear', 1, 6, "");

		this.assert( c.canAccept(k), "ControlSpec should canAccept KrNumberEditor");
		
		// in this case, yes.  normally you stream NumberEditors, not KrNumberEditors
		// but an InstrSpawner can accept streams of KrNumEd
		s = StreamSpec(ControlSpec(0, 11, 'linear', 1, 6, ""));

		this.assert( s.canAccept(k), "StreamSpec should canAccept KrNumberEditor");
		// and please let us know about the cheeseburger...
	}
	
	test_spec {
		var k;
		k = KrNumberEditor(440,\freq);
		this.assert(k.spec.isKindOf(ControlSpec),"is control spec");
	}
}




