UMapOut {
	
	*kr { |channelsArray, map = true|
		ReplaceOut.kr( 
			UMap.busOffset + \u_mapbus.ir(0), 
			if( map ) { \u_spec.asSpecMapKr( channelsArray ) } { channelsArray };
		);
		Udef.buildUdef.numChannels = channelsArray.asCollection.size;
		Udef.buildUdef.outputIsMapped = map;
		Udef.addBuildSpec(
			ArgSpec(\u_mapbus, 0, PositiveIntegerSpec(), true, \init ) 
		);
		Udef.addBuildSpec(
			ArgSpec(\u_spec, [0,1,\lin].asSpec, ControlSpecSpec(), true ) 
		);
	}
	
}