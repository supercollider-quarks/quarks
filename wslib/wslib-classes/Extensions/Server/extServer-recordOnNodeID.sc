// wslib 2006

+ Server {
	recordOnNodeID { |nodeID|
		recordBuf.isNil.if({"Please execute Server-prepareForRecord before recording".warn; }, {
			recordNode.isNil.if({
				recordNode = Synth_ID.tail(RootNode(this), "server-record", [\bufnum, 
					recordBuf.bufnum], nodeID );
			}, { recordNode.run(true) });
			"Recording".postln;
		});
	}
	
	}