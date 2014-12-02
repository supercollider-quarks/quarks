//redFrik

//related:
//RedEffectModule RedInstrumentModule RedAbstractMix

RedAbstractModule {								//abstract class
	classvar <all;
	var <>group, <cvs, <args, <defaultAddAction;
	var prDef;
	*new {|out= 0, group, defaultAddAction= \addToHead|
		^super.new.initRedAbstract(out, group, defaultAddAction);
	}
	*initClass {
		all= [];
	}
	initRedAbstract {|argOut, argGroup, argDefaultAddAction|
		var server;
		group= argGroup ?? {Server.default.defaultGroup};
		server= group.server;
		defaultAddAction= argDefaultAddAction;
		
		//--create cvs and argument array
		cvs= ();
		args= [];
		this.def.metadata[\order].do{|x|
			var spec= this.def.metadata[\specs][x.key];
			var cv= CV.new.spec_(spec);
			if(x.key==\out and:{argOut!=0}, {
				spec= spec.copy;
				spec.default= argOut;
				cv.value= argOut;
			});
			cvs.put(x.value, cv);
			args= args.addAll([x.key, cv]);
		};
		
		forkIfNeeded{
			server.bootSync;
			
			//--send definition
			this.def.add;
			server.sync;
			
			//--create synth
			this.prepareForPlay(server);
			server.sync;
			all= all.add(this);
		};
	}
	defaults {cvs.do{|cv| cv.value= cv.spec.default}}
	out {^this.cvFromControlName(\out).value}
	out_ {|index| this.cvFromControlName(\out).value= index}
	cvFromControlName {|name| ^cvs[this.def.metadata[\order].detect{|x| x.key==name}.value]}
	def {^prDef ?? {prDef= this.class.def}}
	
	//--for subclasses
	*def {^this.subclassResponsibility(thisMethod)}
	prepareForPlay {|server| ^this.subclassResponsibility(thisMethod)}
	free {^this.subclassResponsibility(thisMethod)}
	gui {|parent, position| ^this.subclassResponsibility(thisMethod)}
}
