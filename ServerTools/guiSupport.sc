
/*
	experimental but can later be moved into Quark later
	as they are useful tools for checking dependencies
	
	main problem: doesn't catch class references inside { } blocks yet
*/

+ Class {

	referencesClasses {
		var package = this.package;
		var refklasses = IdentitySet.new;
		(this.class.methods ++ this.methods).do { arg meth;
			if(meth.package == package,{
				refklasses.addAll(meth.referencesClasses)
			})
		};
		^refklasses
	}
	referencesTo { // slow !
		^Class.allClasses.select({ arg class;
			class.referencesClasses.includes(this)
		})
	}
}


+ Method {

	referencesClasses {
		var selectors,refklasses;
		refklasses = [];
		selectors = this.selectors;
		if(selectors.notNil,{
			refklasses = selectors.asArray.select({ arg lit;
				var klass;
				if(lit.isKindOf(Symbol) and: { lit.isClassName },{
					klass = lit.asClass;
					klass.notNil
				},{
					false
				})
			});
			^refklasses.as(IdentitySet).collect(_.asClass)
		});
		^refklasses
	}
}


+ Quark {

	referencesPackages {
		^Quark.referencesPackages(this.name)
	}
	*referencesPackages { arg quarkName;
		var p,q,all;
		q = Quark.find(quarkName.asString);
		all = IdentitySet.new;
		q.definesClasses.do { arg cl;
			all.addAll( cl.referencesClasses )
		};
		q.definesExtensionMethods.do { arg m;
			all.addAll( m.referencesClasses )
		};
		p = all.collect(_.package).as(IdentitySet);
		p.remove(quarkName.asSymbol);
		^p.as(Array)
	}
	packageRoot {
		^Quarks.local.path +/+ this.path
	}
	*makeHelpDocs { arg quarkName;
		/*
			save template docs for all classes that don't have docs yet.
			HelpSource/Classes must exist
			does not overwrite existing
			protip: finish writing your classes first
		*/
		var q,dir;
		q = Quark.find(quarkName.asString);
		dir = q.packageRoot +/+ "HelpSource" +/+ "Classes";
		if(File.exists(dir).not,{
			File.mkdir(dir)
		});
		q.definesClasses.do { arg klass;
			var path;
			path = dir +/+ (klass.name.asString ++ ".schelp");
			if(File.exists(path).not,{
				("Creating " + path).inform;
				SCDoc.makeClassTemplate(klass.name).write(path);
			},{
				(path + "already exists").inform
			});
		};
	}
}

