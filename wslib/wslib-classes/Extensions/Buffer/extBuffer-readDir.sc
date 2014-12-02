+ Buffer { // read all files in directory to a dir
		
	*readMultiple {  |names, path, bufnumOffset, server, notify = true, action|
		var buffers, msgs;
		
		server = server ? Server.default;
		path = path.standardizePath.withTrailingSlash;
		
		msgs = names.collect({ |name, i|
			var buffer;
			buffer = Buffer(server, bufnum: bufnumOffset !? { bufnumOffset + i });
			buffers = buffers.add( buffer );
			buffer.cache; // extra cache needed here?
			buffer.allocReadMsg( (path ++ name), 0, -1, {|buf|["/b_query",buf.bufnum]});
			});
			
		if(notify) { buffers.do({ |buffer, i| 
			("\t" ++ buffer.bufnum ++ " : " ++ names[i] ).postln; }) }; 
		
		/*	
		{	var index = 0; // async loading action
			while { index < msgs.size }
					{ 	server.sendBundle(nil, *msgs[index..index+maxBundleSize] );
						index = index+maxBundleSize;
						server.sync;
					};
				
			if(notify) { "reading % buffers from folder '%/' done\n"
							.postf( names.size, path.basename ); };
			action.value;
		}.fork;
		*/
		
		{ 	server.sync( nil, msgs ); 
			if(notify) { "reading % buffers from folder '%/' done\n"
						.postf( names.size, path.basename ); };
			action.value( buffers ); 
		}.fork;
		
		^buffers;
		}
		
	*readDir { |path, bufnumOffset, ext ="wav", server, notify = true, nlevels = inf, action|
		var names;
		path = path.standardizePath.withTrailingSlash;
		names = path.getPathsInDirectory(ext.removeItems("."), nlevels);
		if(notify) { ("\nread " ++ names.size ++ " files to buffers:").postln;
			path.postln; };
		^Buffer.readMultiple(names, path, bufnumOffset, server, notify, action);
		}
		
	/*
		
	*readMultiple2 { |names, path, bufnumOffset, server, notify = true|
		path = path.standardizePath;
		^names.collect({ |item, i|
			var bufnum = nil, buffer;
			
			// if bufnumoffset is not nil, the buffers will load to a series
			// of bufnums starting at bufnum. Otherwise the bufnum will be assigned
			// automatically with .bufferAllocator .
			
			if(bufnumOffset.notNil) { bufnum = bufnumOffset + i };
			buffer = Buffer.read(server, path.standardizePath ++ item
				.standardizePath, bufnum: bufnum);
			
			if(notify) { ("\t" ++ buffer.bufnum ++ " : " ++ item).postln; };
			buffer;
			});
		}
		
	*readMultipleASync { |names, path, bufnumOffset, server, notify = true|
		// async version; works for 500+ buffers
		var buffers, routine;
		path = path.standardizePath;
		routine = Routine({
			buffers = names.collect({ |item, i|
				var bufnum = nil, buffer;
				
				// if bufnumoffset is not nil, the buffers will load to a series
				// of bufnums starting at bufnum. Otherwise the bufnum will be assigned
				// automatically with .bufferAllocator .
				
				if(bufnumOffset.notNil) { bufnum = bufnumOffset + i };
				buffer = Buffer.read(server, path.standardizePath ++ item
					.standardizePath, action: { routine.next }, bufnum: bufnum);
				if(notify) { ("\t" ++ buffer.bufnum ++ " : " ++ item).postln; };
				buffer.yield;
				buffer;
				});
			});
		routine.next;
		^buffers; // returns nil ..
		}
	
	*/
		
	}
