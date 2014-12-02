
+ TempoClock {
		// for debugging purposes
		// this will generate A LOT of information!
	dumpQueue {
		queue.clump(2).do({ |pair|
			("\n" ++ pair[0]).postln;
			pair[1].dumpFromQueue;
		});
	}
}


+ Function {
	dumpFromQueue {
		("Arguments: " ++ this.def.argNames).postln;
		("Variables: " ++ this.def.varNames).postln;
		this.def.dumpByteCodes;
	}
}

+ Object {
	dumpFromQueue {
		this.dump
	}
}
