
var k = { arg klass; klass.asSymbol.asClass };


(
	allClasses: { arg reply;
		reply.value(Class.allClasses.collect(_.asString))
	},
	// better to wait for json support and fetch everything as a data structure

	hasHelpFile: { arg reply, klass;
		// always returns true because it auto-generates one
		reply.value( k.value(klass).hasHelpFile )
	},
	helpFilePath: { arg reply, klass;
		var path;
		path = k.value(klass).helpFilePath;
		if(path.notNil, {
			path = path.copyToEnd(7); // remove file://
		});
		reply.value( path )
	},
	helpFile: { arg reply, klass;
		var path, body;
		klass = k.value(klass);
		{
			path = klass.helpFilePath;
			if(path.notNil,{
				path = path.copyToEnd(7); // remove file://
				body = File.open(path, "r").readAllString();
				reply.value( body );
			},{
				reply.value( nil );
			})
		}.defer
	},
	subclasses: { arg reply, klass;
		reply.value( k.value(klass).subclasses.collect(_.asString) )
	},
	allSubclasses: { arg reply, klass;
		reply.value( k.value(klass).allSubclasses.collect(_.asString) )
	}

);

