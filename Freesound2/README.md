Freesound2.sc
=============

SuperCollider client for Freesound. Requires curl.

Examples:

```
Freesound2.api_key="<your_api_key_here>";


s = Server.internal.boot


// text search

// http://www.freesound.org/docs/api/resources_apiv1.html#sound-search-resource

FS2Sound.search(q:"glitch",f:"type:wav",action:{|p|
	~snd = p[0]; // first result
	~snd.original_filename.postln;
});

// download
~snd.retrieve("/tmp/",{
	~buf = Buffer.read(s,"/tmp/"++~snd["original_filename"]);
	    "done!".postln;
});

~buf.play;


// metadata fields
~snd.dict.keys;


// note that you can access the fields directly
~snd["id"]
~snd["original_filename"];
~snd["duration"];
~snd["url"];
~snd["preview-hq-mp3"];


// download preview
~preview = ~snd.retrievePreview("/tmp/",{
		~buf = Buffer.read(s,"/tmp/"++~snd["original_filename"]);
	    "done!".postln;
});

("/tmp/"++~preview).postln;

// note: if your libsndfile version supports ogg, try format=ogg and load the resulting file

// get sound by id
// http://www.freesound.org/docs/api/resources_apiv1.html#sound-resource
FS2Sound.getSound(31362,{|f|
	~snd = f;
	~snd.retrieve("/tmp/",{
		~snd["original_filename"].postln;
		~buf = Buffer.read(s,"/tmp/"++~snd["original_filename"]);
		"done!".postln;

	});
});

~buf.play;


// get similar
//http://www.freesound.org/docs/api/resources_apiv1.html#sound-similarity-resource
~snd.getSimilar({|p| ~snd = p[1];})
~snd["original_filename"].postln;


// analysis
//http://www.freesound.org/docs/api/resources_apiv1.html#sound-analysis-resource
//http://www.freesound.org/docs/api/analysis_index.html

~snd.getAnalysis("lowlevel",{|val|
			val["pitch"]["mean"].postln;
			val["pitch_instantaneous_confidence"]["mean"].postln;
		},true)

// content-based search:
//http://www.freesound.org/docs/api/resources_apiv1.html#sound-content-based-search-resource
FS2Sound.contentSearch(t:'.lowlevel.pitch.mean:600',f:'.lowlevel.pitch_instantaneous_confidence.mean:[0.8 TO 1]',action:{|pager|
	~snd = pager[0];
	~snd["original_filename"].postln;
	~snd.retrieve("/tmp/",{
		~buf = Buffer.read(s,"/tmp/"++~snd["original_filename"]);
		"done!".postln;
	});
});

~buf.play;
```
