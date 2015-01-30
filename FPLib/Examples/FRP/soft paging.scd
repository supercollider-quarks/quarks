/*

Example of setting up paged controls

Notice that changing page doesn't cause new values to be emmited.

*/



(

q = ();

//setup gui
q.sl = Slider();
q.but1 = Button();
q.but2 = Button();
q.text = StaticText();
q.window = Window().layout_(HLayout(q.sl, q.but1, q.but2, q.text));

q.defaults = { rrand(0.0,1.0) } ! 10;

q.numPages = q.defaults.size;

q.network = EventNetwork(ENDef({
	//get guis as event streams
	//for this case event streams are better then signals
	var slE = q.sl.enInES;
	var but1E = q.but1.enInES;
	var but2E = q.but2.enInES;

	//page counter
	var increasePage = { |x| (x + 1).clip(0, q.numPages-1) };
	var decreasePage = { |x| (x - 1).clip(0, q.numPages-1) };

	var page = but1E.collect{ decreasePage }
	.merge(but2E.collect{ increasePage })
	.injectF(0);

	//display page
	var dpage = q.text.enSink( page.collect{ |x| [\string_, "page : %".format(x+1)] } );

	//page.enDebug("page");

	//paged slider
	var x = slE.paged(page, q.defaults);
	x.at1.collect{ |es, i|
		es.enDebug("page "++(i+1))
	};

	//x.at1 is an array of event sources, one per page
	//x.at2 is an event source which fires when there is a page change
	//and carries the value of the control for the page which became current

	//reset slider on page change
	q.sl.enSinkValue( x.at2 );

}),true);

q.window.front;
q.network.start;

)

(
q.network.free;
q.window.close;
)

