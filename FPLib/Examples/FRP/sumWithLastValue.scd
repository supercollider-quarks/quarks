//sum with last value
//recursive expressions
/*

frp in haskell can express mutually recursive values
This allows a value to depend on it's own past value:

reactive-banana
sum to the last value:

e2 = (+) <$> b <@> e1
b = stepper 0 e2

unfortunatelly this cannot be done this way in SuperCollider so

f <$> b <@> e

must be changed to

e.apply(f <$> b)

e.collect{ |a| f(a, b.now) }

*/
(
//Setup GUI
var nb1 = NumberBox();
var nb2 = NumberBox();

w = Window(bounds:Rect(200,200,200,100)).layout_(
	HLayout( StaticText().string_("In:"), nb1, StaticText().string_("Out:"), nb2 )
    );

//Setup Event Network
//network
~networkDescription = ENDef({
	var sumSig, sum;

    //in
    var nb1S = nb1.enInES;

	sum = { |a| a + sumSig.now } <%> nb1S;
	sumSig = sum.hold(0);

	//out
	nb2.enSinkValue( sumSig );

});

//compile network
~network = EventNetwork(~networkDescription);

//display guis and start network
~network.start;
w.front;
)