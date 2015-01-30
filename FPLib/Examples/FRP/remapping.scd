(
q = ();

w !? _.close;
q.knobs = 4.collect{ Knob() };
q.labels = ["SinOsc","Saw","LFNoise","WhiteNoise"];
q.numboxes = 4.collect{ NumberBox() };
q.texts = q.labels.collect{ |st| StaticText().string_(st) };
q.button = Button().states_([[0],[1]]);

q.window = Window(bounds:Rect(200,200,200,100)).layout_(
	VLayout(
		HLayout( *q.knobs ),
		HLayout( *q.numboxes ),
		HLayout( *q.texts ),
		q.button
	)
).front;

q.synths = [
	{ |freq=400| Pan2.ar( SinOsc.ar(freq)*0.2, -1.0) }.play,
	{ |freq=400| Pan2.ar( Saw.ar(freq)*0.2, -0.2) }.play,
	{ |freq=400| Pan2.ar( LFNoise0.ar(freq)*0.2, 0.6) }.play,
	{ |freq=400| Pan2.ar( LPF.ar( WhiteNoise.ar, freq)*0.6, 1.0) }.play
];

CmdPeriod.add{ q.window !? _.close };


EventNetwork(ENDef({

	var knobs = q.knobs.collect(_.enInES);
	var knobsSig = knobs.collect(_.hold(0.0));
	var but = q.button.enInES.collect(_.booleanValue);
	var butSig = but.hold(false);

	//**** FRP LOGIC *****

	//selection mode is entered when ~but is set to 1
	//it is exited when two different knobs are moved

	var exchange = (
		but.select({ |x| x}).collect{ { [] } } |
		knobs.collect{ |es,i|
			es.collect{
				{ |state|
					state !? { |xs|
						switch(xs.size)
						{0}{[i]}
						{1}{ if(xs[0] != i){xs++[i]}{xs}}
						{nil}
					}
				}
			}
		}.mreduce
	).injectF(nil)
	.select({ |xs| xs.size == 2})
	.enDebug("exchange");

	//index i of this array is the knob that will go into action i;
	var startState = [0,1,2,3]; //which knobs go to which action

	var currentOrder = exchange.injectSig(startState, { |state, exchangePair|
		var indexA = state.indexOf(exchangePair.postln[0]);
		var indexB = state.indexOf(exchangePair[1]);
		state.swap(indexA, indexB);
	}).enDebug("currentOrder");

	var knobsArray = knobs.collect{ |es| butSig.collect(_.not).when(es).hold(0.0) }.sequence;
	var outputsArray = ({ |order, values|
		values[order]
	} <%> currentOrder <*> knobsArray)
	.enDebug("outputs");

	var outputs = 4.collect{ |i|
		outputsArray.collect{ |xs| xs[i] }
	};

	// **** FRP LOGIC STOP *****

	//set synth
	[outputs, q.synths].flopWith{ |sig, synth|
		synth.enSetSink(\freq,  sig.linlin(0.0,1.0,200.0,2000.0) )
	};

	//set GUIs
	currentOrder.collect{ |array|
		IO{
			[array, q.labels].flopWith{ |i, label|
				defer{ q.texts[i].string_(  label ) }
			}
		}
	}.enOut;

	[q.numboxes, outputs].flopWith{ |box, sig|
		box.enSinkValue( sig )
	}

},true)).start

)