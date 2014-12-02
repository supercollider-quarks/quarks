

(
	add: { arg reply, name, code ... rates;
		var func;
		func = code.compile.value;
		SynthDef(name, func, rates ).add;
		// precise confirmation of arrival
		// not determined
		reply.value
	},
	remove: { arg reply, name;
		SynthDef.removeAt(name);
		reply.value;
	}
);


// list synthDefDir
// load specific ones from there

