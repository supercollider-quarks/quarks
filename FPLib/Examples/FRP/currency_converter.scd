//convert Euros to Pounds
//using signals
(
//Setup GUI
var nb1 = NumberBox();
var nb2 = NumberBox();

w = Window(bounds:Rect(200,200,200,100)).layout_(
	HLayout( StaticText().string_("Euro:"), nb1, StaticText().string_("Pound:"), nb2 )
    );

//Setup Event Network
//network
~networkDescription = ENDef({
	var rate = 0.8;

    //euros
    var nb1S = nb1.enIn;
	//pounds
	var nb2S = nb2.enIn;

	//euros to pounds
	var to = { |x| x*rate }.lift.( nb1S );
	//pounds to euros
	var from = { |x| x/rate }.lift.( nb2S );

	nb1.enSinkValue( from );
	nb2.enSinkValue( to );

});

//compile network
~network = EventNetwork(~networkDescription);

//display guis and start network
~network.start;
w.front;
)