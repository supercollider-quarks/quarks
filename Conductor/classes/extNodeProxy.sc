

+NodeProxy {
	set { arg ... args; // pairs of keys or indices and value
		nodeMap.set(*args);
		if(this.isPlaying) { 
			server.sendBundle(server.latency, [15, group.nodeID] ++ args); 
		};
		this.changed(\set, args);
	}
	
	prset { arg ... args; // pairs of keys or indices and value
		nodeMap.set(*args);
		if(this.isPlaying) { 
			server.sendBundle(server.latency, [15, group.nodeID] ++ args); 
		};
	}

	prSetControls { | kvArray | 
		kvArray.buildCVConnections({ | label, expr| this.prset(label, expr.value)})
	}

	conduct { | func | 
		var topW, keys, con, np = this;
		con = Conductor.make { | con | 
			con.useMIDI;
			con.nodeProxy_(np);
			con[\np] = np;
			con[\npControl] = SimpleController(np).put(\set,
				{ | obj, cmd, kV | 
					var topW, cv;
					cv = con[kV[0] ];
					if (cv.isNil) { 
						kV = con.addCVs(kV);
						topW = Document.current;
						con.gui.resize; 
						if (topW.notNil) { topW.front };
					} {
						cv.value = kV[1];
					}
//					np.prSetControls(kV);
//
				});
			
			// add any keys already set in NodeProxy
			keys = np.nodeMap.settings.keys.asArray;
			keys = keys.select { | k | #[i_out, out, in, fin].includes(k).not };
			keys.do { | k |
				this.prSetControls(con.addCVs([k, nodeMap.settings[k].value]) )
			};
			
		};
		con.make(func ? {});
		topW = Document.current;
		con.show;
		defer({ topW.front }, 0.025);
	
		^con;
	}
		
}

/*

s.boot;

a = NodeProxy.audio(s, 2);

b = a.conduct;
a.set(\freq, rrand(900, 300));

a.play; // play to hardware output, return a group with synths
a.source = { arg freq=400; SinOsc.ar(freq * [1,1.2] * rrand(0.9, 1.1), 0, 0.1) };



*/

