

AudioSpec : Spec {

	var <>numChannels;

	*new { arg numChannels;
		^super.new.numChannels_(numChannels)
	}
	*initClass {
		var mono,stereo,either;
		either = AudioSpec(nil);
		mono=AudioSpec(1);
		stereo=AudioSpec(2);
		specs.addAll(
		 [
			\audio -> either,
			\audio1 -> either,
			\audio2 -> either,
			\stereo -> stereo,
			\mono -> mono
			];
		)
	}
	defaultControl {
		if(numChannels.isNil,{
			// the purpose is to occupy an input
			// so a fixed numChannels is needed
			^PlayerInputProxy.new(\mono.asSpec)
		},{
			^PlayerInputProxy.new(this)
		});
	}
	storeArgs { ^[numChannels] }
	rate { ^\audio }
	canAccept { arg thing;
		^(thing.isKindOf(AbstractPlayer) and: { thing.spec == this })
	}
	constrain { arg ... args;
		Error("AudioSpec cannot constrain").throw;
	}
}


// an array of mono/stereo signals
MultiTrackAudioSpec : AudioSpec {
	
	var <>tracks;
	
	*new { arg tracks=2,numChannels=2;
		^super.new(numChannels).tracks_(tracks)
	}
	storeArgs { ^[tracks,numChannels] }
}


BFormatSpec : AudioSpec {
	
	// w, x, y, z
	
	*new {
		^super.new.numChannels_(4)
	}
	storeArgs { ^[] }
	*initClass {
		specs.addAll(
		 [
			\bformat -> this.new
			];
		)
	}
	/*constrain { arg value;
		^value
	}*/
}


// has a gate. generally short duration.  for Pbind, InstrGateSpawner etc.
// this specifies that the output is audio and that the synth can be expected
// to end via an internal envelope
AudioEventSpec : AudioSpec {

	*initClass {
		var a;
		specs.addAll(
		 [
			\audioEvent -> a = AudioEventSpec.new,
			\audioGateEvent -> a
			];
		)
	}
	default{
		^Event.default
	}
}


// this specifies that the output is audio and it has an input which is also audio
EffectSpec : AudioSpec {
	
	var <>numInputs;
	
	*new { arg numChannels=2,numInputs=1;
		^super.new(numChannels).numInputs_(numInputs)
	}

	*initClass {
		specs.addAll(
		 [
			\monoEffect -> EffectSpec(1),
			\stereoEffect -> EffectSpec(2),
			\audioEffect -> EffectSpec(2), // tho it could be multi chans
			\dualAudioEffect -> EffectSpec(2,2),
			\dualStereoEffect -> EffectSpec(2,2)
			];
		)
	}
}

// specifies an FFT chain output or for an input that takes an FFT chain
FFTSpec : Spec {

	var <>bufSize;

	*new { arg bufSize=2048;	
		^super.new.bufSize_(bufSize)
	}
	*initClass {
		specs.addAll(
		[
			\chain -> FFTSpec.new,
			\fft -> FFTSpec.new
		])
	}	
	defaultControl {
		^Patch(Instr("FFTblank",{FFT(LocalBuf(2048),Silent.ar)},[],\fft))
	}
}


TrigSpec : ControlSpec {

	defaultControl { ^BeatClockPlayer.new(4.0) }
	*initClass {
		specs.addAll(
		 [
			\trig -> this.new,
			\gate -> this.new
			];
		)
	}
	canAccept { arg thing;
		// could accept 0 or 1 but useless
		^(thing.isKindOf(AbstractPlayer) and: { thing.spec == this })
	}
}


DemandSpec : ControlSpec {
	
	// defaultControl { Dwhite(this.minval,this.maxval,this.step,inf) }
	rate { ^\demand }
}


TempoSpec : Spec {
	defaultControl { ^TempoPlayer.new }
	rate { ^\control }
	*initClass {
		specs.addAll(
		 [
			\tempo -> this.new
			];
		)
	}
	canAccept { arg thing;
		^thing.isKindOf(TempoPlayer) or: {thing.isFloat}
	}
}


NoLagControlSpec : ControlSpec {

	*initClass {
		specs.addAll(
		 [
		 	\binary -> this.new,
			\loop -> this.new,
			\paused -> this.new
			];
		)
	}
	defaultControl { arg val;
		^KrNumberEditor.new(this.constrain(val ? this.default),this).lag_(nil)
	}
}


StaticSpec : NoLagControlSpec {
	// also a scalar spec, but better to inherit ControlSpec

	canKr { ^false }
	rate { ^\noncontrol } // builds the constant into the synthDef
	defaultControl { arg val;
		^NumberEditor.new(this.constrain(val ? this.default),this)
	}
}


