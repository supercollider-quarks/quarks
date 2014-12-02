
// this class stores parameters needed to define a mixer channel

// mixers should be customizable for any number of channels, any panning methodology
// define all that in this class; definitions are reused in multiple mixers
// h. james harkins -- jamshark70@dewdrop-world.net

MixerChannelDef {

	// the setters are there for the sake of inheritance
	// to make a similar mcdef, .copy an existing one,
	// then replace the instance variables you need to change

	// outChannels, basicFader and postSendReadyFader automatically cause synthdefs
	// to be resent so that running servers are in sync with the definition

	var	<>name, <>inChannels, <outChannels, <fader,
	<>controls, <>guidef;

	*initClass {
		Class.initClassTree(Collection);
		Library.global.put(\mixerdefs, IdentityDictionary.new);
		StartUp.add {
			MixerChannelDef(\mix1x1, 1, 1,
				fader: SynthDef("mixers/Mxb1x1", {
					arg busin, busout, level;
					var in, bad, badEG;
					in = In.ar(busin, 1) * level;
					bad = CheckBadValues.ar(in, post: 0) + (in.abs > 2);
					SendReply.ar(bad, '/mixerChBadValue', bad, 0);
					badEG = EnvGen.ar(Env(#[1, 0, 1], #[0, 0.05], releaseNode: 1), bad);
					in = Select.ar(bad > 0, [in * badEG, DC.ar(0)]);
					// so that mixerchan bus can be used as postsendbus
					ReplaceOut.ar(busin, in);
					Out.ar(busout, in);
				}, [nil, nil, 0.08]),
				controls: (level: (spec: \amp, value: 0.75))
			);

			MixerChannelDef(\mix1x2, 1, 2,
				fader: SynthDef("mixers/Mxb1x2", {
					arg busin, busout, level, pan;
					var in, out, bad, badEG;
					in = In.ar(busin, 1) * level;
					bad = CheckBadValues.ar(in, post: 0) + (in.abs > 2);
					SendReply.ar(bad, '/mixerChBadValue', bad, 0);
					badEG = EnvGen.ar(Env(#[1, 0, 1], #[0, 0.05], releaseNode: 1), bad);
					in = Select.ar(bad > 0, [in * badEG, DC.ar(0)]);
					out = Pan2.ar(in, pan);
					// so that mixerchan bus can be used as postsendbus
					ReplaceOut.ar(busin, out);
					Out.ar(busout, out);
				}, [nil, nil, 0.08, 0.08]),
				controls: (level: (spec: \amp, value: 0.75),
					pan: \bipolar
				)
			);

			MixerChannelDef(\mix2x2, 2, 2,
				fader: SynthDef("mixers/Mxb2x2", {
					arg busin, busout, level, pan;
					var in, l, r, out, bad, badEG, silent = DC.ar(0);
					in = In.ar(busin, 2) * level;
					bad = (CheckBadValues.ar(in, post: 0) + (8 * (in.abs > 2)));
					SendReply.ar(bad, '/mixerChBadValue', bad, 0);
					bad = bad.sum;
					badEG = EnvGen.ar(Env(#[1, 0, 1], #[0, 0.05], releaseNode: 1), bad);
					bad = bad > 0;
					#l, r = in.collect { |chan| Select.ar(bad, [chan * badEG, silent]) };
					out = Balance2.ar(l, r, pan);
					ReplaceOut.ar(busin, out);
					Out.ar(busout, out);
				}, [nil, nil, 0.08, 0.08]),
				controls: (level: (spec: \amp, value: 0.75),
					pan: \bipolar
				)
			);
		};

	}

	*new { |name, inChannels, outChannels, /*basicFader, */fader, controls, guidef|
		inChannels.isNil.if({
			^this.at(name)
		}, {
			^super.newCopyArgs(name, inChannels, outChannels, fader,
				controls, guidef).init
		});
	}

	// validate inputs and store in Library
	init {
		name = name.asSymbol;
		fader = fader.asSynthDef;
		controls.respondsTo(\keys).not.if({
			MethodError("Error constructing MixerChannelDef - controls must be a Dictionary",
				this).throw
		});

		Library.global.put(\mixerdefs, name, this);

		this.sendDefsToRunningServers;
	}

	*at { |name|
		^Library.global[\mixerdefs, name]
	}

	*all { ^Library.global[\mixerdefs] }

	// instance methods

	synthdef { |postSendReady = false|
		^fader
	}

	defName { |postSendReady = false|
		^fader.name
	}

	makeControlArray { |mixer|
		var out = IdentityDictionary.new, name = mixer.name, gc, gcname;
		controls.keysValuesDo({ |key, gcspec|
			gcname = "% %".format(name, key);
			case
			// (spec: ControlSpec, value: initial value)
			{ gcspec.respondsTo(\keysValuesDo) } {
				gc = gcspec[\spec].asSpec;
				gc = MixerControl(gcname, BusDict.control(mixer.server, 1, gcname),
					gcspec[\value] ?? { gc.default }, gc);
			}
			{ gcspec.isNumber } {
				gc = MixerControl(gcname, BusDict.control(mixer.server, 1, gcname),
					gcspec, \unipolar);
			}
			// default: use asSpec
			{
				gcspec = gcspec.asSpec;
				gc = MixerControl(gcname, BusDict.control(mixer.server, 1, gcname),
					gcspec.default, gcspec);
			};
			out.put(key, gc);
		});
		^out
	}

	outChannels_ { |outCh|
		(outCh.notNil and: { outCh != outChannels }).if({
			outChannels = outCh;
			this.sendDefsToRunningServers;	// update send, xfer and recorder synth
		});
	}

	fader_ { |synthdef|
		synthdef.notNil.if({
			fader = synthdef;
			this.sendDefsToRunningServers;
		});
	}

	sendDefsToRunningServers {
		Server.named.do({ |server|
			this.sendSynthDefs(server);
		});
	}

	// send the mixer, xfer, send and record synths for this MCDef
	sendSynthDefs { |server|
		server = server ?? Server.default;
		server.serverRunning.if({
			this.synthdef.send(server);

			// this doesn't cover all cases
			// if you have defs only for stereo outs, there would be no send def for mono in
			// but why would you route a stereo signal into a mono mixer?
			// (send is not meant to reduce the number of channels)
			// so, you can break it only by doing something stupid to begin with
			SynthDef("mixers/Send" ++ outChannels, {
				arg busin, busout, level;
				var in = In.ar(busin, outChannels), bad, badEG, silent = DC.ar(0);
				bad = (CheckBadValues.ar(in, post: 0) + (in.abs > 2)).asArray.sum > 0;
				// SendReply.ar(bad, '/mixerChBadValue', bad, 1);
				badEG = EnvGen.ar(Env(#[1, 0, 1], #[0, 0.05], releaseNode: 1), bad);
				bad = bad > 0;
				in = in.asArray.collect { |chan| Select.ar(bad, [chan * badEG, silent]) };
				Out.ar(busout, in * level);
			}, [nil, nil, 0.08]).send(server);

			SynthDef("mixers/Rec" ++ outChannels, { arg i_in, i_bufNum = 0;
				DiskOut.ar(i_bufNum, In.ar(i_in, outChannels));
			}).send(server);

		});
	}

	*sendSynthDefs { |server|
		this.all.do({ |def|
			def.sendSynthDefs(server);
		});
	}

}

