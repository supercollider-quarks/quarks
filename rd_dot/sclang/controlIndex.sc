// controlIndex.sc - (c) rohan drape, 2004-2007

+ OutputProxy {
	controlIndex { 
		var counter = 0, index = 0;
		this.synthDef.children.do({ 
			arg ugen;
			if(this.source.synthIndex == ugen.synthIndex, 
				{ index = counter + this.outputIndex; });
			if(ugen.isKindOf(Control), 
				{ counter = counter + ugen.channels.size; });
		});
		["OutputProxy.controlIndex", index].postln;
		^index;
	}
}
