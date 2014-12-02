// dotEdge.sc - (c) rohan drape, 2004-2007

// Generate the line of Dot code that describes an edge in a UGen
// graph.

+ Object {
	dotEdge {
		arg file, ugen, index;
	}
}

+ UGen {
	dotEdge {
		arg file, ugen, index;
		if(this.isControlProxy, 
			{ file.write("CONTROL_" ++ this.controlIndex.asString); }, 
			{ file.write("UGEN_" ++ this.synthIndex.asString ++ ":OUT_");
				if(this.isKindOf(OutputProxy),
					{ file.write(this.outputIndex.asString); },
					{ file.write("0"); }); });
		file.write(" -> UGEN_" ++ ugen.synthIndex.asString);
		file.write(":IN_" ++ index.asString ++ " ");
		file.write("[color=" ++ this.rateNumber.rateNumberToColor ++ "];\n"); 
	}
}
