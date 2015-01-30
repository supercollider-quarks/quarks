(

~but1 = Button();
~but2 = Button();
~text = StaticText();

w = Window(bounds:Rect(200,200,200,200)).layout_(
        HLayout( ~but1, ~but2, ~text )
    ).front;

//network
ENdef(\x, {
    //inputs
    var but1E = ~but1.enInES;
	var but2E = ~but2.enInES;

	var counter = but1E.collect{ { |x| x - 1 } }
	.merge( but2E.collect{ { |x| x + 1 } } )
	.injectF(0);

	~text.enSink( counter.collect{ |x| [\string_, x] } );

}).start;
)

(
ENdef(\x, {
    //inputs
    var but1E = ~but1.enInES;
	var but2E = ~but2.enInES;

	var counter = but1E.collect{ { |x| x *2 } }
	.merge( but2E.collect{ { |x| x /2 } } )
	.injectF(1);

	~text.enSink( counter.collect{ |x| [\string_, x] } );

})
)

