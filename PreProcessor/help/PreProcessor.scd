this.preProcessor = PreProcessor.new;


(
// a little pseudo-lisp language with no syntax safety nets.
// not guaranteed to work in all cases.
e = (
	lang: \lisp,
	languages:
		(
		lisp: // code is the incoming string, event is the event for parameters and output
			{|code, event| 			
			var stack, mem, argument, calculated, char, i = 0, j = 0, selector;
			
			event.use {
			// some cleanup
			// guarantee blanks after parentheses
			code = 
				code	.replace("( ", "(")
					.replace("(", "( ")
					.replace(" )", ")")
					.replace(")", " )")
					.replace("  ", " ");			
			
			while { i.notNil and: { i < code.size }} {
				char = code[i];
				switch(char,
				
						$(, /*)*/ 
						{
							
							stack = stack.add(mem);
							j = code.find(" ", offset: i);
							i = code.find(" ", offset: j + 1);
							mem = List.new;
							mem.add(code[j..i - 1].replace(" ", "").asSymbol);
							
							j = i;
							
						},
						
						/*(*/ 
						$), {
								
							calculated = mem[1..].reduce(mem[0]);
							mem = stack.pop;
							mem.add(calculated);
								
							i = i + 1;
							j = i;
						},
						
						$ , {
							argument = code[j..i].interpret;
							argument !? { mem = mem.add(argument) };
							
							j = i;
							i = i + 1;
						},
						
						{
							i = i + 1;
						}
				);
			};
				
				event.put(\result, calculated)
			}
			}
		)
);
);

// ok, assuming such a little interpreter exists for a given language,
// one can now use it in code.

(
e = <%   (+ 4 3 ( + 2 (- 4 3) ) (/ 10 2))    %>.value(e); // 4 + 3 + (2 + (4 - 3)) + (10 / 2)
e.result;
)

// values can be passed in as environment variables:
(
e.use { ~x = 8; ~y = 9 };
e = <% (* ~x ~y) %>.value(e);
e.result;
);

// remove preprocessor

this.preProcessor = nil;
e = nil;

