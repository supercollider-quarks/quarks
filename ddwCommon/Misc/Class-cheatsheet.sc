+ Class {
	cheatsheet {
			// show variables and methods for this class
			// default setting is to show only methods, but this can be overridden
		arg showVars = false, showMethods = true;

		var namesTemp, methodViewerFunc;

		(this.name.asString).post;
		this.superclasses.do({ arg c;
			(" : " ++ c.name).post;
		});
		"\n".post;
		showVars.if({
			"\tClass variables:".postln;
			this.classVarNames.do({ arg n; ("\t\t" ++ n).postln; });
			
			"\tInstance variables:".postln;
			this.instVarNames.do({ arg n; ("\t\t" ++ n).postln; });
		});
		
		showMethods.if({ 
			methodViewerFunc = {
				arg m, prefix = "";
				("\t\t" ++ prefix ++ m.name).post;
				
				m.argNames.isNil.not.if({
					namesTemp = m.argNames.copy;
					namesTemp.removeAt(0);
					(namesTemp.size > 0).if({ "(".post; });
					namesTemp.do({
						arg n, i;
						(n.asString ++ (i < (namesTemp.size-1)).if({ ", " },  { ")" })).post;
					});
				});
				"\n".post;
			};
			
			"\tMethods:".postln;
			this.class.methods.do({
				arg m; methodViewerFunc.value(m, "*");
			});
			
			this.methods.do({
				arg m; methodViewerFunc.value(m);
			});
		});	// showMethods.if
	}
	
	cheatTree {
		arg showVars = false, showMethods = false;
		var names;
		this.cheatsheet(showVars, showMethods);
		names = this.subclasses.collect({ arg a; a.name });
		names.notNil.if({ 
			names = names.sort;
			names.do({ arg b; b.asClass.cheatTree(showVars, showMethods) });
		});
	}
	
	cheatList {	// show all subclasses of this class sorted in alpha order (not tree order)
		var list, listCollector;
			// recursive function to collect class objects
		listCollector = { arg node, l;
			l.add(node);
			node.subclasses.do({ arg n; listCollector.value(n, l) });
		};
		list = List.new;
		listCollector.value(this, list);	// do the recursion
		list.sort({ arg a, b; a.name < b.name })	// sort it
			.do({ arg n;		// and iterate to post the class names (w/ supers)
			n.name.post;
			n.superclasses.do({ arg s; (" : " ++ s.name).post; });
			"\n".post;
		});
		("\n" ++ list.size.asString ++ " classes listed.").postln;
	}
}