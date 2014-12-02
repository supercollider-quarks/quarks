+ SpatialRender {


	setAudioOutput { |lastSpatialTechnique|

		{

			// 1. disconnect previous connections

			switch (lastSpatialTechnique)

			{\ambisonics} {
				// this.connectSC_AmbDec(\disconnect); // redundant!!
				"killall -9 ambdec".unixCmd;
			}

			{\vbap} {
				this.disconnectSC_System;
			}

			{\binaural} {
				this.disconnectSC_System;
			}

			{this.disconnectSC_System};

			// 2. create new connections


			switch (spatialTechnique)

			{\ambisonics} {
				AmbDec.new;
				this.connectSC_AmbDec;
			}

			{\vbap} {
				this.connectSC_System_VBAP;
			}

			{\binaural} {
				this.connectSC_System_2;
			}

		}.defer(1); // wait for the server in case!


	}

	connectSC_System_2 {
		// binaural, connect from 2 first outputs
		"jack_connect SuperCollider:out_1 system:playback_1".systemCmd;
		"jack_connect SuperCollider:out_2 system:playback_2".systemCmd;
	}

	connectSC_System_VBAP {

		vbapNumSpeakers.do { |i|
			var index = (i+1).asString;
			("jack_connect SuperCollider:out_" ++ index ++ " system:playback_" ++ index).systemCmd;
		}
	}

	disconnectSC_System {
		// get all connections of supercollider-system and disconnect them

		var pipe = Pipe.new("jack_lsp -c SuperCollider", "r");
		var line = pipe.getLine;
		var lastLine;

		while({line.notNil}, {
			if (line.contains("SuperCollider").not) {
				var cmd;
				cmd = "jack_disconnect " ++ lastLine ++ " " ++ line;
				cmd.systemCmd;
			};

			lastLine = line;
			line = pipe.getLine;

		});
		pipe.close;
	}

	connectSC_AmbDec { |action=\connect|
		var sc = "SuperCollider:out_";
		var ambdec = " ambdec:";
		var jack;

		// ACN convention
		var ambdecPorts = ["0w","1y","1z","1x","2v","2t","2r","2s","2u","3q","3o","3m","3k","3l","3n","3p"];
		// standard convention
		// var ambdecPorts = ["0w","1x","1y","1z","2r","2s","2t","2u","2v","3k","3l","3m","3n","3o","3p","3q"];
		ambdecPorts = ambdecPorts.collect{|port| "in."++port};

		jack = switch (action)
		{\connect} {"jack_connect "}
		{\disconnect} {"jack_disconnect "};

		16.do{ |i|
			(jack ++ sc ++ (i+1).asString ++ ambdec ++ ambdecPorts[i]).systemCmd;
		}
	}
}