/*
\limiter

A lookahead transparent limiter.

limit: in dB.lookAhead: lookahead time in seconds. The signal will be delayed by this amount of time, to give the limiter the chance to eliminate peaks from it. Lower lookahead times will result in faster response, but also more side-effects. lookAhead is an 'init' parameter (recognized by the "(i)" in the UChain editor), which means changed values only become effective when the event is started the next time.

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \limiter, { |limit = 0, lookAhead = 0.01|
	var in;
	in = UIn.ar(0,1);
	in = Limiter.ar( in, limit.dbamp, lookAhead );
	UOut.ar( 0, in );
})
	.category_( \dynamics )
	.setSpec( \limit, [ -60, 0, \lin, 1, 0].asSpec )
	.setSpec( \lookAhead, [ 0.001, 0.1, \exp, 0, 0.01].asSpec, \init );