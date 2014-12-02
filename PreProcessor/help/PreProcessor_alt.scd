this.preProcessor = PreProcessor.new;


(
// a little pseudo-lisp language with no syntax safety nets.
e = (
	lang: \lisp,
	languages:
		(
		test: { |code, event| code.quote.postln; },
		lisp: // code is the incoming string, event is the event for parameters and output
			{|code, event| 			
			var stack, char, op, i = 0, j, res, size = code.size;
			while { i.notNil and: { i < size } } {
				char = code.at(i);
				
				if(char == $( /*)*/) {
					j = code.find(" ", offset: i + 1); // next segment
					if(j.isNil) { Error("wrong syntax: %".format(code)) };
					op = code[i..j].asSymbol;
					i = j;
				};
				if(/*(*/char == $)) {
					res = op.performList(stack);
					stack = nil;
				};
				
				j = code.find(" ", offset: i + 1); // next segment
				"///////////////////".post; code[i..j].postln;
				stack = stack.add(code[i..j].asInteger);
				i = j;
			};
			
			event.put(\result, res)
			}
		)
);
);

e = <%(+ 4 3 5 ) %>.value(e);
e


(
// a little rotate language with no syntax safety nets.
// these functions could be looked up in future somewhere else of course!
e = (
	lang: \rot,
	languages: 
		(rot:
			{|code, event|
			var where, parts, n;
		
			#where, n, parts = code.split($/);
			event.put(where.asSymbol, parts.rotate(n.asInteger))
			},
		 rot47:
			{|code, event|
			var where, parts, ciph;
			#where, parts = code.split($/);
			ciph = parts.as(Array).collect(_.ascii) + 47 % 256;
			event.put(where.asSymbol, ciph.collect(_.asAscii).join)
			}
		)
);
);


// preprocessor returns a function that takes an event as arg
// put the result in x, rotate by 3.
e = <%x/3/lisp != psil%>.value(e);

// put the result in y, rotate by -2. 
e = <%y/-2/hallo world%>.value(e); 

// change the language
e.lang = \rot47;
e = <%z/hallo world%>.value(e); 

// remove preprocessor

this.preProcessor = nil;
