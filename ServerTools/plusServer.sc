
+ Server {

	async { arg msg, replyCmd, callback, timeout=3;

		var resp,done=false;
		if(this.serverRunning.not,{
			(this.asString + "is not running" + thisMethod).inform;
			^this
		});
			
		resp = OSCresponderNode(this.addr, replyCmd, { arg time, responder, msg;
				done = true;
				responder.remove;
				{callback.value(msg,time);}.defer;
			  }).add;
		
		this.listSendMsg(msg);
		{
			resp.remove;
			if(done.not) {
				("Server failed to respond to " + msg + "with" + replyCmd).warn;
			}
		}.defer(timeout)
	}
	
	getQueryTree { arg callback;

		this.async(["/g_queryTree", 0, 1],'/g_queryTree.reply',{ arg msg;
			
			var i = 1, synthControls = false, parseNode,suck;
		
			suck = {
				var v;
				v = msg[i];
				i = i + 1;
				v
			};
			
			synthControls = suck.value() == 1;
			
			parseNode = {
				var data;
				data = ();
				data.id = suck.value();
				data.numChildren = suck.value();
				if(data.numChildren == -1,{
					data.nodeType = Synth;
					data.defName = suck.value();
					if(synthControls,{
						data.numControls = suck.value();
						data.controls = Dictionary.new;
						data.numControls.do {
							data.controls[ suck.value() ] = suck.value();
						};
					});
				},{
					data.nodeType = Group;
					data.children = Array.fill(data.numChildren, { arg i; parseNode.value() });
				});
				data
			};	
			callback.value( parseNode.value() )
		
		})
	}
	queryNode { arg nodeID,callback;
		this.getQueryTree({ arg tree; this.prSearchForNode(nodeID,callback,tree); });
	}
	prSearchForNode { arg nodeID,callback,tree;
		if(tree['id'] == nodeID,{
			^callback.value(tree)
		});
		tree['children'].do { arg leaf;
		this.prSearchForNode(nodeID,callback,leaf) !? {^nil};
		};
		^nil
	}	
}


