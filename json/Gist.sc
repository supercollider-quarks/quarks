Gist {
	var <dict;
	var <id;
	
	*newFromJsonDict{|dict|
		var gist;
		
		gist = this.new(dict["id"]);
		gist.updateDictWith(dict);
		
		^gist
	}
	
	*new{|id|
		^super.new.init(id) 	
	}

	*fork{|forkedID, username, password|
		^this.new(forkedID).fork(username, password);
	}	
	
	init {|argID|
		dict = Dictionary.new;
		id = argID;	
	}
	
	doesNotUnderstand {|selector ... args|
		var result;
		result = dict[selector.asString];
		
		result.isNil.if{^super.doesNotUnderstand(selector, *args)};
		^result
		//^dict[selector.cs].perform(selector, *args)
	}
	
	
	updateDictWith {|aDict|
		aDict["message"].notNil.if({
			"Gist: Something went wrong during update, maybe wrong password? Not updating the current Gist dict".error;
			"Message from github:".inform;
			aDict.postln;
			^this;
		});
		dict = dict.composeEvents(aDict);
		id = dict["id"];
	}
	
	*allGistsFor {|user, username, password|
		var options, gistDicts;
		
		options = password.notNil.if({
			"-u %:%".format(username, password)
		}, {
			""	
		});
				
		gistDicts = ("https://api.github.com/users/%/gists".format(user).curl(options: options)).parseJson;
		
		^gistDicts.collect{|dict|
			this.newFromJsonDict(dict)
		}
	}
	
	pull {
		this.updateDictWith("https://api.github.com/gists/%".format(id).curl.parseJson)
	}

	fork {|username, password|
		var options, jsonString;
		
		options = username.notNil.if({
			"-u %:%".format(username, password)
		}, {
			""	
		});

		options = options + "-X POST";

		^this.deepCopy.updateDictWith("https://api.github.com/gists/%/fork".format(id.postln).curl(options: options).parseJson)
	}

	delete {|username, password|
		var options;
		
		options = username.notNil.if({
			"-u %:%".format(username, password)
		}, {
			""	
		});

		options = options + "-X DELETE";

		^"https://api.github.com/gists/%".format(id).curl(options: options)
	}
	
	
	filenames {
		^this.files.keys
	}
	
	at{|filename|
		^this.files[filename]["content"]
	}
	
	put{|filename, content|
		this.files[filename]["content"] = content
	}
	
	*contentAsJsonString {|contentDict|
		var result = "{\n\n";
		var  numCommas = contentDict.size - 1;
		
		contentDict.keysValuesDo({|key, val, i|
			result = result ++ "%: {\"content\":\n %}%\n".format(key.asString.quote, 
				val
				.replace(
					"\\", 
					"\\\\"
				)
				.replace(
					"'", 
					"'\\''"
				)
				.replace(
					";", 
					"'\\\;'"
				)
				.replace(
					"\n",
					"\\n"
				)
				.replace(
					"\t",
					"\\t"
				)
				.escapeChar($").quote, (i < numCommas).if({","}, {""}))
		});
		
		result = result++ "\n}";
		^result
	}
	
	*jsonStringFrom {|descr, content, public|
		^("\'{\"description\": \"%\",\n\"public\": %,\n\"files\": %}\'"
			.format(descr, public, this.contentAsJsonString(content)));
	}
	
	
	asJsonString {
		var f = {|filesDict|
			var result = "{\n\n";
			var  numCommas = filesDict.size - 1;
				
			filesDict.keysValuesDo({|key, val, i|
				result = result ++ "%: {\n\"content\":\n %}%\n"
					.format(key.asString.quote, 
						val["content"].isNil.if({
							"null"
						},{
							val["content"].replace(
								"\\", 
								"\\\\"
							)
							.replace(
								"'", 
								"'\\''"
							)
							.replace(
								";", 
								"'\\\;'"
							)
							.replace(
								"\n",
								"\\n"
							)
							.replace(
								"\t",
								"\\t"
							)
							.escapeChar($").quote
					}), 
					(i < numCommas).if({","}, {""}))
			});
			result = result++ "\n}";
		};
		
		^("\'{\"description\": \"%\",\n\"files\": %}\'".format(dict["description"], f.(dict["files"])));
			
	}
	
	push {|username, password|
		var options, jsonString;
		
		jsonString = this.asJsonString.postln;
		
		options = username.notNil.if({
			"-u %:%".format(username, password)
		}, {
			""	
		});
	
		options = options + "-X PATCH -d %".format(jsonString);
		
		^this.updateDictWith("https://api.github.com/gists/%".format(id).curl(options: options).parseJson).pull;
	}


	printOn { arg stream;
		stream << this.class.name;
		this.storeParamsOn(stream);
	}
	storeArgs { ^[id] }

	prettyprint {|printContent = false|
		"% // (%) %\n".postf(this, this.user["login"], this.description); 
		printContent.if({
			this.files.do{|f| 
				"[ % ]\n%\n-------\n".postf(f["filename"], f["content"])
			}
		}, {
			this.files.do{|f| 
				"[ % ]\n".postf(f["filename"])
			}
		});
		"".postln
	}
	
//	*editToRepo {|id, descr, content, public = true, username, password|
//		var options, jsonString;
//		
//		jsonString = this.jsonStringFrom(descr, content, public);
//		
//		options = username.notNil.if({
//			"-u %:%".format(username, password)
//		}, {
//			""	
//		});
//	
//		options = options + "-X PATCH -d %".format(jsonString);
//		
//		^this.newFromJsonDict("https://api.github.com/gists/%".format(id).curl(options: options).parseJson);
//	}
	
	*createAndPush {|descr, content, public = true, username, password|
		var options, jsonString;
		
		jsonString = this.jsonStringFrom(descr, content, public);
		
		options = username.notNil.if({
			"-u %:%".format(username, password)
		}, {
			""	
		});
	
		options = options + "-d %".format(jsonString);
		
		^this.newFromJsonDict("https://api.github.com/gists".curl(options: options).parseJson);
	}
}