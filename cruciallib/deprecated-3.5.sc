

MultiPageLayout : PageLayout { 

	*new { arg ... args;
		this.deprecated(thisMethod);
		^PageLayout(*args)
	}
}


// move these to main pending-deprecated-3.6.sc

+ PageLayout {
	
	layRight { arg w,h; 
		this.deprecated(thisMethod)
		^Rect(0,0,w,h) 
	}
}


// these were a bad idea since bounds and layout could easily be swapped
// and this would hide the error by treating the point/rect as a layout

+ Point {
	asPageLayout {
		^PageLayout("",this.asRect ).front
	}
}

+ Rect {
	asPageLayout {
		^PageLayout("",this ).front
	}
}

+ Instr {

	// this is a tilda delimited version of the name
	asSingleName {
		this.deprecated(thisMethod);
		^String.streamContents({ arg s;
			name.do({ arg n,i;
				if(i > 0,{ s << $~ });
				s << n;
			})
		})
	}
	*singleNameAsNames { arg singleName;
		this.deprecated(thisMethod);
		^singleName.asString.split($~).collect({ arg n; n.asSymbol })
	}
}


+ Object {
	die { arg ... culprits;
		"\n\nFATAL ERROR:\n".postln;
		culprits.do({ arg c; if(c.isString,{c.postln},{c.dump}) });
		this.dump;
		Error("FATAL ERROR").throw;
	}
	checkKind { arg shouldBeKindOf;
		if(this.isKindOf(shouldBeKindOf).not,{
			Error("Type check failed:" + this + "should be kind of:" + shouldBeKindOf).throw;
		})
	}
}


