/*

Udef(\test,{
   UOut.ar( WhiteNoise.ar * SinOsc.ar( \lfo.ukr(10.0, 1.0, 15.0,\lin,0.0,lag:2) ) )
})

UChain(\test, \stereoOutput).gui

Udef( \testTrig, { 
	var in;
	in = UIn.ar(0);
	in = in * Sweep.kr( \trigger.utr ).wrap(0,1);
	UOut.ar( 0, in );
});

UChain( \sine, \testTrig, \stereoOutput ).gui;

(
Udef( \testTrig, {  // now watch the gui
	var in;
	in = UIn.ar(0);
	in = in * Sweep.kr( \trigger.utr( label: "start" ) ).wrap(0,1);
	UOut.ar( 0, in );
});
)

(
Udef( \testTrig, {  // now watch the gui
	var in;
	in = UIn.ar(0);
	in = in * Sweep.kr( \trigger.utr( 0, nil, 0, 2 ) ).wrap(0,1);
	UOut.ar( 0, in );
});
)

TODO:
intelligently sense the size of the default, and switch to ControlSpec, RangeSpec or ArrayControlSpec
*/
+ Symbol {

	ukr{ |default, minval=0.0, maxval=1.0, warp='lin', step=0.0, lag, fixedLag = false|
		var spec = if( minval.respondsTo( \asSpec ) ) {
			minval.asSpec
		} {
			ControlSpec(minval, maxval, warp, step, default)
		};
		case { default.size == 2 } {
			spec = spec.asRangeSpec;
		} { default.size > 2 } {
			spec = spec.asArrayControlSpec;
		};
		Udef.addBuildSpec( ArgSpec(this, default, spec ) );
		^this.kr(default, lag, fixedLag)
	}

	uir { |default, minval=0.0, maxval=1.0, warp='lin', step=0.0, lag, fixedLag = false|
		var spec = if( minval.respondsTo( \asSpec ) ) {
			minval.asSpec
		} {
			ControlSpec(minval, maxval, warp, step, default)
		};
		Udef.addBuildSpec(ArgSpec(this, default, spec )
			.mode_(\init) );
		^this.kr(default, lag, fixedLag)
	}

	utr { |default, label, minval, maxval, warp, step|
		if( [ minval, maxval, warp, step ].every(_.isNil) ) {
			Udef.addBuildSpec( ArgSpec(this, default, TriggerSpec(label)) );
		} {
			Udef.addBuildSpec( ArgSpec(this, default, TriggerSpec(label,
				ControlSpec( minval ? 0, maxval ? 1, warp ? \lin, step ? 0, default ? 0 )
			) ) );
		};
		^this.tr(default)
	}

	ukrRange { |minval=0.0, maxval=1.0, minRange=0, maxRange = inf, warp='lin',
		step=0.0, lag, fixedLag = false|
		Udef.addBuildSpec( ArgSpec(this, [minval, maxval],
			RangeSpec(minval, maxval, minRange, maxRange, warp, step) ) )
		^this.kr( [minval, maxval], lag, fixedLag)
	}

	ukrInt { |default=0, minval = -inf, maxval = inf, lag, fixedLag = false|
		Udef.addBuildSpec( ArgSpec(this, default, IntegerSpec( default, minval, maxval ) ) );
		^this.kr(default, lag, fixedLag)
	}

	ukrArgSpec{ |controlDefault, specDefault,  spec, private, mode, lag, fixedLag = false|
		Udef.addBuildSpec( ArgSpec(this, specDefault, spec, private, mode ) );
		^this.kr(controlDefault, lag, fixedLag)
	}

}