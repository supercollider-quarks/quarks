// reads a data network log and plays it

SWDataNetworkLog{
	//	var <>recTask;

	var <network;

	var <reader;

	var <playnodes;
	var playTask;

	var <timeMap;
	var <curTime=0;
	var <deltaT=0;

	var <fileClass;
	var <hasStamp = false;
	var <hasExtraTab = false;

	*new{ |fn,network|
		^super.new.init( fn,network );
	}

	init{ |fn,netw|
		network = netw ? SWDataNetwork.new;
		this.checkFileClass( fn );
		this.open( fn );
	}

	checkFileClass{ |fn|
		var tar,txt;
		var path = PathName(fn);
		tar = (path.extension == "tar");
		txt = (path.extension == "txt");
		if ( tar ){
			fileClass = MultiFilePlayer;
		}{
			if ( txt ){
				fileClass = TabFilePlayer;
			}{
				fileClass = MultiFilePlayer;
			}
		};
		//		[tar, txt, fileClass].postln;
	}

	open{ |fn|
		if ( playTask.notNil ){ playTask.stop; };
		if ( reader.notNil ){ reader.close; };

		reader = fileClass.new( fn );
		//fn.postcs;
		//reader.dump;

		this.readHeader;

		playTask = Task{
			var dt = 0;
			while( { dt.notNil }, {
				dt.wait;
				dt = this.readLine;
			});
		};
		// timeMap maps the time elapsed to the line number in the file
		//		timeMap = Order.new;
		//		timeMap.put( 0, 1 );

	}

	goToTime{ |newtime|
		var line,oldid;
		if ( deltaT == 0 ){
			deltaT = this.readLine;
		};
		line = floor( newtime / deltaT );
		curTime = line * deltaT;
		// assuming dt is constant.

		if ( fileClass == MultiFilePlayer ){
			oldid = reader.curid;
			reader.goToLine( line.asInteger );
			// header may have changed:
			if ( oldid != reader.curid ){
				this.readHeader;
			};
		}{
			reader.goToLine( line.asInteger );
		};
	}

	play{
		playTask.start;
	}

	pause{
		playTask.pause;
	}

	resume{
		playTask.resume;
	}

	stop{
		playTask.stop;
		this.reset;
	}

	reset{
		curTime = 0;
		reader.reset;
		this.readHeader;
		playTask.reset;
	}

	close{
		playTask.stop;
		reader.close;
	}


	readHeader{
		var spec,playset,playids;
		var playslots;
		var header;

		playnodes = Dictionary.new;

		header = reader.readHeader(hs:2);
		spec = header[0].last;
		if ( spec.notNil, {
			// network.setSpec( spec );
			// if spec was not local, it may be included in the tar-ball
			// if ( network.spec.isNil ){
			if ( reader.tarBundle ){
				reader.extractFromTar( spec ++ ".spec" );
			};
			network.spec.fromFileName( reader.pathDir +/+ reader.fileName +/+ spec );
			// };
		});

		playslots = header[1].drop(1).collect{ |it| it.interpret };

		if ( fileClass == TabFilePlayer ){
			// backwards compatibility (there was an extra tab written at the end)
			playslots = playslots.drop(-1);
			hasExtraTab = true;
		};

		if ( playslots.first == "time" ){
			// date stamps in the first column:
			playslots.drop(1);
			hasStamp = true;
		};

		playset = Set.new;
		playids = playslots.collect{ |it| it.first }.do{
			|it,i| playset.add( it );
		};
		playset.do{ |it|
			network.addExpected( it );
			playnodes.put( it, Array.new )
		};
		playids.do{ |it,i|
			playnodes.put( it, playnodes[it].add( i ) )
		};
	}

	readLine{ |update=true|
		var dt,line,data,nd;
		var oldid;
		if ( hasExtraTab ){
			line = reader.nextInterpret.drop(-1);
		}{
			oldid = reader.curid;
			line = reader.nextInterpret;
			//	line.postcs;
			// header may have changed:
			if ( oldid != reader.curid ){
				this.readHeader;
			};
		};
		if ( line.isNil ){
			"At end of data".postln;
			^nil;
		};
		if ( hasStamp ){
			nd = 2;
			dt = line[1];
		}{
			nd = 1;
			dt = line.first;
		};
		if ( update ){
			//	data = line.drop( nd );
			playnodes.keysValuesDo{ |key,it|
				network.setData( key, line.at( it + nd ) );
			};
		};
		if( dt.notNil ){
			deltaT = dt;
			curTime = curTime + dt;
		};
		^dt;
	}
}