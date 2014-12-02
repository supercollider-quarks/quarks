
API - SuperCollider API framework
=================================

An API is a way to encapsulate all of the publically callable functions for a single application, composition, piece, class, service etc. in one universally accessible place.

Each API object has a name (eg. "server") and a dictionary of functions.

These can then be called:

in SuperCollider code

	API.async("server.boot", ["default"], {
	    // booted now
	    API.async("group.new", [], { arg groupID;
	        // use this group
	    })
	});

over OSC (simple call of functions)

	API("server").mountOSC;

	// send OSC message to the language on port 57120
	// and it will boot the server process
	// '/API/server/boot' "default"

via the duplex OSC interface (call and response)

	API.mountDuplexOSC;

	// send osc message:
	// '/API/call' "group.new"
	// reply will be sent back to the caller returning the node ID
	// '/API/reply' 1001

Using a client library like supercollider.js

	// supercollider
	API.mountDuplexOSC;

	// start the webserver
	node api_server.js

	// point browser at http://localhost:4040

	// javascript in a browser or mobile device
	sc = new SCApi("localhost",4040);
	sc.call("server.boot", ["default"], function() {
	    sc.call("group.new", [], function(groupID){
	        // spawn synths into this group
	    });
	});

supercollider.js provides a Node.js based api_server through which you can easily communicate with SuperCollider from javascript either on the server (running under Node) or in a browser (communicating back to Node with websockets).

It uses socket.io which is loads of fun and should prove quite useful for installations and pieces that allow many people to interact using mobile phones over normal webrowsers.

https://github.com/crucialfelix/supercolliderjs

Defining APIs
=============

Each Quark can provide a folder called apis/ containing API handler dictionaries.
Post all currently implemented API paths:

	API.loadAll.allPaths.do(_.postln);

You can easily write APIs for your own application just by putting a file containing a dictionary of handlers in:

	// {yourquark}/apis/{apiname}.api.scd
	(
	    hello: { arg reply;
	        reply.value("hello back")
	    },
	    lookup: { arg reply, query;
	        var result = ();
	        {
	            // let me think about it
	            // search lots of files for something to do with "query"
	            result['path'] = "some/path";
	            result['things'] = [1,2,3];
	            result['quantity'] = 4;
	            result['random'] = 1024.rand;
	            // the dictionary will be sent as JSON
	            // and available in the browser as a JavaScript object
	            reply.value( result );
	        }.fork
	    }
	);

Your quark is now on the internets.

	// javascript
	sc.call("apiname.lookup",["a query string"], function(result) {
	    console.log(result);
	    console.log(result.random);
	    result.things; // [1,2,3]
	});


Included APIs
=============

So far:

	group.head
	group.free
	group.tail
	group.new
	instr.list
	instr.play
	instr.head
	instr.detail
	instr.listBySpec
	instr.addSynthDesc
	instr.after
	instr.loadAll
	instr.tail
	instr.before
	instr.replace
	class.allClasses
	class.subclasses
	class.helpFile
	class.helpFilePath
	class.hasHelpFile
	class.allSubclasses
	API.apis
	API.paths
	interpreter.interpret
	interpreter.play
	interpreter.executeFile
	server.freeAll
	server.boot
	server.sendMsg
	server.quit
	server.nextNodeID
	server.isRunning
	synth.head
	synth.release
	synth.grain
	synth.free
	synth.tail
	synth.new
	synth.get
	synth.set
	api.apis
	api.paths
	synthdef.remove
	synthdef.add

supercollider.js includes a web app that browses the API and let's you experiment directly with it.

![api browser](https://github.com/crucialfelix/supercolliderjs/blob/master/examples/images/index-screenshot.png?raw=true)

