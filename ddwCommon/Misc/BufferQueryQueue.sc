
BufferQueryQueue {
	// called by Buffer-readAndQuery to do the work
	// if a file is being read and we're waiting for the b_info message from the server,
	// the new file request goes into a queue to execute when the first is finished
	
	classvar	queue,
			<isRunning = false,	// true if file(s) are being read
			<>timeToFail = 3.0;
	
	*init {	// also reset
		queue = Array.new;		// array elements will be args to Meta_Buffer-read2
		isRunning = false;
	}
	
	*clear { this.init }
	
	*add { arg ... args;	// all args from read2
		queue.isNil.if({ this.init });
		queue = queue.add(args);
		isRunning.not.if({ this.doQueue });	// isRunning==true means another file is in process
										// so don't interrupt
	}
	
	*doQueue {	// reads file and gets info on the first queue item
		var	server, path, startFrame, numFrames, completionFunc, buffer, timeout, resp,
			updater;
			// get arguments
		#server, path, startFrame, numFrames, completionFunc, buffer, timeout = queue.at(0);
		server.serverRunning.not.if({
			"Server must be booted before buffers may be allocated.\nFailed: %"
				.format(queue[0]).warn;
			queue.removeAt(0);		// drop first item from queue
			(queue.size > 0).if({
				this.doQueue	// still an item left? go back for that one
							// no need to clear this responder b/c it will be overwritten
			}, {
				isRunning = false;		// so I can start again with the next .add call
			});
			^this	// if server not booted, the next code block should be skipped
		});
		
		isRunning = true;
			// place OSCResponder -- note that flow of control happens here
		resp = OSCpathResponder(server.addr, ['/b_info', buffer.bufnum], { arg t, r, m;
			buffer.numFrames = m.at(2);
			buffer.numChannels = m.at(3);
			buffer.sampleRate = m.at(4);
			" done".postln;
				// must try here -- if completionFunc fails the queue gets stuck
			try {
				completionFunc.value(buffer);
			} { |error|
				error.reportError;
				"Error occurred during buffer load completionFunc. Continuing with next file."
					.warn;
			};
			queue.removeAt(0);		// drop first item from queue
			(queue.size > 0).if({
				if(server.serverRunning) {
					this.doQueue	// still an item left? go back for that one
							// no need to clear this responder b/c it will be overwritten
				} {
					updater = Updater(server, { |what|
						if(what == \serverRunning and: { server.serverRunning }) {
							updater.remove;
							this.doQueue;
						};
					});
				};
			}, {
				isRunning = false;		// so I can start again with the next .add call
			});
		}).add.removeWhenDone;
		Post << "Loading " << path << "[" << (startFrame ? 0) << ", " << (numFrames ? "")
			<< "]...";
			// start the ball
		buffer.allocRead(path, startFrame, numFrames, ["/b_query", buffer.bufnum]);
			// check for failure -- if failure, continue with next file
		SystemClock.schedAbs(Main.elapsedTime + (timeout ?? { timeToFail }), {
				// this is a valid test b/c readAndQuery doesn't have a sampleRate arg
			buffer.sampleRate.isNil.if({
				format("Buffer-readAndQuery for % failed. Continuing with next.", path).warn;
				resp.remove;	// otherwise old responders remain and break later file loads
				queue.removeAt(0);
				(queue.size > 0).if({
					this.doQueue	// still an item left? go back for that one
								// no need to clear this responder b/c it will be overwritten
				}, {
					isRunning = false;		// so I can start again with the next .add call
				});
			});
			nil
		})
	}
}
