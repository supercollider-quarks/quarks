/* 

r = Republic().makeDefault;
z = RepublicGui.new;
z.object = r;
r.join(\adc);
Server.default = r.myServer;


(
SynthDef(\bla, { |out, freq = 220|
	var frex = freq * \klonk.kr([1, 2, 3, 4]);
	Out.ar(0, SinOsc.ar(frex).mean * Line.kr(0.2, 0, 0.3, doneAction: 2));
}).share; 

SynthDef(\able, { |out, freq = 220|
	var frex = freq * \harm.kr([1, 1.618, 2.618, 4.236]);
	Out.ar(0, Pulse.ar(frex, \width.kr(0.2)).mean  * Line.kr(0.2, 0, 0.3, doneAction: 2));
}).share; 

SynthDef(\bubble, { |out, freq = 220|
	var frex = freq * \blink.kr([1, 2, 3, 4]);
	Out.ar(0, VarSaw.ar(frex, 0, \width.kr(0.2)).mean * Line.kr(0.2, 0, 0.3, doneAction: 2));
}).share; 
)


r.leave;
r.join(\adc);

z.object = nil;

openHelpFile
	auto-IDs work it seems - test again with lots of people.
	manual IDs seem fine - see all others, pick a free one

Republic TODO: 

* rewrite simple help / instructions: 
	recommend quarks for AllGui, Preference; 
	recommend structure of where to keep things

* maybe publish example folders? 
	* pbup_basic 
	* pressure
	* chains
	* ndefset/bing
	* other thematic ones
	* encourage others to contribute folders

//// tech: 

* 	shareSynthDescs: 
	sends to other langs;
	local server requests them from lang when booted.
	request synthdefs from others every now and then? 

* 	get to servers more easily: 
	r.nameList; 
	make the listView bigger, show strengths in listView? 
	use equal-spaced font? 

*	make synths and nodeproxies work;
	nodeID does derive numbers from clientID; 
	
	bus allocation should be rather easy too;
 
 	buffers? hmmm. 
	 

r = Republic.new.makeDefault;
r.join(\adc, 1);
r.gui;
Server.default = r.myServer;
r.join(\asdfg, 3);
r.join(\blong);

r.leave;
Server.default = r.myServer;

(
SynthDef(\bla, { |out, freq = 220|
	var frex = freq * \klonk.kr([1, 2, 3, 4]);
	Out.ar(0, SinOsc.ar(frex).mean * Line.kr(0.2, 0, 0.3, doneAction: 2));
}).share; 

SynthDef(\able, { |out, freq = 220|
	var frex = freq * \harm.kr([1, 2, 3, 4]);
	Out.ar(0, Pulse.ar(frex, \width.kr(0.2)).mean  * Line.kr(0.2, 0, 0.3, doneAction: 2));
}).share; 

SynthDef(\bubble, { |out, freq = 220|
	var frex = freq * \blink.kr([1, 2, 3, 4]);
	Out.ar(0, VarSaw.ar(frex, 0, \width.kr(0.2)).mean * Line.kr(0.2, 0, 0.3, doneAction: 2));
}).share; 
)

Server.default = r.myServer;
r.informServer;
(instrument: \bla).play;
(instrument: \able).play;
(instrument: \bubble).play;

r.postExamples;


b = SynthDescLib.global.synthDescs[\bla]

SynthDescLib.global.synthDescs.do { |sd| sd.exampleEventString.postln }; "";


*/
