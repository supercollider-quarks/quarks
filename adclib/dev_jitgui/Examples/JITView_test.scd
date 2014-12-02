j = JITView(123, nil, Rect(10, 500, 300, 24)); // can enter new value
// j.parent.bounds_(Rect(10, 500, 300, 100)); // crashes sclang! - why?
j.dump;
j.label_(\yo);
j.label_(\yoyoYO);

Ndef

// value can be anything
j.value_([30, 300]);
j.value_([30, 300] * 3);
// floats look like floats
j.value_([30, 300] * 3.0);

j.action.add(\test, { |jv| "jv value is: %\n".postf(jv.value) });
j.doAction;

/* BASIC display mode is 'code' */
j.mode_(\code);
j.mode_(\number);

j.value_(\symbol).refresh;
j.value_(nil).refresh;
j.value_('karli').refresh;


j.valueAction_(\symbol);
j.valueAction_(nil);
j.valueAction_('karli');


// NUMBER display, use MView (Modal/Multi-View). It can show 1, 2, or many numbers

j = MView(nil, nil, Rect(10, 500, 300, 24)); // can enter new value
j.putDict(\myspec, [0, 10].asSpec);

j.putDict(\myspec, \freq.asSpec.copy.step_(0.01)).refresh;
j.value_([50, 500]);
j.putDict(\knobCol, Color.grey(0.0, 0.3));

j.uv.mouseMoveAction

j.putDict(\shiftMode, \clip);
j.putDict(\shiftMode, \stop);

j.value = [30, 300] * 10; j.refresh;
j.value = [60, 600, 6000]; j.refresh;
j.value = [20, 20000]; j.refresh;

// single number behavior:
j.valueAction = 600;

// multi number
j.value_((1..9));
j.putDict(\myspec, [0, 10, \lin, 0.01].asSpec);

/*
*	grabbing slider and moving it sets value and does action
*	x sets to minval,
	m to maxval,
	c to center,
	r to random value
*/

// multi-number behavior:
j.valueAction = [60, 600, 6000];

/*
*	grabbing slider and moving it sets value and does action
*	clicking onto a single dot, one can move just that dot
* 	clicking on the right margin area rescales values by maxval
* 	clicking on the left margin area rescales values by minval
* 	clicking anywhere else shifts range of entire array of values

	QUESTION: should x, m, c, r be disabled or adapted?
	x = shift all vals so that minItem is minval
	m = shift all vals so that maxItem becomes maxval
	c = shift so mean(minItem, maxItem) = 0.5
	r = randomize all values
*/

[123.4560000, 789.0].postcs;

MFunc
// compare behavior with EZGui variants:
(
w = Window("test", Rect(20, 500, 320, 140)).front;
w.addFlowLayout;

j = JITView(123, w, Rect(10, 30, 300, 24));
j.putDict(\label, \freq, \myspec, \freq.asSpec.copy.step_(0.01));
j.number;

k = EZText(w, Rect(10, 40, 300, 24), \code, labelWidth: 30);
l = EZSlider(w, Rect(10, 40, 300, 24), \freq, \freq, labelWidth: 30);
m = EZRanger(w, Rect(10, 40, 300, 24), \freq, \freq, labelWidth: 30);
)

NdefMixer(s)

// example for future EnvirGui2
e = EnvirGui2();
a = ();
e.object = a;

a.put(\abc, \def);

a.put(\def, 123);
a.put(\ghi, 456);
e.valFields[2].number;
a.put(\ghi, nil);

a.put(\def, [123, 456]);

e.valFields[1].refresh;
e.getState.object == a;



//
e.setField(0, \abc, \def);
e.setField(0, \abc, \otto);
e.setField(0, \abc, 123);

e.addReplaceKey(\abc, \ghi);
e.showKeyFor(\abc);

e.setField(1, \def, \adc);

e.highlight(4, "sl4");
