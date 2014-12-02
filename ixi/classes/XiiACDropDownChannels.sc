// audio channel drop down 

XiiACDropDownChannels {	

	classvar <>numChannels;
	
	*new { 
		^super.new.initXiiACDropDownChannels;
	}
		
	*setChannels {arg channels;
		numChannels = channels;
	}
	
	*getChannels {
		^numChannels;
	}
		
	*getStereoChnList {
		var stereolist;
		stereolist = [];
		(numChannels/2).do({ arg i;
			stereolist = stereolist.add(((i*2).asString++","+((i*2)+1).asString)); 
		});
		//stereolist = stereolist.insert(4, "-");
		//stereolist = stereolist.insert(6, "-");
		^stereolist;
	}

	*getMonoChnList {
		var monolist;
		
		monolist = [];
		numChannels.do({ arg i;
			monolist = monolist.add(i.asString); 
		});
		//monolist = monolist.insert(8, "-");
		//monolist = monolist.insert(11, "-");
		^monolist;
	}
}

