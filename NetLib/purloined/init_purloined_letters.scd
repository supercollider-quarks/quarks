
(


NetAddr.broadcastFlag = true; // switch on broadcast

q.quitGame; // quit any old games..


q.findValuesForKey = { |q, commandLine, key, delimiter=$ |
		var j, k, indices, res, keySize;
		key = key ++ delimiter;
		keySize = key.size;
		Pipe.do(commandLine, { |l|			
			indices = l.findAll(key);
			indices !? {
				indices.do { |j|
					j = j + keySize;
					while { l[j] == delimiter } { j = j + 1 };
					k = l.find(delimiter.asString, offset:j) ?? {Êl.size } - 1;
					res = res.add(l[j..k])
				};
			};
		});
		res
};

q.addr = {
		var  res;
		res = q.findValuesForKey("ifconfig", "broadcast");
		if(res.size > 1) { postln("the first of those devices were chosen: " ++ res) };
		NetAddr(res.first, 57120)
}.value;

// in the case the network doesn't work, test with yourself:
//q.addr = NetAddr("127.0.0.1", 57120);


// stamp is a letter id.
q.makeStamp = {
	100000.0.rand
};

q.responderActive = { |q, resp|
	OSCresponder.all.includes(resp)
};

// this listens to every letter.
q.logResponder = OSCresponderNode(nil, \recieveLetter, { |t, r, msg|
		var letter = msg[3];
		var nickname = msg[1];
		var receiver = msg[5];
		q.logLetter(letter, nickname, receiver);
}).add;

// this responds if someone asks for a receiver
q.answerer = OSCresponder(nil, \anyone, { |t,r,msg|
	var id = Date.seed, resp;
	
	resp = OSCresponderNode(nil, \recieveLetter, { |t, r, msg|
		var nickname = msg[1];
		var recvID = msg[2];
		var letter = msg[3];
		var stamp = msg[4];
		if(recvID == id) {
			resp.remove;
			q.openLetter(letter, nickname, stamp);
		};
	});
	resp.add;
	fork {
		1.0.rand.wait; // random answer..
		q.addr.sendMsg(\me, q.nickname, msg[1], id);
		1.0.wait;
		if(q.responderActive(resp)) { "sender changed mind.".postln; resp.remove } 
	}
	
}).add;

// this asks whether someone wants a letter
q.askForSomeone = { |q, letter, stamp|
	var id = Date.seed, resp;
	
	resp = OSCresponder(nil, \me, { |t,r,msg|
				var waitID = msg[2];
				var receiver = msg[1];
				
				if(waitID == id) {
					resp.remove;
					q.sendLetter(msg[3], letter, stamp, receiver);
				}
	});
	resp.add;
	q.addr.sendMsg(\anyone, id);
	SystemClock.sched(1.4, { if(q.responderActive(resp) and: {q.responderActive(q.answerer)}) 
		{ "noone wants it".postln; resp.remove } });
};

// actually send the letter
q.sendLetter = {|q, id, letter, stamp, receiver|
	q.addr.sendMsg(\recieveLetter, q.nickname, id, letter, stamp, receiver)

};

// logging
q.logPath = "purloined_log_" ++ Date.getDate.stamp ++ ".scd";
q.logFile = File(q.logPath, "w");

q.logLetter = {|q, letter, nickname, receiver|
	var str = format("// % wrote to %:\n\n%\n\n", nickname, receiver, letter);
	if(q.logFile.isOpen) {
		q.logFile.write(str);
	};
	
};

// create a document from a letter
q.openLetter = {|q, letter, nickname, stamp|
	var doc;
	defer {
		doc = Document((nickname ? "letter").asString, letter.asString);
		doc.background = Color(stamp.wrap(0.8, 0.9), stamp.wrap(0.7, 0.9), stamp.wrap(0.8, 1.0));
		doc.front;
		doc.promptToSave = false;
		doc.onClose_ { 
			q.askForSomeone(doc.text, stamp);
			q.allDocs.remove(doc);
		};
		q.allDocs = q.allDocs.add(doc);
	};
};

q.explanation = {
	"// this purloined letter works like this:\n"
	"// play it, edit it, and if you are done, send it to the next by closing\n"
	"(\n\n\n\n\n)\n"
};

// quit all
q.quitGame = {
	q.logResponder.remove;
	q.answerer.remove;
	q.addr.disconnect;
	q.logFile.close;
};

// start a letter
q.startLetter = {
	if(q.allDocs.size <= 1) {
		q.askForSomeone(q.explanation, q.makeStamp);
	} {
		"letter box full. send off existing letters first".postln;
	};
};
);


/*
thisProcess.recvOSCfunc = { |...a| a.postln };
thisProcess.recvOSCfunc = nil;
*/