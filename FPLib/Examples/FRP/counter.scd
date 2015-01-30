(

~but1 = Button();
~but2 = Button();
~text = StaticText();

w = Window(bounds:Rect(200,200,200,200)).layout_(
        HLayout( ~but1, ~but2, ~text )
    );

//network
~networkDescription = ENDef({
    //inputs
    var but1E = ~but1.enInES;
	var but2E = ~but2.enInES;

	var counter = but1E.collect{ { |x| x - 1 } }
	.merge( but2E.collect{ { |x| x + 1 } } )
	.injectF(0);

	~text.enSink( counter.collect{ |x| [\string_, x] } );

});
//compile network
~network = EventNetwork(~networkDescription, true);

//start network
~network.start;
w.front;
)