// python preprocessor, given that you have python installed
// thanks to  tasos tsesmetzis

(
this.preProcessor = PreProcessor.new.startDelimiter_("{-").endDelimiter_("-}");

// a simple python call
e = (
	lang: \python,
	languages:
	(
		python: // code is the incoming string, event is the event for parameters and output
		{|code, event|
			var input, pipe, output;
			code = e[\prefix] ++ "print" ++ "(" ++ code ++ ")";
			input = "python -c '%'".format(code);
			pipe = Pipe(input, "r");
			output = pipe.readUpTo;
			pipe.close;
			event.put(\result, output)
		}
	),
	prefix: "import math;"
);
);

// ok, assuming such a little interpreter exists for a given language,
// one can now use it in code.

(
e =
{-
	math.factorial(61)
-}.value(e);
e.result;
)

e.calc_result = e.result.as(Array).collect { |x| x.asString.interpret };

Pbind(\note, Pseq(e.calc_result), \dur, 0.1).play;