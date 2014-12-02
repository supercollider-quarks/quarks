
TestStreamKrDur : UnitTest {
	
	test_pseqSpec {
		var s,k;
		
		k = StreamKrDur( Pseq([0,1,2,3,4,5],inf), 0.25 );
		s = k.spec;
		this.assertEquals(s.class,ControlSpec,"ControlSpec");
		this.assertEquals( s.minval, 0, "minval 0");
		this.assertEquals( s.maxval, 5, "minval 5");
		this.assertEquals(s.step,0,"step 0");
	}
	test_pseqSpec2 {
		var s,k;
		k = StreamKrDur(
						Pseq([ 750, 500, 500, 500, 750, 500, 500, 500, 750, 500, 500, 500, 750, 500, 500, 500 ],inf), 
						1.0);
		s = k.spec;
		this.assertEquals( s , ControlSpec(500, 750, 'linear', 0.0, 500, ""), "expected spec");
	}

	test_defaultSpec {
		this.assert( StreamKrDur.new.spec.isKindOf(ControlSpec),"should be a control spec by default");
	}

}

