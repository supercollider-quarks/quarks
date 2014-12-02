

(
	interpret: { arg reply, code;
		var result;
		result = thisProcess.interpreter.interpret(code.asString);
		reply.value( JSON.stringify(('result':result)) );
	},
	executeFile: { arg reply, path;
		var result = thisProcess.interpreter.executeFile(path.asString);
		reply.value( JSON.stringify(('result':result)) );
	},
	play: { arg reply, code, outbus, fadeTime, addAction;
		// wraps it in play
		// { code }.play()
		var func, result;
		func = thisProcess.interpreter.compile(code.asString);
		result = func.play(outbus, fadeTime, addAction);
		if(result.isKindOf(Synth),{
			result = result.nodeID;
		});
		reply.value( JSON.stringify(('result':result)) );
	}
);

