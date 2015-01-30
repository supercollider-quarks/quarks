

code::


/*

Event network:

Currently only for EventStream

//Inputs
// GUIs - Subclasses of VIew -> .asENInput
// EventSource -> .asENInput
// MKtlElement -> .asENInput

//outputs
// anything that responds to value_ -> es.connectEN(object)
// other EventStream[IO[Unit]] -> .reactimate
// 

//still some bugs:

adding and removing twice.

*/

(
//inputs
x = EventSource();
y = Var(1.0);
//collected all the reactimates
~networkDescription = 
	x.asENInput >>= { |x| 
	y.asENInput >>= { |y|
	x.collect{ |v| putStrLn(" es out : "++v) }.reactimate >>=|
	y.collect{ |v| putStrLn(" var out : "++v) }.reactimate
	}};
//compile network
~network = EventNetwork(~networkDescription);
//start network
~network.actuateNow;
)

(
//inputs

x = EventSource();
y = Var(1.0);
//collected all the reactimates
~networkDescription = Do(
	x <- x.asENInput;
	y <- y.asENInput;
	x.collect{ |v| putStrLn(" es out : "++v) }.reactimate;
	y.collect{ |v| putStrLn(" var out : "++v) }.reactimate
);
//compile network
~network = EventNetwork(~networkDescription);
//start network
~network.actuateNow;
)


	
x.fire(1.0)
y.value_(4.5)
~network.pauseNow;


//GUI
(

~slider = Slider().value_(0.3).front;
//network
~networkDescription = 
	//inputs
	~slider.asENInput >>= { |x| 
		var r = x.collect(_*100);
		//outputs
		r.collect{ |v| putStrLn(v) }.reactimate;
	};
//compile network
~network = EventNetwork(~networkDescription);
//start network
~network.actuateNow;
)

~network.pauseNow;

//GUI
(
~slider = Slider();
//network
~networkDescription = Do(
	x <- ~slider.asENInput;
	let r = x.collect(_*100);
	r.collect{ |v| putStrLn(v) }.reactimate
);

//compile network
~network = EventNetwork(~networkDescription);
//start network
Do(
	~network.actuate;
	~slider.front;
);
)

~network.pause;


//MIDI
(
//Guis
~slider = Slider().front;

//MIDI
k = MIDIMKtl('nnkn0');

//network
~networkDescription = 
	//inputs
	k.at(\sl_1_1).asENInput >>= { |x|
	k.at(\sl_2_1).asENInput >>= { |y|
	k.at(\sl_3_1).asENInput >>= { |z|
	
	//event network and outputs
	var yyy,xxx = x.collect{ |v| v * 100 };
	xxx.collect{ |v| putStrLn(v) }.reactimate >>=|
	
	yyy = y.collect{ |v| "here is the y"+v }
	y.collect{ |v| putStrLn(v) }.reactimate >>=|
	z.collect{ |v| putStrLn(v) }.reactimate >>=|
	y.connectIO(~slider).reactimate

	}}};
~network = EventNetwork(~networkDescription);
~network.actuateNow;
)


~network.actuateNow;
~network.pauseNow;


//GUI
(
//Setup GUI
var sl1 = Slider();
var sl2 = Slider();
var sl3 = Slider();
w = Window(bounds:Rect(200,200,200,200)).layout_(
	    HLayout( sl1, sl2 )
    );

//Setup Event Network
//network
~networkDescription = 
	//inputs
	sl1.asENInput >>= { |x| 
		var r = x.collect(_*100);
		r.collect{ |v| putStrLn(v) }.reactimate >>=|
		x.collect{ |v| 1-v }.connectEN(sl2) >>=|
		x.collect{ |v| sl3.setPropIO(\moveTo, 100+(v*200), 300) }.reactimate;
	};
	
//compile network
~network = EventNetwork(~networkDescription);

//display guis and start network
~result = ~network.actuate >>=| w.frontIO >>=| sl3.frontIO;
//side effects happen.... NOW !
~result.unsafePerformIO
)

/*
Notes on doing pure gui code:

All property sets have to be be done with IO. How to do it ?

gui.setProperty(\resizeTo, 100@200)

*/

x  = Slider().front
x.setPropIO(\close).unsafePerformIO
















/*
-- | A value of type @AddHandler a@ is just a facility for registering
-- callback functions, also known as event handlers.
-- 
-- The type is a bit mysterious, it works like this:
-- 
-- > do unregisterMyHandler <- addHandler myHandler
--
-- The argument is an event handler that will be registered.
-- The return value is an action that unregisters this very event handler again.
type AddHandler a = (a -> IO ()) -> IO (IO ())

type AddHandler'     = AddHandler InputValue

type Preparations t  =
    ( [Event t (IO ())]     -- reactimate outputs
    , [AddHandler']         -- fromAddHandler events 
    , [IO InputValue]       -- fromPoll events
    , [IO ()]               -- liftIOLater
    )

*/
::