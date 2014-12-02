// dot.sc - (c) rohan drape, 2004-2007

// Write the UGen graph at SynthDef to a file using the Dot graph
// language.  See: Eleftherios Koutsofios and Steven North ``Drawing
// Graphs with Dot'' AT&T Technical Report #910904-59113-08TM, 1991

+ ControlName {
	dot {
		arg file;
		file.write("CONTROL_" ++ this.index.asString ++ " ");
		file.write("[shape=trapezium,color=");
		file.write(this.rate.rateNumber.rateNumberToColor);
		file.write(",label=\"" ++ this.name.asString ++ ":");
		file.write(this.defaultValue.asString);
		file.write("\"];\n");
	}
}

+ Control {
	dot {
		arg file;
	}
}

+ UGen {
	dot {
		arg file;
		file.write("UGEN_" ++ this.synthIndex.asString ++ " ");
		file.write("[shape=record,color=" ++ this.rateNumber.rateNumberToColor);
		file.write(",label=\"{{" ++ this.displayName.asString);
		if(this.inputs.size != 0, 
			{ this.inputs.do({ 
				arg i, n;
				var labelStr = "";
				if(DotViewer.drawInputName, {
					labelStr = this.argNameForInputAt(n) ++ ": ";
				}, {
					labelStr = "";
				});
				if(i.isNumber, {
					labelStr = labelStr ++ i.asString;
				});
				file.write("|<IN_" ++ n.asString ++ "> " ++ labelStr); }) });
		file.write("}");
		if(	this.numOutputs != 0,
			{ file.write("|{");
				this.numOutputs.do({ 
					arg o;
					file.write("<OUT_" ++ o.asString ++ ">");
					if(o<(this.numOutputs - 1), {file.write("|");});
				});
				file.write("}"); });
		file.write("}\"];\n"); 
	}
}

+ SynthDef {
	dot { 
		arg file;		
		file.write("digraph \"" ++ this.name ++ "\" {\n");
		this.allControlNames.do({
			arg c;
			c.dot(file);
		});
		this.children.do({ 
			arg ugen;
			ugen.dot(file);
		});
		this.children.do({ 
			arg ugen;
			ugen.inputs.do({ 
				arg input, index;
				input.dotEdge(file,ugen,index);
			});
		});
		file.write("}\n");
	}
}

