Freesound2{

	classvar <base_uri 	       	  = "http://www.freesound.org/api";
	classvar <uri_sounds              = "/sounds/";
	classvar <uri_sounds_search       = "/sounds/search/";
	classvar <uri_sounds_content_search  = "/sounds/content_search/";
	classvar <uri_sound               = "/sounds/%/";
	classvar <uri_sound_analysis      = "/sounds/%/analysis/";
	classvar <uri_sound_siilar        = "/sounds/%/similar/";
	classvar <uri_users               = "/people/";
	classvar <uri_user                = "/people/%/";
	classvar <uri_user_sounds         = "/people/%/sounds/";
	classvar <uri_user_packs          = "/people/%/packs/";
	classvar <uri_packs               = "/packs/";
	classvar <uri_pack                = "/packs/%/";
	classvar <uri_pack_sounds         = "/packs/%/sounds/";
	classvar <>api_key;

	classvar <preview_type_libsndfile = 1;
	classvar <preview_type_afp = 2;
	classvar <preview_type_vlc= 3;

	classvar <parseFunc;
	classvar <previewType;
	classvar <>server;


	*parseJSON{|jsonStr|
		var parsed = jsonStr;
		var a,x;
		jsonStr.do({|char,pos|
			var inString = false;
			char.switch(
				$",{(jsonStr[pos-1]==$\ && inString).not.if({inString = inString.not})},
				${,{ if(inString.not){parsed[pos] = $(} },
				$},{ if(inString.not){parsed[pos] = $)} }
			)
		});
		^parsed.interpret;
	}

	*initClass{
		StartUp.add{
			var testFile = Freesound2.filenameSymbol.asString.dirname++"/test.ogg";
			if(SoundFile.new.openRead(testFile)) {
				previewType=preview_type_libsndfile;

			}{
				if(thisProcess.platform.name=='osx')
					{previewType =preview_type_afp }{previewType=preview_type_vlc};
			};

			try{
				parseFunc = {|str| str.parseYAML}
			}{
				parseFunc = {|str| Freesound2.parseJSON(str)};
			};
		};

		SynthDef("fs2preview", {|buf, amp= 0.7|
			Out.ar(0,DiskIn.ar(1, buf, 0)*amp!2);
		}).store;


		server = Server.default;
	}

	*uri{|uri,args|
		^(Freesound2.base_uri++uri.format(args));
	}
}

FS2Req{
	var <url,<filePath,<cmd;
	*new{|anUrl,params|
		if(Freesound2.api_key.isNil){throw("API key is not set! Can't proceed")};
		^super.new.init(anUrl,params);
	}

	init{|anUrl,params|
		var paramsString,separator="?";
		url = anUrl;
		filePath =PathName.tmp++"fs2_"++UniqueID.next++".txt";
		params = params?IdentityDictionary.new;
		params.put(\api_key,Freesound2.api_key);
		paramsString=params.keys(Array).collect({|k|k.asString++"="++params[k].asString.urlEncode}).join("&");
		if (url.contains(separator)){separator="&"};
		cmd =  "curl '"++this.url++separator++paramsString++"' >"++filePath;
		cmd.postln;
	}

	get{|action,objClass|
		cmd.unixCmd({|res,pid|
			var result = objClass.new(
				Freesound2.parseFunc.value(
					File(filePath,"r").readAllString
				)
			);
			action.value(result);
		});
	}

	*retrieve{|uri,path,action| //assuming no params for retrieve uris
		var cmd;
		uri = uri++"?api_key="++Freesound2.api_key;
		cmd = "curl %>'%'".format(uri,path);
		cmd.postln;
		cmd.unixCmd(action);
	}
}

FS2Obj : Object{
	var <dict;
	*new{|jsonDict|
		^super.new.init(jsonDict);
	}

	init{|jsonDict|
		dict = jsonDict.as(Dictionary);
		dict.keysDo{|k|
			this.addUniqueMethod(k.replace("-","_").asSymbol,{
				var obj = dict[k];
				if (obj.isKindOf(Dictionary)){obj=FS2Obj.new(obj)};
				obj;
			});
		};
	}
	at{|x| ^this.dict.at(x)}
}

