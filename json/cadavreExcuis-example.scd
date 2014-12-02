// a cadavre exquis gist setup.
// a rudimentary example on how to use the interface to the gist cloud, hosted at github.
// if the following fails for you, please check if you have to update wslib's curl method by the one in 
//	json/extString-curl.scd


// 2011 Till Bovermann
// http://tangibleauditoryinterfaces.de 
// http://tai-studio.org 
// http://LFSaw.de 

//////////////////// supposed you want to initiate a cadavre exquis /////////////////////////////

// this setup is based on simple cadavres with only one Ndef. Here's the place to put your Ndef. Work on it and evaluate it.
Ndef(\gistCadavre, {SplayAz.ar(2, SinOsc.ar({LFDNoise3.ar(Rand(0.1, 0.4)).exprange(20, 200)}!20, 0, 5).tanh, spread: 2, orientation: WhiteNoise.ar.lag(LFDNoise3.ar(pi.reciprocal).exprange(0.000001, 0.001))).tanh}).fadeTime_(2).play


(
// push the cadavre as a new gist
var username = nil; // set your user and pasword if you want to follow forks, edit, or let your successor be able to track your cadavre.
var password = nil;
var descr = "Cadavre Excuis";
var isPublic = true;
var content = Dictionary[
	"Ndef.scd" -> Ndef(\gistCadavre).asCode(true, true)
];

g = Gist.createAndPush(descr, content, isPublic, username, password);
)

// look at it in the web browser
g.html_url.openInFinder

//////////////////// if you just want to explore existing cadavres /////////////////////////////////////
(
var id = 1114384; // the cadavre's id
// get an existing cadavre, possibly yours
g = Gist(id).pull;
)

g["Ndef.scd"]
// copy-paste from post window and play it...


// pull all known forks; takes a while to complete...
h = g.forks.collect{|dict| Gist(dict["id"]).pull}

// prettyprint them including sourcecode
h.do(_.prettyprint(printContent: true))

// copy-paste from post window and play it...
h.last["Ndef.scd"]

// get all forks of one of the next cadavres
h = h.choose.forks.collect{|item| Gist(item["id"]).pull}



//////////////////// if you want to fork from an existing cadavre, do this /////////////////////////////

(
var id = 1114384; // the cadavre's id
// fork an existing cadavre
var username = nil; // set your user and password if you want to follow forks
var password = nil;

g = Gist(id).fork(username, password);
)

// pull content from github
g.pull

// see content
g["Ndef.scd"]

// copy-paste from post window and edit it...

(
Ndef('gistCadavre', {SplayAz.ar(2, SinOsc.ar({LFDNoise3.ar(Rand(0.1, 0.4)).exprange(200, 800)}!50, 0, 1).tanh, spread: 0.1, orientation: BrownNoise.ar.lag(LFDNoise3.ar(2pi.reciprocal).exprange(0.000001, 0.001))).tanh});
Ndef('gistCadavre').set('fadeTime', 2);
);


Ndef('gistCadavre').play

// update the gist's content
g["Ndef.scd"] = Ndef('gistCadavre').asCode(true, true);

Gist.contentAsJsonString(g.dict)


(
// push updated version to server
var username = nil; // set your user and password if you want to follow forks, edit, or let your successor be able to track your cadavre.
var password = nil;
g.push(username, password);
)

// look at it in the web browser
g.html_url.openInFinder

//////////////////// if you want to work on an existing cadavre, do this /////////////////////////////

(
var id = 1114384; // the cadavre's id
// get an existing cadavre, possibly yours
g = Gist(id).pull;
)

// see content
g['Ndef.scd']


// copy-paste from post window and edit it...

s.boot;

(
Ndef('gistCadavre', {SplayAz.ar(2, SinOsc.ar({LFDNoise3.ar(Rand(0.1, 0.4)).exprange(200, 800)}!50, 0, 1).tanh, spread: 2, orientation: WhiteNoise.ar.lag(LFDNoise3.ar(2pi.reciprocal).exprange(0.000001, 0.001))).tanh});
Ndef('gistCadavre').set('fadeTime', 2);
);


Ndef('gistCadavre').play

// update the gist's content
g['Ndef.scd'] = Ndef('gistCadavre').asCode(true, true);

(
// push updated version to server. this only works for your own gist.
var username = nil; // set your user and pasword here.
var password = nil;
g.push(username, password);
)

// look at it in the web browser
g.html_url.openInFinder


