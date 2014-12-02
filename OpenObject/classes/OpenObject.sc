
// talk to SuperCollider objects from the network via OpenSoundControl
// basic syntax: /oo objectName selector args ...
// Julian Rohrhuber and Fredrik Olofsson, 2009


OpenObject {

	classvar <>objects;
	classvar <responders;
	classvar <>lookup = false;
	classvar <>replyPort;

	*initClass {
		objects = ();
	}

	*put { |name, object|
		objects.put(name, object)
	}

	*keyFor { |object|
		^objects.findKeyForValue(object)
	}

	*remove { |object|
		var key = this.keyFor(object);
		key !? { objects.removeAt(key) }
	}

	*removeAt { |name|
		^objects.removeAt(name)
	}


	*start { |addr|

		if(NetAddr.langPort != 57120) {
			Error("SC started under a langPort other than 57120. Try quit and restart.").throw // newer versions of SC will not have this limitation.
		};
		if(this.isListening) { "OpenObject: already listening".warn; ^this };

		this.addResponder(addr, '/oo', { |msg| this.oscPerform(msg) });
		this.addResponder(addr, '/oo_k', { |msg| this.oscPerformKeyValuePairs(msg) });

	}

	*end {
		responders.do(_.remove);
		responders = nil;
	}

	*clear {
		objects = ();
	}

	*isListening {
		^responders.notNil
	}

	*openProxies {
		[Pdef, Pdefn, Tdef, Fdef, Ndef].do { |class| class.publish(class.name) };
		lookup = true;
	}

	// a dangerous tool for both good and evil ...

	*openInterpreter { |addr|
			("Networking opens interpreter - use 'openInterpreter' at your own risk"
			"\nuse 'closeInterpreter' to close interpreter. *").postln;
			this.addResponder(addr, '/oo_p', { |msg| this.setProxySource(msg) });
			this.addResponder(addr, '/oo_i', { |msg| this.interpretOSC(msg) });
	}

	// safe again.

	*closeInterpreter {
		this.removeResponder('/oo_i');
		this.removeResponder('/oo_p');
		this.removeResponder('/oor_i');
	}


	////////////// private implementation /////////////////

	*addResponder { |addr, cmd, func|
		responders = responders.add(
			OSCresponderNode(addr, cmd, { |t, r, msg, replyAddr|
				var res, id;
				// some type matching
				if(msg[1].isNumber) {
					// replyID name selector args ...
					res = func.value(msg[2..]);
					id = msg[1];
					this.sendReply(replyAddr, id, res);
				} {
					// name selector args ...
					func.value(msg[1..])
				}
			}).add;
		);
	}

	*removeResponder { |cmd|
		var all = responders.select { |resp| resp.cmdName == cmd };
		all.do { |resp|
			resp.remove;
			responders.remove(resp);
		}
	}

	// if lookup == true, use "name_key" lookup scheme

	*getObject { |name|
		var object, objectName, key;
		^objects.at(name) ?? {
			if(lookup) {
				#objectName ... key = name.asString.split($_);
				object = objects.at(objectName.asSymbol);
				if(object.isNil) { ^nil };
				object.at(key.join($_).asSymbol);
			}
		}
	}

	// name, selector, args ...

	*oscPerform { |msg|
			var name, selector, args, receiver;
			#name, selector ... args = msg;
			receiver = this.getObject(name);
			^if(receiver.isNil) {
				"OpenObject: name: % not found".format(name).warn;
				nil
			} {
				args = args.unfoldOSCArrays;
				receiver.performList(selector, args)
			}
	}

	// name, selector, argName1, val1, argName2, val2 ...

	*oscPerformKeyValuePairs { |msg|
			var name, selector, args, receiver;
			#name, selector ... args = msg;
			receiver = this.getObject(name);
			^if(receiver.isNil) {
				"OpenObject: name: % not found".format(name).warn;
				nil
			} {
				args = args.unfoldOSCArrays;
				receiver.performKeyValuePairs(selector, args)
			}
	}

	// name, sourceCode

	*setProxySource { |msg|

			/*msg.pairsDo { |name, string|
				var object, receiver;
				receiver = this.getObject(name);
				string.postcs;

				if(receiver.isNil) {
					"OpenObject: name: % not found".format(name).warn;
				} {
					object = string.asString.interpret;
					object !? { receiver.source_(object) };
				}
			};*/

			// for now, support only single sets

			var name = msg[0], string = msg[1..].join;
			var object, receiver, ok = 0;
			receiver = this.getObject(name);
			string.postcs;

			if(receiver.isNil) {
				"OpenObject: name: % not found".format(name).warn;
			} {
				object = string.interpret;
				object !? { receiver.source_(object); ok = 1; };
			}

			^ok
	}

	// evaluate an array of strings and return the results

	*interpretOSC { |msg|
			^msg.join.interpret
	}

	// send an array of values back to a sender

	*sendReply { |addr, id, args|
		args = args.asOSCArgArray;
		replyPort !? { addr.port = replyPort };
		addr.sendMsg("/oo_reply", id, *args);
	}

}



/////////////////////// method extensions ///////////////////////

+ Object {

	publish { |name|
		if(OpenObject.objects.at(name).notNil) {
			"OpenObjects: overriding object with this name: %".format(name).warn
		};
		OpenObject.put(name, this)
	}

	unpublish { |name|
		OpenObject.remove(this)
	}

}

+ Ndef {

	*at { arg key;
		^this.dictFor(Server.default).at(key)
	}

}
