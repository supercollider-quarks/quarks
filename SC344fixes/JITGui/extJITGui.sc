+ JITGui {
	
		// make a name view when not in its own window - tbd. 
		// take out line from zone width ... 
	makeNameView { 
		
	}
		// to simplify the checkUpdate methods
	updateFunc { |newState, key, func| 
		var val = newState[key];
		if (val != prevState[key]) { func.value(val) }
	}

	updateVal { |newState, key, guiThing| 
		var val = newState[key];
		if (val != prevState[key]) { guiThing.value_(val) }
	}
	
	updateBinVal { |newState, key, guiThing| 
		var val = newState[key];
		if (val != prevState[key]) { guiThing.value_(val.binaryValue) }
	}
}