StaticIntegerSpec : StaticSpec {
    
	*new { arg minval=0, maxval=10, default, units;
		^super.new(minval.asInteger, maxval.asInteger, \lin, 1, default , units )
	}
	storeArgs { ^[minval,maxval,default,units] }
	*newFrom { arg similar;
		^this.new(similar.minval, similar.maxval,similar.default, similar.units)
	}

	init {
		warp = warp.asWarp(this);
		if(minval < maxval,{
			clipLo = minval.asInteger;
			clipHi = maxval.asInteger;
		}, {
			clipLo = maxval.asInteger;
			clipHi = minval.asInteger;
		});
	}
	defaultControl { arg val;
		^IntegerEditor(this.constrain(val ? this.default),this)
	}
	constrain { arg value;
		^value.asInteger.clip(clipLo, clipHi).round(step)
	}
	*initClass {
		specs.addAll(
		 [
		 	\quantity -> this.new(1,128),
			\qnty -> this.new(1,128)
			];
		)
	}
}


NamedIntegersSpec : ControlSpec {
    
    var <values,<names,<static;
    /*
    two styles for names:
        0-indexed integers 
        [ "LINEAR","CAUCHY",...]   
        using a custom integer list
        [ [512,"512"],[1024,"1024"],[2048,"2048"] ...]
    */
    *new { arg names,default,static=false;
        ^super.new(0,names.size-1,\linear,1,default).nisinit(names,static,default)
    }
	storeArgs { ^[[values,names].flop,default,static] }
	*newFrom { arg similar;
		^this.new([similar.values,similar.names].flop, similar.default,similar.static)
	}

    nisinit { arg n,s,d;
        if(n[0].isString.not) {
            # names, values = n.flop;
            if(d.isNil,{
                default = 0;
            },{
                default = values.indexOf(d) ? 0
            })
        } {
            names = n;
            values = Array.series(names.size);
            default = d ? 0;
        };
        static = s;
    }
 	canKr { ^static.not }
	rate { ^if(static,\noncontrol,\control) }
	defaultControl { arg val;
	    ^PopUpEditor(val ? default,names,values)
	}
}


ScalarSpec : ControlSpec {
	// \scalar means .ir or i_initialValue
	// SendTrig etc. output a 0.0
	// this is a scalar spec.
	canKr { ^false }
	rate { ^\scalar }
	*newFrom { arg similar;
		^this.new(similar.minval, similar.maxval, similar.warp.asSpecifier, similar.step, similar.default, similar.units)
	}
}


NonControlSpec : Spec {
    
	rate { ^\noncontrol }
	canKr { ^false }
}


SymbolSpec : NonControlSpec {}


EnvSpec : NonControlSpec {

	var <>prototype;

	// use a level spec ?

	*new { arg prototype;
		^super.new.prototype_(prototype ?? {Env.asr})
	}
	storeArgs { ^[prototype] }
	defaultControl { arg env;
		^EnvEditor.new(env ?? {prototype.copy})
	}
	default { ^prototype.copy }

	*initClass {
		specs.addAll(
			[ // from the common forms.
				\env -> this.new(Env.new([0,1,0],[1,1])),
				\envlinen -> this.new(Env.linen),
				\envtriangle -> this.new(Env.triangle),
				\envsine -> this.new(Env.sine),
				\envperc -> this.new(Env.perc),
				\envadsr -> this.new(Env.adsr),
				\envasr -> this.new(Env.asr),

				// personal, will remove
				// filter envelope
				\fenv -> this.new(Env.new([ 0, 1, 0.2, 0 ], [ 0.04, 0.4, 0.3 ], [ -6.31, 1.1, -2 ], nil, nil))
			]
		)
	}
	canAccept { arg thing;
		^thing.isKindOf(Env) or: {thing.isKindOf(EnvEditor)}
	}
}


BufferProxySpec : NonControlSpec {

	var <>numFrames=44100,<>numChannels=1,<>sampleRate=44100.0;

	*new { arg numFrames=44100,numChannels=1,sampleRate=44100.0;
		^super.new.numFrames_(numFrames).numChannels_(numChannels).sampleRate_(sampleRate)
	}
	storeArgs { ^[numFrames,numChannels,sampleRate] }
	*initClass {
		specs.addAll(
		 [
			\buffer -> this.new( 44100,2 ),
			\bufferProxy -> this.new( 44100,2 ),
			\fftbuff -> this.new( 2048 , 1)
		];
		)
	}

	//defaultControl { ^prototype.deepCopy } // this caused a deepCopy crash !?
	defaultControl { ^BufferProxy(numFrames,numChannels,sampleRate) }
	default { ^this.defaultControl }
	canAccept { arg thing;
		^thing.isKindOf(BufferProxy)
			and: {thing.numChannels == numChannels}
			and: {thing.sampleRate == sampleRate}
			and: {thing.numFrames >= numFrames}
	}
}


