
+ Buffer {
	*readAndQuery { arg server,path,startFrame = 0,numFrames = -1, completionFunc, timeout;
		var new, buf;
		server = server ? Server.local;
		new = super.newCopyArgs(server,
						buf = server.bufferAllocator.alloc(1),
						numFrames);
			// go do it!
		BufferQueryQueue.add(server, path, startFrame, numFrames, completionFunc, new, timeout);
		^new		// object won't be fully ready until queue finishes, but you can have it now
	}
}