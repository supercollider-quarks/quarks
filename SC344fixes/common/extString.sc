+ String {
	cleanAlphaNum { 
		var cleanName = this.collect { |char| if (char.isAlphaNum, char, $_) };
		^cleanName;
	}
}