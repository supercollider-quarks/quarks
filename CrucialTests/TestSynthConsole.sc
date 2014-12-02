
TestSynthConsole : UnitTest {
	
	// odd bug 
	test_patch {
		var p,sc;
		
		Instr.clearAll;
		Instr("test_patch",{ SinOsc.ar });
		p = Patch("test_patch");
		
		// this needs to be in a defer
		Sheet({ arg l;
			sc = SynthConsole( p, l );
			sc.tempo;
			0.2.wait;
			// actually there will be another Instr for the TempoGui
		}).close
	}
	
	
}

