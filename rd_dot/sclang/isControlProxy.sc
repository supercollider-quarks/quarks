// isControlProxy - (c) rohan drape, 2004-2007

+ Object {
	isControlProxy {
		^false;
	}
}

+ OutputProxy {
	isControlProxy {
		^this.source.isKindOf(Control);
	}
}
