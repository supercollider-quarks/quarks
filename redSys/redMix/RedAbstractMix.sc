//redFrik 090116

//--related:
//RedMixGUI RedAbstractMod RedMatrixMixer RedTapTempo

RedAbstractMix {									//abstract class
	var <group, <cvs, <args,
		<isReady= false, synth;
	*new {|inA= 0, inB= 2, out= 0, group, lag= 0.05|
		^super.new.initRedAbstractMix(inA, inB, out, group, lag);
	}
	initRedAbstractMix {|argInA, argInB, argOut, argGroup, argLag|
		var server;
		group= argGroup ?? {Server.default.defaultGroup};
		server= group.server;
		
		//--create cvs and argument array
		cvs= ();
		this.def.metadata[\specs].keysValuesDo{|k, v|
			cvs.put(k, CV.new.spec_(v));
		};
		cvs.inA.value= argInA;
		cvs.inB.value= argInB;
		cvs.out.value= argOut;
		cvs.lag.value= argLag;
		args= [
			\inA, cvs.inA,
			\inB, cvs.inB,
			\out, cvs.out,
			\mix, cvs.mix,
			\amp, cvs.amp,
			\lag, cvs.lag
		];
		
		forkIfNeeded{
			server.bootSync;
			
			//--send definition
			this.def.add;
			server.sync;
			
			//--create synth
			synth= Synth.controls(this.def.name, args, group, \addToTail);
			server.sync;
			isReady= true;
		};
	}
	def {^this.class.def}
	free {
		synth.free;
	}
	gui {|parent, position|
		^RedMixGUI(this, parent, position);
	}
	
	//--for subclasses
	*def {
		^this.subclassResponsibility(thisMethod);
	}
}
