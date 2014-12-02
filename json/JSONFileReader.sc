JSONFileReader {
	*read { | path |
		var file, dict;
		file = File(path, "r");
		dict = file.readAllString.parseJson;
		file.close;
		^dict;
	}	
}