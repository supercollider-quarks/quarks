+ TaskProxy { 
	paused { ^this.isPaused }
}
+ GamePad { 
		// likely not needed anymore - 

	*putPdef { |pos, name, map| 
		var ctLoop; 
		ctLoop = CtLoop(pos, map).rescaled_(true)
			.dontRescale([\midL, \midR, \lHat, \rHat]); 
		
		space.put(pos, (
			name: name, 
			proxy: Pdef(name),
			ctLoop: ctLoop, 
			map: map, 
			meta: false, 
			recNames: [\joyRY, \joyLX, \joyRX, \joyLY, \midL, \midR, \lHat, \rHat] 
		));
	}	

	*putTdef { |pos, name, map| 

		var ctLoop; 
		ctLoop = CtLoop(pos, map).rescaled_(true)
			.dontRescale([\midL, \midR, \lHat, \rHat]); 
		
		space.put(pos, (
			name: name, 
			proxy: Tdef(name),
			ctLoop: ctLoop, 
			map: map, 
			meta: false, 
			recNames: [\joyRY, \joyLX, \joyRX, \joyLY, \midL, \midR, \lHat, \rHat] 
		));
	}	
}