/* TODO
FS2Preview {
	var sound, pid, synth;
	*new{|sound|
		^super.newCopyArgs(sound);
	}


	bootServer{|f|
		Freesound2.server.waitForBoot{f.value};
	}
	startCMD{|cmd|
		cmd.postln;
		// TODO: unixCmd and set pid to result
	}
	startDiskIn{|path|
		var b = Buffer.cueSoundFile(Freesound2.server,path,0,1);
		b.postln;
		synth = Synth(\fs2preview,
			[\buf, b],Freesound2.server
		);
	}

	downloadAndPlay{
		var path = "/tmp/"++sound.retrievePreview("/tmp/",{},'lq','ogg');
		AppClock.sched(0.5,{this.startDiskIn(path)});
	}
	start{
		Freesound2.previewType.switch(
			Freesound2.preview_type_afp,{this.startCMD("afplay %".format(sound["preview-lq-mp3"]))},
			Freesound2.preview_type_vlc,{this.startCMD("vlc %".format(sound.url))},
			Freesound2.preview_type_libsndfile,{this.downloadAndPlay}
		);
		//^this;
	}
	stop{
		// if pid is not nil, kill pid
	}
}
*/

FS2Pager : FS2Obj {
	next{|action|
		FS2Req.new(dict["next"]).get(action,FS2Pager);
	}
	prev{|action|
		FS2Req.new(dict["prev"]).get(action,FS2Pager);
	}
	at{|i|
		^FS2Sound.new(this.sounds[i]);
	}
	do{|f|
		this.sounds.do({|snd,i| f.value( FS2Sound.new(snd))});
	}

}

FS2Sound : FS2Obj{
	*getSound{|soundId, action|
		FS2Req.new(Freesound2.uri(Freesound2.uri_sound,soundId)).get(action,FS2Sound);
	}

	*search{|q,f,p, action|
		FS2Req.new(Freesound2.uri(Freesound2.uri_sounds_search),('q':q,'f':f,'p':p)).get(action,FS2Pager);
	}

	*contentSearch{|t,f,p, action|
		FS2Req.new(Freesound2.uri(Freesound2.uri_sounds_content_search),('t':t,'f':f,'p':p)).get(action,FS2Pager);
	}
	retrieve{|path, action|
		FS2Req.retrieve(this.serve,path++"/"++this.original_filename,action);
	}

	retrievePreview{|path, action, quality="hq", format="mp3"|
		var key = "%-%-%".format("preview",quality,format);
		var fname = this.original_filename.splitext[0]++"."++format;
		FS2Req.retrieve(this.dict[key],path++fname,action);
		^fname;
	}

	getAnalysis{|filter, action,showAll=false|
		var url = Freesound2.uri(Freesound2.uri_sound_analysis,this.id);
		var params = nil;
		if(filter.notNil){url = url ++filter++"/"};
		if(showAll){params = ('all':1)};
		FS2Req.new(url,params).get(action,FS2Obj);
	}

	retrieveAnalysisFrames{|path, action|
		var fname = this.original_filename.splitext[0]++".json";
		FS2Req.retrieve(this.analysis_frames,path++"/"++fname,action);
	}

	getSimilar{|action, preset="lowlevel", num_results=15|
		var params = ('preset':preset,'num_results':num_results);
		var url = Freesound2.uri(Freesound2.uri_sound_siilar,this.id);
		FS2Req(url,params).get(action,FS2Pager);
	}
	// TODO
	//preview{
	//	^FS2Preview.new(this).start;
	//}
}


+String{
	urlEncode{
		var str="";
		this.do({|c|
			if(c.isAlphaNum)
			{str = str++c}
			{str=str++"%"++c.ascii.asHexString(2)}
		})
		^str;
	}
}
