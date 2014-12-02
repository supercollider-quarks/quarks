//redFrik

//--todo:
//global lag?

//--related:
//RedEffectsRackGUI RedEffectModule RedAbstractMix

RedEffectsRack {
	classvar <>defaultClasses;
	var <group, <cvs, <isReady= false, groupPassedIn,
		<efxs;
	*new {|efxClasses, out= 0, group|
		^super.new.initRedEffectsRack(efxClasses, out, group);
	}
	*initClass {
		defaultClasses= [RedEfxRing, RedEfxTape, RedEfxComb, RedEfxDist, RedEfxTank, RedEfxComp];
	}
	initRedEffectsRack {|efxClasses, argOut, argGroup|
		var server;
		if(efxClasses.isNil or:{efxClasses.isEmpty}, {
			(this.class.name++": efxClasses argument empty so using the default classes").inform;
			efxClasses= defaultClasses;
		});
		
		if(argGroup.notNil, {
			server= argGroup.server;
			groupPassedIn= true;
		}, {
			server= Server.default;
			groupPassedIn= false;
		});
		
		forkIfNeeded{
			if(groupPassedIn.not, {					//boot server and create group
				server.bootSync;
				group= Group.after(server.defaultGroup);
				server.sync;
				CmdPeriod.doOnce({group.free});
			}, {
				group= argGroup;
			});
			
			//--create efxs
			efxs= efxClasses.collect{|x| x.new(argOut, group)};
			
			//--one bus that controlls them all
			cvs= (
				\out: CV.new.sp(argOut, 0, server.options.numAudioBusChannels-1, 1, \lin)
			);
			cvs.out.action= {|cv| efxs.do{|x| x.cvFromControlName(\out).value= cv.value}};
			
			//--add all efx cvs to this cvs
			efxs.do{|x|
				x.cvs.keysValuesDo{|k, v|
					var suffix= 1;
					if(v!=x.cvFromControlName(\out), {//skip bus cvs. controlled by the one
						
						//--add suffix if >1 of the same efx class
						if(cvs.at(k).notNil, {
							cvs.keysValuesDo{|kk, vv|
								if(kk.asString.contains(k.asString), {
									if(suffix<=kk.asString.split($_).last.asInteger, {
										suffix= kk.asString.split($_).last.asInteger+1;
									});
								});
							};
							(this.class.name++": added suffix _"++suffix+"to cvs key"+k).inform;
							cvs.put(k++"_"++suffix, v);
						}, {
							cvs.put(k, v);
						});
					});
				};
			};
			isReady= true;
		};
	}
	free {
		efxs.do{|x| x.free};
		if(groupPassedIn.not, {group.free});
	}
	out {
		^cvs.out;
	}
	defaults {
		cvs.do{|cv| cv.value= cv.spec.default};
	}
	gui {|position|	//parent here???
		^RedEffectsRackGUI(this, position);
	}
	makeView {|parent|
		//todo
		//^RedEffectsRackGUI.view(this)?? embed in view later
	}
}
/*
a= RedEffectsRack([RedEfxTank, RedEfxRing, RedEfxDist, RedEfxComb])
Pbind(\degree, Pseq([0, 0, 5, 4, 5, 6], inf)).play
a.efxs.do{|x| x.cvs.out.value.postln};""
a.cvs.out.value= 1
a.cvs
a.efxs
a.cvs.ringMix.input= 0.5
a.cvs.tankMix.input= 0.5
a.defaults

*/