// TODO : Add output busses (for effects)
// TODO : Show which channels are running

// note - select all (because of brackets matching problem)


XiiLiveCoder {

	var <>win, <>xiigui, params;
	
	*new { arg server, channels, setting = nil; 
		^super.new.initXiiLiveCoder(server, channels, setting);
	}

	initXiiLiveCoder {arg server, channels, setting;
	var txtv, runCode, selectedChannel, codeList, volumeSliderList, 
	volumeBusSynths, codeSynths, templatePop, templateCodeDict, templateNameField;
	var codeButtList, runList, outBusList;
	var point, outbus;
	
	SynthDef(\xiiLCbusVolume, {arg vol=0.0, inbus, outbus=0 ;
		var in, signal;
		in = InFeedback.ar(inbus, 1);
		signal = in * vol;
		Out.ar(outbus, Pan2.ar(signal));
	}, [0.15]).load(server);  // the lag is for shaky hands moving a vol slider - he he
	
	
	outbus = nil;
	codeButtList = List.new;
	runList = List.new;
	volumeSliderList = List.new;
	volumeBusSynths = List.fill(16, {nil});
	outBusList = List.new;
	codeSynths = Array.fill(16, nil);

xiigui = nil;
point = if(setting.isNil, {Point(310, 250)}, {setting[1]});
params = if(setting.isNil, {[{""}!16, {0}!16, {0}!16]}, {setting[2]});

// Post << [\incomingPARAMS, params];

codeList = params[0];

	templateCodeDict = if(Object.readArchive("preferences/livecodetemplate.ixi") == nil, {
		"ixi-NOTE: you don't have 'livecodetemplate.ixi' file in your preferences folder".postln;
					()
					.add(\saw -> "{Saw.ar(440)}.play")
					.add(\sine -> "{SinOsc.ar(440)}.play")
					.add(\pulse -> "{Pulse.ar(440)}.play");
					}, {
						 Object.readArchive("preferences/livecodetemplate.ixi");
					});
	
	
	selectedChannel = 0;
	
	win = Window("- livecoder -", Rect(point.x, point.y, 766, 370), resizable: false); // main windon
	
	
	txtv = TextView(win, Rect(20, 10, 500, 320)) // textview
		.hasVerticalScroller_(true)
		.autohidesScrollers_(true)
		.usesTabToFocusNextView_(false)
		.focus(true)
		.string_(params[0][0])
		.keyUpAction_({
			codeList[selectedChannel] = txtv.string; // if code not run, store string anyway in list
		});
	
	
		StaticText(win, Rect(115, 339, 60, 20))
			.font_(GUI.font.new("Helvetica", 9))
			.string_("patch name:");
	
	templateNameField = TextView(win, Rect(170, 341, 100, 14)) // textview
		.font_(GUI.font.new("Helvetica", 9))
		.autohidesScrollers_(true)
		.focus(false);
	
		Button(win, Rect(280, 340, 65, 16))
			.states_([["store code", Color.black, Color.clear]])
			.font_(GUI.font.new("Helvetica", 9))
			.action_({
				templateCodeDict.add(templateNameField.string.asSymbol -> txtv.string);
				templatePop.items_(templateCodeDict.keys.asArray.sort);
				templateCodeDict.writeArchive("preferences/livecodetemplate.ixi"); 
			});
	
		Button(win, Rect(350, 340, 65, 16))
			.states_([["delete code", Color.black, Color.clear]])
			.font_(GUI.font.new("Helvetica", 9))
			.action_({
				templateCodeDict.removeAt(templatePop.items[templatePop.value].asSymbol);
				templatePop.items_(templateCodeDict.keys.asArray.sort);
				templateCodeDict.writeArchive("preferences/livecodetemplate.ixi"); 
			});
	
	templatePop = PopUpMenu(win, Rect(422, 340, 100, 16))
				.font_(GUI.font.new("Helvetica", 9))
				.items_(templateCodeDict.keys.asArray.sort)
				.background_(Color.new255(255, 255, 255))
				.action_({ arg popup; 
					var templatename, templatecode;
					[\popupval, popup.value].postln;
					templatename = templateCodeDict.keys.asArray.sort[popup.value];
					[\templatename, templatename].postln;
					templatecode = templateCodeDict.at(templatename);
					txtv.string = templatecode;
					codeList[selectedChannel] = templatecode;
				});
	
	
	runCode = { arg channel;
		var code;
		code = codeList[channel];
		
		// ------
		// if text has been pasted, but no keystroke we need to get the text
		// it also means that one can duplicate code into different channels by running the code
		if(code == "", { codeList[channel] = txtv.string; code = codeList[channel] });
		// ------
		
		if(code.contains(".play"), { // if it's a synth
	 		code.size.do({arg index;
	 			if(code.containsStringAt(index, ".play"), {
	 				code = code.copyRange(1, index-2);
	 			});
			});
			outbus = XQ.pref.numberOfChannels + (channel*2);
			codeSynths[channel] =
				SynthDef("o"++channel, { Out.ar(outbus, 
					code.interpret
				)
				}).play(target: server, addAction: \addToHead );
		}, {
			codeSynths[channel] = code.interpret;
		});
	};
	
	// the tracks
	16.do({ arg i;
		
		codeButtList.add(
			Button(win, Rect(530,10+(i*22), 32, 16))
			.states_(				
				if(codeList[i] == "", {
						[["code", Color.black, Color.clear], 
						["code", Color.black, XiiColors.darkgreen]]
					}, {
						[["code", Color.black, XiiColors.ixiorange.alpha_(0.2)], 
						["code", Color.black, XiiColors.darkgreen]]
				});
			)
			.font_(GUI.font.new("Helvetica", 9))
			.action_({
				if(codeList[selectedChannel] == "", {
					codeButtList[selectedChannel].states_(
						[["code", Color.black, Color.clear], 
						["code", Color.black, XiiColors.darkgreen]])
					}, {
					codeButtList[selectedChannel].states_(
						[["code", Color.black, XiiColors.ixiorange.alpha_(0.2)], 
						["code", Color.black, XiiColors.darkgreen]])
				});
	
				codeList[selectedChannel] = txtv.string; // store code in list (even if not run)
				params[0][selectedChannel] = txtv.string; // store the code in params
				txtv.setString("",-1); // then clean the view
				txtv.string = codeList[i]; // get the text from list, if any
				codeButtList.do({arg j; j.value_(0);});
				codeButtList[i].value_(1);
				txtv.focus(true);
				selectedChannel = i; // in order to know where to get or set strings in list
	
			});
		);
		
		runList.add(	
			Button(win, Rect(568,10+(i*22), 30, 16))
			.states_([["run", Color.black,Color.green.alpha_(0.2)], ["kill", Color.black, Color.red.alpha_(0.2)]])
			.font_(GUI.font.new("Helvetica", 9))
			.action_({arg butt;
				if(butt.value == 1, {
						if(codeSynths[i].isNil.not, {
							codeSynths[i].free; 
							codeSynths[i].stop;
							volumeBusSynths[i].free;
						});
						runCode.value(i);
						volumeBusSynths[i] = Synth(\xiiLCbusVolume, 
							[\inbus, XQ.pref.numberOfChannels+(i*2), 
							\outbus, outBusList[i].value*2,
							\vol, [0,1,\amp, 0.00001].asSpec.map(volumeSliderList[i].value)], 
							addAction: \addToHead);
				}, {
						codeSynths[i].free;  // free the synth
						codeSynths[i].stop;  // free the synth
						codeSynths[i] = nil; // delete synth from list
						volumeBusSynths[i].free;
						volumeBusSynths[i] = nil;
						//volumeSliderList[i].value_(0);
				})
	
			});
		);
				
		volumeSliderList.add(
			Slider(win, Rect(606,10+(i*22), 80, 16))
				.value_(params[1][i])
				.action_({arg sl;
					volumeBusSynths[i].set(\vol, [0,1,\amp, 0.00001].asSpec.map(sl.value));
					params[1][i] = sl.value;
				});
		);
		
		StaticText(win, Rect(692, 8+(i*22), 20, 20))
			.font_(GUI.font.new("Helvetica", 9))
			.string_(XQ.pref.numberOfChannels + (i*2));
	
		
		outBusList.add(
			GUI.popUpMenu.new(win, Rect(710,10+(i*22), 40, 16))
				.font_(GUI.font.new("Helvetica", 9))
				.items_(XiiACDropDownChannels.getStereoChnList)
				.background_(Color.new255(255, 255, 255))
				.value_(params[2][i])
				.action_({ arg popup; 
					var outbus;
					outbus = popup.value * 2;
					volumeBusSynths[i].set(\outbus, outbus);
					params[2][i] = popup.value;
				});
		);
	});

	codeButtList[0].valueAction_(1);
	win.onClose_({
		var t;
		
		codeSynths.do({|synth| synth.free;});
		volumeBusSynths.do({|synth| synth.free;});
		
		XQ.globalWidgetList.do({arg widget, i; if(widget == this, {t = i})});
		try{XQ.globalWidgetList.removeAt(t)};

	});
	win.front;

	}

	getState { // for save settings
		var point;
		point = Point(win.bounds.left, win.bounds.top);
		^[2, point, params];
	}

}