BusSpec : NonControlSpec {
	
	/* this is not for i_bus inputs but rather for specifying that you need a Bus object
	for kr or ir bus indices use
		a ControlSpec(0, 4096, 'linear', 1, 0, "Bus")
		or ScalarSpec(0,4096,'linear',1,0,"Bus")
	*/
	var <>rate,<>numChannels,<>private;
	
	*new { |rate,numChannels,private|
		^super.new.rate_(rate).numChannels_(numChannels).private_(private)
	}
}


SampleSpec : NonControlSpec {

	*initClass {
		specs.addAll(
		 [
			\sample -> this.new,
			\arrayBuffer -> this.new
			];
		)
	}
	defaultControl { ^Sample.new } // silent sample
	default { ^this.defaultControl }
	canAccept { arg ting; ^ting.isKindOf(Sample) }
}


ScaleSpec : NonControlSpec {
	/*
		this gets tricky.  the rate depends how you are using it.
		in a pattern
		loaded into a buffer for degree->note table lookups
	*/
	var <>prototype;
	*new { arg prototype;
		^super.new.prototype_(prototype)
	}
	canAccept { arg ting;
		^ting.isArray and: {ting.every(_.isNumber)} // ... and every is in itemSpec
	}
	default { ^prototype }
	defaultControl { ^this.default }
	storeArgs { ^[prototype] }
}


// abstract class for container objects whose content items conform to itemSpec
HasItemSpec : NonControlSpec {

	var <>itemSpec;

	*new { arg itemSpec;
		var spec;
		spec = itemSpec.asSpec;
		if(spec.isNil,{
			(this.class.name.asString ++ " nil or unfound itemSpec:" + itemSpec + spec).warn;
		});
		^super.new.itemSpec_(spec)
	}
	defaultControl { ^itemSpec.defaultControl }
	default { ^itemSpec.default }
	map { arg val;
		^itemSpec.map(val)
	}
	unmap { arg val;
		^itemSpec.unmap(val)
	}
	constrain { arg val;
		^itemSpec.constrain(val)
	}
	storeArgs { ^[itemSpec] }
}


// an array that has items that conform to itemSpec
ArraySpec : HasItemSpec {

	var <>size;

	*new { arg itemSpec,size=16;
		^super.new(itemSpec).size_(size)
	}
	canAccept { arg ting;
		^ting.isArray and: {ting.every({ |item| itemSpec.canAccept(item) })}
	}
	default { ^Array.fill(size,{itemSpec.default}) }
	defaultControl { ^this.default }
	storeArgs { ^[itemSpec,size] }
}


// a stream that returns items conforming to the itemSpec
StreamSpec : HasItemSpec {

	constrain { arg value; ^itemSpec.constrain(value) }
	canAccept { arg ting;
		^(ting.rate == \stream or: {itemSpec.canAccept(ting) })
	}
	rate { ^\stream }
	defaultControl {  arg val;
		^itemSpec.defaultControl(val)
		//^IrNumberEditor(val ? itemSpec.default, itemSpec)
	}
	minval { ^itemSpec.minval }
	maxval { ^itemSpec.maxval }
	clipLo { ^itemSpec.clipLo }
	clipHi { ^itemSpec.clipHi }
}


// an EventStream is playable as audio, but its also a stream of events for further pattern work
EventStreamSpec : NonControlSpec {

	rate { ^\stream }
	defaultControl { ^Pbind.new }
}


// a player whose output conforms to itemSpec
PlayerSpec : HasItemSpec {
    
	canAccept { arg ting;
		^(ting.isKindOf(AbstractPlayer) and: {ting.spec == itemSpec})
	}
	rate { ^itemSpec.rate }
}


// should change to InstrSpec : could be an instr or a string or a function
InstrNameSpec : HasItemSpec {

	var <>hasGate,<>hasAudioInput; // nil means "does not care"

	*new { arg outSpec,hasGate,hasAudioInput;
		^super.new(outSpec ?? {AudioEventSpec.new}).hasGate_(hasGate).hasAudioInput_(hasAudioInput)
	}
	rate {^\noncontrol }
	canAccept { arg ting;
		^(ting.isString and: {Instr(ting).notNil})
	}
	constrain { arg instr; ^instr }
}


ObjectSpec : Spec {

	var  <>defaultControl;

	*new { |defaultControl|
		^super.newCopyArgs(defaultControl)
	}

	storeArgs { |stream|
		^[defaultControl]
	}
}

