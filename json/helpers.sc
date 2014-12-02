+ String {
	parseJson {
		^this.parseYAML;
//		^Json.parseString(this);
	}
}

/*+ IdentityDictionary {
	asJson {
		^ Json.encodeObject(this);
	}
}

+ SequenceableCollection {
	asJson {
		^ Json.encodeObject(this);
	}
}
*/