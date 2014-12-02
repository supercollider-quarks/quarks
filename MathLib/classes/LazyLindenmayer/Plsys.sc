	// jrh 2006
Plsys : Pattern {
	var <>pattern, <>rules, <>level, <>contextSize;
	
	*new { arg pattern, rules, level=1, contextSize;
		^super.newCopyArgs(pattern, rules, level, contextSize)
	}
	
	asStream {
		^pattern.asStream.rewriteString(rules, level, contextSize);
	}

}

