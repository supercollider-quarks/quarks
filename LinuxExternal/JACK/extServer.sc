+ Server{

	meterbridge{ |io=\inout,type="dpm"|
		if ( io == \out ){
			^SCJMeterBridge.new( (0..(this.options.numOutputBusChannels-1)), type, io: io );
		};
		if ( io == \in ){
			^SCJMeterBridge.new( (0..(this.options.numInputBusChannels-1)), type, io: io );
		};
		if ( io == \inout ){
			SCJMeterBridge.new( 
				 [ (0..(this.options.numOutputBusChannels-1)),
					 (0..(this.options.numInputBusChannels-1)) ]
					 , type, io: io );
		};
	}

}