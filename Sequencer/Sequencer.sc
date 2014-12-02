
/*

// WARNING: on older versions of sc, there is a (usually small) memory leak.
// This is experimental. It is not block accurate, due to OSC communication
// But it works reasonably well.



{ Sequencer.kr({ 100.rand }, Impulse.kr(10)).poll }.play;
{ Sequencer.kr({ |mx| mx.rand }, Impulse.kr(10), MouseX.kr(0, 100)).poll }.play;
(
{ Sequencer.kr(
	{ |mx, my| [mx.rand, my] }, 
	Impulse.kr(10), 
	[MouseX.kr(0, 100), MouseY.kr(-100, 0)]
).poll }.play;
)

b = Buffer.alloc(s, 1000);
(
{ 
	var str = Pseq([0, 1], inf).asStream, 
	t = Impulse.ar(100), 
	seq = Sequencer.ar({ str.next }, t); 
	RecordBuf.ar([seq, t], b, loop: 0); 
	
	Line.kr(1, 0, 1, doneAction:2) 
}.play;
)

b.plot;

// control rate
(
{
	var t = Impulse.kr(2.3);
	var str = (Pseq([0, 12], inf) + Pstutter(Prand([2, 5], inf), Pwhite(60, 70, inf))).asStream;
	var freq = Sequencer.kr({ str.next }, t).midicps;
	SinOsc.ar(freq) * 0.2 + (Decay.kr(t, 0.01) * PinkNoise.ar(2))
}.play;
)

// audio rate
(
{
	var t = Impulse.ar(MouseX.kr(2, 100, 1));
	var str = (Pseq([0, 12], inf) + Pstutter(Prand([2, 5], inf), Pwhite(60, 70, inf))).asStream;
	var freq = Sequencer.ar({ str.next }, t).midicps;
	SinOsc.ar(freq) * 0.2 + (Decay.ar(t, 0.01) * PinkNoise.ar(2))
}.play;
)

*/




SynthDefResources {
	classvar <all, responder;
	
	*initClass {
		all = IdentityDictionary.new;
	}
	
	*makeResponder {
		if(responder.isNil) {
			responder = OSCFunc({ |msg, time, addr| this.remove(msg[1], addr) }, "/d_removed").fix
		}
	}
	
	*add { |defName, func|
		this.makeResponder;
		all[defName] = all[defName].addFunc(func)
	}
	
	*remove { |defName, addr|
		all.removeAt(defName).value(addr)
	}
}


Sequencer {
	
	classvar <functions;
	classvar cmd, responder;
	
	*initClass {
		var langID = inf.asInteger.rand; // make sure to look up function in the right client
		cmd = "/sequencer" ++ langID;
		functions = IdentityDictionary.new;
	}
	
	// todo: multichannelExpand

	*kr { |func, trig, args|
		^this.new(\control, func, trig, args)	
	}
	
	*ar { |func, trig, args|
		^this.new(\audio, func, trig, args)	
	}
	
	*new { |rate, func, trig, args|
		var name = UGen.buildSynthDef.name.asSymbol;
		var replyID = UniqueID.next;
		
		functions[replyID] = func;
		SynthDefResources.add(name, { functions.removeAt(replyID) });
		
		this.makeResponders(cmd);
		
		SendReply.perform(
			UGen.methodSelectorForRate(rate), 
			trig, cmd, args, replyID
		);
		
		^NamedControl.perform(
			UGen.methodSelectorForRate(rate), 
			"sequencer_in_" ++ replyID, 0.0 ! args.size.max(1)
		);
	}
	
	*respond { |msg, addr|
		var res, nodeID, replyID, args, func = functions[msg[2]];
		if(func.notNil) {
			#nodeID, replyID ... args = msg[1..];
			res = func.valueArray(args).asOSCArgArray.asArray;
			if(res.notNil) {
				addr.sendMsg(*["/n_setn", nodeID, "sequencer_in_" ++ replyID, res.size] ++ res)
			}
		}
	}
	
	*makeResponders { |cmd|
		if(responder.isNil) {
			responder = OSCFunc({ |msg, time, addr| this.respond(msg, addr) }, cmd).fix;
		}
	}

}


/*

// lang test
"top".runInTerminal;

{ Sequencer.kr({ 100.rand }, Impulse.kr(MouseX.kr(0, 1000))) }.play;

Sequencer.functions
SynthDefResources.all
*/