/*
-----------
Task({ 
	50.do({ arg i;
		Synth(\xiiString, [\freq, 200+(800.rand), \outbus, 52]); 
		0.25.wait; 
	}); 
}).start;

-----------

{Decay.ar(Impulse.ar(4), 0.5, Pulse.ar(30))}.play

-----------

{Decay.ar(Impulse.ar(XLine.kr(1,50,20)), 0.5, Pulse.ar(XLine.kr(20,150,20)))}.play

-----------

{Decay.ar(Impulse.ar(XLine.kr(1,50,20)), 0.5, Pulse.ar(13))}.play

-----------

(
{
p = Impulse.ar(4);
a = Decay2.ar(p, 0.1, 0.5, Pulse.ar(30));
b = Decay2.ar(PulseDivider.ar(p, 4), 0.01, 1.2, Pulse.ar(130));
a+b
}.play
)

-----------


(
{ SinOsc.ar(
	Stepper.kr(
		Impulse.kr(XLine.ar(10, 120, 20, doneAction:2)), 
	0, 4, 16, 1) * 100, 0, 0.05
)}.play
)


-----------


(
{ Saw.ar(
	Stepper.kr(
		Impulse.kr(XLine.ar(10, 120, 20, doneAction:2)), 
	0, 4, 16, 4) * 20, 0.05
)}.play
)



-----------

Buffer.read(s, "sounds/a11wlk01.wav", action:{ arg buf; buf.play });

-----------

// sheperds tone
 {
 var sig, line;
 line = Line.kr(10, 230, 300); 
 sig = Mix.fill(
	 6, { |i|
	 SinOsc.ar(
		 (36 + ((line + (i*12)) % 60)).midicps,
		 2pi.rand,
		 (sin((((line*0.083333333333333)+i)%5)*(0.2*pi)).abs)*0.2
		 );
	 });
 sig
 }.play


-----------
{ SinOsc.ar(
	Stepper.kr(
		Impulse.kr(XLine.ar(10, 120, 20, doneAction:2)), 
	0, 4, 16, 1) * 100, 0, 0.05
)}.play

-----------
{
 Mix.fill(
	 6, { |i|
	 SinOsc.ar(
		 (36 + ((Line.ar(10, 230, 300) + (i*12)) % 60)).midicps,
		 2pi.rand,
		 (sin((((Line.ar(10, 230, 300)*0.083333333333333)+i)%5)*(0.2*pi)).abs)*0.2
		 );
	 });
 }.play

-----------

{
 Mix.fill(
	 6, { |i|
	 Saw.ar(
		 (36 + ((Line.ar(10, 230, 300) + (i*12)) % 60)).midicps,
		 (sin((((Line.ar(10, 230, 300)*0.083333333333333)+i)%5)*(0.2*pi)).abs)*0.2
		 );
	 });
 }.play


-----------
{VarSaw.ar(
	Pulse.ar(
		LFNoise2.kr(
			LFTri.kr(0.808414896252931).exprange(2.0, 6.0)
		).exprange(120, 3121)
	).exprange(222, 3333)
, 0.5, LFSaw.ar(0.5).range(0,1)
).range(-1, 1)
}.play


-----------

{
p = Impulse.ar(4);
a = Decay2.ar(p, 0.1, 0.5, Pulse.ar(30));
b = Decay2.ar(PulseDivider.ar(p, 4), 0.01, 1.2, Pulse.ar(130));
a+b
}.play
 
 
-----------

{SinOsc.ar(Pulse.ar(Impulse.kr(Dust.kr(LFDNoise0.kr(Dust.kr(LFTri.kr(2.3207269576712495, 1.309208869934082, 0.6205825805664062).exprange(17037.69141674, 13981.635122299)).exprange(6.6556851506233, 18.098876428604)).exprange(3580.2074766159, 5821.2517523766)).exprange(10.816042280197, 6.0035542845726)).exprange(8029.2342400551, 14995.68239212)).exprange(634.36201537261, 19645.161578655)).range(-1, 1)}.play

-----------

{SinOsc.ar(Pulse.ar(4092.613120303491, Impulse.kr(SyncSaw.kr(FSinOsc.kr(Line.kr(2261.8961334228516, 5923.852920532227).exprange(17.905752718449, 6.5902675747871)).exprange(12.133239972591, 13.562456011772)).exprange(5.0877082228661, 4.9762544751167)).range(0.47346186637878, 0.77896022796631)).exprange(1282.6335215569, 19043.487529755)).range(-1, 1)}.play 

-----------

{SinOsc.ar(Saw.ar(Impulse.kr(LFNoise1.kr(12.846470237142448, SinOsc.kr(LFDNoise0.kr.exprange(4.8213455796242, 9.5914839029312)).range(0.15152990818024, 0.094544172286987)).exprange(4.6998263120651, 6.6200489878654)).exprange(5295.0951051712, 12935.531184673)).exprange(8264.0452421899, 19677.363536358)).range(-1, 1)}.play 

-----------
{SinOsc.ar(Pulse.ar(LFSaw.kr(FSinOsc.kr(3.747170783231779, 0.9187963008880615).exprange(14.026722478867, 1.3088516116142)).exprange(9091.1009407043, 6919.2980194092)).exprange(5773.7288256106, 19528.38065505)).range(-1, 1)}.play

-----------

{SinOsc.ar(Formant.ar(GrayNoise.kr(Impulse.kr(0.21467797752099096, LFClipNoise.kr.range(0.3949830532074, 0.35216581821442)).range(0.90069842338562, 0.6854043006897)).exprange(2277.3691439629, 17828.061316013)).exprange(311.54950309778, 19316.050187349)).range(-1, 1)}.play

-----------

{SinOsc.ar(Pulse.ar(SinOsc.ar(LFPar.kr(Line.kr(LFDClipNoise.kr.range(7195.7659721375, 15425.386428833)).exprange(11.474647057056, 3.2561584353447)).exprange(17026.151587963, 13097.741401196)).exprange(7788.0029916763, 813.53769540787)).exprange(13261.311626434, 8015.696079731)).range(-1, 1)}.play


-----------

{SinOsc.ar(LFPulse.ar(LFDClipNoise.kr(Crackle.kr(SinOsc.kr(LFNoise1.kr.exprange(2.6018972754478, 4.012377166748)).range(1.6219574213028, 1.6573997735977)).exprange(4.9585550069809, 14.417887604237)).exprange(10485.629107952, 14375.108747482)).exprange(6491.2453985214, 19742.167594433)).range(-1, 1)}.play

-----------

{SinOsc.ar(Saw.ar(236.8047659488767, 0.44683723738699244, 0.829054138877175).exprange(2851.8072915077, 19617.766096592)).range(-1, 1).perform('min', LFClipNoise.ar(21.63735644548361).range(-0.69784450531006, 0.52837800979614))}.play

-----------

{SinOsc.ar(Formant.ar(LFDNoise1.kr(SyncSaw.kr(0.46821777508948065).exprange(1.9593978047371, 9.6959086298943)).exprange(87.719383239746, 2221.1752986908)).exprange(327.39826218458, 6475.7660698891)).range(-1, 1)}.play

-----------

{SinOscFB.ar(LFSaw.ar(1967.165285763459, LFDNoise1.kr(BrownNoise.kr.exprange(16.926237440109, 17.418648457527)).range(1.7920627593994, 0.93042612075806)).exprange(24.059197945986, 4441.9123339653)).range(-1, 1)}.play

-----------

{SinOscFB.ar(LFCub.ar(5081.422380859406, Dust.ar(LFDNoise1.ar.exprange(4658.2680463791, 6027.7944421768)).range(1.025860786438, 0.3866138458252)).exprange(115.1154863392, 10002.440161705)).range(-1, 1)}.play

-----------

{SinOsc.ar(Pulse.ar(LFDClipNoise.kr(Vibrato.kr(LFDNoise3.kr(Vibrato.kr(LFNoise2.kr(Vibrato.kr(SinOsc.kr(0.10190806413309508, 5.5825920132967815, 0.6197648048400879, -0.6639714241027832).exprange(2.9247293114662, 0.94589508771896)).exprange(12.93872590065, 14.231738352776)).exprange(12.822309374809, 2.7643428564072)).exprange(12.913930988312, 8.2951668858528)).exprange(6.0657123684883, 0.40745027065277)).exprange(4.1358725309372, 17.110341799259)).exprange(6015.8735251427, 19764.551765919)).exprange(667.10188249918, 2614.7775411606)).range(-1, 1).perform('-', LFClipNoise.ar(8801.917031654182, PinkNoise.kr(0.5891622919024843, -0.5905970082138524).range(0.17920589447021, 0.075519680976868)).range(-0.75587749481201, 0.73379945755005).perform('clip2', VarSaw.ar(LFDNoise1.kr(4.92779932309234, 0.25566959381103516).exprange(14056.931016445, 17617.055130005)).range(-0.5838086605072, -0.56859612464905)))}.play

-----------

{SinOsc.ar(SinOsc.kr(FSinOsc.kr(2.677792806214107).exprange(0.4262006521225, 14.130473482609)).exprange(19152.678842545, 3907.9171776772)).range(-1, 1)}.play

-----------

{SinOsc.ar(Saw.ar(LFPulse.kr(LFNoise2.kr(SinOscFB.kr(1.134837990413838, BrownNoise.kr(LFDNoise0.kr(XLine.kr.exprange(4.1624845981598, 18.69537293911)).range(0.64308297634125, 0.30711758136749)).range(0.63843870162964, 0.078478097915649)).exprange(0.65343515872955, 2.639677965641)).exprange(18.92814668417, 10.652688121796)).exprange(4333.8976311684, 3250.9900951385)).exprange(6637.5023078918, 13923.702561855)).range(-1, 1)}.play

-----------

{SinOsc.ar(Saw.ar(Impulse.kr(5.548253482861902, LFTri.kr(FSinOsc.kr.exprange(6.1650817990303, 17.096876823902)).range(0.6246235370636, 0.082713842391968)).exprange(5649.9811625481, 13998.488750458)).exprange(2583.2258152962, 13053.034985065)).range(-1, 1)}.play

-----------

{SinOsc.ar(Pulse.ar(LFDNoise0.kr(Dust2.kr(ClipNoise.kr(SinOsc.kr(LFNoise0.kr.exprange(17.233270192146, 19.374829924107)).range(0.85350656509399, 0.28616559505463)).exprange(16676.117260456, 17281.845135689)).exprange(7.1259724259377, 9.6767407298088)).exprange(10249.995093346, 1603.0787277222)).exprange(1873.9834132195, 5892.3280467987)).range(-1, 1)}.play

-----------

{SinOsc.ar(Pulse.ar(LFDNoise0.kr(LFCub.kr(0.808414896252931).exprange(3.2625374555588, 4.9040379881859)).exprange(15479.852640629, 850.02689599991)).exprange(424.13337826729, 2353.7956793308)).range(-1, 1)}.play


-----------

{SinOsc.ar(SinOscFB.ar(222.5110609404171, LFSaw.kr(PinkNoise.kr(0.017853577931722004, 0.1160733699798584).exprange(14.723705756664, 8.9021488070488)).range(0.92265129089355, 0.39499652385712)).exprange(318.23692886718, 12940.678257942)).range(-1, 1)}.play

-----------

{SinOsc.ar(Formant.ar(GrayNoise.kr(Impulse.kr(0.21467797752099096, LFClipNoise.kr.range(0.3949830532074, 0.35216581821442)).range(0.90069842338562, 0.6854043006897)).exprange(2277.3691439629, 17828.061316013)).exprange(311.54950309778, 19316.050187349)).range(-1, 1)}.play

-----------

-----------
-----------
-----------

-----------
-----------
-----------

-----------
-----------
-----------

*/
